package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;

import com.google.inject.Inject;

import jam.data.AbstractHistogram;
import jam.data.control.HistogramZero;
import jam.global.Nameable;

/**
 * Show the zero histograms dialog.
 * @author Ken Swartz
 */
@SuppressWarnings("serial")
final class ShowDialogZeroHistogram extends AbstractShowDialog implements
        PropertyChangeListener, Predicate<Nameable> {

    private final transient PropertyChangeListener selectionObserver = new SelectionObserver(
            this, this);

    /**
     * Initialize command.
     */
    @Inject
    ShowDialogZeroHistogram(final HistogramZero histogramZero) {
        super("Zero\u2026");
        final Icon iZero = loadToolbarIcon("jam/ui/Zero.png");
        putValue(Action.SMALL_ICON, iZero);
        putValue(Action.SHORT_DESCRIPTION, "Zero Histograms.");
        dialog = histogramZero;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // this.selectionObserver.update(observe, obj);
        this.selectionObserver.propertyChange(evt);
    }

    public boolean evaluate(final Nameable selected) {
        return !AbstractHistogram.getHistogramList().isEmpty();
    }
}
