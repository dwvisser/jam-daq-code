package jam.plot;

import jam.JamConsole;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.ComponentPrintable;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.RunInfo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.JPanel;

/**
 * This class is a display routine for plots. It is implemented by
 * <code>Display</code>.
 * <p>
 * 
 * @version 0.5 April 98
 * @author Ken Swartz
 * @see java.awt.Graphics
 * @since JDK1.1
 */
public final class Display extends JPanel implements  PlotSelectListener,
														PreferenceChangeListener,
														PlotPrefs,
														Observer {

	private static final String KEY1 = "1D Plot";

	private static final String KEY2 = "2D Plot";

	private final MessageHandler msgHandler; //output for messages

	private final Action action; //handles display events

	private final Object plotLock = new Object();

	/** Grid panel that contains plots */
	private JPanel plotGridPanel;
	/** Layout of grid with plots*/
	private GridLayout plotGridPanelLayout;
	/** Array of all avaliable plots */
	private ArrayList plotList;
	/** Current plot of plotList */
	private Plot currentPlot;
	/** The layout of the plots */
	private PlotGraphicsLayout graphLayout;
	/** Current  view */
	private View currentView;
	/** Is scrolling enabled */ 
	private boolean isScrolling;
	/** show axis lables */
	private boolean isAxisLabels;
	/** The number of plots */
	private int numberPlots;
	/** Tool bar with plot controls (zoom...) */
	private final Toolbar toolbar;
	
	private final Broadcaster broadcaster = Broadcaster.getSingletonInstance();
	
	private final JamStatus status =JamStatus.instance();
	/**
	 * Constructor called by all constructors
	 * 
	 * @param jc
	 *            the class to call to print out messages
	 */
	public Display(JamConsole jc) {		
		msgHandler = jc; //where to send output messages
		
		//Set gobal status
		JamStatus.instance().setDisplay(this);
		Broadcaster.getSingletonInstance().addObserver(this);
		
		Bin.Factory.init(this);
		//display event handler
		action = new Action(this, jc); 
		prefs.addPreferenceChangeListener(this);
		
		plotList=new ArrayList();		
		
		createGridPanel();
		
		toolbar = new Toolbar(this, action);		

		initPrefs();
		
		//Initial view only 1 plot
		//setView(1,1);
		setView(new View("Single", 1,1));
					
	}
	
	private final void initPrefs() {
		prefs.addPreferenceChangeListener(this);
		isScrolling=prefs.getBoolean(ENABLE_SCROLLING, true);
		isAxisLabels=prefs.getBoolean(DISPLAY_AXIS_LABELS, true);
	}
		
	/**
	 * Constructor helper
	 * Create a panel for plots
	 */
	private void createGridPanel()
	{
		//Create main panel with tool bar 
		final int size = 420;
		setPreferredSize(new Dimension(size, size));
		final int minsize = 400;
		setMinimumSize(new Dimension(minsize, minsize));
		setLayout(new BorderLayout());
		
		//Create imbeded grid panel
		plotGridPanelLayout = new GridLayout(1,1);
		plotGridPanel =  new JPanel();
		plotGridPanel.setLayout(plotGridPanelLayout);
		add(plotGridPanel, BorderLayout.CENTER);		
		
	};
	
	/**
	 * Set the view, tiled layout of plots
	 * 
	 * @param nPlotrows	number of rows
	 * @param nPlotcolumns
	 */
	public void setView(View viewIn){
		currentView=viewIn;
		
		Plot plot=null;
		int plotLayout;
		int i;

		numberPlots=currentView.getNumberHists();
		plotGridPanelLayout.setRows(currentView.getRows());
		plotGridPanelLayout.setColumns(currentView.getColumns());
		plotGridPanel.setLayout(plotGridPanelLayout);		
		plotGridPanel.revalidate(); 
		
		createPlots(numberPlots);

		//Type of layout
		updateLayout();
		
		//Set properties for each plot
		plotGridPanel.removeAll();
		for (i=0;i<numberPlots;i++){
			plot =(Plot)(plotList.get(i));
			plot.removeAllPlotMouseListeners();
			plot.setNumber(i);
			plotGridPanel.add(plot);			
			Histogram hist=currentView.getHistogram(i);
			plot.displayHistogram(hist);
				
		}

		setPlot(plot);			
	}

	/**
	 * Update the layoug, show axis and title
	 * or not 
	 *
	 */
	private void updateLayout(){
		
		Plot plot;
		int plotLayout;
		int i;
		boolean scrollTemp;
		
		//Single plot aways has axis showing
		if (numberPlots==1) {
			if (isAxisLabels) {
				plotLayout=Plot.LAYOUT_TYPE_FULL;
			}else {
				plotLayout=Plot.LAYOUT_TYPE_TILED;
			}			
			//plotLayout=Plot.LAYOUT_TYPE_FULL;
			scrollTemp=true;
		} else {
			//if (isAxisLabels) {
			//	plotLayout=Plot.LAYOUT_TYPE_FULL;
			//}else {
			//	plotLayout=Plot.LAYOUT_TYPE_TILED;
			//}
			plotLayout=Plot.LAYOUT_TYPE_TILED;
			scrollTemp=isScrolling;
		}
		for (i=0;i<numberPlots;i++){
			plot =(Plot)(plotList.get(i));
			plot.setLayoutType(plotLayout);		
			plot.enableScrolling(scrollTemp);
		}
	}
	/**
	 * Set the view, tiled layout of plots
	 * 
	 * @param nPlotrows	number of rows
	 * @param nPlotcolumns
	 */
	public void setView(int nPlotRows, int nPlotColumns){
		
		Plot plot=null;
		int numberPlots;
		int plotLayout;
		int i;

		plotGridPanelLayout.setRows(nPlotRows);
		plotGridPanelLayout.setColumns(nPlotColumns);
		plotGridPanel.setLayout(plotGridPanelLayout);		
		plotGridPanel.revalidate();

		numberPlots=nPlotRows*nPlotColumns;
		createPlots(numberPlots);

		//Type of layout
		if (numberPlots>1)
			plotLayout=Plot.LAYOUT_TYPE_TILED;
		else
			plotLayout=Plot.LAYOUT_TYPE_FULL;

		
		plotGridPanel.removeAll();
		for (i=0;i<numberPlots;i++){
			plot =(Plot)(plotList.get(i));
			plotGridPanel.add(plot);
			
			plot.setLayoutType(plotLayout);
			
		}
		setPlot(plot);			
	}
	
	/**
	 * Create some plots.
	 * 
	 * @param numberPlots the number of plots to create
	 */
	private void createPlots(int numberPlots) {
		for (int i=plotList.size();i<numberPlots;i++) {
			final Plot plotTemp= new Plot(action,  graphLayout, this);
			plotList.add(plotTemp);
		}
	}
	
	/**
	 * Display a histogram.
	 */
	public void displayHistogram() {
		Histogram hist = status.getCurrentHistogram();
		
		if (hist != null) {
			final Limits lim = Limits.getLimits(hist);
			if (lim == null) { //create a new Limits object for this histogram
				makeLimits(hist);
			}
			showPlot(hist); //changes local currentPlot
			toolbar.setHistogramDimension(hist.getDimensionality());
		} else { //we have a null histogram, but display anyway
			showPlot(hist);
		}
		
		//Add to view
		currentView.setHistogram(getPlot().getNumber(), hist);

		
	}
	/**
	 * Overlay a histogram.
	 * 
	 * @param num the number of the hist to overlay
	 */
	public void overlayHistogram(int num) {
		final Histogram h = Histogram.getHistogram(num);
		/* Check we can overlay. */
		if (!(getPlot().getDimensionality()==1)) {
			throw new UnsupportedOperationException(
					"Overlay attempted for non-1D histogram.");
		}
		if (h.getDimensionality() != 1) {
			throw new IllegalArgumentException(
					"You may only overlay 1D histograms.");
		}
		/* Create limits as needed. */
		if (Limits.getLimits(h) == null) {
			makeLimits(h);
		}
		currentPlot.overlayHistograms(num);		
	}
	
	/**
	 * Remove all overlays.
	 */
	public void removeOverlays() {
		currentPlot.removeOverlays();
	}
	
	/**
	 * Handles call back of a plot selected
	 */
	public void plotSelected(Object selectedObject){
		Plot selectedPlot =(Plot)selectedObject;
		setPlot(selectedPlot);
		Histogram hist =selectedPlot.getHistogram();
		
		//Tell the frame work the current histogram ifits different
		if (hist!=status.getCurrentHistogram() &&hist!=null) {
			JamStatus.instance().setCurrentHistogramName(hist.getName());		
			broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
		}
	}
	/**
	 * Update all the plots
	 *
	 */
	void update(){
		Iterator iter =plotList.iterator();
		
		while( iter.hasNext()) {
			Plot p=(Plot)iter.next();
			p.update();
		}
		
	}
	/**
	 * Create the limits for a histogram
	 * @param h
	 */
	private void makeLimits(Histogram h) {
		if (h != null) { //else ignore
			try {
				final Plot plot = getPlot();
				new Limits(h, plot.getIgnoreChZero(), plot.getIgnoreChFull());
			} catch (IndexOutOfBoundsException e) {
				msgHandler.errorOutln("Index out of bounds while "
						+ "creating limits for new histogram [plot.Plot] "
						+ h.getName());
			}
		}
	} 

	public void setRenderForPrinting(boolean rfp, PageFormat pf) {
		getPlot().setRenderForPrinting(rfp, pf);
	}

	public ComponentPrintable getComponentPrintable() {
		return getPlot().getComponentPrintable(RunInfo.runNumber,
				JamStatus.instance().getDate());
	}

	/**
	 * Implementation of Observable interface to receive broadcast events.
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final BroadcastEvent.Command command = be.getCommand();
		if (command == BroadcastEvent.Command.REFRESH) {
			displayHistogram();
		} else if (command == BroadcastEvent.Command.GATE_SET_ON) {
			getPlot().displaySetGate(GateSetMode.GATE_NEW, null, null);
			action.setDefiningGate(true);
		} else if (command == BroadcastEvent.Command.GATE_SET_OFF) {
			getPlot().displaySetGate(GateSetMode.GATE_CANCEL, null, null);
			action.setDefiningGate(false);
			getPlot().repaint();
		} else if (command == BroadcastEvent.Command.GATE_SET_SAVE) {
			getPlot().displaySetGate(GateSetMode.GATE_SAVE, null, null);
			action.setDefiningGate(false);
		} else if (command == BroadcastEvent.Command.GATE_SET_ADD) {
			getPlot().displaySetGate(GateSetMode.GATE_CONTINUE,
					(Bin) be.getContent(), null);
		} else if (command == BroadcastEvent.Command.GATE_SET_REMOVE) {
			getPlot().displaySetGate(GateSetMode.GATE_REMOVE, null, null);
		} else if (command == BroadcastEvent.Command.GATE_SELECT) {
			Gate gate = (Gate) (be.getContent());
			getPlot().displayGate(gate);
		}
	}

	public void setPeakFindProperties(double width, double sensitivity,
			boolean cal) {
		currentPlot.setWidth(width);
		currentPlot.setSensitivity(sensitivity);
		currentPlot.setPeakFindDisplayCal(cal);
		displayHistogram();
	}

	public void displayFit(double[][] signals, double[] background,
			double[] residuals, int ll) {
		currentPlot.displayFit(signals, background, residuals, ll);
	}
	/**
	 * Set a plot as the current plot
	 * @param p
	 */
	private void setPlot(Plot p) {
		
		int i;
		synchronized (plotLock) {		

			//Only do something if the plot has changed 
			if (p!=currentPlot) {
				//Change plot mouse listener source
				if (currentPlot!=null ) {
					currentPlot.reset();
					currentPlot.removeAllPlotMouseListeners();
				}
				
				if (p.getHistogram()!=null)
					p.addPlotMouseListener(action);
				
				//Change selected plot
				for (i=0;i<plotList.size();i++) {
					((Plot)plotList.get(i)).select(false);
				}
				p.select(true);
				
				//Cancel all current actions
				action.plotChanged();
				
				currentPlot = p;				
			}			
		}
	} 

	public Plot getPlot() {
		synchronized (plotLock) {
			return currentPlot;
		}
	}

	/**
	 * Adds a plot mouse listner, plot mouse is a mouse which is calibrated to
	 * the current display.
	 * 
	 * @param listener
	 *            the class to notify when the mouse in pressed in the plot
	 * @see #removePlotMouseListener
	 */
	public void addPlotMouseListener(PlotMouseListener listener) {
		getPlot().addPlotMouseListener(listener);
	}

	/**
	 * Removes a plot mouse.
	 * 
	 * @param listener
	 *            the class to notify when the mouse in pressed in the plot
	 * @see #addPlotMouseListener
	 */
	public void removePlotMouseListener(PlotMouseListener listener) {
		getPlot().removePlotMouseListener(listener);
	}

	/**
	 * Shows (display) a histogram.
	 * 
	 * @param hist
	 *            the histogram to display
	 */
	private void showPlot(Histogram hist) {
				
		Plot plot = getPlot();
			
		/// Cancel all previous stuff.
		plot.setSelectingArea(false);
		plot.setMarkArea(false);
		plot.setMarkingChannels(false);
		
		plot.displaySetGate(GateSetMode.GATE_CANCEL, null, null);
		action.setDefiningGate(false);
	
		if (hist != null) {
			plot.displayHistogram(hist);			
		} 
		
		plot.repaint();	
	}
	/**
	 * Preferences changed
	 */
	public void preferenceChange(PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();

		if (key.equals(PlotPrefs.ENABLE_SCROLLING)){
			isScrolling=Boolean.valueOf(newValue).booleanValue();
		} else if (key.equals(PlotPrefs.DISPLAY_AXIS_LABELS)){		
			isAxisLabels=Boolean.valueOf(newValue).booleanValue();
		}
		updateLayout();
		update();
	}
	
}