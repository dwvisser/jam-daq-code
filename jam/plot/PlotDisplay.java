package jam.plot;

import static jam.plot.PlotPrefs.DISPLAY_LABELS;
import static jam.plot.PlotPrefs.ENABLE_SCROLLING;
import static jam.plot.PlotPrefs.PREFS;
import jam.data.AbstractHist1D;
import jam.data.DataUtility;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandFinder;
import jam.global.JamStatus;
import jam.global.Nameable;
import jam.global.BroadcastEvent.Command;
import jam.plot.PlotContainer.LayoutType;
import jam.ui.Console;
import jam.ui.SelectionTree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;
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
public final class PlotDisplay extends JPanel implements PlotSelectListener,
		PreferenceChangeListener, Observer, CurrentPlotAccessor {

	private transient final Action action; // handles display events

	/** Current plot of plotList */
	private transient PlotContainer currentPlot;

	/** Current view */
	private transient View currentView;

	/** Grid panel that contains plots */
	private transient JPanel gridPanel;

	/** show axis labels */
	private transient boolean isAxisLabels;

	/** Overlay histograms */
	private transient boolean isOverlay;

	/** Is scrolling enabled */
	private transient boolean isScrolling;

	/** Array of all available plots */
	private transient final List<PlotContainer> plotContainers = new ArrayList<PlotContainer>();

	private transient final Object plotLock = new Object();

	private transient final JamStatus status = JamStatus.getSingletonInstance();

	/** Tool bar with plot controls (zoom...) */
	private transient final Toolbar toolbar;

	/**
	 * Constructor called by all constructors
	 * 
	 * @param console
	 *            the class to call to print out messages
	 * @param finder
	 *            finds commands
	 */
	public PlotDisplay(final Console console, final CommandFinder finder) {
		super();
		Broadcaster.getSingletonInstance().addObserver(this);
		/* display event handler */
		action = new Action(this, console, finder);
		PREFS.addPreferenceChangeListener(this);
		createGridPanel();
		toolbar = new Toolbar(this, action);
		initPrefs();
		isOverlay = false;
		/* Initial view only 1 plot */
		setView(View.SINGLE);
	}

	private static final Object DISPLAY_LOCK = new Object();

	private static PlotDisplay display;

	/**
	 * Sets the display.
	 * 
	 * @param plotDisplay
	 *            the display
	 */
	public static void setDisplay(final PlotDisplay plotDisplay) {
		synchronized (DISPLAY_LOCK) {
			display = plotDisplay;
		}
	}

	/**
	 * Gets the display.
	 * 
	 * @return the display
	 */
	public static PlotDisplay getDisplay() {
		synchronized (DISPLAY_LOCK) {
			return display;
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
	public void addPlotMouseListener(final PlotMouseListener listener) {
		getPlotContainer().addPlotMouseListener(listener);
	}

	/**
	 * Constructor helper Create a panel for plots
	 */
	private void createGridPanel() {
		/* Create main panel with tool bar */
		final int size = 200;
		setPreferredSize(new Dimension(size, size));
		final int minsize = 200;
		setMinimumSize(new Dimension(minsize, minsize));
		setLayout(new BorderLayout());
		/* Create imbedded grid panel */
		gridPanel = new JPanel(new GridLayout(1, 1));
		add(gridPanel, BorderLayout.CENTER);
	};

	/**
	 * Create some plots.
	 * 
	 * @param numberPlots
	 *            the number of plots to create
	 */
	private void createPlots(final int numberPlots) {
		for (int i = plotContainers.size(); i < numberPlots; i++) {
			plotContainers.add(PlotContainer.createPlotContainer(this));
		}
	}

	/**
	 * Display a given set of fit curves.
	 * 
	 * @param signals
	 *            individual peak signals
	 * @param background
	 *            background function
	 * @param residuals
	 *            total fit minus actual counts
	 * @param start
	 *            channel where curves start
	 */
	public void displayFit(final double[][] signals, final double[] background,
			final double[] residuals, final int start) {
		currentPlot.displayFit(signals, background, residuals, start);
	}

	/**
	 * Display a histogram.
	 * 
	 * @param hist
	 *            the histogram to display
	 */
	public void displayHistogram(final Histogram hist) {
		if (hist != null) {
			currentPlot.removeAllPlotMouseListeners();
			currentPlot.addPlotMouseListener(action.mouseListener);
			currentPlot.setMarkArea(false);
			currentPlot.setMarkingChannels(false);
			toolbar.setHistogramProperties(hist.getDimensionality(),
					currentPlot.getBinWidth());
		}
		/* Add to view */
		currentPlot.displayHistogram(hist);
		currentPlot.repaint();
		currentView.setHistogram(getPlotContainer().getNumber(), hist);
	}

	/**
	 * @return a printable component
	 */
	public ComponentPrintable getComponentPrintable() {
		return getPlotContainer().getComponentPrintable();
	}

	/**
	 * Get the current select histogram
	 * 
	 * @return histogram
	 */
	public Histogram getHistogram() {
		return getPlotContainer().getHistogram();
	}

	/**
	 * @return the plot currently being displayed
	 */
	public PlotContainer getPlotContainer() {
		synchronized (plotLock) {
			return currentPlot;
		}
	}

	private void initPrefs() {
		PREFS.addPreferenceChangeListener(this);
		isScrolling = PREFS.getBoolean(ENABLE_SCROLLING, false);
		isAxisLabels = PREFS.getBoolean(DISPLAY_LABELS, true);
	}

	/**
	 * Overlay a histogram
	 * 
	 * @param hists
	 *            histogram to overlay
	 */
	private void overlayHistogram(final List<AbstractHist1D> hists) {
		if (hists.isEmpty()) {
			currentPlot.removeOverlays();
		} else {
			currentPlot.overlayHistograms(hists);
		}
	}

	/**
	 * @see PlotSelectListener#plotSelected(Object)
	 */
	public void plotSelected(final Object selectedObject) {
		final PlotContainer selectedPlot = (PlotContainer) selectedObject;
		if (selectedPlot != getPlotContainer()) {
			setPlotContainer(selectedPlot);
			final Histogram hist = selectedPlot.getHistogram();
			/* Tell the framework the current hist */
			SelectionTree.setCurrentHistogram(hist);
			if (hist != null) {
				status.setCurrentGroup(DataUtility.getGroup(hist));
			}
			SelectionTree.setCurrentGate(null);
			status.clearOverlays();
			Broadcaster.getSingletonInstance().broadcast(
					Command.HISTOGRAM_SELECT, hist);
		}
	}

	/**
	 * @see PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();

		if (key.equals(PlotPrefs.ENABLE_SCROLLING)) {
			isScrolling = Boolean.parseBoolean(newValue);
		} else if (key.equals(PlotPrefs.DISPLAY_LABELS)) {
			isAxisLabels = Boolean.parseBoolean(newValue);
		}
		updateLayout();
		update();
	}

	/**
	 * Removes a plot mouse.
	 * 
	 * @param listener
	 *            the class to notify when the mouse in pressed in the plot
	 * @see #addPlotMouseListener
	 */
	public void removePlotMouseListener(final PlotMouseListener listener) {
		getPlotContainer().removePlotMouseListener(listener);
	}

	/**
	 * Set whether there are overlays.
	 * 
	 * @param overlayState
	 *            <code>true</code> if we are overlaying other histograms
	 */
	public void setOverlay(final boolean overlayState) {
		isOverlay = overlayState;
		if (!isOverlay) {
			currentPlot.removeOverlays();
		}
	}

	/**
	 * Set the peak find properties for the plot.
	 * 
	 * @param width
	 *            of peaks to search for
	 * @param sensitivity
	 *            how significant the stats should be
	 * @param cal
	 *            whether to display channel or energy
	 */
	public void setPeakFindProperties(final double width,
			final double sensitivity, final boolean cal) {
		Plot1d.setWidth(width);
		Plot1d.setSensitivity(sensitivity);
		Plot1d.setPeakFindDisplayCal(cal);
		getPlotContainer().repaint();
	}

	/**
	 * Set a plot as the current plot
	 * 
	 * @param container
	 */
	private void setPlotContainer(final PlotContainer container) {
		synchronized (plotLock) {
			/* Only do something if the plot has changed */
			if (!container.equals(currentPlot)) {
				/* Change plot mouse listener source */
				if (currentPlot != null) {
					// / Cancel area setting
					currentPlot.setSelectingArea(false);
					// Cancel gate setting
					currentPlot.displaySetGate(GateSetMode.GATE_CANCEL, null,
							null);
					currentPlot.removeAllPlotMouseListeners();
				}
				if (container.hasHistogram()) {
					container.addPlotMouseListener(action.mouseListener);
				}
				/* Change selected plot */
				for (PlotContainer plotContainer : plotContainers) {
					plotContainer.select(false);
				}
				action.setDefiningGate(false);
				container.select(true);
				/* Cancel all current actions */
				action.plotChanged();
				currentPlot = container;
			}
			toolbar.setHistogramProperties(currentPlot.getDimensionality(),
					currentPlot.getBinWidth());
		}
	}

	/**
	 * Prepare to print to a page.
	 * 
	 * @param rfp
	 *            ??
	 * @param format
	 *            page layout
	 */
	public void setRenderForPrinting(final boolean rfp, final PageFormat format) {
		getPlotContainer().setRenderForPrinting(rfp, format);
	}

	/**
	 * Set the view, tiled layout of plots
	 * 
	 * @param viewIn
	 *            the view to use now
	 */
	public void setView(final View viewIn) {
		currentView = viewIn;
		final int numberPlots = currentView.getNumberHists();
		final GridLayout gridLayout = new GridLayout(currentView.getRows(),
				currentView.getColumns());
		gridPanel.setLayout(gridLayout);
		gridPanel.revalidate();
		createPlots(numberPlots);
		updateLayout();
		/* Set properties for each plot */
		gridPanel.removeAll();
		PlotContainer plotContainer = null;
		/* Set initial states for all plots */
		for (int i = 0; i < numberPlots; i++) {
			plotContainer = plotContainers.get(i);
			plotContainer.removeAllPlotMouseListeners();
			plotContainer.setNumber(i);
			plotContainer.select(false);
			plotContainer.reset();
			gridPanel.add(plotContainer.getComponent());
			final Histogram hist = currentView.getHistogram(i);
			plotContainer.displayHistogram(hist);
		}
		updateLayout();
		// Default set to first plot
		currentPlot = null;// NOPMD
		plotContainer = plotContainers.get(0);
		plotSelected(plotContainer);
	}

	/**
	 * Update all the plots.
	 * 
	 */
	public void update() {
		for (PlotContainer container : plotContainers) {
			container.update();
		}
	}

	/**
	 * Implementation of Observable interface to receive broadcast events.
	 * 
	 * @param observable
	 *            ??
	 * @param object
	 *            the message
	 */
	public void update(final Observable observable, final Object object) {
		final BroadcastEvent event = (BroadcastEvent) object;
		final Command command = event.getCommand();
		if (command == Command.REFRESH) {
			update();
		} else if (command == Command.GATE_SET_ADD) {
			getPlotContainer().displaySetGate(GateSetMode.GATE_CONTINUE,
					(Bin) event.getContent(), null);
		} else if (command == Command.GATE_SELECT) {
			final Gate gate = (Gate) (event.getContent());
			getPlotContainer().displayGate(gate);
		} else {
			processCommand(command);
		}
	}

	private void processCommand(final Command command) {
		if (command == Command.HISTOGRAM_NEW) {
			histogramsCleared();
		} else if (command == Command.HISTOGRAM_SELECT) {
			final Nameable named = SelectionTree.getCurrentHistogram();
			if (named instanceof Histogram) {
				final Histogram hist = (Histogram) named;
				displayHistogram(hist);
				final List<AbstractHist1D> overHists = Histogram
						.getHistogramList(status.getOverlayHistograms(),
								AbstractHist1D.class);
				overlayHistogram(overHists);
			}
		} else if (command == Command.GATE_SET_ON) {
			getPlotContainer().displaySetGate(GateSetMode.GATE_NEW, null, null);
			action.setDefiningGate(true);
		} else if (command == Command.GATE_SET_OFF) {
			getPlotContainer().displaySetGate(GateSetMode.GATE_CANCEL, null,
					null);
			action.setDefiningGate(false);
			getPlotContainer().repaint();
		} else if (command == Command.GATE_SET_SAVE) {
			getPlotContainer()
					.displaySetGate(GateSetMode.GATE_SAVE, null, null);
			action.setDefiningGate(false);
		} else if (command == Command.GATE_SET_REMOVE) {
			getPlotContainer().displaySetGate(GateSetMode.GATE_REMOVE, null,
					null);
		}
	}

	private void histogramsCleared() {
		/* Clear plots select first plot */
		/* Set initial states for all plots */
		for (PlotContainer plots : plotContainers) {
			plots.removeAllPlotMouseListeners();
			plots.select(false);
			plots.reset();
			plots.displayHistogram(null);
		}
		plotSelected(plotContainers.get(0));
	}

	/**
	 * Update the layout, show axis and title or not.
	 */
	private void updateLayout() {
		final int numberPlots = currentView.getNumberHists();
		final LayoutType plotLayout;
		final boolean scrollTemp;
		if (numberPlots == 1) {
			/* Single plot aways has axis showing */
			if (isAxisLabels) {
				plotLayout = PlotContainer.LayoutType.LABELS;
			} else {
				plotLayout = PlotContainer.LayoutType.NO_LABELS;
			}
			scrollTemp = true;
		} else {
			plotLayout = PlotContainer.LayoutType.NO_LABELS_BORDER;
			scrollTemp = isScrolling;
		}
		for (PlotContainer plot : plotContainers) {
			plot.setLayoutType(plotLayout);
			plot.enableScrolling(scrollTemp);
		}
	}

}