package jam.commands;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterJob;

import javax.swing.KeyStroke;

import jam.global.CommandListenerException;

/**
 * Command for Page Setup
 * 
 * @author Ken Swartz
 * 
 */
@SuppressWarnings("serial")
final class PageSetupCmd extends AbstractPrintingCommand {

	PageSetupCmd() {
		super("Page Setup\u2026");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
				CTRL_MASK | InputEvent.SHIFT_DOWN_MASK));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(final Object[] cmdParams) {
		final PrinterJob job = PrinterJob.getPrinterJob();
		mPageFormat = job.pageDialog(mPageFormat);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		execute(null);
	}
}
