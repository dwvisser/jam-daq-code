package jam.commands;

import jam.data.AbstractHistogram;
import jam.data.Gate;
import jam.global.Nameable;

import java.util.Observable;
import java.util.Observer;

/**
 * Provides Observer implementation for gate dialog subclasses.
 * @author Dale Visser
 */
public abstract class AbstractShowGateDialog extends AbstractShowDialog
        implements Observer, Predicate<Nameable> {

    private final transient Observer selectionObserver = new SelectionObserver(
            this, this);

    /**
     * @param string
     *            to match superclass
     */
    public AbstractShowGateDialog(final String string) {
        super(string);
    }

    /**
     * Makes dialog respond to tree selection events.
     * @param observe
     *            unused
     * @param obj
     *            command object
     */
    public final void update(final Observable observe, final Object obj) {
        this.selectionObserver.update(observe, obj);
    }

    /**
     * Predicate implementation.
     * @param selected
     *            the item selected
     * @return whether the command should be enabled
     */
    public final boolean evaluate(final Nameable selected) {
        return !Gate.getGateList().isEmpty()
                && selected instanceof AbstractHistogram;
    }

}
