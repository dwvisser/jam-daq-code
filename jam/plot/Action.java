package jam.plot;

import jam.JamConsole;
import jam.commands.CommandManager;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandListener;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.JOptionPane;

/**
 * Class the does the actions on plots. Receives commands from buttons and
 * command line. Performs action by performing command on plot, plot1d and
 * plot2d. Commands avaliable:
 * <dl>
 * <dt>update</dt>
 * <dd>update a plot with current data</dd>
 * <dt>expand</dt>
 * <dd>expand channels to view</dd>
 * <dt>zoomin, zoomout</dt>
 * <dt>full</dt>
 * <dd>view all channles</dd>
 * <dt>linear, log</dt>
 * <dt>range</dt>
 * <dd>change the count range</dd>
 * <dt>auto</dt>
 * <dd>auto scale the count range</dd>
 * <dt>area</dt>
 * <dd>get an area</dd>
 * <dt>cancel</dt>
 * </dl>
 * 
 * @author Ken Swartz
 * @version 0.5
 */

class Action implements ActionListener, PlotMouseListener,
		PreferenceChangeListener, CommandListener {

	static final String HELP = "help";

	static final String EXPAND = "expand";

	static final String ZOOMIN = "zoomin";

	static final String ZOOMOUT = "zoomout";

	static final String FULL = "full";

	static final String LINEAR = "linear";

	static final String LOG = "log";

	static final String AREA = "area";

	static final String GOTO = "goto";

	static final String NETAREA = "netarea";

	static final String UPDATE = "update";

	static final String AUTO = "auto";

	static final String OVERLAY = "overlay";

	static final String CANCEL = "cancel";

	static final String RANGE = "range";

	static final String DISPLAY = "display";

	static final String REBIN = "rebin";

	static final String SCALE = "scale";

	private static final JamStatus status = JamStatus.instance();

	private boolean autoOnExpand = true;

	/**
	 * Variable to indicate mouse was pressed
	 */
	private boolean mousePressed;

	/**
	 * Accessed by Display.
	 */
	private boolean settingGate;

	private final MessageHandler textOut;

	private final Display display;

	private Broadcaster broadcaster = Broadcaster.getSingletonInstance();

	private final PlotFit inquire;

	private final NumberFormat numFormat;

	//current state
	private String inCommand;

	private String lastCommand;

	private boolean commandPresent;

	private boolean overlayState;

	/**
	 * Used by the GoTo action to let the code know to check for a calibration.
	 */
	private boolean energyEx = false;

	private final Bin cursor;

	private final List clicks = new ArrayList();

	private final Map commandMap;

	private int countLow, countHigh;

	/**
	 * Master constructor has no broadcaster.
	 * 
	 * @param d
	 *            the histogram displayer
	 * @param jc
	 *            Jam's console component
	 */
	Action(Display d, JamConsole jc) {
		display = d;
		textOut = jc;
		cursor = Bin.Factory.create();
		jc.addCommandListener(this);
		commandPresent = false;
		overlayState = false;
		settingGate = false;
		inquire = new PlotFit(); //class with area/centroid routines
		/* numFormat for formatting energy output */
		numFormat = NumberFormat.getInstance();
		numFormat.setGroupingUsed(false);
		final int fracDigits = 2;
		numFormat.setMinimumFractionDigits(fracDigits);
		numFormat.setMaximumFractionDigits(fracDigits);
		commandMap = createCommandMap();
		PlotPrefs.prefs.addPreferenceChangeListener(this);
	}

	/**
	 * Set the current plot, i.e., the one we will do actions on. Reset the
	 * current state.
	 */
	synchronized void plotChanged() {
		settingGate = false;
		overlayState = false;
		commandPresent = false;
		mousePressed = false;
		inCommand = null;
		clicks.clear();
		rangeList.clear();
	}

	/**
	 * Routine called by pressing a button on the action toolbar.
	 * 
	 * @param e
	 *            the event created by the button press
	 */
	public void actionPerformed(ActionEvent e) {
		final String com = e.getActionCommand();
		/* cancel previous command if command has changed */
		if (com != lastCommand) {
			done();
		}
		inCommand = com;
		doCommand();
	}

	final private Map createCommandMap() {
		final String[] commands = { HELP, EXPAND, ZOOMIN, ZOOMOUT, FULL,
				LINEAR, LOG, AREA, GOTO, NETAREA, UPDATE, AUTO, OVERLAY,
				CANCEL, EXPAND, EXPAND, RANGE, DISPLAY, REBIN, SCALE };
		final String[] abbr = { "help", "ex", "zi", "zo", "f", "li", "lo",
				"ar", "g", "n", "u", "a", "o", "c", "x", "y", "ra", "d", "re",
				"s" };
		if (commands.length != abbr.length) {
			JOptionPane
					.showMessageDialog(
							display,
							"Make sure commands and abbreviations arrays are equal in length.",
							"Error in code.", JOptionPane.ERROR_MESSAGE);
		}
		final Map rval = new HashMap();
		for (int i = 0; i < commands.length; i++) {
			rval.put(abbr[i], commands[i]);
		}
		return rval;
	}

	public boolean performParseCommand(String _command, String[] cmdParams) {
		boolean accept = false; //is the command accepted
		boolean handleIt = false;
		final String command = _command.toLowerCase();
		/*
		 * int is a special case meaning no command and just parameters
		 */
		if (command.equals(JamConsole.NUMBERS_ONLY)) {
			final double[] parameters = convertParameters(cmdParams);
			if (DISPLAY.equals(inCommand)) {
				display(parameters);
				accept = true;
			} else if (OVERLAY.equals(inCommand)) {
				overlay(parameters);
				accept = true;
			} else {
				integerChannel(parameters);
				accept = true;
			}
			accept = true;
		} else if (commandMap.containsKey(command)) {
			inCommand = (String) commandMap.get(command);
			accept = true;
			handleIt = true;
		}
		if (accept && handleIt) {
			final double[] parameters = convertParameters(cmdParams);
			if (DISPLAY.equals(inCommand)) {
				display(parameters);
			} else if (OVERLAY.equals(inCommand)) {
				overlay(parameters);
			} else {
				doCommand();
				integerChannel(parameters);
			}
		}
		return accept;
	}

	/**
	 * Convert the parameters to doubles.
	 * 
	 * @param parameters
	 * @return
	 */
	private double[] convertParameters(String[] cmdParams) {
		final int numberParams = cmdParams.length;
		double[] parameters = new double[numberParams];
		/* The parameters must be numbers. */
		try {
			int countParam = 0;
			while (countParam < numberParams) {
				parameters[countParam] = convertNumber(cmdParams[countParam]);
				countParam++;
			}
		} catch (NumberFormatException nfe) {
			throw new NumberFormatException("Input parameter not a number.");
		}
		return parameters;

	}

	/**
	 * Sort the input command and do command.
	 */
	private synchronized void doCommand() {
		lastCommand = inCommand;
		/* check that a histogram is defined */
		if (status.getCurrentHistogram() != null) {
			if (CANCEL.equals(inCommand)) {
				textOut.messageOutln();
				done();
			} else if (HELP.equals(inCommand)) {
				help();
			} else if (UPDATE.equals(inCommand)) {
				update();
			} else if (EXPAND.equals(inCommand)) {
				expand();
			} else if (ZOOMIN.equals(inCommand)) {
				zoomin();
			} else if (ZOOMOUT.equals(inCommand)) {
				zoomout();
			} else if (FULL.equals(inCommand)) {
				full();
			} else if (LINEAR.equals(inCommand)) {
				linear();
			} else if (LOG.equals(inCommand)) {
				log();
			} else if (SCALE.equals(inCommand)) {
				if (display.getPlot().getLimits().getScale() == Limits.ScaleType.LINEAR) {
					log();
				} else {
					linear();
				}
			} else if (AUTO.equals(inCommand)) {
				auto();
			} else if (RANGE.equals(inCommand)) {
				range();
			} else if (AREA.equals(inCommand)) {
				areaCent();
			} else if (GOTO.equals(inCommand)) {
				energyEx = true;
				gotoChannel();
			} else if (NETAREA.equals(inCommand)) {
				netArea();
			} else if (REBIN.equals(inCommand)) {
				rebin();
			} else {
				done();
				textOut.errorOutln(getClass().getName() + ".doCommand() '"
						+ inCommand + "' not recognized.");
			}
		}
	}

	/**
	 * Routine called back by mouse a mouse clicks on plot
	 * 
	 * @param pChannel
	 *            channel of mouse click
	 */
	public synchronized void plotMousePressed(Bin pChannel, Point pPixel) {
		/* check that a histogram is defined */
		final Plot currentPlot = display.getPlot();
		final Histogram hist = status.getCurrentHistogram();
		if (hist == null) {
			return;
		}
		/* cursor position and counts for that channel */
		cursor.setChannel(pChannel);
		/* there is a command currently being processed */
		if (commandPresent) {
			if (RANGE.equals(inCommand)) {
				rangeList.add(new Integer((int) cursor.getCounts()));
			}
			doCommand();
		} else {
			/*
			 * no command being processed check if gate is being set
			 */
			if (settingGate) {
				broadcaster.broadcast(BroadcastEvent.Command.GATE_SET_POINT,
						pChannel);
				currentPlot.displaySetGate(GateSetMode.GATE_CONTINUE, pChannel,
						pPixel);
			} else {
				/* output counts for the channel */
				final double count;
				final String coord;
				final int xch;
				synchronized (cursor) {
					xch = cursor.getX();
					count = cursor.getCounts();
					coord = cursor.getCoordString();
				}
				currentPlot.markChannel(cursor);
				if (hist.isCalibrated()) {
					final Plot plot = (Plot) currentPlot;
					final double energy = plot.getEnergy(xch);
					textOut.messageOutln("Bin " + xch + ":  Counts = "
							+ numFormat.format(count) + "  Energy = "
							+ numFormat.format(energy));
				} else {
					textOut.messageOutln("Bin " + xch + ":  Counts = "
							+ numFormat.format(count));
				}
				done();
			}
		}
	}

	private final List rangeList = Collections
			.synchronizedList(new ArrayList());

	/**
	 * Accepts integer input and does a command if one is present.
	 * 
	 * @param parameters
	 *            the integers
	 */
	private void integerChannel(double[] parameters) {
		final int numPar = parameters.length;
		/*
		 * FIXME we should be better organized so range and rebin are not
		 * special cases
		 */
		if ((commandPresent)) {
			if (RANGE.equals(inCommand)) {
				synchronized (cursor) {
					final int len = Math.min(numPar, 2);
					for (int i = 0; i < len; i++) {
						rangeList.add(new Integer((int) parameters[i]));
						doCommand();
					}
				}
				return;
			} else if (REBIN.equals(inCommand)) {
				if (numPar > 0) {
					parameter = new double[numPar];
					System.arraycopy(parameters, 0, parameter, 0, numPar);
				}
			}
		}
		/* we have a 1 d plot */
		final Plot currentPlot = display.getPlot();
		if (currentPlot.getType() == Plot.TYPE_1D) {
			if (commandPresent) {
				final int loopMax = Math.min(numPar, 2);
				for (int i = 0; i < loopMax; i++) {
					if (GOTO.equals(inCommand)) {
						cursor.setChannel((int) parameters[i], 0);
					} else {
						cursor.setChannel((int) parameters[i], 0);
						cursor.shiftInsidePlot();
					}
					doCommand();
				}
			} else { //no command so get channel
				if (numPar > 0) {
					/* check for out of bounds */
					synchronized (cursor) {
						cursor.setChannel((int) parameters[0], 0);
						cursor.shiftInsidePlot();
						final double cursorCount = cursor.getCounts();
						currentPlot.markChannel(cursor);
						textOut.messageOutln("Bin " + cursor.getX()
								+ ":  Counts = " + cursorCount);
					}
					done();
				}
			}
		} else { //we have a 2d plot
			if (commandPresent) {
				final int loopMax = Math.min(numPar, 4);
				synchronized (this) {
					for (int i = 1; i < loopMax; i += 2) {
						cursor.setChannel((int) parameters[i - 1],
								(int) parameters[i]);
						cursor.shiftInsidePlot();
						doCommand();
					}
				}
			} else { //no command so get channel
				if (numPar > 1) {
					cursor.setChannel((int) parameters[0], (int) parameters[1]);
					cursor.shiftInsidePlot();
					currentPlot.markChannel(cursor);
					textOut.messageOutln("Bin " + cursor.getCoordString()
							+ ":  Counts = " + cursor.getCounts());
					done();
				}
			}
		}
	}

	/**
	 * Call <code>update()</code> on the current plot, reset the command-line,
	 * and broadcast a histogram selection message to force the rest of the GUI
	 * to be consistent with the currently selected histogram.
	 * 
	 * @see Plot#update()
	 */
	private void update() {
		broadcaster.broadcast(BroadcastEvent.Command.OVERLAY_OFF);
		display.update();
		done();
		/*
		 * following to recover the chooser if user just overlayed a histogram
		 */
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT);
	}

	/**
	 * Expand the region to view.
	 */
	private void expand() {
		final Plot currentPlot = display.getPlot();
		if (!commandPresent) {
			init();
			textOut.messageOut("Expand from channel ", MessageHandler.NEW);
		} else if (clicks.size() == 0) {
			synchronized (cursor) {
				currentPlot.initializeSelectingArea(cursor);
				addClick(cursor);
				textOut.messageOut(cursor.getCoordString() + " to ");
			}
		} else {
			currentPlot.setSelectingArea(false);
			synchronized (cursor) {
				textOut.messageOut(cursor.getCoordString(), MessageHandler.END);
				currentPlot.expand(getClick(0), cursor);
			}
			if (autoOnExpand) {
				currentPlot.autoCounts();
			}
			done();
		}
	}

	/**
	 * @param hist
	 *            the first element of which is the number of the hist to
	 *            display
	 */
	private void display(double[] hist) {
		if (!commandPresent) {
			init();
			textOut
					.messageOut("Display histogram number: ",
							MessageHandler.NEW);
		}
		if (hist.length > 0) {
			final int num = (int) hist[0];
			final Histogram h = Histogram.getHistogram(num);
			if (h != null) {
				JamStatus.instance().setCurrentHistogramName(h.getName());
				textOut.messageOut(Integer.toString(num) + " ",
						MessageHandler.END);
				display.removeOverlays();
				broadcaster.broadcast(BroadcastEvent.Command.OVERLAY_OFF);
				broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT);
			} else {
				textOut.messageOut(Integer.toString(num), MessageHandler.END);
				textOut.errorOutln("There is no histogram numbered " + num
						+ ".");
			}
			if (hist.length > 1) {
				if (h.getDimensionality() != 1) {
					textOut
							.errorOutln(h.getName().trim()
									+ " is not 1D, so you may not overlay other histograms.");
				} else {
					final int newlen = hist.length - 1;
					final double[] pass = new double[newlen];
					System.arraycopy(hist, 1, pass, 0, newlen);
					overlay(pass);
				}
			} else {
				done();
			}
		}
	}

	private void overlay(double[] hist) {
		if (!commandPresent) {
			init();
			textOut.messageOut("Overlay histogram numbers: ",
					MessageHandler.NEW);
		}
		boolean areHists = hist.length > 0;
		for (int i = 0; i < hist.length; i++) {
			final int num = (int) hist[i];
			final Histogram h = Histogram.getHistogram(num);
			if (h != null) {
				if (h.getDimensionality() != 1) {
					textOut.errorOutln(h.getName().trim()
							+ " is not 1D, so it cannot be overlaid.");
				} else {
					display.overlayHistogram(num);
					textOut.messageOut(Integer.toString(num) + ' ',
							MessageHandler.CONTINUE);
				}
			} else {
				textOut.errorOutln("There is no histogram numbered " + num
						+ ".");
			}
		}
		if (areHists) {
			done();
		}
	}

	/**
	 * Set the range for the counts scale.
	 */
	private void range() {
		if (!commandPresent) {
			init();
			textOut.messageOut("Range from ", MessageHandler.NEW);
		} else if (rangeList.size() == 1) {
			countLow = ((Integer) rangeList.get(0)).intValue();
			textOut.messageOut(String.valueOf(countLow) + " to ");
		} else {
			countHigh = ((Integer) rangeList.get(1)).intValue();
			display.getPlot().setRange(countLow, countHigh);
			textOut.messageOut(String.valueOf(countHigh), MessageHandler.END);
			done();
		}
	}

	/**
	 * Set the range for the counts scale.
	 */
	private void rebin() {
		if (!commandPresent) {
			init();
			textOut.messageOut("Rebin ", MessageHandler.NEW);
		} else {
			final Plot currentPlot = display.getPlot();
			final Histogram hist = status.getCurrentHistogram();
			final double binWidth = parameter[0];
			if (binWidth >= 1.0 && binWidth < hist.getSizeX()) {
				currentPlot.setBinWidth(binWidth);
				textOut
						.messageOut(String.valueOf(binWidth),
								MessageHandler.END);
				currentPlot.repaint();
				done();
			} else {
				textOut
						.messageOut(String.valueOf(binWidth),
								MessageHandler.END);
				textOut
						.warningOutln("Rebin command ignored. Bin value must be \u2264 1.0 and smaller than the histogram.");
				done();
			}
		}
	}

	private double[] parameter = new double[0];

	/**
	 * Calculate the area and centroid for a region maybe should copy inquire
	 * methods to this class
	 */
	private void areaCent() {
		final Plot currentPlot = display.getPlot();
		if (!commandPresent) {
			init();
			final String name = status.getCurrentHistogramName().trim();
			textOut.messageOut("Area for " + name + " from channel ",
					MessageHandler.NEW);
		} else if (clicks.size() == 0) {
			synchronized (cursor) {
				addClick(cursor);
				currentPlot.initializeSelectingArea(cursor);
				currentPlot.markChannel(cursor);
				textOut.messageOut(cursor.getCoordString() + " to ");
			}
		} else {
			currentPlot.setSelectingArea(false);
			final Bin lim1 = getClick(0);
			if (currentPlot.getType() == Plot.TYPE_1D) {
				synchronized (cursor) {
					textOut.messageOut(String.valueOf(cursor.getX()));
					double[] counts = (double[]) currentPlot.getCounts();
					final double area = inquire.getArea(counts, lim1, cursor);
					final double centroid = inquire.getCentroid(counts, lim1,
							cursor);
					final double fwhm = inquire.getFWHM(counts, lim1, cursor);
					currentPlot.markChannel(cursor);
					currentPlot.markArea(lim1, cursor);
					textOut.messageOut(":  Area = " + numFormat.format(area)
							+ ", Centroid = " + numFormat.format(centroid)
							+ ", FWHM = " + numFormat.format(fwhm),
							MessageHandler.END);
				}
			} else {//2D histogram
				synchronized (cursor) {
					textOut.messageOut(cursor.getCoordString());
					double[][] counts = (double[][]) currentPlot.getCounts();
					final double area = inquire.getArea(counts, lim1, cursor);
					currentPlot.markChannel(cursor);
					currentPlot.markArea(lim1, cursor);
					textOut.messageOut(":  Area = " + numFormat.format(area),
							MessageHandler.END);
				}
			}
			done();
		}
	}

	/**
	 * Background subtracted intensity of 1-d plots
	 */
	private void netArea() {
		final Plot currentPlot = display.getPlot();
		final Histogram hist = status.getCurrentHistogram();
		final double[] netArea = new double[1];
		final double[] netAreaError = new double[1];
		final double[] fwhm = new double[2];
		final double[] centroidError = new double[2];
		final double[] centroid = new double[1];
		final double[] channelBackground = new double[currentPlot.getSizeX()];
		final int nclicks = clicks.size();
		final String crt = "\n\t";
		if (!commandPresent) {
			init();
			final String name = hist.getName().trim();
			textOut
					.messageOut(
							"Net Area fit for "
									+ name
									+ ": select four background markers, then two region-of-interest markers. ",
							MessageHandler.NEW);
		} else if (nclicks == 0) {
			//************ First background Marker
			// ***********************************
			synchronized (cursor) {
				addClick(cursor);
				currentPlot.markChannel(cursor);
				if (currentPlot.getType() == Plot.TYPE_1D) {
					textOut.messageOut(crt + "Background " + cursor.getX()
							+ " to ", MessageHandler.CONTINUE);
				} else {
					textOut.messageOut(cursor.getCoordString() + " to ",
							MessageHandler.CONTINUE);
				}
			}
		} else if (nclicks == 1) {
			//************ Second Background marker
			// **********************************
			synchronized (cursor) {
				addClick(cursor);
				final Bin p1 = getClick(0);
				currentPlot.markChannel(cursor);
				if (currentPlot.getType() == Plot.TYPE_1D) {
					currentPlot.markArea(p1, cursor);
					textOut.messageOut(Integer.toString(cursor.getX()));
				} else {
					textOut.messageOut(cursor.getCoordString(),
							MessageHandler.CONTINUE);
				}
			}
		} else if (nclicks == 2) {
			//************ Third Background Marker
			// **********************************
			synchronized (cursor) {
				addClick(cursor);
				currentPlot.markChannel(cursor);
				if (currentPlot.getType() == Plot.TYPE_1D) {
					textOut.messageOut(" and " + cursor.getX() + " to ");

				} else {
					textOut.messageOut(cursor.getCoordString() + " to ",
							MessageHandler.CONTINUE);
				}
			}
		} else if (nclicks == 3) {
			//************ Fourth Background Marker
			// *********************************
			synchronized (cursor) {
				addClick(cursor);
				final Bin p1 = getClick(2);
				currentPlot.markChannel(cursor);
				if (currentPlot.getType() == Plot.TYPE_1D) {
					currentPlot.markArea(p1, cursor);
					textOut.messageOut(String.valueOf(cursor.getX()),
							MessageHandler.CONTINUE);
				} else {
					textOut.messageOut(cursor.getCoordString(),
							MessageHandler.CONTINUE);
				}
			}
		} else if (nclicks == 4) {
			//************ First Region Marker
			// *********************************
			synchronized (cursor) {
				currentPlot.initializeSelectingArea(cursor);
				addClick(cursor);
				currentPlot.markChannel(cursor);
				if (currentPlot.getType() == Plot.TYPE_1D) {
					textOut.messageOut("." + crt + "Peak " + cursor.getX()
							+ " to ", MessageHandler.CONTINUE);
				} else {

					textOut.messageOut(cursor.getCoordString() + " to ",
							MessageHandler.CONTINUE);
				}
			}
		} else if (nclicks == 5) {
			//************ Second Region Marker
			// *********************************
			currentPlot.setSelectingArea(false);
			final Bin p4;
			synchronized (cursor) {
				addClick(cursor);
				p4 = getClick(4);
				currentPlot.markChannel(cursor);
				if (currentPlot.getType() == Plot.TYPE_1D) {
					currentPlot.markArea(p4, cursor);
					textOut.messageOut(cursor.getX() + ". ",
							MessageHandler.CONTINUE);
				} else {
					textOut.messageOut(cursor.getCoordString() + ". ",
							MessageHandler.CONTINUE);
				}
			}
			double[] counts = (double[]) currentPlot.getCounts();
			final double grossArea = inquire.getArea(counts, p4, cursor);
			final Bin[] passClicks = new Bin[clicks.size()];
			clicks.toArray(passClicks);
			/* results of next call are passed back in the parameters */
			inquire.getNetArea(netArea, netAreaError, channelBackground, fwhm,
					centroid, centroidError, passClicks, grossArea, currentPlot
							.getSizeX(), counts);
			if (hist.isCalibrated()) {
				centroid[0] = currentPlot.getEnergy(centroid[0]);
				fwhm[0] = currentPlot.getEnergy(fwhm[0]);
				fwhm[1] = currentPlot.getEnergy(0.0);
				centroidError[0] = currentPlot.getEnergy(centroidError[0]);
				centroidError[1] = currentPlot.getEnergy(0.0);
				fwhm[0] = fwhm[0] - fwhm[1];
				centroidError[0] = centroidError[0] - centroidError[1];
			}
			final char pm = '\u00b1';
			textOut.messageOut(crt + "Gross Area = " + grossArea + pm
					+ numFormat.format(Math.sqrt(grossArea)) + crt
					+ "NetArea = " + numFormat.format(netArea[0]) + pm
					+ numFormat.format(netAreaError[0]) + crt + "Centroid = "
					+ numFormat.format(centroid[0]) + pm
					+ numFormat.format(centroidError[0]) + crt + "FWHM = "
					+ numFormat.format(fwhm[0]), MessageHandler.END);
			/* Draw Fit on screen by calling DisplayFit in Display.java */
			final int[] bgdPts = { getClick(0).getX(), getClick(1).getX(),
					getClick(2).getX(), getClick(3).getX() };
			Arrays.sort(bgdPts);
			final int ll = bgdPts[0];
			final int ul = bgdPts[3] + 1;
			final double[] bkgd = new double[ul - ll + 1];
			System.arraycopy(channelBackground, ll, bkgd, 0, bkgd.length);
			display.displayFit(null, bkgd, null, ll);
			done();
		}
	}

	private Bin getClick(int i) {
		return (Bin) clicks.get(i);
	}

	/**
	 * Zoom in on the histogram
	 */
	private void zoomin() {
		final Plot currentPlot = display.getPlot();
		currentPlot.zoom(Plot.ZOOM_IN);
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}

	/**
	 * Zoom out on the histogram.
	 */
	private void zoomout() {
		final Plot currentPlot = display.getPlot();
		currentPlot.zoom(Plot.ZOOM_OUT);
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}

	/**
	 * Display the full histogram.
	 */
	private void full() {
		final Plot currentPlot = display.getPlot();
		currentPlot.setFull();
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}

	/**
	 * Set the counts to linear scale.
	 */
	private void linear() {
		display.getPlot().setLinear();
		done();
	}

	/**
	 * Set the counts to log scale.
	 */
	private void log() {
		display.getPlot().setLog();
		done();
	}

	/**
	 * Auto scale the plot.
	 */
	private void auto() {
		display.getPlot().autoCounts();
		done();
	}

	/**
	 * Goto input channel
	 */
	private void gotoChannel() {
		final String sep = ", ";
		final String eq = " = ";
		final String ch = "channel";
		final String en = "energy";
		final String cal = "calibrated";
		final char sp = ' ';
		final String intro = "Goto (click on spectrum or type the ";
		final char lp = ')';
		final Plot currentPlot = display.getPlot();
		final Histogram hist = status.getCurrentHistogram();
		if (!commandPresent) {
			init();
			if (currentPlot.getType() == Plot.TYPE_1D && hist.isCalibrated()) {
				final String mess = new StringBuffer(intro).append(cal).append(
						sp).append(en).append(lp).append(sp).toString();
				textOut.messageOut(mess, MessageHandler.NEW);
			} else {
				final String mess = new StringBuffer(intro).append(ch).append(
						lp).append(sp).toString();
				textOut.messageOut(mess, MessageHandler.NEW);
			}
		} else if (clicks.size() == 0) {
			synchronized (cursor) {
				addClick(cursor);
				StringBuffer output = new StringBuffer();
				int x = cursor.getX();
				if (!hist.isCalibrated()) {
					output.append(ch).append(eq).append(x);
				} else {
					if (currentPlot.getType() == Plot.TYPE_1D) {
						output.append(en).append(eq).append(
								currentPlot.getEnergy(x)).append(sep)
								.append(ch).append(eq).append(x);
					}
				}
				if (currentPlot.getType() == Plot.TYPE_1D) {
					if (!mousePressed) { //FIXME KBS
						if (hist.isCalibrated()) {
							output = new StringBuffer(en).append(eq).append(x);
							synchronized (this) {
								x = currentPlot.getChannel(x);
								if (x > currentPlot.getSizeX()) {
									x = currentPlot.getSizeX() - 1;
								}
							}
							output.append(sep).append(ch).append(eq).append(x);
						}
					}
				}
				final int rangeToUse = 100;
				final int halfRange = rangeToUse / 2;
				final int channelLow = x - halfRange;
				final int channelHigh = channelLow + rangeToUse;
				currentPlot.expand(Bin.Factory.create(channelLow), Bin.Factory
						.create(channelHigh));
				textOut.messageOut(output.toString(), MessageHandler.END);
			}
			synchronized (this) {
				energyEx = false;
			}
			auto();
			done();
		}
	}

	/**
	 * Initializes at the start of a new command accepted
	 */
	private void init() {
		synchronized (this) {
			commandPresent = true;
			clicks.clear();
		}
	}

	/**
	 * A command has been completed so clean up
	 */
	private void done() {
		synchronized (this) {
			commandPresent = false;
			mousePressed = false;
			inCommand = null;
			clicks.clear();
			rangeList.clear();
			display.getPlot().setSelectingArea(false);
		}
	}

	/**
	 * Sets whether expand/zoom also causes an auto-scale.
	 * 
	 * @param whether
	 *            <code>true</code> if auto-scale on expand or zoom is desired
	 */
	private synchronized void setAutoOnExpand(boolean whether) {
		autoOnExpand = whether;
	}

	synchronized void setDefiningGate(boolean whether) {
		settingGate = whether;
	}

	synchronized void setMousePressed(boolean whether) {
		mousePressed = whether;
	}

	private synchronized void addClick(Bin c) {
		clicks.add(Bin.copy(c));
	}

	/**
	 * Parse a string go a number
	 * 
	 * @param s
	 * @return
	 * @throws NumberFormatException
	 */
	private double convertNumber(String s) throws NumberFormatException {
		return (s.indexOf('.') >= 0) ? Double.parseDouble(s) : Integer
				.parseInt(s);
	}

	private void help() {
		final StringBuffer sb = new StringBuffer("Commands:\t");
		sb
				.append("li - Linear Scale\tlo - Log Scale\ta  - Auto Scale\tra - Range\t");
		sb
				.append("ex - Expand\tf  - Full view\t zi - Zoom In\tzo - Zoom Out\t");
		sb.append("d  - Display\to  - Overlay\tu  - Update\tg  - GoTo\t");
		sb.append("ar - Area\tn  - Net Area\tre - Rebin\tc  - Bin\t");
		final String[] commands = CommandManager.getInstance().getAllCommands();
		for (int i = 0; i < commands.length; i++) {
			sb.append(commands[i]).append("\t");
		}
		textOut.messageOutln(sb.toString());
	}

	public void preferenceChange(PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();
		if (key.equals(PlotPrefs.AUTO_ON_EXPAND)) {
			setAutoOnExpand(Boolean.valueOf(newValue).booleanValue());
		}
	}
}