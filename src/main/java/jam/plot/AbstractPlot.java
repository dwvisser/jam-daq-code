package jam.plot;

import jam.data.AbstractHist1D;
import jam.data.AbstractHistogram;
import jam.data.Dimensional;
import jam.data.Gate;
import jam.global.RunInfo;
import jam.plot.color.PlotColorMap;
import jam.plot.common.Scale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import static javax.swing.SwingConstants.*;

/**
 * Abstract class for displayed plots.
 * @version 0.5
 * @see jam.plot.Plot1d
 * @see jam.plot.Plot2d
 * @since JDK 1.1
 * @author Ken Swartz
 */
abstract class AbstractPlot implements PreferenceChangeListener, Dimensional,
        Plot {

    /**
     * The currently selected gate.
     */
    protected transient Gate currentGate;

    /**
     * last point mouse moved to, uses plot coordinates when selecting an area,
     * and uses graphics coordinates when setting a gate (FIX?)
     */
    protected transient final Point lastMovePoint = new Point();

    /**
     * Descriptor of domain and range of histogram to plot.
     */
    protected transient Limits limits;

    /**
     * Channels that have been marked by clicking or typing.
     */
    protected transient final List<Bin> markedChannels = new ArrayList<>();

    protected transient final Options options = new Options();

    /**
     * Plot graphics handler.
     */
    protected transient final Painter painter;

    /**
     * The actual panel.
     */
    protected transient final PlotPanel panel = new PlotPanel(this);

    /** Number of Histogram to plot */
    private transient int plotHistNum = -1;

    /* Gives channels of mouse click. */
    protected transient final PlotMouse plotMouse;

    protected transient final PlotSelection plotSelection;

    /** Gate points in plot coordinates (channels). */
    protected final Polygon pointsGate = new Polygon();

    private transient Limitable scrollbars;

    /**
     * Size of plot window in channels.
     */
    protected transient Size size = new Size(0);

    /**
     * configuration for screen plotting
     */
    protected transient Dimension viewSize;

    /**
     * Constructor
     */
    protected AbstractPlot(final PlotSelection plotSelection) {
        super();
        this.plotSelection = plotSelection;
        panel.setOpaque(true);
        panel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        painter = new Painter(this);
        // Create plot mouse
        plotMouse = new PlotMouse(painter);
        panel.addMouseListener(plotMouse);
        // Setup preferences
        initPrefs();
        PlotPreferences.PREFS.addPreferenceChangeListener(this);
    }

    /*
     * non-javadoc: add scrollbars
     */
    protected void addScrollBars(final Limitable scroller) {
        scrollbars = scroller;
    }

    /**
     * Autoscale the counts scale. Set maximum scale to 110 percent of maximum
     * number of counts in view. Can't call refresh because we need to use the
     * counts before refreshing.
     */
    protected final void autoCounts() {
        final AbstractHistogram plotHist = getHistogram();
        copyCounts(plotHist);
        limits.setMinimumCounts(110 * findMinimumCounts() / 100);
        if (findMaximumCounts() > 5) {
            limits.setMaximumCounts(110 * findMaximumCounts() / 100);
        } else {
            limits.setMaximumCounts(5);
        }
        /* scroll bars do not always reset on their own */
        scrollbars.update();
        panel.repaint();
    }

    /**
     * Clears the area selection clip.
     */
    protected final void clearSelectingAreaClip() {
        synchronized (plotSelection.areaClip) {
            plotSelection.areaClip.setSize(0, 0);
        }
    }

    /**
     * Paints titles, labels and tick marks.
     * @param plotHistogram
     *            for which to paint
     */
    protected void paintTextAndTicks(final AbstractHistogram plotHistogram) {
        painter.drawTitle(plotHistogram.getTitle(), TOP);
        painter.drawNumber(plotHistogram.getNumber(), new int[0]);
        painter.drawTicks(BOTTOM);
        painter.drawLabels(BOTTOM);
        painter.drawTicks(LEFT);
        painter.drawLabels(LEFT);
    }

    protected abstract void copyCounts(AbstractHistogram hist);

    protected abstract void displayFit(double[][] signals,
            double[] background, double[] residuals, int lowerLimit);

    /**
     * Displays a gate on the plot.
     * @param gate
     *            the gate to be displayed
     */
    protected void displayGate(final Gate gate) {
        synchronized (this) {
            final AbstractHistogram plotHist = getHistogram();
            if (plotHist != null && plotHist.getGateCollection().hasGate(gate)) {
                panel.setDisplayingGate(true);
                setCurrentGate(gate);
                panel.repaint();
            } else {
                error("Can't display '" + gate + "' on histogram '" + plotHist
                        + "'.");
            }
        }
    }

    /*
     * non-javadoc: Set the histogram to plot. If the plot limits are null, make
     * one save all neccessary histogram parameters to local variables. Allows
     * general use of data set.
     */
    protected void displayHistogram(final AbstractHistogram hist) {
        synchronized (this) {
            limits = Limits.getLimits(hist);
            if (hist == null) {// we have a null histogram so fake it
                plotHistNum = -1;
                size = new Size(100);
            } else {
                plotHistNum = hist.getNumber();
                copyCounts(hist); // copy hist counts
                /* Limits contains handle to Models */
                scrollbars.setLimits(limits);
            }
            panel.setDisplayingGate(false);
            panel.setDisplayingOverlay(false);
            panel.setDisplayingFit(false);
        }
    }

    /**
     * Show the making of a gate, point by point.
     * @param mode
     *            GATE_NEW, GATE_CONTINUE, GATE_SAVE or GATE_CANCEL
     * @param pChannel
     *            channel coordinates of clicked channel
     * @param pPixel
     *            screen coordinates of click
     */
    protected abstract void displaySetGate(GateSetMode mode, Bin pChannel,
            Point pPixel);

    protected void error(final String mess) {
        final Runnable task = () -> {
            final String plotErrorTitle = "Plot Error";
            JOptionPane.showMessageDialog(panel, mess, plotErrorTitle,
                    JOptionPane.ERROR_MESSAGE);
        };
        SwingUtilities.invokeLater(task);
    }

    /*
     * non-javadoc: Expand the region viewed.
     */
    protected void expand(final Bin bin1, final Bin bin2) {
        final int xCoord1 = bin1.getX();
        final int xCoord2 = bin2.getX();
        final int yCoord1 = bin1.getY();
        final int yCoord2 = bin2.getY();
        int xll = Math.min(xCoord1, xCoord2);// x lower limit
        int xul = Math.max(xCoord1, xCoord2);// x upper limit
        // check for beyond extremes and set to extremes
        if ((xll < 0) || (xll > size.getSizeX() - 1)) {
            xll = 0;
        }
        if ((xul < 0) || (xul > size.getSizeX() - 1)) {
            xul = size.getSizeX() - 1;
        }
        int yll = Math.min(yCoord1, yCoord2);// y lower limit
        int yul = Math.max(yCoord1, yCoord2);// y upper limit
        /* check for beyond extremes and set to extremes */
        if ((yll < 0) || (yll > size.getSizeY() - 1)) {
            yll = 0;
        }
        if ((yul < 0) || (yul > size.getSizeY() - 1)) {
            yul = size.getSizeY() - 1;
        }
        limits.setMinimumX(xll);
        limits.setMaximumX(xul);
        limits.setMinimumY(yll);
        limits.setMaximumY(yul);
        refresh();
    }

    /**
     * Find the maximum number of counts in the region of interest.
     * @return the maximum number of counts in the region of interest
     */
    protected abstract int findMaximumCounts();

    /**
     * Find the minimum number of counts in the region of interest.
     * @return the minimum number of counts in the region of interest
     */
    protected abstract int findMinimumCounts();

    protected abstract int getChannel(double energy);

    protected final Component getComponent() {
        return panel;
    }

    protected ComponentPrintable getComponentPrintable() {
        return new ComponentPrintable(panel);
    }

    /**
     * Get histogram counts at the specified point, which is given in channel
     * coordinates.
     * @param bin
     *            the channel
     * @return the counts at the channel
     */
    protected abstract double getCount(Bin bin);

    /*
     * non-javadoc: Gets the current date and time as a String.
     */
    private String getDate() {
        final Date date = new Date(); // getDate and time
        final DateFormat datef = DateFormat.getDateTimeInstance(); // default
        // format
        datef.setTimeZone(TimeZone.getDefault()); // set time zone
        return datef.format(date); // format date
    }

    /**
     * Get the energy for a channel.
     * @param channel
     *            the channel
     * @return the energy for the channel
     */
    protected abstract double getEnergy(double channel);

    public AbstractHistogram getHistogram() {
        synchronized (this) {
            return plotHistNum < 0 ? null : AbstractHistogram // NOPMD
                    .getHistogram(plotHistNum);
        }
    }

    /**
     * @return limits are how the histogram is to be drawn
     */
    protected Limits getLimits() {
        return limits;
    }

    protected PlotMouse getPlotMouse() {
        return plotMouse;
    }

    protected Size getSize() {
        return size;
    }

    /*
     * non-javadoc: Plot has a valid histogram
     */
    protected boolean hasHistogram() {
        return plotHistNum >= 0;
    }

    /**
     * Start marking an area.
     * @param bin
     *            starting point in plot coordinates
     */
    protected final void initializeSelectingArea(final Bin bin) {
        setSelectingArea(true);
        plotSelection.start.setChannel(bin);
        setLastMovePoint(bin.getPoint());
    }

    private void initPrefs() {
        options.setIgnoreChFull(PlotPreferences.PREFS.getBoolean(
                PlotPreferences.AUTO_IGNORE_FULL, true));
        options.setIgnoreChZero(PlotPreferences.PREFS.getBoolean(
                PlotPreferences.AUTO_IGNORE_ZERO, true));
        panel.setColorMode(PlotPreferences.PREFS.getBoolean(
                PlotPreferences.BLACK_BACKGROUND, false));
        options.setNoFillMode(!PlotPreferences.PREFS.getBoolean(
                PlotPreferences.HIGHLIGHT_GATE, true));
    }

    public boolean isPrinting() {
        return options.isPrinting();
    }

    /**
     * @return <code>true</code> if the area selection clip is clear
     */
    protected final boolean isSelectingAreaClipClear() {
        synchronized (plotSelection.areaClip) {
            return plotSelection.areaClip.height == 0;
        }
    }

    /**
     * Mark an area on the plot.
     * @param bin1
     *            a corner of the rectangle in plot coordinates
     * @param bin2
     *            a corner of the rectangle in plot coordinates
     */
    protected abstract void markArea(Bin bin1, Bin bin2);

    /**
     * Mark a channel on the plot.
     * @param bin
     *            graphics coordinates on the plot where the channel is
     */
    protected final void markChannel(final Bin bin) {
        setMarkingChannels(true);
        markedChannels.add((Bin) bin.clone());
        panel.repaint();
    }

    /**
     * Not used.
     * @param mouseEvent
     *            created when the mouse is moved
     */
    abstract public void mouseMoved(MouseEvent mouseEvent);

    protected abstract void overlayHistograms(List<AbstractHist1D> overlayHists);

    /**
     * Method overridden for 1 and 2 d for painting fits.
     * @param graphics
     *            the graphics context
     */
    abstract public void paintFit(Graphics graphics);

    /**
     * Method overridden for 1 and 2 d for painting the gate.
     * @param graphics
     *            the graphics context
     */
    abstract public void paintGate(Graphics graphics);

    /*
     * non-javadoc: Paints header for plot to screen and printer. Also sets
     * colors and the size in pixels for a plot.
     */
    public void paintHeader(final Graphics graphics) {
        graphics.setColor(PlotColorMap.getInstance().getForeground());
        if (options.isPrinting()) { // output to printer
            painter.drawDate(getDate()); // date
            painter.drawRun(RunInfo.getInstance().runNumber); // run number
        }
        painter.drawBorder();
    }

    /**
     * Method overriden for 1 and 2 d plots for painting the histogram.
     * @param graphics
     *            the graphics context
     */
    abstract public void paintHistogram(Graphics graphics);

    /**
     * Method for painting a clicked area.
     * @param graphics
     *            the graphics context
     */
    abstract public void paintMarkArea(Graphics graphics);

    /**
     * Method for painting a clicked channel.
     * @param graphics
     *            the graphics context
     */
    abstract public void paintMarkedChannels(Graphics graphics);

    /**
     * Paint called if mouse moved is enabled.
     * @param graphics
     *            the graphics context
     */
    public final void paintMouseMoved(final Graphics graphics) {
        if (panel.isSettingGate()) {
            paintSettingGate(graphics);
        } else if (panel.isSelectingArea()) {
            paintSelectingArea(graphics);
        }
    }

    /**
     * Method overriden for 1 and 2 d for painting overlays.
     * @param graphics
     *            the graphics context
     */
    abstract public void paintOverlay(Graphics graphics);

    /**
     * Method for painting a area while it is being selected.
     * @param graphics
     *            the graphics context
     */
    abstract protected void paintSelectingArea(Graphics graphics);

    /**
     * Method for painting segments while setting a gate.
     * @param graphics
     *            the graphics context
     */
    abstract public void paintSetGatePoints(Graphics graphics);

    /**
     * Method for painting segments while setting a gate.
     * @param graphics
     *            the graphics context
     */
    abstract protected void paintSettingGate(Graphics graphics);

    private boolean plotDataExists() {
        synchronized (this) {
            final AbstractHistogram plotHist = getHistogram();
            return plotHist != null && !plotHist.isClear();
        }
    }

    /**
     * @see PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
     */
    public void preferenceChange(final PreferenceChangeEvent pce) {
        final boolean newValue = Boolean.parseBoolean(pce.getNewValue());
        switch (pce.getKey()) {
            case PlotPreferences.AUTO_IGNORE_ZERO:
                options.setIgnoreChZero(newValue);
                if (plotDataExists()) {
                    autoCounts();
                }
                break;
            case PlotPreferences.AUTO_IGNORE_FULL:
                options.setIgnoreChFull(newValue);
                if (plotDataExists()) {
                    autoCounts();
                }
                break;
            case PlotPreferences.BLACK_BACKGROUND:
                panel.setColorMode(newValue);
                break;
            case PlotPreferences.HIGHLIGHT_GATE:
                options.setNoFillMode(!newValue);
                break;
        }
    }

    /**
     * Refresh the display.
     */
    protected void refresh() {
        if (scrollbars != null) {
            scrollbars.update();
        }
        final AbstractHistogram plotHist = getHistogram();
        copyCounts(plotHist);
        panel.repaint();
    }

    protected abstract void removeOverlays();

    /**
     * Reset state
     */
    protected void reset() {
        synchronized (this) {
            panel.setDisplayingGate(false);
            panel.setDisplayingFit(false);
            panel.setDisplayingOverlay(false);
            panel.setSelectingArea(false);
            panel.setAreaMarked(false);
            setMarkingChannels(false);
        }
    }

    private void setCurrentGate(final Gate gate) {
        synchronized (this) {
            currentGate = gate;
        }
    }

    /**
     * set full range X
     */
    protected void setFull() {
        limits.setMinimumX(0);
        limits.setMaximumX(size.getSizeX() - 1);
        limits.setMinimumY(0);
        limits.setMaximumY(size.getSizeY() - 1);
        refresh();
    }

    /**
     * Set the last point the cursor was moved to.
     * @param point
     *            the last point
     */
    protected final void setLastMovePoint(final Point point) {
        synchronized (lastMovePoint) {
            lastMovePoint.setLocation(point);
        }
    }

    /*
     * non-javadoc: Update layout.
     */
    protected void setLayout(final GraphicsLayout.Type type) {
        painter.setLayout(type);
    }

    /**
     * Set the scale to linear scale
     */
    protected void setLinear() {
        limits.setScale(Scale.LINEAR);
        refresh();
    }

    /**
     * Set the scale to log scale
     */
    protected void setLog() {
        limits.setScale(Scale.LOG);
        panel.repaint();
    }

    protected void setMarkArea(final boolean marked) {
        panel.setAreaMarked(marked);
    }

    protected void setMarkingChannels(final boolean marking) {
        panel.setMarkingChannels(marking);
        if (!panel.isMarkingChannels()) {
            markedChannels.clear();
        }
    }

    /**
     * Set the maximum counts limit on the scale, but constrained for scrolling.
     * @param maxC
     *            maximum counts
     */
    protected void setMaximumCountsConstrained(final int maxC) {
        final int FS_MIN = 5; // Minimum that Counts can be set to
        int temp = Math.max(maxC, FS_MIN);
        final int FS_MAX = 1000000; // Maximum that counts can be set to.
        temp = Math.min(temp, FS_MAX);
        limits.setMaximumCounts(temp);
    }

    /* Plot mouse methods */

    /*
     * non-javadoc: method to set Counts scale
     */
    protected void setRange(final int limC1, final int limC2) {
        if (limC1 <= limC2) {
            limits.setMinimumCounts(limC1);
            limits.setMaximumCounts(limC2);
        } else {
            limits.setMinimumCounts(limC2);
            limits.setMaximumCounts(limC1);
        }
        refresh();
    }

    protected void setRenderForPrinting(final boolean rfp,
            final PageFormat format) {
        synchronized (this) {
            options.setPrinting(rfp);
            panel.setPageFormat(format);
        }
    }

    protected void setSelectingArea(final boolean selecting) {
        panel.setSelectingArea(selecting);
    }

    /* End Plot mouse methods */

    public void setView(final PageFormat format) {
        painter.setView(format);
    }

    public void setViewSize(final Dimension size) {
        this.viewSize = size;
    }

    /**
     * Updated the display, resetting so that fits, gates and overlays are no
     * longer shown.
     */
    protected void update() {
        reset();
        if (getCounts() != null) {
            refresh();
        }
    }

    public void update(final Graphics graph) {
        painter.update(graph, viewSize, limits);
    }
}