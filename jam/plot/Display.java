package jam.plot;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.ComponentPrintable;
import jam.global.JamStatus;
import jam.global.RunInfo;
import jam.ui.Console;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.print.PageFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;
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
	private PlotContainer currentPlot;
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
	
	private final JamStatus status =JamStatus.getSingletonInstance();
	
	/**
	 * Constructor called by all constructors
	 * 
	 * @param jc
	 *            the class to call to print out messages
	 */
	public Display(Console jc) {		
		/* Set gobal status */
		JamStatus.getSingletonInstance().setDisplay(this);
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
		/* Set properties for each plot */
		gridPanel.removeAll();
		PlotContainer plotContainer=null;
		/* Set initial states for all plots */
		for (int i=0;i<numberPlots;i++){
			plotContainer =(PlotContainer)plotList.get(i);
			plotContainer.removeAllPlotMouseListeners();
			plotContainer.setNumber(i);
			plotContainer.select(false);
			plotContainer.reset();			
			gridPanel.add(plotContainer.getComponent());			
			Histogram hist=currentView.getHistogram(i);
			plotContainer.displayHistogram(hist);
		}
		updateLayout();
		//Default set to first plot
		currentPlot=null;
		plotContainer =(PlotContainer)plotList.get(0);
		plotSelected(plotContainer);
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
				plotLayout=PlotContainer.LAYOUT_TYPE_LABELS;
			}else {
				plotLayout=PlotContainer.LAYOUT_TYPE_NO_LABELS;
			}			
			scrollTemp=true;
		} else {
			plotLayout=PlotContainer.LAYOUT_TYPE_NO_LABELS_BORDER;
			scrollTemp=isScrolling;
		}
		for (int i=0;i<numberPlots;i++){
			final PlotContainer plot =(PlotContainer)(plotList.get(i));
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
			final PlotContainer plotTemp= new PlotContainer(this);
			plotList.add(plotTemp);
		}
	}
	
	/**
	 * Display a histogram.
	 * 
	 * @param hist the histogram to display
	 */
	public void displayHistogram(Histogram hist) {
        if (hist != null) {
            currentPlot.removeAllPlotMouseListeners();
            currentPlot.addPlotMouseListener(action);
            currentPlot.setMarkArea(false);
            currentPlot.setMarkingChannels(false);
            
            toolbar.setHistogramProperties(hist.getDimensionality(),
                    currentPlot.getBinWidth());
        }
        /* Add to view */
        currentPlot.displayHistogram(hist);
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
	 * @param hists histogram to overlay
	 */
	private void overlayHistogram(Histogram [] hists){
		if (hists.length>0) {
			currentPlot.overlayHistograms(hists);
		}else{
			removeOverlays();
		}
	}
	
	/**
	 * Overlay a histogram.
	 * 
	 * @param num the number of the hist to overlay
	 */
	public void overlayHistogram(int num) {
		final Histogram hist = Histogram.getHistogram(num);
		/* Check we can overlay. */
		if (!(getPlot().getDimensionality()==1)) {
			throw new UnsupportedOperationException(
					"Overlay attempted for non-1D histogram.");
		}
		if (hist.getDimensionality() != 1) {
			throw new IllegalArgumentException(
					"You may only overlay 1D histograms.");
		}
		Histogram [] hists = new Histogram [1];
		hists[0]=hist;
		overlayHistogram(hists);
		
		//FIXME KBS REMOVE currentPlot.overlayHistograms(num);		
	}
	
	/**
	 * Remove all overlays.
	 */
	public void removeOverlays() {
		currentPlot.removeOverlays();
	}
	
	/**
	 * Set whether there are overlays.
	 * 
	 * @param overlayState <code>true</code> if we are overlaying other histograms
	 */
	public void setOverlay(boolean overlayState){
		isOverlay=overlayState;
		if (isOverlay==false){
			currentPlot.removeOverlays();
		}
	}
	
	/**
	 * @see PlotSelectListener#plotSelected(Object)
	 */
	public void plotSelected(Object selectedObject){
		final PlotContainer selectedPlot =(PlotContainer)selectedObject;
		if (selectedPlot!=getPlot()) {
			setPlot(selectedPlot);
			final Histogram hist =selectedPlot.getHistogram();
			/* Tell the framework the current hist */
			if (hist!=null) {
				status.setCurrentHistogram(hist);				 
			}else{
				status.setCurrentHistogram(null);		
			}
			status.setCurrentGateName(null);
			status.clearOverlays();
			broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);			
		}
	}
	
	/**
	 * Update all the plots
	 *
	 */
	void update(){
		final Iterator iter =plotList.iterator();
		while( iter.hasNext()) {
			PlotContainer p=(PlotContainer)iter.next();
			p.update();
		}	
	}

	/**
	 * Prepare to print to a page.
	 * 
	 * @param rfp ??
	 * @param pf page layout
	 */
	public void setRenderForPrinting(boolean rfp, PageFormat pf) {
		getPlot().setRenderForPrinting(rfp, pf);
	}

	/**
	 * @return a printable component
	 */
	public ComponentPrintable getComponentPrintable() {
		return getPlot().getComponentPrintable(RunInfo.runNumber,
				getDate());
	}

	/* non-javadoc:
	 * Gets the current date and time as a String.
	 */
	private String getDate() {
		final Date date = new Date(); //getDate and time
		final DateFormat datef = DateFormat.getDateTimeInstance(); //default format
		datef.setTimeZone(TimeZone.getDefault()); //set time zone
		return datef.format(date); //format date
	}
	
	/**
	 * Implementation of Observable interface to receive broadcast events.
	 * 
	 * @param observable ??
	 * @param o the message
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final BroadcastEvent.Command command = be.getCommand();
		if (command == BroadcastEvent.Command.REFRESH) {
			update();
		}else if (command==BroadcastEvent.Command.HISTOGRAM_NEW){
			//Clear plots select first plot
			final int numberPlots=currentView.getNumberHists();
			PlotContainer plotContainer=null;
			/* Set initial states for all plots */
			for (int i=0;i<numberPlots;i++){
				plotContainer =(PlotContainer)plotList.get(i);
				plotContainer.removeAllPlotMouseListeners();
				plotContainer.select(false);
				plotContainer.reset();						
				plotContainer.displayHistogram(null);
			}
			plotContainer =(PlotContainer)plotList.get(0);
			plotSelected(plotContainer);						
			
		}else if (command==BroadcastEvent.Command.HISTOGRAM_SELECT){
			final Histogram hist = status.getCurrentHistogram();			
			displayHistogram(hist); 
			final Histogram []overHists=status.getOverlayHistograms();
			overlayHistogram(overHists);
			
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

	/**
	 * Set the peak find properties for the plot.
	 * 
	 * @param width of peaks to search for
	 * @param sensitivity how significant the stats should be
	 * @param cal whether to display channel or energy
	 */
	public void setPeakFindProperties(double width, double sensitivity,
			boolean cal) {
	    Plot1d.setWidth(width);
	    Plot1d.setSensitivity(sensitivity);
	    Plot1d.setPeakFindDisplayCal(cal);
		getPlot().repaint();
	}

	/**
	 * Display a given set of fit curves.
	 * 
	 * @param signals individual peak signals
	 * @param background background function
	 * @param residuals total fit minus actual counts
	 * @param ll ??
	 */
	public void displayFit(double[][] signals, double[] background,
			double[] residuals, int ll) {
		currentPlot.displayFit(signals, background, residuals, ll);
	} 
	
	/**
	 * Set a plot as the current plot
	 * @param p
	 */
	private void setPlot(PlotContainer p) {
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
					((PlotContainer)plotList.get(i)).select(false);
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

	/**
	 * @return the plot currently being displayed
	 */
	public PlotContainer getPlot() {
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
	 * @see PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
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