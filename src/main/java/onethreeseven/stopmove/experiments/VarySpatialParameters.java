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
import onethreeseven.stopmove.algorithm.*;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Measure classification effectiveness while only changing the spatial parameters
 * of
 * {@link onethreeseven.stopmove.algorithm.POSMIT}
 * {@link onethreeseven.stopmove.algorithm.CBSMoT}
 * {@link onethreeseven.stopmove.algorithm.SMoT}
 * @author Luke Bermingham
 */
public class VarySpatialParameters {

    //private static final String filename = "ferry";
    //private static final String filename = "hike";
    //private static final String filename = "shopping_trip";
    //private static final String filename = "bus_a_33320";
    //private static final String filename = "bus_b_33424";
    private static final String filename = "bus_c_38092";

    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");
    private static final AbstractGeographicProjection projection = new ProjectionEquirectangular();

    private static final POSMIT algoPOSMIT = new POSMIT();
    private static final CBSMoT algoCBSMOT = new CBSMoT();
    private static final SMoT algoSMOT = new SMoT();
    private static final StopClassificationStats classificationStats = new StopClassificationStats();
    private static final CountStopsAndMoves datasetStats = new CountStopsAndMoves();

    private static final double minSpatialParam = 2;
    private static final double maxSpatialParam = 20;
    private static final double spatialParamStep = 1;

    public static void main(String[] args) {

        System.out.println("Reading in st trajectories...");
        final Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                projection,
                new IdFieldResolver(0),
                new NumericFieldsResolver(1,2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4),
                true).parse(inFile);

        for (Map.Entry<String, STStopTrajectory> entry : trajMap.entrySet()) {
            System.out.println("###################################");
            System.out.println("Running experiment for traj: " + entry.getKey());
            System.out.println("###################################");
            runExperiment(entry.getValue());
        }
    }

    private static void runExperiment(STStopTrajectory traj){

        final DecimalFormat df = new DecimalFormat("#.00");

        //estimate spatial param
        double esimatedSpatialParam = algoPOSMIT.estimateStopVariance(traj);

        //estimate search bandwidth param
        int searchBandwidth = algoPOSMIT.estimateSearchRadius(traj, esimatedSpatialParam);
        //get modal sampling rate
        datasetStats.run(traj);
        //use this value for CBSMOT and SMOT
        long minStopTime = datasetStats.getModalSamplingSeconds() * 1000 * searchBandwidth;

        System.out.println(
                "Stop Variance," +
                        "POSMIT_MinStopPr_Est," +
                        "POSMIT_MinStopPr_025," +
                        "POSMIT_MinStopPr_050," +
                        "POSMIT_MinStopPr_075," +
                        "CBSMOT," +
                        "SMOT," +
                        "Estimated_hd=" + df.format(esimatedSpatialParam) + "," +
                        "Estimated_hi=" + searchBandwidth + "," +
                        "Estimated_minTime=" + minStopTime
        );




        for (double spatialParam = minSpatialParam; spatialParam <= maxSpatialParam; spatialParam += spatialParamStep) {

            System.out.print(spatialParam + ",");

            //POSMIT
            {
                double[] stopPrs = algoPOSMIT.run(traj, searchBandwidth, spatialParam);

                //do estimated minStopPr
                double minStopPr = algoPOSMIT.estimateMinStopPr(stopPrs);
                classificationStats.calculateStats(traj, algoPOSMIT.toStopTrajectory(traj, stopPrs, minStopPr));
                System.out.print(classificationStats.getMCC() + ",");

                //posmit 0.25-0.75 minStopPr
                for (minStopPr = 0.25; minStopPr <= 0.75; minStopPr+=0.25) {
                    classificationStats.calculateStats(traj, algoPOSMIT.toStopTrajectory(traj, stopPrs, minStopPr));
                    System.out.print(classificationStats.getMCC() + ",");
                }

            }

            //CBSMOT
            classificationStats.calculateStats(traj, algoCBSMOT.run(traj, spatialParam, minStopTime));
            System.out.print(classificationStats.getMCC() + ",");

            //SMOT
            classificationStats.calculateStats(traj, algoSMOT.run(traj, spatialParam, minStopTime));
            System.out.print(classificationStats.getMCC());

            int paramDistance = (int) (Math.floor(spatialParam) - Math.floor(esimatedSpatialParam));

            if(paramDistance == 0){
                System.out.print("," + 0);
            }

            if(paramDistance == 1){
                System.out.print("," + 1);
            }

            System.out.print("\n");


        }

    }

}
