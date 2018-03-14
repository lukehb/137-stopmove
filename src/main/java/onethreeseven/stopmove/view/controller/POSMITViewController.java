package onethreeseven.stopmove.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import onethreeseven.stopmove.command.AbstractStopMoveCommand;
import onethreeseven.stopmove.command.FindStopsMovesPOSMIT;
import java.util.ArrayList;

/**
 * Controller for stop/move commands
 * @author Luke Bermingham
 */
public class POSMITViewController extends AbstractStopMoveViewController {

    @FXML
    public CheckBox estimateHdCheckbox;

    @FXML
    public CheckBox estimateHiCheckbox;

    @FXML
    public Spinner<Double> hdSpinner;

    @FXML
    public Spinner<Integer> hiSpinner;

    @FXML
    public Spinner<Integer> minStopPrSpinner;

    @FXML
    public ProgressBar progressBar;

    @FXML
    public Button stopMovesBtn;

    @FXML
    public Label nSelectedTrajsLabel;

    @FXML
    public Label feedbackLabel;

    @FXML
    public void initialize(){
        super.initialize();
        //bind spinner disabled to checkbox
        hdSpinner.disableProperty().bind(estimateHdCheckbox.selectedProperty());
        hiSpinner.disableProperty().bind(estimateHiCheckbox.selectedProperty());

        estimateHdCheckbox.setSelected(true);
        estimateHiCheckbox.setSelected(true);

        //setup spinners
        hiSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 5));
        hdSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100000, 5, 1));
        minStopPrSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 70));
    }

    @Override
    protected AbstractStopMoveCommand getCommand(){
        return new FindStopsMovesPOSMIT();
    }

    @Override
    protected String[] generateCommandString(){

        double minStopPr = minStopPrSpinner.getValue() / 100.0;
        ArrayList<String> params = new ArrayList<>();
        params.add("posmit");
        params.add("-pr");
        params.add(String.valueOf(minStopPr));

        //add hd param (if not estimating)
        if(!estimateHdCheckbox.isSelected()){
            double hd = hdSpinner.getValue();
            params.add("-hd");
            params.add(String.valueOf(hd));
        }

        //add hi param (if not estimating)
        if(!estimateHiCheckbox.isSelected()){
            int hi = hiSpinner.getValue();
            params.add("-hi");
            params.add(String.valueOf(hi));
        }

        String[] paramsArr = new String[params.size()];
        params.toArray(paramsArr);
        return paramsArr;
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

}
