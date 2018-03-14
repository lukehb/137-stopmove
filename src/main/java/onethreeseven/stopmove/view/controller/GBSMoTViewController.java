package onethreeseven.stopmove.view.controller;

import onethreeseven.stopmove.command.AbstractStopMoveCommand;
import onethreeseven.stopmove.command.FindStopsMovesGBSMoT;

/**
 * view controller for gbsmot
 * {@link onethreeseven.stopmove.algorithm.GBSMoT}
 * @author Luke Bermingham
 */
public class GBSMoTViewController extends CBSMoTViewController {

    @Override
    protected String[] generateCommandString() {
        return new String[]{
                "gbsmot",
                "-r", String.valueOf(spatialParamSpinner.getValue()),
                "-t", String.valueOf(minStopTimeSecSpinner.getValue())
        };
    }

    @Override
    protected AbstractStopMoveCommand getCommand() {
        return new FindStopsMovesGBSMoT();
    }
}
