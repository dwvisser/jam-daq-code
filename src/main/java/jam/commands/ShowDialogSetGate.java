package jam.commands;

import com.google.inject.Inject;
import jam.data.AbstractHistogram;
import jam.data.control.GateSet;
import jam.global.Nameable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

/**
 * Show the scalers dialog box.
 * @author Ken Swartz
 */
final class ShowDialogSetGate extends AbstractShowDialog implements PropertyChangeListener,
        Predicate<Nameable> {

    private final transient PropertyChangeListener selectionObserver = new SelectionObserver(
            this, this);

    @Inject
    ShowDialogSetGate(final GateSet gateSet) {
        super();
        dialog = gateSet;
        putValue(NAME, "Set\u2026");
        final Icon iGateSet = loadToolbarIcon("jam/ui/GateSet.png");
        putValue(Action.SMALL_ICON, iGateSet);
        putValue(Action.SHORT_DESCRIPTION, "Set Gate.");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // this.selectionObserver.update(observe, obj);
        this.selectionObserver.propertyChange(evt);
    }

    public boolean evaluate(final Nameable selected) {
        boolean result = false;
        if (selected instanceof AbstractHistogram) {
            final AbstractHistogram hist = (AbstractHistogram) selected;
            result = hist.getGateCollection().getGates().isEmpty();
            result ^= true; // bitwise XOR is a really fast inversion
        }

        return result;
    }
}
