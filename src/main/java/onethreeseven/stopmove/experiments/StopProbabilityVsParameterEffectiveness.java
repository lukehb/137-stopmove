package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.resolver.IdFieldResolver;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.stopmove.algorithm.CBSMoT;
import onethreeseven.stopmove.algorithm.POSMIT;
import onethreeseven.stopmove.algorithm.StopClassificationStats;
import java.io.File;
import java.util.Map;

/**
 * In this experiment we measure the effect that the spatial epsilon
 * parameter has on the Stop Probability algorithms.
 * @author Luke Bermingham
 */
public class StopProbabilityVsParameterEffectiveness {

    private static final StopClassificationStats stats = new StopClassificationStats();

    /**
     * Data-set parameters
     */
    private static final File inFile = new File(FileUtil.makeAppDir("traj"), "short_walk.txt");

    /**
     * Algorithms and params
     */
    private static final double minSpatialParam = 0;
    private static final double maxSpatialParam = 5;
    private static final double spatialParamIncrement = 0.05;
    private static final POSMIT algoPosmit = new POSMIT();
    private static final CBSMoT algoCbSMoT = new CBSMoT();
    private static final long minStopMsCbSmot = 2000L;
    private static final int indexSearchRadius = 2;
    private static final double minStopPr = 0.8;

    public static void main(String[] args) {
        final STStopTrajectory traj = getTraj();
        testSpatialParamVsEffectiveness(traj);
    }

    private static void testSpatialParamVsEffectiveness(STStopTrajectory traj){

        double estimatedSp = algoPosmit.estimateStopVariance(traj);
        System.out.println("SpatialParam,POSMIT,CBSMoT"
                + ",minStopMsCBSMoT = " + minStopMsCbSmot
                + ",minStopPr = " + minStopPr
                + ",indexSearchRadius = " + indexSearchRadius
                + ",estimatedSP = " + estimatedSp
                + ",score = " + runPOSMIT(traj, estimatedSp));

        System.out.println("Measuring MCC....");

        for (double spatialParam = minSpatialParam; spatialParam < maxSpatialParam; spatialParam += spatialParamIncrement) {
            runAlgos(traj, spatialParam);
        }
    }

    private static void runAlgos(STStopTrajectory traj, double spatialParam){
        System.out.print(spatialParam + ",");
        //POSMIT
        System.out.print(runPOSMIT(traj, spatialParam) + ",");
        //CB-SMoT
        System.out.print(runCBSMOT(traj, spatialParam) + "\n");
    }

    private static double runCBSMOT(STStopTrajectory traj, double spatialParam){
        STStopTrajectory out = algoCbSMoT.run(traj, spatialParam, minStopMsCbSmot);
        stats.calculateStats(traj, out);
        return stats.getMCC();
    }

    private static double runPOSMIT(STStopTrajectory traj, double spatialParam){
        double[] stopPrs = algoPosmit.run(traj, indexSearchRadius, spatialParam);
        STStopTrajectory out = algoPosmit.toStopTrajectory(traj, stopPrs, minStopPr);
        stats.calculateStats(traj, out);
        return stats.getMCC();
    }

    private static STStopTrajectory getTraj(){
        Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                new IdFieldResolver(0), 1, 2, 4, 3)
                .setInCartesianMode(true).parse(inFile);
        return trajMap.values().iterator().next();
    }

}
