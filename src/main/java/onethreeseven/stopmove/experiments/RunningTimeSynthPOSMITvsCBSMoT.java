package onethreeseven.stopmove.experiments;

import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.util.DataGeneratorUtil;
import onethreeseven.stopmove.algorithm.CBSMoT;
import onethreeseven.stopmove.algorithm.POSMIT;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Measure the running time for both POSMIT and CB-SMoT using
 * increasingly larger synthetic data-sets.
 * @author Luke Bermingham
 */
public class RunningTimeSynthPOSMITvsCBSMoT {

    private static final int datasetStartingSize = 10000;
    private static final int datasetEndingSize = 10000000;
    private static final int increment = 100000;
    private static final POSMIT algoPOSMIT = new POSMIT();
    private static final CBSMoT algoCBSMOT = new CBSMoT();
    private static final double startLat = -16.9186;
    private static final double startLon = 145.7781;

    private static final ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    public static void main(String[] args) {

        //generate a stop/move dataset
        System.out.println("#Entries,POSMIT(ms),CB-SMoT(ms)");

        for (int nEntries = datasetStartingSize; nEntries <= datasetEndingSize; nEntries+=increment) {

            int nStops = nEntries/20;
            STStopTrajectory traj = DataGeneratorUtil.generateTrajectoryWithStops(
                    nEntries, nStops, 1000L, 10, 0.3, startLat, startLon);

            int searchRadius = 10;
            double spatialParam = algoPOSMIT.calculateStopVariance(traj, searchRadius);

            System.gc();

            //time POSMIT
            long runningTimePOSMIT = bean.getCurrentThreadUserTime();
            double[] stopPrs = algoPOSMIT.run(traj, 10, spatialParam);
            algoPOSMIT.toStopTrajectory(traj, stopPrs, 0.8);
            runningTimePOSMIT = (bean.getCurrentThreadUserTime() - runningTimePOSMIT)/1000000L;

            System.gc();

            //time CB-SMoT
            long runningtimeCBSMOT = bean.getCurrentThreadUserTime();
            algoCBSMOT.run(traj, spatialParam, 10000L);
            runningtimeCBSMOT = (bean.getCurrentThreadUserTime() - runningtimeCBSMOT)/1000000L;

            System.out.println(nEntries + "," + runningTimePOSMIT + "," + runningtimeCBSMOT);
        }

    }

}
