package jam.commands;

import jam.global.CommandListenerException;

import java.awt.print.PrinterJob;
import java.awt.print.PageFormat;

/**
 * Command for Page Setup 
 * @author Ken Swartz
 *
 */
public class PageSetupCmd extends AbstractCommand implements Commandable {

	
	
	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	public void execute(Object[] cmdParams) {
		PageFormat mPageFormat=PrinterJob.getPrinterJob().defaultPage();		
		PrinterJob pj=PrinterJob.getPrinterJob();
		mPageFormat = pj.pageDialog(mPageFormat);

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	public void executeParse(String[] cmdTokens) throws CommandListenerException {
		execute(null);

	}

}
