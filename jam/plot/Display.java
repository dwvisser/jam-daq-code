package jam.plot;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandListener;
import jam.global.CommandListenerException;
import jam.global.ComponentPrintable;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.RunInfo;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * This class is a display routine for plots.
 * It is implemented by <code>Display</code>.
 * <p>
 *
 * @version  0.5 April 98
 * @author   Ken Swartz
 * @see         java.awt.Graphics
 * @since       JDK1.1
 */
public class Display extends JPanel implements CommandListener, Observer {

	/**
	 * Enumeration of the various preference types for displaying
	 * histograms.
	 *
	 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
	 * @see #AUTO_IGNORE_ZERO
	 * @see #AUTO_IGNORE_FULL
	 * @see #WHITE_BACKGROUND
	 * @see #BLACK_BACKGROUND
	 * @see #AUTO_PEAK_FIND
	 * @see #CONTINUOUS_2D_LOG
	 */
	static public class JamPrefs {
		private int type;

		/**
		 * If true, ignore channel 0 when autoscaling the plot.
		 */
		static public JamPrefs AUTO_IGNORE_ZERO = new JamPrefs(0);

		/**
		 * If true, ignore the last channel when autoscaling the plot.
		 */
		static public JamPrefs AUTO_IGNORE_FULL = new JamPrefs(1);

		/**
		 * If true, use black-on-white for displaying the plot.
		 */
		static public JamPrefs WHITE_BACKGROUND = new JamPrefs(2);

		/**
		 * If true, use white-on-black for displaying the plot.
		 */
		static public JamPrefs BLACK_BACKGROUND = new JamPrefs(3);

		/**
		 * If true, automatically find peaks and display their centroids.
		 */
		static public JamPrefs AUTO_PEAK_FIND = new JamPrefs(4);

		/**
		 * If true, show a continuous gradient color scale on 2d plots.
		 */
		static public JamPrefs CONTINUOUS_2D_LOG = new JamPrefs(5);

		private JamPrefs(int type) {
			this.type = type;
		}
	}
	
		

	private final MessageHandler msgHandler; //output for messages
	private final Action action; //handles display events

	private Histogram currentHist;
	private Histogram[] overlayHist;

	private Plot currentPlot;

	/* plot panels */
	private final JPanel plotswap;
	private final CardLayout plotswapLayout;
	private final Plot1d plot1d;
	private final Plot2d plot2d;

	/**
	 * Constructor called by all constructors
	 *
	 * @param   mh  the class to call to print out messages
	 */
	public Display(MessageHandler mh) {
		msgHandler = mh; //where to send output messages
		action = new Action(this, msgHandler); // display event handler
		final int size = 420;
		setPreferredSize(new Dimension(size, size));
		final int minsize = 400;
		setMinimumSize(new Dimension(minsize, minsize));
		setLayout(new BorderLayout());
		/* setup up middle panel containing plots panel to holds 1d and 2d
		 * plots and swaps them */
		plotswap = new JPanel();
		plotswapLayout = new CardLayout();
		plotswap.setLayout(plotswapLayout);
		/* panel 1d plot and its scroll bars */
		plot1d = new Plot1d(action);
		plot1d.addPlotMouseListener(action);
		final Scroller scroller1d = new Scroller(plot1d);
		plotswap.add("OneD", scroller1d);
		/*  panel 2d plot and its scroll bars */
		plot2d = new Plot2d(action);
		plot2d.addPlotMouseListener(action);
		final Scroller scroller2d = new Scroller(plot2d);
		plotswap.add("TwoD", scroller2d);
		setPlot(plot1d);
		add(plotswap, BorderLayout.CENTER);
		JamStatus.instance().setDisplay(this);
		Broadcaster.getSingletonInstance().addObserver(this);
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
			final boolean oneD = currentHist.getDimensionality() == 1;
			bgoto.setEnabled(oneD);
			brebin.setEnabled(oneD);
			bnetarea.setEnabled(oneD);
		} else { //we have a null histogram, but display anyway
			showPlot(currentHist);
		}
	}

	private final List overlays = Collections.synchronizedList(new ArrayList());

	public void addToOverlay(Histogram h) {
		if (h.getDimensionality() != 1){
			throw new IllegalArgumentException("You may only overlay 1D histograms.");
		}
		overlays.add(h);
		doOverlay();
	}
	
	public void removeOverlays(){
		overlays.clear();
	}

	/**
	 * Overlay a histogram
	 * only works for 1 d
	 */
	public void overlayHistograms(List hists) {
		overlays.clear();
		overlays.add(hists);
		doOverlay();
	}

	private void doOverlay() {
		if (!(currentPlot instanceof Plot1d)) {
			throw new UnsupportedOperationException("Overlay attempted for non-1D histogram.");
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
				throw new IllegalStateException("Display attempted overlay with null histogram.");
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
	 * A new histogram not previously displayed is being displayed, so
	 * create limits for histogram and initialize
	 * the limits.
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
				new Limits(
					h,
					currentPlot.getIgnoreChZero(),
					currentPlot.getIgnoreChFull());
			} catch (IndexOutOfBoundsException e) {
				msgHandler.errorOutln(
					"Index out of bounds while "
						+ "creating limits for new histogram [plot.Plot] "
						+ currentHist.getName());
			}
		}
	}

	public void setRenderForPrinting(boolean rfp, PageFormat pf) {
		currentPlot.setRenderForPrinting(rfp, pf);
	}

	public ComponentPrintable getComponentPrintable() {
		return currentPlot.getComponentPrintable(
			RunInfo.runNumber,
			JamStatus.instance().getDate());
	}

	/**
	 * Perform a command
	 * @param commandIn
	 * @param cmdParams
	 */
	public boolean performParseCommand(String commandIn, String[] cmdParams)
		throws CommandListenerException {
		try {
			final boolean accept = action.commandPerform(commandIn, cmdParams);
			return accept;
		} catch (NumberFormatException e) {
			throw new CommandListenerException(e.getMessage());
		}
	}

	/**
	 * Implementation of Observable interface to
	 * receive broadcast events.
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
			currentPlot.displaySetGate(
				GateSetMode.GATE_CONTINUE,
				(Point) be.getContent(),
				null);
		} else if (command == BroadcastEvent.GATE_SET_REMOVE) {
			currentPlot.displaySetGate(GateSetMode.GATE_REMOVE, null, null);
		} else if (command == BroadcastEvent.GATE_SELECT) {
			Gate gate = (Gate) (be.getContent());
			currentPlot.displayGate(gate);
		}
	}

	public void setPeakFindProperties(
		double width,
		double sensitivity,
		boolean cal) {
		plot1d.setWidth(width);
		plot1d.setSensitivity(sensitivity);
		plot1d.setPeakFindDisplayCal(cal);
		displayHistogram(currentHist);
	}

	public void displayFit(
		double[][] signals,
		double[] background,
		double[] residuals,
		int ll) {
		plot1d.displayFit(signals, background, residuals, ll);
	}

	private synchronized final void setPlot(Plot p) {
		currentPlot = p;
		action.setPlot(p);
	}

	/**
	 * Adds a plot mouse listner, plot mouse is a mouse which is
	 * calibrated to the current display.
	 *
	 * @param   listener the class to notify when the mouse in pressed
	 * in the plot
	 * @see #removePlotMouseListener
	 */
	public void addPlotMouseListener(PlotMouseListener listener) {
		currentPlot.addPlotMouseListener(listener);
	}

	/**
	 * Removes a plot mouse.
	 *
	 * @param   listener the class to notify when the mouse in pressed
	 * in the plot
	 * @see #addPlotMouseListener
	 */
	public void removePlotMouseListener(PlotMouseListener listener) {
		currentPlot.removePlotMouseListener(listener);
	}

	/**
	 * Shows (display) a histogram.
	 *
	 * @param   hist  the histogram to display
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
			} else if (
				(currentHist.getType() == Histogram.TWO_DIM_INT)
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

	private JButton bnetarea, brebin, bgoto;

	/**
	 * Adds the tool bar the left hand side of the plot.
	 *
	 * @since Version 0.5
	 */
	public void addToolbarAction() {
		Icon iUpdate = loadToolbarIcon("jam/plot/Update.png");
		Icon iLinLog = loadToolbarIcon("jam/plot/LinLog.png");
		Icon iAutoScale = loadToolbarIcon("jam/plot/AutoScale.png");
		Icon iRange = loadToolbarIcon("jam/plot/Range.png");
		Icon iRebin = loadToolbarIcon("jam/plot/Rebin.png");

		Icon iExpand = loadToolbarIcon("jam/plot/ZoomRegion.png");
		Icon iFullScale = loadToolbarIcon("jam/plot/FullScale.png");
		Icon iZoomIn = loadToolbarIcon("jam/plot/ZoomIn.png");
		Icon iZoomOut = loadToolbarIcon("jam/plot/ZoomOut.png");
		Icon iGoto = loadToolbarIcon("jam/plot/Goto.png");

		Icon iArea = loadToolbarIcon("jam/plot/Area.png");
		Icon iNetArea = loadToolbarIcon("jam/plot/NetArea.png");
		Icon iCancel = loadToolbarIcon("jam/plot/Cancel.png");

		final String defaultVal = BorderLayout.NORTH;
		final String key = "toolbarLocation";
		final java.util.prefs.Preferences helpnode =
			java.util.prefs.Preferences.userNodeForPackage(getClass());
		final String location = helpnode.get(key, defaultVal);
		final int orientation =
			(BorderLayout.NORTH.equals(location)
				|| BorderLayout.SOUTH.equals(location))
				? JToolBar.HORIZONTAL
				: JToolBar.VERTICAL;
		final JToolBar ptoolbar = new JToolBar("Actions", orientation);
		ptoolbar.setToolTipText(
			"Underlined letters are shortcuts for the console.");
		add(ptoolbar, location);

		try {
			ptoolbar.setRollover(true);

			final JButton bupdate = new JButton(iUpdate);
			bupdate.setToolTipText(
				getHTML("<u>U</u>pdate display with most current data."));
			bupdate.setActionCommand(Action.UPDATE);
			bupdate.addActionListener(action);
			ptoolbar.add(bupdate);

			final JButton blinear = new JButton(iLinLog);
			blinear.setToolTipText(
				getHTML("<u>Li</u>near/<u>Lo</u>g scale toggle."));
			blinear.setActionCommand(Action.SCALE);
			blinear.addActionListener(action);
			ptoolbar.add(blinear);

			final JButton bauto = new JButton(iAutoScale);
			bauto.setToolTipText(
				getHTML("<u>A</u>utomatically set the counts scale."));
			bauto.setActionCommand(Action.AUTO);
			bauto.addActionListener(action);
			ptoolbar.add(bauto);

			final JButton brange = new JButton(iRange);
			brange.setToolTipText(getHTML("<u>Ra</u>nge set counts scale."));
			brange.setActionCommand(Action.RANGE);
			brange.addActionListener(action);
			ptoolbar.add(brange);

			brebin = new JButton(iRebin);
			brebin.setToolTipText(
				getHTML("<u>Re</u>bin, enter a bin width in the console."));
			brebin.setActionCommand(Action.REBIN);
			brebin.addActionListener(action);
			ptoolbar.add(brebin);

			ptoolbar.addSeparator();

			final JButton bexpand = new JButton(iExpand);
			bexpand.setToolTipText(getHTML("<u>E</u>xpand plot region."));
			bexpand.setActionCommand(Action.EXPAND);
			bexpand.addActionListener(action);
			ptoolbar.add(bexpand);

			final JButton bfull = new JButton(iFullScale);
			bfull.setActionCommand(Action.FULL);
			bfull.setToolTipText(getHTML("<u>F</u>ull plot view."));
			bfull.addActionListener(action);
			ptoolbar.add(bfull);

			final JButton bzoomin = new JButton(iZoomIn);
			bzoomin.setToolTipText(getHTML("<u>Z</u>oom<u>i</u>n plot."));
			bzoomin.setActionCommand(Action.ZOOMIN);
			bzoomin.addActionListener(action);
			ptoolbar.add(bzoomin);

			final JButton bzoomout = new JButton(iZoomOut);
			bzoomout.setToolTipText(getHTML("<u>Z</u>oom<u>o</u>ut plot."));
			bzoomout.setActionCommand(Action.ZOOMOUT);
			bzoomout.addActionListener(action);
			ptoolbar.add(bzoomout);

			bgoto = new JButton(iGoto);
			bgoto.setActionCommand(Action.GOTO);
			bgoto.setToolTipText(getHTML("<u>G</u>oto selected."));
			bgoto.addActionListener(action);
			ptoolbar.add(bgoto);

			ptoolbar.addSeparator();

			final JButton barea = new JButton(iArea);
			barea.setToolTipText(getHTML("<u>Ar</u>ea display."));
			barea.setActionCommand(Action.AREA);
			barea.addActionListener(action);
			ptoolbar.add(barea);

			bnetarea = new JButton(iNetArea);
			bnetarea.setToolTipText(getHTML("<u>N</u>et Area display."));
			bnetarea.setActionCommand(Action.NETAREA);
			bnetarea.addActionListener(action);
			ptoolbar.add(bnetarea);

			ptoolbar.addSeparator();

			final JButton bcancel = new JButton(iCancel);
			bcancel.setActionCommand(Action.CANCEL);
			bcancel.setToolTipText(getHTML("<u>C</u>ancel plot action."));
			bcancel.addActionListener(action);
			ptoolbar.add(bcancel);

			/* Listen for changes in orientation */
			ptoolbar
				.addPropertyChangeListener(
					"orientation",
					new java.beans.PropertyChangeListener() {
				public void propertyChange(
					java.beans.PropertyChangeEvent evt) {
					/* Get the new orientation */
					Integer newValue = (Integer) evt.getNewValue();
					/* place an appropriate value in the user prefs */
					helpnode.put(
						key,
						(newValue.intValue() == JToolBar.HORIZONTAL)
							? BorderLayout.NORTH
							: BorderLayout.WEST);
					fitToolbar(ptoolbar);
				}
			});
			fitToolbar(ptoolbar);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fitToolbar(JToolBar tb) {
		final boolean vertical = tb.getOrientation() == JToolBar.VERTICAL;
		if (vertical) {
			final int height = tb.getPreferredSize().height;
			final Dimension oldmin = getMinimumSize();
			if (height > oldmin.height) {
				final Dimension newMin = new Dimension(oldmin.width, height);
				setMinimumSize(newMin);
			}
		} else {
			final int width = tb.getPreferredSize().width;
			final Dimension oldmin = getMinimumSize();
			if (width > oldmin.width) {
				final Dimension newMin = new Dimension(width, oldmin.height);
				setMinimumSize(newMin);
			}
		}
	}

	/**
	 * Load icons for tool bar
	 */
	private Icon loadToolbarIcon(String path) {
		final Icon toolbarIcon;
		final ClassLoader cl = this.getClass().getClassLoader();
		final URL urlResource = cl.getResource(path);
		if (!(urlResource == null)) {
			toolbarIcon = new ImageIcon(urlResource);
		} else { //instead use path, ugly but lets us see button
			JOptionPane.showMessageDialog(
				this,
				"Can't load resource: " + path,
				"Missing Icon",
				JOptionPane.ERROR_MESSAGE);
			toolbarIcon = null; //FIXME KBS put in default icon here
		}
		return toolbarIcon;
	}

	private String getHTML(String body) {
		final StringBuffer rval =
			new StringBuffer("<html><body>").append(body).append(
				"</html></body>");
		return rval.toString();
	}

	/**
	 * Set whether the display should auto-scale the counts on a plot when
	 * the viewport changes.
	 *
	 * @param whether true if autoscaling should occur
	 */
	public void setAutoOnExpand(boolean whether) {
		action.setAutoOnExpand(whether);
	}

}
