package jam.commands;

import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.ui.SelectionTree;

import java.awt.Frame;
import java.io.File;
import java.util.List;
import java.util.Observer;

import javax.swing.JFileChooser;

/**
 * Add counts to histograms in memory from histograms in an HDF file.
 * 
 * @author Ken Swartz
 */
abstract class AbstractLoaderHDF extends AbstractCommand implements Observer,
		HDFIO.AsyncListener {

	protected final transient HDFIO hdfio;

	protected transient Group loadGroup;

	/**
	 * Mode under which to do the loading.
	 */
	protected transient FileOpenMode fileOpenMode;

	AbstractLoaderHDF() {
		super();
		final Frame frame = STATUS.getFrame();
		hdfio = new HDFIO(frame);
	}

	/**
	 * Read in an HDF file.
	 * 
	 * @param file
	 *            a file reference or null
	 * @param load
	 * @return whether file was read
	 */
	protected final boolean loadHDFFile(final File file, final Group load) {
		loadGroup = load;
		final boolean fileRead;
		if (file == null) {// No file given
			final JFileChooser jfile = new JFileChooser(HDFIO
					.getLastValidFile());
			jfile.setFileFilter(new HDFileFilter(true));
			final int option = jfile.showOpenDialog(STATUS.getFrame());
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
	 * 
	 */
	private void notifyApp() {
		Histogram firstHist = null;
		/*
		 * Set to sort group. Set the current histogram to the first opened
		 * histogram.
		 */
		if (loadGroup.getHistogramList().size() > 0) {
			final Group group = (Group) STATUS.getCurrentGroup();
			if (Group.isValid(group)) {
				final List<Histogram> histList = group.getHistogramList();
				if (!histList.isEmpty()) {
					firstHist = group.getHistogramList().get(0);
				}
			}
		}
		SelectionTree.setCurrentHistogram(firstHist);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
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
