package jam.plot;
import jam.JamConsole;
import jam.commands.JamCmdManager;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

/**
 * Class the does the actions on plots. Receives commands from buttons
 *  and command line. Performs action by performing command on plot, 
 * plot1d and plot2d.
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

class Action implements ActionListener, PlotMouseListener {

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
	static final String SCALE ="scale";

	private boolean autoOnExpand = true;

	/**
	 *  Variable to indicate mouse was pressed
	 */
	private boolean mousePressed;

	/**
	 * Accessed by Display.
	 */
	private boolean settingGate;

	private final MessageHandler textOut;
	private final Display display;
	private Broadcaster broadcaster;
	private Plot currentPlot;
	private final PlotFit inquire;
	private final NumberFormat numFormat;

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
	private Point cursor = new Point();
	private Point pixel = new Point();
	private double cursorCount;
	private final List clicks = new ArrayList();
	private final Map commandMap;
	private double countLow, countHigh;

	/**
	 * Master constructor has no broadcaster.
	 * 
	 * @param d the histogram displayer
	 * @param mh the message area of the Jam window
	 */
	Action(Display d, MessageHandler mh) {
		this.display = d;
		this.textOut = mh;
		broadcaster=Broadcaster.getSingletonInstance();
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
	}

	/**
	 * Set the broadcaster.
	 *
	 * @param b the one to add
	 */
	synchronized void setBroadcaster(Broadcaster b) {
		broadcaster = b;
	}

	/**
	 * Set the current plot, i.e., the one we will do actions on.
	 * Reset the current state.
	 *
	 * @param mp the current plot
	 */
	synchronized void setPlot(Plot mp) {
		currentPlot = mp;
		settingGate = false;
		overlayState = false;
	}

	/**
	 * Routine called by pressing a button on the action toolbar.
	 *
	 * @param e the event created by the button press
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
		final String[] commands =
			{
				HELP,
				EXPAND,
				ZOOMIN,
				ZOOMOUT,
				FULL,
				LINEAR,
				LOG,
				AREA,
				GOTO,
				NETAREA,
				UPDATE,
				AUTO,
				OVERLAY,
				CANCEL,
				EXPAND,
				EXPAND,
				RANGE,
				DISPLAY,
				REBIN, SCALE };
		final String[] abbr =
			{
				"help",
				"ex",
				"zi",
				"zo",
				"f",
				"li",
				"lo",
				"ar",
				"g",
				"n",
				"u",
				"a",
				"o",
				"c",
				"x",
				"y",
				"ra",
				"d","re","s" };
		if (commands.length != abbr.length) {
			JOptionPane.showMessageDialog(
				display,
				"Make sure commands and abbreviations arrays are equal in length.",
				"Error in code.",
				JOptionPane.ERROR_MESSAGE);
		}
		final Map rval = new HashMap();
		for (int i = 0; i < commands.length; i++) {
			rval.put(abbr[i], commands[i]);
		}
		return rval;
	}

	/**
	 * Do a command sent in as a message. Sees if the string is 
	 * command that plot can understand and sets a command string to
	 * something that can be interpreted by doCommand(), i.e., expand
	 * abbreviations.
	 * 
	 * @param _command entry from console
	 * @param parameters integer parameters from console
	 */
	boolean commandPerform(String _command, String [] cmdParams) {
		boolean accept = false; //is the command accepted
		boolean handleIt=false;
		final String command = _command.toLowerCase();
		/* int is a special case meaning
		 * no command and just parameters */
		if (command.equals(JamConsole.NUMBERS_ONLY)) {
			final double [] parameters=convertParameters(cmdParams);
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
			accept=true;
		} else if (commandMap.containsKey(command)) {
			inCommand = (String) commandMap.get(command);
			accept = true;
			handleIt=true;
		}
		if (accept && handleIt) {
			final double [] parameters=convertParameters(cmdParams);
			if (DISPLAY.equals(inCommand)){
				display(parameters);
			} else if (OVERLAY.equals(inCommand)){
				overlay(parameters);
			} else {
				doCommand();
				integerChannel(parameters);
			}
		} 
		return accept;
	}
	
	/**
	 * Convert the parameters to doubles
	 * @param parameters
	 * @return
	 */
	private double [] convertParameters(String [] cmdParams)  {
		final int numberParams = cmdParams.length;
		double [] parameters = new double [numberParams];
		//rest of tokens must be numbers
		try {
			int countParam=0;
			while (countParam<numberParams) {
				parameters[countParam] = convertNumber(cmdParams[countParam]);
				countParam++;
			}
		} catch (NumberFormatException nfe) {
			throw new NumberFormatException("Input parameter not a number");
		}
		return parameters; 
		
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
		if (CANCEL.equals(inCommand)) {
			textOut.messageOutln("");
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
		} else if (SCALE.equals(inCommand)){
			if (currentPlot.getLimits().getScale()==Limits.ScaleType.LINEAR){
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
		} else if (REBIN.equals(inCommand)){
			 rebin();
		}else {
			done();
			textOut.errorOutln(
				getClass().getName()
					+ ".doCommand() '"
					+ inCommand
					+ "' not recognized.");
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
		cursor = pChannel;
		pixel =pPixel;
		cursorCount = currentPlot.getCount(pChannel);
		/* there is a command currently being processed */
		if (commandPresent) {
			doCommand();
			/* no command being processed 
			 * check if gate is being set */
		} else {
			if (settingGate) {
				broadcaster.broadcast(BroadcastEvent.GATE_SET_POINT,pChannel);
				currentPlot.displaySetGate(GateSetMode.GATE_CONTINUE,
				pChannel,pPixel);
			} else {
				/* output counts for the channel */
				currentPlot.markChannel(cursor);
				if (currentPlot instanceof Plot1d) {
					if (currentPlot.isCalibrated) {
						final Plot1d plot1d = (Plot1d) currentPlot;
						final double energy = plot1d.getEnergy(cursor.x);
						textOut.messageOutln(
							"Channel "
								+ cursor.x
								+ ":  Counts = "
								+ numFormat.format(cursorCount)
								+ "  Energy = "
								+ numFormat.format(energy));
					} else {
						textOut.messageOutln(
							"Channel "
								+ cursor.x
								+ ":  Counts = "
								+ numFormat.format(cursorCount));
					}
				} else {
					textOut.messageOutln(
						"Channel "
							+ getCoordString(cursor)
							+ ":  Counts = "
							+ numFormat.format(cursorCount));
				}
				done();
			}
		}
	}

	/**
	 * Accepts integer input and does a command if one
	 * is present.
	 *
	 * @param parameters the integers
	 */
	private void integerChannel(double [] parameters) {
		final int numPar = parameters.length;
		/* FIXME we should be better organized so this if is not here
		 * so range is not a special case */
		if ((commandPresent)) {
			if (RANGE.equals(inCommand)) {
				for (int i = 0;(i < numPar) && (i < 2); i++) {
					cursor.y = (int)parameters[i];
					cursorCount = parameters[i];
					doCommand();
				}
				return;
			} else if (REBIN.equals(inCommand)){
				if (numPar > 0){
					parameter=parameters;
				}
			}
		}
		/* we have a 1 d plot */
		if (currentPlot instanceof Plot1d) {
			if (commandPresent) {
				final int loopMax = Math.min(numPar, 2);
				for (int i = 0; i < loopMax; i++) {
					//check for out of bounds
					setCursor(closestInsidePoint(new Point((int)parameters[i], 0)));
					if (!energyEx) {
						cursorCount = currentPlot.getCount(cursor);
					}
					doCommand();
				}
			} else { //no command so get channel
				if (numPar > 0) {
					/* check for out of bounds */
					setCursor(closestInsidePoint(new Point((int)parameters[0], 0)));
					synchronized (this) {
						cursorCount = currentPlot.getCount(cursor);
					}
					currentPlot.markChannel(cursor);
					textOut.messageOutln(
						"Channel " + cursor.x + ":  Counts = " + cursorCount);
					done();
				}
			}
		} else { //we have a 2 d plot
			if (commandPresent) {
				final int loopMax = Math.min(numPar, 4);
				synchronized (this) {
					for (int i = 1; i < loopMax; i += 2) {
						/* check for out of bounds */
						cursor =
							closestInsidePoint(
								new Point((int)parameters[i - 1], (int)parameters[i]));
						cursorCount = currentPlot.getCount(cursor);
						doCommand();
					}
				}
			} else { //no command so get channel
				if (numPar > 1) {
					cursor =
						closestInsidePoint(
							new Point((int)parameters[0], (int)parameters[1]));
					cursorCount = currentPlot.getCount(cursor);
					currentPlot.markChannel(cursor);
					textOut.messageOutln(
						"Channel "
							+ getCoordString(cursor)
							+ ":  Counts = "
							+ cursorCount);
					done();
				}
			}
		}
	}

	/**
	 * Call <code>update()</code> on the current plot, reset the 
	 * command-line, and broadcast a histogram selection message to 
	 * force the rest of the GUI to be consistent with the currently 
	 * selected histogram.
	 * 
	 * @see Plot#update()
	 */
	private void update() {
		currentPlot.update();
		done();
		/* following to recover the chooser if user just overlayed
		 * a histogram
		 */
		broadcaster.broadcast(BroadcastEvent.HISTOGRAM_SELECT);
	}

	/**
	 * Expand the region to view.
	 */
	private void expand() {
		if (!commandPresent) {
			init();
			textOut.messageOut("Expand from channel ", MessageHandler.NEW);
		} else if (clicks.size() == 0) {
			//currentPlot.setSelectingArea(true);
			currentPlot.initializeSelectingArea(cursor);
			addClick(cursor);
			textOut.messageOut(getCoordString(cursor) + " to ");
		} else {
			currentPlot.setSelectingArea(false);
			textOut.messageOut(getCoordString(cursor), MessageHandler.END);
			currentPlot.expand(getClick(0), cursor);
			if (autoOnExpand) {
				currentPlot.autoCounts();
			}
			done();
		}
	}

	private Point closestInsidePoint(Point p) {
		final Point rval = new Point(p);
		if (rval.x < 0) {
			rval.x = 0;
		} else if (rval.x >= currentPlot.getSizeX()) {
			rval.x = currentPlot.getSizeX() - 1;
		}
		if (rval.y < 0) {
			rval.y = 0;
		} else if (rval.y >= currentPlot.getSizeY()) {
			cursor.y = currentPlot.getSizeY() - 1;
		}
		return rval;
	}

	/**
	 * @param hist the first element of which is the number 
	 * of the hist to display
	 */
	private void display(double [] hist) {
		if (!commandPresent) {
			init();
			textOut.messageOut(
				"Display histogram number: ",
				MessageHandler.NEW);
		}
		if (hist.length > 0) {
			final int num = (int)hist[0];
			final Histogram h = Histogram.getHistogram(num);
			if (h != null) {
				JamStatus.instance().setCurrentHistogramName(h.getName());
				textOut.messageOut(Integer.toString(num)+" ", MessageHandler.END);
				if (hist.length < 2){
					display.removeOverlays();
				}
				broadcaster.broadcast(BroadcastEvent.HISTOGRAM_SELECT);
			} else {
				textOut.messageOut(Integer.toString(num), MessageHandler.END);
				textOut.errorOutln(
					"There is no histogram numbered " + num + ".");
			}
			if (hist.length > 1){
				if (h.getDimensionality() != 1){
					textOut.errorOutln(h.getName().trim()+
					" is not 1D, so you may not overlay other histograms.");
				} else {
					final int newlen=hist.length-1;
					final double [] pass=new double[newlen];
					System.arraycopy(hist,1,pass,0,newlen);
					overlay(pass);
				}
			} else {
				done();
			}
		}
	}
	
	private void overlay(double [] hist) {
		if (!commandPresent) {
			init();
			textOut.messageOut(
				"Overlay histogram numbers: ",
				MessageHandler.NEW);
		}
		boolean areHists=hist.length>0;
		for (int i=0; i<hist.length;i++){
			final int num = (int)hist[i];
			final Histogram h = Histogram.getHistogram(num);
			if (h != null) {
				if (h.getDimensionality() != 1){
					textOut.errorOutln(h.getName().trim()+
					" is not 1D, so it cannot be overlaid.");
				} else {
					display.addToOverlay(h);
					textOut.messageOut(Integer.toString(num)+' ', MessageHandler.CONTINUE);
				}
			} else {
				textOut.errorOutln(
					"There is no histogram numbered " + num + ".");
			}
		}
		if (areHists){
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
		} else if (clicks.size() == 0) {
			countLow = getCursorCounts();
			addClick(cursor);
			textOut.messageOut(String.valueOf(countLow) + " to ");
		} else {
			countHigh = getCursorCounts();
			currentPlot.setRange((int) countLow, (int) countHigh);
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
			final double binWidth = parameter[0];
			if (binWidth >= 1.0  && binWidth < currentPlot.getHistogram().getSizeX()){
				currentPlot.setBinWidth(binWidth);
				textOut.messageOut(String.valueOf(binWidth), MessageHandler.END);
				currentPlot.repaint();
				done();
			} else {
				textOut.messageOut(String.valueOf(binWidth), MessageHandler.END);
				textOut.warningOutln("Rebin command ignored. Bin value must be \u2264 1.0 and smaller than the histogram.");
				done();
			}
		}
	}


	/**
	 * @return the counts in the last channel clicked
	 */
	private double getCursorCounts() {
		final double rval;
		if (currentPlot instanceof Plot1d) {
			rval = cursor.y;
		} else {
			rval = cursorCount;
		}
		return rval;
	}
	
	private double [] parameter=new double[0];

	/**
	 * Calculate the area and centroid for a region
	 * maybe should copy inquire methods to this class
	 */
	private void areaCent() {
		if (!commandPresent) {
			init();
			final String name = currentPlot.getHistogram().getName();
			textOut.messageOut(
				"Area for " + name + " from channel ",
				MessageHandler.NEW);
		} else if (clicks.size() == 0) {
			addClick(cursor);
			//currentPlot.setSelectingArea(true);
			currentPlot.initializeSelectingArea(cursor);
			currentPlot.markChannel(cursor);
			textOut.messageOut(getCoordString(cursor) + " to ");
		} else {
			currentPlot.setSelectingArea(false);
			final Point lim1 = getClick(0);
			if (currentPlot instanceof Plot1d) {
				textOut.messageOut(String.valueOf(cursor.x));
				final double area =
					inquire.getArea(
						((Plot1d) currentPlot).getCounts(),
						lim1,
						cursor);
				final double centroid =
					inquire.getCentroid(
						((Plot1d) currentPlot).getCounts(),
						lim1,
						cursor);
				final double fwhm =
					inquire.getFWHM(
						((Plot1d) currentPlot).getCounts(),
						lim1,
						cursor);
				currentPlot.markChannel(cursor);
				currentPlot.markArea(lim1, cursor);
				textOut.messageOut(
					":  Area = "
						+ numFormat.format(area)
						+ ", Centroid = "
						+ numFormat.format(centroid)
						+ ", FWHM = "
						+ numFormat.format(fwhm),
					MessageHandler.END);

			} else {//2D histogram
				textOut.messageOut(getCoordString(cursor));
				final double area =
					inquire.getArea(
						((Plot2d) currentPlot).getCounts(),
						lim1,
						cursor);
				currentPlot.markChannel(cursor);
				currentPlot.markArea(lim1, cursor);
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
	  */
	private void netArea() {
		final double[] netArea = new double[1];
		final double[] netAreaError = new double[1];
		final double[] fwhm = new double[2];
		final double[] centroidError = new double[2];
		final double[] centroid = new double[1];
		final double[] channelBackground = new double[currentPlot.getSizeX()];
		final int nclicks = clicks.size();
		if (!commandPresent) {
			init();
			final String name = currentPlot.getHistogram().getName().trim();
			textOut.messageOut(
				"Net Area fit for "
					+ name
					+ ": select four background markers, then two region-of-interest markers. ",
				MessageHandler.NEW);
		} else if (nclicks == 0) {
			//************ First background Marker ***********************************
			addClick(cursor);
			final Point p = cursor;
			if (currentPlot instanceof Plot1d) {
				currentPlot.markChannel(p);
				textOut.messageOut(
					"Bgd Channel " + p.x + " to ",
					MessageHandler.CONTINUE);
			} else {
				currentPlot.markChannel(p);
				textOut.messageOut(
					getCoordString(p) + " to ",
					MessageHandler.CONTINUE);
			}
		} else if (nclicks == 1) {
			//************ Second Background marker **********************************
			addClick(cursor);
			final Point p1 = getClick(0);
			final Point p2 = cursor;
			currentPlot.markChannel(p2);
			if (currentPlot instanceof Plot1d) {
				currentPlot.markArea(p1, p2);
				textOut.messageOut(Integer.toString(p2.x));
			} else {
				textOut.messageOut(getCoordString(p2), MessageHandler.CONTINUE);
			}
		} else if (nclicks == 2) {
			//************ Third Background Marker **********************************
			addClick(cursor);
			final Point p = cursor;
			currentPlot.markChannel(p);
			if (currentPlot instanceof Plot1d) {
				textOut.messageOut(" and " + p.x + " to ");

			} else {
				textOut.messageOut(
					getCoordString(p) + " to ",
					MessageHandler.CONTINUE);
			}
		} else if (nclicks == 3) {
			//************ Fourth Background Marker *********************************
			addClick(cursor);
			final Point p1 = getClick(2);
			final Point p2 = cursor;
			currentPlot.markChannel(p2);
			if (currentPlot instanceof Plot1d) {
				currentPlot.markArea(p1, p2);
				textOut.messageOut(
					String.valueOf(p2.x),
					MessageHandler.CONTINUE);
			} else {
				textOut.messageOut(getCoordString(p2), MessageHandler.CONTINUE);
			}
		} else if (nclicks == 4) {
			//************ First Region Marker *********************************
			//currentPlot.setSelectingArea(true);
			currentPlot.initializeSelectingArea(cursor);
			addClick(cursor);
			final Point p = cursor;
			currentPlot.markChannel(p);
			if (currentPlot instanceof Plot1d) {
				textOut.messageOut(
					". Peak " + p.x + " to ",
					MessageHandler.CONTINUE);
			} else {

				textOut.messageOut(
					getCoordString(p) + " to ",
					MessageHandler.CONTINUE);
			}
		} else if (nclicks == 5) {
			//************ Second Region Marker *********************************
			currentPlot.setSelectingArea(false);
			addClick(cursor);
			final Point p1 = getClick(4);
			final Point p2 = cursor;
			currentPlot.markChannel(p2);
			if (currentPlot instanceof Plot1d) {
				currentPlot.markArea(p1, p2);
				textOut.messageOut(p2.x + ". ", MessageHandler.CONTINUE);
			} else {
				textOut.messageOut(
					getCoordString(p2) + ". ",
					MessageHandler.CONTINUE);
			}
			final double grossArea =
				inquire.getArea(((Plot1d) currentPlot).getCounts(), p1, p2);
			Point[] passClicks = new Point[clicks.size()];
			clicks.toArray(passClicks);
			/* results of next call are passed back in the parameters */
			inquire.getNetArea(
				netArea,
				netAreaError,
				channelBackground,
				fwhm,
				centroid,
				centroidError,
				passClicks,
				grossArea,
				currentPlot.getSizeX(),
				((Plot1d) currentPlot).getCounts());
			if (currentPlot.isCalibrated) {
				final Plot1d plot1d = (Plot1d) currentPlot;
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
			final int ll = getClick(0).x;
			final int ul = getClick(3).x + 1;
			final double[] bkgd = new double[ul - ll + 1];
			System.arraycopy(channelBackground, ll, bkgd, 0, bkgd.length);
			this.display.displayFit(null, bkgd, null, ll);
			done();
		}
	}

	private Point getClick(int i) {
		return (Point) clicks.get(i);
	}

	/**
	 * Zoom in on the histogram
	 */
	private void zoomin() {
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
		currentPlot.setLinear();
		done();
	}

	/**
	 * Set the counts to log scale.
	 */
	private void log() {
		currentPlot.setLog();
		done();
	}

	/**
	 * Auto scale the plot.
	 */
	private void auto() {
		currentPlot.autoCounts();
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
		final int nclicks = clicks.size();
		if (!commandPresent) {
			init();
			if (currentPlot instanceof Plot1d && currentPlot.isCalibrated) {
				final String mess =
					new StringBuffer(intro)
						.append(cal)
						.append(sp)
						.append(en)
						.append(lp)
						.append(sp)
						.toString();
				textOut.messageOut(mess, MessageHandler.NEW);
			} else {
				final String mess =
					new StringBuffer(intro)
						.append(ch)
						.append(lp)
						.append(sp)
						.toString();
				textOut.messageOut(mess, MessageHandler.NEW);
			}
		} else if (nclicks == 0) {
			addClick(cursor);
			StringBuffer output = new StringBuffer();
			if (!currentPlot.isCalibrated) {
				output.append(ch).append(eq).append(cursor.x);
			} else {
				if (currentPlot instanceof Plot1d) {
					final Plot1d plot1d = (Plot1d) currentPlot;
					output
						.append(en)
						.append(eq)
						.append(plot1d.getEnergy(cursor.x))
						.append(sep)
						.append(ch)
						.append(eq)
						.append(cursor.x);
				}
			}
			if (currentPlot instanceof Plot1d) {
				final Plot1d plot1d = (Plot1d) currentPlot;
				if (!mousePressed) {
					if (currentPlot.isCalibrated) {
						output =
							new StringBuffer(en).append(eq).append(cursor.x);
						synchronized (this) {
							cursor.x = plot1d.getChannel(cursor.x);
							if (cursor.x > currentPlot.getSizeX()) {
								cursor.x = currentPlot.getSizeX() - 1;
							}
						}
						output.append(sep).append(ch).append(eq).append(
							cursor.x);
					}
				}
			}
			final int rangeToUse = 100;
			final int halfRange = rangeToUse / 2;
			final int channelLow = cursor.x - halfRange;
			final int channelHigh = channelLow + rangeToUse;
			currentPlot.expand(
				new Point(channelLow, 0),
				new Point(channelHigh, 0));
			textOut.messageOut(output.toString(), MessageHandler.END);
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
			currentPlot.setSelectingArea(false);
			clicks.clear();
		}
	}

	/**
	 * Sets whether expand/zoom also causes an auto-scale.
	 *
	 * @param whether <code>true</code> if auto-scale on expand or zoom
	 * is desired
	 */
	synchronized void setAutoOnExpand(boolean whether) {
		autoOnExpand = whether;
	}

	synchronized void setDefiningGate(boolean whether) {
		settingGate = whether;
	}

	synchronized void setMousePressed(boolean whether) {
		mousePressed = whether;
	}

	private String getCoordString(Point p) {
		final StringBuffer rval = new StringBuffer().append(p.x);
		if (currentPlot instanceof Plot2d) {
			rval.append(',').append(p.y);
		}
		return rval.toString();
	}

	private synchronized void addClick(Point p) {
		clicks.add(p);
	}

	private synchronized void setCursor(Point p) {
		cursor = p;
	}
	/**
	 * Parse a string go a number
	 * @param s
	 * @return
	 * @throws NumberFormatException
	 */
	private double convertNumber(String s) throws NumberFormatException {
		return (s.indexOf('.')>=0) ? Double.parseDouble(s) : 
		Integer.parseInt(s);
	}
	
	private void help() {
		final StringBuffer sb=new StringBuffer("Commands:\t");
		sb.append("li - Linear Scale\tlo - Log Scale\ta  - Auto Scale\tra - Range\t");		
		sb.append("ex - Expand\tf  - Full view\t zi - Zoom In\tzo - Zoom Out\t");
		sb.append("d  - Display\to  - Overlay\tu  - Update\tg  - GoTo\t");
		sb.append("ar - Area\tn  - Net Area\tre - Rebin\tc  - Channel\t");
		final String [] commands=JamCmdManager.getInstance().getAllCommands();	
		for (int i=0; i< commands.length; i++){
			sb.append(commands[i]).append("\t");
		}
		textOut.messageOutln(sb.toString());
	}
	
}
