package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jam.data.AbstractHistogram;
import jam.data.Gate;
import jam.global.Nameable;

/**
 * Provides Observer implementation for gate dialog subclasses.
 * @author Dale Visser
 */

public abstract class AbstractShowGateDialog extends AbstractShowDialog
        implements PropertyChangeListener, Predicate<Nameable> {

    private final transient PropertyChangeListener selectionObserver = new SelectionObserver(
            this, this);

    /**
     * @param string
     *            to match superclass
     */
    public AbstractShowGateDialog(final String string) {
        super(string);
    }

    @Override
    public final void propertyChange(PropertyChangeEvent evt) {
        // this.selectionObserver.update(observe, obj);
        this.selectionObserver.propertyChange(evt);
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
