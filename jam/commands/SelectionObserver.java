package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.ui.SelectionTree;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;

/**
 * Implementation of Observer that is common to several commands that just
 * observe the tree selections.
 * @author Dale Visser
 */
public final class SelectionObserver implements Observer {

    private final transient Action action;
    private final transient Predicate<Nameable> predicate;

    protected SelectionObserver(final Action action,
            final Predicate<Nameable> predicate) {
        this.action = action;
        this.predicate = predicate;
    }

    /**
     * Observer implementation.
     * @param observe
     *            ignored
     * @param obj
     *            the command event
     */
    public void update(final Observable observe, final Object obj) {
        final BroadcastEvent event = (BroadcastEvent) obj;
        final BroadcastEvent.Command command = event.getCommand();
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
