package onethreeseven.stopmove.experiments;

import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.util.DataGeneratorUtil;
import onethreeseven.stopmove.algorithm.CBSMoT;
import onethreeseven.stopmove.algorithm.POSMIT;
import onethreeseven.stopmove.algorithm.GBSMoT;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Measure the running time for both POSMIT and CB-SMoT using
 * increasingly larger synthetic data-sets.
 * @author Luke Bermingham
 */
public class RunningTimeSynthStopClassifiers {

    private static final POSMIT algoPOSMIT = new POSMIT();
    private static final CBSMoT algoCBSMOT = new CBSMoT();
    private static final GBSMoT ALGO_SMOT = new GBSMoT();
    private static final double startLat = -16.9186;
    private static final double startLon = 145.7781;
    private static final ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    //tweak these params
    private static final int datasetStartingSize = 10000;
    private static final int datasetEndingSize = 10000000;
    private static final int increment = 100000;
    private static final int[] searchBandwidths = new int[]{1,5,10,15,20};
    private static final boolean runPOSMIT = true;
    private static final boolean runCBSMOT = true;
    private static final boolean runSMOT = false;

    public static void main(String[] args) {

        //generate a stop/move dataset
        System.out.print("entries,");
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

        for (int nEntries = datasetStartingSize; nEntries <= datasetEndingSize; nEntries+=increment) {

            int nStops = nEntries/20;
            STStopTrajectory traj = DataGeneratorUtil.generateTrajectoryWithStops(
                    nEntries,
                    nStops,
                    1000L,
                    10000,
                    20,
                    0.3,
                    startLat,
                    startLon);


            final double spatialParam = algoPOSMIT.estimateStopVariance(traj);

            System.out.print(nEntries + ",");
            System.gc();

            //time POSMIT

            if(runPOSMIT){
                for (int searchBandwidth : searchBandwidths) {
                    long startingTimePOSMIT = bean.getCurrentThreadUserTime();
                    double[] stopPrs = algoPOSMIT.run(traj, searchBandwidth, spatialParam);
                    algoPOSMIT.toStopTrajectory(traj, stopPrs, 0.5);
                    long runningTimePOSMIT = (bean.getCurrentThreadUserTime() - startingTimePOSMIT)/1000000L;
                    System.out.print(runningTimePOSMIT + ",");
                    System.gc();
                }
            }

            //time CB-SMoT
            if(runCBSMOT){
                long startingTimeCBSMOT = bean.getCurrentThreadUserTime();
                algoCBSMOT.run(traj, spatialParam, 10000L);
                long runningTimeCBSMOT = (bean.getCurrentThreadUserTime() - startingTimeCBSMOT)/1000000L;
                System.out.print(runningTimeCBSMOT + ",");
                System.gc();
            }

            //time SMoT
            if(runSMOT){
                long startingTimeSMOT = bean.getCurrentThreadUserTime();
                ALGO_SMOT.run(traj, spatialParam, 10000L);
                long runningTimeSMOT = (bean.getCurrentThreadUserTime() - startingTimeSMOT)/1000000L;
                System.out.print(runningTimeSMOT + ",");
            }

            System.out.print("\n");

        }

    }

}
