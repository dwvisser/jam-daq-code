package jam.plot;
import java.awt.*;
import java.util.*;
import jam.data.*;

/**
 * Plots a 1-dimensional histogram.
 *
 * @see jam.plot.Plot
 * @author Ken Swartz
 */
class Plot1d extends Plot {

	//private double [] fitChan,fitFunc,fitResd;
	private double[] fitChannels;
	private double[] fitResiduals;
	private double[] fitBackground;
	private double[] fitTotal;
	private double[][] fitSignals;

	/**
	 * Constructor
	 */
	public Plot1d() {
		super();
	}

	/**
	 * Overlay a second histogram
	 */
	public void overlayHistogram(Histogram hist) {
		int countsInt[];
		double countsDble[];

		displayingOverlay = true;
		overlayHist = hist;
		countsOverlay = new double[hist.getSizeX()];
		if (hist.getType() == Histogram.ONE_DIM_INT) {
			countsInt = (int[]) hist.getCounts();
			for (int i = 0; i < hist.getSizeX(); i++) {
				countsOverlay[i] = countsInt[i];
			}
		} else if (hist.getType() == Histogram.ONE_DIM_DOUBLE) {
			countsDble = (double[]) hist.getCounts();
			for (int i = 0; i < hist.getSizeX(); i++) {
				countsOverlay[i] = countsDble[i];
			}
		}
		Graphics g = this.getGraphics();
		graph.update(g); //so graph has all pertinent info
		paintOverlay(g);
		g.dispose();
	}

	/**
	 * Display a gate
	 *
	 */
	public void displayGate(Gate gate) throws DataException {
		if (currentHist != null && currentHist.hasGate(gate)) {
			displayingGate = true;
			currentGate = gate;
			Graphics g = this.getGraphics();
			//update plotgraphics with pertinent imfo
			graph.update(g, getSize(), plotLimits);
			paintGate(g);
			g.dispose();
		} else {
			System.err.println(
				getClass().getName()
					+ ": trying to display '"
					+ gate
					+ "' on histogram '"
					+ currentHist
					+ "'");
		}
	}

	/**
	 * Show the making of a gate
	 *
	 */
	public void displaySetGate(int mode, Point pChannel, Point pPixel) {
		if (mode == GATE_NEW) {
			pointsGate = new Vector(10, 5);
		} else if (mode == GATE_CONTINUE) {
			pointsGate.add(pChannel);
			Graphics g = this.getGraphics();
			g.setColor(PlotColorMap.gateDraw);
			graph.update(g); //so graph has all pertinent imfo
			graph.settingGate1d(pointsGate);
			g.dispose();
		} else if (mode == GATE_SAVE) {
			pointsGate = null;
		} else if (mode == GATE_CANCEL) {
			pointsGate = null;
		}
	}

	/**
	 * display a fit as a a line graph
	 *
	 * @param channel the channels of the fit
	 * @param countsdl  the counts of the fit
	 */
	/*public void displayFit(double [] countsdl, int lowerLimit, int upperLimit) {
	    displayFit(countsdl, null, lowerLimit, upperLimit);
	}*/

	/**
	 * display a fit as a a line graph
	 * with residual
	 */
	/*public void displayFit(double [] countsdl, double [] residual, int lowerLimit, int upperLimit){
	    int length,i,j;
	
	    displayingFit=true;
	    length = upperLimit - lowerLimit + 1;
	    fitChan = new double [length];
	    fitFunc = new double [length];
	    for (i = lowerLimit, j=0; j < length; i++,j++) {
	        fitChan[j]=i+0.5;
	    }
	    System.arraycopy(countsdl, lowerLimit, fitFunc,   0, length);
	    if(residual!=null) {
	        fitResd = new double [length];
	        System.arraycopy(residual, lowerLimit, fitResd,   0, length);
	    }
	    Graphics g=this.getGraphics();
	    graph.update(g,viewSize,plotLimits);  //so graph has all pertinent imfo
	    paintFit(g);
	    g.dispose();
	}*/

	/**
	 * Displays a fit, starting
	 */
	public void displayFit(
		double[][] signals,
		double[] background,
		double[] residuals,
		int ll) {
		this.fitBackground = null;
		this.fitChannels = null;
		this.fitResiduals = null;
		this.fitTotal = null;
		this.displayingFit = true;
		int length = 0;
		if (signals != null) {
			length = signals[0].length;
		} else {
			length = background.length;
		}
		fitChannels = new double[length];
		for (int i = 0; i < length; i++) {
			this.fitChannels[i] = ll + i + 0.5;
		}
		if (signals != null) {
			this.fitSignals = new double[signals.length][length];
			this.fitTotal = new double[length];
			for (int sig = 0; sig < signals.length; sig++) {
				System.arraycopy(
					signals[sig],
					0,
					this.fitSignals[sig],
					0,
					length);
				for (int bin = 0; bin < length; bin++) {
					fitTotal[bin] += signals[sig][bin];
				}
			}
		}
		if (background != null) {
			this.fitBackground = new double[length];
			System.arraycopy(background, 0, fitBackground, 0, length);
			if (signals != null) {
				for (int bin = 0; bin < length; bin++) {
					fitTotal[bin] += background[bin]; 
					for (int sig=0; sig<signals.length; sig++){
						fitSignals[sig][bin] += background[bin];
					}
				}
			}
		}
		if (residuals != null) {
			this.fitResiduals = new double[length];
			System.arraycopy(residuals,0,fitResiduals,0,length);
		}
		Graphics g=this.getGraphics();
		graph.update(g,viewSize,plotLimits);  //so graph has all pertinent imfo
		paintFit(g);
		g.dispose();
	}

	/**
	 * Mark a channel
	 * ignores the channelY
	 */
	public void markChannel(int channelX, int channelY) {
		Graphics g = this.getGraphics();
		g.setColor(PlotColorMap.mark);
		graph.update(g, viewSize, plotLimits);
		//so graph has all pertinent imfo
		graph.markChannel1d(channelX, counts[channelX]);
		g.dispose();
	}

	/**
	 * Mark Area
	 * @param minChanX the lower x channel
	 * @param minchanY the lower y channel
	 * @param maxChanX the upper x channel
	 * @param maxchanY the upper y channel
	 * dont use y values
	 */
	public void markArea(
		int minChanX,
		int maxChanX,
		int minChanY,
		int maxChanY) {
		int xll, xul;

		if (minChanX <= maxChanX) {
			xll = minChanX;
			xul = maxChanX;
		} else {
			xll = maxChanX;
			xul = minChanX;
		}
		Graphics g = this.getGraphics();
		g.setColor(PlotColorMap.area);
		graph.update(g, viewSize, plotLimits);
		//so graph has all pertinent imfo
		graph.markArea1d(xll, xul, counts);
		g.dispose();
	}

	/**
	 * Draw the current histogram
	 * including title, border, tickmarks, tickmark labels
	 * and last but not least update the scrollbars
	 */
	void paintHistogram(Graphics g) {
		g.setColor(PlotColorMap.hist);
		graph.drawHist(counts);
		if (autoPeakFind) {
			try {
				graph.drawPeakLabels(
					currentHist.findPeaks(sensitivity, width, pfcal));
			} catch (DataException e) {
				System.err.println(e);
			}
		}
		//draw ticks after histogram so they are on top
		g.setColor(PlotColorMap.foreground);
		g.setColor(PlotColorMap.foreground);
		graph.drawTitle(title, PlotGraphics.TOP);
		graph.drawNumber(number);
		graph.drawTicks(PlotGraphics.BOTTOM);
		graph.drawLabels(PlotGraphics.BOTTOM);
		graph.drawTicks(PlotGraphics.LEFT);
		graph.drawLabels(PlotGraphics.LEFT);
		if (axisLabelX != null) {
			graph.drawAxisLabel(axisLabelX, PlotGraphics.BOTTOM);
		} else {
			graph.drawAxisLabel(X_LABEL_1D, PlotGraphics.BOTTOM);
		}
		if (axisLabelY != null) {
			graph.drawAxisLabel(axisLabelY, PlotGraphics.LEFT);
		} else {
			graph.drawAxisLabel(Y_LABEL_1D, PlotGraphics.LEFT);
		}
	}

	private boolean autoPeakFind = true;
	void setPeakFind(boolean which) {
		autoPeakFind = which;
	}

	private double sensitivity = 3;
	void setSensitivity(double val) {
		sensitivity = val;
	}

	private double width = 12;
	void setWidth(double val) {
		width = val;
	}

	private boolean pfcal = true;
	void setPeakFindDisplayCal(boolean which) {
		pfcal = which;
	}

	/**
	 * Draw a overlay of another data set
	 */
	void paintOverlay(Graphics g) {
		g.setColor(PlotColorMap.overlay);
		graph.drawHist(countsOverlay);
	}

	/**
	 * Paint a gate on the give graphics object
	 */
	void paintGate(Graphics g) throws DataException {
		int ll;
		int ul;
		g.setColor(PlotColorMap.gateShow);
		ll = currentGate.getLimits1d()[0];
		ul = currentGate.getLimits1d()[1];
		graph.drawGate1d(ll, ul);
	}

	/**
	 * paints a fit to a given graphics
	 */
	void paintFit(Graphics g) {
		if (fitChannels != null){
			if (fitBackground != null){
				g.setColor(PlotColorMap.fitBackground);
				graph.drawLine(fitChannels,fitBackground);
			}
			if (fitResiduals != null){
				g.setColor(PlotColorMap.fitResidual);
				graph.drawLine(fitChannels,fitResiduals);
			}
			if (fitSignals != null){
				g.setColor(PlotColorMap.fitSignal);
				for (int sig=0; sig<fitSignals.length; sig++){
					graph.drawLine(fitChannels,fitSignals[sig]);					
				}
			}
			if (fitTotal != null){
				g.setColor(PlotColorMap.fitTotal);
				graph.drawLine(fitChannels,fitTotal);
			}
		}
	}

	/**
	 * Get the counts in a X channel,
	 * Y channel ignored.
	 */
	public double getCount(int channelX, int channelY) {
		return counts[channelX];
	}

	/**
	 * Get the array of counts for the current histogram
	 */
	public double[] getCounts() {
		return counts;
	}

	/**
	 * Find the maximum counts in the part of the histogram displayed
	 */
	public int findMaximumCounts() {
		int chmax = plotLimits.getMaximumX();
		int chmin = plotLimits.getMinimumX();
		int maxCounts = 0;
		if ((chmin == 0) && (ignoreChZero)) {
			chmin = 1;
		}
		if ((chmax == (sizeX - 1)) && (ignoreChFull)) {
			chmax = sizeX - 2;
		}
		for (int i = chmin; i <= chmax; i++) {
			if (counts[i] > maxCounts) {
				maxCounts = (int) counts[i]; //FIXME
			}
		}
		return (maxCounts);
	}

	/**
	 * Find the minimum counts in the part of the histogram displayed
	 */
	public int findMinimumCounts() {
		int chmax = plotLimits.getMaximumX();
		int chmin = plotLimits.getMinimumX();
		int minCounts = 0;
		if ((chmin == 0) && (ignoreChZero)) {
			chmin = 1;
		}
		if ((chmax == (sizeX - 1)) && (ignoreChFull)) {
			chmax = sizeX - 2;
		}
		for (int i = chmin; i <= chmax; i++) {
			if (counts[i] < minCounts) {
				minCounts = (int) counts[i];
			}
		}
		return minCounts;
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 */
	public double getEnergy(double channel) {
		return currentHist.getCalibration().getCalculatedEnergy(channel);
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 */
	public int getChannel(double energy) {
		return (int) Math.round(
			currentHist.getCalibration().getChannel(energy));
	}
}
