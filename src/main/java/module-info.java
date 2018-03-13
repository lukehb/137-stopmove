import onethreeseven.jclimod.AbstractCommandsListing;
import onethreeseven.stopmove.command.StopMoveCommandListing;
import onethreeseven.stopmove.view.StopMoveContextMenuSupplier;
import onethreeseven.trajsuitePlugin.model.EntitySupplier;
import onethreeseven.trajsuitePlugin.model.TransactionProcessor;
import onethreeseven.trajsuitePlugin.view.EntityContextMenuSupplier;

module onethreeseven.stopmove{
    requires jcommander;
    requires onethreeseven.datastructures;
    requires onethreeseven.trajsuitePlugin;
    requires onethreeseven.common;
    requires java.management;
    requires java.desktop;

    exports onethreeseven.stopmove.algorithm;
    exports onethreeseven.stopmove.command;
    exports onethreeseven.stopmove to javafx.graphics;

    //for commands
    opens onethreeseven.stopmove.command to jcommander, onethreeseven.jclimod;

    uses EntitySupplier;
    uses TransactionProcessor;
    provides AbstractCommandsListing with StopMoveCommandListing;
    provides EntityContextMenuSupplier with StopMoveContextMenuSupplier;

}