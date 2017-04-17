package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.SpatioCompositeTrajectoryWriter;
import onethreeseven.datastructures.data.resolver.IdFieldResolver;
import onethreeseven.datastructures.data.resolver.NumericFieldsResolver;
import onethreeseven.datastructures.data.resolver.StopFieldResolver;
import onethreeseven.datastructures.data.resolver.TemporalFieldResolver;
import onethreeseven.datastructures.model.STStopPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.Kneedle;
import onethreeseven.stopmove.algorithm.POSMIT;
import onethreeseven.stopmove.algorithm.StopClassificationStats;
import onethreeseven.stopmove.algorithm.UnivariateKMeans;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Finds the stops of the buses.
 * @author Luke Bermingham
 */
public class FindBusStops {

    //0 - 'Timestamp micro since 1970 01 01 00:00:00 GMT
    //1 - 'Line ID
    //2 - 'Direction
    //3 - 'Journey Pattern ID
    //4 - 'Time Frame'
    //5 - Vehicle Journey ID'
    //6 - Operator (Bus operator, not the driver)
    //7 - 'Congestion [0=no,1=yes]
    //8 - 'Lon WGS84'
    //9 - Lat WGS84'
    //10 - Delay (seconds, negative if bus is ahead of schedule)
    //11 - 'Block ID (a section ID of the journey pattern)'
    //12 - Vehicle ID'
    //13 - Stop ID'
    //14 - At Stop [0=no,1=yes]

    private static final Function<String, LocalDateTime> microsToTime = s -> {
        Long micros = Long.parseLong(s);
        Long millis = micros / 1000L;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    };

    private static final STStopTrajectoryParser busesParser = new STStopTrajectoryParser(
            new ProjectionEquirectangular(),
            new IdFieldResolver(12),
            new NumericFieldsResolver(9, 8),
            new TemporalFieldResolver(microsToTime, 0),
            new StopFieldResolver(14, "1"), true);

    private static final STStopTrajectoryParser standardParser = new STStopTrajectoryParser(
            new ProjectionEquirectangular(),
            new IdFieldResolver(0),
            new NumericFieldsResolver(1,2),
            new TemporalFieldResolver(3),
            new StopFieldResolver(4),
            true
    );

    private static final File inFile = new File(FileUtil.makeAppDir("traj"), "buses.txt");

    private static final POSMIT algo = new POSMIT();
    private static final Kneedle paramEstimator = new Kneedle();
    private static final int searchRadius = 5;
    private static final StopClassificationStats stats = new StopClassificationStats();

    private static final boolean cleanUp = false;

    public static void main(String[] args) {

        if(cleanUp){
            writeTrajsOut(busesParser.parse(inFile));
        }
        else{
            File singleBusFile = new File(FileUtil.makeAppDir("traj/buses"), "33535.txt");
            Map<String, STStopTrajectory> trajs = standardParser.parse(singleBusFile);
            findStops(trajs);
        }
    }

    public static void findStops(Map<String, STStopTrajectory> trajs){

         for (Map.Entry<String, STStopTrajectory> entry : trajs.entrySet()) {
             System.out.println("Find stops for traj: " + entry.getKey());
             //estimating stop variance by finding displacements
             STStopTrajectory traj = entry.getValue();
             double stopVarianceEst = algo.estimateStopVariance(traj);
             System.out.println("Estimated stop variance: " + stopVarianceEst);
             //run posmit


             //double minStopPr = algo.estimateMinStopPr(stopPrs);
             //System.out.println("Estimated minimum stop pr cutoff: " + minStopPr);
             //STStopTrajectory stopTraj = algo.toStopTrajectory(traj, stopPrs, minStopPr);
             //stats.calculateStats(traj, stopTraj);
             //stats.printStats();

             System.out.println("StopVariance,MinStopPr,MCC");
             //go over a range of min stop prs
             for (double stopVariance = 0.5; stopVariance < 20; stopVariance+= 0.5) {
                 double[] stopPrs = algo.run(traj, searchRadius, stopVariance);
                 //turn into stop traj
                 double minStopPr = algo.estimateMinStopPr(stopPrs);

                 STStopTrajectory stopTraj = algo.toStopTrajectory(traj, stopPrs, minStopPr);
                 stats.calculateStats(traj, stopTraj);
                 System.out.println(stopVariance + "," + minStopPr + "," + stats.getMCC());
             }
        }
    }

    public static void writeTrajsOut(Map<String, STStopTrajectory> trajs){

        double stopVariance = 5;

        //write the buses into their own folder, one traj per file
        SpatioCompositeTrajectoryWriter writer = new SpatioCompositeTrajectoryWriter();
        for (Map.Entry<String, STStopTrajectory> entry : trajs.entrySet()) {
            File outFile = new File(FileUtil.makeAppDir("traj/buses"), entry.getKey() + ".txt");
            HashMap<String, STStopTrajectory> singleTraj = new HashMap<>();

            STStopTrajectory traj = entry.getValue();
            //clean-up single stops
            for (int i = 0; i < traj.size(); i++) {
                STStopPt curEntry = traj.get(i);
                double distToPrev = (i-1 >= 0) ? traj.getEuclideanDistance(i, i-1) : 100;
                double distToNext = (i+1 <= traj.size()-1) ? traj.getEuclideanDistance(i, i+1) : 100;

                //check for same entries
                if(!curEntry.isStopped() && (distToPrev < stopVariance) && (distToNext < stopVariance)){
                    curEntry.setIsStopped(true);
                }
                else if(curEntry.isStopped() && (distToPrev > stopVariance) && (distToNext > stopVariance)){
                    curEntry.setIsStopped(false);
                }
            }

            singleTraj.put(entry.getKey(), traj);
            writer.write(outFile, singleTraj);
            break;
        }
    }


}
