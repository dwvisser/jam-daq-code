package jam.plot;

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

    static final int BOTTOM = 1;

    static final int TOP = 2;

    static final int LEFT = 3;

    static final int RIGHT = 4;

    /* fake zero for Log scale 1/2 a count */
    static final double LOG_FAKE_ZERO = 0.5;

    private static final PlotColorMap COLOR_MAP = PlotColorMap.getInstance();

    PlotGraphicsLayout graphLayout;

    /* current stuff to draw font, and font metrics and colors */
    private Graphics2D g;

    private Font font;

    private FontMetrics fm;

    private Tickmarks tm;

    /**
     * Border for plot in pixels
     */
    private Insets border;

    /** The Limits in channel and scale of the plot */
    private Limits plotLimits;

    /** is the plot 1d or 2d */
    private final int plotDimensions;

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

    /** number of pixels per channel */
    private double conversionX;

    /** number of pixels per channel */
    private double conversionY;

    /** number of pixels per channel */
    private double conversionYLog;

    /** the dimensions of the plot canvas */
    private Dimension viewSize;

    /** sides of plot in pixels */
    private int viewLeft; //left hand side of plot area

    private int viewRight; //right hand side of plot area

    private int viewTop; //top side of plot area

    private int viewBottom; //bottom side of plot area

    private int viewWidth; //width of plot area

    private int viewHeight; //height of plot area

    private Point viewMiddle; //middle of plot area

    private Font screenFont; //Screen Font

    //private Font printFont; //Printing Font

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
        tm = new Tickmarks();
        /* margin for printing */
        /* maybe should be avaliable in constructor, middle of plot */
        viewMiddle = new Point();
        if (plot instanceof Plot1d) {
            plotDimensions = 1;
        } else {//Plot2d
            plotDimensions = 2;
        }
        setLayout(PlotGraphicsLayout.LAYOUT_TYPE_LABELS);
    }

    /**
     * Set the layout type
     * 
     * @param type
     */
    void setLayout(int type) {
        graphLayout = PlotGraphicsLayout.getLayout(type);
        /* some initial layout stuff */
        border = new Insets(graphLayout.BORDER_TOP, graphLayout.BORDER_LEFT,
                graphLayout.BORDER_BOTTOM, graphLayout.BORDER_RIGHT);
        screenFont = new Font(graphLayout.FONT_CLASS, Font.BOLD,
                (int) graphLayout.SCREEN_FONT_SIZE);
        //TODO use printFont
        /*
         * printFont = new Font(graphLayout.FONT_CLASS, Font.PLAIN,
         * graphLayout.PRINT_FONT_SIZE);
         */
        setGraphicsFont(screenFont);
    }

    /*
     * non-javadoc: Set the font used on the plot.
     */
    final synchronized void setGraphicsFont(Font f) {
        font = f;
        if (g != null) {
            g.setFont(f);
            fm = g.getFontMetrics();
        }
    }

    private PageFormat pageformat = null;

    synchronized void setView(PageFormat pf) {
        pageformat = pf;
        if (pf != null) {
            viewSize = new Dimension((int) pf.getImageableWidth(), (int) pf
                    .getImageableHeight());
        }
    }

    private final Object limitsLock = new Object();

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
    void update(Graphics graph, Dimension newViewSize, Limits limits) {
        update(graph); //get graphics and copy to local variables
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
                rangeXch = maxXch - minXch + 1;
                final int rangeY = maxY - minY + 1;
                final double rangeYLog = maxYLog - minYLog;
                if (pageformat == null) {
                    this.viewSize = newViewSize;
                }
                /* plot* are the borders and are part of the plot */
                viewLeft = border.left; //really 0+border.left
                viewTop = border.top; //really 0+border.top
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

    private static final RenderingHints RH = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    static {
        RH.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /**
     * Update just the graphics object
     * 
     * @param graph
     *            grapics object
     */
    private void update(Graphics graph) {
        g = (Graphics2D) graph;
        g.setRenderingHints(RH);
        if (fm == null) {
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
    void drawTitle(String title, int side) {
        int offset = 1;
        int xPos;
        int yPos;
        offset = fm.stringWidth(title);
        if (graphLayout == PlotGraphicsLayout.LABELS) {
            xPos = viewMiddle().x - offset / 2;
        } else {
            xPos = viewLeft + graphLayout.TITLE_OFFSET_LEFT;
        }
        yPos = viewTop - graphLayout.TITLE_OFFSET_TOP;
        if (side == PlotGraphics.TOP) {
            setGraphicsFont(font.deriveFont(graphLayout.TITLE_SCREEN_SIZE));
            g.drawString(title, xPos, yPos);
            setGraphicsFont(font.deriveFont(graphLayout.SCREEN_FONT_SIZE));
        }
    }

    void drawNumber(int number, int[] overlayNumbers) {
        final String s = Integer.toString(number);
        setGraphicsFont(font);
        int width = fm.stringWidth(s);
        int xNext = this.viewLeft - graphLayout.TITLE_OFFSET_TOP - width;
        final int y = viewTop - graphLayout.TITLE_OFFSET_TOP;
        final Color c = g.getColor();
        g.setColor(COLOR_MAP.getForeground());
        g.drawString(s, xNext, y);
        for (int i = 0; i < overlayNumbers.length; i++) {
            xNext += width;
            final String sNext = ", " + overlayNumbers[i];
            width = fm.stringWidth(sNext);
            g.setColor(COLOR_MAP.getOverlay(i));
            g.drawString(sNext, xNext, y);
        }
        g.setColor(c);
    }

    /*
     * non-javadoc: Draws the date in the upper right hand corner
     * 
     * @since Version 0.5
     */
    void drawDate(String sdate) {
        final int x = viewRight - fm.stringWidth(sdate); //position of string
        final int y = viewTop - graphLayout.TITLE_OFFSET_DATE;
        g.drawString(sdate, x, y);
    }

    /*
     * non-javadoc: Draws the run number in the upper left hand corner
     * 
     * @since Version 0.5
     */
    void drawRun(int runNumber) {
        String runLabel = "Run " + runNumber;
        int x = viewLeft;
        int y = viewTop - graphLayout.TITLE_OFFSET_DATE;
        g.drawString(runLabel, x, y);
    }

    /**
     * Draws the border around the plot As plotSize() returns the the size of
     * the plot including the borders we have to subtract one from the width and
     * height. as drawRect(x,y,dx,dy) draws at x, y and at x+dx, y+dy.
     * 
     * @since Version 0.5
     */
    void drawBorder() {
        g.drawRect(viewLeft, viewTop, viewWidth - 1, viewHeight - 1);
    }

    /**
     * Draws the tickmarks on for a plot
     * 
     * @param side
     * @since Version 0.5
     */
    void drawTicks(int side) {
        Scale scale = Scale.LINEAR;
        if (side == BOTTOM) {//always linear
            ticksBottom(minXch, maxXch);
        } else { //side==LEFT, if 1d-depends on Limits's scale
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
    private void ticksBottom(int lowerLimit, int upperLimit) {
        final Scale scale = Scale.LINEAR;
        final int[] ticks = tm.getTicks(lowerLimit, upperLimit, scale,
                Tickmarks.MINOR);
        for (int i = 0; i < ticks.length; i++) {
            final int x = toViewHorzLin(ticks[i]);
            int y = viewBottom;
            g.drawLine(x, y, x, y - graphLayout.TICK_MINOR);
            y = viewTop;
            g.drawLine(x, y, x, y + graphLayout.TICK_MINOR);
        }
        final int[] ticksMajor = tm.getTicks(lowerLimit, upperLimit, scale,
                Tickmarks.MAJOR);
        for (int i = 0; i < ticksMajor.length; i++) {
            final int x = toViewHorzLin(ticksMajor[i]);
            int y = viewBottom;
            g.drawLine(x, y, x, y - graphLayout.TICK_MAJOR);
            y = viewTop;
            g.drawLine(x, y, x, y + graphLayout.TICK_MAJOR);
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
    private void ticksLeft(int lowerLimit, int upperLimit, Scale scale) {
        int x;
        int y;

        int[] ticks = tm.getTicks(lowerLimit, upperLimit, scale,
                Tickmarks.MINOR);
        for (int i = 0; i < ticks.length; i++) {
            if (scale == Scale.LINEAR) {
                y = toViewVertLin(ticks[i]);
            } else {
                y = toViewVertLog(ticks[i]);
            }
            x = viewLeft;
            g.drawLine(x, y, x + graphLayout.TICK_MINOR, y);
            x = viewRight;
            g.drawLine(x, y, x - graphLayout.TICK_MINOR, y);
        }

        int[] ticksMajor = tm.getTicks(lowerLimit, upperLimit, scale,
                Tickmarks.MAJOR);
        for (int i = 0; i < ticksMajor.length; i++) {
            if (scale == Scale.LINEAR) {
                y = toViewVertLin(ticksMajor[i]);
            } else {
                y = toViewVertLog(ticksMajor[i]);
            }
            x = viewLeft;
            g.drawLine(x, y, x + graphLayout.TICK_MAJOR, y);
            x = viewRight;
            g.drawLine(x, y, x - graphLayout.TICK_MAJOR, y);
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
    private void labelsBottom(int lowerLimit, int upperLimit) {
        final Scale scale = Scale.LINEAR;
        final int[] ticksMajor = tm.getTicks(lowerLimit, upperLimit, scale,
                Tickmarks.MAJOR);
        for (int i = 0; i < ticksMajor.length; i++) {
            final String label = Integer.toString(ticksMajor[i]);
            final int offset = fm.stringWidth(label); //length of string
            final int x = toViewHorzLin(ticksMajor[i]) - offset / 2;
            final int y = viewBottom + fm.getAscent()
                    + graphLayout.LABEL_OFFSET_BOTTOM;
            g.drawString(label, x, y);
        }
    }

    /*
     * non-javadoc: Draws the Labels on for the left side of a plot
     * 
     * @param side
     * 
     * @since Version 0.5
     */
    private void labelsLeft(int lowerLimit, int upperLimit, Scale scale) {
        int[] ticksMajor = tm.getTicks(lowerLimit, upperLimit, scale,
                Tickmarks.MAJOR);
        for (int i = 0; i < ticksMajor.length; i++) {
            final String label = Integer.toString(ticksMajor[i]);
            final int offset = fm.stringWidth(label);
            int y = fm.getAscent() / 2;
            if (scale == Scale.LINEAR) {
                y += toViewVertLin(ticksMajor[i]);
            } else {
                y += toViewVertLog(ticksMajor[i]);
            }
            final int x = viewLeft - offset - graphLayout.LABEL_OFFSET_LEFT;
            g.drawString(label, x, y);
        }
    }

    /*
     * non-javadoc: Draws the axis Labels on for a plot
     * 
     * @param side
     * 
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

    /*
     * non-javadoc: Draws the axis Labels on for the bottom side of a plot
     * 
     * @param side
     * 
     * @since Version 0.5
     */
    private void axisLabelBottom(String label) {
        final int offset = fm.stringWidth(label);
        final int x = viewMiddle().x - offset / 2;
        final int y = viewBottom + fm.getAscent()
                + graphLayout.AXIS_LABEL_OFFSET_BOTTOM;
        g.drawString(label, x, y);
    }

    /*
     * non-javadoc: Draws the axis Labels on for the left side of a plot
     * 
     * @param side
     * 
     * @since Version 0.5
     */
    private void axisLabelLeft(String label) {
        final double ninetyDeg = -Math.PI * 0.5;
        final int offset = fm.stringWidth(label);
        final int y = viewMiddle().y + offset / 2;
        final int x = viewLeft - graphLayout.AXIS_LABEL_OFFSET_LEFT;
        final AffineTransform original = g.getTransform();
        g.translate(x, y);
        g.rotate(ninetyDeg);
        g.drawString(label, 0, 0);
        g.setTransform(original);
    }

    /*
     * non-javadoc: Histogram a plot with double count array
     * 
     * @param side
     * 
     * @since Version 0.5
     */
    void drawHist(double[] counts, double binWidth) {
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
    void drawLine(double[] channel, double[] countsdl) {
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
    private void drawHist(double[] counts, double binWidth, boolean log) {
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
            int x = toViewHorzLin(minXch);
            int y = viewBottom;
            double delCh = binWidth;
            if (binChLo < minXch) {
                delCh = binChLo + binWidth - minXch;
                binChLo = minXch;
            }
            path.moveTo(x, y);
            for (int i = firstBin; i <= lastBin; i++) {
                /* first a vertical line */
                y = log ? toViewVertLog(drawCounts[i])
                        : toViewVertLinCk(drawCounts[i]);
                path.lineTo(x, y);
                /* now horizontal across bin */
                binChLo += delCh;
                x = Math.min(viewRight, toViewHorzLin(binChLo));
                path.lineTo(x, y);
                delCh = Math.min(binWidth, maxXch + 1 - binChLo);
            }
            // last vertical line
            if (x < viewRight) {
                y = viewBottom;
                path.lineTo(x, y);
            }
            g.draw(path);
        }
    }

    private static final double EPSILON = 0.001;

    private double[] getDrawCounts(double[] counts, double bin) {
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
    private void drawLineLinear(double[] channel, double[] counts) {
        final int lowerX = Math.max(this.minXch, (int) channel[0] + 1);
        final int upperX = Math.min(this.maxXch,
                (int) channel[channel.length - 1]);
        int x1 = toViewHorzLin(channel[0]);
        /* check dont go beyond border */
        int y1 = Math.max(toViewVertLin(counts[0]), viewTop);
        /* for each point draw from last line to next line */
        for (int i = 1; i < channel.length; i++) {
            final int x2 = toViewHorzLin(channel[i]);
            /* could go 1 pixel too far for last i */
            final int y2 = Math.max(toViewVertLin(counts[i]), viewTop);
            /* check we don't go beyond border */
            if ((channel[i] >= lowerX) && (channel[i] <= upperX)) {
                g.drawLine(x1, y1, x2, y2);
            }
            /* save start for next line segment */
            x1 = x2;
            y1 = y2;
        }
    }

    /*
     * non-javadoc: Plot a line graph, Log scale
     * 
     * @param side
     * 
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
     * @param colors
     *            colors to use
     * @since Version 0.5
     */
    void drawScale2d(DiscreteColorScale colors) {
        colors.setRange(minCount, maxCount);
        final int[] colorThresholds = colors.getColorThresholds();
        final int numberColors = colorThresholds.length;
        final int textHeight = (fm.getAscent());
        /* lowest threshold for color to be drawn */
        String label = Integer.toString(minCount);
        g.drawString(label, viewRight + graphLayout.COLOR_SCALE_OFFSET
                + graphLayout.COLOR_SCALE_SIZE
                + graphLayout.COLOR_SCALE_LABEL_OFFSET, viewBottom + textHeight
                / 2);
        for (int k = 0; k < numberColors; k++) {
            label = Integer.toString(colorThresholds[k]);
            g.drawString(label, viewRight + graphLayout.COLOR_SCALE_OFFSET
                    + graphLayout.COLOR_SCALE_SIZE
                    + graphLayout.COLOR_SCALE_LABEL_OFFSET, viewBottom
                    - graphLayout.COLOR_SCALE_SIZE - k
                    * graphLayout.COLOR_SCALE_SIZE + textHeight / 2);
        }
        /* draw colors on side */
        for (int k = 0; k < numberColors; k++) {
            g.setColor(colors.getColorByIndex(k));
            g.fillRect(viewRight + graphLayout.COLOR_SCALE_OFFSET, //horizontal
                    viewBottom - graphLayout.COLOR_SCALE_SIZE - k
                            * graphLayout.COLOR_SCALE_SIZE, //vertical
                    graphLayout.COLOR_SCALE_SIZE, graphLayout.COLOR_SCALE_SIZE); //size
        }
    }

    void drawScale2d() {
        final Scale scale = plotLimits.getScale();
        final ColorScale colors = GradientColorScale.getScale(scale);
        colors.setRange(minCount, maxCount);
        int lowerLimit = minCount;
        int upperLimit = maxCount;
        setGraphicsFont(font);
        int textHeight = (fm.getAscent());
        final DiscreteColorScale dcs = DiscreteColorScale.getScale(scale);
        dcs.setRange(lowerLimit, upperLimit);
        final int[] colorThresholds = dcs.getColorThresholds();
        final int numberColors = colorThresholds.length;
        /* lowest threshold for color to be drawn */
        String label = Integer.toString(lowerLimit);
        g.drawString(label, viewRight + graphLayout.COLOR_SCALE_OFFSET
                + graphLayout.COLOR_SCALE_SIZE
                + graphLayout.COLOR_SCALE_LABEL_OFFSET, viewBottom + textHeight
                / 2);
        for (int k = 0; k < numberColors; k++) {
            label = Integer.toString(colorThresholds[k]);
            g.drawString(label, viewRight + graphLayout.COLOR_SCALE_OFFSET
                    + graphLayout.COLOR_SCALE_SIZE
                    + graphLayout.COLOR_SCALE_LABEL_OFFSET, viewBottom
                    - graphLayout.COLOR_SCALE_SIZE - k
                    * graphLayout.COLOR_SCALE_SIZE + textHeight / 2);
        }
        /* draw colors on side */
        int scaleHeight = numberColors * graphLayout.COLOR_SCALE_SIZE;
        int x1 = viewRight + graphLayout.COLOR_SCALE_OFFSET;
        int x2 = x1 + graphLayout.COLOR_SCALE_SIZE - 1;
        double level;
        double lowEnd = Math.max(1.0, lowerLimit);
        double highEnd = colorThresholds[numberColors - 1];
        for (int row = 0; row < scaleHeight; row++) {
            int y = viewBottom - row;
            if (plotLimits.getScale() == Scale.LINEAR) {
                level = lowerLimit + row * (highEnd - lowEnd) / scaleHeight;
            } else { //log scale
                level = lowEnd
                        * Math
                                .pow(highEnd / lowEnd, (double) row
                                        / scaleHeight);
            }
            g.setColor(colors.getColor(level));
            g.drawLine(x1, y, x2, y);
        }
    }

    /*
     * non-javadoc: Draw a 2d plot.
     * 
     * @param counts the counts to be displayed @param colors the colors to use
     * 
     * @since Version 0.5
     */
    void drawHist2d(double[][] counts, int minChanX, int minChanY,
            int maxChanX, int maxChanY, DiscreteColorScale colors) {
        //numberColors = colors.length;
        //colorThresholds = new int[numberColors];
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
                    final int x = toViewHorzLin(i);
                    final int y = toViewVertLin(j);
                    final int channelWidth = toViewHorzLin(i + 1) - x;
                    final int channelHeight = y - toViewVertLin(j + 1);
                    //paintChannel: for (int k = 0; k < numberColors; k++) {
                    /* check for min counts first as these are most likely */
                    //if (count <= colorThresholds[k]) {
                    final Color paint = colors.getColor(count);
                    g.setColor(paint);
                    /* inline for speed */
                    g.fillRect(x, y - channelHeight + 1, channelWidth,
                            channelHeight);
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
    void drawHist2d(double[][] counts, int minChanX, int minChanY,
            int maxChanX, int maxChanY) {
        final ColorScale colors = GradientColorScale.getScale(plotLimits
                .getScale());
        colors.setRange(minCount, maxCount);
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
                    g.fillRect(x, y - channelHeight + 1, channelWidth,
                            channelHeight);
                } //end of loop for each point
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
    void drawGate1d(int ll, int ul, boolean noFillMode) {
        clipPlot();
        final int x = toViewHorzLin(ll);
        final int x2 = Math.min(toViewHorzLin(ul + 1), viewRight);
        final int width = x2 - x;
        final int height = viewBottom - viewTop;
        if (noFillMode) {
            g.drawRect(x, viewTop, width, height);
        } else {
            g.fillRect(x, viewTop, width, height);
        }
    }

    /**
     * Draw a 2d Gate
     * 
     * @param gate
     *            the array to be displayed
     */
    void drawGate2d(boolean[][] gate) {
        if (gate != null) {
            for (int j = minY; j <= maxY; j++) { // for each point
                for (int i = minXch; i <= maxXch; i++) {
                    if (gate[i][j]) { //if inside gate
                        final int x = toViewHorzLin(i);
                        final int y = toViewVertLin(j);
                        final int channelWidth = toViewHorzLin(i + 1) - x;
                        final int channelHeight = y - toViewVertLin(j + 1);
                        g.fillRect(x, y - channelHeight + 1, channelWidth,
                                channelHeight);
                    }
                }
            }// --end for each point
        }
        g.setPaintMode();
    }

    /**
     * Setting a 1d Gate
     * 
     * @param gatePoints
     *            gate points to be drawn (in graphics coordinates)
     * @since Version 0.5
     */
    void settingGate1d(Polygon gatePoints) {
        clipPlot();
        if (gatePoints.npoints > 0) {
            final int x1 = gatePoints.xpoints[gatePoints.npoints - 1];
            if (gatePoints.npoints > 1) {
                markAreaOutline1d(
                        toDataHorz(gatePoints.xpoints[gatePoints.npoints - 2]),
                        toDataHorz(x1));
            } else {
                g.drawLine(x1, viewBottom, x1, viewTop);
            }
        }
    }

    /**
     * Determine the outline of an area in a 1d plot.
     * 
     * @param x1
     *            a point in plot coordinates
     * @param x2
     *            a point in plot coordinates
     * @return a rectangle in graphics coordinates that will highlight the
     *         channels indicated
     */
    Rectangle getRectangleOutline1d(int x1, int x2) {
        final int height = viewBottom - viewTop;//Full plot vertically
        final int x;
        final int width;

        if (x1 < x2) {
            final int xv1 = toViewHorzLin(x1);
            //pixel before next channel
            final int xv2 = toViewHorzLin(x2 + 1) - 1;
            x = xv1 + 1;
            width = xv2 - xv1;
        } else if (x1 > x2) {
            //pixel before next channel
            final int xv1 = toViewHorzLin(x1 + 1) - 1;
            final int xv2 = toViewHorzLin(x2);
            x = xv2 + 1;
            width = xv1 - xv2;
            //so both at the same point shows something
        } else {
            final int xv1 = toViewHorzLin(x1);
            //pixel before next channel
            final int xv2 = toViewHorzLin(x2 + 1) - 1;
            x = xv1;
            //At least 1 wide
            width = Math.max(xv2 - xv1, 1);
        }
        return new Rectangle(x, viewTop, width, height);
    }

    /**
     * Mark the outline of an area in a 1d plot.
     * 
     * @param x1
     *            a point in plot coordinates
     * @param x2
     *            a point in plot coordinates
     */
    void markAreaOutline1d(int x1, int x2) {
        clipPlot();
        g.draw(getRectangleOutline1d(x1, x2));
    }

    /**
     * Given a rectangle in plot coordinates, return the bounding rectangle in
     * graphics coordinates.
     * 
     * @param channels
     *            in plot coordinates
     * @return in graphics coordinates
     */
    Rectangle getRectangleOutline2d(Rectangle channels) {
        final int highX = (int) channels.getMaxX();
        final int highY = (int) channels.getMaxY();
        return getRectangleOutline2d(
                Bin.Factory.create(channels.getLocation()), Bin.Factory.create(
                        highX, highY));
    }

    /**
     * Returns the rectangle that includes the box of plot coordinates indicated
     * by the given plot coordinates.
     * 
     * @param p1
     *            in plot coordinates
     * @param p2
     *            in plot coordinates
     * @return in graphics coordinates
     */
    Rectangle getRectangleOutline2d(Bin p1, Bin p2) {
        final int x1 = p1.getX();
        final int y1 = p1.getY();
        final int x2 = p2.getX();
        final int y2 = p2.getY();
        final int x, y;
        final int width, height;

        /* Horizontal */
        if (x1 < x2) {
            final int xv1 = toViewHorzLin(x1);
            /* pixel before next channel */
            final int xv2 = toViewHorzLin(x2 + 1) - 1;
            x = xv1 + 1;
            width = xv2 - xv1;
        } else if (x1 > x2) {
            /* pixel before next channel */
            final int xv1 = toViewHorzLin(x1 + 1) - 1;
            final int xv2 = toViewHorzLin(x2);
            x = xv2 + 1;
            width = xv1 - xv2;
        } else {//same horizontal
            final int xv1 = toViewHorzLin(x1);
            //pixel before next channel
            final int xv2 = toViewHorzLin(x2 + 1) - 1;
            x = xv1;
            /* At least 1 wide */
            width = Math.max(xv2 - xv1, 1);
        }
        /* Vertical (y view starts at top right corner) */
        if (y2 < y1) {
            final int yv1 = toViewVertLin(y1 + 1);
            /* pixel before next channel */
            final int yv2 = toViewVertLin(y2) - 1;
            y = yv1 + 1;
            height = yv2 - yv1;
        } else if (y1 < y2) {
            /* pixel before next channel */
            final int yv1 = toViewVertLin(y1) - 1;
            final int yv2 = toViewVertLin(y2 + 1);
            y = yv2 + 1;
            height = yv1 - yv2;
        } else {//same vertical
            final int yv1 = toViewVertLin(y1);
            /* pixel before next channel */
            final int yv2 = toViewVertLin(y2 + 1);
            y = yv2;
            /* At least 1 tall */
            height = Math.max(yv1 - yv2, 1);
        }
        return new Rectangle(x, y, width, height);
    }

    /**
     * Mark an area whose corners are indicated by the given points.
     * 
     * @param p1
     *            in plot coordinates
     * @param p2
     *            in plot coordinates
     */
    void markArea2dOutline(Bin p1, Bin p2) {
        clipPlot();
        g.draw(getRectangleOutline2d(p1, p2));
    }

    /**
     * Setting a 2d Gate
     * 
     * @param gatePoints
     *            the points of the gate to be drawn
     * @since Version 0.5
     */
    void settingGate2d(Polygon gatePoints) {
        clipPlot();
        final Polygon p = toView(gatePoints);
        g.drawPolyline(p.xpoints, p.ypoints, p.npoints);
    }

    Polygon toView(Polygon p) {
        final int n = p.npoints;
        final int[] x = new int[n];
        final int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = toViewHorzLin(p.xpoints[i]);
            y[i] = toViewVertLin(p.ypoints[i]);
        }
        return new Polygon(x, y, n);
    }

    /*
     * non-javadoc: Mark a channel for 1d.
     */
    void markChannel1d(int channel, double count) {
        int y2;

        int x1 = toViewHorzLin(channel + 0.5);
        int y1 = viewBottom;
        int x2 = x1;
        if (plotLimits.getScale() == Scale.LINEAR) {
            y2 = toViewVertLinCk(count);
        } else {
            y2 = toViewVertLog(count);
        }
        //draw the line at least a mark min length
        if ((y1 - y2) < graphLayout.MARK_MIN_LENGTH) {
            y2 = viewBottom - graphLayout.MARK_MIN_LENGTH;
        }
        //are we inside the plot area
        if (x1 >= viewLeft && x1 <= viewRight) {
            g.drawLine(x1, y1, x2, y2);
            String label = "" + channel;
            g.drawString(label, x2, y2 - graphLayout.MARK_OFFSET);
        }
    }

    void drawPeakLabels(double[][] peaks) {
        int y1; //bottom of line
        Color initColor = g.getColor();
        setGraphicsFont(font.deriveFont(graphLayout.SCREEN_FONT_SIZE));
        g.setColor(COLOR_MAP.getPeakLabel());
        for (int i = 0; i < peaks[0].length; i++) {
            int x1 = toViewHorzLin(peaks[0][i] + 0.5);
            int x2 = x1;
            if (plotLimits.getScale() == Scale.LINEAR) {
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

    /*
     * non-javadoc: Mark a channel for 2d
     */
    void markChannel2d(Bin p) {
        final Rectangle r = getRectangleOutline2d(p, p);
        final String label = "" + p.getX() + "," + p.getY();
        g.draw(r);
        g.drawString(label, (int) r.getMaxX() + graphLayout.MARK_OFFSET, r.y
                - graphLayout.MARK_OFFSET);
    }

    /*
     * non-javadoc: Mark an area in a 1 d plot
     * 
     * @lowChan lower channel @highChan upper channel
     *  
     */
    void markArea1d(int lowChan, int highChan, double[] counts) {
        int minChan = Math.max(minXch, lowChan);
        int maxChan = Math.min(maxXch, highChan);
        final Polygon fill = new Polygon();
        final boolean log = plotLimits.getScale() != Scale.LINEAR;
        int xi = toViewHorzLin(minChan);
        int yi = log ? toViewVertLog(0) : toViewVertLinCk(0);
        fill.addPoint(xi, yi);
        int lastx = xi;
        int lasty = yi;
        int x = xi;
        int y = yi;
        /* vertical traverse, followed by horizontal */
        for (int i = minChan; i <= maxChan; i++) {
            y = log ? toViewVertLog(counts[i]) : toViewVertLinCk(counts[i]);
            if (y != lasty) {
                fill.addPoint(x, y);
                lasty = y;
            }
            x = Math.min(viewRight, toViewHorzLin(i + 1));
            if (!(x == lastx && y == lasty)) {
                fill.addPoint(x, y);
                lastx = x;
            }
        }
        if (y != yi) {//go back to bottom on last
            y = yi;
            fill.addPoint(x, y);
        }
        g.fill(fill);
    }

    /**
     * Mark an area in a 2d plot.
     * 
     * @param r
     *            rectangle in graphics coordinates
     */
    void markArea2d(Rectangle r) {
        clipPlot();
        g.fill(r);
    }

    /*
     * non-javadoc: Draw a line in data co-ordinates
     * 
     * @param side
     * 
     * @since Version 0.5
     */
    void drawDataLine(int x1, int y1, int x2, int y2) {
        g.drawLine(toViewHorzLin(x1), toViewVertLin(y1), toViewHorzLin(x2),
                toViewVertLin(y2));
    }

    /*
     * non-javadoc: Convert to data corodinates, given a screeen point. These
     * routines do not have to be as fast as the to view ones, as it is not very
     * often we want to go this way. The ones that return int are faster.
     */
    synchronized Bin toData(Point viewPoint) {
        return Bin.Factory.create(toDataHorz(viewPoint.x),
                toDataVert(viewPoint.y));
    }

    /*
     * non-javadoc: Give the horizontal plot coordinate for the given graphics
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

    /*
     * non-javadoc: Give the vertical plot coordinate for the given graphics
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

    /*
     * non-javadoc: Convert data point to view point
     */
    Point toViewLin(Bin dataPoint) {
        Point viewPoint = new Point(toViewHorzLin(dataPoint.getX()),
                toViewVertLin(dataPoint.getY()));
        return (viewPoint);
    }

    /*
     * non-javadoc: Get the middle point of the plot usefull for drawing title
     * and labels
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

    /*
     * non-javadoc: Convert horizontal channel coordinate to the graphics
     * coordinate which represents the "low" (left) side of the bin.
     */
    private int toViewHorzLin(double data) {
        int view = (int) (viewLeft + (conversionX * (data - minXch)));
        return view;
    }

    /*
     * non-javadoc: Convert vertical channel coordinate to the graphics
     * coordinate which represents the "low" (bottom) side of the bin.
     */
    private int toViewVertLin(double data) {
        final int view = (int) (viewBottom - (conversionY * (data - minY)));
        return view;
    }

    /*
     * non-javadoc: Convert vertical data to vertical view (screen) screen
     * vertical linear scale.
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

    /*
     * non-javadoc: Convert data vertical to view vertical for Log scale
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

    /*
     * non-javadoc: Take the log of a data point if valid to otherwise return
     * fake zero
     * 
     * @param point point to take log of
     */
    private double takeLog(double point) {
        return Math.log(point > 0.0 ? point : LOG_FAKE_ZERO);
    }
}