package jam.plot;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import jam.global.*;
import jam.data.Histogram;

/**
 * Class the does the actions on plots. Receives commands from buttons and command
 * line. Performs action by performing command on plot, plot1d and plot2d.
 * Commands avaliable:
 * <dl><dt>update</dt><dd> update a plot with current data</dd>
 * <dt>expand</dt><dd>expand channels to view </dd>
 * <dt>zoomin, zoomout</dt>
 * <dt>full</dt><dd>view all channles </dd>
 * <dt>linear, log</dt>
 * <dt>range</dt><dd>change the count range   </dd>
 * <dt>auto</dt><dd>auto scale the count range  </dd>
 * <dt>area</dt><dd>get an area    </dd>
 * <dt>cancel </dt> </dl>
 *
 * @author Ken Swartz
 * @version 0.5
 */

public class Action
	implements ActionListener, PlotMouseListener, CommandListener {

	/**
	 *  Variable to indicate mouse was pressed
	 */
	static boolean mousePressed;

	/**
	 * Accessed by Display.
	 */
	static boolean settingGate;

	private MessageHandler textOut;
	private Display display;
	private Broadcaster broadcaster;
	private Plot currentPlot;
	private PlotFit inquire;
	private NumberFormat numFormat;

	//current state
	private String inCommand;
	private String lastCommand;
	private boolean commandPresent;
	private boolean overlayState;

	/**
	 * Used by the GoTo action to let the code know
	 * to check for a calibration.
	 */
	private boolean energyEx = false;

	//variables for commands
	private int numberPoints; //number of mouse points needed
	private int cursorX;
	private int cursorY;
	private double cursorCount;
	private int limX1;
	private int limX2;
	private int limY1;
	private int limY2;
	private double countLow;
	private double countHigh;
	private int inputCursorValue; //Used with goto button
	private int[][] xyCursor = new int[6][6];

	// Array used for markers in SimpleFit.java
	/**
	 * Master constructor has no broadcaster
	 * 
	 * @param Display
	 * @param messageHandler
	 */
	public Action(Display display, MessageHandler messageHandler) {
		this.display = display;
		this.textOut = messageHandler;
		commandPresent = false;
		numberPoints = 0;
		overlayState = false;
		settingGate = false;
		inquire = new PlotFit(); //class with area/centroid routines
		/* numFormat for formatting energy output */
		numFormat = NumberFormat.getInstance();
		numFormat.setGroupingUsed(false);
		numFormat.setMinimumFractionDigits(2);
		numFormat.setMaximumFractionDigits(2);
	}

	/**
	 * Add a broadcaster
	 */
	public void setBroadcaster(Broadcaster broadcaster) {
		this.broadcaster = broadcaster;
	}

	/**
	 * Set the current plot, i.e., the one we will do actions on.
	 * Reset the current state.
	 */
	public void setPlot(Plot mp) {
		currentPlot = mp;
		settingGate = false;
		overlayState = false;
	}

	/**
	 * For a button press
	 * routine called back by pressing a button
	 */
	public void actionPerformed(ActionEvent e) {
		/* cancel command if command has changed */
		if (e.getActionCommand() != lastCommand) {
			done();
		}
		inCommand = e.getActionCommand();
		doCommand();
	}

	/**
	 * Do a command sent in as a message. Sees if the 
	 * string is command that plot can understand
	 * and sets a command string to something that 
	 * can be interpreted by doCommand(), i.e.,
	 * expand abbreviations.
	 * 
	 * @param _command entry from console
	 * @param parameters integer parameters from console
	 */
	public void commandPerform(String _command, int[] parameters) {
		boolean accept = false; //is the command accepted
		boolean display = false;
		String command = _command.toLowerCase();
		int len = command.length();
		/* int is a special case meaning
		 * no command and just parameters */
		if (len >= 3) {
			if (command.substring(0, 3).equals("int")) {
				if (inCommand.equals("display")){
					display(parameters);
					return;
				} else {
					integerChannel(parameters);
					accept = true;
					return;
				}
			}
		}
		/* All your usual commands
		 * a command and array of integers was input */

		if (command.startsWith("ex")) {
			inCommand = "expand";
			accept = true;
		} else if (command.startsWith("zi")) {
			inCommand = "zoomin";
			accept = true;
		} else if (command.startsWith("zo")) {
			inCommand = "zoomout";
			accept = true;
		} else if (command.startsWith("fu")) {
			inCommand = "full";
			accept = true;
		} else if (command.startsWith("li")) {
			inCommand = "linear";
			accept = true;
		} else if (command.startsWith("lo")) {
			inCommand = "log";
			accept = true;
		} else if (command.startsWith("ar")) {
			inCommand = "area";
			accept = true;
		} else if (command.startsWith("go")) {
			inCommand = "goto"; // new goto button
			accept = true;
		} else if (command.startsWith("ne")) {
			inCommand = "netarea"; // new net area button
			accept = true;
		} else if (command.startsWith("u")) {
			inCommand = "update";
			accept = true;
		} else if (command.startsWith("a")) {
			inCommand = "auto";
			accept = true;
		} else if (command.startsWith("o")) {
			inCommand = "overlay";
			accept = true;
		} else if (command.startsWith("c")) {
			inCommand = "cancel";
			accept = true;
		} else if (command.startsWith("x")) {
			inCommand = "expand";
			accept = true;
		} else if (command.startsWith("y")) {
			inCommand = "expand";
			accept = true;
		} else if (command.startsWith("r")) {
			inCommand = "range";
			accept = true;
		} else if (command.startsWith("d")) {//display hist by #
			inCommand="display";
			display(parameters);
			display=true;
		}
		if (accept) {
			doCommand();
			integerChannel(parameters);
		} else if (!display) {
			textOut.errorOutln(getClass().getName()+".commandPerform(): Command '"+command+
			"' not understood.");
		}

	}

	/**
	 * Sort the input command and do command.
	 */
	private synchronized void doCommand() {
		lastCommand = inCommand;
		/* check that a histogram is defined */
		if (currentPlot.getHistogram() == null) {
			return;
		}
		if (inCommand == "cancel") {
			textOut.messageOutln("");
			done();
		} else if (inCommand == "update") {
			update();
		} else if (inCommand == "expand") {
			expand();
		} else if (inCommand == "zoomin") {
			zoomin();
		} else if (inCommand == "zoomout") {
			zoomout();
		} else if (inCommand == "full") {
			full();
		} else if (inCommand == "linear") {
			linear();
		} else if (inCommand == "log") {
			log();
		} else if (inCommand == "auto") {
			auto();
		} else if (inCommand == "range") {
			range();
		} else if (inCommand == "area") {
			areaCent();
		} else if (inCommand == "goto") {
			energyEx = true;
			gotoChannel();
		} else if (inCommand == "netarea") {
			netArea();
		} else {
			done();
			textOut.errorOutln(getClass().getName()+".doCommand() '"+
			inCommand + "' not recognized.");
		}
	}

	/**
	 * Routine called back by mouse a mouse clicks on plot
	 *
	 * @param pChannel  channel of mouse click
	 */
	public synchronized void plotMousePressed(Point pChannel, Point pPixel) {
		/* check that a histogram is defined */
		if (currentPlot.currentHist == null) {
			return;
		}
		/* cursor position and counts for that channel */
		cursorX = pChannel.x;
		cursorY = pChannel.y;
		cursorCount = currentPlot.getCount(cursorX, cursorY);
		/* there is a command currently being processed */
		if (commandPresent) {
			doCommand();
			/* no command being processed 
			 * check if gate is being set */
		} else {
			if (settingGate) {
				try {
					broadcaster.broadcast(
						BroadcastEvent.GATE_SET_POINT,
						pChannel);
				} catch (GlobalException ge) {
					textOut.errorOutln(
						getClass().getName() + ".plotMousePressed()" + ge);
				}
				currentPlot.displaySetGate(
					Plot.GATE_CONTINUE,
					pChannel,
					pPixel);
			} else {
				/* output counts for the channel */
				currentPlot.markChannel(cursorX, cursorY);
				if (currentPlot instanceof Plot1d) {
					if (currentPlot.isCalibrated) {
						Plot1d plot1d = (Plot1d) currentPlot;
						double energy = plot1d.getEnergy(cursorX);
						textOut.messageOutln(
							"Channel "
								+ cursorX
								+ ":  Counts = "
								+ numFormat.format(cursorCount)
								+ "  Energy = "
								+ numFormat.format(energy));
					} else {
						textOut.messageOutln(
							"Channel "
								+ cursorX
								+ ":  Counts = "
								+ numFormat.format(cursorCount));
					}
				} else {
					textOut.messageOutln(
						"Channel "
							+ cursorX
							+ ","
							+ cursorY
							+ ":  Counts = "
							+ numFormat.format(cursorCount));
				}
				done();
			}
		}
	}

	/**
	 * Accepts integer input and does a command if one
	 * is present
	 *
	 */
	public synchronized void integerChannel(int[] parameters) {
		int numPar = parameters.length;
		/* FIXME we should be better organized so this if is not here
		 * so range is not a special case */
		if ((commandPresent)) {
			if (inCommand == "range") {
				for (int i = 0;(i < numPar) && (i < 2); i++) {
					cursorY = parameters[i];
					cursorCount = parameters[i];
					doCommand();
				}
				return;
			} 
		}
		/* we have a 1 d plot */
		if (currentPlot instanceof Plot1d) {
			if (commandPresent) {
				for (int i = 0;(i < numPar) && (i < 2); i++) {
					//check for out of bounds
					if (parameters[i] < 0) {
						cursorX = 0;
					}
					if (energyEx && currentPlot.isCalibrated) {
						cursorX = parameters[i];
					} else if (parameters[i] > (currentPlot.getSizeX() - 1)) {
						cursorX = currentPlot.getSizeX() - 1;
					} else {
						cursorX = parameters[i];
					}
					cursorY = 0;
					if (!energyEx) {
						cursorCount = currentPlot.getCount(cursorX, cursorY);
					}
					doCommand();
				}
			} else { //no command so get channel
				if (numPar > 0) {
					/* check for out of bounds */
					if (parameters[0] < 0) {
						cursorX = 0;
					} else if (parameters[0] > (currentPlot.getSizeX() - 1)) {
						cursorX = currentPlot.getSizeX() - 1;
					} else {
						cursorX = parameters[0];
					}
					cursorY = 0;
					cursorCount = currentPlot.getCount(cursorX, cursorY);
					currentPlot.markChannel(cursorX, cursorY);
					textOut.messageOutln(
						"Channel " + cursorX + ":  Counts = " + cursorCount);
					done();
				}
			}
		} else { //we have a 2 d plot
			if (commandPresent) {
				for (int i = 1;(i < numPar) && (i < 4); i = i + 2) {
					/* check for out of bounds */
					if (parameters[i - 1] < 0) {
						cursorX = 0;
					} else if (
						parameters[i - 1] > (currentPlot.getSizeX() - 1)) {
						cursorX = currentPlot.getSizeX() - 1;
					} else {
						cursorX = parameters[i - 1];
					}
					if (parameters[i] < 0) {
						cursorY = 0;
					} else if (parameters[i] > (currentPlot.getSizeY() - 1)) {
						cursorY = currentPlot.getSizeY() - 1;
					} else {
						cursorY = parameters[i];
					}
					cursorCount = currentPlot.getCount(cursorX, cursorY);
					doCommand();
				}
			} else { //no command so get channel
				if (numPar > 1) {
					/* check for out of bounds */
					if (parameters[0] < 0) {
						cursorX = 0;
					} else if (parameters[0] > (currentPlot.getSizeX() - 1)) {
						cursorX = currentPlot.getSizeX() - 1;
					} else {
						cursorX = parameters[0];
					}
					if (parameters[1] < 0) {
						cursorY = 0;
					} else if (parameters[1] > (currentPlot.getSizeY() - 1)) {
						cursorY = currentPlot.getSizeY() - 1;
					} else {
						cursorY = parameters[1];
					}
					cursorCount = currentPlot.getCount(cursorX, cursorY);
					currentPlot.markChannel(cursorX, cursorY);
					textOut.messageOutln(
						"Channel "
							+ cursorX
							+ ","
							+ cursorY
							+ ":  Counts = "
							+ cursorCount);
					done();
				}
			}
		}
	}

	/**
	 * update paint the current histogram
	 *
	 */
	void update() {
		currentPlot.update();
		done();
	}

	/**
	 * Expand the region to view.
	 */
	private void expand() {
		if (!commandPresent) {
			init();
			textOut.messageOut("Expand from channel ", MessageHandler.NEW);
		} else if (numberPoints == 0) {
			limX1 = cursorX;
			limY1 = cursorY;
			numberPoints = 1;
			if (currentPlot instanceof Plot1d) {
				textOut.messageOut(limX1 + " to ");
			} else {
				textOut.messageOut(limX1 + "," + limY1 + " to ");
			}
		} else {
			limX2 = cursorX;
			limY2 = cursorY;
			if (currentPlot instanceof Plot1d) {
				textOut.messageOut("" + limX2, MessageHandler.END);
			} else {
				textOut.messageOut(limX2 + "," + limY2, MessageHandler.END);
			}
			currentPlot.expand(limX1, limX2, limY1, limY2);
			if (autoOnExpand) {
				currentPlot.autoCounts();
			}
			done();
		}
	}

	/**
	 * @param hist the first element of which is the number 
	 * of the hist to display
	 */
	private void display(int[] hist) {
		if (!commandPresent) {
			init();
			textOut.messageOut(
				"Display histogram number: ",
				MessageHandler.NEW);
		}
		if (hist.length > 0) {
			final int num = hist[0];
			final Histogram h=Histogram.getHistogram(num);
			if (h != null) {
				display.displayHistogram(Histogram.getHistogram(num));
				textOut.messageOut(Integer.toString(num),MessageHandler.END);
			} else {
				textOut.messageOut(Integer.toString(num),MessageHandler.END);
				textOut.errorOutln("There is no histogram numbered "+num+".");
			}
			done();
		}
	}

	/**
	 * Set the range for the counts scale.
	 */
	private void range() {
		String text;

		if (!commandPresent) {
			init();
			textOut.messageOut("Range from ", MessageHandler.NEW);
		} else if (numberPoints == 0) {
			if (currentPlot instanceof Plot1d) {
				countLow = cursorY;
			} else {
				countLow = cursorCount;
			}
			numberPoints = 1;
			text = "" + countLow + " to ";
			textOut.messageOut(text);
		} else {
			if (currentPlot instanceof Plot1d) {
				countHigh = cursorY;
			} else {
				countHigh = cursorCount;
			}
			currentPlot.setRange((int) countLow, (int) countHigh);
			text = "" + countHigh;
			//need a tempory variable text here I dont know why.
			textOut.messageOut(text, MessageHandler.END);
			done();
		}
	}
	/**
	 * Calculate the area and centroid for a region
	 * maybe should copy inquire methods to this class
	 */

	private void areaCent() {
		double area, centroid, fwhm;
		String name;

		if (!commandPresent) {
			init();
			name = currentPlot.getHistogram().getName();
			textOut.messageOut(
				"Area for " + name + " from channel ",
				MessageHandler.NEW);
		} else if (numberPoints == 0) {
			limX1 = cursorX;
			limY1 = cursorY;
			numberPoints = 1;
			if (currentPlot instanceof Plot1d) {
				currentPlot.markChannel(limX1, limY1);
				textOut.messageOut(limX1 + " to ");

			} else {
				currentPlot.markChannel(limX1, limY1);
				textOut.messageOut(limX1 + "," + limY1 + " to ");
			}
		} else {
			limX2 = cursorX;
			limY2 = cursorY;
			if (currentPlot instanceof Plot1d) {
				textOut.messageOut("" + limX2);
				area =
					inquire.getArea(
						((Plot1d) currentPlot).getCounts(),
						limX1,
						limX2);
				centroid =
					inquire.getCentroid(
						((Plot1d) currentPlot).getCounts(),
						limX1,
						limY1,
						limX2,
						limY2);
				fwhm =
					inquire.getFWHM(
						((Plot1d) currentPlot).getCounts(),
						limX1,
						limY1,
						limX2,
						limY2);
				currentPlot.markChannel(limX2, limY2);
				currentPlot.markArea(limX1, limX2, limY1, limY2);
				textOut.messageOut(
					":  Area = "
						+ numFormat.format(area)
						+ ", Centroid = "
						+ numFormat.format(centroid)
						+ ", FWHM = "
						+ numFormat.format(fwhm),
					MessageHandler.END);

			} else {
				textOut.messageOut("" + limX2 + "," + limY2);
				area =
					inquire.getArea(
						((Plot2d) currentPlot).getCounts(),
						limX1,
						limY1,
						limX2,
						limY2);
				currentPlot.markChannel(limX2, limY2);
				currentPlot.markArea(limX1, limX2, limY1, limY2);
				textOut.messageOut(
					":  Area = " + numFormat.format(area),
					MessageHandler.END);
			}
			done();
		}
	}

	/**
	  * Background subtracted intensity of 
	  * 1-d plots
	  *
	  */
	public void netArea() {
		double grossArea;
		String name;
		double[] netArea;
		double[] netAreaError;
		double[] fwhm;
		double[] centroid;
		double[] centroidError;
		double[] channelBackground;
		netArea = new double[1];
		netAreaError = new double[1];
		fwhm = new double[2];
		centroidError = new double[2];
		centroid = new double[1];
		channelBackground = new double[currentPlot.getSizeX()];

		if (!commandPresent) {
			init();
			name = currentPlot.getHistogram().getName().trim();
			textOut.messageOut(
				"Net Area fit for "
					+ name
					+ ": select four background markers, then two region of interest markers. ",
				MessageHandler.NEW);
		} else if (numberPoints == 0) {
			//************ First background Marker ***********************************
			xyCursor[0][0] = cursorX;
			xyCursor[0][1] = cursorY;
			numberPoints = 1;
			if (currentPlot instanceof Plot1d) {
				currentPlot.markChannel(xyCursor[0][0], xyCursor[0][1]);
				textOut.messageOut(
					"Bgd Channel " + xyCursor[0][0] + " to ",
					MessageHandler.CONTINUE);
			} else {
				currentPlot.markChannel(xyCursor[0][0], xyCursor[0][1]);
				textOut.messageOut(
					xyCursor[0][0] + ", " + xyCursor[0][1] + " to ",
					MessageHandler.CONTINUE);
			}
		} else if (numberPoints == 1) {
			//************ Second Background marker **********************************
			xyCursor[1][0] = cursorX;
			xyCursor[1][1] = cursorY;
			if (currentPlot instanceof Plot1d) {
				currentPlot.markChannel(xyCursor[1][0], xyCursor[1][1]);
				currentPlot.markArea(
					xyCursor[0][0],
					xyCursor[1][0],
					xyCursor[0][0],
					xyCursor[1][1]);
				textOut.messageOut(Integer.toString(xyCursor[1][0]));
			} else {
				currentPlot.markChannel(xyCursor[1][0], xyCursor[1][1]);
				textOut.messageOut(
					xyCursor[1][0] + ", " + xyCursor[1][1],
					MessageHandler.CONTINUE);
			}
			numberPoints++;
		} else if (numberPoints == 2) {
			//************ Third Background Marker **********************************
			xyCursor[2][0] = cursorX;
			xyCursor[2][1] = cursorY;
			if (currentPlot instanceof Plot1d) {

				currentPlot.markChannel(xyCursor[2][0], xyCursor[2][1]);
				textOut.messageOut(" and " + xyCursor[2][0] + " to ");

			} else {
				textOut.messageOut(
					xyCursor[2][0] + ", " + xyCursor[2][1] + " to ",
					MessageHandler.CONTINUE);
				currentPlot.markChannel(xyCursor[2][0], xyCursor[2][1]);
			}
			numberPoints++;
		} else if (numberPoints == 3) {
			//************ Fourth Background Marker *********************************
			xyCursor[3][0] = cursorX;
			xyCursor[3][1] = cursorY;
			if (currentPlot instanceof Plot1d) {
				currentPlot.markChannel(xyCursor[3][0], xyCursor[3][1]);
				currentPlot.markArea(
					xyCursor[2][0],
					xyCursor[3][0],
					xyCursor[2][1],
					xyCursor[3][1]);
				textOut.messageOut(
					"" + xyCursor[3][0],
					MessageHandler.CONTINUE);
			} else {

				textOut.messageOut(
					xyCursor[3][0] + ", " + xyCursor[3][1],
					MessageHandler.CONTINUE);
				currentPlot.markChannel(xyCursor[3][0], xyCursor[3][1]);
			}
			numberPoints++;
		} else if (numberPoints == 4) {
			//************ First Region Marker *********************************
			xyCursor[4][0] = cursorX;
			xyCursor[4][1] = cursorY;
			if (currentPlot instanceof Plot1d) {

				currentPlot.markChannel(xyCursor[4][0], xyCursor[4][1]);
				textOut.messageOut(
					". Peak " + xyCursor[4][0] + " to ",
					MessageHandler.CONTINUE);
			} else {

				textOut.messageOut(
					"" + xyCursor[4][0] + "," + xyCursor[4][1] + " to ",
					MessageHandler.CONTINUE);
				currentPlot.markChannel(xyCursor[4][0], xyCursor[4][1]);
			}
			numberPoints++;
		} else if (numberPoints == 5) {
			//************ Second Region Marker *********************************
			xyCursor[5][0] = cursorX;
			xyCursor[5][1] = cursorY;
			if (currentPlot instanceof Plot1d) {
				currentPlot.markChannel(xyCursor[5][0], xyCursor[5][1]);
				currentPlot.markArea(
					xyCursor[4][0],
					xyCursor[5][0],
					xyCursor[4][1],
					xyCursor[5][1]);
				textOut.messageOut(
					xyCursor[5][0] + ". ",
					MessageHandler.CONTINUE);
			} else {
				textOut.messageOut(
					xyCursor[5][0] + ", " + xyCursor[5][1] + ". ",
					MessageHandler.CONTINUE);
				currentPlot.markChannel(xyCursor[5][0], xyCursor[5][1]);
			}
			grossArea =
				inquire.getArea(
					((Plot1d) currentPlot).getCounts(),
					xyCursor[4][0],
					xyCursor[5][0]);
			inquire.getNetArea(
				netArea,
				netAreaError,
				channelBackground,
				fwhm,
				centroid,
				centroidError,
				xyCursor,
				grossArea,
				currentPlot.getSizeX(),
				((Plot1d) currentPlot).getCounts());
			if (currentPlot.isCalibrated) {
				Plot1d plot1d = (Plot1d) currentPlot;
				centroid[0] = plot1d.getEnergy(centroid[0]);
				fwhm[0] = plot1d.getEnergy(fwhm[0]);
				fwhm[1] = plot1d.getEnergy(0.0);
				centroidError[0] = plot1d.getEnergy(centroidError[0]);
				centroidError[1] = plot1d.getEnergy(0.0);
				fwhm[0] = fwhm[0] - fwhm[1];
				centroidError[0] = centroidError[0] - centroidError[1];
			}
			textOut.messageOut(
				"GrossArea = "
					+ grossArea
					+ "\u00b1"
					+ numFormat.format(Math.sqrt(grossArea))
					+ ", "
					+ "NetArea = "
					+ numFormat.format(netArea[0])
					+ "\u00b1"
					+ numFormat.format(netAreaError[0])
					+ ", "
					+ "Centroid = "
					+ numFormat.format(centroid[0])
					+ "\u00b1"
					+ numFormat.format(centroidError[0])
					+ ", "
					+ "FWHM = "
					+ numFormat.format(fwhm[0]),
				MessageHandler.END);
			/* Draw Fit on screen by calling DisplayFit in Display.java */
			int ll = xyCursor[0][0];
			int ul = xyCursor[3][0] + 1;
			double[] bkgd = new double[ul - ll + 1];
			System.arraycopy(channelBackground, ll, bkgd, 0, bkgd.length);
			this.display.displayFit(null, bkgd, null, ll);
			done();
		}

	}
	/**
	 * Zoom in on the histogram
	 */
	public void zoomin() {
		currentPlot.zoom(Plot.ZOOM_IN);
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}

	/**
	 * Zoom out on the histogram
	 *
	 */
	public void zoomout() {
		currentPlot.zoom(Plot.ZOOM_OUT);
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}

	/**
	 * Display the full histogram
	 *
	 */
	public void full() {
		currentPlot.setFull();
		if (autoOnExpand) {
			currentPlot.autoCounts();
		}
		done();
	}

	/**
	 * Set the plot to linear
	 */
	private void linear() {
		currentPlot.setLinear();
		done();
	}

	/**
	 * Set the plot to log
	 */
	private void log() {
		currentPlot.setLog();
		done();
	}

	/**
	 * Auto scale the plot
	 */
	private void auto() {
		currentPlot.autoCounts();
		done();
	}

	/**	
	 * Goto input channel
	 */
	private void gotoChannel() {
		int channelLow, channelHigh;
		double inputEnergyValue;

		inputEnergyValue = 0;
		if (!commandPresent) {
			init();
			if (currentPlot instanceof Plot1d && currentPlot.isCalibrated) {
				textOut.messageOut(
					"Goto (click on spectrum or type the calibrated energy) ",
					MessageHandler.NEW);
			} else {
				textOut.messageOut(
					"Goto (click on spectrum or type the channel) ",
					MessageHandler.NEW);
			}
		} else if (numberPoints == 0) {
			numberPoints = 1;
			xyCursor[0][0] = cursorX;
			xyCursor[0][1] = cursorY;
			inputCursorValue = xyCursor[0][0];
			String output = "";
			if (!currentPlot.isCalibrated) {
				output = "channel = " + xyCursor[0][0];
			} else {
				if (currentPlot instanceof Plot1d) {
					Plot1d plot1d = (Plot1d) currentPlot;
					output =
						"energy = "
							+ plot1d.getEnergy(xyCursor[0][0])
							+ ", channel = "
							+ xyCursor[0][0];
				}
			}
			if (currentPlot instanceof Plot1d) {
				Plot1d plot1d = (Plot1d) currentPlot;
				if (!mousePressed) {
					if (currentPlot.isCalibrated) {
						output = "energy = " + xyCursor[0][0];
						xyCursor[0][0] =
							(int) plot1d.getChannel(xyCursor[0][0]);
						if (xyCursor[0][0] > currentPlot.getSizeX()) {
							xyCursor[0][0] = currentPlot.getSizeX() - 1;
						}
						output += ", channel = " + xyCursor[0][0];
					}
				}
			}
			channelLow = xyCursor[0][0] - 50;
			channelHigh = channelLow + 100;
			currentPlot.expand(channelLow, channelHigh, 0, 0);
			textOut.messageOut(output, MessageHandler.END);
			energyEx = false;
			auto();
			done();
		}
	}

	/**
	 * Initializes at the start of a new command accepted
	 */
	private void init() {
		commandPresent = true;
		numberPoints = 0;
	}

	/**
	 * A command has been completed so clean up
	 */
	private void done() {
		commandPresent = false;
		mousePressed = false;
		inCommand = null;
		numberPoints = 0;
	}

	static private boolean autoOnExpand = true;

	/**
	 * Sets whether expand/zoom also causes an auto-scale.
	 */
	static public void setAutoOnExpand(boolean whether) {
		autoOnExpand = whether;
	}

}
