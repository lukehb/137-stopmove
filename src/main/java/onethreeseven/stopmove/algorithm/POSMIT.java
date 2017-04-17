package onethreeseven.stopmove.algorithm;

import onethreeseven.common.util.Maths;
import onethreeseven.datastructures.model.STPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import onethreeseven.datastructures.model.TimeAndStop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Probabilistic stop classifier
 * @author Luke Bermingham
 */
public class POSMIT {

    private static final Logger log = Logger.getLogger(POSMIT.class.getSimpleName());

    private static final double cutoff = Maths.gaussian(3, 1, 0, 1);

    public int estimateSearchRadius(SpatioCompositeTrajectory<? extends STPt> traj, double stopVariance){
        ArrayList<Long> chunkSizes = new ArrayList<>();

        for (int i = 0; i < traj.size(); i++) {
            int prevIdx = i;

            long curChunkSize = 1;

            for (++i; i < traj.size(); i++) {
                double displacement = traj.getEuclideanDistance(prevIdx, i);
                if(displacement <= stopVariance){
                    curChunkSize++;
                }else{
                    break;
                }
                prevIdx = i;
            }

            if(curChunkSize > 1){
                chunkSizes.add(curChunkSize);
            }

        }

        //double searchRadius = new Kneedle().run(chunkSizes.stream().mapToDouble(Long::doubleValue).sorted().toArray());
        long searchRadius = Maths.mean(chunkSizes.stream().mapToLong(value -> value).toArray());

        searchRadius = Math.max(1, Math.round(searchRadius * 0.5d));
        return (int) searchRadius;
    }

    public double estimateMinStopPr(double[] stopPrs){
        UnivariateKMeans.Cluster[] clusters = new UnivariateKMeans().run(stopPrs, 2);
        double c1Max = clusters[0].getMax();
        double c2Min = clusters[1].getMin();
        return c1Max + ((c2Min - c1Max) * 0.5);
    }

    public double estimateStopVariance(SpatioCompositeTrajectory<? extends STPt> traj){
        double[] displacements = new double[traj.size()-1];
        for (int i = 1; i < traj.size()-1; i++) {
            displacements[i] = traj.getEuclideanDistance(i-1, i);
        }

        //we assume actual stops are happening somewhere between 0 and 20 meters per second
        displacements = Arrays.stream(displacements).filter(value -> value > 0 && value < 20).sorted().toArray();
        return new Kneedle().run(displacements);
    }

    public double[] run(SpatioCompositeTrajectory<? extends STPt> stTraj, int nSearchRadius){
        return run(stTraj, nSearchRadius, estimateStopVariance(stTraj));
    }

    /**
     * Run the POSMIT algorithm.
     * @param stTraj A spatio-composite trajectory.
     * @param nSearchRadius Essentially how large of a sliding window to use when calculating probabilities.
     * @param stopVariance The common speed variance within a stop (in meters per second).
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

        final double[] centerCoords = stTraj.getCoords(centerIdx, true);
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
                double[] idxCoords = stTraj.getCoords(idx, true);
                double score = score( centerCoords, idxCoords, stopVariance);
                sumWeights += (indexWeight * score);
                sumIndexWeight += indexWeight;
                bounds[i] = idx;
            }
        }
        return sumWeights/sumIndexWeight;
    }

    private double score(double[] coordsA, double[] coordsB, double stopVariance){
        double displacementMeters = Maths.dist(coordsA, coordsB);
        if(stopVariance == 0){
            return 0;
        }
        return kernel(displacementMeters/stopVariance);
    }

    private double kernel(double x){
        return Maths.gaussian(x, 1, 0, 1);
    }

}
