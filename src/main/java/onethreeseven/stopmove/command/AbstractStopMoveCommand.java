package onethreeseven.stopmove.command;

import onethreeseven.common.util.ColorUtil;
import onethreeseven.datastructures.graphics.STStopTrajectoryGraphic;
import onethreeseven.datastructures.model.STStopTrajectory;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import onethreeseven.jclimod.CLICommand;
import onethreeseven.trajsuitePlugin.model.EntitySupplier;
import onethreeseven.trajsuitePlugin.model.TransactionProcessor;
import onethreeseven.trajsuitePlugin.model.WrappedEntity;
import onethreeseven.trajsuitePlugin.transaction.AddEntitiesTransaction;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Abstract command for stop/move command concrete implementations.
 * @author Luke Bermingham
 */
public abstract class AbstractStopMoveCommand extends CLICommand {

    protected Map<String, SpatioCompositeTrajectory> getSelectedTrajs(){

        HashMap<String, SpatioCompositeTrajectory> allSelectedTrajs = new HashMap<>();

        ServiceLoader<EntitySupplier> service = ServiceLoader.load(EntitySupplier.class);
        for (EntitySupplier entitySupplier : service) {
            Map<String, WrappedEntity> selectedTrajs = entitySupplier.supplyAllMatching(wrappedEntity -> wrappedEntity.isSelectedProperty().get()
                    && wrappedEntity.getModel() instanceof SpatioCompositeTrajectory);

            for (Map.Entry<String, WrappedEntity> entry : selectedTrajs.entrySet()) {
                allSelectedTrajs.put(entry.getKey(), (SpatioCompositeTrajectory) entry.getValue().getModel());
            }

        }

        return allSelectedTrajs;
    }

    Map<String, SpatioCompositeTrajectory> allTrajs = null;

    @Override
    protected boolean parametersValid() {
        allTrajs = getSelectedTrajs();
        if(allTrajs.isEmpty()){
            System.err.println("There was no selected trajectory to find stop/move from.");
            return false;
        }
        return true;
    }

    @Override
    protected boolean runImpl() {

        if(allTrajs == null){
            return false;
        }

        AddEntitiesTransaction transaction = new AddEntitiesTransaction();
        String layername = generateLayerNameForNewStopMoveTrajs(allTrajs);

        java.awt.Color[] colors = ColorUtil.generateNColors(allTrajs.size());

        int i = 0;
        for (Map.Entry<String, SpatioCompositeTrajectory> entry : allTrajs.entrySet()) {

            if(!isRunning.get()){
                return false;
            }

            SpatioCompositeTrajectory traj = entry.getValue();

            STStopTrajectory stopTraj = toStopMoveTraj(traj);

            String entityId = "sm_" + entry.getKey();

            STStopTrajectoryGraphic graphic = new STStopTrajectoryGraphic(stopTraj);

            Color color = colors[i];
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 128);

            graphic.fallbackColor.setValue(color);

            transaction.add(layername, entityId, stopTraj, graphic);

            i++;
        }

        //add entities
        ServiceLoader<TransactionProcessor> services = ServiceLoader.load(TransactionProcessor.class);
        for (TransactionProcessor service : services) {
            service.process(transaction);
        }

        return true;
    }

    protected abstract String generateLayerNameForNewStopMoveTrajs(Map<String, SpatioCompositeTrajectory> allTrajs);

    protected abstract STStopTrajectory toStopMoveTraj(SpatioCompositeTrajectory traj);

    @Override
    public String getCategory() {
        return "Stop/Move";
    }
}
