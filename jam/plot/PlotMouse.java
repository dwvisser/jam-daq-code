package jam.plot;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

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
	//	list of listeners for plotmouse
	private List listenersList;
	//	converts screen pixels to data values
	private PlotGraphics pg; 
	// Called so a change in slelect plot can be made
	private PlotSelectListener plotSelectListener;

	/**
	 * Construction, PlotMouseListener belongs to a Plot.
	 * this plot will now call PlotMouseListener for 
	 * mouse events.
	 *
	 */
	PlotMouse(PlotGraphics plotGraphics) {
		this.pg = plotGraphics;
		
		listenersList = new ArrayList();
	}
	/**
	 * Add listener for plot select  	 
	 */
	void addPlotSelectListener(PlotSelectListener plotSelectListener) {
		this.plotSelectListener=plotSelectListener;
	}
	/**
	 * Add a class, a listener, that will be called if a PlotMouse 
	 * event occurs. Listener must implement PlotMouseListener
	 * (have method plot
	 *
	 */
	void addListener(PlotMouseListener listener) {
		listenersList.add(listener);
	}
	
	/**
	 * Remove a class that was called if a PlotMouse event occured.
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
	 * A mousePressed event has occured, so call all listeners 
	 * in the listeners list.
	 */
	public void mousePressed(MouseEvent e) {

		AbstractPlot selectedPlot=(AbstractPlot)e.getSource();

	    //First listeners about selected plot firsts
		plotSelectListener.plotSelected(selectedPlot);
		
	    //FIXME KBS remove
		//action.setMousePressed(true);		
		//for (int i = 0; i < listenersList.size(); i++) {
			//((PlotMouseListener) listenersList.get(i)).plotMouseSelected(selectedPlot);
		//}	   

		//Only fire event if plot has counts 
		if (selectedPlot.getCounts()!=null) {
			final Point pin = e.getPoint();
			final Bin pout = pg.toData(pin);
			for (int i = 0; i < listenersList.size(); i++) {
				((PlotMouseListener) listenersList.get(i)).plotMousePressed(pout, pin);
			}
		}
	}
}
