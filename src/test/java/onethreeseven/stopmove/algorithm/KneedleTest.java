package onethreeseven.stopmove.algorithm;

import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test for {@link Kneedle}
 * @author Luke Bermingham
 */
public class KneedleTest {

    @Test
    public void testKneedleFindKnee2d() {

        //Test data based on that used in:
        //"Finding a “Kneedle” in a Haystack: Detecting Knee Points in System Behavior"

        double[][] testData = new double[][]{
                new double[]{0,0},
                new double[]{0.1, 0.55},
                new double[]{0.2, 0.75},
                new double[]{0.35, 0.825},
                new double[]{0.45, 0.875},
                new double[]{0.55, 0.9},
                new double[]{0.675, 0.925},
                new double[]{0.775, 0.95},
                new double[]{0.875, 0.975},
                new double[]{1,1}
        };

        ArrayList<double[]> kneePoints = new Kneedle().run(testData, 1, 1, false);

        for (double[] kneePoint : kneePoints) {
            System.out.println("Knee point:" + Arrays.toString(kneePoint));
        }

        Assert.assertTrue(kneePoints.size() == 1);

        //according to the paper the knee should be at x = 0.2
        Assert.assertArrayEquals(new double[]{0.2, 0.75}, kneePoints.get(0), 1e-05);


    }
}