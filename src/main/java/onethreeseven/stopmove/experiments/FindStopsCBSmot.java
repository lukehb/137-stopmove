package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.SpatioCompositieTrajectoryWriter;
import onethreeseven.datastructures.data.resolver.NumericFieldsResolver;
import onethreeseven.datastructures.data.resolver.SameIdResolver;
import onethreeseven.datastructures.data.resolver.StopFieldResolver;
import onethreeseven.datastructures.data.resolver.TemporalFieldResolver;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import onethreeseven.geo.projection.AbstractGeographicProjection;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.CBSMoT;
import onethreeseven.stopmove.algorithm.StopClassificationStats;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Experiment to test find stops using CB-SMoT
 * @author Luke Bermingham
 */
public class FindStopsCBSmot {

    private static final String filename = "hike";
    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");

    private static final long minTimeMillis = 1000;
    private static final double epsMeters = 0.05;

    private static final String metersStr = String.format("%.2f", epsMeters).replace(".", "-");
    private static final String outName = filename + "_cbsmot_" + minTimeMillis + "ms_" + metersStr + "m";
    private static final File outFile = new File(FileUtil.makeAppDir("traj"), outName + ".txt");

    private static final AbstractGeographicProjection projection = new ProjectionEquirectangular();
    private static final CBSMoT algo = new CBSMoT();

    private static final boolean calculateStats = true;
    private static final StopClassificationStats stats = new StopClassificationStats();

    public static void main(String[] args) {

        Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                projection,
                new SameIdResolver("1"),
                new NumericFieldsResolver(0,1),
                new TemporalFieldResolver(2),
                new StopFieldResolver(3),
                true).parse(inFile);

        Map<String, STStopTrajectory> outMap = new HashMap<>();

        //actual algorithm
        long startTime = System.currentTimeMillis();

        for (Map.Entry<String, STStopTrajectory> entry : trajMap.entrySet()) {
            System.out.println("CB-SMoT finding stops for traj: " + entry.getKey());
            STStopTrajectory outTraj = algo.run(entry.getValue(), epsMeters, minTimeMillis);
            outTraj.toGeographic();
            outMap.put(entry.getKey(), outTraj);
        }
        long endTime = System.currentTimeMillis();
        long timeTakenMs = endTime - startTime;
        System.out.println("CB-SMoT took: " + timeTakenMs + "ms to process " + trajMap.size() + " trajectories.");

        //convert to geo first
        System.out.println("Converting to geographic");
        outMap.values().forEach(SpatioCompositeTrajectory::toGeographic);
        System.out.println("Preparing to write to file");
        new SpatioCompositieTrajectoryWriter().write(outFile, outMap);
        System.out.println("Collect cb-smot stops at: " + outFile.getAbsolutePath());

        if(calculateStats){
            for (Map.Entry<String, STStopTrajectory> entry : trajMap.entrySet()) {
                STStopTrajectory truth = entry.getValue();
                STStopTrajectory calculated = outMap.get(entry.getKey());
                System.out.println("Calculating stats for traj: " + entry.getKey());
                stats.calculateStats(truth, calculated);
                stats.printStats();
            }
        }
    }



}
