package onethreeseven.stopmove.view;

import javafx.stage.Stage;
import onethreeseven.trajsuitePlugin.model.BaseTrajSuiteProgram;
import onethreeseven.trajsuitePlugin.view.*;

/**
 * Supplies stop/move menu to other modules
 * @author Luke Bermingham
 */
public class StopMoveMenuSupplier implements MenuSupplier {
    @Override
    public void supplyMenus(AbstractMenuBarPopulator populator, BaseTrajSuiteProgram program, Stage primaryStage) {

        TrajSuiteMenu stopmoveMenu = new TrajSuiteMenu("Stops/Moves", 4);

        //posmit
        TrajSuiteMenuItem posmitMenuItem = new TrajSuiteMenuItem("POSMIT", ()->{
            ViewUtil.loadUtilityView(StopMoveMenuSupplier.class, primaryStage, "POSMIT", "/onethreeseven/stopmove/view/posmit.fxml");
        });
        stopmoveMenu.addChild(posmitMenuItem);

        //cbsmot
        TrajSuiteMenuItem cbsmotMenuItem = new TrajSuiteMenuItem("CB-SMoT", ()->{
            ViewUtil.loadUtilityView(StopMoveMenuSupplier.class, primaryStage, "CB-SMoT", "/onethreeseven/stopmove/view/cbsmot.fxml");
        });
        stopmoveMenu.addChild(cbsmotMenuItem);

        //gbsmot
        TrajSuiteMenuItem gbsmotMenuItem = new TrajSuiteMenuItem("GB-SMoT", ()->{
            ViewUtil.loadUtilityView(StopMoveMenuSupplier.class, primaryStage, "GB-SMoT", "/onethreeseven/stopmove/view/gbsmot.fxml");
        });
        stopmoveMenu.addChild(gbsmotMenuItem);

        populator.addMenu(stopmoveMenu);

    }
}
