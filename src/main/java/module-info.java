import onethreeseven.jclimod.AbstractCommandsListing;
import onethreeseven.stopmove.command.StopMoveCommandListing;
import onethreeseven.stopmove.view.StopMoveMenuSupplier;
import onethreeseven.trajsuitePlugin.model.EntitySupplier;
import onethreeseven.trajsuitePlugin.model.TransactionProcessor;
import onethreeseven.trajsuitePlugin.view.MenuSupplier;

module onethreeseven.stopmove{
    requires jcommander;
    requires onethreeseven.datastructures;
    requires onethreeseven.trajsuitePlugin;
    requires onethreeseven.common;
    requires java.management;
    requires java.desktop;

    exports onethreeseven.stopmove.view;
    exports onethreeseven.stopmove.algorithm;
    exports onethreeseven.stopmove.command;
    exports onethreeseven.stopmove to javafx.graphics;

    //for commands
    opens onethreeseven.stopmove.command to jcommander, onethreeseven.jclimod;

    uses EntitySupplier;
    uses TransactionProcessor;
    provides AbstractCommandsListing with StopMoveCommandListing;

    //for top menus
    provides MenuSupplier with StopMoveMenuSupplier;

    //for load view fxml to work
    opens onethreeseven.stopmove.view;
    exports onethreeseven.stopmove.view.controller to javafx.fxml;
    opens onethreeseven.stopmove.view.controller to javafx.fxml;

}