package jam.plot;

import static jam.plot.Constants.BOTTOM;
import static jam.plot.Constants.LEFT;
import static jam.plot.Constants.LOG_FAKE_ZERO;
import static jam.plot.Constants.TOP;
import jam.plot.color.ColorScale;
import jam.plot.color.DiscreteColorScale;
import jam.plot.color.GradientColorScale;
import jam.plot.color.PlotColorMap;

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
import java.awt.geom.GeneralPath;
import java.awt.print.PageFormat;

import javax.swing.JOptionPane;

/**
 * Class of a library of methods to draw things for a graph. First the update
 * method is called to set the limits for the plot and to give it a handle to
 * the graphics object.
 * 
 * <p>
 * The screen origin is upper right hand corner while the data point origin is
 * lower left hand corner. There are private methods that map screen pixels to
 * data co-ordinates and other public methods that class to map data points to
 * screen pixels.
 * <p>
 * The border size is the area around the plot the outline the plot is part of
 * the plot, the outline is part not part of the border. Therefore channels with
 * zero counts will therefore draw along the lower border, and the left and
 * right border will be part of a channel.
 * 
 * The method getSize() for <code>Component</code> returns the number of
 * pixels which are labeled starting with 0, to getSize()-1
 * 
 * @version 0.5 April 98, May 99
 * @author Ken Swartz
 * @see java.awt.Graphics
 * @since JDK1.1
 */
final class PlotGraphics {

	private static final PlotColorMap COLOR_MAP = PlotColorMap.getInstance();

	private transient PlotGraphicsLayout graphLayout;

	/* current stuff to draw font, and font metrics and colors */
	private transient Graphics2D graphics2d;

	private transient Font font;

	private transient FontMetrics metrics;

	private transient final Tickmarks tickmarks;

	/**
	 * Border for plot in pixels
	 */
	private transient Insets border;

	/** The Limits in channel and scale of the plot */
	private transient Limits plotLimits;

	/** is the plot 1d or 2d */
	private transient final int plotDimensions;

	/**
	 * variable for converting pixels to data and data to pixels
	 */
	private transient int minXch; // minimum horizontal for data

	private transient int maxXch; // maximum horizontal for data

	/** limits in Y, counts (1d) or channels (2d) */
	private transient int minY;

	private transient int maxY;

	private transient double minYLog;

	private transient double maxYLog;

	private transient int minCount;

	private transient int maxCount;

	/** number of pixels per channel */
	private transient double conversionX;

	/** number of pixels per channel */
	private transient double conversionY;

	/** number of pixels per channel */
	private transient double conversionYLog;

	/** the dimensions of the plot canvas */
	private transient Dimension viewSize;

	/** sides of plot in pixels */
	private transient int viewLeft; // left hand side of plot area

	private transient int viewRight; // right hand side of plot area

	private transient int viewTop; // top side of plot area

	private transient int viewBottom; // bottom side of plot area

	private transient int viewWidth; // width of plot area

	private transient int viewHeight; // height of plot area

	private transient final Point middle; // middle of plot area

	// private Font printFont; //Printing Font

	/**
	 * Full constructor, all contructors eventually call this one. Other
	 * constructors have defaults.
	 * 
	 * @param plot
	 *            the plot this belongs to
	 */
	PlotGraphics(AbstractPlot plot) {
		graphLayout = PlotGraphicsLayout.LABELS;
		/* class that draws tick marks and makes color thresholds */
		tickmarks = new Tickmarks();
		/* margin for printing */
		/* maybe should be avaliable in constructor, middle of plot */
		middle = new Point();
		if (plot instanceof Plot1d) {
			plotDimensions = 1;
		} else {// Plot2d
			plotDimensions = 2;
		}
		setLayout(PlotGraphicsLayout.Type.WITH_LABELS);
	}

	/**
	 * Set the layout type
	 * 
	 * @param type
	 */
	void setLayout(final PlotGraphicsLayout.Type type) {
		graphLayout = PlotGraphicsLayout.getLayout(type);
		/* some initial layout stuff */
		border = new Insets(graphLayout.border.top, graphLayout.border.left,
				graphLayout.border.bottom, graphLayout.border.right);
		final Font screenFont = new Font(PlotGraphics.FONT_CLASS,
				Font.BOLD, (int) PlotGraphicsLayout.SCREEN_FONT_SIZE);
		setGraphicsFont(screenFont);
	}

	/*
	 * non-javadoc: Set the font used on the plot.
	 */
	void setGraphicsFont(final Font newFont) {
		synchronized (monitor) {
			font = newFont;
			if (graphics2d != null) {
				graphics2d.setFont(newFont);
				metrics = graphics2d.getFontMetrics();
			}
		}
	}

	private transient PageFormat pageformat = null;

	private transient final Object monitor = new Object();

	void setView(final PageFormat format) {
		synchronized (monitor) {
			pageformat = format;
			if (format != null) {
				viewSize = new Dimension((int) format.getImageableWidth(),
						(int) format.getImageableHeight());
			}
		}
	}

	private transient final Object limitsLock = new Object();

	private final static String FONT_CLASS = "Serif";

	private final static float TITLE_SCREEN_SIZE=PlotGraphicsLayout.SCREEN_FONT_SIZE + 2;

	private final static transient int MARK_OFFSET=3;

	// stuff for channel marker
	private final static transient int MARK_MIN_LENGTH=20;

	/**
	 * updates the current display parameters the most basic update this one
	 * must always be called
	 * 
	 * @param graph
	 *            the Graphics object
	 * @param newViewSize
	 *            the viewable size of the canvas in pixels
	 * @param limits
	 *            the limits of the plot
	 * @param f
	 *            the font for labels
	 * 
	 * @since Version 0.5
	 */
	void update(final Graphics graph, final Dimension newViewSize,
			final Limits limits) {
		update(graph); // get graphics and copy to local variables
		synchronized (limitsLock) {
			plotLimits = limits;
			if (plotLimits != null) {
				/* retrieve imformation from plotLimits object */
				minCount = plotLimits.getMinimumCounts();
				maxCount = plotLimits.getMaximumCounts();
				if (plotDimensions == 1) {
					minXch = plotLimits.getMinimumX();
					maxXch = plotLimits.getMaximumX();
					minY = plotLimits.getMinimumCounts();
					maxY = plotLimits.getMaximumCounts();
				} else if (plotDimensions == 2) {
					minXch = plotLimits.getMinimumX();
					maxXch = plotLimits.getMaximumX();
					minY = plotLimits.getMinimumY();
					maxY = plotLimits.getMaximumY();
				}
				minYLog = takeLog(minY);
				maxYLog = takeLog(maxY);
				final int rangeXch = maxXch - minXch + 1;
				final int rangeY = maxY - minY + 1;
				final double rangeYLog = maxYLog - minYLog;
				if (pageformat == null) {
					this.viewSize = newViewSize;
				}
				/* plot* are the borders and are part of the plot */
				viewLeft = border.left; // really 0+border.left
				viewTop = border.top; // really 0+border.top
				viewRight = viewSize.width - border.right - 1;
				/* subtract 1 as last pixel size-1 */
				viewBottom = viewSize.height - border.bottom - 1;
				/* subtract 1 as last pixel size-1 */
				viewWidth = viewRight - viewLeft + 1;
				/* add 1 as border part of plot */
				viewHeight = viewBottom - viewTop + 1;
				/* add 1 as border part of plot */
				conversionX = (double) viewWidth / ((double) rangeXch);
				/* number of pixels per channel */
				conversionY = (double) viewHeight / ((double) rangeY);
				/* number of pixels per channel */
				conversionYLog = viewHeight / rangeYLog;
			}
		}
	}

	private static final RenderingHints HINTS = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	static {
		HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	/**
	 * Update just the graphics object
	 * 
	 * @param graph
	 *            grapics object
	 */
	private void update(final Graphics graph) {
		graphics2d = (Graphics2D) graph;
		graphics2d.setRenderingHints(HINTS);
		if (metrics == null) {
			setGraphicsFont(font);
		}
	}

	/**
	 * Draws the title for a plot.
	 * 
	 * @param title
	 *            the title
	 * @param side
	 *            the side on which to draw the title
	 * @since Version 0.5
	 */
	void drawTitle(final String title, final int side) {
		int offset = 1;
		int xPos;
		int yPos;
		offset = metrics.stringWidth(title);
		if (graphLayout == PlotGraphicsLayout.LABELS) {
			xPos = viewMiddle().x - offset / 2;
		} else {
			xPos = viewLeft + graphLayout.titleOffsets.left;
		}
		yPos = viewTop - graphLayout.titleOffsets.top;
		if (side == TOP) {
			setGraphicsFont(font
					.deriveFont(PlotGraphics.TITLE_SCREEN_SIZE));
			graphics2d.drawString(title, xPos, yPos);
			setGraphicsFont(font
					.deriveFont(PlotGraphicsLayout.SCREEN_FONT_SIZE));
		}
	}

	void drawNumber(final int number, final int[] overlayNumbers) {
		final String string = Integer.toString(number);
		setGraphicsFont(font);
		int width = metrics.stringWidth(string);
		int xNext = this.viewLeft - graphLayout.titleOffsets.top - width;
		final int yVal = viewTop - graphLayout.titleOffsets.top;
		final Color color = graphics2d.getColor();
		graphics2d.setColor(COLOR_MAP.getForeground());
		graphics2d.drawString(string, xNext, yVal);
		for (int i = 0; i < overlayNumbers.length; i++) {
			xNext += width;
			final String sNext = ", " + overlayNumbers[i];
			width = metrics.stringWidth(sNext);
			graphics2d.setColor(COLOR_MAP.getOverlay(i));
			graphics2d.drawString(sNext, xNext, yVal);
		}
		graphics2d.setColor(color);
	}

	/*
	 * non-javadoc: Draws the date in the upper right hand corner
	 * 
	 * @since Version 0.5
	 */
	void drawDate(final String sdate) {
		final int xCoord = viewRight - metrics.stringWidth(sdate); // position
		// of
		// string
		final int yCoord = viewTop - graphLayout.titleOffsets.date;
		graphics2d.drawString(sdate, xCoord, yCoord);
	}

	/*
	 * non-javadoc: Draws the run number in the upper left hand corner
	 * 
	 * @since Version 0.5
	 */
	void drawRun(final int runNumber) {
		final String runLabel = "Run " + runNumber;
		final int xCoordinate = viewLeft;
		final int yCoordinate = viewTop - graphLayout.titleOffsets.date;
		graphics2d.drawString(runLabel, xCoordinate, yCoordinate);
	}

	/**
	 * Draws the border around the plot As plotSize() returns the the size of
	 * the plot including the borders we have to subtract one from the width and
	 * height. as drawRect(x,y,dx,dy) draws at x, y and at x+dx, y+dy.
	 * 
	 * @since Version 0.5
	 */
	void drawBorder() {
		graphics2d.drawRect(viewLeft, viewTop, viewWidth - 1, viewHeight - 1);
	}

	/**
	 * Draws the tickmarks on for a plot
	 * 
	 * @param side
	 * @since Version 0.5
	 */
	void drawTicks(final int side) {
		Scale scale = Scale.LINEAR;
		if (side == BOTTOM) {// always linear
			ticksBottom(minXch, maxXch);
		} else { // side==LEFT, if 1d-depends on Limits's scale
			synchronized (limitsLock) {
				if (plotDimensions == 1 && plotLimits != null) {
					scale = plotLimits.getScale();
				}
				ticksLeft(minY, maxY, scale);
			}
		}
	}

	/**
	 * Draws the tickmarks on the bottom side, X
	 * 
	 * @param lowerLimit
	 * @param upperLimit
	 * @param scale
	 * @since Version 0.5
	 */
	private void ticksBottom(final int lowerLimit, final int upperLimit) {
		final Scale scale = Scale.LINEAR;
		final int[] ticks = tickmarks.getTicks(lowerLimit, upperLimit, scale,
				Tickmarks.Type.MINOR);
		for (int i = 0; i < ticks.length; i++) {
			final int xOrigin = toViewHorzLin(ticks[i]);
			int bottom = viewBottom;
			graphics2d.drawLine(xOrigin, bottom, xOrigin, bottom
					- graphLayout.tick.minor);
			bottom = viewTop;
			graphics2d.drawLine(xOrigin, bottom, xOrigin, bottom
					+ graphLayout.tick.minor);
		}
		final int[] ticksMajor = tickmarks.getTicks(lowerLimit, upperLimit,
				scale, Tickmarks.Type.MAJOR);
		for (int i = 0; i < ticksMajor.length; i++) {
			final int xOrigin = toViewHorzLin(ticksMajor[i]);
			int bottom = viewBottom;
			graphics2d.drawLine(xOrigin, bottom, xOrigin, bottom
					- graphLayout.tick.major);
			bottom = viewTop;
			graphics2d.drawLine(xOrigin, bottom, xOrigin, bottom
					+ graphLayout.tick.major);
		}
	}

	/**
	 * Draws the tickmarks on for the left side, Y
	 * 
	 * @param lowerLimit
	 * @param upperLimit
	 * @param scale
	 * @since Version 0.5
	 */
	private void ticksLeft(final int lowerLimit, final int upperLimit,
			final Scale scale) {
		int xCoordinate;
		int yCoordinate;

		final int[] ticks = tickmarks.getTicks(lowerLimit, upperLimit, scale,
				Tickmarks.Type.MINOR);
		for (int i = 0; i < ticks.length; i++) {
			if (scale == Scale.LINEAR) {
				yCoordinate = toViewVertLin(ticks[i]);
			} else {
				yCoordinate = toViewVertLog(ticks[i]);
			}
			xCoordinate = viewLeft;
			graphics2d.drawLine(xCoordinate, yCoordinate, xCoordinate
					+ graphLayout.tick.minor, yCoordinate);
			xCoordinate = viewRight;
			graphics2d.drawLine(xCoordinate, yCoordinate, xCoordinate
					- graphLayout.tick.minor, yCoordinate);
		}
		final int[] ticksMajor = tickmarks.getTicks(lowerLimit, upperLimit,
				scale, Tickmarks.Type.MAJOR);
		for (int i = 0; i < ticksMajor.length; i++) {
			if (scale == Scale.LINEAR) {
				yCoordinate = toViewVertLin(ticksMajor[i]);
			} else {
				yCoordinate = toViewVertLog(ticksMajor[i]);
			}
			xCoordinate = viewLeft;
			graphics2d.drawLine(xCoordinate, yCoordinate, xCoordinate
					+ graphLayout.tick.major, yCoordinate);
			xCoordinate = viewRight;
			graphics2d.drawLine(xCoordinate, yCoordinate, xCoordinate
					- graphLayout.tick.major, yCoordinate);
		}
	}

	/**
	 * Draws the tick mark Labels on for a plot
	 * 
	 * @param side
	 * @since Version 0.5
	 */
	void drawLabels(final int side) {
		if (side == BOTTOM) {
			labelsBottom(minXch, maxXch);
		}
		synchronized (limitsLock) {
			if (plotDimensions == 1 && side == LEFT && plotLimits != null) {
				labelsLeft(minY, maxY, plotLimits.getScale());
			} else if (plotDimensions == 2 && side == LEFT) {
				labelsLeft(minY, maxY, Scale.LINEAR);
			}
		}
	}

	/*
	 * non-javadoc: Draws the Labels on for the bottom side of a plot
	 * 
	 * @param side
	 * 
	 * @since Version 0.5
	 */
	private void labelsBottom(final int lowerLimit, final int upperLimit) {
		final Scale scale = Scale.LINEAR;
		final int[] ticksMajor = tickmarks.getTicks(lowerLimit, upperLimit,
				scale, Tickmarks.Type.MAJOR);
		for (int i = 0; i < ticksMajor.length; i++) {
			final String label = Integer.toString(ticksMajor[i]);
			final int offset = metrics.stringWidth(label); // length of string
			final int xCoordinate = toViewHorzLin(ticksMajor[i]) - offset / 2;
			final int yCoordinate = viewBottom + metrics.getAscent()
					+ graphLayout.labelOffsets.bottom;
			graphics2d.drawString(label, xCoordinate, yCoordinate);
		}
	}

	/*
	 * non-javadoc: Draws the Labels on for the left side of a plot
	 * 
	 * @param side
	 * 
	 * @since Version 0.5
	 */
	private void labelsLeft(final int lowerLimit, final int upperLimit,
			final Scale scale) {
		final int[] ticksMajor = tickmarks.getTicks(lowerLimit, upperLimit,
				scale, Tickmarks.Type.MAJOR);
		for (int i = 0; i < ticksMajor.length; i++) {
			final String label = Integer.toString(ticksMajor[i]);
			final int offset = metrics.stringWidth(label);
			int yCoordinate = metrics.getAscent() / 2;
			if (scale == Scale.LINEAR) {
				yCoordinate += toViewVertLin(ticksMajor[i]);
			} else {
				yCoordinate += toViewVertLog(ticksMajor[i]);
			}
			final int xCoordinate = viewLeft - offset
					- graphLayout.labelOffsets.left;
			graphics2d.drawString(label, xCoordinate, yCoordinate);
		}
	}

	/*
	 * non-javadoc: Draws the axis Labels on for a plot
	 * 
	 * @param side
	 * 
	 * @since Version 0.5
	 */
	void drawAxisLabel(final String label, final int side) {
		if (side == BOTTOM) {
			axisLabelBottom(label);
		}
		if (side == LEFT) {
			axisLabelLeft(label);
		}
	}

	/*
	 * non-javadoc: Draws the axis Labels on for the bottom side of a plot
	 * 
	 * @param side
	 * 
	 * @since Version 0.5
	 */
	private void axisLabelBottom(final String label) {
		final int offset = metrics.stringWidth(label);
		final int xCoordinate = viewMiddle().x - offset / 2;
		final int yCoordinate = viewBottom + metrics.getAscent()
				+ graphLayout.axisLabelOffsets.bottom;
		graphics2d.drawString(label, xCoordinate, yCoordinate);
	}

	/*
	 * non-javadoc: Draws the axis Labels on for the left side of a plot
	 * 
	 * @param side
	 * 
	 * @since Version 0.5
	 */
	private void axisLabelLeft(final String label) {
		final double ninetyDeg = -Math.PI * 0.5;
		final int offset = metrics.stringWidth(label);
		final int yCoordinate = viewMiddle().y + offset / 2;
		final int xCoordinate = viewLeft - graphLayout.axisLabelOffsets.left;
		final AffineTransform original = graphics2d.getTransform();
		graphics2d.translate(xCoordinate, yCoordinate);
		graphics2d.rotate(ninetyDeg);
		graphics2d.drawString(label, 0, 0);
		graphics2d.setTransform(original);
	}

	/*
	 * non-javadoc: Histogram a plot with double count array
	 * 
	 * @param side
	 * 
	 * @since Version 0.5
	 */
	void drawHist(final double[] counts, final double binWidth) {
		final Scale scale = plotLimits == null ? null : plotLimits.getScale();
		drawHist(counts, binWidth, scale != Scale.LINEAR);
	}

	/*
	 * non-javadoc: Plot a line graph
	 * 
	 * @param side
	 * 
	 * @since Version 0.5
	 */
	void drawLine(final double[] channel, final double[] countsdl) {
		if (plotLimits.getScale() == Scale.LINEAR) {
			drawLineLinear(channel, countsdl);
		} else {
			drawLineLog(channel, countsdl);
		}

	}

	/*
	 * non-javadoc: Draw a histogram for linear scale. Stair step, step up at
	 * channel beginning.
	 * 
	 * @param counts to draw
	 * 
	 * @since Version 0.5
	 */
	private void drawHist(final double[] counts, final double binWidth,
			final boolean log) {
		/* x's are in channels, y's are in counts */
		final GeneralPath path = new GeneralPath();
		final double[] drawCounts = getDrawCounts(counts, binWidth);
		final int dclen = drawCounts.length;
		final int lastBinAll = dclen - 1;
		final double lastBinAllLo = lastBinAll * binWidth;
		if (counts == null) {
			JOptionPane.showMessageDialog(null,
					"drawHistLinear() called with null array.", getClass()
							.getName(), JOptionPane.WARNING_MESSAGE);
		} else {
			final int upperX = (int) Math.min(maxXch + 1, lastBinAllLo);
			final int firstBin = (int) Math.floor(minXch / binWidth);
			final int lastBin = (int) Math.floor(upperX / binWidth);
			double binChLo = firstBin * binWidth;
			int xCoordinate = toViewHorzLin(minXch);
			int yCoordinate = viewBottom;
			double delCh = binWidth;
			if (binChLo < minXch) {
				delCh = binChLo + binWidth - minXch;
				binChLo = minXch;
			}
			path.moveTo(xCoordinate, yCoordinate);
			for (int i = firstBin; i <= lastBin; i++) {
				/* first a vertical line */
				yCoordinate = log ? toViewVertLog(drawCounts[i])
						: toViewVertLinCk(drawCounts[i]);
				path.lineTo(xCoordinate, yCoordinate);
				/* now horizontal across bin */
				binChLo += delCh;
				xCoordinate = Math.min(viewRight, toViewHorzLin(binChLo));
				path.lineTo(xCoordinate, yCoordinate);
				delCh = Math.min(binWidth, maxXch + 1 - binChLo);
			}
			// last vertical line
			if (xCoordinate < viewRight) {
				yCoordinate = viewBottom;
				path.lineTo(xCoordinate, yCoordinate);
			}
			graphics2d.draw(path);
		}
	}

	private static final double EPSILON = 0.001;

	private double[] getDrawCounts(final double[] counts, final double bin) {
		/* bin assumed >= 1.0 */
		final double one = 1.0;
		final int clen = counts.length;
		final int len = (int) Math.ceil(counts.length / bin);
		double[] rval = new double[len];
		if ((bin - 1.0) <= EPSILON) {
			rval = counts;
		} else {
			double remain = bin;
			int index = 0;
			for (int i = 0; i < clen; i++) {
				if (remain > one) {
					rval[index] += counts[i];
					remain -= one;
				} else {
					rval[index] += counts[i] * remain;
					if (index < len - 1) {
						index++;
					}
					rval[index] += counts[i] * (one - remain);
					remain += bin - one;
				}
			}
			for (int i = 0; i < len; i++) {
				rval[i] /= bin;
			}
		}
		return rval;
	}

	/**
	 * Plot a line graph. plot is lines segments
	 * 
	 * @param channel
	 * @param counts
	 * @since Version 0.5
	 */
	private void drawLineLinear(final double[] channel, final double[] counts) {
		final int lowerX = Math.max(this.minXch, (int) channel[0] + 1);
		final int upperX = Math.min(this.maxXch,
				(int) channel[channel.length - 1]);
		int xValue1 = toViewHorzLin(channel[0]);
		/* check dont go beyond border */
		int yValue1 = Math.max(toViewVertLin(counts[0]), viewTop);
		/* for each point draw from last line to next line */
		for (int i = 1; i < channel.length; i++) {
			final int xValue2 = toViewHorzLin(channel[i]);
			/* could go 1 pixel too far for last i */
			final int yValue2 = Math.max(toViewVertLin(counts[i]), viewTop);
			/* check we don't go beyond border */
			if ((channel[i] >= lowerX) && (channel[i] <= upperX)) {
				graphics2d.drawLine(xValue1, yValue1, xValue2, yValue2);
			}
			/* save start for next line segment */
			xValue1 = xValue2;
			yValue1 = yValue2;
		}
	}

	/*
	 * non-javadoc: Plot a line graph, Log scale
	 * 
	 * @param side
	 * 
	 * @since Version 0.5
	 */
	private void drawLineLog(final double[] channel, final double[] counts) {
		final int lowerX = Math.max(this.minXch, (int) channel[0] + 1);
		final int upperX = Math.min(this.maxXch,
				(int) channel[channel.length - 1]);
		int xValue1 = toViewHorzLin(channel[0]);
		int yValue1 = toViewVertLog(counts[0]);
		/* check we don't go beyond border */
		if (yValue1 < viewTop) {
			yValue1 = viewTop;
		}
		/* for each point draw from last line to next line */
		for (int i = 1; i < channel.length; i++) {
			final int xValue2 = toViewHorzLin(channel[i]);
			// could go 1 pixel too far for last i
			int yValue2 = toViewVertLog(counts[i]);
			// check dont go beyond border
			if (yValue2 < viewTop) {
				yValue2 = viewTop;
			}
			if ((channel[i] >= lowerX) && (channel[i] <= upperX)) {
				graphics2d.drawLine(xValue1, yValue1, xValue2, yValue2);
			}
			// save start for next line segment
			xValue1 = xValue2;
			yValue1 = yValue2;
		}
	}

	/**
	 * Draw scale for a 2d plot
	 * 
	 * @param colors
	 *            colors to use
	 * @since Version 0.5
	 */
	void drawScale2d(final DiscreteColorScale colors) {
		colors.setRange(minCount, maxCount);
		final int[] colorThresholds = colors.getColorThresholds();
		final int numberColors = colorThresholds.length;
		final int textHeight = (metrics.getAscent());
		/* lowest threshold for color to be drawn */
		String label = Integer.toString(minCount);
		graphics2d.drawString(label, viewRight + graphLayout.colorScale.offset
				+ graphLayout.colorScale.size
				+ graphLayout.colorScale.labelOffset, viewBottom + textHeight
				/ 2);
		for (int k = 0; k < numberColors; k++) {
			label = Integer.toString(colorThresholds[k]);
			graphics2d.drawString(label, viewRight
					+ graphLayout.colorScale.offset
					+ graphLayout.colorScale.size
					+ graphLayout.colorScale.labelOffset, viewBottom
					- graphLayout.colorScale.size - k
					* graphLayout.colorScale.size + textHeight / 2);
		}
		/* draw colors on side */
		for (int k = 0; k < numberColors; k++) {
			graphics2d.setColor(colors.getColorByIndex(k));
			graphics2d.fillRect(viewRight + graphLayout.colorScale.offset, // horizontal
					viewBottom - graphLayout.colorScale.size - k
							* graphLayout.colorScale.size, // vertical
					graphLayout.colorScale.size, graphLayout.colorScale.size); // size
		}
	}

	void drawScale2d() {
		final Scale scale = plotLimits.getScale();
		final ColorScale colors = GradientColorScale.getScale(scale);
		colors.setRange(minCount, maxCount);
		final int lowerLimit = minCount;
		final int upperLimit = maxCount;
		setGraphicsFont(font);
		final int textHeight = (metrics.getAscent());
		final DiscreteColorScale dcs = DiscreteColorScale.getScale(scale);
		dcs.setRange(lowerLimit, upperLimit);
		final int[] colorThresholds = dcs.getColorThresholds();
		final int numberColors = colorThresholds.length;
		/* lowest threshold for color to be drawn */
		String label = Integer.toString(lowerLimit);
		graphics2d.drawString(label, viewRight + graphLayout.colorScale.offset
				+ graphLayout.colorScale.size
				+ graphLayout.colorScale.labelOffset, viewBottom + textHeight
				/ 2);
		for (int k = 0; k < numberColors; k++) {
			label = Integer.toString(colorThresholds[k]);
			graphics2d.drawString(label, viewRight
					+ graphLayout.colorScale.offset
					+ graphLayout.colorScale.size
					+ graphLayout.colorScale.labelOffset, viewBottom
					- graphLayout.colorScale.size - k
					* graphLayout.colorScale.size + textHeight / 2);
		}
		/* draw colors on side */
		final int scaleHeight = numberColors * graphLayout.colorScale.size;
		final int xValue1 = viewRight + graphLayout.colorScale.offset;
		final int xValue2 = xValue1 + graphLayout.colorScale.size - 1;
		double level;
		final double lowEnd = Math.max(1.0, lowerLimit);
		final double highEnd = colorThresholds[numberColors - 1];
		for (int row = 0; row < scaleHeight; row++) {
			final int yValue = viewBottom - row;
			if (plotLimits.getScale() == Scale.LINEAR) {
				level = lowerLimit + row * (highEnd - lowEnd) / scaleHeight;
			} else { // log scale
				level = lowEnd
						* Math
								.pow(highEnd / lowEnd, (double) row
										/ scaleHeight);
			}
			graphics2d.setColor(colors.getColor(level));
			graphics2d.drawLine(xValue1, yValue, xValue2, yValue);
		}
	}

	/*
	 * non-javadoc: Draw a 2d plot.
	 * 
	 * @param counts the counts to be displayed @param colors the colors to use
	 * 
	 * @since Version 0.5
	 */
	void drawHist2d(final double[][] counts, final int minChanX,
			final int minChanY, final int maxChanX, final int maxChanY,
			final DiscreteColorScale colors) {
		// numberColors = colors.length;
		// colorThresholds = new int[numberColors];
		/*
		 * colorThresholds = tm.getColorThresholds(minCounts, maxCounts,
		 * numberColors, plotLimits.getScale());
		 */
		colors.setRange(minCount, maxCount);
		/* for each bin */
		for (int j = minChanY; j <= maxChanY; j++) {
			for (int i = minChanX; i <= maxChanX; i++) {
				final double count = counts[i][j];
				/* quickly check if above lower limit */
				if (count > minCount) {
					/*
					 * Constructing a color lookup array would be faster, but
					 * this seems to draw fast enough.
					 */
					final int xValue = toViewHorzLin(i);
					final int yValue = toViewVertLin(j);
					final int channelWidth = toViewHorzLin(i + 1) - xValue;
					final int channelHeight = yValue - toViewVertLin(j + 1);
					// paintChannel: for (int k = 0; k < numberColors; k++) {
					/* check for min counts first as these are most likely */
					// if (count <= colorThresholds[k]) {
					final Color paint = colors.getColor(count);
					graphics2d.setColor(paint);
					/* inline for speed */
					graphics2d.fillRect(xValue, yValue - channelHeight + 1,
							channelWidth, channelHeight);
					/*
					 * break paintChannel; } }
					 */
					/* go here on break. */
					/* check if greater than all thresholds */
					/*
					 * if (count > colorThresholds[numberColors - 1]) {
					 * g.setColor(colors[numberColors - 1]); g.fillRect(x, y -
					 * channelHeight + 1, channelWidth, channelHeight); }
					 */
					/* end of loop for each point */
				}
			}
		}
	}

	/*
	 * non-javadoc: Draw a 2d plot.
	 * 
	 * @param counts the counts to be displayed @param colors the colors to use
	 * 
	 * @since Version 0.5
	 */
	void drawHist2d(final double[][] counts, final int minChanX,
			final int minChanY, final int maxChanX, final int maxChanY) {
		final ColorScale colors = GradientColorScale.getScale(plotLimits
				.getScale());
		colors.setRange(minCount, maxCount);
		/* loop over channels */
		for (int j = minChanY; j <= maxChanY; j++) {
			for (int i = minChanX; i <= maxChanX; i++) {
				final double count = counts[i][j];
				/* quickly check above lower limit */
				if (count > minCount) {
					graphics2d.setColor(colors.getColor(count));
					/* inline for speed */
					final int xValue = toViewHorzLin(i);
					final int yValue = toViewVertLin(j);
					final int channelWidth = toViewHorzLin(i + 1) - xValue;
					final int channelHeight = yValue - toViewVertLin(j + 1);
					graphics2d.fillRect(xValue, yValue - channelHeight + 1,
							channelWidth, channelHeight);
				} // end of loop for each point
			}
		}
	}

	/*
	 * non-javadoc: Draw a 1d Gate
	 * 
	 * @param ll lower limit @param ul upper limit
	 * 
	 * @since Version 0.5
	 */
	void drawGate1d(final int lowerLimit, final int upperLimit,
			final boolean noFillMode) {
		clipPlot();
		final int xValue = toViewHorzLin(lowerLimit);
		final int xValue2 = Math.min(toViewHorzLin(upperLimit + 1), viewRight);
		final int width = xValue2 - xValue;
		final int height = viewBottom - viewTop;
		if (noFillMode) {
			graphics2d.drawRect(xValue, viewTop, width, height);
		} else {
			graphics2d.fillRect(xValue, viewTop, width, height);
		}
	}

	/**
	 * Draw a 2d Gate
	 * 
	 * @param gate
	 *            the array to be displayed
	 */
	void drawGate2d(final boolean[][] gate) {
		if (gate != null) {
			for (int j = minY; j <= maxY; j++) { // for each point
				for (int i = minXch; i <= maxXch; i++) {
					if (gate[i][j]) { // if inside gate
						final int xValue = toViewHorzLin(i);
						final int yValue = toViewVertLin(j);
						final int channelWidth = toViewHorzLin(i + 1) - xValue;
						final int channelHeight = yValue - toViewVertLin(j + 1);
						graphics2d.fillRect(xValue, yValue - channelHeight + 1,
								channelWidth, channelHeight);
					}
				}
			}// --end for each point
		}
		graphics2d.setPaintMode();
	}

	/**
	 * Setting a 1d Gate
	 * 
	 * @param gatePoints
	 *            gate points to be drawn (in graphics coordinates)
	 * @since Version 0.5
	 */
	void settingGate1d(final Polygon gatePoints) {
		clipPlot();
		if (gatePoints.npoints > 0) {
			final int xValue = gatePoints.xpoints[gatePoints.npoints - 1];
			if (gatePoints.npoints > 1) {
				markAreaOutline1d(
						toDataHorz(gatePoints.xpoints[gatePoints.npoints - 2]),
						toDataHorz(xValue));
			} else {
				graphics2d.drawLine(xValue, viewBottom, xValue, viewTop);
			}
		}
	}

	/**
	 * Determine the outline of an area in a 1d plot.
	 * 
	 * @param xValue1
	 *            a point in plot coordinates
	 * @param xValue2
	 *            a point in plot coordinates
	 * @return a rectangle in graphics coordinates that will highlight the
	 *         channels indicated
	 */
	Rectangle getRectangleOutline1d(final int xValue1, final int xValue2) {
		final int height = viewBottom - viewTop;// Full plot vertically
		final int tempX;
		final int width;

		if (xValue1 < xValue2) {
			final int xv1 = toViewHorzLin(xValue1);
			// pixel before next channel
			final int xv2 = toViewHorzLin(xValue2 + 1) - 1;
			tempX = xv1 + 1;
			width = xv2 - xv1;
		} else if (xValue1 > xValue2) {
			// pixel before next channel
			final int xv1 = toViewHorzLin(xValue1 + 1) - 1;
			final int xv2 = toViewHorzLin(xValue2);
			tempX = xv2 + 1;
			width = xv1 - xv2;
			// so both at the same point shows something
		} else {
			final int xv1 = toViewHorzLin(xValue1);
			// pixel before next channel
			final int xv2 = toViewHorzLin(xValue2 + 1) - 1;
			tempX = xv1;
			// At least 1 wide
			width = Math.max(xv2 - xv1, 1);
		}
		return new Rectangle(tempX, viewTop, width, height);
	}

	/**
	 * Mark the outline of an area in a 1d plot.
	 * 
	 * @param xValue1
	 *            a point in plot coordinates
	 * @param xValue2
	 *            a point in plot coordinates
	 */
	void markAreaOutline1d(final int xValue1, final int xValue2) {
		clipPlot();
		graphics2d.draw(getRectangleOutline1d(xValue1, xValue2));
	}

	/**
	 * Given a rectangle in plot coordinates, return the bounding rectangle in
	 * graphics coordinates.
	 * 
	 * @param channels
	 *            in plot coordinates
	 * @return in graphics coordinates
	 */
	Rectangle getRectangleOutline2d(final Rectangle channels) {
		final int highX = (int) channels.getMaxX();
		final int highY = (int) channels.getMaxY();
		return getRectangleOutline2d(Bin.create(channels.getLocation()), Bin
				.create(highX, highY));
	}

	/**
	 * Returns the rectangle that includes the box of plot coordinates indicated
	 * by the given plot coordinates.
	 * 
	 * @param bin1
	 *            in plot coordinates
	 * @param bin2
	 *            in plot coordinates
	 * @return in graphics coordinates
	 */
	Rectangle getRectangleOutline2d(final Bin bin1, final Bin bin2) {
		final int xValue1 = bin1.getX();
		final int yValue1 = bin1.getY();
		final int xValue2 = bin2.getX();
		final int yValue2 = bin2.getY();
		final int xTemp, yTemp;
		final int width, height;

		/* Horizontal */
		if (xValue1 < xValue2) {
			final int xv1 = toViewHorzLin(xValue1);
			/* pixel before next channel */
			final int xv2 = toViewHorzLin(xValue2 + 1) - 1;
			xTemp = xv1 + 1;
			width = xv2 - xv1;
		} else if (xValue1 > xValue2) {
			/* pixel before next channel */
			final int xv1 = toViewHorzLin(xValue1 + 1) - 1;
			final int xv2 = toViewHorzLin(xValue2);
			xTemp = xv2 + 1;
			width = xv1 - xv2;
		} else {// same horizontal
			final int xv1 = toViewHorzLin(xValue1);
			// pixel before next channel
			final int xv2 = toViewHorzLin(xValue2 + 1) - 1;
			xTemp = xv1;
			/* At least 1 wide */
			width = Math.max(xv2 - xv1, 1);
		}
		/* Vertical (y view starts at top right corner) */
		if (yValue2 < yValue1) {
			final int yv1 = toViewVertLin(yValue1 + 1);
			/* pixel before next channel */
			final int yv2 = toViewVertLin(yValue2) - 1;
			yTemp = yv1 + 1;
			height = yv2 - yv1;
		} else if (yValue1 < yValue2) {
			/* pixel before next channel */
			final int yv1 = toViewVertLin(yValue1) - 1;
			final int yv2 = toViewVertLin(yValue2 + 1);
			yTemp = yv2 + 1;
			height = yv1 - yv2;
		} else {// same vertical
			final int yv1 = toViewVertLin(yValue1);
			/* pixel before next channel */
			final int yv2 = toViewVertLin(yValue2 + 1);
			yTemp = yv2;
			/* At least 1 tall */
			height = Math.max(yv1 - yv2, 1);
		}
		return new Rectangle(xTemp, yTemp, width, height);
	}

	/**
	 * Mark an area whose corners are indicated by the given points.
	 * 
	 * @param bin1
	 *            in plot coordinates
	 * @param bin2
	 *            in plot coordinates
	 */
	void markArea2dOutline(final Bin bin1, final Bin bin2) {
		clipPlot();
		graphics2d.draw(getRectangleOutline2d(bin1, bin2));
	}

	/**
	 * Setting a 2d Gate
	 * 
	 * @param gatePoints
	 *            the points of the gate to be drawn
	 * @since Version 0.5
	 */
	void settingGate2d(final Polygon gatePoints) {
		clipPlot();
		final Polygon shape = toView(gatePoints);
		graphics2d.drawPolyline(shape.xpoints, shape.ypoints, shape.npoints);
	}

	Polygon toView(final Polygon shape) {
		final int nPoints = shape.npoints;
		final int[] xValues = new int[nPoints];
		final int[] yValues = new int[nPoints];
		for (int i = 0; i < nPoints; i++) {
			xValues[i] = toViewHorzLin(shape.xpoints[i]);
			yValues[i] = toViewVertLin(shape.ypoints[i]);
		}
		return new Polygon(xValues, yValues, nPoints);
	}

	/*
	 * non-javadoc: Mark a channel for 1d.
	 */
	void markChannel1d(final int channel, final double count) {
		int yValue2;

		final int xValue1 = toViewHorzLin(channel + 0.5);
		final int yValue1 = viewBottom;
		final int xValue2 = xValue1;
		if (plotLimits.getScale() == Scale.LINEAR) {
			yValue2 = toViewVertLinCk(count);
		} else {
			yValue2 = toViewVertLog(count);
		}
		// draw the line at least a mark min length
		if ((yValue1 - yValue2) < PlotGraphics.MARK_MIN_LENGTH) {
			yValue2 = viewBottom - PlotGraphics.MARK_MIN_LENGTH;
		}
		// are we inside the plot area
		if (xValue1 >= viewLeft && xValue1 <= viewRight) {
			graphics2d.drawLine(xValue1, yValue1, xValue2, yValue2);
			final String label = "" + channel;
			graphics2d.drawString(label, xValue2, yValue2
					- PlotGraphics.MARK_OFFSET);
		}
	}

	void drawPeakLabels(final double[][] peaks) {
		int yValue1; // bottom of line
		final Color initColor = graphics2d.getColor();
		setGraphicsFont(font.deriveFont(PlotGraphicsLayout.SCREEN_FONT_SIZE));
		graphics2d.setColor(COLOR_MAP.getPeakLabel());
		for (int i = 0; i < peaks[0].length; i++) {
			final int xValue1 = toViewHorzLin(peaks[0][i] + 0.5);
			if (plotLimits.getScale() == Scale.LINEAR) {
				yValue1 = toViewVertLinCk(peaks[2][i]) - 3;
			} else {
				yValue1 = toViewVertLog(peaks[2][i]) - 3;
			}
			final int yValue2 = yValue1 - 7; // top of line
			// are we inside the plot area?
			if (xValue1 >= viewLeft && xValue1 <= viewRight) {
				graphics2d.drawLine(xValue1, yValue1, xValue1, yValue2);
				final String label = Integer.toString((int) Math
						.round(peaks[1][i]));
				graphics2d.drawString(label, xValue1, yValue2 - 2);
			}
		}
		graphics2d.setColor(initColor);
	}

	/*
	 * non-javadoc: Mark a channel for 2d
	 */
	void markChannel2d(final Bin bin) {
		final Rectangle rectangle = getRectangleOutline2d(bin, bin);
		final String label = "" + bin.getX() + "," + bin.getY();
		graphics2d.draw(rectangle);
		graphics2d.drawString(label, (int) rectangle.getMaxX()
				+ PlotGraphics.MARK_OFFSET, rectangle.y
				- PlotGraphics.MARK_OFFSET);
	}

	/*
	 * non-javadoc: Mark an area in a 1 d plot
	 * 
	 * @lowChan lower channel @highChan upper channel
	 * 
	 */
	void markArea1d(final int lowChan, final int highChan, final double[] counts) {
		final int minChan = Math.max(minXch, lowChan);
		final int maxChan = Math.min(maxXch, highChan);
		final Polygon fill = new Polygon();
		final boolean log = plotLimits.getScale() != Scale.LINEAR;
		final int xInitial = toViewHorzLin(minChan);
		final int yInitial = log ? toViewVertLog(0) : toViewVertLinCk(0);
		fill.addPoint(xInitial, yInitial);
		int lastx = xInitial;
		int lasty = yInitial;
		int xValue = xInitial;
		int yValue = yInitial;
		/* vertical traverse, followed by horizontal */
		for (int i = minChan; i <= maxChan; i++) {
			yValue = log ? toViewVertLog(counts[i])
					: toViewVertLinCk(counts[i]);
			if (yValue != lasty) {
				fill.addPoint(xValue, yValue);
				lasty = yValue;
			}
			xValue = Math.min(viewRight, toViewHorzLin(i + 1));
			if (!(xValue == lastx && yValue == lasty)) {
				fill.addPoint(xValue, yValue);
				lastx = xValue;
			}
		}
		if (yValue != yInitial) {// go back to bottom on last
			yValue = yInitial;
			fill.addPoint(xValue, yValue);
		}
		graphics2d.fill(fill);
	}

	/**
	 * Mark an area in a 2d plot.
	 * 
	 * @param rectangle
	 *            rectangle in graphics coordinates
	 */
	void markArea2d(final Rectangle rectangle) {
		clipPlot();
		graphics2d.fill(rectangle);
	}

	/*
	 * non-javadoc: Draw a line in data co-ordinates
	 * 
	 * @param side
	 * 
	 * @since Version 0.5
	 */
	void drawDataLine(final int xValue1, final int yValue1, final int xValue2,
			final int yValue2) {
		graphics2d.drawLine(toViewHorzLin(xValue1), toViewVertLin(yValue1),
				toViewHorzLin(xValue2), toViewVertLin(yValue2));
	}

	/*
	 * non-javadoc: Convert to data corodinates, given a screeen point. These
	 * routines do not have to be as fast as the to view ones, as it is not very
	 * often we want to go this way. The ones that return int are faster.
	 */
	Bin toData(final Point viewPoint) {
		synchronized (monitor) {
			return Bin.create(toDataHorz(viewPoint.x), toDataVert(viewPoint.y));
		}
	}

	/*
	 * non-javadoc: Give the horizontal plot coordinate for the given graphics
	 * horizontal coodinate.
	 */
	int toDataHorz(final int view) {
		synchronized (monitor) {
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
	}

	/*
	 * non-javadoc: Give the vertical plot coordinate for the given graphics
	 * vertical coordinate.
	 */
	int toDataVert(final int view) {
		synchronized (monitor) {
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
	}

	/*
	 * non-javadoc: Convert data point to view point
	 */
	Point toViewLin(final Bin dataPoint) {
		final Point viewPoint = new Point(toViewHorzLin(dataPoint.getX()),
				toViewVertLin(dataPoint.getY()));
		return viewPoint;
	}

	/*
	 * non-javadoc: Get the middle point of the plot usefull for drawing title
	 * and labels
	 */
	private Point viewMiddle() {
		middle.x = viewLeft + viewWidth / 2;
		middle.y = viewTop + viewHeight / 2;
		return middle;
	}

	/**
	 * Clip so only active region of plot is drawn on.
	 */
	void clipPlot() {
		graphics2d.clipRect(viewLeft, viewTop, viewWidth + 1, viewHeight + 1);
	}

	/*
	 * non-javadoc: Convert horizontal channel coordinate to the graphics
	 * coordinate which represents the "low" (left) side of the bin.
	 */
	private int toViewHorzLin(final double data) {
		return (int) (viewLeft + (conversionX * (data - minXch)));
	}

	/*
	 * non-javadoc: Convert vertical channel coordinate to the graphics
	 * coordinate which represents the "low" (bottom) side of the bin.
	 */
	private int toViewVertLin(final double data) {
		return (int) (viewBottom - (conversionY * (data - minY)));
	}

	/*
	 * non-javadoc: Convert vertical data to vertical view (screen) screen
	 * vertical linear scale.
	 */
	private int toViewVertLinCk(final double data) {
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

	/*
	 * non-javadoc: Convert data vertical to view vertical for Log scale
	 */
	private int toViewVertLog(final double data) {
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

	/*
	 * non-javadoc: Take the log of a data point if valid to otherwise return
	 * fake zero
	 * 
	 * @param point point to take log of
	 */
	private double takeLog(final double point) {
		return Math.log(point > 0.0 ? point : LOG_FAKE_ZERO);
	}
}