package jam.commands;

import jam.global.CommandListenerException;

import java.awt.Event;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterJob;

import javax.swing.KeyStroke;

/**
 * Command for Page Setup 
 * @author Ken Swartz
 *
 */
final class PageSetupCmd extends AbstractPrintingCommand {
	
	PageSetupCmd(){
		super();
		putValue(NAME,"Page Setup\u2026");
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(
			KeyEvent.VK_P,
			CTRL_MASK | Event.SHIFT_MASK));
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		final PrinterJob pj=PrinterJob.getPrinterJob();
		mPageFormat = pj.pageDialog(mPageFormat);
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) throws CommandListenerException {
		execute(null);
	}
}
