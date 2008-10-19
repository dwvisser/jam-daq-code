package jam.commands;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

/**
 * Superclass for printing-related commands.
 * 
 * @author Ken Swartz
 */
abstract class AbstractPrintingCommand extends AbstractCommand {// NOPMD

	private static final double MARGIN_BOTTOM = 0.5;

	private static final double MARGIN_LEFT = 0.5;

	private static final double MARGIN_RIGHT = 0.5;

	// stuff for printing, margins in inches, font
	private static final double MARGIN_TOP = 0.5;

	/**
	 * Page format object common to all printing commands.
	 */
	protected static PageFormat mPageFormat = PrinterJob.getPrinterJob()
			.defaultPage();

	static {// initial configuration of page format
		final double top = inchesToPica(MARGIN_TOP);
		final double bottom = mPageFormat.getHeight()
				- inchesToPica(MARGIN_BOTTOM);
		final double height = bottom - top;
		final double left = inchesToPica(MARGIN_LEFT);
		final double right = mPageFormat.getWidth()
				- inchesToPica(MARGIN_RIGHT);
		final double width = right - left;
		final Paper paper = mPageFormat.getPaper();
		paper.setImageableArea(top, left, width, height);
		mPageFormat.setPaper(paper);
		mPageFormat.setOrientation(PageFormat.LANDSCAPE);
	}

	private static final double inchesToPica(final double inches) {
		return inches * 72.0;
	}

	AbstractPrintingCommand() {
		super();
	}

	AbstractPrintingCommand(final String name) {
		super(name);
	}
}
