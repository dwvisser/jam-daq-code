package jam.commands;

import jam.global.CommandListenerException;

import java.awt.print.PrinterJob;

/**
 * Command for Page Setup 
 * @author Ken Swartz
 *
 */
final class PageSetupCmd extends AbstractPrintingCommand {
	
	PageSetupCmd(){
		super();
		putValue(NAME,"Page Setup\u2026");
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
