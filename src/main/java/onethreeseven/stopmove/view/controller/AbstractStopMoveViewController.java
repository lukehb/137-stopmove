package onethreeseven.stopmove.view.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import onethreeseven.jclimod.CLIProgram;
import onethreeseven.stopmove.command.AbstractStopMoveCommand;
import onethreeseven.stopmove.command.FindStopsMovesPOSMIT;
import onethreeseven.trajsuitePlugin.model.BaseTrajSuiteProgram;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Todo: write documentation
 *
 * @author Luke Bermingham
 */
public abstract class AbstractStopMoveViewController {

    @FXML
    public void initialize(){
        ProgressBar progressBar = getProgressBar();
        progressBar.setProgress(0);
        progressBar.setDisable(true);

        //setup ntrajs label
        int nTrajsInit = getCommand().getSelectedTrajs().size();
        updateNTrajsSelected(nTrajsInit);

        //update n selected trajs when num edited entities changes
        BaseTrajSuiteProgram.getInstance().getLayers().numEditedEntitiesProperty.addListener((observable, oldValue, newValue) -> Platform.runLater(()->{
            updateNTrajsSelected(getCommand().getSelectedTrajs().size());
        }));

        //update progress bar from the command's progress
        getCommand().setProgressReporter(progress -> Platform.runLater(()-> progressBar.setProgress(progress)));
    }

    private void updateNTrajsSelected(int nSelectedTrajs){
        getNSelectedTrajsLabel().setText(String.valueOf(nSelectedTrajs));
        Label feedbackLabel = getFeedbackLabel();
        Button stopMovesBtn = getStopMoveBtn();
        if(nSelectedTrajs < 1){
            feedbackLabel.setText("Need to select some spatio-temporal trajectories.");
            stopMovesBtn.setDisable(true);
        }
        else{
            feedbackLabel.setText("");
            stopMovesBtn.setDisable(false);
        }
    }

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AbstractStopMoveCommand command = getCommand();

    @FXML
    public void onStopMovesBtnClicked(ActionEvent actionEvent) {

        if(isRunning.get()){
            command.stop();
            getStopMoveBtn().setDisable(true);
        }
        else{
            getStopMoveBtn().setText("Cancel");
            getProgressBar().setDisable(false);

            CompletableFuture.runAsync(()->{
                CLIProgram prog = new CLIProgram();
                prog.addCommand(command);
                isRunning.set(true);
                prog.doCommand(generateCommandString());
            }).handle((aVoid, throwable) -> {
                isRunning.set(false);
                if(throwable != null){
                    Platform.runLater(()->{
                        getProgressBar().setProgress(0);
                        getProgressBar().setDisable(true);
                        getStopMoveBtn().setDisable(false);
                        getStopMoveBtn().setText("Find Stops/Moves");
                    });
                }else{
                    Platform.runLater(()->{
                        ((Stage)(getStopMoveBtn().getScene().getWindow())).close();
                    });
                }
                return null;
            });
        }

    }

    protected abstract String[] generateCommandString();
    protected abstract Button getStopMoveBtn();
    protected abstract Label getFeedbackLabel();
    protected abstract Label getNSelectedTrajsLabel();
    protected abstract ProgressBar getProgressBar();
    protected abstract AbstractStopMoveCommand getCommand();

}
