package jam.plot;
import jam.data.DataException;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandListener;
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
import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JOptionPane;

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
public class Display
	extends JPanel
	implements Displayer, CommandListener, Observer {

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
	static public class Preferences {
		private int type;

		/**
		 * If true, ignore channel 0 when autoscaling the plot.
		 */
		static public Preferences AUTO_IGNORE_ZERO = new Preferences(0);

		/**
		 * If true, ignore the last channel when autoscaling the plot.
		 */
		static public Preferences AUTO_IGNORE_FULL = new Preferences(1);

		/**
		 * If true, use black-on-white for displaying the plot.
		 */
		static public Preferences WHITE_BACKGROUND = new Preferences(2);

		/**
		 * If true, use white-on-black for displaying the plot.
		 */
		static public Preferences BLACK_BACKGROUND = new Preferences(3);

		/**
		 * If true, automatically find peaks and display their centroids.
		 */
		static public Preferences AUTO_PEAK_FIND = new Preferences(4);

		/**
		 * If true, show a continuous gradient color scale on 2d plots.
		 */
		static public Preferences CONTINUOUS_2D_LOG = new Preferences(5);

		private Preferences(int type) {
			this.type = type;
		}
	}

	private final MessageHandler msgHandler; //output for messages
	private Broadcaster broadcaster; //broadcaster if needed
	private final Action action; //handles display events

	private Displayable currentData;
	private Displayable overlayData;
	private Histogram currentHist;
	private Histogram overlayHist;

	private Plot currentPlot;
	private boolean overlayState = false;

	/* plot panels */
	private final JPanel plotswap;
	private final CardLayout plotswapLayout;
	private final Plot1d plot1d;
	private final Plot2d plot2d;

	/**
	 * Constructor for Applet, no boadcaster
	 *
	 * @param mh the class to call if the plot button is pushed.
	 */
	public Display(Broadcaster b, MessageHandler mh) {
		this(mh);
		this.broadcaster = b;
		action.setBroadcaster(broadcaster);
	}

	/**
	 * Constructor called by all constructors
	 *
	 * @param   mh  the class to call to print out messages
	 */
	public Display(MessageHandler mh) {
		msgHandler = mh; //where to send output messages
		action = new Action(this, msgHandler);// display event handler
		final int size=400;
		setPreferredSize(new Dimension(size,size));
		final int minsize=400;
		setMinimumSize(new Dimension(minsize,minsize));
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
		/* default preferences */
		plot1d.setIgnoreChZero(true);
		plot1d.setIgnoreChFull(true);
		plot2d.setIgnoreChZero(true);
		plot2d.setIgnoreChFull(true);
		this.add(plotswap, BorderLayout.CENTER);
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
			overlayState = false;
			showPlot(currentHist); //changes local currentPlot
			final boolean oneD=currentHist.getDimensionality() == 1;
			bgoto.setEnabled(oneD);
			brebin.setEnabled(oneD);
			bnetarea.setEnabled(oneD);
		} else { //we have a null histogram, but display anyway
			showPlot(currentHist);
		}
	}

	/**
	 * Overlay a histogram
	 * only works for 1 d
	 */
	public void overlayHistogram(Histogram hist) {
		if (hist != null) {
			if (Limits.getLimits(hist) == null) {
				makeLimits(hist);
			}
			/* test to make sure none of the histograms are 2d */
			if (currentPlot instanceof Plot1d) {
				if (hist.getDimensionality() == 1) {
					overlayHist = hist;
					overlayState = true;
					plot1d.overlayHistogram(overlayHist);
				} else {
					msgHandler.errorOutln("Cannot overlay a 2D histogram.");
				}
			} else {
				msgHandler.errorOutln(
					"Cannot overlay on top of a 2D histogram.");
			}
		} else {
			msgHandler.errorOutln("Cannot overlay null histogram");
		}
	}

	/**
	 * Display a <code>Displayable</code> object.
	 */
	public void displayData(Displayable data) {
		currentData = data;
		if (data != null) {
			if (Limits.getLimits(data) == null) {
				newHistogram();
			}
			overlayState = false;
		}
	}

	/**
	 * Overlay a displayble data
	 * only works for 1 d
	 */
	public void overlayData(Displayable data) {
		if (data != null) {
			if (Limits.getLimits(data) == null) {
				newHistogram();
			}
			// test to make sure none of the histograms are 2d
			if (currentPlot instanceof Plot1d) {
				if (data.getType() == Displayable.ONE_DIMENSION) {
					overlayData = data;
					overlayState = true;
				} else {
					msgHandler.errorOutln("Cannot overlay a 2D histogram.");
				}
			} else {
				msgHandler.errorOutln(
					"Cannot overlay on top of a 2D histogram.");
			}
		} else {
			msgHandler.errorOutln("Error tried to overlay null.");
		}
	}

	/**
	 * Get the displayed Histogram.
	 */
	public Histogram getHistogram() {
		return currentHist;
	}

	/**
	 * Get the displayed data.
	 */
	public Displayable getData() {
		return currentData;
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
		if (h != null) {//else ignore
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

	/**
	 * Display a gate.
	 *
	 * @param gate The gate to display.
	 */
	public void displayGate(Gate gate) {
		try {
			currentPlot.displayGate(gate);
		} catch (DataException de) {
			msgHandler.errorOutln(
				"Display.displayGate() DataException: " + de.getMessage());
		}
	}

	public void setRenderForPrinting(boolean rfp, PageFormat pf){
		currentPlot.setRenderForPrinting(rfp,pf);
	}

	public ComponentPrintable getComponentPrintable(){
		return currentPlot.getComponentPrintable(RunInfo.runNumber,
		JamStatus.instance().getDate());
	}

	/**
	 * Do a command sent in as a message.
	 */
	public void commandPerform(String commandIn, double [] parameters) {
		action.commandPerform(commandIn, parameters);
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
			currentPlot.displaySetGate(GateSetMode.GATE_CONTINUE,
			(Point) be.getContent(),null);
		} else if (command == BroadcastEvent.GATE_SET_REMOVE) {
			currentPlot.displaySetGate(GateSetMode.GATE_REMOVE, null, null);
		}
	}

	/**
	 * Set a display preference.
	 *
	 * @param preference the preference to set
	 * @param state the state of the preference, if applicable
	 */
	public void setPreference(Preferences preference, boolean state) {
		if (preference == Preferences.AUTO_IGNORE_ZERO) {
			plot1d.setIgnoreChZero(state);
			plot2d.setIgnoreChZero(state);
		} else if (preference == Preferences.AUTO_IGNORE_FULL) {
			plot1d.setIgnoreChFull(state);
			plot2d.setIgnoreChFull(state);
		} else if (preference == Preferences.BLACK_BACKGROUND) {
			plot1d.setColorMode(PlotColorMap.WHITE_ON_BLACK);
			plot2d.setColorMode(PlotColorMap.WHITE_ON_BLACK);
			displayHistogram(currentHist);
		} else if (preference == Preferences.WHITE_BACKGROUND) {
			plot1d.setColorMode(PlotColorMap.BLACK_ON_WHITE);
			plot2d.setColorMode(PlotColorMap.BLACK_ON_WHITE);
			displayHistogram(currentHist);
		} else if (preference == Preferences.AUTO_PEAK_FIND) {
			plot1d.setPeakFind(state);
			displayHistogram(currentHist);
		} else if (preference == Preferences.CONTINUOUS_2D_LOG) {
			displayHistogram(currentHist);
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

	/**
	 * The current Plot either Plot1d or Plot2d
	 *
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public Plot getPlot() {
		return currentPlot;
	}

	synchronized final void setPlot(Plot p) {
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
		boolean doRepaint = false;
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
				if (currentPlot == plot1d) {
					doRepaint = true;
				}
				setPlot(plot1d);
			} else if (
				(currentHist.getType() == Histogram.TWO_DIM_INT)
					|| (currentHist.getType() == Histogram.TWO_DIM_DOUBLE)) {
				//show plot repaint if last plot was also 2d
				plotswapLayout.show(plotswap, "TwoD");
				if (currentPlot == plot2d) {
					doRepaint = true;
				}
				setPlot(plot2d);
			}
			currentPlot.setMarkingArea(false);
			currentPlot.setMarkingChannels(false);
		} else { //null histogram lets be in plot1d
			plotswapLayout.show(plotswap, "OneD");
			setPlot(plot1d);
			doRepaint = true;
		}
		//plot current histogram
		currentPlot.displayHistogram(currentHist);
		//        action.setPlot(currentPlot);
		//only repaint if we did not do a card swap
		if (doRepaint) {
			currentPlot.repaint();
		}
	}

	private  JButton bnetarea;		//FIXME clean up KBS
	private  JButton brebin; 		// =new JButton(getHTML("<u>Re</u>bin"));
	private  JButton bgoto; 		//= new JButton(getHTML("<u>G</u>oto"));

	/**
	 * Adds the tool bar the left hand side of the plot.
	 *
	 * @since Version 0.5
	 */
	public void addToolbarAction() {
		//final ClassLoader cl = this.getClass().getClassLoader();

		Icon iUpdate =loadToolbarIcon("jam/plot/Update.png");
		Icon iLinLog = loadToolbarIcon("jam/plot/LinLog.png");
		Icon iAutoScale = loadToolbarIcon("jam/plot/AutoScale.png");
		Icon iRange = loadToolbarIcon("jam/plot/Range.png");
		Icon iRebin = loadToolbarIcon("jam/plot/Rebin.png");

		Icon iExpand = loadToolbarIcon("toolbarButtonGraphics/general/Zoom24.gif");
		Icon iFullScale = loadToolbarIcon("jam/plot/FullScale.png");
		Icon iZoomIn =loadToolbarIcon("toolbarButtonGraphics/general/ZoomIn24.gif");
		Icon iZoomOut = loadToolbarIcon("toolbarButtonGraphics/general/ZoomOut24.gif");
		Icon iGoto = loadToolbarIcon("jam/plot/Goto.png");

		Icon iArea = loadToolbarIcon("jam/plot/Area.png");
		Icon iNetArea = loadToolbarIcon("jam/plot/NetArea.png");
		Icon iCancel = loadToolbarIcon("jam/plot/Cancel.png");

		final JToolBar ptoolbar = new JToolBar("Actions", JToolBar.VERTICAL);
		ptoolbar.setToolTipText("Underlined letters are shortcuts for the console.");
		add(ptoolbar, BorderLayout.WEST);
		/* tbcontents may seem unnecessary, but it is needed
		 * for Windows XP systems using the Aqua theme.
		 * Without an additional JPanel container between the
		 * buttons and the JToolbar, Windows XP displays the
		 * buttons without borders, and doesn't enable/disable
		 * them as expected
		 */
		//final JPanel tbcontents=new JPanel();
		// FIXME clean up KBS
		//ptoolbar.add(tbcontents,BorderLayout.CENTER);
		//BoxLayout bl = new BoxLayout(tbcontents,BoxLayout.Y_AXIS);
		//tbcontents.setLayout(new GridLayout(0,1));
		/* FIXME clean up KBS
		final JPanel view=new JPanel(new BorderLayout());
		final JPanel scale=new JPanel(new BorderLayout());
		final JPanel inquire=new JPanel(new BorderLayout());
		tbcontents.add(scale);
		tbcontents.add(view);
		tbcontents.add(inquire);
		*/
		try {
			ptoolbar.setRollover(true);
			//view.add(new JLabel("View", SwingConstants.CENTER),BorderLayout.NORTH);


			final JButton bupdate = new JButton(iUpdate);
			bupdate.setToolTipText(getHTML("<u>U</u>pdate display with most current data."));
			bupdate.setActionCommand(Action.UPDATE);
			bupdate.addActionListener(action);
			ptoolbar.add(bupdate);

			final JButton blinear = new JButton(iLinLog);
			blinear.setToolTipText(getHTML("<u>Li</u>near/<u>Lo</u>g scale toggle."));
			blinear.setActionCommand(Action.SCALE);
			blinear.addActionListener(action);
			ptoolbar.add(blinear);

			final JButton bauto = new JButton(iAutoScale);
			bauto.setToolTipText(getHTML("<u>A</u>utomatically set the counts scale."));
			bauto.setActionCommand(Action.AUTO);
			bauto.addActionListener(action);
			ptoolbar.add(bauto);

			final JButton brange = new JButton(iRange);
			brange.setToolTipText(getHTML("<u>Ra</u>nge set counts scale."));
			brange.setActionCommand(Action.RANGE);
			brange.addActionListener(action);
			ptoolbar.add(brange);

			brebin = new JButton(iRebin);
			brebin.setToolTipText(getHTML("<u>Re</u>bin, enter a bin width in the console."));
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

		    bgoto= new JButton(iGoto);
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

		    bnetarea= new JButton(iNetArea);
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

			//tbcontents.add(Box.createVerticalGlue());

			/* FIXME cleanup KBS
			final JPanel firstPanel=new JPanel(new GridLayout(0,1));


			brebin.setToolTipText("After clicking, enter a bin width in the console.");
			brebin.setActionCommand(Action.REBIN);

			brebin.addActionListener(action);
			firstPanel.add(bupdate);
			final JPanel viewCenter=new JPanel(new BorderLayout());
			view.add(viewCenter,BorderLayout.CENTER);
			viewCenter.add(firstPanel,BorderLayout.NORTH);
			final JPanel zoomPanel = new JPanel(new GridLayout(2,2));

			final Icon i_expand = new ImageIcon(urlZoom);
			final JButton bexpand = new JButton(i_expand);
			bexpand.setToolTipText(getHTML("<u>E</u>xpand. Set new display limits."));
			bexpand.setActionCommand(Action.EXPAND);
			bexpand.addActionListener(action);
			zoomPanel.add(bexpand);
			final Icon iFullScale =new ImageIcon(urlFullScale);
			final JButton bfull = new JButton(iFullScale);
			//final JButton bfull = new JButton(getHTML("<u>F</u>ull"));
			bfull.setActionCommand(Action.FULL);
			bfull.setToolTipText(
				"Set display limits to full histogram extents.");
			bfull.addActionListener(action);
			zoomPanel.add(bfull);
			final Icon i_zoomin = new ImageIcon(urlZoomIn);
			final JButton bzoomin = new JButton(i_zoomin);
			bzoomin.setToolTipText(getHTML(
			"<u>Z</u>oom<u>i</u>n the display limits."));
			bzoomin.setActionCommand(Action.ZOOMIN);
			bzoomin.addActionListener(action);
			zoomPanel.add(bzoomin);
			final Icon i_zoomout = new ImageIcon(urlZoomOut);
			final JButton bzoomout = new JButton(i_zoomout);
			bzoomout.setToolTipText(getHTML(
			"<u>Z</u>oom<u>o</u>ut the display limits."));
			bzoomout.setActionCommand(Action.ZOOMOUT);
			bzoomout.addActionListener(action);
			zoomPanel.add(bzoomout);
			viewCenter.add(zoomPanel,BorderLayout.CENTER);
			bgoto.setActionCommand(Action.GOTO);
			bgoto.setToolTipText("Zoom in on desired region");
			bgoto.addActionListener(action);
			view.add(bgoto,BorderLayout.SOUTH);
			scale.add(new JLabel("Scale", SwingConstants.CENTER),BorderLayout.NORTH);
			final JPanel scaleCenter=new JPanel(new GridLayout(0,1));
			scale.add(scaleCenter,BorderLayout.CENTER);

			final JButton blinear = new JButton(getHTML("<u>Li</u>near/<u>Lo</u>g"));
			blinear.setToolTipText(getHTML("Toggle <u>s</u>cale type."));
			blinear.setActionCommand(Action.SCALE);
			blinear.addActionListener(action);
			scaleCenter.add(blinear);

			scaleCenter.add(brebin);
			inquire.add(new JLabel("Inquire", SwingConstants.CENTER),BorderLayout.NORTH);
			final Icon iArea = new ImageIcon(urlArea);
			final JButton barea = new JButton(iArea);
			//final JButton barea = new JButton(getHTML("<u>Ar</u>ea"));
			barea.setToolTipText(
				"Click here then on display to get area and summary stats of a region.");
			barea.setActionCommand(Action.AREA);
			barea.addActionListener(action);
			final JPanel inquireCenter=new JPanel(new GridLayout(0,1));
			inquire.add(inquireCenter,BorderLayout.CENTER);
			inquireCenter.add(barea);
			bnetarea.setToolTipText(
				"Get net area and summary stats of a region.");
			bnetarea.setActionCommand(Action.NETAREA);
			bnetarea.addActionListener(action);
			inquireCenter.add(bnetarea);
			final JButton bcancel = new JButton(getHTML("<u>C</u>ancel"));
			bcancel.setActionCommand(Action.CANCEL);
			bcancel.setToolTipText("Cancel an action in progress.");
			bcancel.addActionListener(action);
			inquireCenter.add(new JPanel());//blank grid space before cancel
			inquireCenter.add(bcancel);
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load icons for tool bar
	 */
	private Icon loadToolbarIcon(String path) {
		final Icon toolbarIcon;
		final ClassLoader cl = this.getClass().getClassLoader();
		final URL urlResource = cl.getResource(path);
		if (!(urlResource==null)) {
			toolbarIcon = new ImageIcon(urlResource);
		} else {//instead use path, ugly but lets us see button
			JOptionPane.showMessageDialog(this,"Can't load resource: "+path,
			"Missing Icon",JOptionPane.ERROR_MESSAGE);
			toolbarIcon=null; //FIXME KBS put in default icon here
		}
		return toolbarIcon;
	}

	private String getHTML(String body){
		final StringBuffer rval=new StringBuffer("<html><body>").append(
		body).append("</html></body>");
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
