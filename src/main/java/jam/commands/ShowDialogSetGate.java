package jam.commands;

import com.google.inject.Inject;
import jam.data.AbstractHistogram;
import jam.data.control.GateSet;
import jam.global.Nameable;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Show the scalers dialog box.
 * @author Ken Swartz
 */
final class ShowDialogSetGate extends AbstractShowDialog implements Observer,
        Predicate<Nameable> {

    private final transient Observer selectionObserver = new SelectionObserver(
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

    public void update(final Observable observe, final Object obj) {
        this.selectionObserver.update(observe, obj);
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
