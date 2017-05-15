package jam.plot;

import jam.data.Dimensional;
import jam.plot.color.ColorScale;
import jam.plot.color.DiscreteColorScale;
import jam.plot.color.GradientColorScale;
import jam.plot.color.PlotColorMap;
import jam.plot.common.Scale;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.print.PageFormat;

import static javax.swing.SwingConstants.*;

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
 * The method getSize() for <code>Component</code> returns the number of pixels
 * which are labeled starting with 0, to getSize()-1
 * 
 * @version 0.5 April 98, May 99
 * @author Ken Swartz
 * @see java.awt.Graphics
 * @since JDK1.1
 */
final class Painter {

	private static final PlotColorMap COLOR_MAP = PlotColorMap.getInstance();

	private transient GraphicsLayout graphLayout;

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
	private transient Limits plotLimits = Limits.NULL;

	/** is the plot 1d or 2d */
	private transient final int plotDimensions;

	private transient Conversion conversion = new Conversion(1.0, 1.0, 1.0);

	/** the dimensions of the plot canvas */
	private transient Dimension viewSize;

	private transient final Point middle; // middle of plot area

	private transient final PlotInternalView view = new PlotInternalView();

	/**
	 * Full constructor, all contructors eventually call this one. Other
	 * constructors have defaults.
	 * 
	 * @param plot
	 *            the plot this belongs to
	 */
	Painter(final Dimensional plot) {
		graphLayout = GraphicsLayout.LABELS;
		/* class that draws tick marks and makes color thresholds */
		tickmarks = new Tickmarks();
		/* margin for printing */
		/* maybe should be available in constructor, middle of plot */
		middle = new Point();
		plotDimensions = plot.getDimensionality();
		setLayout(GraphicsLayout.Type.WITH_LABELS);
	}

	/**
	 * Set the layout type
	 * 
	 * @param type type of graphics layout
	 */
	protected void setLayout(final GraphicsLayout.Type type) {
		graphLayout = GraphicsLayout.getLayout(type);
		/* some initial layout stuff */
		border = new Insets(graphLayout.border.top, graphLayout.border.left,
				graphLayout.border.bottom, graphLayout.border.right);
		final Font screenFont = new Font(Painter.FONT_CLASS, Font.BOLD,
				(int) GraphicsLayout.SCREEN_FONT_SIZE);
		setGraphicsFont(screenFont);
	}

	/*
	 * non-javadoc: Set the font used on the plot.
	 */
	private void setGraphicsFont(final Font newFont) {
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

	protected void setView(final PageFormat format) {
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

	private final static float TITLE_SCREEN_SIZE = GraphicsLayout.SCREEN_FONT_SIZE + 2;

	private final static transient int MARK_OFFSET = 3;

	// stuff for channel marker
	private final static transient int MARK_MIN_LENGTH = 20;

	private int getMinimumY() {
		synchronized (limitsLock) {
			return plotDimensions == 1 ? plotLimits.getMinimumCounts()
					: plotLimits.getMinimumY();
		}
	}

	private int getMaximumY() {
		synchronized (limitsLock) {
			return plotDimensions == 1 ? plotLimits.getMaximumCounts()
					: plotLimits.getMaximumY();
		}
	}

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
	 * @since Version 0.5
	 */
	protected void update(final Graphics graph, final Dimension newViewSize,
			final Limits limits) {
		update(graph); // get graphics and copy to local variables
		synchronized (limitsLock) {
			plotLimits = limits;
			if (plotLimits != null) {
				/* retrieve information from plotLimits object */
				final int minX = plotLimits.getMinimumX();
				final int maxX = plotLimits.getMaximumX();
				final int minY = getMinimumY();
				final int maxY = getMaximumY();
				final double maxYLog = takeLog(maxY);
				final int rangeXch = maxX - minX + 1;
				final int rangeY = maxY - minY + 1;
				final double rangeYLog = maxYLog - takeLog(minY);
				if (pageformat == null) {
					this.viewSize = newViewSize;
				}
				/* plot are the borders and are part of the plot */
				view.setRight(viewSize.width - border.right - 1);
				/* subtract 1 as last pixel size-1 */
				view.setBottom(viewSize.height - border.bottom - 1);
				/* subtract 1 as last pixel size-1 */
				view.setWidth(view.getRight() - border.left + 1);
				/* add 1 as border part of plot */
				view.setHeight(view.getBottom() - border.top + 1);
				/* add 1 as border part of plot */
				final double conversionX = (double) view.getWidth()
						/ ((double) rangeXch);
				/* number of pixels per channel */
				final double conversionY = (double) view.getHeight()
						/ ((double) rangeY);
				/* number of pixels per channel */
				final double conversionYLog = view.getHeight() / rangeYLog;
				conversion = new Conversion(conversionX, conversionY,
						conversionYLog);
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
	protected void drawTitle(final String title, final int side) {
		int offset = 1;
		int xPos;
		int yPos;
		offset = metrics.stringWidth(title);
		if (graphLayout == GraphicsLayout.LABELS) {
			xPos = viewMiddle().x - offset / 2;
		} else {
			xPos = border.left + graphLayout.titleOffsets.left;
		}
		yPos = border.top - graphLayout.titleOffsets.top;
		if (side == TOP) {
			setGraphicsFont(font.deriveFont(Painter.TITLE_SCREEN_SIZE));
			graphics2d.drawString(title, xPos, yPos);
			setGraphicsFont(font.deriveFont(GraphicsLayout.SCREEN_FONT_SIZE));
		}
	}

	protected void drawNumber(final int number, final int[] overlayNumbers) {
		final String string = Integer.toString(number);
		setGraphicsFont(font);
		int width = metrics.stringWidth(string);
		int xNext = border.left - graphLayout.titleOffsets.top - width;
		final int yVal = border.top - graphLayout.titleOffsets.top;
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
	protected void drawDate(final String sdate) {
		final int xCoord = view.getRight() - metrics.stringWidth(sdate); // position
		// of
		// string
		final int yCoord = border.top - graphLayout.titleOffsets.date;
		graphics2d.drawString(sdate, xCoord, yCoord);
	}

	/*
	 * non-javadoc: Draws the run number in the upper left hand corner
	 * 
	 * @since Version 0.5
	 */
	protected void drawRun(final int runNumber) {
		final String runLabel = "Run " + runNumber;
		final int yCoordinate = border.top - graphLayout.titleOffsets.date;
		graphics2d.drawString(runLabel, border.left, yCoordinate);
	}

	/**
	 * Draws the border around the plot As plotSize() returns the the size of
	 * the plot including the borders we have to subtract one from the width and
	 * height. as drawRect(x,y,dx,dy) draws at x, y and at x+dx, y+dy.
	 * 
	 * @since Version 0.5
	 */
	protected void drawBorder() {
		graphics2d.drawRect(border.left, border.top, view.getWidth() - 1, view
				.getHeight() - 1);
	}

	/**
	 * Draws the tick marks on for a plot
	 * 
	 * @param side which side of plot
	 * @since Version 0.5
	 */
	protected void drawTicks(final int side) {
		Scale scale = Scale.LINEAR;
		if (side == BOTTOM) {// always linear
			synchronized (limitsLock) {
				ticksBottom(plotLimits.getMinimumX(), plotLimits.getMaximumX());
			}
		} else { // side==LEFT, if 1d-depends on Limits's scale
			synchronized (limitsLock) {
				if (plotDimensions == 1 && plotLimits != null) {
					scale = plotLimits.getScale();
				}
				ticksLeft(getMinimumY(), getMaximumY(), scale);
			}
		}
	}

	/**
	 * Draws the tick marks on the bottom side, X
	 * 
	 * @param lowerLimit lower limit
	 * @param upperLimit upper limit
	 * @since Version 0.5
	 */
	private void ticksBottom(final int lowerLimit, final int upperLimit) {
		final Scale scale = Scale.LINEAR;
		final int[] ticks = tickmarks.getTicks(lowerLimit, upperLimit, scale,
				Tickmarks.Type.MINOR);
		for (int tick : ticks) {
			final int xOrigin = toViewHorzLin(tick);
			int bottom = view.getBottom();
			graphics2d.drawLine(xOrigin, bottom, xOrigin, bottom
					- graphLayout.tick.minor);
			bottom = border.top;
			graphics2d.drawLine(xOrigin, bottom, xOrigin, bottom
					+ graphLayout.tick.minor);
		}
		final int[] ticksMajor = tickmarks.getTicks(lowerLimit, upperLimit,
				scale, Tickmarks.Type.MAJOR);
		for (int aTicksMajor : ticksMajor) {
			final int xOrigin = toViewHorzLin(aTicksMajor);
			int bottom = view.getBottom();
			graphics2d.drawLine(xOrigin, bottom, xOrigin, bottom
					- graphLayout.tick.major);
			bottom = border.top;
			graphics2d.drawLine(xOrigin, bottom, xOrigin, bottom
					+ graphLayout.tick.major);
		}
	}

	/**
	 * Draws the tick marks on for the left side, Y
	 * 
	 * @param lowerLimit lower limit
	 * @param upperLimit upper limit
	 * @param scale linear or log
	 * @since Version 0.5
	 */
	private void ticksLeft(final int lowerLimit, final int upperLimit,
			final Scale scale) {
		int xCoordinate;
		int yCoordinate;

		final int[] ticks = tickmarks.getTicks(lowerLimit, upperLimit, scale,
				Tickmarks.Type.MINOR);
		for (int tick : ticks) {
			if (scale == Scale.LINEAR) {
				yCoordinate = toViewVertLin(tick);
			} else {
				yCoordinate = toViewVertLog(tick);
			}
			xCoordinate = border.left;
			graphics2d.drawLine(xCoordinate, yCoordinate, xCoordinate
					+ graphLayout.tick.minor, yCoordinate);
			xCoordinate = view.getRight();
			graphics2d.drawLine(xCoordinate, yCoordinate, xCoordinate
					- graphLayout.tick.minor, yCoordinate);
		}
		final int[] ticksMajor = tickmarks.getTicks(lowerLimit, upperLimit,
				scale, Tickmarks.Type.MAJOR);
		for (int aTicksMajor : ticksMajor) {
			if (scale == Scale.LINEAR) {
				yCoordinate = toViewVertLin(aTicksMajor);
			} else {
				yCoordinate = toViewVertLog(aTicksMajor);
			}
			xCoordinate = border.left;
			graphics2d.drawLine(xCoordinate, yCoordinate, xCoordinate
					+ graphLayout.tick.major, yCoordinate);
			xCoordinate = view.getRight();
			graphics2d.drawLine(xCoordinate, yCoordinate, xCoordinate
					- graphLayout.tick.major, yCoordinate);
		}
	}

	/**
	 * Draws the tick mark Labels on for a plot
	 * 
	 * @param side which side of plot
	 * @since Version 0.5
	 */
	protected void drawLabels(final int side) {
		synchronized (limitsLock) {
			if (side == BOTTOM) {
				labelsBottom(plotLimits.getMinimumX(), plotLimits.getMaximumX());
			}
			if (plotDimensions == 1 && side == LEFT && plotLimits != null) {
				labelsLeft(getMinimumY(), getMaximumY(), plotLimits.getScale());
			} else if (plotDimensions == 2 && side == LEFT) {
				labelsLeft(getMinimumY(), getMaximumY(), Scale.LINEAR);
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
		for (int aTicksMajor : ticksMajor) {
			final String label = Integer.toString(aTicksMajor);
			final int offset = metrics.stringWidth(label); // length of string
			final int xCoordinate = toViewHorzLin(aTicksMajor) - offset / 2;
			final int yCoordinate = view.getBottom() + metrics.getAscent()
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
		for (int aTicksMajor : ticksMajor) {
			final String label = Integer.toString(aTicksMajor);
			final int offset = metrics.stringWidth(label);
			int yCoordinate = metrics.getAscent() / 2;
			if (scale == Scale.LINEAR) {
				yCoordinate += toViewVertLin(aTicksMajor);
			} else {
				yCoordinate += toViewVertLog(aTicksMajor);
			}
			final int xCoordinate = border.left - offset
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
	protected void drawAxisLabel(final String label, final int side) {
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
		final int yCoordinate = view.getBottom() + metrics.getAscent()
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
		final int offset = metrics.stringWidth(label);
		final int yCoordinate = viewMiddle().y + offset / 2;
		final int xCoordinate = border.left - graphLayout.axisLabelOffsets.left;
		final AffineTransform original = graphics2d.getTransform();
		graphics2d.translate(xCoordinate, yCoordinate);
		graphics2d.rotate(-Math.PI * 0.5); // negative 90 degrees
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
	protected void drawHist(final double[] counts, final double binWidth) {
		Scale scale = null;
		synchronized (limitsLock) {
			scale = plotLimits.getScale();
		}
		drawHist(counts, binWidth, scale != Scale.LINEAR);
	}

	/*
	 * non-javadoc: Plot a line graph
	 * 
	 * @param side
	 * 
	 * @since Version 0.5
	 */
	protected void drawLine(final double[] channel, final double[] countsdl) {
		synchronized (limitsLock) {
			if (plotLimits.getScale() == Scale.LINEAR) {
				drawLineLinear(channel, countsdl);
			} else {
				drawLineLog(channel, countsdl);
			}
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
			int minX = 0;
			int maxX = 0;
			double binChLo = 0.0;
			int firstBin = 0;
			int lastBin = 0;
			synchronized (limitsLock) {
				maxX = plotLimits.getMaximumX();
				final int upperX = (int) Math.min(maxX + 1, lastBinAllLo);
				firstBin = (int) Math
						.floor(plotLimits.getMinimumX() / binWidth);
				lastBin = (int) Math.floor(upperX / binWidth);
				binChLo = firstBin * binWidth;
				minX = plotLimits.getMinimumX();
			}
			int xCoordinate = toViewHorzLin(minX);
			int yCoordinate = view.getBottom();
			double delCh = binWidth;
			if (binChLo < minX) {
				delCh = binChLo + binWidth - minX;
				binChLo = minX;
			}
			path.moveTo(xCoordinate, yCoordinate);
			for (int i = firstBin; i <= lastBin; i++) {
				/* first a vertical line */
				yCoordinate = log ? toViewVertLog(drawCounts[i])
						: toViewVertLinCk(drawCounts[i]);
				path.lineTo(xCoordinate, yCoordinate);
				/* now horizontal across bin */
				binChLo += delCh;
				xCoordinate = Math.min(view.getRight(), toViewHorzLin(binChLo));
				path.lineTo(xCoordinate, yCoordinate);
				delCh = Math.min(binWidth, maxX + 1 - binChLo);
			}
			// last vertical line
			if (xCoordinate < view.getRight()) {
				yCoordinate = view.getBottom();
				path.lineTo(xCoordinate, yCoordinate);
			}
			graphics2d.draw(path);
		}
	}

	private static final double EPSILON = 0.001;

	private double[] getDrawCounts(final double[] counts, final double bin) {
		/* bin assumed >= 1.0 */
		final double one = 1.0;
		final int len = (int) Math.ceil(counts.length / bin);
		double[] result = new double[len];
		if ((bin - 1.0) <= EPSILON) {
			result = counts;
		} else {
			double remain = bin;
			int index = 0;
			for (double count : counts) {
				if (remain > one) {
					result[index] += count;
					remain -= one;
				} else {
					result[index] += count * remain;
					if (index < len - 1) {
						index++;
					}
					result[index] += count * (one - remain);
					remain += bin - one;
				}
			}
			for (int i = 0; i < len; i++) {
				result[i] /= bin;
			}
		}
		return result;
	}

	/**
	 * Plot a line graph. plot is lines segments
	 * 
	 * @param channel channel numbers
	 * @param counts counts for channels
	 * @since Version 0.5
	 */
	private void drawLineLinear(final double[] channel, final double[] counts) {
		int lowerX, upperX;
		synchronized (limitsLock) {
			lowerX = Math.max(plotLimits.getMinimumX(), (int) channel[0] + 1);
			upperX = Math.min(plotLimits.getMaximumX(),
					(int) channel[channel.length - 1]);
		}
		int xValue1 = toViewHorzLin(channel[0]);

		/* check don't go beyond border */
		int yValue1 = Math.max(toViewVertLin(counts[0]), border.top);

		/* for each point draw from last line to next line */
		for (int i = 1; i < channel.length; i++) {
			final int xValue2 = toViewHorzLin(channel[i]);

			/* could go 1 pixel too far for last i */
			final int yValue2 = Math.max(toViewVertLin(counts[i]), border.top);

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
		int lowerX = 0;
		int upperX = 0;
		synchronized (limitsLock) {
			lowerX = Math.max(plotLimits.getMinimumX(), (int) channel[0] + 1);
			upperX = Math.min(plotLimits.getMaximumX(),
					(int) channel[channel.length - 1]);
		}
		int xValue1 = toViewHorzLin(channel[0]);
		int yValue1 = toViewVertLog(counts[0]);
		/* check we don't go beyond border */
		if (yValue1 < border.top) {
			yValue1 = border.top;
		}
		/* for each point draw from last line to next line */
		for (int i = 1; i < channel.length; i++) {
			final int xValue2 = toViewHorzLin(channel[i]);
			// could go 1 pixel too far for last i
			int yValue2 = toViewVertLog(counts[i]);
			// check dont go beyond border
			if (yValue2 < border.top) {
				yValue2 = border.top;
			}
			if ((channel[i] >= lowerX) && (channel[i] <= upperX)) {
				graphics2d.drawLine(xValue1, yValue1, xValue2, yValue2);
			}
			// save start for next line segment
			xValue1 = xValue2;
			yValue1 = yValue2;
		}
	}

	private int getMinimumCounts() {
		synchronized (limitsLock) {
			return plotLimits.getMinimumCounts();
		}
	}

	private int getMaximumCounts() {
		synchronized (limitsLock) {
			return plotLimits.getMaximumCounts();
		}
	}

	/**
	 * Draw scale for a 2d plot
	 * 
	 * @param colors
	 *            colors to use
	 * @since Version 0.5
	 */
	protected void drawScale2d(final DiscreteColorScale colors) {
		final int minCount = getMinimumCounts();
		colors.setRange(minCount, getMaximumCounts());
		final int[] colorThresholds = colors.getColorThresholds();
		final int numberColors = colorThresholds.length;
		final int textHeight = metrics.getAscent();
		/* lowest threshold for color to be drawn */
		drawScaleKey(minCount, colorThresholds, numberColors, textHeight);
		/* draw colors on side */
		for (int k = 0; k < numberColors; k++) {
			graphics2d.setColor(colors.getColorByIndex(k));
			graphics2d.fillRect(
					view.getRight() + graphLayout.colorScale.offset, // horizontal
					view.getBottom() - graphLayout.colorScale.size - k
							* graphLayout.colorScale.size, // vertical
					graphLayout.colorScale.size, graphLayout.colorScale.size); // size
		}
	}

	private void drawScaleKey(final int minCount, final int[] colorThresholds,
			final int numberColors, final int textHeight) {
		String label = Integer.toString(minCount);
		graphics2d.drawString(label, view.getRight()
				+ graphLayout.colorScale.offset + graphLayout.colorScale.size
				+ graphLayout.colorScale.labelOffset, view.getBottom()
				+ textHeight / 2);
		for (int k = 0; k < numberColors; k++) {
			label = Integer.toString(colorThresholds[k]);
			graphics2d.drawString(label, view.getRight()
					+ graphLayout.colorScale.offset
					+ graphLayout.colorScale.size
					+ graphLayout.colorScale.labelOffset, view.getBottom()
					- graphLayout.colorScale.size - k
					* graphLayout.colorScale.size + textHeight / 2);
		}
	}

	protected void drawScale2d() {
		Scale scale = null;
		synchronized (limitsLock) {
			scale = plotLimits.getScale();
		}
		final ColorScale colors = GradientColorScale.getScale(scale);
		final int lowerLimit = getMinimumCounts();
		final int upperLimit = getMaximumCounts();
		colors.setRange(lowerLimit, upperLimit);
		setGraphicsFont(font);
		final int textHeight = metrics.getAscent();
		final DiscreteColorScale dcs = DiscreteColorScale.getScale(scale);
		dcs.setRange(lowerLimit, upperLimit);
		final int[] colorThresholds = dcs.getColorThresholds();
		final int numberColors = colorThresholds.length;
		drawScaleKey(lowerLimit, colorThresholds, numberColors, textHeight);
		/* draw colors on side */
		final int scaleHeight = numberColors * graphLayout.colorScale.size;
		final int xValue1 = view.getRight() + graphLayout.colorScale.offset;
		final int xValue2 = xValue1 + graphLayout.colorScale.size - 1;
		double level;
		final double lowEnd = Math.max(1.0, lowerLimit);
		final double highEnd = colorThresholds[numberColors - 1];
		for (int row = 0; row < scaleHeight; row++) {
			final int yValue = view.getBottom() - row;
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
	protected void drawHist2d(final double[][] counts, final int minChanX,
			final int minChanY, final int maxChanX, final int maxChanY,
			final DiscreteColorScale colors) {
		final int minCount = getMinimumCounts();
		colors.setRange(minCount, getMaximumCounts());
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
					/* check for min counts first as these are most likely */
					final Color paint = colors.getColor(count);
					graphics2d.setColor(paint);
					/* inline for speed */
					graphics2d.fillRect(xValue, yValue - channelHeight + 1,
							channelWidth, channelHeight);
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
	protected void drawHist2d(final double[][] counts, final int minChanX,
			final int minChanY, final int maxChanX, final int maxChanY) {
		ColorScale colors = null;
		synchronized (limitsLock) {
			colors = GradientColorScale.getScale(plotLimits.getScale());
		}
		final int minCount = getMinimumCounts();
		colors.setRange(minCount, getMaximumCounts());
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
	protected void drawGate1d(final int lowerLimit, final int upperLimit,
			final boolean noFillMode) {
		clipPlot();
		final int xValue = toViewHorzLin(lowerLimit);
		final int xValue2 = Math.min(toViewHorzLin(upperLimit + 1), view
				.getRight());
		final int width = xValue2 - xValue;
		final int height = view.getBottom() - border.top;
		if (noFillMode) {
			graphics2d.drawRect(xValue, border.top, width, height);
		} else {
			graphics2d.fillRect(xValue, border.top, width, height);
		}
	}

	/**
	 * Draw a 2d Gate
	 * 
	 * @param gate
	 *            the array to be displayed
	 */
	protected void drawGate2d(final boolean[][] gate) {
		if (gate != null) {
			int minX = 0;
			int maxX = 0;
			synchronized (limitsLock) {
				minX = plotLimits.getMinimumX();
				maxX = plotLimits.getMaximumX();
			}
			final int maxY = getMaximumY();
			for (int j = getMinimumY(); j <= maxY; j++) { // for each point
				for (int i = minX; i <= maxX; i++) {
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
	protected void settingGate1d(final Polygon gatePoints) {
		clipPlot();
		if (gatePoints.npoints > 0) {
			final int xValue = gatePoints.xpoints[gatePoints.npoints - 1];
			if (gatePoints.npoints > 1) {
				markAreaOutline1d(
						toDataHorz(gatePoints.xpoints[gatePoints.npoints - 2]),
						toDataHorz(xValue));
			} else {
				graphics2d.drawLine(xValue, view.getBottom(), xValue,
						border.top);
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
	protected Rectangle getRectangleOutline1d(final int xValue1,
			final int xValue2) {
		final int height = view.getBottom() - border.top;// Full plot
		// vertically
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
		return new Rectangle(tempX, border.top, width, height);
	}

	/**
	 * Mark the outline of an area in a 1d plot.
	 * 
	 * @param xValue1
	 *            a point in plot coordinates
	 * @param xValue2
	 *            a point in plot coordinates
	 */
	protected void markAreaOutline1d(final int xValue1, final int xValue2) {
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
	protected Rectangle getRectangleOutline2d(final Rectangle channels) {
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
	protected Rectangle getRectangleOutline2d(final Bin bin1, final Bin bin2) {
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
	protected void markArea2dOutline(final Bin bin1, final Bin bin2) {
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
	protected void settingGate2d(final Polygon gatePoints) {
		clipPlot();
		final Polygon shape = toView(gatePoints);
		graphics2d.drawPolyline(shape.xpoints, shape.ypoints, shape.npoints);
	}

	protected Polygon toView(final Polygon shape) {
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
	protected void markChannel1d(final int channel, final double count) {
		int yValue2;

		final int xValue1 = toViewHorzLin(channel + 0.5);
		final int yValue1 = view.getBottom();
		final int xValue2 = xValue1;
		synchronized (limitsLock) {
			if (plotLimits.getScale() == Scale.LINEAR) {
				yValue2 = toViewVertLinCk(count);
			} else {
				yValue2 = toViewVertLog(count);
			}
		}
		// draw the line at least a mark min length
		if ((yValue1 - yValue2) < Painter.MARK_MIN_LENGTH) {
			yValue2 = view.getBottom() - Painter.MARK_MIN_LENGTH;
		}
		// are we inside the plot area
		if (xValue1 >= border.left && xValue1 <= view.getRight()) {
			graphics2d.drawLine(xValue1, yValue1, xValue2, yValue2);
			final String label = Integer.toString(channel);
			graphics2d
					.drawString(label, xValue2, yValue2 - Painter.MARK_OFFSET);
		}
	}

	protected void drawPeakLabels(final double[][] peaks) {
		int yValue1; // bottom of line
		final Color initColor = graphics2d.getColor();
		setGraphicsFont(font.deriveFont(GraphicsLayout.SCREEN_FONT_SIZE));
		graphics2d.setColor(COLOR_MAP.getPeakLabel());
		for (int i = 0; i < peaks[0].length; i++) {
			final int xValue1 = toViewHorzLin(peaks[0][i] + 0.5);
			synchronized (limitsLock) {
				if (plotLimits.getScale() == Scale.LINEAR) {
					yValue1 = toViewVertLinCk(peaks[2][i]) - 3;
				} else {
					yValue1 = toViewVertLog(peaks[2][i]) - 3;
				}
			}
			final int yValue2 = yValue1 - 7; // top of line
			// are we inside the plot area?
			if (xValue1 >= border.left && xValue1 <= view.getRight()) {
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
	protected void markChannel2d(final Bin bin) {
		final Rectangle rectangle = getRectangleOutline2d(bin, bin);
		final String label = Integer.toString(bin.getX()) + "," + bin.getY();
		graphics2d.draw(rectangle);
		graphics2d.drawString(label, (int) rectangle.getMaxX()
				+ Painter.MARK_OFFSET, rectangle.y - Painter.MARK_OFFSET);
	}

	/*
	 * non-javadoc: Mark an area in a 1 d plot
	 * 
	 * @lowChan lower channel @highChan upper channel
	 */
	protected void markArea1d(final int lowChan, final int highChan,
			final double[] counts) {
		int minChan = 0;
		int maxChan = 0;
		boolean log = false;
		final Polygon fill = new Polygon();
		synchronized (limitsLock) {
			minChan = Math.max(plotLimits.getMinimumX(), lowChan);
			maxChan = Math.min(plotLimits.getMaximumX(), highChan);
			log = plotLimits.getScale() != Scale.LINEAR;
		}
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
			lasty = addToFillY(fill, lasty, xValue, yValue);
			xValue = Math.min(view.getRight(), toViewHorzLin(i + 1));
			lastx = addToFillX(fill, lastx, lasty, xValue, yValue);
		}
		if (yValue != yInitial) {// go back to bottom on last
			yValue = yInitial;
			fill.addPoint(xValue, yValue);
		}
		graphics2d.fill(fill);
	}

	private int addToFillX(final Polygon fill, final int lastx,
			final int lasty, final int xValue, final int yValue) {
		int result = lastx;
		if (!(xValue == lastx && yValue == lasty)) {
			fill.addPoint(xValue, yValue);
			result = xValue;
		}
		return result;
	}

	private int addToFillY(final Polygon fill, final int lasty,
			final int xValue, final int yValue) {
		int result = lasty;
		if (yValue != lasty) {
			fill.addPoint(xValue, yValue);
			result = yValue;
		}
		return result;
	}

	/**
	 * Mark an area in a 2d plot.
	 * 
	 * @param rectangle
	 *            rectangle in graphics coordinates
	 */
	protected void markArea2d(final Rectangle rectangle) {
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
	protected void drawDataLine(final int xValue1, final int yValue1,
			final int xValue2, final int yValue2) {
		graphics2d.drawLine(toViewHorzLin(xValue1), toViewVertLin(yValue1),
				toViewHorzLin(xValue2), toViewVertLin(yValue2));
	}

	/*
	 * non-javadoc: Convert to data corodinates, given a screeen point. These
	 * routines do not have to be as fast as the to view ones, as it is not very
	 * often we want to go this way. The ones that return int are faster.
	 */
	protected Bin toData(final Point viewPoint) {
		synchronized (monitor) {
			return Bin.create(toDataHorz(viewPoint.x), toDataVert(viewPoint.y));
		}
	}

	/*
	 * non-javadoc: Give the horizontal plot coordinate for the given graphics
	 * horizontal coodinate.
	 */
	protected int toDataHorz(final int xView) {
		synchronized (monitor) {
			final int data;
			/* if we are beyond limits set point to limit */
			synchronized (limitsLock) {
				if (xView < border.left) {
					data = plotLimits.getMinimumX();
				} else if (xView >= view.getRight()) {
					data = plotLimits.getMaximumX();
				} else {
					data = (int) (plotLimits.getMinimumX() + (xView - border.left)
							/ conversion.getX());
				}
			}
			return data;
		}
	}

	/*
	 * non-javadoc: Give the vertical plot coordinate for the given graphics
	 * vertical coordinate.
	 */
	protected int toDataVert(final int yCoordinate) {
		synchronized (monitor) {
			final int data;
			/* if we are beyond limits set point to limit */
			if (yCoordinate < border.top) {
				data = getMaximumY();
			} else if (yCoordinate > view.getBottom()) {
				data = getMinimumY();
			} else {
				data = (int) (getMinimumY() + (view.getBottom() - yCoordinate)
						/ conversion.getY());
			}
			return data;
		}
	}

	/*
	 * non-javadoc: Convert data point to view point
	 */
	protected Point toViewLin(final Bin dataPoint) {
		final Point viewPoint = new Point(toViewHorzLin(dataPoint.getX()),
				toViewVertLin(dataPoint.getY()));
		return viewPoint;
	}

	/*
	 * non-javadoc: Get the middle point of the plot usefull for drawing title
	 * and labels
	 */
	private Point viewMiddle() {
		middle.x = border.left + view.getWidth() / 2;
		middle.y = border.top + view.getHeight() / 2;
		return middle;
	}

	/**
	 * Clip so only active region of plot is drawn on.
	 */
	protected void clipPlot() {
		graphics2d.clipRect(border.left, border.top, view.getWidth() + 1, view
				.getHeight() + 1);
	}

	/*
	 * non-javadoc: Convert horizontal channel coordinate to the graphics
	 * coordinate which represents the "low" (left) side of the bin.
	 */
	private int toViewHorzLin(final double data) {
		synchronized (limitsLock) {
			return (int) (border.left + (conversion.getX() * (data - plotLimits
					.getMinimumX())));
		}
	}

	/*
	 * non-javadoc: Convert vertical channel coordinate to the graphics
	 * coordinate which represents the "low" (bottom) side of the bin.
	 */
	private int toViewVertLin(final double data) {
		return (int) (view.getBottom() - (conversion.getY() * (data - getMinimumY())));
	}

	/*
	 * non-javadoc: Convert vertical data to vertical view (screen) screen
	 * vertical linear scale.
	 */
	private int toViewVertLinCk(final double data) {
		final int rval;
		if (data > getMaximumY()) {
			rval = border.top;
		} else if (data < getMinimumY()) {
			rval = view.getBottom();
		} else {
			rval = toViewVertLin(data);
		}
		return rval;
	}

	/*
	 * non-javadoc: Convert data vertical to view vertical for Log scale
	 */
	private int toViewVertLog(final double data) {
		final int rval;
		final double dataLog = takeLog(data);
		final double minYLog = takeLog(getMinimumY());
		if (dataLog > takeLog(getMaximumY())) {
			rval = border.top;
		} else if (dataLog < minYLog) {
			rval = view.getBottom();
		} else {
			rval = view.getBottom()
					- (int) (conversion.getYLog() * (dataLog - minYLog));
		}
		return rval;
	}

	/*
	 * non-javadoc: Take the log of a data point if valid to otherwise return
	 * fake zero
	 * 
	 * @param point point to take log of
	 */
	private double takeLog(final double point) {
		// fake zero for Log scale 1/2 a count
		final double LOG_FAKE_ZERO = 0.5;
		return Math.log(point > 0.0 ? point : LOG_FAKE_ZERO);
	}
}