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

    //private static final String filename = "ferry";
    //private static final String filename = "hike";
    //private static final String filename = "shopping_trip";
    //private static final String filename = "bus_a_33320";
    //private static final String filename = "bus_b_33424";
    private static final String filename = "bus_c_38092";

    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");
    private static final AbstractGeographicProjection projection = new ProjectionEquirectangular();

    private static final boolean testStopVariance = false;
    private static final boolean testSearchRadius = false;
    private static final boolean testMinStopPr = true;

    private static final POSMIT algo = new POSMIT();
    private static final StopClassificationStats stats = new StopClassificationStats();

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
            final double minStopVariance = 1;
            final double maxStopVariance = 20;
            final double stopVarianceStep = 1;
            for (STStopTrajectory traj : trajMap.values()) {
                double estimatedStopVariance = algo.estimateStopVariance(traj);
                System.out.println("Estimated stop variance: " + estimatedStopVariance);
                int estimatedSearchBandwidth = algo.estimateSearchRadius(traj, estimatedStopVariance);
                System.out.println("Estimated search bandwidth: " + estimatedSearchBandwidth);
                testStopVariance(traj, minStopVariance, maxStopVariance,
                        stopVarianceStep, estimatedSearchBandwidth);
            }
        }
        if(testMinStopPr){
            final double minEps = 0;
            final double maxEps = 1;
            final double epsStep = 0.05;
            for (STStopTrajectory traj : trajMap.values()) {
                double stopVariance = algo.estimateStopVariance(traj);
                int searchRadius = algo.estimateSearchRadius(traj, stopVariance);
                testMinStopPr(traj, minEps, maxEps, epsStep, stopVariance, searchRadius);
            }
        }
        if(testSearchRadius){
            final int minSearchRadius = 1;

            for (STStopTrajectory trajectory : trajMap.values()) {

                final int maxSearchRadius = Math.min(100, trajectory.size()/2);
                double stopVariance = algo.estimateStopVariance(trajectory);

                System.out.println("Estimated search radius: " + algo.estimateSearchRadius(trajectory, stopVariance));
                testSearchRadius(trajectory, minSearchRadius, maxSearchRadius, stopVariance);
            }
        }
    }

    private static void testMinStopPr(STStopTrajectory traj, double minPr, double maxPr,
                                  double prStep, double stopVariance, int searchRadius){
        System.out.println("Test where we vary the minStopPr (eps) parameter.");
        System.out.println("Search radius: " + searchRadius);
        System.out.println("Stop variance: " + stopVariance);
        System.out.println("Min Stop Pr, TP, TN, FP, FN, MCC");

        final StopClassificationStats stats = new StopClassificationStats();

        double[] stopPrs = algo.run(traj, searchRadius, stopVariance);
        System.out.println("Estimated eps: " + algo.estimateMinStopPr(stopPrs)) ;

        for (double minStopPr = minPr; minStopPr <= maxPr+prStep; minStopPr+=prStep) {
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
                                  double stopVarianceStep, int searchRadius){
        System.out.println("Test where we vary the stopVariance (h_d) parameter.");
        System.out.println("Search radius: " + searchRadius);
        System.out.println("Min stop variance: " + minStopVariance);
        System.out.println("Max stop variance: " + maxStopVariance);
        System.out.println("Stop Variance, POSMIT_MinStopPr_Est, POSMIT_MinStopPr_025, POSMIT_MinStopPr_050, POSMIT_MinStopPr_075");


        for (double stopVariance = minStopVariance; stopVariance <= maxStopVariance; stopVariance+=stopVarianceStep) {
            runThrough25To75MinStopPr(traj, searchRadius, stopVariance);
        }
    }

    private static void testSearchRadius(STStopTrajectory traj, int minSearchRadius, int maxSearchRadius, double stopVariance){
        System.out.println("Test where we vary the search radius (h_i) parameter.");
        System.out.println("Stop variance: " + stopVariance);
        System.out.println("h_i, POSMIT_MinStopPr_Est, POSMIT_MinStopPr_025, POSMIT_MinStopPr_050, POSMIT_MinStopPr_075");

        for (int searchRadius = minSearchRadius; searchRadius <= maxSearchRadius; searchRadius++) {
            runThrough25To75MinStopPr(traj, searchRadius, stopVariance);
        }
    }

    private static void runThrough25To75MinStopPr(STStopTrajectory traj, int searchRadius, double stopVariance){
        double[] stopPrs = algo.run(traj, searchRadius, stopVariance);
        System.out.print(stopVariance);

        //estimate the minStopPr and print the stats for that
        {
            double estimatedMinStopPr = algo.estimateMinStopPr(stopPrs);
            STStopTrajectory stopTraj = algo.toStopTrajectory(traj, stopPrs, estimatedMinStopPr);
            stats.calculateStats(traj, stopTraj);
            System.out.print("," + stats.getMCC());
        }

        for (double minStopPr = 0.25; minStopPr <= 0.75; minStopPr+=0.25) {
            STStopTrajectory stopTraj = algo.toStopTrajectory(traj, stopPrs, minStopPr);
            stats.calculateStats(traj, stopTraj);
            System.out.print("," + stats.getMCC());
        }

        System.out.print("\n");
    }

}
