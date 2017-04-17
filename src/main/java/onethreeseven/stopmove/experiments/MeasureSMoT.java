package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.resolver.IdFieldResolver;
import onethreeseven.datastructures.data.resolver.NumericFieldsResolver;
import onethreeseven.datastructures.data.resolver.StopFieldResolver;
import onethreeseven.datastructures.data.resolver.TemporalFieldResolver;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.geo.projection.AbstractGeographicProjection;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.POSMIT;
import onethreeseven.stopmove.algorithm.SMoT;
import onethreeseven.stopmove.algorithm.StopClassificationStats;

import java.io.File;
import java.util.Map;

/**
 * Using a ground truth stop/move data-set it is possible
 * to measure the how many correct or incorrect classifications
 * were made by our {@link POSMIT} algorithm. This class performs
 * an experiment by varying the spatial noise parameter and observing how
 * this affect the classification.
 * @author Luke Bermingham
 */
public class MeasureSMoT {

    private static final String filename = "short_walk";
    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");
    private static final AbstractGeographicProjection projection = new ProjectionEquirectangular();

    private static final boolean testRegionSize = true;


    public static void main(String[] args) {

        System.out.println("Reading in st trajectories...");
        final Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                projection,
                new IdFieldResolver(0),
                new NumericFieldsResolver(1,2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4),
                true).parse(inFile);

        if(testRegionSize){
            final double minRegionSize = 0.5;
            final double maxRegionSize = 20;
            final double regionStepSize = 0.5;
            final long minTimeMillis = 2000;
            for (STStopTrajectory traj : trajMap.values()) {
                testRegionSize(traj, minRegionSize, maxRegionSize, regionStepSize, minTimeMillis);
            }
        }
    }



    private static void testRegionSize(STStopTrajectory traj, double minRegionSize, double maxRegionSize,
                                       double regionSizeStep, long minTimeMillis){
        System.out.println("Test where we vary the region size parameter.");
        System.out.println("Stop time(ms): " + minTimeMillis);
        System.out.println("Region Size, Stop Time (ms), MCC");

        final StopClassificationStats stats = new StopClassificationStats();
        final SMoT algo = new SMoT();

        for (double regionSize = minRegionSize; regionSize <= maxRegionSize; regionSize+=regionSizeStep) {
            STStopTrajectory stopTraj = algo.run(traj, regionSize, minTimeMillis);
            stats.calculateStats(traj, stopTraj);
            System.out.println(regionSize + "," + minTimeMillis +  "," + stats.getMCC());
        }
    }

}
