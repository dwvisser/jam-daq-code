package jam.commands;

import java.awt.print.PrinterJob;
import java.awt.print.PageFormat;

/**
 * Command for Page Setup 
 * @author Ken Swartz
 *
 */
abstract class AbstractPrintingCommand extends AbstractCommand {

	protected static PageFormat mPageFormat = PrinterJob.getPrinterJob().defaultPage();
	
}
