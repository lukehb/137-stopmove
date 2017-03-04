package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.resolver.IdFieldResolver;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.stopmove.algorithm.POSMIT;
import onethreeseven.stopmove.algorithm.StopClassificationStats;
import java.io.File;
import java.util.Map;

/**
 * Measures the effects of varying the minStopPr for POSMIT
 * @author Luke Bermingham
 */
public class VaryingMinStopPrPOSMIT {

    private static final File inFile = new File(FileUtil.makeAppDir("traj"), "dog_walk.txt");
    private static final POSMIT algo = new POSMIT();
    private static final int indexSearchRadius = 2;

    public static void main(String[] args) {

        //get traj
        STStopTrajectory truth = getTraj();
        double stopVariance = 3; //algo.calculateStopVariance(truth, indexSearchRadius);

        StopClassificationStats stats = new StopClassificationStats();

        System.out.println("MinStopPr,TP,FP,TN,FN");
        for (double minStopPr = 0; minStopPr < 1.0; minStopPr+=0.05) {
            double[] stopPrs = algo.run(truth, indexSearchRadius, stopVariance);
            STStopTrajectory calculatedTraj = algo.toStopTrajectory(truth, stopPrs, minStopPr);

            stats.calculateStats(truth, calculatedTraj);

            System.out.println(
                    minStopPr + "," +
                    stats.getTruePositive()+ "," +
                    stats.getFalsePositive()+ "," +
                    stats.getTrueNegative()+ "," +
                    stats.getFalseNegative());
        }

    }

    private static STStopTrajectory getTraj(){
        Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                new IdFieldResolver(0), 1, 2, 4, 3)
                .setInCartesianMode(true).parse(inFile);
        return trajMap.values().iterator().next();
    }

}
