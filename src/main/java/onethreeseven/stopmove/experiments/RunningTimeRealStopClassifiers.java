package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.resolver.IdFieldResolver;
import onethreeseven.datastructures.data.resolver.NumericFieldsResolver;
import onethreeseven.datastructures.data.resolver.StopFieldResolver;
import onethreeseven.datastructures.data.resolver.TemporalFieldResolver;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.CBSMoT;
import onethreeseven.stopmove.algorithm.POSMIT;
import onethreeseven.stopmove.algorithm.SMoT;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Measure the running time for both POSMIT and CB-SMoT using
 * different data-sets.
 * @author Luke Bermingham
 */
public class RunningTimeRealStopClassifiers {


    private static final POSMIT algoPOSMIT = new POSMIT();
    private static final CBSMoT algoCBSMOT = new CBSMoT();
    private static final SMoT algoSMOT = new SMoT();
    private static final int nRuns = 50;
    private static final double spatialParam = 8.6;
    private static final int indexSearchRadius = 1;
    private static final long minTimeMillis = 20000L;
    private static final double minStopPr = 0.5;
    private static final String filename = "ferry";

    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");

    private static final ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    public static void main(String[] args) {

        Map<String, STStopTrajectory> trajs = new STStopTrajectoryParser(new ProjectionEquirectangular(),
                new IdFieldResolver(0),
                new NumericFieldsResolver(1,2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4), true).parse(inFile);

        STStopTrajectory traj = trajs.values().iterator().next();

        //generate a stop/move dataset
        System.out.println(filename +
                " spatial param = " + spatialParam +
                " minPr = " + minStopPr +
                " minTimeMillis = " + minTimeMillis +
                " indexSearchRadius = " + indexSearchRadius +
                " nRuns = " + nRuns);

        System.out.println("POSMIT(ms),CB-SMoT(ms),SMoT(ms)");

        double avgRunTimePOSMIT = 0;
        double avgRunTimeCBSMOT = 0;
        double avgRunTimeSMOT = 0;

        for (int i = 0; i < nRuns; i++) {
            //posmit
            long runningTimePOSMIT = bean.getCurrentThreadUserTime();
            algoPOSMIT.toStopTrajectory(traj, algoPOSMIT.run(traj, indexSearchRadius, spatialParam), minStopPr);
            runningTimePOSMIT = (bean.getCurrentThreadUserTime() - runningTimePOSMIT);
            avgRunTimePOSMIT += runningTimePOSMIT;

            //cbsmot
            long runningTimeCBSMOT = bean.getCurrentThreadUserTime();
            algoCBSMOT.run(traj, spatialParam, minTimeMillis);
            runningTimeCBSMOT = (bean.getCurrentThreadUserTime() - runningTimeCBSMOT);
            avgRunTimeCBSMOT += runningTimeCBSMOT;

            //smot
            long runningTimeSMOT = bean.getCurrentThreadUserTime();
            algoSMOT.run(traj, spatialParam, minTimeMillis);
            runningTimeSMOT = (bean.getCurrentThreadUserTime() - runningTimeSMOT);
            avgRunTimeSMOT += runningTimeSMOT;
        }

        avgRunTimePOSMIT = TimeUnit.NANOSECONDS.toMillis((long) avgRunTimePOSMIT) / (double)nRuns;
        avgRunTimeCBSMOT = TimeUnit.NANOSECONDS.toMillis((long) avgRunTimeCBSMOT) / (double)nRuns;
        avgRunTimeSMOT = TimeUnit.NANOSECONDS.toMillis((long) avgRunTimeSMOT) / (double)nRuns;

        System.out.println(avgRunTimePOSMIT + "," + avgRunTimeCBSMOT + "," + avgRunTimeSMOT);

    }

}
