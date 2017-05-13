package jam.plot;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts screen pixel to data value for a mouse Click. You add listeners and
 * remove listeners, classes that implement PlotMouseListener. (method
 * plotMousePressed); The PlotMouse class belongs to a plot class. Only
 * mousePressed event are implemented
 * 
 * @author Ken Swartz.
 */
final class PlotMouse extends MouseAdapter {
	/* list of listeners for PlotMouse */
	private transient final List<PlotMouseListener> listeners = new ArrayList<>();

	/* converts screen pixels to data values */
	private transient final Painter painter;

	/* Called so a change in select plot can be made */
	private transient PlotContainerSelectListener plotSelect;

	/*
	 * non-javadoc: Construction, PlotMouseListener belongs to a Plot. this plot
	 * will now call PlotMouseListener for mouse events.
	 */
	PlotMouse(final Painter plotGraphics) {
		super();
		this.painter = plotGraphics;
	}

	/*
	 * non-javadoc: Add listener for plot select
	 */
	protected void setPlotContainerSelectListener(
			final PlotContainerSelectListener plotSelect) {
		this.plotSelect = plotSelect;
	}

	/*
	 * non-javadoc: Add a class, a listener, that will be called if a PlotMouse
	 * event occurs. Listener must implement PlotMouseListener (have method plot
	 */
	protected void addListener(final PlotMouseListener listener) {
		listeners.add(listener);
	}

	/*
	 * Non-javadoc: Remove a class that was called if a PlotMouse event occured.
	 * returns true if it could remove this listener
	 */
	protected boolean removeListener(final PlotMouseListener listener) {
		return listeners.remove(listener);
	}

	/**
	 * Remove all listeners
	 */
	protected void removeAllListeners() {
		listeners.clear();
	}

	/**
	 * A mousePressed event has occurred, so call all listeners in the listeners
	 * list.
	 * 
	 * @param event
	 *            the mouse-pressed event
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		/* First listeners about selected plot firsts */
		this.plotSelect.plotSelected();
		final CountsContainer countsContainer = (CountsContainer) event
				.getSource();
		/* Only fire event if plot has counts */
		if (countsContainer.getCounts() != null) {
			final Point pin = event.getPoint();
			final Bin pout = painter.toData(pin);
			for (PlotMouseListener listener : listeners) {
				listener.plotMousePressed(pout, pin);
			}
		}
	}
}
