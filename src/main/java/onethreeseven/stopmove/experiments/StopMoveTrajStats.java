package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.resolver.*;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.CountStopsAndMoves;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Compute some basic stats about Stop/Move Trajectories
 * @author Luke Bermingham
 */
public class StopMoveTrajStats {

    //private static final String filename = "ferry";
    private static final String filename = "hike";
    //private static final String filename = "shopping_trip";
    //private static final String filename = "bus_a_33320";
    //private static final String filename = "bus_b_33424";
    //private static final String filename = "bus_c_38092";

    private static final File trajFile = new File(FileUtil.makeAppDir("traj"), filename + ".txt");
    private static final CountStopsAndMoves statsAlgo = new CountStopsAndMoves();

    public static void main(String[] args) throws IOException {

        STStopTrajectoryParser parser = new STStopTrajectoryParser(
                new ProjectionEquirectangular(),
                new IdFieldResolver(0),
                new LatFieldResolver(1),
                new LonFieldResolver(2),
                new TemporalFieldResolver(3),
                new StopFieldResolver(4), false);

        Map<String, STStopTrajectory> trajs = parser.parse(trajFile);

        for (Map.Entry<String, STStopTrajectory> trajEntry : trajs.entrySet()) {

            System.out.println("Stats for trajectory: " + trajEntry.getKey());
            statsAlgo.run(trajEntry.getValue());

            System.out.println(
                    "#Stops = " + statsAlgo.getnStops() +
                            "\n#Moves = " + statsAlgo.getnMoves() +
                            "\nnStopEpisodes = " + statsAlgo.getnStopEpisodes() +
                            "\nnMoveEpisodes = " + statsAlgo.getnMoveEpisodes() +
                            "\nduration (s) = " + statsAlgo.getDurationSeconds() +
                            "\ninterval (s) = " + statsAlgo.getIntervalSeconds() +
                            "\nmin interval (s) = " + statsAlgo.getMinIntervalSeconds() +
                            "\nmax interval (s) = " + statsAlgo.getMaxIntervalSeconds() +
                            "\nmodal sampling rate (s) = " + statsAlgo.getModalSamplingSeconds()
            );
        }
    }

}
