package onethreeseven.stopmove.command;

import com.beust.jcommander.Parameter;
import onethreeseven.datastructures.model.STPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import onethreeseven.stopmove.algorithm.GBSMoT;

import java.util.Map;

/**
 * Command for {@link GBSMoT}.
 * @author Luke Bermingham
 */
public class FindStopsMovesGBSMoT extends AbstractStopMoveCommand {

    @Parameter(names = {"-r", "-regionSizeMetres"}, description = "This algorithm partitions the trajectory using grid cells of this size (metres).")
    private double regionSizeMetres;

    @Parameter(names = {"-t", "-minStopTime"}, description = "The minimum time for a trajectory to stay in a region for it to be considered a stop.")
    private int minStopTimeSeconds;

    @Override
    protected String generateLayerNameForNewStopMoveTrajs(Map<String, SpatioCompositeTrajectory<? extends STPt>> allTrajs) {
        return allTrajs.size() + " Stop/Moves Trajectories (POSMIT) Region=" + regionSizeMetres + "m";
    }

    @Override
    protected STStopTrajectory toStopMoveTraj(SpatioCompositeTrajectory<? extends STPt> traj) {
        GBSMoT gbsMoT = new GBSMoT();
        long minStopTimeMillis = minStopTimeSeconds * 1000L;
        return gbsMoT.run(traj, regionSizeMetres, minStopTimeMillis);
    }

    @Override
    protected String getUsage() {
        return "gbsmot -r 100 -t 10";
    }

    @Override
    protected boolean parametersValid() {
        if(regionSizeMetres < 0){
            System.err.println("Region size must be a positive integer.");
            return false;
        }
        if(minStopTimeSeconds < 0){
            System.err.println("Minimum stop time must be greater than zero.");
            return false;
        }
        return super.parametersValid();
    }

    @Override
    public boolean shouldStoreRerunAlias() {
        return false;
    }

    @Override
    public String generateRerunAliasBasedOnParams() {
        return null;
    }

    @Override
    public String getCommandName() {
        return "gbsmot";
    }

    @Override
    public String[] getOtherCommandNames() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "Partitions the trajectory using a grid of specified cell size and then classifies " +
                "entries that stay in grid cells for a certain duration as stops.";
    }
}
