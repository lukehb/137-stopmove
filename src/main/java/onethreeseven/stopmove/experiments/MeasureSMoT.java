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
import onethreeseven.stopmove.algorithm.CBSMoT;
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

    //private static final String filename = "ferry";
    //private static final String filename = "hike";
    //private static final String filename = "shopping_trip";
    private static final String filename = "bus_a_33320";
    //private static final String filename = "bus_b_33424";
    //private static final String filename = "bus_c_38092";

    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");
    private static final AbstractGeographicProjection projection = new ProjectionEquirectangular();

    private static final boolean testSpatialParameter = true;
    private static final boolean testTemporalParameter = false;


    public static void main(String[] args) {

        System.out.println("Reading in st trajectories...");
        final Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                projection,
                new IdFieldResolver(0),
                new NumericFieldsResolver(1,2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4),
                true).parse(inFile);

        if(testSpatialParameter){
            final double minRegionSize = 1;
            final double maxRegionSize = 20;
            final double regionStepSize = 1;
            final long minTimeMillis = 40000;
            for (STStopTrajectory traj : trajMap.values()) {
                testRegionSize(traj, minRegionSize, maxRegionSize, regionStepSize, minTimeMillis);
            }
        }

        if(testTemporalParameter){
            final long timeStepMillis = 1000;
            final long minTimeParam = 1000;
            final long maxTimeParam = timeStepMillis * 100;
            for (STStopTrajectory traj : trajMap.values()) {
                final double spatialParam = 4.7;
                testMinTimeParameter(traj, spatialParam, minTimeParam, maxTimeParam, timeStepMillis);
            }

        }

    }

    private static void testMinTimeParameter(STStopTrajectory traj, double epMetres, long minStopTime, long maxStopTime, long timeStepMillis){
        System.out.println("Test where we vary the time parameter of CB-SMoT.");
        System.out.println("Spatial Parameter (m), Stop Time (ms), MCC");

        final StopClassificationStats stats = new StopClassificationStats();
        final SMoT algo = new SMoT();

        for (long stopTime = minStopTime; stopTime <= maxStopTime; stopTime+=timeStepMillis) {
            STStopTrajectory stopTraj = algo.run(traj, epMetres, stopTime);
            stats.calculateStats(traj, stopTraj);
            System.out.println(epMetres + "," + stopTime +  "," + stats.getMCC());
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
