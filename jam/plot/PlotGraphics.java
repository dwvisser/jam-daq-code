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
	private Color[] colorScale;
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
	public PlotGraphics(JPanel plot, Insets border, Font font) {
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
	 * contructors with default settings
	 * default font
	 */
	public PlotGraphics(JPanel plot, Insets border) {
		this(plot, border, (new Font("SansSerif", Font.PLAIN, 12)));
	}

	/**
	 * contructors with default settings
	 * default border
	 */
	public PlotGraphics(JPanel plot, Font font) {
		this(
			plot,
			new Insets(BORDER_TOP, BORDER_LEFT, BORDER_BOTTOM, BORDER_RIGHT),
			font);
	}

	/**
	 * Set the font used on the plot.
	 */
	public final synchronized void setFont(Font f) {
		font = f;
		if (g != null) {
			g.setFont(f);
			fm = g.getFontMetrics();
		}
	}

	private PageFormat pageformat=null;
	public synchronized void setView(PageFormat pf){
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
	public void update(
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
	public void update(Graphics graph) {
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
	public void drawTitle(String title, int side) {
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

	public void drawNumber(int number, int overlayNumber) {
		final String s = Integer.toString(number);
		setFont(font);
		final int width = fm.stringWidth(s);
		final int x = this.viewLeft - TITLE_OFFSET_TOP - width;
		final int y = viewTop - TITLE_OFFSET_TOP;
		final Color c = g.getColor();
		g.setColor(Color.BLACK);
		g.drawString(s, x, y);
		if (overlayNumber >= 0){
			final int x2=x+width;
			final StringBuffer s2=new StringBuffer(", ").append(overlayNumber);
			g.setColor(PlotColorMap.overlay);
			g.drawString(s2.toString(),x2,y);
		}
		g.setColor(c);
	}

	/**
	 * Draws the date in the upper right hand corner
	 *
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public void drawDate(String sdate) {
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
	public void drawRun(int runNumber) {
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
	public void drawBorder() {
		g.drawRect(viewLeft, viewTop, viewWidth - 1, viewHeight - 1);
	}

	/**
	 * Draws the tickmarks on for a plot
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public void drawTicks(int side) {
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
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public void drawLabels(int side) {
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
	public void drawAxisLabel(String label, int side) {
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
	 * Histogram a plot with int count array
	 *
	 * @param counts the counts to histogram
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public void drawHist(int[] counts, double binWidth) {
		final double[] countOut = new double[counts.length];
		for (int i = 0; i < counts.length; i++) {
			countOut[i] = (int) counts[i];
		}
		drawHist(countOut, binWidth);
	}

	/**
	 * Histogram a plot with double count array
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public void drawHist(double[] counts, double binWidth) {
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
	public void drawLine(double[] channel, double[] countsdl) {
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
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public void drawLineLinear(double[] channel, double[] counts) {
		int x2, y2;
		
		int lowerX = Math.max(this.minXch, (int) channel[0] + 1);
		int upperX = Math.min(this.maxXch, (int) channel[channel.length - 1]);
		int x1 = toViewHorzLin(channel[0]);
		/* check dont go beyond border */
		int y1 = Math.max(toViewVertLin(counts[0]),viewTop);
		/*if (y1 < viewTop) {
			y1 = viewTop;
		}*/
		//for each point draw from last line to next line
		for (int i = 1; i < channel.length; i++) {
			x2 = toViewHorzLin(channel[i]);
			//could go 1 pixel too far for last i
			y2 = Math.max(toViewVertLin(counts[i]),viewTop);
			//check dont go beyond border
			/*if (y2 < viewTop) {
				y2 = viewTop;
			}*/
			if ((channel[i] >= lowerX) && (channel[i] <= upperX)) {
				g.drawLine(x1, y1, x2, y2);
			}
			//save start for next line segment
			x1 = x2;
			y1 = y2;
		}
	}

	/**
	 * Plot a line graph,
	 * Log scale
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public void drawLineLog(double[] channel, double[] counts) {

		int lowerX = Math.max(this.minXch, (int) channel[0] + 1);
		int upperX = Math.min(this.maxXch, (int) channel[channel.length - 1]);
		int x1, x2;
		int y1, y2;

		x1 = toViewHorzLin(channel[0]);
		y1 = toViewVertLog(counts[0]);
		//check dont go beyond border
		if (y1 < viewTop) {
			y1 = viewTop;
		}

		//for each point draw from last line to next line
		for (int i = 1; i < channel.length; i++) {
			x2 = toViewHorzLin(channel[i]);
			//could go 1 pixel too far for last i
			y2 = toViewVertLog(counts[i]);
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
	public void drawScale2d(Color[] colors) {
		int lowerLimit = minCount;
		int upperLimit = maxCount;
		colorScale = colors;
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
			g.setColor(colorScale[k]);
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
	public void drawHist2d(double[][] counts, int minChanX, int minChanY,
	int maxChanX, int maxChanY, Color[] colors) {
		double count;
		int channelWidth, channelHeight;

		int x = 0;
		int y = 0;

		int minCounts = minCount;
		int maxCounts = maxCount;

		colorScale = colors;
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
				count = counts[i][j];
				//quickly check above lower limit
				if (count > minCounts) {
					//FIXME must be faster way then going trough all thresholds
					paintChannel : for (int k = 0; k < numberColors; k++) {
						//check for min counts first as these are most likely
						if (count <= colorThresholds[k]) {
							g.setColor(colorScale[k]);
							// inline for speed
							x = toViewHorzLin(i);
							y = toViewVertLin(j);
							channelWidth = toViewHorzLin(i + 1) - x;
							channelHeight = y - toViewVertLin(j + 1);
							g.fillRect(
								x,
								y - channelHeight + 1,
								channelWidth,
								channelHeight);
							//FIXME +1 hack fix, needed why
							break paintChannel;
						}
					}
					// go here on break;
					//check if greater than all thresholds
					if (count > colorThresholds[numberColors - 1]) {
						g.setColor(colorScale[numberColors - 1]);
						// inline for speed was drawChannel(i,j);
						x = toViewHorzLin(i);
						y = toViewVertLin(j);
						channelWidth = toViewHorzLin(i + 1) - x;
						channelHeight = y - toViewVertLin(j + 1);
						g.fillRect(
							x,
							y - channelHeight + 1,
							channelWidth,
							channelHeight);
						//FIXME +1 hack fix, needed why
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
	public void drawHist2d(double[][] counts, int minChanX, int minChanY,
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
	public void drawGate2d(boolean[][] gate) {
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
					//FIXME +1 needed why?
				}
				// --end for each point
			}
		}
		g.setPaintMode();
	}

	/**
	 * Setting a 1d Gate
	 *
	 * @param gatePoints the vector of gate points to be drawn
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public void settingGate1d(Polygon gatePoints) {
		clipPlot();
		for (int i = 0; i < gatePoints.npoints; i++) {
			final int x1 = gatePoints.xpoints[i];
			g.drawLine(x1, viewBottom, x1, viewTop);
		}
	}

	/**
	 * Setting a 2d Gate
	 *
	 * @param gatePoints the points of the gate to be drawn
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public void settingGate2d(Polygon gatePoints) {
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
	public void markChannel1d(int channel, double count) {
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
		g.setColor(Color.blue);
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
	public void markChannel2d(Point p) {
		final int x = toViewHorzLin(p.x);
		final int y = toViewVertLin(p.y);
		final int width = toViewHorzLin(p.x + 1) - x;
		final int height = y - toViewVertLin(p.y + 1);
		/*are we inside plot window? */
		if ((x >= viewLeft && x <= viewRight)
			&& (y >= viewTop && y <= viewBottom)) {
			g.drawRect(x, y - height + 1, width - 1, height - 1);
			//+1 need why?
			//draw label
			final String label = "" + p.x + "," + p.y;
			g.drawString(label, x + width, y - height - MARK_OFFSET);
		}
	}

	/**
	 * Mark an area in a 1 d plot
	 * @lowChan  lower channel
	 * @highChan upper channel
	 *
	 */
	public void markArea1d(int lowChan, int highChan, double[] counts) {
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
	 * @param lowChanX lower limit in X
	 * @param highChanX upper limit in X
	 * @param lowChanY lower limit in Y
	 * @param highChanY upper limit in Y
	 */
	public void markArea2d(
		Rectangle r) {
		g.fill(r);
	}
	
	Rectangle get2dAreaMark(
	int lowChanX,
	int highChanX,
	int lowChanY,
	int highChanY) {
		final int x1 = toViewHorzLin(lowChanX);
		final int x2 = toViewHorzLin(highChanX + 1);
		final int y1 = toViewVertLin(lowChanY);
		final int y2 = toViewVertLin(highChanY + 1);
		final int width = x2 - x1 - 1;
		final int height = y1 - y2 - 1;
		return new Rectangle(x1, y2 + 1, width, height);		
	}
	
	Rectangle get2dAreaMark(Rectangle channels){
		final int lowX=(int)channels.getMinX();
		final int lowY=(int)channels.getMinY();
		final int highX=(int)channels.getMaxX();
		final int highY=(int)channels.getMaxY();
		return get2dAreaMark(lowX,highX,lowY,highY);
	}

	/**
	 * Make a mark to middle of the plot
	 * for 1d
	 * @param channel channel to mark
	 */
	public void markMid(int channel) {
		final int x1 = toViewHorzLin(channel + 0.5);
		final int y1 = viewBottom;
		final int x2 = toViewHorzLin(channel + 0.5);
		final int y2 = viewMiddle.y;
		g.drawLine(x1, y1, x2, y2);
	}

	/**
	 * Draw a line in data co-ordinates
	 *
	 * @param side
	 * @return  <code>void</code>
	 * @since Version 0.5
	 */
	public void drawDataLine(int x1, int y1, int x2, int y2) {
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
	public synchronized Point toData(Point viewPoint) {
		Point dataPoint =
			new Point(toDataHorz(viewPoint.x), toDataVert(viewPoint.y));
		return (dataPoint);
	}

	/**
	 *  Convert screen point to data poin in horizonal
	 */
	public synchronized int toDataHorz(int view) {
		int data;
		//if we are beyond limits set point to limit
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
	 * Convert screen poin to data point in the vertical
	 */
	public synchronized int toDataVert(int view) {
		int data;
		//if we are beyond limits set point to limit
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
	public void clipPlot() {
		g.clipRect(viewLeft, viewTop, viewWidth + 1, viewHeight + 1);
	}

	/**
	 * Convert horizontal data to horizontal view point
	 * screen horizontal linear scale.
	 */
	private int toViewHorzLin(int data) {
		int view = (int) (viewLeft + (conversionX * (data - minXch)));
		//put at start of bin
		return view;
	}

	/**
	 * Convert horizontal data to horizontal view point
	 * screen horizontal linear scale.
	 */
	private int toViewHorzLin(double data) {
		int view = (int) (viewLeft + (conversionX * (data - minXch)));
		//put at start of bin
		return view;
	}

	/**
	 * Convert vertical data to vertical view (screen)
	 * screen vertical linear scale.
	 */
	private int toViewVertLin(int data) {
		final int view = (int) (viewBottom - (conversionY * (data - minY)));
		return view;
	}

	/**
	 *Convert vertical data to vertical view (screen)
	 *screen vertical linear scale
	 *
	 */
	private int toViewVertLin(double data) {
		final int view = (int) (viewBottom - (conversionY * (data - minY)));
		return view;
	}

	/**
	 * Convert vertical data to vertical view (screen)
	 * screen vertical linear scale.
	 */
	/*private int toViewVertLinCk(int data) {
		final int view;

		if (data > maxY) {
			view = viewTop;
		} else if (data < minY) {
			view = viewBottom;
		} else {
			view = (int) (viewBottom - (conversionY * (data - minY)));
		}
		return view;
	}*/

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
	/*private int toViewVertLog(int data) {
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
	}*/

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
