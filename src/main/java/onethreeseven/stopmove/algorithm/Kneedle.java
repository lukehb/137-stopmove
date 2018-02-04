package onethreeseven.stopmove.algorithm;

import java.util.ArrayList;

import static onethreeseven.stopmove.algorithm.Maths2.gaussianSmooth;

/**
 * Given set of values look for the elbow/knee points.
 * See paper: "Finding a Kneedle in a Haystack: Detecting Knee Points in System Behavior"
 * @author Luke Bermingham
 */
public class Kneedle {

    /**
     * Finds the indices of all local minimum or local maximum values.
     * @param data The data to process
     * @param findMinima If true find local minimums, else find local maximums.
     * @return A list of the indices that have local minimum or maximum values.
     */
    private ArrayList<Integer> findCandidateIndices(double[][] data, boolean findMinima){
        ArrayList<Integer> candidates = new ArrayList<>();
        //a coordinate is considered a candidate if both of its adjacent points have y-values
        //that are greater or less (depending on whether we want local minima or local maxima)
        for (int i = 1; i < data.length - 1; i++) {
            double prev = data[i-1][1];
            double cur = data[i][1];
            double next = data[i+1][1];
            boolean isCandidate = (findMinima) ? (prev > cur && next > cur) : (prev < cur && next < cur);
            if(isCandidate){
                candidates.add(i);
            }
        }
        return candidates;
    }


    /**
     * Find the index in the data the represents a most exaggerated elbow point.
     * @param data the data to find an elbow in
     * @return The index of the elbow point.
     */
    private int findElbowIndex(double[] data){

        int bestIdx = 0;
        double bestScore = 0;
        for (int i = 0; i < data.length; i++) {
            double score = Math.abs(data[i]);
            if(score > bestScore){
                bestScore = score;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    /**
     * Prepares the data by smoothing, then normalising into unit range 0-1,
     * and finally, subtracting the y-value from the x-value.
     * @param data The data to prepare.
     * @param smoothingWindow Size of the smoothing window.
     * @return The normalised data.
     */
    private double[][] prepare(double[][] data, int smoothingWindow){

        //smooth the data to make local minimum/maximum easier to find (this is Step 1 in the paper)
        double[][] smoothedData = Maths2.gaussianSmooth2d(data, smoothingWindow);

        //prepare the data into the unit range (step 2 of paper)
        double[][] normalisedData = Maths2.minmaxNormalise(smoothedData);

        //subtract normalised x from normalised y (this is step 3 in the paper)
        for (int i = 0; i < normalisedData.length; i++) {
            normalisedData[i][1] = normalisedData[i][1] - normalisedData[i][0];
        }

        return normalisedData;
    }

    private double computeAverageVarianceX(double[][] data){
        int sumVariance = 0;
        for (int i = 0; i < data.length - 1; i++) {
            sumVariance += data[i + 1][0] - data[i][0];
        }
        return sumVariance / (data.length - 1);
    }

    /**
     * Uses a heuristic to find what may be an elbow in the 1d data.
     * This method is a heuristic so it may return in invalid elbow.
     * If you need guarantees use the other method {@link Kneedle#run(double[][], double, int, boolean)}
     * @param data The
     * @return A possible elbow for this 1d data.
     */
    public double findElbowQuick(double[] data){
        if(data.length <= 1){
            return 0;
        }

        double[] normalisedData = Maths2.minmaxNormalise1d(gaussianSmooth(data, 3));

        //do kneedle y'-x' (in this case x' is normalised index value)
        for (int i = 0; i < normalisedData.length; i++) {
            double normalisedIndex = (double)i / data.length;
            normalisedData[i] = normalisedData[i] - normalisedIndex;
        }

        int elbowIdx = findElbowIndex(normalisedData);
        return data[elbowIdx];
    }

    /**
     * This algorithm finds the so-called elbow/knee in the data.
     * See paper: "Finding a Kneedle in a Haystack: Detecting Knee Points in System Behavior"
     * for more details.
     * @param data The 2d data to find an elbow in.
     * @param s How many "flat" points to require before we consider it a knee/elbow.
     * @param smoothingWindow The data is smoothed using Gaussian kernel average smoother, this parameter is the window used for averaging
     *                        (higher values mean more smoothing, try 3 to begin with).
     * @param findElbows Whether to find elbows or knees.
     * @return The elbow or knee values.
     */
    public ArrayList<double[]> run(double[][] data, double s, int smoothingWindow, boolean findElbows){

        if(data.length == 0){
            throw new IllegalArgumentException("Cannot find elbow or knee points in empty data.");
        }
        if(data[0].length != 2){
            throw new IllegalArgumentException("Cannot run Kneedle, this method expects all data to be 2d.");
        }

        ArrayList<double[]> localMinMaxPts = new ArrayList<>();
        //do steps 1,2,3 of the paper in the prepare method
        double[][] normalisedData = prepare(data, smoothingWindow);
        //find candidate indices (this is step 4 in the paper)
        {
            ArrayList<Integer> candidateIndices = findCandidateIndices(normalisedData, findElbows);
            //go through each candidate index, i, and see if the indices after i are satisfy the threshold requirement
            //(this is step 5 in the paper)
            double step = computeAverageVarianceX(normalisedData);
            step = findElbows ? step * s : step * -s;

            //check each candidate to see if it is a real elbow/knee
            //(this is step 6 in the paper)
            for (int i = 0; i < candidateIndices.size(); i++) {
                Integer candidateIdx = candidateIndices.get(i);
                Integer endIdx = (i + 1 < candidateIndices.size()) ? candidateIndices.get(i+1) : data.length;

                double threshold = normalisedData[candidateIdx][1] + step;

                for (int j = candidateIdx + 1; j < endIdx; j++) {
                    boolean isRealElbowOrKnee = (findElbows) ?
                            normalisedData[j][1] > threshold : normalisedData[j][1] < threshold;
                    if(isRealElbowOrKnee) {
                        localMinMaxPts.add(data[candidateIdx]);
                        break;
                    }
                }
            }
        }
        return localMinMaxPts;
    }



}
