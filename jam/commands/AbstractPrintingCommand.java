package jam.commands;

import jam.plot.PlotGraphicsLayout;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

/**
 * Superclass for printing-related commands.
 *  
 * @author Ken Swartz
 */
abstract class AbstractPrintingCommand extends AbstractCommand {

	/**
	 * Page format object common to all printing commands.
	 */
	protected static PageFormat mPageFormat = 
	PrinterJob.getPrinterJob().defaultPage();
	
	static {//initial configuration of page format
		final double inchesToPica=72.0;
		final double top=PlotGraphicsLayout.MARGIN_TOP*inchesToPica;
		final double bottom=mPageFormat.getHeight()-
		PlotGraphicsLayout.MARGIN_BOTTOM*inchesToPica;
		final double height=bottom-top;
		final double left=PlotGraphicsLayout.MARGIN_LEFT*inchesToPica;
		final double right=mPageFormat.getWidth()-
		PlotGraphicsLayout.MARGIN_RIGHT*inchesToPica;
		final double width=right-left;
		final Paper paper=mPageFormat.getPaper();
		paper.setImageableArea(top,left,width,height);
		mPageFormat.setPaper(paper);
		mPageFormat.setOrientation(PageFormat.LANDSCAPE);
	}
}
