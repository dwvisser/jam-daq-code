package jam.commands;

import com.google.inject.Inject;
import jam.data.AbstractHistogram;
import jam.global.CommandListenerException;
import jam.global.Nameable;
import jam.plot.ComponentPrintable;
import jam.plot.PlotDisplay;
import jam.ui.SelectionTree;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;

/**
 * Command for Page Setup.
 * @author Ken Swartz
 */
final class Print extends AbstractPrintingCommand implements PropertyChangeListener,
        Predicate<Nameable> {

    private transient boolean firstTime = true;
    private final transient PlotDisplay display;
    private final transient PropertyChangeListener selectionObserver = new SelectionObserver(
            this, this);

    @Inject
    Print(final PlotDisplay display) {
        super();
        this.display = display;
        putValue(NAME, "Print\u2026");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
                CTRL_MASK));
        final Icon iPrint = loadToolbarIcon("jam/ui/Print.png");
        putValue(Action.SMALL_ICON, iPrint);
        putValue(Action.SHORT_DESCRIPTION, "Print histogram");

    }

    @Override
    protected void execute(final Object[] cmdParams) {
        if (firstTime) {
            LOGGER.warning("On some systems, it will be necessary to first "
                    + "use 'Page Setup\u2026' for your hardcopy to "
                    + "have correct size and margins.");
            firstTime = false;
        }
        final PrinterJob job = PrinterJob.getPrinterJob();
        final ComponentPrintable printable = display.getComponentPrintable();
        job.setPrintable(printable, mPageFormat);
        if (job.printDialog()) {
            final String name = ((AbstractHistogram) SelectionTree
                    .getCurrentHistogram()).getFullName();
            LOGGER.info("Preparing to send histogram '" + name
                    + "' to printer\u2026");
            try {
                display.setRenderForPrinting(true, mPageFormat);
                job.print();
                LOGGER.info("Page sent.");
                display.setRenderForPrinting(false, null);
            } catch (PrinterException e) {
                String mess = getClass()
                        .getName() + ": " + e.getMessage();
                LOGGER.log(Level.SEVERE, mess, e);
            }
        }
    }

    @Override
    protected void executeParse(final String[] cmdTokens)
            throws CommandListenerException {
        execute(null);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // this.selectionObserver.update(observe, obj);
        this.selectionObserver.propertyChange(evt);
    }

    public boolean evaluate(final Nameable selected) {
        return true;
    }
}
