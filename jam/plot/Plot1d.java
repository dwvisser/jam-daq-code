package jam.plot;
import java.awt.*;
import jam.data.*;
import jam.global.JamProperties;

/**
 * Plots a 1-dimensional histogram.
 *
 * @see jam.plot.Plot
 * @author Ken Swartz
 */
class Plot1d extends Plot {

	private double[] fitChannels, fitResiduals, fitBackground, fitTotal;
	private double[][] fitSignals;
	private int areaMark1, areaMark2;

	/**
	 * Constructor
	 */
	public Plot1d(Action a) {
		super(a);
	}

	/**
	 * Overlay a second histogram
	 */
	public void overlayHistogram(Histogram hist) {
		displayingOverlay = true;
		overlayHist = hist;
		final int sizex=hist.getSizeX();
		countsOverlay = new double[sizex];
		if (hist.getType() == Histogram.ONE_DIM_INT) {
			final int [] countsInt = (int[]) hist.getCounts();
			for (int i = 0; i < hist.getSizeX(); i++) {
				countsOverlay[i] = countsInt[i];
			}
		} else if (hist.getType() == Histogram.ONE_DIM_DOUBLE) {
			final double [] countsDble = (double[]) hist.getCounts();
			System.arraycopy(countsDble,0,countsOverlay,0,sizex);
		}
		repaint();
	}

	/**
	 * Show the making of a gate, point by point.
	 * 
	 * @param mode GATE_NEW, GATE_CONTINUE, GATE_SAVE or GATE_CANCEL
	 * @param pChannel channel coordinates of clicked channel
	 */
	public void displaySetGate(GateSetMode mode, Point pChannel, Point pPixel) {
		if (mode == GateSetMode.GATE_NEW) {
			pointsGate.reset();
			setSettingGate(true);
		} else {
			if (mode == GateSetMode.GATE_CONTINUE) {
				pointsGate.addPoint(pChannel.x,pChannel.y);
			} else if (mode == GateSetMode.GATE_SAVE) {
				pointsGate.reset();
			} else if (mode == GateSetMode.GATE_CANCEL) {
				pointsGate.reset();
				setSettingGate(false);
			}
			repaint();
		}
	}
	
	void paintSetGate(Graphics g){
		g.setColor(PlotColorMap.gateDraw);
		graph.settingGate1d(graph.toView(pointsGate));
	}
	
	void paintMouseMoved(Graphics g){
		
	}

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
					for (int sig = 0; sig < signals.length; sig++) {
						fitSignals[sig][bin] += background[bin];
					}
				}
			}
		}
		if (residuals != null) {
			this.fitResiduals = new double[length];
			System.arraycopy(residuals, 0, fitResiduals, 0, length);
		}
		repaint();
	}

	/**
	 * Mark a channel
	 * ignores the channelY
	 */
	public void markChannel(Point p) {
		Graphics g = this.getGraphics();
		g.setColor(PlotColorMap.mark);
		/* so graph has all pertinent imfo */
		graph.update(g, viewSize, plotLimits);
		graph.markChannel1d(p.x, counts[p.x]);
		g.dispose();
	}

	/**
	 * Mark Area. The y-values are ignored.
	 * 
	 * @param minChanX the lower x channel
	 * @param minchanY the lower y channel
	 * @param maxChanX the upper x channel
	 * @param maxchanY the upper y channel
	 */
	public void markArea(Point p1, Point p2) {
		synchronized (this) {
			markingArea = (p1 != null) && (p2 != null);
			if (markingArea) {
				areaMark1 = Math.min(p1.x, p2.x);
				areaMark2 = Math.max(p1.x, p2.x);
			}
		}
		repaint();
	}

	void paintMarkArea(Graphics g) {
		final Graphics2D g2=(Graphics2D)g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		0.5f));
		g.setColor(PlotColorMap.area);
		graph.update(g, viewSize, plotLimits);
		graph.markArea1d(areaMark1, areaMark2, counts);
	}

	/**
	 * Draw the current histogram
	 * including title, border, tickmarks, tickmark labels
	 * and last but not least update the scrollbars
	 */
	void paintHistogram(Graphics g) { 
		g.setColor(PlotColorMap.hist);
		graph.drawHist(counts,binWidth);
		if (autoPeakFind) {
			try {
				graph.drawPeakLabels(
					currentHist.findPeaks(sensitivity, width, pfcal));
			} catch (DataException e) {
				System.err.println(e);
			}
		}
		/* draw ticks after histogram so they are on top */
		g.setColor(PlotColorMap.foreground);
		g.setColor(PlotColorMap.foreground);
		graph.drawTitle(title, PlotGraphics.TOP);
		final int nOverlay = displayingOverlay ? overlayHist.getNumber() : -1;
		graph.drawNumber(number, nOverlay);
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
		graph.drawHist(countsOverlay,binWidth);
	}

	/**
	 * Paint a gate on the give graphics object
	 */
	void paintGate(Graphics g) throws DataException {
		final Graphics2D g2=(Graphics2D)g;
		final boolean noFillMode =
			JamProperties.getBooleanProperty(JamProperties.NO_FILL_GATE);
		if (!noFillMode) {
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
			0.5f));
		} 
		g.setColor(PlotColorMap.gateShow);
		int ll = currentGate.getLimits1d()[0];
		int ul = currentGate.getLimits1d()[1];
		graph.drawGate1d(ll, ul, noFillMode);
	}

	/**
	 * paints a fit to a given graphics
	 */
	void paintFit(Graphics g) {
		if (fitChannels != null) {
			if (fitBackground != null) {
				g.setColor(PlotColorMap.fitBackground);
				graph.drawLine(fitChannels, fitBackground);
			}
			if (fitResiduals != null) {
				g.setColor(PlotColorMap.fitResidual);
				graph.drawLine(fitChannels, fitResiduals);
			}
			if (fitSignals != null) {
				g.setColor(PlotColorMap.fitSignal);
				for (int sig = 0; sig < fitSignals.length; sig++) {
					graph.drawLine(fitChannels, fitSignals[sig]);
				}
			}
			if (fitTotal != null) {
				g.setColor(PlotColorMap.fitTotal);
				graph.drawLine(fitChannels, fitTotal);
			}
		}
	}

	/**
	 * Get the counts in a X channel,
	 * Y channel ignored.
	 */
	public double getCount(Point p) {
		return counts[p.x];
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
