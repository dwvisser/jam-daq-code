package jam.plot;

import jam.JamConsole;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.ComponentPrintable;
import jam.global.JamStatus;
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

	private final Action action; //handles display events

	private final Object plotLock = new Object();

	/** Grid panel that contains plots */
	private JPanel gridPanel;
	/** Layout of grid with plots*/
	//private GridLayout gridLayout;
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
	/** Overlay histograms */
	private boolean isOverlay;

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
		/* Set gobal status */
		JamStatus.instance().setDisplay(this);
		Broadcaster.getSingletonInstance().addObserver(this);
		Bin.Factory.init(this);
		/* display event handler */
		action = new Action(this, jc); 
		PREFS.addPreferenceChangeListener(this);
		plotList=new ArrayList();		
		createGridPanel();
		toolbar = new Toolbar(this, action);		
		initPrefs();
		isOverlay=false;
		/* Initial view only 1 plot */
		setView(View.SINGLE);
	}
	
	private final void initPrefs() {
		PREFS.addPreferenceChangeListener(this);
		isScrolling=PREFS.getBoolean(ENABLE_SCROLLING, true);
		isAxisLabels=PREFS.getBoolean(DISPLAY_AXIS_LABELS, true);
	}
		
	/**
	 * Constructor helper
	 * Create a panel for plots
	 */
	private void createGridPanel() {
		/* Create main panel with tool bar */ 
		final int size = 400;
		setPreferredSize(new Dimension(size, size));
		final int minsize = 400;
		setMinimumSize(new Dimension(minsize, minsize));
		setLayout(new BorderLayout());
		/* Create imbedded grid panel */
		gridPanel =  new JPanel(new GridLayout(1,1));
		add(gridPanel, BorderLayout.CENTER);		
	};
	
	/**
	 * Set the view, tiled layout of plots
	 * 
	 * @param viewIn the view to use now
	 */
	public void setView(View viewIn){
		currentView=viewIn;
		final int numberPlots=currentView.getNumberHists();
		final GridLayout gridLayout=new GridLayout(currentView.getRows(),
				currentView.getColumns());
		gridPanel.setLayout(gridLayout);		
		gridPanel.revalidate(); 
		createPlots(numberPlots);
		updateLayout();		
		//Set properties for each plot
		gridPanel.removeAll();
		Plot plot=null;
		//Set initial states for all plots
		for (int i=0;i<numberPlots;i++){
			plot =(Plot)plotList.get(i);
			plot.removeAllPlotMouseListeners();
			plot.setNumber(i);
			plot.select(false);
			plot.reset();
			plot.removeAllPlotMouseListeners();			
			gridPanel.add(plot);			
			Histogram hist=currentView.getHistogram(i);
			plot.displayHistogram(hist);
		}
		updateLayout();
		//Default set to first plot
		currentPlot=null;
		plot =(Plot)plotList.get(0);
		plotSelected(plot);
		
	}

	/**
	 * Update the layout, show axis and title
	 * or not.
	 */
	private void updateLayout(){
		final int numberPlots=currentView.getNumberHists();
		final int plotLayout;
		final boolean scrollTemp;
		if (numberPlots==1) {
			/* Single plot aways has axis showing */
			if (isAxisLabels) {
				plotLayout=Plot.LAYOUT_TYPE_LABELS;
			}else {
				plotLayout=Plot.LAYOUT_TYPE_NO_LABELS;
			}			
			scrollTemp=true;
		} else {
			plotLayout=Plot.LAYOUT_TYPE_NO_LABELS_BORDER;
			scrollTemp=isScrolling;
		}
		for (int i=0;i<numberPlots;i++){
			final Plot plot =(Plot)(plotList.get(i));
			plot.setLayoutType(plotLayout);		
			plot.enableScrolling(scrollTemp);
		}
	}
	
	/**
	 * Create some plots.
	 * 
	 * @param numberPlots the number of plots to create
	 */
	private void createPlots(int numberPlots) {
		for (int i=plotList.size();i<numberPlots;i++) {
			final Plot plotTemp= new Plot(graphLayout, this);
			plotList.add(plotTemp);
		}
	}
	
	/**
	 * Display a histogram.
	 */
	public void displayHistogram(Histogram hist) {
		if (hist != null) {
			if (!isOverlay){
				final Limits lim = Limits.getLimits(hist);
				currentPlot.removeAllPlotMouseListeners();
				currentPlot.addPlotMouseListener(action);
				currentPlot.setMarkArea(false);
				currentPlot.setMarkingChannels(false);
				currentPlot.displayHistogram(hist);			 
				toolbar.setHistogramProperties(hist.getDimensionality(), currentPlot.getBinWidth());
			}else{
				overlayHistogram(hist);
			}
				
		}
		/* Add to view */
		currentPlot.repaint();
		currentView.setHistogram(getPlot().getNumber(), hist);

	}
	/**
	 * Get the current select histogram
	 * @return histogram
	 */
	public Histogram getHistogram(){
		return getPlot().getHistogram();
	}
	/**
	 * Overlay a histogram
	 * @param hist histogram to overlay
	 */
	public void overlayHistogram(Histogram hist){
		int num=hist.getNumber();
		overlayHistogram(num);
		
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
		currentPlot.overlayHistograms(num);		
	}
	/**
	 * Remove all overlays.
	 */
	public void removeOverlays() {
		currentPlot.removeOverlays();
	}
	public void setOverlay(boolean overlayState){
		isOverlay=overlayState;
		if (isOverlay==false)
			currentPlot.removeOverlays();
	}
	
	/**
	 * Handles call back of a plot selected
	 */
	public void plotSelected(Object selectedObject){
		Plot selectedPlot =(Plot)selectedObject;
		if (selectedPlot!=getPlot()) {
			setPlot(selectedPlot);
			Histogram hist =selectedPlot.getHistogram();
		
			//Tell the framework the current hist
			if (hist!=null) {
				status.setHistName(hist.getName());				 
			}else{
				status.setHistName(null);		
			}
			status.setCurrentGateName(null);
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
			final Histogram hist = status.getCurrentHistogram();
			displayHistogram(hist);
		}else if (command==BroadcastEvent.Command.HISTOGRAM_SELECT){
			final Histogram hist = status.getCurrentHistogram();			
			displayHistogram(hist); 
			removeOverlays();
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
		synchronized (plotLock) {
			currentPlot.setWidth(width);
			currentPlot.setSensitivity(sensitivity);
			currentPlot.setPeakFindDisplayCal(cal);
		}
		//displayHistogram();
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
			/* Only do something if the plot has changed */ 
			if (p!=currentPlot) {
				/* Change plot mouse listener source */
				if (currentPlot!=null ) {
					/// Cancel area setting 
					currentPlot.setSelectingArea(false);
					//Cancel gate setting
					currentPlot.displaySetGate(GateSetMode.GATE_CANCEL, null, null);
					currentPlot.removeAllPlotMouseListeners();
				}
				if (p.hasHistogram()){
					p.addPlotMouseListener(action);
				}
				/* Change selected plot */
				for (i=0;i<plotList.size();i++) {
					((Plot)plotList.get(i)).select(false);
				}
				action.setDefiningGate(false);
				p.select(true);
				/* Cancel all current actions */
				action.plotChanged();
				currentPlot = p;				
			}			
			toolbar.setHistogramProperties(currentPlot.getDimensionality(), currentPlot.getBinWidth());
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