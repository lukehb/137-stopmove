package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.resolver.*;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.CBSMoT;
import onethreeseven.stopmove.algorithm.POSMIT;
import onethreeseven.stopmove.algorithm.GBSMoT;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
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
    private static final GBSMoT ALGO_SMOT = new GBSMoT();
    private static final int nRuns = 50;
    private static final double spatialParam = 8.6;
    private static final long minTimeMillis = 20000L;
    private static final double minStopPr = 0.5;

    //changes these
    //private static final String filename = "ferry";
    //private static final String filename = "hike";
    private static final String filename = "shopping_trip";
    //private static final String filename = "bus_a_33320";
    //private static final String filename = "bus_b_33424";
    //private static final String filename = "bus_c_38092";
    private static final int[] searchBandwidths = new int[]{1,5,10,15,20};
    private static final boolean runPOSMIT = true;
    private static final boolean runCBSMOT = false;
    private static final boolean runSMOT = false;


    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");

    private static final ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    public static void main(String[] args) throws IOException {

        Map<String, STStopTrajectory> trajs = new STStopTrajectoryParser(new ProjectionEquirectangular(),
                new IdFieldResolver(0),
                new LatFieldResolver(1),
                new LonFieldResolver(2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4), true).parse(inFile);

        STStopTrajectory traj = trajs.values().iterator().next();

        //generate a stop/move dataset
        System.out.println(filename +
                " spatial param = " + spatialParam +
                " minPr = " + minStopPr +
                " minTimeMillis = " + minTimeMillis +
                " indexSearchBandwidths = " + Arrays.toString(searchBandwidths) +
                " nRuns = " + nRuns);

        //print csv header
        System.out.print(filename + ",");
        if(runPOSMIT){
            for (int searchBandwidth : searchBandwidths) {
                System.out.print(searchBandwidth + ",");
            }
        }
        if(runCBSMOT){
            System.out.print("cbsmot,");
        }
        if(runSMOT){
            System.out.print("smot");
        }
        System.out.print("\n");


        double[] avgRuningTimesPOSMIT = new double[searchBandwidths.length];
        double avgRunTimeCBSMOT = 0;
        double avgRunTimeSMOT = 0;

        for (int i = 0; i < nRuns; i++) {
            //posmit
            if(runPOSMIT){
                for (int j = 0; j < searchBandwidths.length; j++) {
                    long runningTimePOSMIT = bean.getCurrentThreadUserTime();
                    algoPOSMIT.toStopTrajectory(traj, algoPOSMIT.run(traj, searchBandwidths[j], spatialParam), minStopPr);
                    runningTimePOSMIT = (bean.getCurrentThreadUserTime() - runningTimePOSMIT);
                    avgRuningTimesPOSMIT[j] += runningTimePOSMIT;
                }
            }

            //cbsmot
            if(runCBSMOT){
                long runningTimeCBSMOT = bean.getCurrentThreadUserTime();
                algoCBSMOT.run(traj, spatialParam, minTimeMillis);
                runningTimeCBSMOT = (bean.getCurrentThreadUserTime() - runningTimeCBSMOT);
                avgRunTimeCBSMOT += runningTimeCBSMOT;
            }


            //smot
            if(runSMOT){
                long runningTimeSMOT = bean.getCurrentThreadUserTime();
                ALGO_SMOT.run(traj, spatialParam, minTimeMillis);
                runningTimeSMOT = (bean.getCurrentThreadUserTime() - runningTimeSMOT);
                avgRunTimeSMOT += runningTimeSMOT;
            }

        }

        if(runPOSMIT){
            for (int i = 0; i < avgRuningTimesPOSMIT.length; i++) {
                avgRuningTimesPOSMIT[i] = TimeUnit.NANOSECONDS.toMillis((long) avgRuningTimesPOSMIT[i]) / (double)nRuns;
                System.out.print(avgRuningTimesPOSMIT[i] + ",");
            }
        }

        if(runCBSMOT){
            avgRunTimeCBSMOT = TimeUnit.NANOSECONDS.toMillis((long) avgRunTimeCBSMOT) / (double)nRuns;
            System.out.print(avgRunTimeCBSMOT + ",");
        }

        if(runSMOT){
            avgRunTimeSMOT = TimeUnit.NANOSECONDS.toMillis((long) avgRunTimeSMOT) / (double)nRuns;
            System.out.print(avgRunTimeSMOT + ",");
        }
        System.out.print("\n");
    }

}
