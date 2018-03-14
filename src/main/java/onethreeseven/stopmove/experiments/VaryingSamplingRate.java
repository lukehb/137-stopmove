package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.resolver.*;
import onethreeseven.datastructures.model.STStopPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.util.CountStopsAndMoves;
import onethreeseven.geo.projection.AbstractGeographicProjection;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.*;

import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;

/**
 * Todo: write documentation
 *
 * @author Luke Bermingham
 */
public class VaryingSamplingRate {


    private static final String filename = "ferry";
    //private static final String filename = "hike";

    private static final File inFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");
    private static final AbstractGeographicProjection projection = new ProjectionEquirectangular();

    public static void main(String[] args) throws IOException {

        final Map<String, STStopTrajectory> trajMap = new STStopTrajectoryParser(
                projection,
                new IdFieldResolver(0),
                new LatFieldResolver(1),
                new LonFieldResolver(2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4),
                true).parse(inFile);


        final long minSamplingRateMillis = 1000;
        final long maxSamplingRateMillis = 150000L;
        final long samplingRateStep = 1000L;

        System.out.println("Varying sampling rate for: " + filename + " dataset.");

        for (STStopTrajectory traj : trajMap.values()) {
            varySamplingRate(traj, minSamplingRateMillis, maxSamplingRateMillis, samplingRateStep);
        }

    }

    private static void varySamplingRate(STStopTrajectory traj, long minSamplingRate, long maxSamplingRate, long samplingRateStep){

        CountStopsAndMoves datasetStats = new CountStopsAndMoves();
        datasetStats.run(traj);

        final POSMIT algoPOSMIT = new POSMIT();
        final CBSMoT algoCBSMoT = new CBSMoT();
        final GBSMoT algoGBSMoT = new GBSMoT();
        final StopClassificationStats stats = new StopClassificationStats();

        System.out.println("Sampling Rate, " +
                "POSMIT_MinStopPr_Est, " +
//                "POSMIT_MinStopPr_025, " +
//                "POSMIT_MinStopPr_050, " +
//                "POSMIT_MinStopPr_075, " +
                "CBSMOT, " +
                "SMOT");

        for (long samplingRate = minSamplingRate; samplingRate <= maxSamplingRate; samplingRate+=samplingRateStep) {

            System.out.print(samplingRate + ",");

            STStopTrajectory resampledTraj = resampleTrajectory(traj, samplingRate);

            double stopVariance = algoPOSMIT.estimateStopVariance(resampledTraj);
            int searchRadius = algoPOSMIT.estimateSearchRadius(resampledTraj, stopVariance);

            //posmit
            {
                double[] stopPrs = algoPOSMIT.run(resampledTraj, searchRadius, stopVariance);
                double minStopPr = algoPOSMIT.estimateMinStopPr(stopPrs);
                stats.calculateStats(resampledTraj, algoPOSMIT.toStopTrajectory(resampledTraj, stopPrs, minStopPr));
                System.out.print(stats.getMCC() + ",");

//                for (minStopPr = 0.25; minStopPr <= 0.75; minStopPr+=0.25) {
//                    stats.calculateStats(resampledTraj, algoPOSMIT.toStopTrajectory(resampledTraj, stopPrs, minStopPr));
//                    System.out.print(stats.getMCC() + ",");
//                    System.out.print("");
//                }

            }

            //cbsmot
            {
                stopVariance = 1;
                STStopTrajectory computedTraj = algoCBSMoT.run(resampledTraj, stopVariance, samplingRate * searchRadius);
                stats.calculateStats(resampledTraj, computedTraj);
                System.out.print(stats.getMCC() + ",");
            }

            //smot
            {
                stopVariance = 7; //Math.max(0.5, stopVariance);
                STStopTrajectory computedTraj = algoGBSMoT.run(resampledTraj, stopVariance, samplingRate * searchRadius);
                stats.calculateStats(resampledTraj, computedTraj);
                System.out.print(stats.getMCC() + "\n");
            }

            //System.out.print(stopVariance + "," + searchRadius + "\n");

        }

    }

    private static STStopTrajectory resampleTrajectory(STStopTrajectory traj, long targetSamplingRateMillis){

        long durationPassed = 0L;
        STStopTrajectory resampledTraj = new STStopTrajectory(traj.isInCartesianMode(), traj.getProjection());

        //add the first entry no matter
        Iterator<STStopPt> iter = traj.iterator();
        STStopPt prevPt = iter.next();
        resampledTraj.add(prevPt);

        while(iter.hasNext()){
            STStopPt curEntry = iter.next();
            long deltaMillis = ChronoUnit.MILLIS.between(prevPt.getTime(), curEntry.getTime());
            durationPassed += deltaMillis;
            if(durationPassed >= targetSamplingRateMillis){
                resampledTraj.add(curEntry);
                durationPassed = 0L;
            }
            prevPt = curEntry;
        }
        return resampledTraj;
    }


}
