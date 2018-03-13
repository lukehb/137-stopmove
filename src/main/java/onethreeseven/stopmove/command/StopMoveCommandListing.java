package onethreeseven.stopmove.command;

import com.beust.jcommander.JCommander;
import onethreeseven.jclimod.AbstractCommandsListing;
import onethreeseven.jclimod.CLICommand;

/**
 * Commands in this module.
 * @author Luke Bermingham
 */
public class StopMoveCommandListing extends AbstractCommandsListing {
    @Override
    protected CLICommand[] createCommands(JCommander jc, Object... args) {
        return new CLICommand[]{
                new FindStopsMovesPOSMIT(),
                new FindStopsMovesGBSMoT()
        };
    }
}
