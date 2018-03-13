package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.SpatioCompositeTrajectoryWriter;
import onethreeseven.datastructures.data.resolver.*;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import onethreeseven.geo.projection.AbstractGeographicProjection;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.CBSMoT;
import onethreeseven.stopmove.algorithm.StopClassificationStats;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Experiment to test find stops using CB-SMoT
 * @author Luke Bermingham
 */
public class FindStopCBSMoT {

    private static final String filename = "house_visit";
    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");

    private static final long minTimeMillis = 9000;
    private static final double epsMeters = 0.05;

    private static final String metersStr = String.format("%.2f", epsMeters).replace(".", "-");
    private static final String outName = filename + "_cbsmot_" + minTimeMillis + "ms_" + metersStr + "m";
    private static final File outFile = new File(FileUtil.makeAppDir("traj"), outName + ".txt");

    private static final AbstractGeographicProjection projection = new ProjectionEquirectangular();
    private static final CBSMoT algo = new CBSMoT();

    private static final boolean calculateStats = true;
    private static final boolean writeOutput = false;
    private static final StopClassificationStats stats = new StopClassificationStats();

    public static void main(String[] args) throws IOException {

        Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                projection,
                new SameIdResolver("1"),
                new LatFieldResolver(0),
                new LonFieldResolver(1),
                new TemporalFieldResolver(2),
                new StopFieldResolver(3),
                true).parse(inFile);

        Map<String, STStopTrajectory> outMap = new HashMap<>();

        //actual algorithm
        long startTime = System.currentTimeMillis();

        for (Map.Entry<String, STStopTrajectory> entry : trajMap.entrySet()) {
            System.out.println("CB-SMoT finding stops for traj: " + entry.getKey());
            STStopTrajectory calculatedTraj = algo.run(entry.getValue(), epsMeters, minTimeMillis);
            if(calculateStats){
                STStopTrajectory truth = entry.getValue();
                System.out.println("Calculating stats for traj: " + entry.getKey());
                stats.calculateStats(truth, calculatedTraj);
                stats.printStats();
            }
            calculatedTraj.toGeographic();
            if(writeOutput){
                outMap.put(entry.getKey(), calculatedTraj);
            }
        }
        long endTime = System.currentTimeMillis();
        long timeTakenMs = endTime - startTime;
        System.out.println("CB-SMoT took: " + timeTakenMs + "ms to process " + trajMap.size() + " trajectories.");

        if(writeOutput){
            //convert to geo first
            System.out.println("Converting to geographic");
            outMap.values().forEach(SpatioCompositeTrajectory::toGeographic);
            System.out.println("Preparing to write to file");
            new SpatioCompositeTrajectoryWriter().write(outFile, outMap);
            System.out.println("Collect cb-smot stops at: " + outFile.getAbsolutePath());
        }
    }



}
