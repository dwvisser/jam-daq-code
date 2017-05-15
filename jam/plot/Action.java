package jam.plot;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jam.data.AbstractHist1D;
import jam.data.AbstractHistogram;
import jam.data.DataUtility;
import jam.data.peaks.GaussianConstants;
import jam.global.*;
import jam.plot.common.Scale;
import jam.ui.Console;
import jam.ui.ConsoleLog;
import jam.ui.SelectionTree;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class the does the actions on plots. Receives commands from buttons and
 * command line. Performs action by performing command on plot, plot1d and
 * plot2d. Commands available:
 * <dl>
 * <dt>update</dt>
 * <dd>update a plot with current data</dd>
 * <dt>expand</dt>
 * <dd>expand channels to view</dd>
 * <dt>zoomin, zoomout</dt>
 * <dt>full</dt>
 * <dd>view all channels</dd>
 * <dt>linear, log</dt>
 * <dt>range</dt>
 * <dd>change the count range</dd>
 * <dt>auto</dt>
 * <dd>auto scale the count range</dd>
 * <dt>area</dt>
 * <dd>get an area</dd>
 * <dt>cancel</dt>
 * </dl>
 * @author Ken Swartz
 * @version 0.5
 */

@Singleton
public final class Action {

    /** Broadcaster for event and gate change */
    private transient final Broadcaster broadcaster;

    private final static String CHANNEL = "channel";

    private final static String ENERGY = "energy";

    private static final Logger LOGGER = Logger.getLogger(Action.class
            .getPackage().getName());

    private final static Map<String, Method> NO_ARG_MAP = new HashMap<>();

    private static final String S_TO = " to ";

    private static final String UNUSED = "unused";

    static {
        final List<String> NO_ARG_CMDS = new ArrayList<>();
        NO_ARG_CMDS.add(PlotCommands.HELP);
        NO_ARG_CMDS.add(PlotCommands.EXPAND);
        NO_ARG_CMDS.add(PlotCommands.ZOOMIN);
        NO_ARG_CMDS.add(PlotCommands.ZOOMOUT);
        NO_ARG_CMDS.add(PlotCommands.ZOOMVERT);
        NO_ARG_CMDS.add(PlotCommands.ZOOMHORZ);
        NO_ARG_CMDS.add(PlotCommands.FULL);
        NO_ARG_CMDS.add(PlotCommands.LINEAR);
        NO_ARG_CMDS.add(PlotCommands.LOG);
        NO_ARG_CMDS.add(PlotCommands.AREA);
        NO_ARG_CMDS.add(PlotCommands.GOTO);
        NO_ARG_CMDS.add(PlotCommands.NETAREA);
        NO_ARG_CMDS.add(PlotCommands.UPDATE);
        NO_ARG_CMDS.add(PlotCommands.AUTO);
        NO_ARG_CMDS.add(PlotCommands.CANCEL);
        NO_ARG_CMDS.add(PlotCommands.SCALE);
        NO_ARG_CMDS.add(PlotCommands.CURSOR);
        for (String command : NO_ARG_CMDS) {
            try {
                final Method method = Action.class.getDeclaredMethod(command);
                NO_ARG_MAP.put(command, method);
            } catch (NoSuchMethodException nsme) {
                LOGGER.log(Level.SEVERE, nsme.getMessage(), nsme);
            }
        }
    }

    private static boolean existsAndIsCalibrated(final AbstractHistogram hist) {
        return (hist != null && hist instanceof AbstractHist1D) && ((AbstractHist1D) hist)
                .isCalibrated();
    }

    private final transient List<Bin> clicks = new ArrayList<>();

    /** Is there a command present */
    private transient boolean commandPresent;

    private transient int countHigh; // NOPMD

    private transient int countLow; // NOPMD

    /** current command being processed */
    private transient String currentCommand = "";

    private transient final Bin cursorBin;

    private transient final PlotFit inquire;

    /** Command requires a cursor input */
    private transient boolean cursorCommand;

    private transient final NumberFormat numFormat;

    /** Plot displayer */
    private transient CurrentPlotAccessor plotAccessor;

    /** Accessed by Display. */
    private transient boolean settingGate;

    /** Output text to */
    private transient final MessageHandler textOut;

    private transient final CommandFinder commandFinder;

    private transient final AutoCounts autoCounts = new AutoCounts();

    private transient final JamStatus status;

    private transient final Console console;

    /**
     * Master constructor has no broadcaster.
     * @param disp
     *            the histogram displayer
     * @param console
     *            Jam's console component
     */
    @Inject
    Action(final Console console, final ConsoleLog consoleLog,
            final CommandFinder finder, final JamStatus status,
            final Broadcaster broadcaster) {
        super();
        this.console = console;
        this.broadcaster = broadcaster;
        this.commandFinder = finder;
        textOut = consoleLog;
        this.status = status;
        cursorBin = Bin.create();
        commandPresent = false;
        settingGate = false;
        inquire = PlotFit.getInstance(); // class with area/centroid routines
        /* numFormat for formatting energy output */
        numFormat = NumberFormat.getInstance();
        numFormat.setGroupingUsed(false);
        final int fracDigits = 2;
        numFormat.setMinimumFractionDigits(fracDigits);
        numFormat.setMaximumFractionDigits(fracDigits);
        PlotPreferences.PREFS.addPreferenceChangeListener(this.autoCounts);
    }

    private void cloneClickAndAdd(final Bin bin) {
        synchronized (this) {
            clicks.add((Bin) bin.clone());
        }
    }

    /**
     * Calculate the area and centroid for a region. Maybe we should copy
     * inquire methods to this class?
     */
    @SuppressWarnings(UNUSED)
    private void area() {// NOPMD
        if (commandPresent) {
            areaCommandPresent();
        } else {
            cursorCommand = true;
            init();
            final String name = ((AbstractHistogram) SelectionTree
                    .getCurrentHistogram()).getFullName().trim();
            textOut.messageOut("Area for " + name + " from channel ",
                    MessageHandler.NEW);
        }
    }

    /**
     * @param currentPlot
     */
    private void areaCommandPresent() {
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        if (clicks.isEmpty()) {
            synchronized (cursorBin) {
                cloneClickAndAdd(cursorBin);
                currentPlot.initializeSelectingArea(cursorBin);
                currentPlot.markChannel(cursorBin);
                textOut.messageOut(getCoordString(cursorBin) + S_TO);
            }
        } else {
            currentPlot.setSelectingArea(false);
            final Bin lim1 = getClick(0);
            if (currentPlot.getDimensionality() == 1) {
                synchronized (cursorBin) {
                    textOut.messageOut(String.valueOf(cursorBin.getX()));
                    final double[] counts = (double[]) currentPlot.getCounts();
                    final double area = inquire.getArea(counts, lim1,
                            cursorBin);
                    final double centroid = inquire.getCentroid(counts, lim1,
                            cursorBin);
                    final double fwhm = inquire.getFWHM(counts, lim1,
                            cursorBin);
                    currentPlot.markChannel(cursorBin);
                    currentPlot.markArea(lim1, cursorBin);
                    textOut.messageOut(":  Area = " + numFormat.format(area)
                            + ", Centroid = " + numFormat.format(centroid)
                            + ", FWHM = " + numFormat.format(fwhm),
                            MessageHandler.END);
                }
            } else {// 2D histogram
                synchronized (cursorBin) {
                    textOut.messageOut(getCoordString(cursorBin));
                    final double[][] counts = (double[][]) currentPlot
                            .getCounts();
                    final double area = inquire.getArea(counts, lim1,
                            cursorBin);
                    currentPlot.markChannel(cursorBin);
                    currentPlot.markArea(lim1, cursorBin);
                    textOut.messageOut(":  Area = " + numFormat.format(area),
                            MessageHandler.END);
                }
            }
            done();
        }
    }

    /**
     * Auto scale the plot.
     */
    private void auto() {
        cursorCommand = false;
        plotAccessor.getPlotContainer().autoCounts();
        done();
    }

    /**
     * Cancel current command
     */
    @SuppressWarnings(UNUSED)
    private void cancel() {// NOPMD
        cursorCommand = false;
        textOut.messageOutln();
        done();
    }

    /*
     * non-javadoc: display the counts at cursor
     */
    @SuppressWarnings(UNUSED)
    private void cursor() {// NOPMD
        /* output counts for the channel */
        final double count;
        final int xch, ych;
        /* check that a histogram is defined */
        final AbstractHistogram hist = (AbstractHistogram) SelectionTree
                .getCurrentHistogram();
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        synchronized (cursorBin) {
            xch = cursorBin.getX();
            ych = cursorBin.getY();
            // Bins can be made by keyboard input too, so checking for good
            // values here.
            if (xch < 0 || xch >= hist.getSizeX() || ych < 0) {
                return; // NOPMD
            }

            if (ych >= hist.getSizeY() && hist.getDimensionality() > 1) {
                return;
            }

            count = getCounts(cursorBin);
        }
        currentPlot.markChannel(cursorBin);
        final StringBuilder binText = new StringBuilder();
        if (currentPlot.getDimensionality() == 1) {
            binText.append("Bin ").append(xch).append(":  Counts = ").append(
                    numFormat.format(count));
            if (existsAndIsCalibrated(hist)) {
                final double chEnergy = currentPlot.getEnergy(xch);
                binText.append("  Energy = ").append(
                        numFormat.format(chEnergy));
            }
        } else {// 2 Dim plot
            binText.append("Bin ").append(xch).append(',').append(ych).append(
                    ":  Counts = ").append(numFormat.format(count));
        }
        textOut.messageOutln(binText.toString());
        done();
    }

    /**
     * @param hist
     *            the first element of which is the number of the hist to
     *            display
     */
    protected void display(final List<Double> hist) {
        if (!commandPresent) {
            cursorCommand = false;
            init();
            textOut.messageOut("Display histogram number: ",
                    MessageHandler.NEW);
        }
        if (!hist.isEmpty()) {
            final double dNum = hist.get(0);
            final int num = (int) dNum;
            final AbstractHistogram histogram = AbstractHistogram
                    .getHistogram(num);
            if (histogram == null) {
                textOut.messageOut(Integer.toString(num), MessageHandler.END);
                LOGGER.severe("There is no histogram numbered " + num + ".");
            } else {
                SelectionTree.setCurrentHistogram(histogram);
                status.setCurrentGroup(DataUtility.getGroup(histogram));
                textOut.messageOut(Integer.toString(num) + " ",
                        MessageHandler.END);
                plotAccessor.getPlotContainer().removeOverlays();
                broadcaster.broadcast(BroadcastEvent.Command.OVERLAY_OFF);
                broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
                        histogram);
                if (hist.size() > 1) {
                    if (histogram.getDimensionality() == 1) {
                        final List<Double> pass = hist.subList(1, hist.size());
                        overlay(pass);
                    } else {
                        LOGGER
                                .warning(histogram.getFullName().trim()
                                        + " is not 1D, so you may not overlay other histograms.");
                    }
                } else {
                    done();
                }
            }
        }
    }

    /**
     * Publicly exposed commandable member.
     */
    public transient final Commandable commandable = new Commandable() {
        public String getCurrentCommand() {
            synchronized (this) {
                return Action.this.currentCommand;
            }
        }

        public void setCursor(final Bin cursorIn) {
            synchronized (this) {
                Action.this.cursorBin.setChannel(cursorIn);
            }
        }

        public boolean isCursorCommand() {
            synchronized (this) {
                return Action.this.cursorCommand;
            }
        }

        /*
         * non-javadoc: Command with no parameters
         * 
         * @param inCommand
         */
        public void doCommand(final String inCommand,
                final boolean typedAtConsole) {
            this.doCommand(inCommand, null, typedAtConsole);
        }

        /*
         * non-javadoc: does a command with parameters
         */
        public void doCommand(final String inCommand,
                final List<Double> inParams, final boolean typedAtConsole) {
            synchronized (this) {
                /* if inCommand is null, keep currentCommand */
                if (inCommand != null) {
                    if (inCommand.equals(PlotCommands.CURSOR)) {
                        /* use cursor only if current command does not exist */
                        if (Action.this.currentCommand.length() == 0) {
                            Action.this.currentCommand = inCommand;
                        }
                    } else {
                        /* Not a cursor command so its a "real" command */
                        if (!inCommand.equals(currentCommand)) {
                            /* cancel previous command */
                            done();
                        }
                        Action.this.currentCommand = inCommand;
                    }
                }
                /* check that a histogram is defined */
                if (SelectionTree.getCurrentHistogram() != UnNamed
                        .getSingletonInstance()) {
                    Action.this.doCurrentCommand(inParams, typedAtConsole);
                }
            }
        }
    };

    private void doCurrentCommand(final List<Double> parameters,
            final boolean typedAtConsole) {
        if (PlotCommands.DISPLAY.equals(currentCommand)) {
            display(parameters);
        } else if (PlotCommands.OVERLAY.equals(currentCommand)) {
            overlay(parameters);
        } else if (PlotCommands.RANGE.equals(currentCommand)) {
            range(typedAtConsole);
        } else if (PlotCommands.REBIN.equals(currentCommand)) {
            rebin(parameters);
        } else if (NO_ARG_MAP.containsKey(currentCommand)) {
            try {
                final Method method = NO_ARG_MAP.get(currentCommand);
                method.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException iae) {
                LOGGER.log(Level.SEVERE, iae.getMessage(), iae);
            }
        } else {
            done();
            LOGGER.severe("Plot command, \"" + currentCommand
                    + "\", not recognized.");
        }
    }

    /**
     * A command has been completed so clean up
     */
    private void done() {
        synchronized (this) {
            cursorCommand = true;
            commandPresent = false;
            currentCommand = "";
            clicks.clear();
            plotAccessor.getPlotContainer().setSelectingArea(false);
        }
    }

    /**
     * Expand the region to view.
     */
    @SuppressWarnings(UNUSED)
    private void expand() {// NOPMD
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        if (commandPresent) {
            updateSelectedAreaForClick(currentPlot);
        } else {
            cursorCommand = true;
            init();
            textOut.messageOut("Expand from channel ", MessageHandler.NEW);
        }
    }

    /**
     * @param currentPlot
     */
    private void updateSelectedAreaForClick(final PlotContainer currentPlot) {
        if (clicks.isEmpty()) {
            synchronized (cursorBin) {
                currentPlot.initializeSelectingArea(cursorBin);
                cloneClickAndAdd(cursorBin);
                textOut.messageOut(getCoordString(cursorBin) + S_TO);
            }
        } else {
            currentPlot.setSelectingArea(false);
            synchronized (cursorBin) {
                textOut.messageOut(getCoordString(cursorBin),
                        MessageHandler.END);
                currentPlot.expand(getClick(0), cursorBin);
            }
            this.autoCounts.conditionalAutoCounts(currentPlot);
            done();
        }
    }

    /**
     * Display the full histogram.
     */
    @SuppressWarnings(UNUSED)
    private void full() {// NOPMD
        cursorCommand = false;
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        currentPlot.setFull();
        this.autoCounts.conditionalAutoCounts(currentPlot);
        done();
    }

    /**
     * @param currentPlot
     * @param hist
     * @param fwhm
     * @param centroidError
     * @param centroid
     */
    private void getCalibratedPeakStatistics(final PlotContainer currentPlot,
            final AbstractHistogram hist, final double[] fwhm,
            final double[] centroidError, final double[] centroid) {
        if (existsAndIsCalibrated(hist)) {
            centroid[0] = currentPlot.getEnergy(centroid[0]);
            fwhm[0] = currentPlot.getEnergy(fwhm[0]);
            fwhm[1] = currentPlot.getEnergy(0.0);
            centroidError[0] = currentPlot.getEnergy(centroidError[0]);
            centroidError[1] = currentPlot.getEnergy(0.0);
            fwhm[0] = fwhm[0] - fwhm[1];
            centroidError[0] = centroidError[0] - centroidError[1];
        }
    }

    private Bin getClick(final int bin) {
        return clicks.get(bin);
    }

    private void getNetArea(final double[] netArea, double[] netAreaError,
            double[] channelBkgd, double[] fwhm, double[] centroid,
            double[] centroidErr, final double grossArea,
            final int numChannels, final double[] counts) {
        double netBkgd = 0;
        double[] channel = new double[numChannels];
        double countsHigh = 0;
        double countsLow = 0;
        double area = 0;
        double variance = 0;
        double distance = 0;
        final int[] bgdX1 = {clicks.get(0).getX(), clicks.get(1).getX(),
                clicks.get(2).getX(), clicks.get(3).getX() };
        Arrays.sort(bgdX1);
        final int bkgd1 = bgdX1[0];
        final int bkgd2 = bgdX1[1];
        final int bkgd3 = bgdX1[2];
        final int bkgd4 = bgdX1[3];
        final int x5temp = clicks.get(4).getX();
        final int x6temp = clicks.get(5).getX();
        final int rx1 = Math.min(x5temp, x6temp);
        final int rx2 = Math.max(x5temp, x6temp);
        for (int n = bkgd1; n <= bkgd2; n++) {
            countsLow += counts[n];
        }
        for (int n = bkgd3; n <= bkgd4; n++) {
            countsHigh += counts[n];
        }
        final double avLow = countsLow / (bkgd2 - bkgd1 + 1);
        final double avHigh = countsHigh / (bkgd4 - bkgd3 + 1);
        final double midLow = (bkgd2 + bkgd1) / 2.0;
        final double midHigh = (bkgd4 + bkgd3) / 2.0;
        final double gradient = (avHigh - avLow) / (midHigh - midLow);
        final double intercept = avHigh - (gradient * midHigh);
        /* sum counts between region - background at each channel */
        for (int p = rx1; p <= rx2; p++) {
            area += counts[p];
            channel[p] = p + 0.5;
            channelBkgd[p] = gradient * p + intercept;
            netArea[0] += counts[p] - channelBkgd[p];
            netBkgd += channelBkgd[p];
        }
        for (int n = bkgd1; n <= bkgd4 + 1; n++) {
            channelBkgd[n] = gradient * n + intercept;
        }
        netAreaError[0] = Math.pow(grossArea + netBkgd, 0.5);
        /* calculate weight */
        if (area > 0) { // must have more than zero counts
            for (int i = rx1; i <= rx2; i++) {
                centroid[0] += (i * counts[i] / area);
            }
        } else {
            centroid[0] = 0;
        }
        /* Calculation of Variance */
        for (int i = rx1; i <= rx2; i++) {
            distance = Math.pow((i - centroid[0]), 2);
            variance += counts[i] * distance / (area - 1.0);

        }
        /* Error in Centroid position */
        centroidErr[0] = Math.sqrt(variance) / Math.sqrt(rx2 - rx1 + 1);
        fwhm[0] = GaussianConstants.SIG_TO_FWHM * Math.sqrt(variance);
    }

    /**
     * Goto input channel
     */
    @SuppressWarnings(UNUSED)
    private void go() {// NOPMD
        final String cal = "calibrated";
        final char space = ' ';
        final String intro = "Goto (click on spectrum or type the ";
        final char leftParen = ')';
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        final AbstractHistogram hist = (AbstractHistogram) SelectionTree
                .getCurrentHistogram();
        if (commandPresent) {
            if (clicks.isEmpty()) {
                goNoClicks(currentPlot, hist);
            }
        } else {
            cursorCommand = true;
            init();
            if (currentPlot.getDimensionality() == 1
                    && existsAndIsCalibrated(hist)) {
                final String mess = new StringBuffer(intro).append(cal)
                        .append(space).append(ENERGY).append(leftParen)
                        .append(space).toString();
                textOut.messageOut(mess, MessageHandler.NEW);
            } else {
                final String mess = new StringBuffer(intro).append(CHANNEL)
                        .append(leftParen).append(space).toString();
                textOut.messageOut(mess, MessageHandler.NEW);
            }
        }
    }

    private void goNoClicks(final PlotContainer currentPlot,
            final AbstractHistogram hist) {
        final String sep = ", ";
        final String equal = " = ";
        synchronized (cursorBin) {
            cloneClickAndAdd(cursorBin);
            StringBuffer output = new StringBuffer();
            int xCoord = cursorBin.getX();
            if (existsAndIsCalibrated(hist)) {
                if (currentPlot.getDimensionality() == 1) {
                    output.append(ENERGY).append(equal).append(
                            currentPlot.getEnergy(xCoord)).append(sep).append(
                            CHANNEL).append(equal).append(xCoord);
                }
            } else {
                output.append(CHANNEL).append(equal).append(xCoord);
            }
            if (currentPlot.getDimensionality() == 1
                    && existsAndIsCalibrated(hist)) {
                output = new StringBuffer(ENERGY).append(equal).append(xCoord);
                synchronized (this) {
                    xCoord = currentPlot.getChannel(xCoord);
                    if (xCoord > currentPlot.getSizeX()) {
                        xCoord = currentPlot.getSizeX() - 1;
                    }
                }
                output.append(sep).append(CHANNEL).append(equal)
                        .append(xCoord);
            }
            final int rangeToUse = 100;
            final int halfRange = rangeToUse / 2;
            final int channelLow = xCoord - halfRange;
            final int channelHigh = channelLow + rangeToUse;
            currentPlot
                    .expand(Bin.create(channelLow), Bin.create(channelHigh));
            textOut.messageOut(output.toString(), MessageHandler.END);
        }
        auto();
        done();
    }

    /**
     * @param currentPlot
     * @param hist
     * @param netArea
     * @param netAreaError
     * @param fwhm
     * @param centroidError
     * @param centroid
     * @param channelBackground
     * @param crt
     */
    private void handleClick6(final PlotContainer currentPlot,
            final AbstractHistogram hist) {
        final double[] netArea = new double[1];
        final double[] netAreaError = new double[1];
        final double[] fwhm = new double[2];
        final double[] centroidError = new double[2];
        final double[] centroid = new double[1];
        final double[] channelBackground = new double[currentPlot.getSizeX()];
        // ************ Second Region Marker
        // *********************************
        currentPlot.setSelectingArea(false);
        final Bin bin4;
        synchronized (cursorBin) {
            cloneClickAndAdd(cursorBin);
            bin4 = getClick(4);
            currentPlot.markChannel(cursorBin);
            markClick6inUI(currentPlot, bin4);
        }
        final double[] counts = (double[]) currentPlot.getCounts();
        final double grossArea = inquire.getArea(counts, bin4, cursorBin);
        /* results of next call are passed back in the parameters */
        getNetArea(netArea, netAreaError, channelBackground, fwhm, centroid,
                centroidError, grossArea, currentPlot.getSizeX(), counts);
        getCalibratedPeakStatistics(currentPlot, hist, fwhm, centroidError,
                centroid);
        final char plusMinus = '\u00b1';
        final String crt = "\n\t";
        textOut.messageOut(crt + "Gross Area = " + grossArea + plusMinus
                + numFormat.format(Math.sqrt(grossArea)) + crt + "NetArea = "
                + numFormat.format(netArea[0]) + plusMinus
                + numFormat.format(netAreaError[0]) + crt + "Centroid = "
                + numFormat.format(centroid[0]) + plusMinus
                + numFormat.format(centroidError[0]) + crt + "FWHM = "
                + numFormat.format(fwhm[0]), MessageHandler.END);
        /* Draw Fit on screen by calling DisplayFit in Display.java */
        final int[] bgdPts = {getClick(0).getX(), getClick(1).getX(),
                getClick(2).getX(), getClick(3).getX() };
        Arrays.sort(bgdPts);
        final int lowerLimit = bgdPts[0];
        final int upperLimit = bgdPts[3] + 1;
        final double[] bkgd = new double[upperLimit - lowerLimit + 1];
        System.arraycopy(channelBackground, lowerLimit, bkgd, 0, bkgd.length);
        plotAccessor.getPlotContainer().displayFit(null, bkgd, null,
                lowerLimit);
        done();
    }

    /*
     * non-javadoc: Display help
     */
    @SuppressWarnings(UNUSED)
    private void help() {// NOPMD
        final StringBuilder buffer = new StringBuilder(240);
        buffer
                .append("Commands:\tli - Linear Scale\tlo - Log Scale\ta  - Auto Scale\tra - Range\tex - Expand\tf  - Full view\t zi - Zoom In\tzo - Zoom Out\td  - Display\to  - Overlay\tu  - Update\tg  - GoTo\tar - Area\tn  - Net Area\tre - Rebin\tc  - Bin\t");
        final Collection<String> commands = commandFinder.getAll();
        for (String command : commands) {
            buffer.append(command).append('\t');
        }
        textOut.messageOutln(buffer.toString());
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
     * Set the counts to linear scale.
     */
    private void linear() {
        cursorCommand = false;
        plotAccessor.getPlotContainer().setLinear();
        done();
    }

    /**
     * Set the counts to log scale.
     */
    private void log() {
        cursorCommand = false;
        plotAccessor.getPlotContainer().setLog();
        done();
    }

    /**
     * @param currentPlot
     * @param crt
     */
    private void markClick1inUI(final PlotContainer currentPlot) {
        final String crt = "\n\t";
        if (currentPlot.getDimensionality() == 1) {
            textOut.messageOut(crt + "Background " + cursorBin.getX() + S_TO,
                    MessageHandler.CONTINUE);
        } else {
            textOut.messageOut(getCoordString(cursorBin) + S_TO,
                    MessageHandler.CONTINUE);
        }
    }

    /**
     * @param currentPlot
     * @param bin1
     */
    private void markClick2inUI(final PlotContainer currentPlot, final Bin bin1) {
        if (currentPlot.getDimensionality() == 1) {
            currentPlot.markArea(bin1, cursorBin);
            textOut.messageOut(Integer.toString(cursorBin.getX()));
        } else {
            textOut.messageOut(getCoordString(cursorBin),
                    MessageHandler.CONTINUE);
        }
    }

    /**
     * @param currentPlot
     */
    private void markClick3inUI(final PlotContainer currentPlot) {
        if (currentPlot.getDimensionality() == 1) {
            textOut.messageOut(" and " + cursorBin.getX() + S_TO);

        } else {
            textOut.messageOut(getCoordString(cursorBin) + S_TO,
                    MessageHandler.CONTINUE);
        }
    }

    /**
     * @param currentPlot
     * @param bin1
     */
    private void markClick4inUI(final PlotContainer currentPlot, final Bin bin1) {
        if (currentPlot.getDimensionality() == 1) {
            currentPlot.markArea(bin1, cursorBin);
            textOut.messageOut(String.valueOf(cursorBin.getX()),
                    MessageHandler.CONTINUE);
        } else {
            textOut.messageOut(getCoordString(cursorBin),
                    MessageHandler.CONTINUE);
        }
    }

    /**
     * @param currentPlot
     * @param crt
     */
    private void markClick5inUI(final PlotContainer currentPlot) {
        final String crt = "\n\t";
        if (currentPlot.getDimensionality() == 1) {
            textOut.messageOut("." + crt + "Peak " + cursorBin.getX() + S_TO,
                    MessageHandler.CONTINUE);
        } else {

            textOut.messageOut(getCoordString(cursorBin) + S_TO,
                    MessageHandler.CONTINUE);
        }
    }

    /**
     * @param currentPlot
     * @param bin4
     */
    private void markClick6inUI(final PlotContainer currentPlot, final Bin bin4) {
        if (currentPlot.getDimensionality() == 1) {
            currentPlot.markArea(bin4, cursorBin);
            textOut.messageOut(cursorBin.getX() + ". ",
                    MessageHandler.CONTINUE);
        } else {
            textOut.messageOut(getCoordString(cursorBin) + ". ",
                    MessageHandler.CONTINUE);
        }
    }

    /**
     * Background subtracted intensity of 1-d plots
     */
    @SuppressWarnings(UNUSED)
    private void netarea() {// NOPMD
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        final AbstractHistogram hist = (AbstractHistogram) SelectionTree
                .getCurrentHistogram();
        final int nclicks = clicks.size();
        if (!commandPresent) {// NOPMD
            cursorCommand = true;
            init();
            final String name = hist.getFullName().trim();
            textOut
                    .messageOut(
                            "Net Area fit for "
                                    + name
                                    + ": select four background markers, then two region-of-interest markers. ",
                            MessageHandler.NEW);
        } else if (nclicks == 0) {
            // ************ First background Marker
            // ***********************************
            synchronized (cursorBin) {
                cloneClickAndAdd(cursorBin);
                currentPlot.markChannel(cursorBin);
                markClick1inUI(currentPlot);
            }
        } else if (nclicks == 1) {
            // ************ Second Background marker
            // **********************************
            synchronized (cursorBin) {
                cloneClickAndAdd(cursorBin);
                final Bin bin1 = getClick(0);
                currentPlot.markChannel(cursorBin);
                markClick2inUI(currentPlot, bin1);
            }
        } else if (nclicks == 2) {
            // ************ Third Background Marker
            // **********************************
            synchronized (cursorBin) {
                cloneClickAndAdd(cursorBin);
                currentPlot.markChannel(cursorBin);
                markClick3inUI(currentPlot);
            }
        } else if (nclicks == 3) {
            // ************ Fourth Background Marker
            // *********************************
            synchronized (cursorBin) {
                cloneClickAndAdd(cursorBin);
                final Bin bin1 = getClick(2);
                currentPlot.markChannel(cursorBin);
                markClick4inUI(currentPlot, bin1);
            }
        } else if (nclicks == 4) {
            // ************ First Region Marker
            // *********************************
            synchronized (cursorBin) {
                currentPlot.initializeSelectingArea(cursorBin);
                cloneClickAndAdd(cursorBin);
                currentPlot.markChannel(cursorBin);
                markClick5inUI(currentPlot);
            }
        } else if (nclicks == 5) {
            handleClick6(currentPlot, hist);
        }
    }

    /**
     * Overlay a histogram
     * @param hist
     */
    private void overlay(final List<Double> hist) {
        if (!commandPresent) {
            cursorCommand = false;
            init();
            textOut.messageOut("Overlay histogram numbers: ",
                    MessageHandler.NEW);
        }
        final boolean areHists = !hist.isEmpty();
        for (double dNum : hist) {
            final int num = (int) dNum;
            final AbstractHistogram histogram = AbstractHistogram
                    .getHistogram(num);
            if (histogram == null) {
                LOGGER.warning("There is no histogram numbered " + num + ".");
            } else {
                if (histogram.getDimensionality() == 1) {
                    if (plotAccessor.getPlotContainer().getDimensionality() == 1) {
                        plotAccessor.getPlotContainer().overlayHistogram(num);
                        textOut.messageOut(Integer.toString(num) + ' ',
                                MessageHandler.CONTINUE);
                    } else {
                        LOGGER
                                .warning(" Current histogram not 1D, so it cannot be overlaid.");

                    }
                } else {
                    LOGGER.warning(histogram.getFullName().trim()
                            + " is not 1D, so it cannot be overlaid.");
                }
            }
        }
        if (areHists) {
            done();
        }
    }

    /**
     * Set the current plot, i.e., the one we will do actions on. Reset the
     * current state.
     */
    protected void plotChanged() {
        synchronized (this) {
            /* Clear command if in middle of a command */
            /* Command present at more and 1 or more clicks */
            if (commandPresent && !clicks.isEmpty()) {
                done();
            }
            settingGate = false;
        }
    }

    protected PlotMouseListener getMouseListener() {
        return this.mouseListener;
    }

    private transient final PlotMouseListener mouseListener = new PlotMouseListener() {
        /**
         * @see PlotMouseListener#plotMousePressed(Bin, Point)
         */
        public void plotMousePressed(final Bin pChannel, final Point pPixel) {
            synchronized (this) {
                /* cursor position and counts for that channel */
                Action.this.cursorBin.setChannel(pChannel);
                /* see if there is a command currently being processed */
                if (Action.this.commandPresent) {
                    /* Do the command */
                    Action.this.commandable.doCommand(
                            Action.this.currentCommand, false);
                } else if (settingGate) {
                    /* No command being processed check if gate is being set */
                    final PlotContainer currentPlot = Action.this.plotAccessor
                            .getPlotContainer();
                    Action.this.broadcaster.broadcast(
                            BroadcastEvent.Command.GATE_SET_POINT, pChannel);
                    currentPlot.displaySetGate(GateSetMode.GATE_CONTINUE,
                            pChannel, pPixel);
                } else {
                    Action.this.commandable.doCommand(PlotCommands.CURSOR,
                            false);
                }
            }
        }
    };

    /*
     * non-javadoc: Set the range for the counts scale.
     */
    private void range(final boolean typedAtConsole) {
        if (commandPresent) {
            final PlotContainer plot = plotAccessor.getPlotContainer();
            final boolean twoD = plot.getDimensionality() == 2;
            final boolean useCounts = twoD && !typedAtConsole;
            final double cts = useCounts ? getCounts(cursorBin) : cursorBin
                    .getY();
            if (clicks.isEmpty()) {
                countLow = (int) cts;
                clicks.add(cursorBin);
                textOut.messageOut(Integer.toString(countLow) + S_TO);
            } else {
                countHigh = (int) cts;
                clicks.add(cursorBin);
                plot.setRange(countLow, countHigh);
                textOut.messageOut(String.valueOf(countHigh),
                        MessageHandler.END);
                done();
            }
        } else {
            cursorCommand = true;
            init();
            textOut.messageOut("Range from ", MessageHandler.NEW);
        }
    }

    /*
     * non-javadoc: Set the range for the counts scale.
     */
    private void rebin(final List<Double> parameters) {
        if (!commandPresent) {
            cursorCommand = false;
            init();
            textOut.messageOut("Rebin ", MessageHandler.NEW);
        }
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        final AbstractHistogram hist = (AbstractHistogram) SelectionTree
                .getCurrentHistogram();
        if (!parameters.isEmpty()) {
            final double binWidth = parameters.get(0);
            if (binWidth >= 1.0 && binWidth < hist.getSizeX()) {
                currentPlot.setBinWidth(binWidth);
                textOut.messageOut(String.valueOf(binWidth),
                        MessageHandler.END);
                currentPlot.repaint();
                done();
            } else {
                textOut.messageOut(String.valueOf(binWidth),
                        MessageHandler.END);
                LOGGER
                        .warning("Rebin command ignored. Bin value must be \u2265 1.0 and smaller than the histogram.");
                done();
            }
        }
    }

    /**
     * Change the scale for linear to log or log to linear
     */
    @SuppressWarnings(UNUSED)
    private void scale() {// NOPMD
        cursorCommand = false;
        if (plotAccessor.getPlotContainer().getLimits().getScale() == Scale.LINEAR) {
            log();
        } else {
            linear();
        }
    }

    protected void setDefiningGate(final boolean whether) {
        synchronized (this) {
            settingGate = whether;
        }
    }

    /**
     * Call <code>update()</code> on the current plot, reset the command-line,
     * and broadcast a histogram selection message to force the rest of the GUI
     * to be consistent with the currently selected histogram.
     * @see PlotContainer#update()
     */
    @SuppressWarnings(UNUSED)
    private void update() {// NOPMD
        cursorCommand = false;
        broadcaster.broadcast(BroadcastEvent.Command.OVERLAY_OFF);
        plotAccessor.update();
        // Reset rebin to 1
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        currentPlot.setBinWidth(1.0);
        done();
        /*
         * following to recover the chooser if user just overlaid a histogram
         */
        broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT);
    }

    /**
     * Expand the region to view.
     */
    @SuppressWarnings(UNUSED)
    private void zoomhorz() {// NOPMD
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        final boolean noCommand = !commandPresent;
        if (noCommand) {
            cursorCommand = true;
            init();
            textOut.messageOut("Expand from channel ", MessageHandler.NEW);
        } else {
            updateSelectedAreaForClick(currentPlot);
        }
    }

    /**
     * Zoom in on the histogram
     */
    @SuppressWarnings(UNUSED)
    private void zoomin() {// NOPMD
        cursorCommand = false;
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        currentPlot.zoom(PlotContainer.Zoom.IN);
        this.autoCounts.conditionalAutoCounts(currentPlot);
        done();
    }

    /**
     * Zoom out on the histogram.
     */
    @SuppressWarnings(UNUSED)
    private void zoomout() {// NOPMD
        cursorCommand = false;
        final PlotContainer currentPlot = plotAccessor.getPlotContainer();
        currentPlot.zoom(PlotContainer.Zoom.OUT);
        this.autoCounts.conditionalAutoCounts(currentPlot);
        done();
    }

    @SuppressWarnings(UNUSED)
    private void zoomvert() {// NOPMD
        // do-nothing
    }

    private double getCounts(final Bin bin) {
        synchronized (this) {
            return plotAccessor.getPlotContainer().getCount(bin);
        }
    }

    private String getCoordString(final Bin bin) {
        synchronized (this) {
            final StringBuilder rval = new StringBuilder().append(bin.getX());
            if (plotAccessor.getPlotContainer().getDimensionality() == 2) {
                rval.append(',').append(bin.getY());
            }
            return rval.toString();
        }
    }

    protected void setPlotAccessor(final CurrentPlotAccessor plotAccessor) {
        this.plotAccessor = plotAccessor;
        final ParseCommand parseCommand = new ParseCommand(this.commandable,
                this.plotAccessor);
        this.console.addCommandListener(parseCommand);
    }

}