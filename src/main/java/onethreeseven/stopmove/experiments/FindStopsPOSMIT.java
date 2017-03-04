package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.SpatioCompositieTrajectoryWriter;
import onethreeseven.datastructures.data.resolver.IdFieldResolver;
import onethreeseven.datastructures.data.resolver.NumericFieldsResolver;
import onethreeseven.datastructures.data.resolver.StopFieldResolver;
import onethreeseven.datastructures.data.resolver.TemporalFieldResolver;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.POSMIT;
import onethreeseven.stopmove.algorithm.StopClassificationStats;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Compute the stops from a spatio-temporal trajectory
 * @author Luke Bermingham
 */
public class FindStopsPOSMIT {

    private static final boolean writeOutputFile = false;

    private static final String filename = "synthetic_100k";
    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");

    private static final boolean computeStats = true;
    private static final StopClassificationStats stats = new StopClassificationStats();

    public static void main(String[] args) {

        System.out.println("Reading in st-stop trajectories...");

        Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                new ProjectionEquirectangular(),
                new IdFieldResolver(0),
                new NumericFieldsResolver(1,2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4),
                true).parse(inFile);

        final POSMIT algo = new POSMIT();
        final int nSearchRadius = 2;

        //actual algorithm
        for (Map.Entry<String, STStopTrajectory> entry : trajMap.entrySet()) {
            STStopTrajectory traj = entry.getValue();

            double stopVariance = algo.calculateStopVariance(traj, nSearchRadius);

            System.out.println("Computing stop probabilities for trajectory: " + entry.getKey());
            System.out.println("Search radius: " + nSearchRadius);
            System.out.println("Calculated stop variance as: " + stopVariance);

            double[] stopProbabilities = algo.run(traj, nSearchRadius, stopVariance);

            //estimate a good min confidence for classifying stops
            double minStopConfidence = 0.8;//Maths.mean(Maths.maxGap(stopProbabilities));
            System.out.println("Min stop confidence of: " + minStopConfidence);

            STStopTrajectory stopTraj = algo.toStopTrajectory(traj, stopProbabilities, minStopConfidence);

            Map<String, STStopTrajectory> outMap = new HashMap<>();
            outMap.put(entry.getKey(), stopTraj);

            if(writeOutputFile){
                System.out.println("Done computing probabilities, now writing to file");
                String meters = String.format("%.2f", stopVariance).replace(".", "-");
                String probability = String.valueOf((int)(minStopConfidence * 100));
                File outFile = new File(FileUtil.makeAppDir("traj"),
                        filename + "_Pr" + probability + "_" + nSearchRadius + "neighbourhood_" + meters + "m.txt");
                new SpatioCompositieTrajectoryWriter().write(outFile, outMap);
                System.out.println("Collect file at:" + outFile.getAbsolutePath());
            }

            if(computeStats){
                System.out.println("Calculating stats for traj: " + entry.getKey());
                stats.calculateStats(entry.getValue(), stopTraj);
                stats.printStats();
            }

        }
    }


}
