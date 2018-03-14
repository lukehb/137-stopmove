package onethreeseven.stopmove.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import onethreeseven.stopmove.command.AbstractStopMoveCommand;
import onethreeseven.stopmove.command.FindStopsMovesCBSMoT;

/**
 * Controller for CBSMoT view fxml.
 * @author Luke Bermingham
 */
public class CBSMoTViewController extends AbstractStopMoveViewController {

    @FXML
    public Spinner<Double> spatialParamSpinner;
    @FXML
    public Spinner<Integer> minStopTimeSecSpinner;
    @FXML
    public Label nSelectedTrajsLabel;
    @FXML
    public Label feedbackLabel;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Button stopMovesBtn;

    @Override
    public void initialize() {
        super.initialize();

        spatialParamSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100000, 10, 1));

        minStopTimeSecSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000000, 60));
    }

    @Override
    protected String[] generateCommandString() {
        return new String[]{
                "cbsmot",
                "-e", String.valueOf(spatialParamSpinner.getValue()),
                "-t", String.valueOf(minStopTimeSecSpinner.getValue())
        };
    }

    @Override
    protected Button getStopMoveBtn() {
        return stopMovesBtn;
    }

    @Override
    protected Label getFeedbackLabel() {
        return feedbackLabel;
    }

    @Override
    protected Label getNSelectedTrajsLabel() {
        return nSelectedTrajsLabel;
    }

    @Override
    protected ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    protected AbstractStopMoveCommand getCommand() {
        return new FindStopsMovesCBSMoT();
    }
}
