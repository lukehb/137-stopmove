package onethreeseven.stopmove.algorithm;

import onethreeseven.common.util.Maths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * todo: move this into the Maths class
 * @author Luke Bermingham
 */
public class Maths2 {


    public static double mode(double[] data){
        HashMap<Double, Integer> tally = new HashMap<>();

        for (double v : data) {
            int nOccurrences = tally.getOrDefault(v, 0) + 1;
            tally.put(v, nOccurrences);
        }

        Optional<Map.Entry<Double, Integer>> modalOpt =
                tally.entrySet().stream().max((o1, o2) -> Integer.compare(o1.getValue(), o2.getValue()));

        if(modalOpt.isPresent()){
            return modalOpt.get().getKey();
        }

        return Double.NaN;
    }

    /**
     * Smooth the data using a gaussian kernel.
     * @param data The data to smooth.
     * @param n The size of sliding window (i.e number of indices either side to sample).
     * @return The smoothed version of the data.
     */
    public static double[] gaussianSmooth(double[] data, int n){
        double[] smoothed = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            int startIdx = Math.max(0, i - n);
            int endIdx = Math.min(data.length - 1, i + n);

            double sumWeights = 0;
            double sumIndexWeight = 0;

            for (int j = startIdx; j < endIdx + 1; j++) {
                double indexScore = Math.abs(j - i)/(double)n;
                double indexWeight = Maths.gaussian(indexScore, 1, 0, 1);
                sumWeights += (indexWeight * data[j]);
                sumIndexWeight += indexWeight;
            }
            smoothed[i] = sumWeights/sumIndexWeight;
        }
        return smoothed;
    }

    /**
     * Smooth the data using a gaussian kernel.
     * @param data The data to smooth.
     * @param w The size of sliding window (i.e number of indices either side to sample).
     * @return The smoothed version of the data.
     */
    public static double[][] gaussianSmooth2d(double[][] data, int w){
        final int dataSize = data.length;

        if(dataSize == 0){
            throw new IllegalArgumentException("Cannot smooth empty data.");
        }

        final int nDims = data[0].length;

        if(nDims == 0){
            throw new IllegalArgumentException("Cannot smooth a data point with no values. " +
                    "Uniformly populate every entry in your data with 1 or more dimensions.");
        }

        double[][] smoothed = new double[dataSize][nDims];

        for (int i = 0; i < dataSize; i++) {
            int startIdx = Math.max(0, i - w);
            int endIdx = Math.min(dataSize - 1, i + w);

            double[] sumWeights = new double[nDims];
            double sumIndexWeight = 0;

            for (int j = startIdx; j < endIdx + 1; j++) {
                double indexScore = Math.abs(j - i)/(double)w;
                double indexWeight = Maths.gaussian(indexScore, 1, 0, 1);

                for (int n = 0; n < nDims; n++) {
                    sumWeights[n] += (indexWeight * data[j][n]);
                }
                sumIndexWeight += indexWeight;
            }

            for (int n = 0; n < nDims; n++) {
                smoothed[i][n] = sumWeights[n]/sumIndexWeight;
            }
        }
        return smoothed;
    }

    /**
     * Normalise the 1d data using min-max normalisation.
     * @see <a href="https://en.wikipedia.org/wiki/Feature_scaling#Rescaling">Wikipedia article about feature re-scaling.</a>
     * @param data The data to normalise.
     * @return The new array containing the normalised data.
     */
    public static double[] minmaxNormalise1d(double[] data){
        //find min and max value
        double curMin = Double.POSITIVE_INFINITY;
        double curMax = Double.NEGATIVE_INFINITY;
        for (double v : data) {
            if(v < curMin){
                curMin = v;
            }
            if(v > curMax){
                curMax = v;
            }
        }

        //normalise the data using min-max normalisation
        //and also subtract each value from its normalised index
        final double range = curMax - curMin;
        double[] normalisedData = new double[data.length];

        for (int i = 0; i < normalisedData.length; i++) {
            normalisedData[i] = ((data[i] - curMin) / range);
        }
        return normalisedData;
    }

    /**
     * Performs min-max normalisation on n-dimensional data (as long as the dimensionality is uniform, that is, all data is 2d or all 3d etc.).
     * @see <a href="https://en.wikipedia.org/wiki/Feature_scaling#Rescaling">Wikipedia article about feature re-scaling.</a>
     * @param data The data to normalised.
     * @return A new normalised data-set.
     */
    public static double[][] minmaxNormalise(double[][] data){

        final int dataSize = data.length;

        if(dataSize == 0){
            throw new IllegalArgumentException("Cannot smooth empty data.");
        }

        final int nDims = data[0].length;

        if(nDims == 0){
            throw new IllegalArgumentException("Cannot smooth a data point with no values. " +
                    "Uniformly populate every entry in your data with 1 or more dimensions.");
        }

        //1) get min and max for each dimension of the data

        double[] minEachDim = new double[nDims];
        double[] maxEachDim = new double[nDims];
        for (int i = 0; i < nDims; i++) {
            minEachDim[i] = Double.POSITIVE_INFINITY;
            maxEachDim[i] = Double.NEGATIVE_INFINITY;
        }

        for (double[] coords : data) {
            for (int n = 0; n < nDims; n++) {
                double v = coords[n];
                if (v < minEachDim[n]) {
                    minEachDim[n] = v;
                }
                if (v > maxEachDim[n]) {
                    maxEachDim[n] = v;
                }
            }
        }

        //2) normalise the data using the min and max
        double[] rangeEachDim = new double[nDims];
        for (int n = 0; n < nDims; n++) {
            rangeEachDim[n] = maxEachDim[n] - minEachDim[n];
        }

        double[][] outputNormalised = new double[dataSize][nDims];
        for (int i = 0; i < dataSize; i++) {
            for (int n = 0; n < nDims; n++) {
                //normalising step
                outputNormalised[i][n] = (data[i][n] - minEachDim[n]) / rangeEachDim[n];
            }
        }
        return outputNormalised;
    }

}
