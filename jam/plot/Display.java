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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

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
public final class Display extends JPanel implements Observer {

	private final MessageHandler msgHandler; //output for messages

	private final Action action; //handles display events

	private Histogram currentHist;

	private Histogram[] overlayHist;

	private final Object plotLock = new Object();

	private Plot currentPlot;

	/* plot panels */
	private final JPanel plotswap;

	private final CardLayout plotswapLayout;

	private final Plot1d plot1d;

	private final Plot2d plot2d;

	private final Toolbar toolbar;

	/**
	 * Constructor called by all constructors
	 * 
	 * @param jc
	 *            the class to call to print out messages
	 */
	public Display(JamConsole jc) {
		Bin.Factory.init(this);
		msgHandler = jc; //where to send output messages
		action = new Action(this, jc); // display event handler
		final int size = 420;
		setPreferredSize(new Dimension(size, size));
		final int minsize = 400;
		setMinimumSize(new Dimension(minsize, minsize));
		setLayout(new BorderLayout());
		/*
		 * setup up middle panel containing plots panel to holds 1d and 2d plots
		 * and swaps them
		 */
		plotswap = new JPanel();
		plotswapLayout = new CardLayout();
		plotswap.setLayout(plotswapLayout);
		/* panel 1d plot and its scroll bars */
		plot1d = new Plot1d(action);
		plot1d.addPlotMouseListener(action);
		final Scroller scroller1d = new Scroller(plot1d);
		plotswap.add("OneD", scroller1d);
		/* panel 2d plot and its scroll bars */
		plot2d = new Plot2d(action);
		plot2d.addPlotMouseListener(action);
		final Scroller scroller2d = new Scroller(plot2d);
		plotswap.add("TwoD", scroller2d);
		setPlot(plot1d);
		add(plotswap, BorderLayout.CENTER);
		JamStatus.instance().setDisplay(this);
		Broadcaster.getSingletonInstance().addObserver(this);
		toolbar = new Toolbar(this, action);
	}

	/**
	 * Set the histogram to display
	 */
	public void displayHistogram(Histogram hist) {
		currentHist = hist;
		if (hist != null) {
			final Limits lim = Limits.getLimits(hist);
			if (lim == null) { //create a new Limits object for this histogram
				newHistogram();
			}
			showPlot(currentHist); //changes local currentPlot
			toolbar.setHistogramDimension(currentHist.getDimensionality());
		} else { //we have a null histogram, but display anyway
			showPlot(currentHist);
		}
	}

	private final List overlays = Collections.synchronizedList(new ArrayList());

	public void addToOverlay(Histogram h) {
		if (h.getDimensionality() != 1) {
			throw new IllegalArgumentException(
					"You may only overlay 1D histograms.");
		}
		overlays.add(h);
		doOverlay();
	}

	public void removeOverlays() {
		overlays.clear();
	}

	/**
	 * Overlay a histogram only works for 1 d
	 */
	public void overlayHistograms(List hists) {
		overlays.clear();
		overlays.add(hists);
		doOverlay();
	}

	private void doOverlay() {
		if (!(currentPlot instanceof Plot1d)) {
			throw new UnsupportedOperationException(
					"Overlay attempted for non-1D histogram.");
		}
		overlayHist = new Histogram[overlays.size()];
		int i = 0;
		for (Iterator it = overlays.iterator(); it.hasNext(); i++) {
			final Histogram hist = (Histogram) it.next();
			if (hist != null) {
				if (Limits.getLimits(hist) == null) {
					makeLimits(hist);
				}
				/* test to make sure none of the histograms are 2d */
				if (hist.getDimensionality() == 1) {
					overlayHist[i] = hist;
				} else {
					throw new IllegalStateException(
							"Display attempted overlay with 2d histogram.");
				}
			} else {
				throw new IllegalStateException(
						"Display attempted overlay with null histogram.");
			}
		}
		plot1d.overlayHistograms(overlayHist);
	}

	/**
	 * Get the displayed Histogram.
	 */
	public Histogram getHistogram() {
		return currentHist;
	}

	/**
	 * A new histogram not previously displayed is being displayed, so create
	 * limits for histogram and initialize the limits.
	 */
	private void newHistogram() {
		makeLimits(currentHist);
	}

	private void makeLimits(Histogram h) {
		if (h != null) { //else ignore
			try {
				if (h.getDimensionality() == 1) {
					setPlot(plot1d);
				} else {
					setPlot(plot2d);
				}
				new Limits(h, currentPlot.getIgnoreChZero(), currentPlot
						.getIgnoreChFull());
			} catch (IndexOutOfBoundsException e) {
				msgHandler.errorOutln("Index out of bounds while "
						+ "creating limits for new histogram [plot.Plot] "
						+ currentHist.getName());
			}
		}
	}

	public void setRenderForPrinting(boolean rfp, PageFormat pf) {
		currentPlot.setRenderForPrinting(rfp, pf);
	}

	public ComponentPrintable getComponentPrintable() {
		return currentPlot.getComponentPrintable(RunInfo.runNumber, JamStatus
				.instance().getDate());
	}

	/**
	 * Implementation of Observable interface to receive broadcast events.
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final int command = be.getCommand();
		if (command == BroadcastEvent.REFRESH) {
			displayHistogram(currentHist);
		} else if (command == BroadcastEvent.GATE_SET_ON) {
			currentPlot.displaySetGate(GateSetMode.GATE_NEW, null, null);
			action.setDefiningGate(true);
		} else if (command == BroadcastEvent.GATE_SET_OFF) {
			currentPlot.displaySetGate(GateSetMode.GATE_CANCEL, null, null);
			action.setDefiningGate(false);
			currentPlot.repaint();
		} else if (command == BroadcastEvent.GATE_SET_SAVE) {
			currentPlot.displaySetGate(GateSetMode.GATE_SAVE, null, null);
			action.setDefiningGate(false);
		} else if (command == BroadcastEvent.GATE_SET_ADD) {
			currentPlot.displaySetGate(GateSetMode.GATE_CONTINUE,
					(Bin) be.getContent(), null);
		} else if (command == BroadcastEvent.GATE_SET_REMOVE) {
			currentPlot.displaySetGate(GateSetMode.GATE_REMOVE, null, null);
		} else if (command == BroadcastEvent.GATE_SELECT) {
			Gate gate = (Gate) (be.getContent());
			currentPlot.displayGate(gate);
		}
	}

	public void setPeakFindProperties(double width, double sensitivity,
			boolean cal) {
		plot1d.setWidth(width);
		plot1d.setSensitivity(sensitivity);
		plot1d.setPeakFindDisplayCal(cal);
		displayHistogram(currentHist);
	}

	public void displayFit(double[][] signals, double[] background,
			double[] residuals, int ll) {
		plot1d.displayFit(signals, background, residuals, ll);
	}

	private void setPlot(Plot p) {
		synchronized (plotLock) {
			currentPlot = p;
			action.setPlotChanged();
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
		currentPlot.addPlotMouseListener(listener);
	}

	/**
	 * Removes a plot mouse.
	 * 
	 * @param listener
	 *            the class to notify when the mouse in pressed in the plot
	 * @see #addPlotMouseListener
	 */
	public void removePlotMouseListener(PlotMouseListener listener) {
		currentPlot.removePlotMouseListener(listener);
	}

	/**
	 * Shows (display) a histogram.
	 * 
	 * @param hist
	 *            the histogram to display
	 */
	private void showPlot(Histogram hist) {
		currentHist = hist;
		//cancel all previous stuff
		if (currentPlot != null) {
			currentPlot.displaySetGate(GateSetMode.GATE_CANCEL, null, null);
			action.setDefiningGate(false);
		}
		if (hist != null) {
			if ((currentHist.getType() == Histogram.ONE_DIM_INT)
					|| (currentHist.getType() == Histogram.ONE_DIM_DOUBLE)) {
				//show plot repaint if last plot was also 1d
				plotswapLayout.show(plotswap, "OneD");
				setPlot(plot1d);
			} else if ((currentHist.getType() == Histogram.TWO_DIM_INT)
					|| (currentHist.getType() == Histogram.TWO_DIM_DOUBLE)) {
				//show plot repaint if last plot was also 2d
				plotswapLayout.show(plotswap, "TwoD");
				setPlot(plot2d);
			}
			currentPlot.setSelectingArea(false);
			currentPlot.setMarkArea(false);
			currentPlot.setMarkingChannels(false);
		} else { //null histogram lets be in plot1d
			plotswapLayout.show(plotswap, "OneD");
			setPlot(plot1d);
		}
		currentPlot.displayHistogram(currentHist);
		/* only repaint if we did not do a card swap */
		currentPlot.repaint();
	}

}