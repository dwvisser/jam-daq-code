package jam.plot;

import jam.commands.CommandManager;
import jam.data.AbstractHist1D;
import jam.data.Histogram;
import jam.data.Gate;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.ui.Console;

import java.awt.Point;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

class Action implements PlotMouseListener, PreferenceChangeListener {

	static final String HELP = "help";

	static final String EXPAND = "expand";

	static final String ZOOMIN = "zoomin";

	static final String ZOOMOUT = "zoomout";

	static final String ZOOMVERT = "zoomvert";

	static final String ZOOMHORZ = "zoomhorz";
	
	static final String FULL = "full";

	static final String LINEAR = "linear";

	static final String LOG = "log";

	static final String AREA = "area";

	static final String GOTO = "goto";

	static final String NETAREA = "netarea";

	static final String UPDATE = "update";

	static final String AUTO = "auto";

	static final String OVERLAY = "overlay";

	static final String OVERLAY_STATE = "overlaystate";
	
	static final String CANCEL = "cancel";

	static final String RANGE = "range";

	static final String DISPLAY = "display";

	static final String REBIN = "rebin";

	static final String SCALE = "scale";

	static final String CURSOR = "cursor";

	private final PlotFit inquire;

	private final NumberFormat numFormat;

	/** Variable to indicate mouse was pressed */
	private boolean mousePressed;

	/** Accessed by Display. */
	private boolean settingGate;

	/** current command being processed */
	private String currentCommand;

	/** Is there a command present */
	private boolean commandPresent;

	/** Command requires a cursor input */
	private boolean isCursorCommand;

	//private boolean overlayState;

	/** Class that parses commands */
	private final ParseCommand parseCommand;

	private final Bin cursor;

	private final List clicks = new ArrayList();

	private int countLow, countHigh;

	/* reference auto scale on expand */
	private boolean autoOnExpand = true;

	/** Output text to */
	private final MessageHandler textOut;

	/** Plot displayer */
	private final Display display;

	/** Jam status to get current histogram */
	private static final JamStatus STATUS = JamStatus.instance();

	/** Broadcaster for event and gate change */
	private static final Broadcaster BROADCASTER = Broadcaster.getSingletonInstance();

	/**
	 * Master constructor has no broadcaster.
	 * 
	 * @param d
	 *            the histogram displayer
	 * @param jc
	 *            Jam's console component
	 */
	Action(Display d, Console jc) {
		display = d;
		textOut = jc;
		parseCommand = new ParseCommand(this);
		jc.addCommandListener(parseCommand);
		cursor = Bin.Factory.create();
		commandPresent = false;
		//overlayState = false;
		settingGate = false;
		inquire = new PlotFit(); //class with area/centroid routines
		/* numFormat for formatting energy output */
		numFormat = NumberFormat.getInstance();
		numFormat.setGroupingUsed(false);
		final int fracDigits = 2;
		numFormat.setMinimumFractionDigits(fracDigits);
		numFormat.setMaximumFractionDigits(fracDigits);
		PlotPrefs.PREFS.addPreferenceChangeListener(this);
	}

	/**
	 * Set the current plot, i.e., the one we will do actions on. Reset the
	 * current state.
	 */
	synchronized void plotChanged() {
		/* Clear command if in middle of a command */
		/* Command present at more and 1 or more clicks */
		if (commandPresent && clicks.size() > 0) {
			done();
		}
		settingGate = false;
		//overlayState = false;
	}

	/**
	 * @see PlotMouseListener#plotMousePressed(Bin, Point)
	 */
	public synchronized void plotMousePressed(Bin pChannel, Point pPixel) {
		/* cursor position and counts for that channel */
		cursor.setChannel(pChannel);
		/* see if there is a command currently being processed */
		if (commandPresent) {
			/* Do the command */
			doCommand(currentCommand, false);
		} else if (settingGate) {
			/* No command being processed check if gate is being set */
			final PlotContainer currentPlot = display.getPlot();
			BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SET_POINT,
					pChannel);
			currentPlot.displaySetGate(GateSetMode.GATE_CONTINUE, pChannel,
					pPixel);
		} else {
			doCommand(CURSOR, false);
		}
	}

	/* non-javadoc:
	 * Command with no paramters
	 * 
	 * @param inCommand
	 */
	void doCommand(String inCommand, boolean console) {
		doCommand(inCommand, null, console);
	}

	/*
	 * non-javadoc: does a command with parameters
	 */
	synchronized void doCommand(String inCommand, final double[] inParams, boolean console) {
		/* if inCommand is null, keep currentCommand */
		if (inCommand != null) {
			if (!inCommand.equals(CURSOR)) {
				/* Not a cursor command so its a "real" command */
				if (inCommand != currentCommand) {
					/* cancel previous command */
					done();
				}
				currentCommand = inCommand;
			} else {
				/* use cursor only if current command does not exist */
				if (currentCommand == null) {
					currentCommand = inCommand;
				}
			}
		}
		/* check that a histogram is defined */
		if (STATUS.getCurrentHistogram() != null) {
			doCurrentCommand(inParams == null ? new double[0] : inParams, console);
		}
	}
	
	private void doCurrentCommand(double [] parameters, boolean console){
		if (CANCEL.equals(currentCommand)) {
			cancel();
		} else if (DISPLAY.equals(currentCommand)) {
			display(parameters);
		} else if (OVERLAY.equals(currentCommand)) {
			overlay(parameters);			
		/*} else if (OVERLAY_STATE.equals(currentCommand)) {			
			overlayEnable(parameters);*/
		} else if (CURSOR.equals(currentCommand)) {
			channelDisplay();
		} else if (UPDATE.equals(currentCommand)) {
			update();
		} else if (EXPAND.equals(currentCommand)) {
			expand();
		} else if (ZOOMIN.equals(currentCommand)) {
			zoomin();
		} else if (ZOOMOUT.equals(currentCommand)) {
			zoomout();			
		} else if (ZOOMVERT.equals(currentCommand)) {
			zoomvert();
		} else if (ZOOMHORZ.equals(currentCommand)) {			
			zoomhorz();
		} else if (FULL.equals(currentCommand)) {
			full();
		} else if (LINEAR.equals(currentCommand)) {
			linear();
		} else if (LOG.equals(currentCommand)) {
			log();
		} else if (SCALE.equals(currentCommand)) {
			changeScale();
		} else if (AUTO.equals(currentCommand)) {
			auto();
		} else if (RANGE.equals(currentCommand)) {
			range(console);
		} else if (AREA.equals(currentCommand)) {
			areaCent();
		} else if (GOTO.equals(currentCommand)) {
			gotoChannel();
		} else if (NETAREA.equals(currentCommand)) {
			netArea();
		} else if (REBIN.equals(currentCommand)) {
			rebin(parameters);
		} else if (HELP.equals(currentCommand)) {
			help();
		} else {
			done();
			textOut.errorOutln("Plot command not recognized.");
		}
	}

	synchronized boolean getIsCursorCommand() {
		return isCursorCommand;
	}

	synchronized void setCursor(Bin cursorIn) {
		cursor.setChannel(cursorIn);
	}

	/*
	 * non-javadoc: display the counts at cursor
	 */
	private void channelDisplay() {
		/* output counts for the channel */
        final double count;
        final int xch;
        final int ych;
        String binText;
        /* check that a histogram is defined */
        final Histogram hist = STATUS.getCurrentHistogram();
        final PlotContainer currentPlot = display.getPlot();
        synchronized (cursor) {
            xch = cursor.getX();
            ych = cursor.getY();
            count = cursor.getCounts();
        }
        currentPlot.markChannel(cursor);
        if (currentPlot.getDimensionality() == 1) {
            binText = "Bin " + xch + ":  Counts = " + numFormat.format(count);
            if (isCalibrated(hist)) {
                final double energy = currentPlot.getEnergy(xch);
                binText = binText + "  Energy = " + numFormat.format(energy);
            }
        } else {//2 Dim plot
            binText = "Bin " + xch + "," + ych + ":  Counts = "
                    + numFormat.format(count);
        }
        textOut.messageOutln(binText);
        done();
    }

	/**
	 * Call <code>update()</code> on the current plot, reset the command-line,
	 * and broadcast a histogram selection message to force the rest of the GUI
	 * to be consistent with the currently selected histogram.
	 * 
	 * @see PlotContainer#update()
	 */
	private void update() {
		isCursorCommand = false;
		BROADCASTER.broadcast(BroadcastEvent.Command.OVERLAY_OFF);
		display.update();
		
		//Reset rebin to 1
		final PlotContainer currentPlot = display.getPlot();
		currentPlot.setBinWidth(1.0);		
		
		done();
		/*
		 * following to recover the chooser if user just overlayed a histogram
		 */
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT);
	}

	/**
	 * @param hist
	 *            the first element of which is the number of the hist to
	 *            display
	 */
	void display(double[] hist) {
		if (!commandPresent) {
			isCursorCommand = false;
			init();
			textOut
					.messageOut("Display histogram number: ",
							MessageHandler.NEW);
		}
		if (hist.length > 0) {
			final int num = (int) hist[0];
			final Histogram h = Histogram.getHistogram(num);
			if (h != null) {
				JamStatus.instance().setHistName(h.getName());
				textOut.messageOut(Integer.toString(num) + " ",
						MessageHandler.END);
				display.removeOverlays();
				BROADCASTER.broadcast(BroadcastEvent.Command.OVERLAY_OFF);
				BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
						h);
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
	/**
	 * Overlay a histogram
	 * @param hist
	 */
	void overlay(double[] hist) {
		if (!commandPresent) {
			isCursorCommand = false;
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
				} else if (display.getPlot().getDimensionality() != 1) {
					textOut
							.errorOutln(" Current histogram not 1D, so it cannot be overlaid.");

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

	/*void overlayEnable(double[] enable) {
        init();
        textOut.messageOut("Overlay histogram numbers: ", MessageHandler.NEW);
        if (enable[0] < 1.0) {
            overlayState = false;
        } else {
            overlayState = true;
        }
        display.setOverlay(overlayState);
        done();
    }*/
	
	/**
	 * Display a gate
	 * @param params
	 */
	void displayGate(Object []params ) {
		Gate gate =(Gate)params[0];
		STATUS.setCurrentGateName(gate.getName());
		BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SELECT, gate);
		final double area = gate.getArea();
		if (gate.getDimensionality() == 1) {
			final double centroid = ((int) (gate.getCentroid() * 100.0)) / 100.0;
			final int lowerLimit = gate.getLimits1d()[0];
			final int upperLimit = gate.getLimits1d()[1];
			textOut.messageOut("Gate: " + gate.getName() + ", Ch. "
					+ lowerLimit + " to " + upperLimit, MessageHandler.NEW);
			textOut.messageOut("  Area = " + area + ", Centroid = "
					+ centroid, MessageHandler.END);
		} else {
			textOut
					.messageOut("Gate " + gate.getName(),
							MessageHandler.NEW);
			textOut.messageOut(", Area = " + area, MessageHandler.END);
		}
	}
	
	/*
	 * non-javadoc: Set the range for the counts scale.
	 */
	private void range(boolean console) {
		if (!commandPresent) {
			isCursorCommand = true;
			init();
			textOut.messageOut("Range from ", MessageHandler.NEW);
		} else {
			final PlotContainer plot = display.getPlot();
			final boolean twoD = plot.getDimensionality() == 2;
			final boolean useCounts = twoD && !console;
			final double cts = useCounts ? cursor.getCounts() : cursor.getY();
			if (clicks.size() == 0) {
				countLow = (int)cts;
				clicks.add(cursor);
				textOut.messageOut("" + countLow + " to ");
			} else {
				countHigh = (int)cts;
				clicks.add(cursor);
				plot.setRange(countLow, countHigh);
				textOut.messageOut(String.valueOf(countHigh),
						MessageHandler.END);
				done();
			}
		}
	}

	/*
	 * non-javadoc: Set the range for the counts scale.
	 */
	private void rebin(double[] parameters) {
		if (!commandPresent) {
			isCursorCommand = false;
			init();
			textOut.messageOut("Rebin ", MessageHandler.NEW);
		}
		final PlotContainer currentPlot = display.getPlot();
		final Histogram hist = STATUS.getCurrentHistogram();
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
				textOut
						.warningOutln("Rebin command ignored. Bin value must be \u2265 1.0 and smaller than the histogram.");
				done();
			}
		}
	}
	
	/**
	 * Expand the region to view.
	 */
	private void expand() {
		final PlotContainer currentPlot = display.getPlot();
		if (!commandPresent) {
			isCursorCommand = true;
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
	 * Expand the region to view.
	 */
	private void zoomhorz() {
		final PlotContainer currentPlot = display.getPlot();
		if (!commandPresent) {
			isCursorCommand = true;
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
	
	private void zoomvert(){
		
	}




	/**
	 * Zoom in on the histogram
	 */
	private void zoomin() {
		isCursorCommand = false;
		final PlotContainer currentPlot = display.getPlot();
		currentPlot.zoom(PlotContainer.Zoom.IN);
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}

	/**
	 * Zoom out on the histogram.
	 */
	private void zoomout() {
		isCursorCommand = false;
		final PlotContainer currentPlot = display.getPlot();
		currentPlot.zoom(PlotContainer.Zoom.OUT);
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}
	
	/**
	 * Display the full histogram.
	 */
	private void full() {
		isCursorCommand = false;
		final PlotContainer currentPlot = display.getPlot();
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
		isCursorCommand = false;
		display.getPlot().setLinear();
		done();
	}

	/**
	 * Set the counts to log scale.
	 */
	private void log() {
		isCursorCommand = false;
		display.getPlot().setLog();
		done();
	}

	/**
	 * Change the scale for linear to log or log to linear
	 */
	private void changeScale() {
		isCursorCommand = false;
		if (display.getPlot().getLimits().getScale() == Scale.LINEAR) {
			log();
		} else {
			linear();
		}
	}

	
	/**
	 * Auto scale the plot.
	 */
	private void auto() {
		isCursorCommand = false;
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
		final PlotContainer currentPlot = display.getPlot();
		final Histogram hist = STATUS.getCurrentHistogram();
		if (!commandPresent) {
			isCursorCommand = true;
			init();
			if (currentPlot.getDimensionality() == 1 && isCalibrated(hist)) {
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
				if (!isCalibrated(hist)) {
					output.append(ch).append(eq).append(x);
				} else {
					if (currentPlot.getDimensionality() == 1) {
						output.append(en).append(eq).append(
								currentPlot.getEnergy(x)).append(sep)
								.append(ch).append(eq).append(x);
					}
				}
				if (currentPlot.getDimensionality() == 1) {
					if (!mousePressed) { //FIXME KBS
						if (isCalibrated(hist)) {
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
			auto();
			done();
		}
	}

	/**
	 * Calculate the area and centroid for a region maybe should copy inquire
	 * methods to this class
	 */
	private void areaCent() {
		final PlotContainer currentPlot = display.getPlot();
		if (!commandPresent) {
			isCursorCommand = true;
			init();
			final String name = STATUS.getHistName().trim();
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
			if (currentPlot.getDimensionality() == 1) {
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
		final PlotContainer currentPlot = display.getPlot();
		final Histogram hist = STATUS.getCurrentHistogram();
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
				if (currentPlot.getDimensionality() == 1) {
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
				if (currentPlot.getDimensionality() == 1) {
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
				if (currentPlot.getDimensionality() == 1) {
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
				if (currentPlot.getDimensionality() == 1) {
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
				if (currentPlot.getDimensionality() == 1) {
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
				if (currentPlot.getDimensionality() == 1) {
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
			if (isCalibrated(hist)) {
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
	 * Cancel current command
	 *  
	 */
	private void cancel() {
		isCursorCommand = false;
		textOut.messageOutln();
		done();
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
			isCursorCommand = true;
			commandPresent = false;
			mousePressed = false;
			currentCommand = null;
			clicks.clear();
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
	 * @see PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange(PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();
		if (key.equals(PlotPrefs.AUTO_ON_EXPAND)) {
			setAutoOnExpand(Boolean.valueOf(newValue).booleanValue());
		}
	}

	String getCurrentCommand() {
		return currentCommand;
	}
	
	private static boolean isCalibrated(Histogram hist){
		return hist != null && hist instanceof AbstractHist1D ?
			((AbstractHist1D)hist).isCalibrated() : false;
	}
	
	/* non-javadoc:
	 * Display help
	 */
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
			sb.append(commands[i]).append('\t');
		}
		textOut.messageOutln(sb.toString());
	}
}