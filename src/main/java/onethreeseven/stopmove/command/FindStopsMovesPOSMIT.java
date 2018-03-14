package onethreeseven.stopmove.command;

import com.beust.jcommander.Parameter;
import onethreeseven.datastructures.model.STPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import onethreeseven.stopmove.algorithm.POSMIT;
import java.util.Map;

/**
 * CLI command for {@link onethreeseven.stopmove.algorithm.POSMIT}
 * @author Luke Bermingham
 */
public class FindStopsMovesPOSMIT extends AbstractStopMoveCommand {

    @Parameter(names = {"-hd", "--stopVariance"},
            description = "Approximately the maximum spatial displacement a true stop is allowed to have.")
    private Double stopVariance = null;

    @Parameter(names = {"-hi", "--indexNeighbourhood"},
            description = "When checking for a stop, how many indices to sample either side of the current entry.")
    private Integer indexNeighbourhood = null;

    @Parameter(names = {"-pr", "--minStopPr"}, description = "Each entry has its stop probability calculated, " +
            "this value represents the minimum stop probability for an entry to be classified as a stop.")
    private double minStopPr = 0.7;

    @Override
    protected String getUsage() {
        return "posmit -hd 10 -hi 5 -pr 0.7";
    }

    @Override
    protected boolean parametersValid() {
        if(stopVariance != null && stopVariance <= 0){
            System.err.println("Stop variance must be greater than zero.");
            return false;
        }
        if(indexNeighbourhood != null && indexNeighbourhood < 1){
            System.err.println("Index neighbourhood must be one or greater.");
            return false;
        }
        if(minStopPr < 0 || minStopPr > 1){
            System.err.println("Minimum stop probability must between 0 and 1.");
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
        return "posmit";
    }

    @Override
    public String[] getOtherCommandNames() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "Computes the stop probability of each entry in a trajectory, then all entries with a " +
                "stop probability equal to or greater than the specified minimum stop probability " +
                "parameter become stops and the rest become moves.";
    }

    @Override
    protected String generateLayerNameForNewStopMoveTrajs(Map<String, SpatioCompositeTrajectory<? extends STPt>> allTrajs) {
        return allTrajs.size() + " Stop/Moves Trajectories (POSMIT) StopPr=" + (int)(minStopPr * 100) + "%";
    }

    @Override
    protected STStopTrajectory toStopMoveTraj(SpatioCompositeTrajectory<? extends STPt> traj) {
        POSMIT posmit = new POSMIT();
        //get params (potentially estimating them if not passed in)
        double hd = this.stopVariance == null ?
                posmit.estimateStopVariance(traj) : this.stopVariance;

        int hi = this.indexNeighbourhood == null ?
                posmit.estimateSearchRadius(traj, hd) : this.indexNeighbourhood;

        double[] stopPrs = posmit.run(traj, hi, hd);
        return posmit.toStopTrajectory(traj, stopPrs, minStopPr);
    }
}
