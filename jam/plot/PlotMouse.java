package jam.plot;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Converts screen pixel to data value for a mouse Click. You add listeners and
 * remove listeners, classes that implement PlotMouseListener. (method
 * plotMousePressed); The PlotMouse class belongs to a plot class. Only
 * mousePressed event are implemented
 * 
 * @author Ken Swartz.
 */
class PlotMouse extends MouseAdapter {
	/* list of listeners for plotmouse */
	private final List<PlotMouseListener> listenersList = new ArrayList<PlotMouseListener>();

	/* converts screen pixels to data values */
	private final PlotGraphics pg;

	/* Called so a change in select plot can be made */
	private PlotSelectListener plotSelectListener;

	/*
	 * non-javadoc: Construction, PlotMouseListener belongs to a Plot. this plot
	 * will now call PlotMouseListener for mouse events.
	 */
	PlotMouse(PlotGraphics plotGraphics) {
		pg = plotGraphics;
	}

	/*
	 * non-javadoc: Add listener for plot select
	 */
	void setPlotSelectListener(PlotSelectListener plotSelectListener) {
		this.plotSelectListener = plotSelectListener;
	}

	/*
	 * non-javadoc: Add a class, a listener, that will be called if a PlotMouse
	 * event occurs. Listener must implement PlotMouseListener (have method plot
	 */
	void addListener(PlotMouseListener listener) {
		listenersList.add(listener);
	}

	/*
	 * Non-javadoc: Remove a class that was called if a PlotMouse event occured.
	 * returns true if it could remove this listener
	 */
	boolean removeListener(PlotMouseListener listener) {
		return listenersList.remove(listener);
	}

	/**
	 * Remove all listeners
	 */
	void removeAllListeners() {
		listenersList.clear();
	}

	/**
	 * A mousePressed event has occured, so call all listeners in the listeners
	 * list.
	 * 
	 * @param e
	 *            the mouse-pressed event
	 */
	public void mousePressed(MouseEvent e) {
		final AbstractPlot selectedPlot = ((PlotPanel) e
				.getSource()).getPlot();
		/* First listeners about selected plot firsts */
		plotSelectListener.plotSelected(selectedPlot);
		/* Only fire event if plot has counts */
		if (selectedPlot.getCounts() != null) {
			final Point pin = e.getPoint();
			final Bin pout = pg.toData(pin);
			final Iterator iter = listenersList.iterator();
			while (iter.hasNext()) {
				((PlotMouseListener) iter.next()).plotMousePressed(pout, pin);
			}
		}
	}
}
