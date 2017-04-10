package onethreeseven.stopmove.algorithm;

import java.util.Arrays;

/**
 * Given set of values look for the elbow/knee point.
 * See paper: "Finding a “Kneedle” in a Haystack: Detecting Knee Points in System Behavior"
 * @author Luke Bermingham
 */
public class Kneedle {

    /**
     * This algorithm finds the so-called elbow in the data.
     * It does this by sorting the data, then making a line between the start
     * and end data points in the sorted data. Each point in the data is the projected
     * onto this line, and the point with the biggest euclidean distance is considered
     * the most likely elbow.
     * See paper: "Finding a “Kneedle” in a Haystack: Detecting Knee Points in System Behavior"
     * for more details.
     * @param data The data to find an elbow in.
     * @param minValue Discard values in the data that are smaller than this value.
     * @param maxValue Discard values in the data that are larger than this value.
     * @return The value of the elbow point.
     */
    public double run(double[] data, double minValue, double maxValue){
        //discard a any value 'y' that does satisfy minValue >= y <= maxValue.
        data = Arrays.stream(data).filter(y -> y <= maxValue && y >= minValue).sorted().toArray();
        //prepare the data into the unit range and subtract normalised index
        double[] normalisedData = prepare(data);

        //x-axis is the indices of the data
        //y-axis is the values of the data

        //make a vector, called "b", between start and end point
        double[] b = new double[]{normalisedData.length-1, normalisedData[normalisedData.length-1] - normalisedData[0]};
        double bNorm = Math.sqrt( b[0]*b[0] + b[1]*b[1] );
        double[] unitB = new double[]{ b[0]/bNorm, b[1]/bNorm };
        //go through each point in the data and make a vector called "a" between the
        //the current point and the first point
        //the best biggest distance is most likely the elbow
        double biggestDist = 0;
        int bestIdx = 0;

        for (int i = 0; i < normalisedData.length; i++) {
            double[] a = new double[]{i, normalisedData[i] - normalisedData[0]};
            //the projection of a onto b, is (a dot unitB) * unitB
            double a1 = a[0]*unitB[0] + a[1]*unitB[1];
            double[] projAonB = new double[]{a1*unitB[0], a1*unitB[1]};
            //euclidean distance between projection and a
            double dist = Math.sqrt(
                    (projAonB[0]-a[0])*(projAonB[0]-a[0]) +
                            (projAonB[1]-a[1])*(projAonB[1]-a[1]) );
            if(dist > biggestDist){
                biggestDist = dist;
                bestIdx = i;
            }
        }

        return data[bestIdx];
    }

    /**
     * In this step we prepare the data by normalising into unit range 0-1
     * and also subtracting the value from its normalised index value.
     * @param data The data to prepare.
     * @return The normalised data.
     */
    private double[] prepare(double[] data){

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
            double normalisedIndex = (double)i / data.length;
            normalisedData[i] = ((data[i] - curMin) / range) - normalisedIndex;
        }
        return normalisedData;
    }

    /**
     * Finds an elbow point in the data.
     * @see Kneedle#run(double[], double, double) It calls this with Double.MIN_VALUE and Double.MAX_VALUE.
     * @param data The data to find an elbow for.
     * @return The value of the elbow point.
     */
    public double run(double[] data){
        return run(data, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

}
