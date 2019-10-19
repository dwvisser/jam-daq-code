package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.ui.SelectionTree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

/**
 * Observer that is common to several commands that just
 * observe the tree selections.
 * @author Dale Visser
 */
public final class SelectionObserver implements PropertyChangeListener {

    private final transient Action action;
    private final transient Predicate<Nameable> predicate;

    protected SelectionObserver(final Action action,
            final Predicate<Nameable> predicate) {
        this.action = action;
        this.predicate = predicate;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final BroadcastEvent.Command command = ((BroadcastEvent) evt).getCommand();
        if ((command == BroadcastEvent.Command.GROUP_SELECT)
                || (command == BroadcastEvent.Command.ROOT_SELECT)) {
            this.action.setEnabled(false);
        } else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
                || (command == BroadcastEvent.Command.GATE_SELECT)) {
            final Nameable hist = SelectionTree.getCurrentHistogram();
            this.action.setEnabled(this.predicate.evaluate(hist));
        }
    }

}
