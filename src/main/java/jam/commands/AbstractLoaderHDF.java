package jam.commands;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import injection.GuiceInjector;
import jam.data.AbstractHistogram;
import jam.data.DataBase;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.Nameable;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.ui.SelectionTree;

/**
 * Add counts to histograms in memory from histograms in an HDF file.
 * @author Ken Swartz
 */
abstract class AbstractLoaderHDF extends AbstractCommand implements PropertyChangeListener,
        HDFIO.AsyncListener {

    protected final transient HDFIO hdfio;

    protected transient Group loadGroup;

    /**
     * Mode under which to do the loading.
     */
    protected transient FileOpenMode fileOpenMode;

    protected transient final Broadcaster broadcaster;

    AbstractLoaderHDF(final HDFIO hdfio, final Broadcaster broadcaster) {
        super();
        this.hdfio = hdfio;
        this.broadcaster = broadcaster;
    }

    /**
     * Read in an HDF file.
     * @param file
     *            a file reference or null
     * @param load group to load HDF data into (?)
     * @return whether file was read
     */
    protected final boolean loadHDFFile(final File file, final Group load) {
        loadGroup = load;
        final boolean fileRead;
        if (file == null) {// No file given
            final JFileChooser jfile = new JFileChooser(
                    HDFIO.getLastValidFile());
            jfile.setFileFilter(new HDFileFilter(true));
            final int option = jfile.showOpenDialog(GuiceInjector
                    .getObjectInstance(JFrame.class));
            /* Don't do anything if it was cancel. */
            if (option == JFileChooser.APPROVE_OPTION
                    && jfile.getSelectedFile() != null) {
                final File selectedFile = jfile.getSelectedFile();
                hdfio.setListener(this);
                fileRead = hdfio.readFile(fileOpenMode, selectedFile, load);
            } else {
                fileRead = false;
            }
        } else {
            fileRead = hdfio.readFile(fileOpenMode, file, load);
        }
        return fileRead;
    }

    @Override
    protected final void executeParse(final String[] cmdTokens) {
        // LATER KBS needs to be implemented
        // execute(null); //has unhandled exception
    }

    /**
     * Notify the application when the command is done
     */
    private void notifyApp() {
        AbstractHistogram firstHist = null;
        /*
         * Set to sort group. Set the current histogram to the first opened
         * histogram.
         */
        if (loadGroup.histograms.getList().size() > 0) {
            final Nameable nameable = GuiceInjector.getObjectInstance(
                    JamStatus.class).getCurrentGroup();
            if (DataBase.getInstance().isValid(nameable)) {
                final Group group = (Group) nameable;
                final List<AbstractHistogram> histList = group.histograms
                        .getList();
                if (!histList.isEmpty()) {
                    firstHist = group.histograms.getList().get(0);
                }
            }
        }
        SelectionTree.setCurrentHistogram(firstHist);
        this.broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
                firstHist);

    }

    /**
     * Called by HDFIO when asynchronized IO is completed
     */
    public void completedIO(final String message, final String errorMessage) {
        hdfio.removeListener();
        notifyApp();
    }

}
