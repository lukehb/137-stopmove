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

        TrajSuiteMenu preprocessingMenu = new TrajSuiteMenu("Pre-processing", 4);

        TrajSuiteMenu subMenuStopMove = new TrajSuiteMenu("Find Stops/Moves", 0);
        preprocessingMenu.addChild(subMenuStopMove);

        //posmit
        TrajSuiteMenuItem posmitMenuItem = new TrajSuiteMenuItem("POSMIT", ()->{
            ViewUtil.loadUtilityView(StopMoveMenuSupplier.class, primaryStage, "POSMIT", "/onethreeseven/stopmove/view/posmit.fxml");
        });
        subMenuStopMove.addChild(posmitMenuItem);

        //cbsmot
        TrajSuiteMenuItem cbsmotMenuItem = new TrajSuiteMenuItem("CB-SMoT", ()->{
            ViewUtil.loadUtilityView(StopMoveMenuSupplier.class, primaryStage, "CB-SMoT", "/onethreeseven/stopmove/view/cbsmot.fxml");
        });
        subMenuStopMove.addChild(cbsmotMenuItem);

        //gbsmot
        TrajSuiteMenuItem gbsmotMenuItem = new TrajSuiteMenuItem("GB-SMoT", ()->{
            ViewUtil.loadUtilityView(StopMoveMenuSupplier.class, primaryStage, "GB-SMoT", "/onethreeseven/stopmove/view/gbsmot.fxml");
        });
        subMenuStopMove.addChild(gbsmotMenuItem);

        populator.addMenu(preprocessingMenu);

    }
}
