package onethreeseven.stopmove;

import javafx.stage.Stage;
import onethreeseven.trajsuitePlugin.model.BaseTrajSuiteProgram;
import onethreeseven.trajsuitePlugin.view.BasicFxApplication;

/**
 * Entry point for running stop/move module as standalone.
 * @author Luke Bermingham
 */
public class Main extends BasicFxApplication {
    @Override
    protected BaseTrajSuiteProgram preStart(Stage stage) {
        return BaseTrajSuiteProgram.getInstance();
    }

    @Override
    public String getTitle() {
        return "StopMove module";
    }

    @Override
    public int getStartWidth() {
        return 640;
    }

    @Override
    public int getStartHeight() {
        return 480;
    }

    @Override
    protected void afterStart(Stage stage) {
        super.afterStart(stage);

        BaseTrajSuiteProgram.getInstance().getCLI().doCommand(new String[]{
              "gt", "-ne", "1000", "-nt", "50", "-n", "5"
        });

        System.out.println("Type lc to see commands.");
        BaseTrajSuiteProgram.getInstance().getCLI().startListeningForInput();

    }
}
