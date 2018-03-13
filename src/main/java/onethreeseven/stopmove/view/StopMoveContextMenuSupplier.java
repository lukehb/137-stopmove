package onethreeseven.stopmove.view;

import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.stopmove.algorithm.CountStopsAndMoves;
import onethreeseven.trajsuitePlugin.model.WrappedEntity;
import onethreeseven.trajsuitePlugin.model.WrappedEntityLayer;
import onethreeseven.trajsuitePlugin.view.*;

/**
 * Supply context menu for layer stack items relevant to this module
 * @author Luke Bermingham
 */
public class StopMoveContextMenuSupplier implements EntityContextMenuSupplier {
    @Override
    public void supplyMenuForLayer(ContextMenuPopulator populator, WrappedEntityLayer layer) {



    }

    @Override
    public void supplyMenuForEntity(ContextMenuPopulator populator, WrappedEntity entity, String parentLayer) {

        if(entity.getModel() instanceof STStopTrajectory){
            TrajSuiteMenuItem stopmoveTrajMenu = new TrajSuiteMenuItem("Stop/Move Stats", ()->{

                CountStopsAndMoves statistician = new CountStopsAndMoves();
                statistician.run((STStopTrajectory) entity.getModel());

                String title = entity.getId() + " stop/move stats";

                ViewUtil.showInformationWindow(title, statistician.getAllStats());
            });
            populator.addMenu(stopmoveTrajMenu);
        }

    }
}
