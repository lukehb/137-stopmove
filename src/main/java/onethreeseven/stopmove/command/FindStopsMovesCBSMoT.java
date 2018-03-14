package onethreeseven.stopmove.command;

import com.beust.jcommander.Parameter;
import onethreeseven.datastructures.model.STPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import onethreeseven.stopmove.algorithm.CBSMoT;
import java.util.Map;

/**
 * Command for {@link onethreeseven.stopmove.algorithm.CBSMoT}
 * @author Luke Bermingham
 */
public class FindStopsMovesCBSMoT extends AbstractStopMoveCommand {

    @Parameter(names = {"-e", "-epsMetres"}, description = "This search distance for surrounding entries when formulating stops (metres).")
    private double epMetres;

    @Parameter(names = {"-t", "-minStopTime"}, description = "The minimum time for a trajectory to be considered stopping (seconds).")
    private int minStopTimeSeconds;

    @Override
    protected String generateLayerNameForNewStopMoveTrajs(Map<String, SpatioCompositeTrajectory<? extends STPt>> allTrajs) {
        return allTrajs.size() + " Stop/Moves Trajectories (CBSMOT) Eps Metres=" + epMetres + "m";
    }

    @Override
    protected boolean parametersValid() {
        if(epMetres < 0){
            System.err.println("Epsilon metres must be a positive integer.");
            return false;
        }
        if(minStopTimeSeconds < 0){
            System.err.println("Minimum stop time must be greater than zero.");
            return false;
        }
        return super.parametersValid();
    }

    @Override
    protected STStopTrajectory toStopMoveTraj(SpatioCompositeTrajectory<? extends STPt> traj) {
        CBSMoT algo = new CBSMoT();
        long minTimeMillis = minStopTimeSeconds * 1000L;
        return algo.run(traj, epMetres, minTimeMillis);
    }

    @Override
    protected String getUsage() {
        return "cbsmot -e 20 -t 60";
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
        return "cbsmot";
    }

    @Override
    public String[] getOtherCommandNames() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "Find stops by looking for dense clusters of entries in sequence that stay in a cluster for a user-specified stopping duration.";
    }

}
