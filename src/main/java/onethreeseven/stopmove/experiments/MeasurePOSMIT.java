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
public class MeasurePOSMIT {

    private static final String filename = "38092";
    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");
    private static final AbstractGeographicProjection projection = new ProjectionEquirectangular();

    private static final boolean testStopVariance = false;
    private static final boolean testSearchRadius = false;//true;
    private static final boolean testMinStopPr = true;//true;


    public static void main(String[] args) {

        System.out.println("Reading in st trajectories...");
        final Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                projection,
                new IdFieldResolver(0),
                new NumericFieldsResolver(1,2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4),
                true).parse(inFile);

        if(testStopVariance){
            final double minStopVariance = 0;
            final double maxStopVariance = 20;
            final double stopVarianceStep = 0.1;
            final int searchRadius = 21;
            final double minStopPr = 0.5;
            for (STStopTrajectory traj : trajMap.values()) {
                System.out.println("Estimated stop variance: " + new POSMIT().estimateStopVariance(traj));
                testStopVariance(traj, minStopVariance, maxStopVariance,
                        stopVarianceStep, searchRadius, minStopPr);
            }
        }
        if(testMinStopPr){
            final double minEps = 0;
            final double maxEps = 1;
            final double epsStep = 0.05;
            //final double stopVariance = 2.922;
            final int searchRadius = 1;
            for (STStopTrajectory traj : trajMap.values()) {
                double stopVariance = new POSMIT().estimateStopVariance(traj);
                testMinStopPr(traj, minEps, maxEps, epsStep, stopVariance, searchRadius);
            }
        }
        if(testSearchRadius){
            final int minSearchRadius = 1;
            final int maxSearchRadius = 50;
            //double stopVariance = 17.32;
            final double minStopPr = 0.45;
            for (STStopTrajectory trajectory : trajMap.values()) {
                double stopVariance = new POSMIT().estimateStopVariance(trajectory);

                System.out.println("Estimated search radius: " + new POSMIT().estimateSearchRadius(trajectory, stopVariance));
                testSearchRadius(trajectory, minSearchRadius, maxSearchRadius, stopVariance, minStopPr);
            }
        }
    }

    private static void testMinStopPr(STStopTrajectory traj, double minEps, double maxEps,
                                  double epsStep, double stopVariance, int searchRadius){
        System.out.println("Test where we vary the minStopPr (eps) parameter.");
        System.out.println("Search radius: " + searchRadius);
        System.out.println("Stop variance: " + stopVariance);
        System.out.println("Min Stop Pr, TP, TN, FP, FN, MCC");

        final StopClassificationStats stats = new StopClassificationStats();
        final POSMIT algo = new POSMIT();

        double[] stopPrs = algo.run(traj, searchRadius, stopVariance);
        System.out.println("Estimated eps: " + algo.estimateMinStopPr(stopPrs)) ;

        for (double minStopPr = minEps; minStopPr <= maxEps+epsStep; minStopPr+=epsStep) {
            STStopTrajectory stopTraj = algo.toStopTrajectory(traj, stopPrs, minStopPr);
            stats.calculateStats(traj, stopTraj);
            System.out.println(
                    minStopPr + "," +
                    stats.getTruePositiveRate() + "," +
                    stats.getTrueNegativeRate() + "," +
                    stats.getFalsePositiveRate() + "," +
                    stats.getFalseNegativeRate() + "," +
                    stats.getMCC());
        }
    }

    private static void testStopVariance(STStopTrajectory traj, double minStopVariance, double maxStopVariance,
                                  double stopVarianceStep, int searchRadius, double minStopPr){
        System.out.println("Test where we vary the stopVariance (h_d) parameter.");
        System.out.println("Search radius: " + searchRadius);
        System.out.println("Min stop variance: " + minStopVariance);
        System.out.println("Max stop variance: " + maxStopVariance);
        System.out.println("Stop Variance, Min Stop Pr, Search Radius, MCC");

        final StopClassificationStats stats = new StopClassificationStats();
        final POSMIT algo = new POSMIT();

        for (double stopVariance = minStopVariance; stopVariance <= maxStopVariance; stopVariance+=stopVarianceStep) {
            double[] stopPrs = algo.run(traj, searchRadius, stopVariance);
            STStopTrajectory stopTraj = algo.toStopTrajectory(traj, stopPrs, minStopPr);
            stats.calculateStats(traj, stopTraj);
            System.out.println(stopVariance + "," + minStopPr + "," + searchRadius + "," + stats.getMCC());
        }
    }

    private static void testSearchRadius(STStopTrajectory traj, int minSearchRadius, int maxSearchRadius,
                                  double stopVariance, double minStopPr){
        System.out.println("Test where we vary the search radius (h_i) parameter.");
        System.out.println("Stop variance: " + stopVariance);
        System.out.println("Min stop pr: " + minStopPr);
        System.out.println("Stop Variance, Min Stop Pr, Search Radius, MCC");

        final StopClassificationStats stats = new StopClassificationStats();
        final POSMIT algo = new POSMIT();

        for (int searchRadius = minSearchRadius; searchRadius <= maxSearchRadius; searchRadius++) {
            double[] stopPrs = algo.run(traj, searchRadius, stopVariance);
            STStopTrajectory stopTraj = algo.toStopTrajectory(traj, stopPrs, minStopPr);
            stats.calculateStats(traj, stopTraj);
            System.out.println(stopVariance + "," + minStopPr + "," + searchRadius + "," + stats.getMCC());
        }
    }

}
