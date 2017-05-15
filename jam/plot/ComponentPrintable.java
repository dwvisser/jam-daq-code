package jam.plot;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/**
 * Utility wrapper class for taking a Component making it Printable. See p. 287
 * of "Java 2D Graphics" by Jonathan Knudsen.
 * 
 * @author Jonathan Knudsen
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Jan 22, 2004
 */
public class ComponentPrintable implements Printable {

	private transient final Component mComponent;

	/**
	 * Creates a Printable object from an AWT or Swing component.
	 * 
	 * @param component
	 *            what to print
	 * @see java.awt.print.Printable
	 */
	public ComponentPrintable(final Component component) {
		super();
		mComponent = component;
	}

	private boolean disableDoubleBuffering() {
		boolean rval = false;
		if (mComponent instanceof JComponent) {
			final JComponent component = (JComponent) mComponent;
			rval = component.isDoubleBuffered();
			component.setDoubleBuffered(false);
		}
		return rval;
	}

	/**
	 * @see java.awt.print.Printable#print(java.awt.Graphics,
	 *      java.awt.print.PageFormat, int)
	 */
	public int print(final Graphics graphics, final PageFormat pageFormat,
			final int pageIndex) throws PrinterException {
		int rval = NO_SUCH_PAGE;
		if (pageIndex <= 0) {
			final Graphics2D graphics2d = (Graphics2D) graphics;
			graphics2d.translate(pageFormat.getImageableX(), pageFormat
					.getImageableY());
			final boolean wasBuffered = disableDoubleBuffering();
			mComponent.paint(graphics2d);
			restoreDoubleBuffering(wasBuffered);
			rval = PAGE_EXISTS;
		}
		return rval;
	}

	private void restoreDoubleBuffering(final boolean wasBuffered) {
		if (mComponent instanceof JComponent) {
			((JComponent) mComponent).setDoubleBuffered(wasBuffered);
		}
	}
}
