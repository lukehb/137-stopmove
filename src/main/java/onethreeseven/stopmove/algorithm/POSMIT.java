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

    public double calculateStopVariance(SpatioCompositeTrajectory<? extends STPt> traj, int nSearchRadius){

        //find min displacement and the associated index
        double[] prevCoord = traj.getCoords(0, true);
        double[] curCoord = traj.getCoords(1, true);
        double minDisp = Double.MAX_VALUE;
        int idxOfMinDisp = 0;

        for (int i = 1; i < traj.size()-1; i++) {
            double[] nextCoord = traj.getCoords(i+1, true);
            double disp =  Maths.dist(prevCoord, curCoord) + Maths.dist(curCoord, nextCoord);
            if(disp < minDisp){
                minDisp = disp;
                idxOfMinDisp = i;
            }
            prevCoord = curCoord;
            curCoord = nextCoord;
        }

        //calculate pairwise displacement between entries

        prevCoord = traj.getCoords(idxOfMinDisp, true);
        double[] arr = new double[nSearchRadius*2];
        int n = 0;
        //go left
        int expandedLeft = 0;
        for (int i = idxOfMinDisp - 1; i >= 0; i--) {
            double[] coord = traj.getCoords(i, true);
            double disp = Maths.dist(prevCoord, coord);
            if(disp != 0){
                expandedLeft++;
                arr[n] = disp;
                n++;
                prevCoord = coord;
                if(expandedLeft >= nSearchRadius){
                    break;
                }
            }
        }
        //go right
        prevCoord = traj.getCoords(idxOfMinDisp, true);
        int expandedRight = 0;
        for (int i = idxOfMinDisp; i < traj.size() - 1; i++) {
            double[] coord = traj.getCoords(i, true);
            double disp = Maths.dist(prevCoord, coord);
            if(disp != 0){
                expandedRight++;
                arr[n] = disp;
                n++;
                prevCoord = coord;
                if(expandedRight >= nSearchRadius){
                    break;
                }
            }
        }
        //get avg disp
        if(n == nSearchRadius*2){
            return Maths.mean(arr);
        }else{
            double[] disps = new double[n];
            System.arraycopy(arr, 0, disps, 0, n);
            return Maths.mean(disps);
        }
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
        return kernel(disp/stopVariance);
    }

    private double kernel(double x){
        return Maths.gaussian(x, 1, 0, 1);
    }

}
