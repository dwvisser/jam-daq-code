package jam.commands;

import jam.global.CommandListenerException;

import java.awt.print.PrinterJob;
import java.awt.print.PageFormat;

/**
 * Command for Page Setup 
 * @author Ken Swartz
 *
 */
final class PageSetupCmd extends AbstractCommand {

	
	
	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		PageFormat mPageFormat=PrinterJob.getPrinterJob().defaultPage();		
		PrinterJob pj=PrinterJob.getPrinterJob();
		mPageFormat = pj.pageDialog(mPageFormat);

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) throws CommandListenerException {
		execute(null);

	}

}
