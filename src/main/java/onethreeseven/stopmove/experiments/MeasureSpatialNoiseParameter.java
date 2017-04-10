package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.resolver.IdFieldResolver;
import onethreeseven.datastructures.data.resolver.NumericFieldsResolver;
import onethreeseven.datastructures.data.resolver.StopFieldResolver;
import onethreeseven.datastructures.data.resolver.TemporalFieldResolver;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import onethreeseven.geo.projection.AbstractGeographicProjection;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.Kneedle;
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
public class MeasureSpatialNoiseParameter {

    private static final String filename = "ferry";
    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");
    private static final AbstractGeographicProjection projection = new ProjectionEquirectangular();

    public static void main(String[] args) {

        System.out.println("Reading in st trajectories...");
//        final Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
//                projection,
//                new SameIdResolver("1"),
//                new NumericFieldsResolver(0,1),
//                new TemporalFieldResolver(2),
//                new StopFieldResolver(3),
//                true).parse(inFile);

        final Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                projection,
                new IdFieldResolver(0),
                new NumericFieldsResolver(1,2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4),
                true).parse(inFile);

        final POSMIT posmit = new POSMIT();
        final StopClassificationStats stats = new StopClassificationStats();
        final int nSearchRadius = 10;
        double estimatedMinStopCutoff = 0.8;
        final Kneedle kneedle = new Kneedle();


        for (STStopTrajectory traj : trajMap.values()) {

            double estimatedStopVariance = kneedle.run(getDisplacements(traj), 0, 50);
            System.out.println("Estimated stop variance: " + estimatedStopVariance);
            System.out.println("Computing stop probabilities using neighbourhood: " + nSearchRadius);
            System.out.println("Stop Variance, Min Stop Pr, MCC");

            for (double stopVariance = 0; stopVariance < 10; stopVariance += 0.5) {
                double[] stopPrs = posmit.run(traj, nSearchRadius, stopVariance);

                estimatedMinStopCutoff = kneedle.run(stopPrs);

                STStopTrajectory stopTraj = posmit.toStopTrajectory(traj, stopPrs, estimatedMinStopCutoff);
                stats.calculateStats(traj, stopTraj);

                System.out.println(stopVariance + "," + estimatedMinStopCutoff + "," + stats.getMCC());
            }
        }

    }

    private static double[] getDisplacements(SpatioCompositeTrajectory traj){
        double[] displacements = new double[traj.size()-1];
        for (int i = 1; i < traj.size()-1; i++) {
            displacements[i] = traj.getEuclideanDistance(i-1, i);
        }
        return displacements;
    }

}
