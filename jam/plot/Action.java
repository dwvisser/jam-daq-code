/*
 */
package jam.plot;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import jam.global.*;
import jam.data.*;
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

	private MessageHandler textOut;
	private Display display;
	private Broadcaster broadcaster;

	private Plot currentPlot;
	PlotFit inquire;

	private NumberFormat numFormat;

	//current state
	private String inCommand;
	private String lastCommand;
	private boolean commandPresent;
	private boolean overlayState;
	boolean settingGate;
	// Variable to indicate mouse was pressed
	static boolean mousePressed;
	static boolean energyEx;

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

		inCommand = null;
		commandPresent = false;
		numberPoints = 0;
		overlayState = false;
		settingGate = false;
		inquire = new PlotFit(); //class with area/centroid routines

		//formating enery output
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
		//cancel command if command has changed
		if (e.getActionCommand() != lastCommand) {
			done();
		}
		inCommand = e.getActionCommand();
		doCommand();
	}

	/**
	 * Do a command sent in as a message
	 *
	 * sees if the string is command that plot can understand
	 * and sets command something to the string that display can
	 * interpret expand abbreviations
	 * translate command to local commands
	 */
	public void commandPerform(String command, int[] parameters) {
		int len; //length on command
		boolean accept = false; //is the command accepted

		command = command.toLowerCase();
		len = command.length();
		//int is special case no command just parameters
		if (len >= 3) {
			if (command.substring(0, 3).equals("int")) {
				integerChannel(parameters);
				accept = true;
				return;
			}
		}
		//All your usual commands
		//a command and array of integers was input
		if (len == 2) {
			if (command.substring(0, 2).equals("ex")) {
				inCommand = "expand";
				accept = true;
			} else if (command.substring(0, 2).equals("zi")) {
				inCommand = "zoomin";
				accept = true;
			} else if (command.substring(0, 2).equals("zo")) {
				inCommand = "zoomout";
				accept = true;
			} else if (command.substring(0, 2).equals("fu")) {
				inCommand = "full";
				accept = true;
			} else if (command.substring(0, 2).equals("li")) {
				inCommand = "linear";
				accept = true;
			} else if (command.substring(0, 2).equals("lo")) {
				inCommand = "log";
				accept = true;
			} else if (command.substring(0, 2).equals("ar")) {
				inCommand = "area";
				accept = true;
			} else if (command.substring(0, 2).equals("go")) {
				inCommand = "goto"; // new goto button
				accept = true;
			} else if (command.substring(0, 2).equals("ne")) {
				inCommand = "netarea"; // new net area button
				accept = true;
			}
		} else if (len == 1) {
			if (command.substring(0, 1).equals("u")) {
				inCommand = "update";
				accept = true;
			} else if (command.substring(0, 1).equals("a")) {
				inCommand = "auto";
				accept = true;
			} else if (command.substring(0, 1).equals("o")) {
				inCommand = "overlay";
				accept = true;
			} else if (command.substring(0, 1).equals("c")) {
				inCommand = "cancel";
				accept = true;
			} else if (command.substring(0, 1).equals("x")) {
				inCommand = "expand";
				accept = true;

			} else if (command.substring(0, 1).equals("y")) {
				inCommand = "expand";
				accept = true;

			} else if (command.substring(0, 1).equals("r")) {
				inCommand = "range";
				accept = true;

			} else if (command.equals("d")) {
				inCommand = "disp";
				//FIXME does not work yet
				accept = true;
			}
		}
		if (accept) {
			doCommand();
			integerChannel(parameters);
			//FIXME
		} else {
			textOut.errorOutln("Command not understood [Action]");
		}

	}

	/**
	 * Sort the input command and do command.
	 */
	private synchronized void doCommand() {
		lastCommand = inCommand;

		//check that a histogram is defined
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
			System.err.println(
				"Error: Unrecongized command " + inCommand + " [Action]");
		}

	}
	/**
	 * Routine called back by mouse a mouse clicks on plot
	 *
	 * @param pChannel  channel of mouse click
	 */
	public synchronized void plotMousePressed(Point pChannel, Point pPixel) {

		double energy; //for calibrated spectrum

		//check that a histogram is defined
		if (currentPlot.currentHist == null) {
			return;
		}
		//cursor position and counts for that channel
		cursorX = pChannel.x;
		cursorY = pChannel.y;
		cursorCount = currentPlot.getCount(cursorX, cursorY);

		//there is a command currently been processed
		if (commandPresent) {
			doCommand();
			//no command being processed check if gate is being
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
				//output counts for the channel
				currentPlot.markChannel(cursorX, cursorY);
				if (currentPlot instanceof Plot1d) {
					if (currentPlot.isCalibrated) {
						energy = display.calibrationFunction.getValue(cursorX);
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

		int numPar;
		numPar = parameters.length;

		//FIXME we should be better organized so this if is not here
		//so range is not a special case
		if ((commandPresent) && (inCommand == "range")) {
			for (int i = 0;(i < numPar) && (i < 2); i++) {
				cursorY = parameters[i];
				cursorCount = parameters[i];
				doCommand();
			}
			return;
		}

		//we have a 1 d plot
		if (currentPlot instanceof Plot1d) {
			//command present
			if (commandPresent) {
				for (int i = 0;(i < numPar) && (i < 2); i++) {
					//check for out of bounds
					if (parameters[i] < 0) {
						cursorX = 0;
					}
					if (energyEx == true && currentPlot.isCalibrated) {
						cursorX = parameters[i];
					} else if (parameters[i] > (currentPlot.getSizeX() - 1)) {
						cursorX = currentPlot.getSizeX() - 1;
					} else {
						cursorX = parameters[i];
					}
					cursorY = 0;
					if (energyEx != true) {
						cursorCount = currentPlot.getCount(cursorX, cursorY);
					}
					doCommand();
				}

				//no command so get channel
			} else {
				if (numPar > 0) {
					//check for out of bounds
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

			//we have a 2 d plot
		} else {
			//commandPresent
			if (commandPresent) {
				for (int i = 1;(i < numPar) && (i < 4); i = i + 2) {
					//check for out of bounds
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

				//no command so get channel
			} else {
				if (numPar > 1) {
					//check for out of bounds
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
		if (commandPresent == false) {
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
	 * Set the range for the counts scale
	 *
	 */
	private void range() {

		String text;
		if (commandPresent == false) {
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

		if (commandPresent == false) {
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

    public void netArea(){
    double grossArea;
    String name;
	double [] netArea;
	double [] netAreaError;
	double [] fwhm;
	double [] centroid;
	double [] centroidError;
	double [] channelBackground;
	netArea = new double[1];
	netAreaError = new double[1];
	fwhm = new double[1];
	centroidError = new double[1];
	centroid = new double[1];
    channelBackground = new double[currentPlot.getSizeX()];

        if (commandPresent==false){
            init();
            name=currentPlot.getHistogram().getName();
            textOut.messageOut("Net Area fit for "+name+"select four background and two region of interest markers\n", MessageHandler.NEW);
        } else if(numberPoints==0){
	  //************ First background Marker ***********************************
            xyCursor[0][0]=cursorX;
            xyCursor[0][1]=cursorY;
            numberPoints=1;
            if (currentPlot instanceof Plot1d){
	    currentPlot.markChannel(xyCursor[0][0],xyCursor[0][1]);
	    textOut.messageOut("Bgd Channel "+xyCursor[0][0]+" to ");
            } else {
		currentPlot.markChannel(xyCursor[0][0],xyCursor[0][1]);
                textOut.messageOut(xyCursor[0][0]+","+xyCursor[0][1]+" to ");
            }
        } else if (numberPoints == 1){
	  //************ Second Background marker **********************************
            xyCursor[1][0]=cursorX;
            xyCursor[1][1]=cursorY;
            if (currentPlot instanceof Plot1d){
                currentPlot.markChannel(xyCursor[1][0],xyCursor[1][1]);
	       	currentPlot.markArea(xyCursor[0][0], xyCursor[1][0], xyCursor[0][0], xyCursor[1][1]);
	       	textOut.messageOut(""+xyCursor[1][0]);
            }else {                        
                currentPlot.markChannel(xyCursor[1][0], xyCursor[1][1]);
		textOut.messageOut(xyCursor[1][0] + "," + xyCursor[1][1],MessageHandler.END );
	    } 
	    numberPoints++; 
	} else if (numberPoints == 2){
	  //************ Third Background Marker **********************************
            xyCursor[2][0]=cursorX;
            xyCursor[2][1]=cursorY;
            if (currentPlot instanceof Plot1d){
               
		currentPlot.markChannel(xyCursor[2][0], xyCursor[2][1]);
		textOut.messageOut("  and  "+xyCursor[2][0]+" to ");

            } else {
                textOut.messageOut(""+xyCursor[2][0]+","+xyCursor[2][1] +" to ",MessageHandler.END);             
		currentPlot.markChannel(xyCursor[2][0], xyCursor[2][1]);        
	    }
	    numberPoints++;
	} else if (numberPoints == 3){
	  //************ Fourth Background Marker *********************************
            xyCursor[3][0]=cursorX;
            xyCursor[3][1]=cursorY;
            if (currentPlot instanceof Plot1d){
               
		currentPlot.markChannel(xyCursor[3][0], xyCursor[3][1]);
		currentPlot.markArea(xyCursor[2][0], xyCursor[3][0],
				     xyCursor[2][1], xyCursor[3][1]);
		textOut.messageOut(""+xyCursor[3][0]);
            } else {

                textOut.messageOut(""+xyCursor[3][0]+","+xyCursor[3][1],MessageHandler.END);               
		currentPlot.markChannel(xyCursor[3][0], xyCursor[3][1]);
	    }
	    numberPoints++;
	}

else if (numberPoints == 4){
	  //************ First Region Marker *********************************
            xyCursor[4][0]=cursorX;
            xyCursor[4][1]=cursorY;
            if (currentPlot instanceof Plot1d){

		currentPlot.markChannel(xyCursor[4][0], xyCursor[4][1]);
		textOut.messageOut(": Peak "+xyCursor[4][0]+" to ");
            } else {

                textOut.messageOut(""+xyCursor[4][0]+","+xyCursor[4][1]+" to ",MessageHandler.END);
		currentPlot.markChannel(xyCursor[4][0], xyCursor[4][1]);
	    }
	    numberPoints++;	    
	}

else if (numberPoints == 5){
	  //************ Second Region Marker *********************************
            xyCursor[5][0]=cursorX;
            xyCursor[5][1]=cursorY;

            if (currentPlot instanceof Plot1d){

		currentPlot.markChannel(xyCursor[5][0], xyCursor[5][1]);
		currentPlot.markArea(xyCursor[4][0], xyCursor[5][0],
				     xyCursor[4][1], xyCursor[5][1]);

		textOut.messageOut(""+xyCursor[5][0]+"\n",MessageHandler.END);
            } else {
                textOut.messageOut(""+xyCursor[5][0]+","+xyCursor[5][1],MessageHandler.END);               
		currentPlot.markChannel(xyCursor[5][0], xyCursor[5][1]);        
	    }

	    grossArea=inquire.getArea(((Plot1d)currentPlot).getCounts(),
				      xyCursor[4][0], xyCursor[5][0]); 
       	inquire.getNetArea(netArea, netAreaError, channelBackground, fwhm, 
	    centroid, centroidError, xyCursor, grossArea,
	    currentPlot.getSizeX(), ((Plot1d)currentPlot).getCounts());

	    	    if (currentPlot.isCalibrated){
	    	centroid[0]=display.calibrationFunction.getCalculatedEnergy(centroid[0]);
			fwhm[0]=display.calibrationFunction.getCalculatedEnergy(fwhm[0]);
			centroidError[0]=display.calibrationFunction.getCalculatedEnergy(centroidError[0]);

	    	    }
	    textOut.messageOut("GrossArea = " + grossArea +", " + "NetArea = "
                           + numFormat.format(netArea[0]) + ", " + "Error = " 
                           + numFormat.format(netAreaError[0])+ ", " + "Centroid = "     
			   + numFormat.format(centroid[0]) + ", " + "Error = "
			   + numFormat.format(centroidError[0])+ ", " + "FWHM = "
			   + numFormat.format(fwhm[0]), MessageHandler.END); 
	  
	    //	     Draw Fit on screen by calling DisplayFit in Display.java
	    this.display.displayFit(channelBackground,null,
				    xyCursor[0][0], xyCursor[3][0]+1);  
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
		if (commandPresent == false) {
			init();
			textOut.messageOut(
				"Click on spectra or type the channel number (energy if calibrated) of interest ",
				MessageHandler.NEW);
		} else if (numberPoints == 0) {
			numberPoints = 1;
			xyCursor[0][0] = cursorX;
			xyCursor[0][1] = cursorY;
			inputCursorValue = xyCursor[0][0];
			if (currentPlot instanceof Plot1d) {
				if (mousePressed != true) {
					if (currentPlot.isCalibrated) {
						xyCursor[0][0] =
							(int) display.calibrationFunction.getEnergy(
								xyCursor[0][0]);
						if (xyCursor[0][0] > currentPlot.getSizeX()) {
							xyCursor[0][0] = currentPlot.getSizeX() - 1;
						}
					}
				}
			}
			channelLow = xyCursor[0][0] - 50;
			channelHigh = channelLow + 100;
			currentPlot.expand(channelLow, channelHigh, 0, 0);
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
	
	static private boolean autoOnExpand=true;
	
	/**
	 * Sets whether expand/zoom also causes an auto-scale.
	 */
	static public void setAutoOnExpand(boolean whether) {
		autoOnExpand=whether;
	}
		
}
