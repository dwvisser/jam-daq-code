package jam.plot;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Class of a library of methods to draw things for a graph.
 * First the update method is called to set the limits for the
 * plot and to give it a handle to the graphics object.
 *
 * <p>
 * The screen origin is upper right hand corner
 * while the data point origin is lower left hand corner.
 * There are private methods that map screen pixels to
 * data co-ordinates and other public methods that
 * class to map data points to screen pixels.
 * <p>
 * The border size is the area around the plot
 * the outline the plot is part of the plot,
 * the outline is part not part of the border.
 * Therefore channels with zero counts will therefore draw along
 * the lower border, and the left and right border will be part of
 * a channel.
 *
 * The method getSize() for <code>Component</code> returns the number of
 * pixels which are labeled starting with 0, to getSize()-1
 *
 * @version  0.5 April 98, May 99
 * @author   Ken Swartz
 * @see         java.awt.Graphics
 * @since       JDK1.1
 */
class PlotGraphics implements PlotGraphicsLayout {

	static final int BOTTOM = 1;
	static final int TOP = 2;
	static final int LEFT = 3;
	static final int RIGHT = 4;

	static final int ONE_DIMENSION = 1;
	static final int TWO_DIMENSION = 2;

	/* fake zero for Log scale  1/2 a count */
	static final double LOG_FAKE_ZERO = 0.5;

	//  current stuff to draw font, and font metrics and colors
	private Graphics2D g;
	private Font font;
	private FontMetrics fm;
	private int numberColors;
	private int[] colorThresholds;
	private Tickmarks tm;
	/**
	 * Border for plot in pixels
	 */
	private Insets border;
	/**
	 * margins for printing a hard copy.
	 */
	private Insets margin;

	/**The Limits in channel and scale of the plot */
	private Limits plotLimits;
	/** is the plot 1d or 2d */
	private int plotType;

	/**
	 * variable for converting pixels to data and data to pixels
	 */
	private int minXch; //minimum horizontal for data
	private int maxXch; //maximum horizontal for data
	private int rangeXch;
	/** limits in Y, counts (1d) or channels (2d) */
	private int minY;
	private int maxY;
	private double minYLog;
	private double maxYLog;
	private int minCount;
	private int maxCount;
	/**number of pixels per channel */
	private double conversionX;
	/**number of pixels per channel */
	private double conversionY;
	/**number of pixels per channel */
	private double conversionYLog;

	/** the dimensions of the plot canvas */
	private Dimension viewSize;

	/**sides of plot in pixels */
	private int viewLeft; //left hand side of plot area
	private int viewRight; //right hand side of plot area
	private int viewTop; //top side of plot area
	private int viewBottom; //bottom side of plot area
	private int viewWidth; //width of plot area
	private int viewHeight; //height of plot area
	private Point viewMiddle; //middle of plot area

	/**
	 * Full constructor, all contructors eventually call this one.
	 * Other constructors have  defaults
	 */
	PlotGraphics(JPanel plot, Insets border, Font font) {
		this.border = border;
		setFont(font);
		//class that draws tick marks and makes color thresholds
		tm = new Tickmarks();
		//margin for printing
		margin = new Insets(0, 0, 0, 0);
		//maybe should be avaliable in constructor
		//middle of plot
		viewMiddle = new Point();
		if (plot instanceof Plot1d) {
			plotType = ONE_DIMENSION;
		} else if (plot instanceof Plot2d) {
			plotType = TWO_DIMENSION;
		}
	}

	/**
	 * Set the font used on the plot.
	 */
	final synchronized void setFont(Font f) {
		font = f;
		if (g != null) {
			g.setFont(f);
			fm = g.getFontMetrics();
		}
	}

	private PageFormat pageformat=null;
	synchronized void setView(PageFormat pf){
		pageformat=pf;
		if (pf!=null){
			viewSize = new Dimension((int)pf.getImageableWidth(),
			(int)pf.getImageableHeight());
		}
	}

	/**
	 * updates the current display parameters
	 *  the most basic update this one must always be called
	 *
	 * @param   graph  the Graphics object
	 * @param   viewSize  the viewable size of the canvas in pixels
	 * @param   limits  the limits of the plot
	 * @param   f  the font for labels
	 *
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void update(
		Graphics graph,
		Dimension newViewSize,
		Limits plotLimits) {
		update(graph); //get graphics and copy to local variables
		this.plotLimits = plotLimits;
		//retrieve imformation from plotLimits object
		if (plotLimits != null) {
			minCount = plotLimits.getMinimumCounts();
			maxCount = plotLimits.getMaximumCounts();
			if (plotType == ONE_DIMENSION) {
				minXch = plotLimits.getMinimumX();
				maxXch = plotLimits.getMaximumX();
				minY = plotLimits.getMinimumCounts();
				maxY = plotLimits.getMaximumCounts();
			} else if (plotType == TWO_DIMENSION) {
				minXch = plotLimits.getMinimumX();
				maxXch = plotLimits.getMaximumX();
				minY = plotLimits.getMinimumY();
				maxY = plotLimits.getMaximumY();
			}
			minYLog = takeLog(minY);
			maxYLog = takeLog(maxY);
			rangeXch = maxXch - minXch + 1;
			final int rangeY = maxY - minY + 1;
			final double rangeYLog = maxYLog - minYLog;
			if (pageformat == null){
				this.viewSize = newViewSize;
			}
			//plot* are the borders and are part of the plot
			viewLeft = border.left; //really 0+border.left
			viewTop = border.top; //really 0+border.top
			viewRight = viewSize.width - border.right - 1;
			//subtract 1 as last pixel size-1
			viewBottom = viewSize.height - border.bottom - 1;
			//subtract 1 as last pixel size-1
			viewWidth = viewRight - viewLeft + 1;
			//add 1 as border part of plot
			viewHeight = viewBottom - viewTop + 1;
			//add 1 as border part of plot
			conversionX = (double) viewWidth / ((double) rangeXch);
			//number of pixels per channel
			conversionY = (double) viewHeight / ((double) rangeY);
			//number of pixels per channel
			conversionYLog = viewHeight / rangeYLog;
			//number of pixels per channel
		}
	}
	
	private static final RenderingHints RH=new RenderingHints(
	RenderingHints.KEY_ANTIALIASING,
	RenderingHints.VALUE_ANTIALIAS_OFF);
	static {
		RH.put(RenderingHints.KEY_TEXT_ANTIALIASING,
		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	
	/**
	 * Update just the graphics object
	 * @param graph  grapics object
	 */
	private void update(Graphics graph) {
		g = (Graphics2D) graph;
		g.setRenderingHints(RH);
		if (fm==null){
			this.setFont(font);
		}
	}

	/**
	 * Draws the title for a plot.
	 *
	 * @param title the title
	 * @param side the side on which to draw the title
	 * @return  <code>void</code>
	 * @see  jam.plot.PlotMouse
	 * @since Version 0.5
	 */
	void drawTitle(String title, int side) {
		int offset = 1;
		if (side == PlotGraphics.TOP) {
			setFont(font.deriveFont(PlotGraphicsLayout.TITLE_SCREEN_SIZE));
			offset = fm.stringWidth(title); //title.length()*CHAR_SIZE;
			g.drawString(
				title,
				viewMiddle().x - offset / 2,
				viewTop - TITLE_OFFSET_TOP);
			setFont(font.deriveFont(PlotGraphicsLayout.SCREEN_FONT_SIZE));
		}
	}

	void drawNumber(int number, int [] overlayNumbers) {
		final String s = Integer.toString(number);
		setFont(font);
		int width = fm.stringWidth(s);
		int xNext = this.viewLeft - TITLE_OFFSET_TOP - width;
		final int y = viewTop - TITLE_OFFSET_TOP;
		final Color c = g.getColor();
		g.setColor(PlotColorMap.foreground);
		g.drawString(s, xNext, y);
		for (int i=0; i<overlayNumbers.length; i++){
			xNext += width;
			final String sNext=", "+overlayNumbers[i];
			width=fm.stringWidth(sNext);
			g.setColor(PlotColorMap.overlay[i%PlotColorMap.overlay.length]);
			g.drawString(sNext,xNext,y);
		}
		g.setColor(c);
	}

	/**
	 * Draws the date in the upper right hand corner
	 *
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawDate(String sdate) {
		int x = viewRight - fm.stringWidth(sdate); //position of string
		int y = viewTop - TITLE_OFFSET_DATE;
		g.drawString(sdate, x, y);
	}

	/**
	 * Draws the run number in the upper left hand corner
	 *
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawRun(int runNumber) {
		String runLabel = "Run " + runNumber;
		int x = viewLeft;
		int y = viewTop - TITLE_OFFSET_DATE;
		g.drawString(runLabel, x, y);
	}

	/**
	 * Draws the border around the plot
	 * As plotSize() returns the the size of the plot including the borders
	 * we have to subtract one from the width and height.
	 * as drawRect(x,y,dx,dy) draws at x, y and at x+dx, y+dy.
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawBorder() {
		g.drawRect(viewLeft, viewTop, viewWidth - 1, viewHeight - 1);
	}

	/**
	 * Draws the tickmarks on for a plot
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawTicks(int side) {
		final int ll, ul;
		Limits.ScaleType scale = Limits.ScaleType.LINEAR;
		if (side == BOTTOM) {//always linear
			ll = minXch;
			ul = maxXch;
			ticksBottom(ll, ul);
		} else { //side==LEFT, if 1d-depends on Limits's scale
			ll = minY;
			ul = maxY;
			if (plotType == ONE_DIMENSION) {
				scale = plotLimits.getScale();
			}
			ticksLeft(ll, ul, scale);
		}
	}

	/**
	 * Draws the tickmarks on the bottom side, X
	 *
	 * @param lowerLimit
	 * @param upperLimit
	 * @param scale
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	private void ticksBottom(
		int lowerLimit,
		int upperLimit) {
		final Limits.ScaleType scale=Limits.ScaleType.LINEAR;
		final int [] ticks =
			tm.getTicks(lowerLimit, upperLimit, scale, Tickmarks.MINOR);
		for (int i = 0; i < ticks.length; i++) {
			final int x = toViewHorzLin(ticks[i]);
			int y = viewBottom;
			g.drawLine(x, y, x, y - TICK_MINOR);
			y = viewTop;
			g.drawLine(x, y, x, y + TICK_MINOR);
		}
		final int [] ticksMajor =
			tm.getTicks(lowerLimit, upperLimit, scale, Tickmarks.MAJOR);
		for (int i = 0; i < ticksMajor.length; i++) {
			final int x = toViewHorzLin(ticksMajor[i]);
			int y = viewBottom;
			g.drawLine(x, y, x, y - TICK_MAJOR);
			y = viewTop;
			g.drawLine(x, y, x, y + TICK_MAJOR);
		}
	}

	/**
	 * Draws the tickmarks on for the left side, Y
	 *
	 * @param lowerLimit
	 * @param upperLimit
	 * @param scale
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	private void ticksLeft(
		int lowerLimit,
		int upperLimit,
		Limits.ScaleType scale) {
		int x;
		int y;

		int[] ticks =
			tm.getTicks(lowerLimit, upperLimit, scale, Tickmarks.MINOR);
		for (int i = 0; i < ticks.length; i++) {
			if (scale == Limits.ScaleType.LINEAR) {
				y = toViewVertLin(ticks[i]);
			} else {
				y = toViewVertLog(ticks[i]);
			}
			x = viewLeft;
			g.drawLine(x, y, x + TICK_MINOR, y);
			x = viewRight;
			g.drawLine(x, y, x - TICK_MINOR, y);
		}

		int[] ticksMajor =
			tm.getTicks(lowerLimit, upperLimit, scale, Tickmarks.MAJOR);
		for (int i = 0; i < ticksMajor.length; i++) {
			if (scale == Limits.ScaleType.LINEAR) {
				y = toViewVertLin(ticksMajor[i]);
			} else {
				y = toViewVertLog(ticksMajor[i]);
			}
			x = viewLeft;
			g.drawLine(x, y, x + TICK_MAJOR, y);
			x = viewRight;
			g.drawLine(x, y, x - TICK_MAJOR, y);
		}
	}

	/**
	 * Draws the tick mark Labels on for a plot
	 *
	 * @param side
	 * @since Version 0.5
	 */
	void drawLabels(int side) {
		if (side == BOTTOM) {
			labelsBottom(minXch, maxXch);
		}
		if (plotType == ONE_DIMENSION && side == LEFT) {
			labelsLeft(minY, maxY, plotLimits.getScale());
		} else if (plotType == TWO_DIMENSION && side == LEFT) {
			labelsLeft(minY, maxY, Limits.ScaleType.LINEAR);
		}
	}
	
	/**
	 * Draws the Labels on for the bottom side of a plot
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */

	private void labelsBottom(
		int lowerLimit,
		int upperLimit) {
		final Limits.ScaleType scale=Limits.ScaleType.LINEAR;
		final int [] ticksMajor =
			tm.getTicks(lowerLimit, upperLimit, scale, Tickmarks.MAJOR);
		for (int i = 0; i < ticksMajor.length; i++) {
			final String label = Integer.toString(ticksMajor[i]);
			final int offset = fm.stringWidth(label); //length of string
			final int x = toViewHorzLin(ticksMajor[i]) - offset / 2;
			final int y = viewBottom + fm.getAscent() + LABEL_OFFSET_BOTTOM;
			g.drawString(label, x, y);
		}
	}
	
	/**
	 * Draws the Labels on for the left side of a plot
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	private void labelsLeft(
		int lowerLimit,
		int upperLimit,
		Limits.ScaleType scale) {
		int[] ticksMajor =
			tm.getTicks(lowerLimit, upperLimit, scale, Tickmarks.MAJOR);
		for (int i = 0; i < ticksMajor.length; i++) {
			final String label = Integer.toString(ticksMajor[i]);
			final int offset = fm.stringWidth(label);
			int y=fm.getAscent() / 2;
			if (scale == Limits.ScaleType.LINEAR) {
				y += toViewVertLin(ticksMajor[i]);
			} else {
				y += toViewVertLog(ticksMajor[i]);
			}
			final int x = viewLeft - offset - LABEL_OFFSET_LEFT;
			g.drawString(label, x, y);
		}
	}
	
	/**
	 * Draws the axis Labels on for a plot
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawAxisLabel(String label, int side) {
		if (side == BOTTOM) {
			axisLabelBottom(label);
		}
		if (side == LEFT) {
			axisLabelLeft(label);
		}
	}

	/**
	 * Draws the axis Labels on for the bottom side of a plot
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	private void axisLabelBottom(String label) {
		final int offset = fm.stringWidth(label);
		final int x = viewMiddle().x - offset / 2;
		final int y = viewBottom + fm.getAscent() + AXIS_LABEL_OFFSET_BOTTOM;
		g.drawString(label, x, y);
	}

	/**
	 * Draws the axis Labels on for the left side of a plot
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	private void axisLabelLeft(String label) {
		final double ninetyDeg=-Math.PI*0.5;
		final int offset=fm.stringWidth(label);
		final int y=viewMiddle().y+offset/2;
		final int x=viewLeft - AXIS_LABEL_OFFSET_LEFT;
		final AffineTransform original=g.getTransform();
		g.translate(x,y);
		g.rotate(ninetyDeg);
		g.drawString(label,0,0);
		g.setTransform(original);
	}

	/**
	 * Histogram a plot with double count array
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawHist(double[] counts, double binWidth) {
		drawHist(counts, binWidth, 
		plotLimits.getScale()!=Limits.ScaleType.LINEAR);
	}

	/**
	 * Plot a line graph
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawLine(double[] channel, double[] countsdl) {
		if (plotLimits.getScale() == Limits.ScaleType.LINEAR) {
			drawLineLinear(channel, countsdl);
		} else {
			drawLineLog(channel, countsdl);
		}

	}

	/**
	 * Draw  a histogram  for linear scale.
	 * Stair step, step up at channel beginning.
	 *
	 * @param counts to draw
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	private void drawHist(double[] counts, double binWidth, boolean log) {
		/* x's are in channels, y's are in counts */
		final double [] drawCounts=getDrawCounts(counts,binWidth);
		final int dclen=drawCounts.length;
		final int lastBinAll=dclen-1;
		final double lastBinAllLo=lastBinAll*binWidth;
		if (counts == null) {
			JOptionPane.showMessageDialog(null,
			"drawHistLinear() called with null array.",
			getClass().getName(),JOptionPane.WARNING_MESSAGE);
		} else {
			final int upperX = (int)Math.min(maxXch+1,lastBinAllLo);
			final int firstBin=(int)Math.floor(minXch/binWidth);
			final int lastBin=(int)Math.floor(upperX/binWidth);
			double binChLo=firstBin*binWidth;
			int x1 = 0;
			int x2 = toViewHorzLin(minXch);
			int y2 = viewBottom;
			double delCh=binWidth;
			if (binChLo<minXch){
				delCh=binChLo+binWidth-minXch;
				binChLo=minXch;
			}
			for (int i=firstBin; i<=lastBin; i++){
				/* first a vertical line */
				x1=x2; x2=x1;
				int y1=y2;
				y2= log ? toViewVertLog(drawCounts[i]) :
				toViewVertLinCk(drawCounts[i]);
				g.drawLine(x1,y1,x2,y2);
				/* now horizontal across bin, i.e., y1==y2 */
				x1=x2; y1=y2; //last point becomes start
				binChLo += delCh;
				x2 = Math.min(viewRight,toViewHorzLin(binChLo));
				g.drawLine(x1,y1,x2,y2);
				delCh=Math.min(binWidth,maxXch+1-binChLo);
			}
			// last vertical line
			if (x2 < viewRight) {
				x1 = x2;
				int y1 = y2;
				y2 = viewBottom;
				g.drawLine(x1, y1, x2, y2);
			}
		}
	}
	
	private static final double EPSILON=0.001;
	private double [] getDrawCounts(double [] counts, double bin){
		/* bin assumed >= 1.0 */
		final double one=1.0;
		final int clen=counts.length;
		final int len=(int)Math.ceil(counts.length/bin);
		double [] rval=new double[len];
		if ((bin-1.0)<=EPSILON) {
			rval=counts;
		} else {
			double remain=bin;
			int index=0;
			for (int i=0; i<clen; i++){
				if (remain > one){
					rval[index] += counts[i];
					remain -= one;
				} else {
					rval[index] += counts[i]*remain;
					if(index < len-1){
						index++;
					}
					rval[index] += counts[i]*(one-remain);
					remain += bin-one;
				}
			}
			for (int i=0; i<len; i++){
				rval[i] /= bin;
			}
		}
		return rval;
	}

	/**
	 * Plot a line graph.
	 * plot is lines segments
	 *
	 * @param channel
	 * @param counts
	 * @since Version 0.5
	 */
	private void drawLineLinear(double[] channel, double[] counts) {
		final int lowerX = Math.max(this.minXch, (int) channel[0] + 1);
		final int upperX = Math.min(this.maxXch, (int) channel[channel.length - 1]);
		int x1 = toViewHorzLin(channel[0]);
		/* check dont go beyond border */
		int y1 = Math.max(toViewVertLin(counts[0]),viewTop);
		/* for each point draw from last line to next line */
		for (int i = 1; i < channel.length; i++) {
			final int x2 = toViewHorzLin(channel[i]);
			/* could go 1 pixel too far for last i */
			final int y2 = Math.max(toViewVertLin(counts[i]),viewTop);
			/* check we don't go beyond border */
			if ((channel[i] >= lowerX) && (channel[i] <= upperX)) {
				g.drawLine(x1, y1, x2, y2);
			}
			/* save start for next line segment */
			x1 = x2;
			y1 = y2;
		}
	}

	/**
	 * Plot a line graph,
	 * Log scale
	 *
	 * @param side
	 * @since Version 0.5
	 */
	private void drawLineLog(double[] channel, double[] counts) {
		int lowerX = Math.max(this.minXch, (int) channel[0] + 1);
		int upperX = Math.min(this.maxXch, (int) channel[channel.length - 1]);
		int x1 = toViewHorzLin(channel[0]);
		int y1 = toViewVertLog(counts[0]);
		/* check we don't go beyond border */
		if (y1 < viewTop) {
			y1 = viewTop;
		}
		/* for each point draw from last line to next line */
		for (int i = 1; i < channel.length; i++) {
			final int x2 = toViewHorzLin(channel[i]);
			//could go 1 pixel too far for last i
			int y2 = toViewVertLog(counts[i]);
			//check dont go beyond border
			if (y2 < viewTop) {
				y2 = viewTop;
			}
			if ((channel[i] >= lowerX) && (channel[i] <= upperX)) {
				g.drawLine(x1, y1, x2, y2);
			}
			//save start for next line segment
			x1 = x2;
			y1 = y2;
		}
	}

	/**
	 * Draw scale for a 2d plot
	 *
	 * @param colors colors to use
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawScale2d(Color[] colors) {
		int lowerLimit = minCount;
		int upperLimit = maxCount;
		numberColors = colors.length;
		colorThresholds = new int[numberColors];
		colorThresholds =
			tm.getColorThresholds(
				lowerLimit,
				upperLimit,
				numberColors,
				plotLimits.getScale());
		int textHeight = (fm.getAscent());
		/* lowest threshold for color to be drawn */
		String label = Integer.toString(lowerLimit);
		g.drawString(
			label,
			viewRight
				+ COLOR_SCALE_OFFSET
				+ COLOR_SCALE_SIZE
				+ COLOR_SCALE_LABEL_OFFSET,
			viewBottom + textHeight / 2);
		for (int k = 0; k < numberColors; k++) {
			label = Integer.toString(colorThresholds[k]);
			g.drawString(
				label,
				viewRight
					+ COLOR_SCALE_OFFSET
					+ COLOR_SCALE_SIZE
					+ COLOR_SCALE_LABEL_OFFSET,
				viewBottom
					- COLOR_SCALE_SIZE
					- k * COLOR_SCALE_SIZE
					+ textHeight / 2);
		}
		/* draw colors on side */
		for (int k = 0; k < numberColors; k++) {
			g.setColor(colors[k]);
			g.fillRect(viewRight + COLOR_SCALE_OFFSET, //horizontal
			viewBottom - COLOR_SCALE_SIZE - k * COLOR_SCALE_SIZE, //vertical
			COLOR_SCALE_SIZE, COLOR_SCALE_SIZE); //size
		}
	}

	void drawScale2d() {
		ColorScale colors =
			new GradientColorScale(minCount, maxCount, plotLimits.getScale());
		int lowerLimit = minCount;
		int upperLimit = maxCount;
		setFont(font);
		int textHeight = (fm.getAscent());
		numberColors = PlotColorMap.getNumberColors();
		colorThresholds =
			tm.getColorThresholds(
				lowerLimit,
				upperLimit,
				numberColors,
				plotLimits.getScale());
		/* lowest threshold for color to be drawn */
		String label = Integer.toString(lowerLimit);
		g.drawString(
			label,
			viewRight
				+ COLOR_SCALE_OFFSET
				+ COLOR_SCALE_SIZE
				+ COLOR_SCALE_LABEL_OFFSET,
			viewBottom + textHeight / 2);
		for (int k = 0; k < numberColors; k++) {
			label = Integer.toString(colorThresholds[k]);
			g.drawString(
				label,
				viewRight
					+ COLOR_SCALE_OFFSET
					+ COLOR_SCALE_SIZE
					+ COLOR_SCALE_LABEL_OFFSET,
				viewBottom
					- COLOR_SCALE_SIZE
					- k * COLOR_SCALE_SIZE
					+ textHeight / 2);
		}
		/* draw colors on side */
		int scaleHeight = numberColors * COLOR_SCALE_SIZE;
		int x1 = viewRight + COLOR_SCALE_OFFSET;
		int x2 = x1 + COLOR_SCALE_SIZE - 1;
		double level;
		double lowEnd = Math.max(1.0, lowerLimit);
		double highEnd = colorThresholds[numberColors - 1];
		for (int row = 0; row < scaleHeight; row++) {
			int y = viewBottom - row;
			if (plotLimits.getScale() == Limits.ScaleType.LINEAR) {
				level =
					lowerLimit
						+ (double) row * (highEnd - lowEnd) / scaleHeight;
			} else { //log scale
				level =
					lowEnd
						* Math.pow(highEnd / lowEnd, (double) row / scaleHeight);
			}
			g.setColor(colors.getColor(level));
			g.drawLine(x1, y, x2, y);
		}
	}

	/**
	 * Draw a 2d plot.
	 *
	 * @param counts the counts to be displayed
	 * @param colors the colors to use
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawHist2d(double[][] counts, int minChanX, int minChanY,
	int maxChanX, int maxChanY, Color[] colors) {
		int minCounts = minCount;
		int maxCounts = maxCount;

		numberColors = colors.length;
		colorThresholds = new int[numberColors];
		colorThresholds =
			tm.getColorThresholds(
				minCounts,
				maxCounts,
				numberColors,
				plotLimits.getScale());
		//for data each point
		for (int j = minChanY; j <= maxChanY; j++) {
			for (int i = minChanX; i <= maxChanX; i++) {
				final double count = counts[i][j];
				//quickly check above lower limit
				if (count > minCounts) {
					/* Constructing a color lookup array would be faster, 
					 * but this seems to draw fast enough. */
					final int x = toViewHorzLin(i);
					final int y = toViewVertLin(j);
					final int channelWidth = toViewHorzLin(i + 1) - x;
					final int channelHeight = y - toViewVertLin(j + 1);
					paintChannel : for (int k = 0; k < numberColors; k++) {
						//check for min counts first as these are most likely
						if (count <= colorThresholds[k]) {
							g.setColor(colors[k]);
							// inline for speed
							g.fillRect(
								x,
								y - channelHeight + 1,
								channelWidth,
								channelHeight);
							break paintChannel;
						}
					}
					// go here on break;
					//check if greater than all thresholds
					if (count > colorThresholds[numberColors - 1]) {
						g.setColor(colors[numberColors - 1]);
						g.fillRect(
							x,
							y - channelHeight + 1,
							channelWidth,
							channelHeight);
					}
					//end of loop for each point
				}
			}
		}
	}

	/**
	 * Draw a 2d plot.
	 *
	 * @param counts the counts to be displayed
	 * @param colors the colors to use
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawHist2d(double[][] counts, int minChanX, int minChanY,
	int maxChanX, int maxChanY) {
		ColorScale colors =
			new GradientColorScale(minCount, maxCount, plotLimits.getScale());
		/* loop over channels */
		for (int j = minChanY; j <= maxChanY; j++) {
			for (int i = minChanX; i <= maxChanX; i++) {
				double count = counts[i][j];
				/* quickly check above lower limit */
				if (count > minCount) {
					g.setColor(colors.getColor(count));
					/* inline for speed */
					int x = toViewHorzLin(i);
					int y = toViewVertLin(j);
					int channelWidth = toViewHorzLin(i + 1) - x;
					int channelHeight = y - toViewVertLin(j + 1);
					g.fillRect(
						x,
						y - channelHeight + 1,
						channelWidth,
						channelHeight);
				} //end of loop for each point
			}
		}
	}

	/**
	 * Draw a 1d Gate
	 *
	 * @param ll lower limit
	 * @param ul upper limit
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawGate1d(int ll, int ul, boolean noFillMode) {
		clipPlot();
		final int x = toViewHorzLin(ll);
		final int x2=Math.min(toViewHorzLin(ul+1),viewRight);
		final int width=x2-x;
		final int height=viewBottom-viewTop;
		if (noFillMode){
			g.drawRect(x,viewTop,width,height);
		} else {
			g.fillRect(x,viewTop,width,height);
		}
	}

	/**
	 * Draw a 2d Gate
	 *
	 * @param gate the array to be displayed
	 * @return  <code>void</code>
	 */
	void drawGate2d(boolean[][] gate) {
		for (int j = minY; j <= maxY; j++) { // for each point
			for (int i = minXch; i <= maxXch; i++) {
				if (gate[i][j]) { //if inside gate
					final int x = toViewHorzLin(i);
					final int y = toViewVertLin(j);
					final int channelWidth = toViewHorzLin(i + 1) - x;
					final int channelHeight = y - toViewVertLin(j + 1);
					g.fillRect(
						x,
						y - channelHeight + 1,
						channelWidth,
						channelHeight);
				}
				// --end for each point
			}
		}
		g.setPaintMode();
	}

	/**
	 * Setting a 1d Gate
	 *
	 * @param gatePoints gate points to be drawn (in graphics coordinates)
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void settingGate1d(Polygon gatePoints) {
		clipPlot();
		if (gatePoints.npoints>0){
			final int x1=gatePoints.xpoints[gatePoints.npoints-1];
			if (gatePoints.npoints>1){
				markAreaOutline1d(toDataHorz(gatePoints.xpoints[gatePoints.npoints-2]), 
				toDataHorz(x1));
			} else {
				g.drawLine(x1, viewBottom, x1, viewTop);
			}
		}
	}

	/**
	 * Determine the outline of an area in a 1d plot.
	 * 
	 * @param x1 a point in plot coordinates
	 * @param x2 a point in plot coordinates
	 * @return a rectangle in graphics coordinates that will
	 * highlight the channels indicated
	 */
	Rectangle getRectangleOutline1d(int x1, int x2){
		final int height=viewBottom-viewTop;//Full plot vertically
		final int x;
		final int width;		
		
		if (x1<x2) {
			final int xv1 = toViewHorzLin(x1);
			//pixel before next channel
			final int xv2 = toViewHorzLin(x2+1)-1;			
			x=xv1+1;
			width=xv2-xv1;
		}else if (x1>x2){
			//pixel before next channel
			final int xv1 = toViewHorzLin(x1+1)-1;
			final int xv2 = toViewHorzLin(x2);			
			x=xv2+1;
			width=xv1-xv2;	
		//so both at the same point shows something	
		} else{
			final int xv1 = toViewHorzLin(x1);
			//pixel before next channel
			final int xv2 = toViewHorzLin(x2+1)-1;			
			x=xv1;
			//At least 1 wide
			width=Math.max(xv2-xv1,1);
		} 	
		return new Rectangle(x,viewTop,width,height);					
	}
	
	/**
	 * Mark the outline of an area in a 1d plot.
	 * 
	 * @param x1 a point in plot coordinates
	 * @param x2 a point in plot coordinates
	 */
	void markAreaOutline1d(int x1, int x2){
		clipPlot();
		g.draw(getRectangleOutline1d(x1,x2));
	}
	
	Rectangle getRectangleOutline2d(Rectangle channels){
		final int highX=(int)channels.getMaxX();
		final int highY=(int)channels.getMaxY();
		return getRectangleOutline2d(channels.getLocation(),new Point(highX,highY));
	}	
	
	/**
	 * Returns the rectangle that includes the box of plot 
	 * coordinates indicated by the given plot coordinates.
	 * 
	 * @param p1 in plot coordinates
	 * @param p2 in plot coordinates
	 * @return in graphics coordinates
	 */
	Rectangle getRectangleOutline2d(Point p1, Point p2){
		final int x1=p1.x;
		final int y1=p1.y;
		final int x2=p2.x;
		final int y2=p2.y;
		final int x, y;
		final int width, height;
		
		/* Horizontal */
		if (x1<x2) {
			final int xv1 = toViewHorzLin(x1);
			/* pixel before next channel */
			final int xv2 = toViewHorzLin(x2+1)-1;			
			x=xv1+1;
			width=xv2-xv1;
		} else if (x1>x2){
			/* pixel before next channel */
			final int xv1 = toViewHorzLin(x1+1)-1;
			final int xv2 = toViewHorzLin(x2);			
			x=xv2+1;
			width=xv1-xv2;			
		} else{//same horizontal
			final int xv1 = toViewHorzLin(x1);
			//pixel before next channel
			final int xv2 = toViewHorzLin(x2+1)-1;			
			x=xv1;
			/* At least 1 wide */
			width=Math.max(xv2-xv1,1);
		}	
		/* Vertical (y view starts at top right corner) */
		if (y2<y1) {
			final int yv1 = toViewVertLin(y1+1);
			/* pixel before next channel */
			final int yv2 = toViewVertLin(y2)-1;			
			y=yv1+1;
			height=yv2-yv1;
		}else if (y1<y2){
			/* pixel before next channel */
			final int yv1 = toViewVertLin(y1)-1;
			final int yv2 = toViewVertLin(y2+1);			
			y=yv2+1;
			height=yv1-yv2;
		} else{//same vertical
			final int yv1 = toViewVertLin(y1);
			/* pixel before next channel */
			final int yv2 = toViewVertLin(y2+1);			
			y=yv2;
			/* At least 1 tall */
			height=Math.max(yv1-yv2,1);
		} 		
		return new Rectangle(x,y,width,height);					
	}
	
	/**
	 * Mark an area whose corners are indicated by the given points.
	 * 
	 * @param p1 in plot coordinates
	 * @param p2 in plot coordinates
	 */
	void markArea2dOutline(Point p1, Point p2){
		clipPlot();				
		g.draw(getRectangleOutline2d(p1,p2));		
	}
	
	/**
	 * Setting a 2d Gate
	 *
	 * @param gatePoints the points of the gate to be drawn
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void settingGate2d(Polygon gatePoints) {
		clipPlot();
		final Polygon p=toView(gatePoints);
		g.drawPolyline(p.xpoints,p.ypoints,p.npoints);
	}
	
	Polygon toView(Polygon p){
		final int n=p.npoints;
		final int [] x=new int[n];
		final int [] y=new int[n];
		for (int i=0; i<n; i++){
			x[i]=toViewHorzLin(p.xpoints[i]);
			y[i]=toViewVertLin(p.ypoints[i]);
		}
		return new Polygon(x,y,n);
	}
	
	/**
	 * Mark a channel
	 * for 1d
	 */
	void markChannel1d(int channel, double count) {
		int y2;

		int x1 = toViewHorzLin(channel + 0.5);
		int y1 = viewBottom;
		int x2 = x1;
		if (plotLimits.getScale() == Limits.ScaleType.LINEAR) {
			y2 = toViewVertLinCk(count);
		} else {
			y2 = toViewVertLog(count);
		}
		//draw the line at least a mark min length
		if ((y1 - y2) < MARK_MIN_LENGTH) {
			y2 = viewBottom - MARK_MIN_LENGTH;
		}
		//are we inside the plot area
		if (x1 >= viewLeft && x1 <= viewRight) {
			g.drawLine(x1, y1, x2, y2);
			String label = "" + channel;
			g.drawString(label, x2, y2 - MARK_OFFSET);
		}
	}

	void drawPeakLabels(double[][] peaks) {
		int y1; //bottom of line
		Color initColor = g.getColor();
		setFont(font.deriveFont(PlotGraphicsLayout.SCREEN_FONT_SIZE));
		g.setColor(PlotColorMap.peakLabel);
		for (int i = 0; i < peaks[0].length; i++) {
			int x1 = toViewHorzLin(peaks[0][i] + 0.5);
			int x2 = x1;
			if (plotLimits.getScale() == Limits.ScaleType.LINEAR) {
				y1 = toViewVertLinCk(peaks[2][i]) - 3;
			} else {
				y1 = toViewVertLog(peaks[2][i]) - 3;
			}
			int y2 = y1 - 7; //top of line
			//are we inside the plot area?
			if (x1 >= viewLeft && x1 <= viewRight) {
				g.drawLine(x1, y1, x2, y2);
				String label = Integer.toString((int) Math.round(peaks[1][i]));
				g.drawString(label, x2, y2 - 2);
			}
		}
		g.setColor(initColor);
	}

	/**
	 * Mark a channel
	 * for 2d
	 */
	void markChannel2d(Point p) {
		final Rectangle r=getRectangleOutline2d(p,p);
		final String label = "" + p.x + "," + p.y;
		g.draw(r);
		g.drawString(label, (int)r.getMaxX()+MARK_OFFSET, r.y - MARK_OFFSET);		
	}

	/**
	 * Mark an area in a 1 d plot
	 * @lowChan  lower channel
	 * @highChan upper channel
	 *
	 */
	void markArea1d(int lowChan, int highChan, double[] counts) {
		int minChan = Math.max(minXch, lowChan);
		int maxChan = Math.min(maxXch, highChan);
		final Polygon fill=new Polygon();
		final boolean log=plotLimits.getScale() != Limits.ScaleType.LINEAR;
		int xi=toViewHorzLin(minChan);
		int yi=log ? toViewVertLog(0) : toViewVertLinCk(0);
		fill.addPoint(xi,yi);
		int lastx=xi;
		int lasty=yi;
		int x=xi;
		int y=yi;
		/* vertical traverse, followed by horizontal */
		for (int i = minChan; i <= maxChan; i++) {
			y = log ? toViewVertLog(counts[i]) : toViewVertLinCk(counts[i]);
			if (y != lasty){
				fill.addPoint(x,y);
				lasty=y;	
			}
			x=Math.min(viewRight,toViewHorzLin(i+1));
			if (!(x == lastx && y==lasty)){
				fill.addPoint(x,y);
				lastx=x;
			}
		}	
		if (y != yi) {//go back to bottom on last
			y=yi;
			fill.addPoint(x,y);
		}
		g.fill(fill);
	}

	/**
	 * Mark an area in a 2d plot.
	 * 
	 * @param r rectangle in graphics coordinates
	 */
	void markArea2d(Rectangle r) {
		clipPlot();
		g.fill(r);
	}
	
	/**
	 * Draw a line in data co-ordinates
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	void drawDataLine(int x1, int y1, int x2, int y2) {
		g.drawLine(
			toViewHorzLin(x1),
			toViewVertLin(y1),
			toViewHorzLin(x2),
			toViewVertLin(y2));
	}

	/**
	 *  Convert to data corodinates, given a screeen point.
	 *  These routines do not have to be as fast as the to view ones,
	 *  as it is not very often we want to go this way.
	 *  The ones that return int are faster.
	 */
	synchronized Point toData(Point viewPoint) {
		return new Point(toDataHorz(viewPoint.x), toDataVert(viewPoint.y));
	}

	/**
	 * Give the horizontal plot coordinate for the given graphics
	 * horizontal coodinate.
	 */
	synchronized int toDataHorz(int view) {
		final int data;
		/* if we are beyond limits set point to limit */
		if (view < viewLeft) {
			data = minXch;
		} else if (view >= viewRight) {
			data = maxXch;
		} else {
			data = (int) (minXch + (view - viewLeft) / conversionX);
		}
		return data;
	}

	/**
	 * Give the vertical plot coordinate for the given graphics
	 * vertical coordinate.
	 */
	synchronized int toDataVert(int view) {
		final int data;
		/* if we are beyond limits set point to limit */
		if (view < viewTop) {
			data = maxY;
		} else if (view > viewBottom) {
			data = minY;
		} else {
			data = (int) (minY + (viewBottom - view) / conversionY);
		}
		return data;
	}

	/**
	 * Convert data point to view point
	 */
	public Point toViewLin(Point dataPoint) {
		Point viewPoint =
			new Point(toViewHorzLin(dataPoint.x), toViewVertLin(dataPoint.y));
		return (viewPoint);
	}

	/**
	 * Get the middle point of the plot
	 * usefull for drawing title and labels
	 */
	private Point viewMiddle() {
		viewMiddle.x = viewLeft + viewWidth / 2;
		viewMiddle.y = viewTop + viewHeight / 2;
		return (viewMiddle);
	}

	/**
	 * Clip so only active region of plot is drawn on.
	 */
	void clipPlot() {
		g.clipRect(viewLeft, viewTop, viewWidth + 1, viewHeight + 1);
	}

	/**
	 * Convert horizontal channel coordinate to the graphics 
	 * coordinate which represents the "low" (left) side of the bin.
	 */
	private int toViewHorzLin(double data) {
		int view = (int) (viewLeft + (conversionX * (data - minXch)));
		return view;
	}

	/**
	 * Convert vertical channel coordinate to the graphics 
	 * coordinate which represents the "low" (bottom) side of the
	 * bin.
	 */
	private int toViewVertLin(double data) {
		final int view = (int) (viewBottom - (conversionY * (data - minY)));
		return view;
	}

	/**
	 * Convert vertical data to vertical view (screen)
	 * screen vertical linear scale.
	 */
	private int toViewVertLinCk(double data) {
		final int view;

		if (data > maxY) {
			view = viewTop;
		} else if (data < minY) {
			view = viewBottom;
		} else {
			view = (int) (viewBottom - (conversionY * (data - minY)));
		}
		return view;
	}

	/**
	 * Convert data vertical to view vertical
	 * for Log scale
	 */
	private int toViewVertLog(double data) {
		final int view;
		
		final double dataLog = takeLog(data);
		if (dataLog > maxYLog) {
			view = viewTop;
		} else if (dataLog < minYLog) {
			view = viewBottom;
		} else {
			view = viewBottom - (int) (conversionYLog * (dataLog - minYLog));
		}
		return view;
	}

	/**
	 * Take the log of a data point if valid to otherwise return fake zero
	 * @param point point to take log of
	 */
	private double takeLog(int point) {
		if (point > 0) {
			return (Math.log((double) point));
		} else {
			return (Math.log(LOG_FAKE_ZERO));
		}
	}

	/**
	 * Take the log of a data point if valid to otherwise return fake zero
	 * @param point point to take log of
	 */
	private double takeLog(double point) {
		if (point > 0.0) {
			return (Math.log(point));
		} else {
			return (Math.log(LOG_FAKE_ZERO));
		}
	}
}
