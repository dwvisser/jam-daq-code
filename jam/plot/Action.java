package jam.plot;

import jam.commands.CommandManager;
import jam.data.AbstractHist1D;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.ui.Console;

import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

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

class Action implements PlotMouseListener, PreferenceChangeListener,
		PlotCommands {

	/** Broadcaster for event and gate change */
	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	private final static String chan = "channel";

	private final static String energy = "energy";

	private final static Map<String, Method> NO_ARG_MAP = new HashMap<String, Method>();

	private static final String S_TO = " to ";

	/** Jam status to get current histogram */
	private static final JamStatus STATUS = JamStatus.getSingletonInstance();

	private static final Logger LOGGER = Logger.getLogger("jam.plot");

	static {
		final List<String> NO_ARG_CMDS = new ArrayList<String>();
		NO_ARG_CMDS.add(HELP);
		NO_ARG_CMDS.add(EXPAND);
		NO_ARG_CMDS.add(ZOOMIN);
		NO_ARG_CMDS.add(ZOOMOUT);
		NO_ARG_CMDS.add(ZOOMVERT);
		NO_ARG_CMDS.add(ZOOMHORZ);
		NO_ARG_CMDS.add(FULL);
		NO_ARG_CMDS.add(LINEAR);
		NO_ARG_CMDS.add(LOG);
		NO_ARG_CMDS.add(AREA);
		NO_ARG_CMDS.add(GOTO);
		NO_ARG_CMDS.add(NETAREA);
		NO_ARG_CMDS.add(UPDATE);
		NO_ARG_CMDS.add(AUTO);
		NO_ARG_CMDS.add(CANCEL);
		NO_ARG_CMDS.add(SCALE);
		NO_ARG_CMDS.add(CURSOR);
		for (String command : NO_ARG_CMDS) {
			try {
				final Method method = Action.class.getDeclaredMethod(command);
				NO_ARG_MAP.put(command, method);
			} catch (NoSuchMethodException nsme) {
				LOGGER.log(Level.SEVERE, nsme.getMessage(), nsme);
			}
		}
	}

	private static boolean isCalibrated(final Histogram hist) {
		return hist != null && hist instanceof AbstractHist1D ? ((AbstractHist1D) hist)
				.isCalibrated()
				: false;
	}

	/* reference auto scale on expand */
	private transient boolean autoOnExpand = true;

	private final transient List<Bin> clicks = new ArrayList<Bin>();

	/** Is there a command present */
	private transient boolean commandPresent;

	private transient int countLow, countHigh;

	/** current command being processed */
	private transient String currentCommand;

	private transient final Bin cursorBin;

	private transient final PlotFit inquire;

	/** Command requires a cursor input */
	private transient boolean isCursorCommand;

	/** Variable to indicate mouse was pressed */
	private transient boolean mousePressed;

	private transient final NumberFormat numFormat;

	/** Plot displayer */
	private transient final PlotDisplay plotDisplay;

	/** Accessed by Display. */
	private transient boolean settingGate;

	/** Output text to */
	private transient final MessageHandler textOut;

	// private transient final Bin.Factory binFactory =
	// Bin.Factory.getInstance();

	/**
	 * Master constructor has no broadcaster.
	 * 
	 * @param disp
	 *            the histogram displayer
	 * @param console
	 *            Jam's console component
	 */
	Action(PlotDisplay disp, Console console) {
		super();
		plotDisplay = disp;
		textOut = console;
		ParseCommand parseCommand = new ParseCommand(this);
		console.addCommandListener(parseCommand);
		cursorBin = Bin.create();
		commandPresent = false;
		// overlayState = false;
		settingGate = false;
		inquire = PlotFit.getInstance(); // class with area/centroid routines
		/* numFormat for formatting energy output */
		numFormat = NumberFormat.getInstance();
		numFormat.setGroupingUsed(false);
		final int fracDigits = 2;
		numFormat.setMinimumFractionDigits(fracDigits);
		numFormat.setMaximumFractionDigits(fracDigits);
		PlotPrefs.PREFS.addPreferenceChangeListener(this);
	}

	private void addClick(final Bin bin) {
		synchronized (this) {
			clicks.add((Bin) bin.clone());
		}
	}

	/**
	 * Calculate the area and centroid for a region. Maybe we should copy
	 * inquire methods to this class?
	 */
	@SuppressWarnings("unused")
	private void area() {
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		if (commandPresent) {
			if (clicks.size() == 0) {
				synchronized (cursorBin) {
					addClick(cursorBin);
					currentPlot.initializeSelectingArea(cursorBin);
					currentPlot.markChannel(cursorBin);
					textOut.messageOut(cursorBin.getCoordString() + S_TO);
				}
			} else {
				currentPlot.setSelectingArea(false);
				final Bin lim1 = getClick(0);
				if (currentPlot.getDimensionality() == 1) {
					synchronized (cursorBin) {
						textOut.messageOut(String.valueOf(cursorBin.getX()));
						final double[] counts = (double[]) currentPlot
								.getCounts();
						final double area = inquire.getArea(counts, lim1,
								cursorBin);
						final double centroid = inquire.getCentroid(counts,
								lim1, cursorBin);
						final double fwhm = inquire.getFWHM(counts, lim1,
								cursorBin);
						currentPlot.markChannel(cursorBin);
						currentPlot.markArea(lim1, cursorBin);
						textOut.messageOut(":  Area = "
								+ numFormat.format(area) + ", Centroid = "
								+ numFormat.format(centroid) + ", FWHM = "
								+ numFormat.format(fwhm), MessageHandler.END);
					}
				} else {// 2D histogram
					synchronized (cursorBin) {
						textOut.messageOut(cursorBin.getCoordString());
						final double[][] counts = (double[][]) currentPlot
								.getCounts();
						final double area = inquire.getArea(counts, lim1,
								cursorBin);
						currentPlot.markChannel(cursorBin);
						currentPlot.markArea(lim1, cursorBin);
						textOut.messageOut(":  Area = "
								+ numFormat.format(area), MessageHandler.END);
					}
				}
				done();
			}
		} else {
			isCursorCommand = true;
			init();
			final String name = ((Histogram) STATUS.getCurrentHistogram())
					.getFullName().trim();
			textOut.messageOut("Area for " + name + " from channel ",
					MessageHandler.NEW);
		}
	}

	/**
	 * Auto scale the plot.
	 */
	private void auto() {
		isCursorCommand = false;
		plotDisplay.getPlotContainer().autoCounts();
		done();
	}

	/**
	 * Cancel current command
	 * 
	 */
	@SuppressWarnings("unused")
	private void cancel() {
		isCursorCommand = false;
		textOut.messageOutln();
		done();
	}

	/*
	 * non-javadoc: display the counts at cursor
	 */
	@SuppressWarnings("unused")
	private void cursor() {
		/* output counts for the channel */
		final double count;
		final int xch;
		final int ych;
		String binText;
		/* check that a histogram is defined */
		final Histogram hist = (Histogram) STATUS.getCurrentHistogram();
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		synchronized (cursorBin) {
			xch = cursorBin.getX();
			ych = cursorBin.getY();
			count = cursorBin.getCounts();
		}
		currentPlot.markChannel(cursorBin);
		if (currentPlot.getDimensionality() == 1) {
			binText = "Bin " + xch + ":  Counts = " + numFormat.format(count);
			if (isCalibrated(hist)) {
				final double chEnergy = currentPlot.getEnergy(xch);
				binText = binText + "  Energy = " + numFormat.format(chEnergy);
			}
		} else {// 2 Dim plot
			binText = "Bin " + xch + "," + ych + ":  Counts = "
					+ numFormat.format(count);
		}
		textOut.messageOutln(binText);
		done();
	}

	/**
	 * @param hist
	 *            the first element of which is the number of the hist to
	 *            display
	 */
	void display(final double[] hist) {
		if (!commandPresent) {
			isCursorCommand = false;
			init();
			textOut
					.messageOut("Display histogram number: ",
							MessageHandler.NEW);
		}
		if (hist.length > 0) {
			final int num = (int) hist[0];
			final Histogram histogram = Histogram.getHistogram(num);
			if (histogram == null) {
				textOut.messageOut(Integer.toString(num), MessageHandler.END);
				LOGGER.severe("There is no histogram numbered " + num + ".");
			} else {
				final JamStatus status = JamStatus.getSingletonInstance();
				status.setCurrentHistogram(histogram);
				status.setCurrentGroup(histogram.getGroup());
				textOut.messageOut(Integer.toString(num) + " ",
						MessageHandler.END);
				plotDisplay.removeOverlays();
				BROADCASTER.broadcast(BroadcastEvent.Command.OVERLAY_OFF);
				BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
						histogram);
			}
			if (hist.length > 1) {
				if (histogram.getDimensionality() == 1) {
					final int newlen = hist.length - 1;
					final double[] pass = new double[newlen];
					System.arraycopy(hist, 1, pass, 0, newlen);
					overlay(pass);
				} else {
					textOut
							.errorOutln(histogram.getFullName().trim()
									+ " is not 1D, so you may not overlay other histograms.");
				}
			} else {
				done();
			}
		}
	}

	/*
	 * non-javadoc: Command with no paramters
	 * 
	 * @param inCommand
	 */
	void doCommand(final String inCommand, final boolean console) {
		doCommand(inCommand, null, console);
	}

	/*
	 * non-javadoc: does a command with parameters
	 */
	void doCommand(final String inCommand, final double[] inParams,
			final boolean console) {
		synchronized (this) {
			/* if inCommand is null, keep currentCommand */
			if (inCommand != null) {
				if (inCommand.equals(CURSOR)) {
					/* use cursor only if current command does not exist */
					if (currentCommand == null) {
						currentCommand = inCommand;
					}
				} else {
					/* Not a cursor command so its a "real" command */
					if (!inCommand.equals(currentCommand)) {
						/* cancel previous command */
						done();
					}
					currentCommand = inCommand;
				}
			}
			/* check that a histogram is defined */
			if (STATUS.getCurrentHistogram() != null) {
				doCurrentCommand(inParams == null ? new double[0] : inParams,
						console);
			}
		}
	}

	private void doCurrentCommand(final double[] parameters,
			final boolean console) {
		if (DISPLAY.equals(currentCommand)) {
			display(parameters);
		} else if (OVERLAY.equals(currentCommand)) {
			overlay(parameters);
		} else if (RANGE.equals(currentCommand)) {
			range(console);
		} else if (REBIN.equals(currentCommand)) {
			rebin(parameters);
		} else if (NO_ARG_MAP.containsKey(currentCommand)) {
			try {
				final Method method = NO_ARG_MAP.get(currentCommand);
				method.invoke(this);
			} catch (IllegalAccessException iae) {
				LOGGER.log(Level.SEVERE, iae.getMessage(), iae);
			} catch (InvocationTargetException ite) {
				LOGGER.log(Level.SEVERE, ite.getMessage(), ite);
			}
		} else {
			done();
			LOGGER.severe("Plot command, \"" + currentCommand
					+ "\", not recognized.");
		}
	}

	/**
	 * A command has been completed so clean up
	 */
	private void done() {
		synchronized (this) {
			isCursorCommand = true;
			commandPresent = false;
			mousePressed = false;
			currentCommand = null;
			clicks.clear();
			plotDisplay.getPlotContainer().setSelectingArea(false);
		}
	}

	/**
	 * Expand the region to view.
	 */
	@SuppressWarnings("unused")
	private void expand() {
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		if (commandPresent) {
			if (clicks.size() == 0) {
				synchronized (cursorBin) {
					currentPlot.initializeSelectingArea(cursorBin);
					addClick(cursorBin);
					textOut.messageOut(cursorBin.getCoordString() + S_TO);
				}
			} else {
				currentPlot.setSelectingArea(false);
				synchronized (cursorBin) {
					textOut.messageOut(cursorBin.getCoordString(),
							MessageHandler.END);
					currentPlot.expand(getClick(0), cursorBin);
				}
				if (autoOnExpand) {
					currentPlot.autoCounts();
				}
				done();
			}
		} else {
			isCursorCommand = true;
			init();
			textOut.messageOut("Expand from channel ", MessageHandler.NEW);
		}
	}

	/**
	 * Display the full histogram.
	 */
	@SuppressWarnings("unused")
	private void full() {
		isCursorCommand = false;
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		currentPlot.setFull();
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}

	private Bin getClick(final int bin) {
		return clicks.get(bin);
	}

	String getCurrentCommand() {
		return currentCommand;
	}

	boolean getIsCursorCommand() {
		synchronized (this) {
			return isCursorCommand;
		}
	}

	/**
	 * Goto input channel
	 */
	@SuppressWarnings("unused")
	private void go() {
		final String cal = "calibrated";
		final char space = ' ';
		final String intro = "Goto (click on spectrum or type the ";
		final char leftParen = ')';
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		final Histogram hist = (Histogram) STATUS.getCurrentHistogram();
		if (commandPresent) {
			if (clicks.size() == 0) {
				goNoClicks(currentPlot, hist);
			}
		} else {
			isCursorCommand = true;
			init();
			if (currentPlot.getDimensionality() == 1 && isCalibrated(hist)) {
				final String mess = new StringBuffer(intro).append(cal).append(
						space).append(energy).append(leftParen).append(space)
						.toString();
				textOut.messageOut(mess, MessageHandler.NEW);
			} else {
				final String mess = new StringBuffer(intro).append(chan)
						.append(leftParen).append(space).toString();
				textOut.messageOut(mess, MessageHandler.NEW);
			}
		}
	}

	private void goNoClicks(final PlotContainer currentPlot,
			final Histogram hist) {
		final String sep = ", ";
		final String equal = " = ";
		synchronized (cursorBin) {
			addClick(cursorBin);
			StringBuffer output = new StringBuffer();
			int xCoord = cursorBin.getX();
			if (isCalibrated(hist)) {
				if (currentPlot.getDimensionality() == 1) {
					output.append(energy).append(equal).append(
							currentPlot.getEnergy(xCoord)).append(sep).append(
							chan).append(equal).append(xCoord);
				}
			} else {
				output.append(chan).append(equal).append(xCoord);
			}
			if (currentPlot.getDimensionality() == 1 && !mousePressed
					&& isCalibrated(hist)) {
				output = new StringBuffer(energy).append(equal).append(xCoord);
				synchronized (this) {
					xCoord = currentPlot.getChannel(xCoord);
					if (xCoord > currentPlot.getSizeX()) {
						xCoord = currentPlot.getSizeX() - 1;
					}
				}
				output.append(sep).append(chan).append(equal).append(xCoord);
			}
			final int rangeToUse = 100;
			final int halfRange = rangeToUse / 2;
			final int channelLow = xCoord - halfRange;
			final int channelHigh = channelLow + rangeToUse;
			currentPlot.expand(Bin.create(channelLow), Bin.create(channelHigh));
			textOut.messageOut(output.toString(), MessageHandler.END);
		}
		auto();
		done();
	}

	/*
	 * non-javadoc: Display help
	 */
	@SuppressWarnings("unused")
	private void help() {
		final StringBuffer buffer = new StringBuffer("Commands:\t");
		buffer
				.append("li - Linear Scale\tlo - Log Scale\ta  - Auto Scale\tra - Range\t");
		buffer
				.append("ex - Expand\tf  - Full view\t zi - Zoom In\tzo - Zoom Out\t");
		buffer.append("d  - Display\to  - Overlay\tu  - Update\tg  - GoTo\t");
		buffer.append("ar - Area\tn  - Net Area\tre - Rebin\tc  - Bin\t");
		final String[] commands = CommandManager.getInstance().getAllCommands();
		for (int i = 0; i < commands.length; i++) {
			buffer.append(commands[i]).append('\t');
		}
		textOut.messageOutln(buffer.toString());
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
	 * Set the counts to linear scale.
	 */
	private void linear() {
		isCursorCommand = false;
		plotDisplay.getPlotContainer().setLinear();
		done();
	}

	/**
	 * Set the counts to log scale.
	 */
	private void log() {
		isCursorCommand = false;
		plotDisplay.getPlotContainer().setLog();
		done();
	}

	/**
	 * Background subtracted intensity of 1-d plots
	 */
	@SuppressWarnings("unused")
	private void netarea() {
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		final Histogram hist = (Histogram) STATUS.getCurrentHistogram();
		final double[] netArea = new double[1];
		final double[] netAreaError = new double[1];
		final double[] fwhm = new double[2];
		final double[] centroidError = new double[2];
		final double[] centroid = new double[1];
		final double[] channelBackground = new double[currentPlot.getSizeX()];
		final int nclicks = clicks.size();
		final String crt = "\n\t";
		if (!commandPresent) {
			isCursorCommand = true;
			init();
			final String name = hist.getFullName().trim();
			textOut
					.messageOut(
							"Net Area fit for "
									+ name
									+ ": select four background markers, then two region-of-interest markers. ",
							MessageHandler.NEW);
		} else if (nclicks == 0) {
			// ************ First background Marker
			// ***********************************
			synchronized (cursorBin) {
				addClick(cursorBin);
				currentPlot.markChannel(cursorBin);
				if (currentPlot.getDimensionality() == 1) {
					textOut.messageOut(crt + "Background " + cursorBin.getX()
							+ S_TO, MessageHandler.CONTINUE);
				} else {
					textOut.messageOut(cursorBin.getCoordString() + S_TO,
							MessageHandler.CONTINUE);
				}
			}
		} else if (nclicks == 1) {
			// ************ Second Background marker
			// **********************************
			synchronized (cursorBin) {
				addClick(cursorBin);
				final Bin bin1 = getClick(0);
				currentPlot.markChannel(cursorBin);
				if (currentPlot.getDimensionality() == 1) {
					currentPlot.markArea(bin1, cursorBin);
					textOut.messageOut(Integer.toString(cursorBin.getX()));
				} else {
					textOut.messageOut(cursorBin.getCoordString(),
							MessageHandler.CONTINUE);
				}
			}
		} else if (nclicks == 2) {
			// ************ Third Background Marker
			// **********************************
			synchronized (cursorBin) {
				addClick(cursorBin);
				currentPlot.markChannel(cursorBin);
				if (currentPlot.getDimensionality() == 1) {
					textOut.messageOut(" and " + cursorBin.getX() + S_TO);

				} else {
					textOut.messageOut(cursorBin.getCoordString() + S_TO,
							MessageHandler.CONTINUE);
				}
			}
		} else if (nclicks == 3) {
			// ************ Fourth Background Marker
			// *********************************
			synchronized (cursorBin) {
				addClick(cursorBin);
				final Bin bin1 = getClick(2);
				currentPlot.markChannel(cursorBin);
				if (currentPlot.getDimensionality() == 1) {
					currentPlot.markArea(bin1, cursorBin);
					textOut.messageOut(String.valueOf(cursorBin.getX()),
							MessageHandler.CONTINUE);
				} else {
					textOut.messageOut(cursorBin.getCoordString(),
							MessageHandler.CONTINUE);
				}
			}
		} else if (nclicks == 4) {
			// ************ First Region Marker
			// *********************************
			synchronized (cursorBin) {
				currentPlot.initializeSelectingArea(cursorBin);
				addClick(cursorBin);
				currentPlot.markChannel(cursorBin);
				if (currentPlot.getDimensionality() == 1) {
					textOut.messageOut("." + crt + "Peak " + cursorBin.getX()
							+ S_TO, MessageHandler.CONTINUE);
				} else {

					textOut.messageOut(cursorBin.getCoordString() + S_TO,
							MessageHandler.CONTINUE);
				}
			}
		} else if (nclicks == 5) {
			// ************ Second Region Marker
			// *********************************
			currentPlot.setSelectingArea(false);
			final Bin bin4;
			synchronized (cursorBin) {
				addClick(cursorBin);
				bin4 = getClick(4);
				currentPlot.markChannel(cursorBin);
				if (currentPlot.getDimensionality() == 1) {
					currentPlot.markArea(bin4, cursorBin);
					textOut.messageOut(cursorBin.getX() + ". ",
							MessageHandler.CONTINUE);
				} else {
					textOut.messageOut(cursorBin.getCoordString() + ". ",
							MessageHandler.CONTINUE);
				}
			}
			final double[] counts = (double[]) currentPlot.getCounts();
			final double grossArea = inquire.getArea(counts, bin4, cursorBin);
			final Bin[] passClicks = new Bin[clicks.size()];
			clicks.toArray(passClicks);
			/* results of next call are passed back in the parameters */
			inquire.getNetArea(netArea, netAreaError, channelBackground, fwhm,
					centroid, centroidError, passClicks, grossArea, currentPlot
							.getSizeX(), counts);
			if (isCalibrated(hist)) {
				centroid[0] = currentPlot.getEnergy(centroid[0]);
				fwhm[0] = currentPlot.getEnergy(fwhm[0]);
				fwhm[1] = currentPlot.getEnergy(0.0);
				centroidError[0] = currentPlot.getEnergy(centroidError[0]);
				centroidError[1] = currentPlot.getEnergy(0.0);
				fwhm[0] = fwhm[0] - fwhm[1];
				centroidError[0] = centroidError[0] - centroidError[1];
			}
			final char plusMinus = '\u00b1';
			textOut.messageOut(crt + "Gross Area = " + grossArea + plusMinus
					+ numFormat.format(Math.sqrt(grossArea)) + crt
					+ "NetArea = " + numFormat.format(netArea[0]) + plusMinus
					+ numFormat.format(netAreaError[0]) + crt + "Centroid = "
					+ numFormat.format(centroid[0]) + plusMinus
					+ numFormat.format(centroidError[0]) + crt + "FWHM = "
					+ numFormat.format(fwhm[0]), MessageHandler.END);
			/* Draw Fit on screen by calling DisplayFit in Display.java */
			final int[] bgdPts = { getClick(0).getX(), getClick(1).getX(),
					getClick(2).getX(), getClick(3).getX() };
			Arrays.sort(bgdPts);
			final int lowerLimit = bgdPts[0];
			final int upperLimit = bgdPts[3] + 1;
			final double[] bkgd = new double[upperLimit - lowerLimit + 1];
			System.arraycopy(channelBackground, lowerLimit, bkgd, 0,
					bkgd.length);
			plotDisplay.displayFit(null, bkgd, null, lowerLimit);
			done();
		}
	}

	/**
	 * Overlay a histogram
	 * 
	 * @param hist
	 */
	void overlay(final double[] hist) {
		if (!commandPresent) {
			isCursorCommand = false;
			init();
			textOut.messageOut("Overlay histogram numbers: ",
					MessageHandler.NEW);
		}
		final boolean areHists = hist.length > 0;
		for (int i = 0; i < hist.length; i++) {
			final int num = (int) hist[i];
			final Histogram histogram = Histogram.getHistogram(num);
			if (histogram == null) {
				LOGGER.warning("There is no histogram numbered " + num
						+ ".");
			} else {
				if (histogram.getDimensionality() == 1) {
					if (plotDisplay.getPlotContainer().getDimensionality() == 1) {
						plotDisplay.overlayHistogram(num);
						textOut.messageOut(Integer.toString(num) + ' ',
								MessageHandler.CONTINUE);
					} else {
						LOGGER.warning(" Current histogram not 1D, so it cannot be overlaid.");

					}
				} else {
					LOGGER.warning(histogram.getFullName().trim()
							+ " is not 1D, so it cannot be overlaid.");
				}
			}
		}
		if (areHists) {
			done();
		}
	}

	/**
	 * Set the current plot, i.e., the one we will do actions on. Reset the
	 * current state.
	 */
	void plotChanged() {
		synchronized (this) {
			/* Clear command if in middle of a command */
			/* Command present at more and 1 or more clicks */
			if (commandPresent && clicks.size() > 0) {
				done();
			}
			settingGate = false;
		}
	}

	/**
	 * @see PlotMouseListener#plotMousePressed(Bin, Point)
	 */
	public void plotMousePressed(final Bin pChannel, final Point pPixel) {
		synchronized (this) {
			/* cursor position and counts for that channel */
			cursorBin.setChannel(pChannel);
			/* see if there is a command currently being processed */
			if (commandPresent) {
				/* Do the command */
				doCommand(currentCommand, false);
			} else if (settingGate) {
				/* No command being processed check if gate is being set */
				final PlotContainer currentPlot = plotDisplay
						.getPlotContainer();
				BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SET_POINT,
						pChannel);
				currentPlot.displaySetGate(GateSetMode.GATE_CONTINUE, pChannel,
						pPixel);
			} else {
				doCommand(CURSOR, false);
			}
		}
	}

	/**
	 * @see PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();
		if (key.equals(PlotPrefs.AUTO_ON_EXPAND)) {
			setAutoOnExpand(Boolean.valueOf(newValue).booleanValue());
		}
	}

	/*
	 * non-javadoc: Set the range for the counts scale.
	 */
	private void range(final boolean console) {
		if (commandPresent) {
			final PlotContainer plot = plotDisplay.getPlotContainer();
			final boolean twoD = plot.getDimensionality() == 2;
			final boolean useCounts = twoD && !console;
			final double cts = useCounts ? cursorBin.getCounts() : cursorBin
					.getY();
			if (clicks.size() == 0) {
				countLow = (int) cts;
				clicks.add(cursorBin);
				textOut.messageOut("" + countLow + S_TO);
			} else {
				countHigh = (int) cts;
				clicks.add(cursorBin);
				plot.setRange(countLow, countHigh);
				textOut.messageOut(String.valueOf(countHigh),
						MessageHandler.END);
				done();
			}
		} else {
			isCursorCommand = true;
			init();
			textOut.messageOut("Range from ", MessageHandler.NEW);
		}
	}

	/*
	 * non-javadoc: Set the range for the counts scale.
	 */
	private void rebin(final double[] parameters) {
		if (!commandPresent) {
			isCursorCommand = false;
			init();
			textOut.messageOut("Rebin ", MessageHandler.NEW);
		}
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		final Histogram hist = (Histogram) STATUS.getCurrentHistogram();
		if (parameters.length > 0) {
			final double binWidth = parameters[0];
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
				LOGGER
						.warning("Rebin command ignored. Bin value must be \u2265 1.0 and smaller than the histogram.");
				done();
			}
		}
	}

	/**
	 * Change the scale for linear to log or log to linear
	 */
	@SuppressWarnings("unused")
	private void scale() {
		isCursorCommand = false;
		if (plotDisplay.getPlotContainer().getLimits().getScale() == Scale.LINEAR) {
			log();
		} else {
			linear();
		}
	}

	/**
	 * Sets whether expand/zoom also causes an auto-scale.
	 * 
	 * @param whether
	 *            <code>true</code> if auto-scale on expand or zoom is desired
	 */
	private void setAutoOnExpand(final boolean whether) {
		synchronized (this) {
			autoOnExpand = whether;
		}
	}

	void setCursor(final Bin cursorIn) {
		synchronized (this) {
			cursorBin.setChannel(cursorIn);
		}
	}

	void setDefiningGate(final boolean whether) {
		synchronized (this) {
			settingGate = whether;
		}
	}

	void setMousePressed(final boolean whether) {
		synchronized (this) {
			mousePressed = whether;
		}
	}

	/**
	 * Call <code>update()</code> on the current plot, reset the command-line,
	 * and broadcast a histogram selection message to force the rest of the GUI
	 * to be consistent with the currently selected histogram.
	 * 
	 * @see PlotContainer#update()
	 */
	@SuppressWarnings("unused")
	private void update() {
		isCursorCommand = false;
		BROADCASTER.broadcast(BroadcastEvent.Command.OVERLAY_OFF);
		plotDisplay.update();

		// Reset rebin to 1
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		currentPlot.setBinWidth(1.0);

		done();
		/*
		 * following to recover the chooser if user just overlayed a histogram
		 */
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT);
	}

	/**
	 * Expand the region to view.
	 */
	@SuppressWarnings("unused")
	private void zoomhorz() {
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		if (!commandPresent) {
			isCursorCommand = true;
			init();
			textOut.messageOut("Expand from channel ", MessageHandler.NEW);
		} else if (clicks.size() == 0) {
			synchronized (cursorBin) {
				currentPlot.initializeSelectingArea(cursorBin);
				addClick(cursorBin);
				textOut.messageOut(cursorBin.getCoordString() + S_TO);
			}
		} else {
			currentPlot.setSelectingArea(false);
			synchronized (cursorBin) {
				textOut.messageOut(cursorBin.getCoordString(),
						MessageHandler.END);
				currentPlot.expand(getClick(0), cursorBin);
			}
			if (autoOnExpand) {
				currentPlot.autoCounts();
			}
			done();
		}
	}

	/**
	 * Zoom in on the histogram
	 */
	@SuppressWarnings("unused")
	private void zoomin() {
		isCursorCommand = false;
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		currentPlot.zoom(PlotContainer.Zoom.IN);
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}

	/**
	 * Zoom out on the histogram.
	 */
	@SuppressWarnings("unused")
	private void zoomout() {
		isCursorCommand = false;
		final PlotContainer currentPlot = plotDisplay.getPlotContainer();
		currentPlot.zoom(PlotContainer.Zoom.OUT);
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}

	@SuppressWarnings("unused")
	private void zoomvert() {
		// do-nothing
	}
}