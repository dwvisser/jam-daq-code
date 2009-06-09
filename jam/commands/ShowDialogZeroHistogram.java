package jam.commands;

import jam.data.AbstractHistogram;
import jam.data.control.HistogramZero;
import jam.global.Nameable;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;

import com.google.inject.Inject;

/**
 * Show the zero histograms dialog.
 * @author Ken Swartz
 */
final class ShowDialogZeroHistogram extends AbstractShowDialog implements
        Observer, Predicate<Nameable> {

    private final transient Observer selectionObserver = new SelectionObserver(
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

    public void update(final Observable observe, final Object obj) {
        this.selectionObserver.update(observe, obj);
    }

    public boolean evaluate(final Nameable selected) {
        return !AbstractHistogram.getHistogramList().isEmpty();
    }
}
