package jam.commands;

import jam.global.CommandListenerException;

import javax.swing.*;
import java.awt.event.*;
import java.awt.print.PrinterJob;

/**
 * Command for Page Setup
 * 
 * @author Ken Swartz
 * 
 */
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
