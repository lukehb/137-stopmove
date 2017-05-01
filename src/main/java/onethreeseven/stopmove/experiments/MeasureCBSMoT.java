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
public class MeasureCBSMoT {

    private static final String filename = "ferry";
    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");
    private static final AbstractGeographicProjection projection = new ProjectionEquirectangular();

    private static final boolean testSpatialParameter = true;


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
            final double minSpatialEps = 0;
            final double maxSpatialEps = 20;
            final double spatialEpsStepSize = 0.1;
            final long minTimeMillis = 21000;
            for (STStopTrajectory traj : trajMap.values()) {
                testSpatialParameter(traj, minSpatialEps, maxSpatialEps, spatialEpsStepSize, minTimeMillis);
            }
        }
    }


    private static void testSpatialParameter(STStopTrajectory traj, double minEpsSize, double maxEpsSize,
                                             double epsStepSize, long minTimeMillis){
        System.out.println("Test where we vary the region size parameter.");
        System.out.println("Stop time(ms): " + minTimeMillis);
        System.out.println("Spatial Parameter (m), Stop Time (ms), MCC");

        final StopClassificationStats stats = new StopClassificationStats();
        final CBSMoT algo = new CBSMoT();

        for (double regionSize = minEpsSize; regionSize <= maxEpsSize; regionSize+=epsStepSize) {
            STStopTrajectory stopTraj = algo.run(traj, regionSize, minTimeMillis);
            stats.calculateStats(traj, stopTraj);
            System.out.println(regionSize + "," + minTimeMillis +  "," + stats.getMCC());
        }
    }

}
