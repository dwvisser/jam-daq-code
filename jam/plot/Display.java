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

	private static final JamStatus status = JamStatus.instance();

	private static final String KEY1 = "1D Plot";

	private static final String KEY2 = "2D Plot";

	private final MessageHandler msgHandler; //output for messages

	private final Action action; //handles display events

	private final Object plotLock = new Object();

	private final JPanel plotswap;

	private final CardLayout plotswapLayout;

	private final Plot1d plot1d;

	private final Plot2d plot2d;

	private Plot currentPlot;

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
		plotswap.add(KEY1, scroller1d);
		/* panel 2d plot and its scroll bars */
		plot2d = new Plot2d(action);
		plot2d.addPlotMouseListener(action);
		final Scroller scroller2d = new Scroller(plot2d);
		plotswap.add(KEY2, scroller2d);
		setPlot(plot1d);
		add(plotswap, BorderLayout.CENTER);
		JamStatus.instance().setDisplay(this);
		Broadcaster.getSingletonInstance().addObserver(this);
		toolbar = new Toolbar(this, action);
	}

	/**
	 * Set the histogram to display
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
	}

	private final List overlays = Collections.synchronizedList(new ArrayList());

	public void addToOverlay(int num) {
		final Histogram h=Histogram.getHistogram(num);
		if (h.getDimensionality() != 1) {
			throw new IllegalArgumentException(
					"You may only overlay 1D histograms.");
		}
		if (Limits.getLimits(h) == null) {
			makeLimits(h);
		}
		overlays.add(new Integer(num));
		doOverlay();
	}

	public void removeOverlays() {
		overlays.clear();
	}

	private void doOverlay() {
		if (!(getPlot() instanceof Plot1d)) {
			throw new UnsupportedOperationException(
					"Overlay attempted for non-1D histogram.");
		}
		plot1d.overlayHistograms(Collections.unmodifiableList(overlays));
	}

	private void makeLimits(Histogram h) {
		if (h != null) { //else ignore
			try {
				if (h.getDimensionality() == 1) {
					setPlot(plot1d);
				} else {
					setPlot(plot2d);
				}
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
		plot1d.setWidth(width);
		plot1d.setSensitivity(sensitivity);
		plot1d.setPeakFindDisplayCal(cal);
		displayHistogram();
	}

	public void displayFit(double[][] signals, double[] background,
			double[] residuals, int ll) {
		plot1d.displayFit(signals, background, residuals, ll);
	}

	private void setPlot(Plot p) {
		synchronized (plotLock) {
			final String key = p instanceof Plot1d ? KEY1 : KEY2;
			plotswapLayout.show(plotswap, key);
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
		/* Cancel all previous stuff. */
		Plot plot = getPlot();
		if (plot != null) {
			plot.displaySetGate(GateSetMode.GATE_CANCEL, null, null);
			action.setDefiningGate(false);
		}
		if (hist != null) {
			final int dim=hist.getDimensionality();
			if (dim==1) {
				/* Show plot repaint if last plot was also 1d. */
				setPlot(plot1d);
			} else if (dim==2) {
				/* Show plot repaint if last plot was also 2d. */
				setPlot(plot2d);
			}
			plot.setSelectingArea(false);
			plot.setMarkArea(false);
			plot.setMarkingChannels(false);
		} else { //null histogram lets be in plot1d
			setPlot(plot1d);
		}
		plot = getPlot();
		plot.displayHistogram(hist);
		plot.repaint();
	}
}