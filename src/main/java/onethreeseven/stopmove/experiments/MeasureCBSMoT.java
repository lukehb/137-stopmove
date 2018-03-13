package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.resolver.*;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.geo.projection.AbstractGeographicProjection;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.CBSMoT;
import onethreeseven.stopmove.algorithm.POSMIT;
import onethreeseven.stopmove.algorithm.StopClassificationStats;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Using a ground truth stop/move data-set it is possible
 * to measure the how many correct or incorrect classifications
 * were made by our {@link POSMIT} algorithm. This class performs
 * an experiment by varying the spatial noise parameter and observing how
 * this affect the classification.
 * @author Luke Bermingham
 */
public class MeasureCBSMoT {

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


    public static void main(String[] args) throws IOException {

        System.out.println("Reading in st trajectories...");
        final Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                projection,
                new IdFieldResolver(0),
                new LatFieldResolver(1),
                new LonFieldResolver(2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4),
                true).parse(inFile);

        if(testSpatialParameter){
            final double minSpatialEps = 1;
            final double maxSpatialEps = 20;
            final double spatialEpsStepSize = 1;
            final long minTimeMillis = 40000;
            for (STStopTrajectory traj : trajMap.values()) {
                testSpatialParameter(traj, minSpatialEps, maxSpatialEps, spatialEpsStepSize, minTimeMillis);
            }
        }

        if(testTemporalParameter){
            final long timeStepMillis = 1000;
            final long minTimeParam = 1000;
            final long maxTimeParam = timeStepMillis * 100;
            for (STStopTrajectory traj : trajMap.values()) {
                final double spatialParam = 3;
                testMinTimeParameter(traj, spatialParam, minTimeParam, maxTimeParam, timeStepMillis);
            }

        }
    }

    private static void testMinTimeParameter(STStopTrajectory traj, double epMetres, long minStopTime, long maxStopTime, long timeStepMillis){
        System.out.println("Test where we vary the time parameter of CB-SMoT.");
        System.out.println("Spatial Parameter (m), Stop Time (ms), MCC");

        final StopClassificationStats stats = new StopClassificationStats();
        final CBSMoT algo = new CBSMoT();

        for (long stopTime = minStopTime; stopTime <= maxStopTime; stopTime+=timeStepMillis) {
            STStopTrajectory stopTraj = algo.run(traj, epMetres, stopTime);
            stats.calculateStats(traj, stopTraj);
            System.out.println(epMetres + "," + stopTime +  "," + stats.getMCC());
        }
    }

    private static void testSpatialParameter(STStopTrajectory traj, double minSpatialParam, double maxSpatialParam,
                                             double spatialStepSize, long minTimeMillis){
        System.out.println("Test where we vary the region size parameter.");
        System.out.println("Stop time(ms): " + minTimeMillis);
        System.out.println("Spatial Parameter (m), Stop Time (ms), MCC");

        final StopClassificationStats stats = new StopClassificationStats();
        final CBSMoT algo = new CBSMoT();

        for (double epsMetres = minSpatialParam; epsMetres <= maxSpatialParam; epsMetres+=spatialStepSize) {
            STStopTrajectory stopTraj = algo.run(traj, epsMetres, minTimeMillis);
            stats.calculateStats(traj, stopTraj);
            System.out.println(epsMetres + "," + minTimeMillis +  "," + stats.getMCC());
        }
    }

}
