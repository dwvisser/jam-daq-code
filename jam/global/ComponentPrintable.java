package jam.global;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import javax.swing.JComponent;

/**
 * Utility wrapper class for taking a Component making it Printable.
 * See p. 287 of "Java 2D Graphics" by Jonathan Knudsen.
 * 
 * @author Jonathan Knudsen
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jan 22, 2004
 */
public class ComponentPrintable implements Printable {
	
	private final Component mComponent;

	/**
	 * Creates a Printable object from an AWT or Swing component.
	 *  
	 * @param c what to print
	 * @see java.awt.print.Printable
	 */
	public ComponentPrintable(Component c){
		mComponent=c;
	}
	
	/** 
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
	throws PrinterException {
		int rval=NO_SUCH_PAGE;
		if (pageIndex <= 0) {
			Graphics2D g2=(Graphics2D)g;
			g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			boolean wasBuffered=disableDoubleBuffering();
			mComponent.paint(g2);
			restoreDoubleBuffering(wasBuffered);
			rval=PAGE_EXISTS;
		}
		return rval;
	}
	
	private boolean disableDoubleBuffering(){
		boolean rval=false;
		if (mComponent instanceof JComponent) {
			JComponent jc=(JComponent)mComponent;
			rval=jc.isDoubleBuffered();
			jc.setDoubleBuffered(false);
		}
		return rval;
	}
	
	private void restoreDoubleBuffering(boolean wasBuffered){
		if (mComponent instanceof JComponent){
			((JComponent)mComponent).setDoubleBuffered(wasBuffered);
		}
	}
}
