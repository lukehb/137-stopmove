package onethreeseven.stopmove.algorithm;

import onethreeseven.common.util.Maths;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * todo: move this into the Maths class
 * @author Luke Bermingham
 */
public class Maths2 {


    public static ArrayList<Double> mode(double[] data){
        HashMap<Double, Integer> tally = new HashMap<>();

        ArrayList<Double> modes = new ArrayList<>();
        int mostOccurrences = 0;

        for (double v : data) {
            int nOccurrences = tally.getOrDefault(v, 0) + 1;
            tally.put(v, nOccurrences);
            if(nOccurrences >= mostOccurrences){
                if(nOccurrences > mostOccurrences){
                    modes.clear();
                    mostOccurrences = nOccurrences;
                }
                modes.add(v);
            }
        }

        return modes;
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


}
