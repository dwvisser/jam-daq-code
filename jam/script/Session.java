package jam.script;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import injection.MapListener;
import jam.commands.CommandNames;
import jam.data.Warehouse;
import jam.data.control.HistogramZero;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandListener;
import jam.global.RunState;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.sort.control.RunControl;
import jam.sort.control.SetupSortOff;
import jam.sort.control.SetupSortOn;
import jam.sort.control.SortControl;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventOutputStream;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which exposes an API for scripting offline sorting sessions. Using this
 * class, you can write and compile a .java file which, when executed, will
 * <p>
 * <ol>
 * <li>Launch Jam in the background.</li>
 * <li>Setup offline sorting.</li>
 * <li>Load an HDF file (i.e., load gates and parameters).</li>
 * <li>Zero histograms (often needed after the previous step).</li>
 * <li>Define the list of event files to sort.</li>
 * <li>For sort routines that invoke <code>writeEvent()</code>, specify the
 * event output file.</li>
 * <li>(Optional) Show the Jam window to observe sort progress.</li>
 * <li><em>Perform the sort.</em></li>
 * <li>Save the results in an HDF file.</li>
 * <li>(Optional) Add histograms in stored HDF files together.
 * </ol>
 * The last step may be desirable in the case where you would like to execute
 * portions of the sorting task on different machines at the same time, and
 * combine their results in a "merge script".
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version April 5, 2004
 */
@Singleton
public final class Session implements Observer {

    private static final String INSTREAM = "\tin: ";

    private static final Logger LOGGER = Logger.getLogger(Session.class
            .getPackage().getName());

    private static final String OUTSTREAM = "\tout: ";

    private transient final File base;

    private transient boolean filesGiven = false;

    private transient final HDFIO hdfio;

    private transient boolean isSetup = false;

    private transient final Object lockObject = new Object();

    private transient final RunControl runControl;

    private transient final SetupSortOff setupSortOffline;

    private transient final SetupSortOn setupSortOnline;

    private transient final SortControl sortControl;

    private transient RunState state = RunState.NO_ACQ;

    private transient final JFrame frame;

    private transient final HistogramZero histogramZero;

    private transient final CommandListener listener;

    /**
     * Creates an instance, of which the user then invokes the methods to script
     * an offline sorting session. A non-trivial side-effect of invoking this
     * constructor is that an instance of Jam is started up in the background.
     * @param sortOffline
     *            sort offline setup dialog
     * @param sortOnline
     *            sort online setup dialog
     * @param sortControl
     *            offline sort control
     * @param runControl
     *            online run control
     * @param hdfio
     *            HDF I/O
     */
    @Inject
    protected Session(final JFrame frame, final SetupSortOff sortOffline,
            final SetupSortOn sortOnline, final SortControl sortControl,
            final RunControl runControl, final HDFIO hdfio,
            final HistogramZero histogramZero, final Broadcaster broadcaster,
            final @MapListener CommandListener listener) {
        super();
        broadcaster.addObserver(this);
        this.listener = listener;
        this.frame = frame;
        this.setupSortOffline = sortOffline;
        this.setupSortOnline = sortOnline;
        this.sortControl = sortControl;
        this.runControl = runControl;
        this.hdfio = hdfio;
        this.histogramZero = histogramZero;
        base = new File(System.getProperty("user.dir"));
    }

    /**
     * Add an event file to the list of event files to sort. If the file
     * reference passed represents a directory, then all files with names ending
     * in <code>.evn</code> in the directory are added to the list. Files whose
     * names don't meet this requirement are ignored and not added to the list.
     * This method does <em>not</em> recurse into the directory tree.
     * @param fileOrDir
     *            <code>.evn</code> file or folder containing such files
     * @throws IllegalStateException
     *             if <code>setupOffline()</code> hasn't been called yet
     */
    public void addEventFile(final File fileOrDir) {
        if (!isSetup) {
            throw new IllegalStateException(
                    "You may not call addEventFile() before calling setupOffline().");
        }
        final int numFiles = sortControl.addEventFile(fileOrDir);
        if (fileOrDir.isFile()) {
            LOGGER.log(Level.INFO, "Added event file to sort: "
                    + fileOrDir.getName());
        }
        if (fileOrDir.isDirectory()) {
            LOGGER.log(Level.INFO, "Added event files in folder: "
                    + fileOrDir.getName());
        }
        if (numFiles > 0) {
            filesGiven = true;
        } else {
            LOGGER.log(Level.WARNING, fileOrDir.getName()
                    + " didn't contain any usable files.");
        }
    }

    /**
     * Add the histogram counts in the given HDF file, or all HDF files in the
     * given directory, into the histograms in memory. You must have already
     * invoked <code>setupSort()</code>.
     * @param hdf
     *            an HDF file or folder containing HDF files
     * @throws IllegalStateException
     *             if <code>setupOffline()</code> hasn't been called yet
     */
    public void addHDF(final File hdf) {
        final FileFilter filter = new HDFileFilter(false);
        if (!isSetup) {
            throw new IllegalStateException(
                    "You may not call loadHDF() before calling setupOffline().");
        }
        if (hdf.isDirectory()) {
            for (File file : hdf.listFiles(filter)) {
                hdfio.readFile(FileOpenMode.ADD, file);
                LOGGER.log(Level.INFO, "Added HDF file: " + file);
            }
        } else if (filter.accept(hdf)) {
            hdfio.readFile(FileOpenMode.ADD, hdf);
            LOGGER.log(Level.INFO, "Added HDF file: " + hdf);
        } else {
            LOGGER.log(Level.INFO, hdf + " isn't an HDF file, not added.");
        }
    }

    /**
     * Launch the offline sort.
     * @throws IllegalStateException
     *             if <code>setupOffline()</code> hasn't been called yet, or if
     *             no event files have been specified
     */
    public void beginSort() {
        if (!isSetup) {
            throw new IllegalStateException(
                    "You may not call beginSort() before calling setupOffline().");
        }
        if (!filesGiven) {
            throw new IllegalStateException(
                    "You may not call beginSort() without first specifying event files to sort.");
        }
        try {
            sortControl.beginSort();
            LOGGER.log(Level.INFO, "Began sort. Waiting for finish...");
            synchronized (lockObject) {
                while (state != RunState.ACQ_OFF) {
                    /*
                     * Relinquish the lock for 2.5 seconds or until a notify()
                     * is received
                     */
                    lockObject.wait(2500);
                }
            }
            LOGGER.log(Level.INFO, "Reached end of sort.");
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE,
                    "Interrupted while waiting for sort to finish.", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while beginning sort: ", e);
        }
    }

    /**
     * Cancel online setup.
     */
    public void cancelOnline() {
        this.setupSortOnline.cancelOnlineSetup();
    }

    /**
     * Utility method for defining a file reference relative to the directory
     * that java was launched from. Here's an example for Linux and MacOS X
     * systems: If the JVM was launched from <code>/home/user/sortscript</code>,
     * and this method is called with the argument
     * <code>"../thesisRuns/run34.evn"</code>, then a File object representing
     * <code>/home/user/thesisRuns/run34.evn</code> is returned,
     * <em>whether or not that file really exists yet</em>.
     * @param fname
     *            relative path reference to the file of interest
     * @return File object resolving <code>fname</code> against the base path
     *         given by the system property <code>user.dir</code> at startup
     */
    public File defineFile(final String fname) {
        return new File(base, fname);
    }

    /**
     * @return the number of events that were sorted offline
     */
    public int getEventsSorted() {
        return this.sortControl.getEventsSorted();
    }

    /**
     * Hide Jam's graphical interface.
     */
    public void hideJam() {
        this.frame.setVisible(false);
    }

    /**
     * Reads in the given text file, interpreting each line as a filename, which
     * gets added to the list of event files to sort. Such files are typically
     * created using the <code>Save List</code> button in the
     * <code>Control|Sort...</code> dialog in Jam.
     * @param list
     *            text file listing event files
     * @throws IllegalStateException
     *             if <code>setupOffline()</code> hasn't been called yet
     */
    public void loadFileList(final File list) {
        if (!isSetup) {
            throw new IllegalStateException(
                    "You may not call loadFileList() before calling setupOffline().");
        }
        final int numFiles = sortControl.readList(list);
        if (numFiles > 0) {
            filesGiven = true;
        } else {
            LOGGER.log(Level.WARNING, list.getName()
                    + " didn't contain any usable filenames.");
        }
    }

    /**
     * Load the given HDF file into memory. You must have already invoked
     * <code>setupOffline()</code>.
     * @param hdf
     *            an HDF file
     * @throws IllegalStateException
     *             if <code>setupOffline()</code> hasn't been called yet
     */
    public void loadHDF(final File hdf) {
        if (!isSetup) {
            throw new IllegalStateException(
                    "You may not call loadHDF() before calling setupOffline().");
        }
        hdfio.readFile(FileOpenMode.RELOAD, hdf, Warehouse
                .getSortGroupGetter().getSortGroup());
        LOGGER.log(Level.INFO, "Loaded HDF file: " + hdf);
    }

    /**
     * Used to unload the offline sort setup, including emptying the list of
     * files to sort.
     */
    public void resetOfflineSorting() {
        setupSortOffline.resetSort();
        this.sortControl.removeAllFiles();
    }

    /**
     * Load the given HDF file into memory.
     * @param hdf
     *            an HDF file
     */
    public void saveHDF(final File hdf) {
        hdfio.writeFile(hdf);
        LOGGER.log(Level.INFO, "Saved HDF file: " + hdf);
    }

    /**
     * Set the file to output events to when the user's sort routine invokes
     * <code>writeEvent()</code>
     * @param eventsOut
     *            where to write "pre-sort" events
     * @see jam.sort.AbstractSortRoutine#writeEvent(int [])
     * @throws IllegalStateException
     *             if <code>setupOffline()</code> hasn't been called yet
     */
    public void setEventOutput(final File eventsOut) {
        if (!isSetup) {
            throw new IllegalStateException(
                    "You may not call setEventOutput() before calling setupOffline().");
        }
        sortControl.setEventOutput(eventsOut);
        LOGGER.log(Level.INFO, "Set file for pre-sorted events"
                + eventsOut.getAbsolutePath());
    }

    /**
     * Completes the task equivalent of specifying the settings in Jam's dialog
     * for setting up offline sorting. That is, calling this defines the
     * classpath to sort routines, the fully qualified name of the
     * <code>AbstractSortRoutine</code> to use for sorting, and references to
     * the <code>EventInputStream</code> and <code>EventOutputStream</code> to
     * use.
     * @param classPath
     *            the path that sort routines get loaded from
     * @param sortName
     *            fully qualified with all package names in the standard java
     *            "dot" notation, e.g., <code>"sort.Calorimeter"</code> for the
     *            file <code>sort/Calorimeter.class</code> relative to
     *            <code>classPath</code>
     * @param inStream
     *            e.g., <code>jam.sort.stream.YaleInputStream.class</code>
     * @param outStream
     *            e.g., <code>jam.sort.stream.YaleOutputStream.class</code>
     * @see jam.sort.AbstractSortRoutine
     * @see jam.sort.stream.AbstractEventInputStream
     * @see jam.sort.stream.AbstractEventOutputStream
     */
    public void setupOffline(final File classPath, final String sortName,
            final Class<? extends AbstractEventInputStream> inStream,
            final Class<? extends AbstractEventOutputStream> outStream) {
        setupSortOffline.setupSort(classPath, sortName, inStream, outStream);
        LOGGER.log(Level.INFO, "Setup offline sorting:");
        LOGGER.log(Level.INFO, "\t" + classPath + ": " + sortName);
        LOGGER.log(Level.INFO, INSTREAM + inStream);
        LOGGER.log(Level.INFO, OUTSTREAM + outStream);
        isSetup = true;
    }

    /**
     * Completes the task equivalent of specifying the settings in Jam's dialog
     * for setting up offline sorting. That is, calling this defines the
     * classpath to sort routines, the fully qualified name of the
     * <code>AbstractSortRoutine</code> to use for sorting, and references to
     * the <code>EventInputStream</code> and <code>EventOutputStream</code> to
     * use.
     * @param classPath
     *            the path that sort routines get loaded from
     * @param sortName
     *            fully qualified with all package names in the standard java
     *            "dot" notation, e.g., <code>"sort.Calorimeter"</code> for the
     *            file <code>sort/Calorimeter.class</code> relative to
     *            <code>classPath</code>
     * @param inStream
     *            e.g., <code>jam.sort.stream.YaleInputStream.class</code>
     * @param outStream
     *            e.g., <code>jam.sort.stream.YaleOutputStream.class</code>
     * @see jam.sort.AbstractSortRoutine
     * @see jam.sort.stream.AbstractEventInputStream
     * @see jam.sort.stream.AbstractEventOutputStream
     */
    public void setupOffline(final String sortName,
            final Class<? extends AbstractEventInputStream> inStream,
            final Class<? extends AbstractEventOutputStream> outStream) {
        setupSortOffline.setupSort(sortName, inStream, outStream);
        LOGGER.log(Level.INFO, "Setup online sorting:");
        LOGGER.log(Level.INFO, "\t" + sortName);
        LOGGER.log(Level.INFO, INSTREAM + inStream);
        LOGGER.log(Level.INFO, OUTSTREAM + outStream);
        isSetup = true;
    }

    /**
     * Show Jam's graphical interface. Once shown, the user may interact with
     * Jam like normal, including interrupting the scripted process.
     */
    public void showJam() {
        this.frame.setVisible(true);
    }

    /**
     * Online acquisition scripting.
     */
    public Online online = new Online();

    /**
     * Online acquisition scripting.
     * @author Dale Visser
     */
    public class Online {

        Online() {
            // nothing
        }

        /**
         * Start online acquisition.
         */
        public void start() {
            Session.this.runControl.startAcq();
        }

        /**
         * @param classPath
         *            the path that sort routines get loaded from
         * @param sortName
         *            fully qualified with all package names in the standard
         *            java "dot" notation, e.g., <code>"sort.Calorimeter"</code>
         *            for the file <code>sort/Calorimeter.class</code> relative
         *            to <code>classPath</code>
         * @param inStream
         *            e.g., <code>jam.sort.stream.YaleInputStream.class</code>
         * @param outStream
         *            e.g., <code>jam.sort.stream.YaleOutputStream.class</code>
         */
        public void setup(final File classPath, final String sortName,
                final Class<? extends AbstractEventInputStream> inStream,
                final Class<? extends AbstractEventOutputStream> outStream) {
            Session.this.setupSortOnline.setupSort(classPath, sortName,
                    inStream, outStream);
            Session.LOGGER.log(Level.INFO, "Setup online sorting:");
            Session.LOGGER.log(Level.INFO, "\t" + classPath + ": " + sortName);
            this.internalSetup(inStream, outStream);
        }

        private void internalSetup(
                final Class<? extends AbstractEventInputStream> inStream,
                final Class<? extends AbstractEventOutputStream> outStream) {
            Session.LOGGER.log(Level.INFO, INSTREAM + inStream);
            Session.LOGGER.log(Level.INFO, OUTSTREAM + outStream);
            Session.this.isSetup = true;
        }

        /**
         * @param sortName
         *            fully qualified with all package names in the standard
         *            java "dot" notation, e.g., <code>"sort.Calorimeter"</code>
         *            for the file <code>sort/Calorimeter.class</code> relative
         *            to <code>classPath</code>
         * @param inStream
         *            e.g., <code>jam.sort.stream.YaleInputStream.class</code>
         * @param outStream
         *            e.g., <code>jam.sort.stream.YaleOutputStream.class</code>
         */
        public void setup(final String sortName,
                final Class<? extends AbstractEventInputStream> inStream,
                final Class<? extends AbstractEventOutputStream> outStream) {
            Session.this.setupSortOnline.setupSort(sortName, inStream,
                    outStream);
            Session.LOGGER.log(Level.INFO, "Setup online sorting:");
            Session.LOGGER.log(Level.INFO, "\t" + sortName);
            this.internalSetup(inStream, outStream);
        }

        /**
         * Stop online acquisition.
         * @param script
         *            TODO
         */
        public void stop() {
            Session.this.runControl.stopAcq();
        }

    }

    public void update(final Observable event, final Object param) {
        final BroadcastEvent bEvent = (BroadcastEvent) param;
        final BroadcastEvent.Command command = bEvent.getCommand();
        if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
            synchronized (lockObject) {
                state = (RunState) bEvent.getContent();
                lockObject.notifyAll();
            }
        }
    }

    /**
     * Zero all histograms in memory.
     */
    public void zeroHistograms() {
        this.histogramZero.zeroAll();
    }

    /**
     * Send the command to the front end to read the scalers.
     */
    public void readScalers() {
        final String[] read = {"read" };
        this.listener.performParseCommand(CommandNames.SCALERS, read);
    }
}
