package onethreeseven.stopmove.experiments;

import onethreeseven.common.util.FileUtil;
import onethreeseven.datastructures.data.STStopTrajectoryParser;
import onethreeseven.datastructures.data.resolver.IdFieldResolver;
import onethreeseven.datastructures.data.resolver.NumericFieldsResolver;
import onethreeseven.datastructures.data.resolver.StopFieldResolver;
import onethreeseven.datastructures.data.resolver.TemporalFieldResolver;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.geo.projection.ProjectionEquirectangular;
import onethreeseven.stopmove.algorithm.CountStopsAndMoves;
import java.io.File;
import java.util.Map;

/**
 * Compute some basic stats about Stop/Move Trajectories
 * @author Luke Bermingham
 */
public class StopMoveTrajStats {

    private static final File trajFile = new File(FileUtil.makeAppDir("traj"), "dog_walk.txt");
    private static final CountStopsAndMoves statsAlgo = new CountStopsAndMoves();

    public static void main(String[] args) {

        STStopTrajectoryParser parser = new STStopTrajectoryParser(
                new ProjectionEquirectangular(),
                new IdFieldResolver(0),
                new NumericFieldsResolver(1,2),
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
                            "\nduration (s) = " + statsAlgo.getDurationSeconds()
            );
        }
    }

}
