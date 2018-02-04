package onethreeseven.stopmove.algorithm;

import onethreeseven.datastructures.model.STPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import onethreeseven.datastructures.model.TimeAndStop;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Implementation of the Stop/Move classifier, CB-SMoT, from
 * "A Clustering-based Approach for Discovering Interesting Places in Trajectories"
 * by Palma et al.
 * @author Luke Bermingham
 */
public class CBSMoT {

    /**
     * Classifies each point in the trajectory as either being a stop or a move
     * depending on its so-called "linear neighbourhood" (as defined by a spatial parameter, eps)
     * and the minimum duration of the cluster that is formed by expanding this neighbourhood.
     * In their original paper the authors differentiate between known and unknown stops; we
     * make a modification in this implementation as we are only interested in the unknown stops.
     * @param traj The trajectory whose points will be classified.
     * @param epsMeters How close points must be to each other to be considered neighbours.
     * @param minTimeMillis A neighbourhood of points must last at least this long to be considered stop.
     * @return A spatio-temporal trajectory with an extra dimension indicating whether points are moving or stopping.
     */
    public STStopTrajectory run(SpatioCompositeTrajectory<? extends STPt> traj, double epsMeters, long minTimeMillis){

        //make an output trajectory that has the stop meta-data, set all stops to false for now
        boolean isCartesian = traj.isInCartesianMode();
        STStopTrajectory output = new STStopTrajectory(isCartesian, traj.getProjection());

        for (STPt stPt : traj) {
            if(isCartesian){
                output.addCartesian(stPt.getCoords(), new TimeAndStop(stPt.getTime(), false));
            }else{
                output.addGeographic(stPt.getCoords(), new TimeAndStop(stPt.getTime(), false));
            }
        }

        //start the actual stop-classification algorithm
        BitSet processed = new BitSet();

        for (int idx : sortIndicesBySpeed(traj)) {
            if(processed.get(idx)){continue;}

            final int[] neighbourhood = getNeighbourhood(traj, idx, epsMeters);
            {
                int hoodSize = neighbourhood[1] - neighbourhood[0];
                if(hoodSize <= 0){continue;}
            }
            //grow the neighbourhood at the extremes
            //note: growing at the extremes is a slight optimisation over the original
            boolean[] lrCanGrow = new boolean[]{true, true};
            while(lrCanGrow[0] || lrCanGrow[1]){
                for (int i = 0; i < 2; i++) {
                    if(!lrCanGrow[i]){continue;}
                    int[] newHood = getNeighbourhood(traj, neighbourhood[i], epsMeters);
                    //if the new hood index is not equal it means it grew
                    if(newHood[i] != neighbourhood[i]){
                        neighbourhood[i] = newHood[i];
                    }else{
                        lrCanGrow[i] = false;
                    }
                }
            }
            //done growing the neighbourhood, check whether it meets the min time requirement
            long neighbourhoodDeltaTime =
                    ChronoUnit.MILLIS.between(
                            traj.get(neighbourhood[0]).getTime(),
                            traj.get(neighbourhood[1]).getTime());
            //check it we have a cluster of stops
            if(neighbourhoodDeltaTime >= minTimeMillis){
                //process all the indices in this cluster and set them as stops
                for (int i = neighbourhood[0]; i < neighbourhood[1]+1; i++) {
                    processed.set(i);
                    output.get(i).setIsStopped(true);
                }
            }
        }

        return output;
    }

    private int[] getNeighbourhood(SpatioCompositeTrajectory traj, int idx, double epsMeters){
        final int[] lrIndices = new int[]{idx,idx};
        //we do epsilon check in both directions so store sum of distance in both directions from idx
        final double[] sumDists = new double[]{0,0};
        final int lastIdx = traj.size()-1;
        //boolean array indicating if we can keep expanding indices left or right
        final boolean[] lrCanProceed = new boolean[]{idx-1 >= 0, idx + 1 <= lastIdx};
        //while we can proceed indices either left or right, do so
        while(lrCanProceed[0] || lrCanProceed[1]){
            for (int i = 0; i < 2; i++) {
                if(!lrCanProceed[i]){continue;}
                boolean isLeft = i == 0;
                int curIdx = lrIndices[i];
                int movedIdx = curIdx + ((isLeft) ? -1 : 1);
                double dist = traj.getEuclideanDistance(curIdx, movedIdx);
                double totalDistance = sumDists[i] + dist;
                //check it is within eps meters, if so, add it to the neighbourhood
                if(totalDistance <= epsMeters){
                    lrIndices[i] = movedIdx;
                    sumDists[i] = totalDistance;
                    //check that moving the indices is within bounds
                    lrCanProceed[i] = (isLeft ? movedIdx-1 >= 0 : movedIdx+1 <= lastIdx);
                }else{
                    lrCanProceed[i] = false;
                }
            }
        }
        return lrIndices;
    }

    private int[] sortIndicesBySpeed(SpatioCompositeTrajectory<? extends STPt> traj){

        //create the indices
        int[] indices = new int[traj.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        //sort using custom comparator which compares speed
        indices = Arrays.stream(indices).
                boxed().
                sorted((a, b) -> Double.compare(getSpeed(traj,a), getSpeed(traj,b))).
                mapToInt(i -> i).
                toArray();
        return indices;
    }

    private double getSpeed(SpatioCompositeTrajectory<? extends STPt> traj, int i){
        if(i == 0){
            return 0.0;
        }
        long deltaTimeMillis = ChronoUnit.MILLIS.between(traj.get(i-1).getTime(), traj.get(i).getTime());
        double dist = traj.getEuclideanDistance(i, i-1);
        return dist/deltaTimeMillis;
    }



}
