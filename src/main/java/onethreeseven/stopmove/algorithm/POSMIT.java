package onethreeseven.stopmove.algorithm;

import onethreeseven.common.util.Maths;
import onethreeseven.datastructures.model.STPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import onethreeseven.datastructures.model.TimeAndStop;

/**
 * Probabilistic stop classifier
 * @author Luke Bermingham
 */
public class POSMIT {

    private static final double cutoff = Maths.gaussian(3, 1, 0, 1);

    public double estimateStopVariance(SpatioCompositeTrajectory<? extends STPt> traj){
        double[] displacements = new double[traj.size()-1];
        for (int i = 1; i < traj.size()-1; i++) {
            displacements[i] = traj.getEuclideanDistance(i-1, i);
        }
        return new Kneedle().run(displacements, 0, 50);
    }

    public double[] run(SpatioCompositeTrajectory<? extends STPt> stTraj, int nSearchRadius){
        return run(stTraj, nSearchRadius, estimateStopVariance(stTraj));
    }

    /**
     * Run the POSMIT algorithm.
     * @param stTraj A spatio-composite trajectory.
     * @param nSearchRadius How many entries to search either side of any entry.
     * @param stopVariance How much a stop can spatially vary and still be considered a stop (meters).
     * @return A stop probability for each entry in the trajectory.
     */
    public double[] run(SpatioCompositeTrajectory<? extends STPt> stTraj, int nSearchRadius, double stopVariance){

        double[] stopProbabilities = new double[stTraj.size()];

        //calculate preliminary stop probabilities
        for (int centerIdx = 0; centerIdx < stTraj.size(); centerIdx++) {
            stopProbabilities[centerIdx] = getStopPr(stTraj, centerIdx, nSearchRadius, stopVariance);
        }

        return stopProbabilities;
    }

    /**
     * Converts a spatio-temporal trajectory into spatio-temporal stop/move annotated trajectory using the
     * given stop probabilities and a minimum stop probability confidence.
     * @param traj A spatio-temporal trajectory.
     * @param stopProbabilities Stop probability for each entry in the spatio-temporal trajectory.
     * @param minStopProbability The minimum probability an entry must have to be classified as a stop, otherwise it will be
     *                a move.
     * @return spatio-temporal stop/move annotated trajectory.
     */
    public STStopTrajectory toStopTrajectory(SpatioCompositeTrajectory<? extends STPt> traj,
                                             double[] stopProbabilities, double minStopProbability){
        STStopTrajectory stopTraj = new STStopTrajectory(false, traj.getProjection());

        for (int i = 0; i < traj.size(); i++) {
            boolean isStopped = stopProbabilities[i] >= minStopProbability;
            STPt time = traj.get(i);
            stopTraj.addGeographic(traj.getCoords(i, false), new TimeAndStop(time.getTime(), isStopped));
        }
        return stopTraj;
    }

    private double getStopPr(SpatioCompositeTrajectory<? extends STPt> stTraj, int centerIdx,
                             int nSearchRadius, double stopVariance){

        double[] center = stTraj.getCoords(centerIdx, true);
        //so we assume that the coordinate at centerIdx is indeed a stop
        //so now we calculate its probability using its neighbours
        double sumWeights = 0;
        double sumIndexWeight = 0;

        int lastIdx = stTraj.size() - 1;
        int[] bounds = new int[]{centerIdx, centerIdx};
        int[] increments = new int[]{-1,1};

        while(increments[0] != 0 || increments[1] != 0){
            for (int i = 0; i < increments.length; i++) {
                int increment = increments[i];
                //when increment is zero skip over the index
                if (increment == 0) {continue;}
                int idx = bounds[i] + increment;
                //if we exceed the bounds, cease expanding
                if(idx < 0 || idx > lastIdx){
                    increments[i] = 0;
                    continue;
                }
                //do the actual scoring
                double indexScore = Math.abs(idx - centerIdx)/(double)nSearchRadius;
                double indexWeight = kernel(indexScore);
                if(indexWeight < cutoff){
                    increments[i] = 0;
                    continue;
                }
                double score = score( stTraj.getCoords(idx, true), center, stopVariance);
                sumWeights += (indexWeight * score);
                sumIndexWeight += indexWeight;
                bounds[i] = idx;
            }
        }
        return sumWeights/sumIndexWeight;
    }

    private double score(double[] pt, double[] stopCentroid, double stopVariance){
        double disp = Maths.dist(pt, stopCentroid);
        if(stopVariance == 0){
            return 0;
        }
        return kernel(disp/stopVariance);
    }

    private double kernel(double x){
        return Maths.gaussian(x, 1, 0, 1);
    }

}
