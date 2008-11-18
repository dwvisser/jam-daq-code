package jam.commands;

import injection.GuiceInjector;
import jam.data.AbstractHistogram;
import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.plot.ComponentPrintable;
import jam.plot.PlotDisplay;
import jam.ui.SelectionTree;

import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * Command for Page Setup
 * 
 * @author Ken Swartz
 * 
 */
final class Print extends AbstractPrintingCommand implements Observer {

	private boolean firstTime = true;// NOPMD

	Print() {
		super();
		putValue(NAME, "Print\u2026");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
				CTRL_MASK));
		final Icon iPrint = loadToolbarIcon("jam/ui/Print.png");
		putValue(Action.SMALL_ICON, iPrint);
		putValue(Action.SHORT_DESCRIPTION, "Print histogram");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		final PlotDisplay display = GuiceInjector.getPlotDisplay();
		if (firstTime) {
			LOGGER
					.warning("On some systems, it will be necessary to first "
							+ "use 'Page Setup\u2026' for your hardcopy to have correct size and margins.");
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
				final StringBuffer mess = new StringBuffer(getClass().getName());
				final String colon = ": ";
				mess.append(colon);
				mess.append(e.getMessage());
				LOGGER.log(Level.SEVERE, mess.toString(), e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		execute(null);
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			setEnabled(true);
		}
	}

}
