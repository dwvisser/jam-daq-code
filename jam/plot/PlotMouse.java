package jam.plot;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

/**
 * Converts screen pixel to data value for a mouse Click.
 * You add listeners and remove listeners, classes that 
 * implement PlotMouseListener. (method plotMousePressed);
 * The PlotMouse class belongs to a plot class.
 * Only mousePressed event are implemented
 *
 * @author Ken Swartz.
 */
public class PlotMouse extends MouseAdapter {
	private java.util.List listenersList; //list of listeners for plotmouse
	private PlotGraphics pg; //converts screen pixels to data values

	/**
	 * Construction, PlotMouseListener belongs to a Plot.
	 * this plot will now call PlotMouseListener for 
	 * mouse events.
	 *
	 */
	public PlotMouse(PlotGraphics plotGraphics) {
		this.pg = plotGraphics;
		listenersList = new Vector(2);
	}
	
	/**
	 * Add a class, a listener, that will be called if a PlotMouse 
	 * event occurs. Listener must implement PlotMouseListener
	 * (have method plot
	 *
	 */
	public void addListener(PlotMouseListener listener) {
		listenersList.add(listener);
	}
	
	/**
	 * Remove a class that was called if a PlotMouse event occured.
	 * returns true if it could remove this listener
	 */
	public boolean removeListener(PlotMouseListener listener) {
		return listenersList.remove(listener);
	}
	
	/**
	 * A mousePressed event has occured call all listeners 
	 * in the listeners list
	 */
	public void mousePressed(MouseEvent e) {
	// Set mousePressed (true) indicates mouse was used 
	    Action.mousePressed = true;
		Point pin = e.getPoint();
		Point pout = pg.toData(pin);
		for (int i = 0; i < listenersList.size(); i++) {
			((PlotMouseListener) listenersList.get(i)).plotMousePressed(
				pout,
				pin);
		}
	}
}
