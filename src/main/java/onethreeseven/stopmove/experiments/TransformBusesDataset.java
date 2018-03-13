package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.SpatioCompositeTrajectoryWriter;
import onethreeseven.datastructures.data.resolver.*;
import onethreeseven.datastructures.model.STStopPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.Kneedle;
import onethreeseven.stopmove.algorithm.POSMIT;
import onethreeseven.stopmove.algorithm.StopClassificationStats;
import onethreeseven.stopmove.algorithm.UnivariateKMeans;

import java.io.File;
import java.io.IOException;
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
public class TransformBusesDataset {

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
            new LatFieldResolver(9),
            new LonFieldResolver(8),
            new TemporalFieldResolver(microsToTime, 0),
            new StopFieldResolver(14, "1"), true);


    private static final File inFile = new File(FileUtil.makeAppDir("traj"), "buses.txt");

    public static void main(String[] args) throws IOException {
        writeTrajsOut(busesParser.parse(inFile));
    }

    public static void writeTrajsOut(Map<String, STStopTrajectory> trajs){

        double stopVariance = 10;

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

            System.out.println("File output at: " + outFile.getAbsolutePath());
        }
    }


}
