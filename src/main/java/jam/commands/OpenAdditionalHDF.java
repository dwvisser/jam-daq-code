package jam.commands;

import java.awt.Frame;
import java.io.File;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;

import com.google.inject.Inject;

import jam.data.AbstractHistogram;
import jam.data.Group;
import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.ui.SelectionTree;

/**
 * Open an additional hdf file
 * 
 * @author Ken Swartz
 * 
 */
@SuppressWarnings("serial")
public class OpenAdditionalHDF extends AbstractCommand implements
		HDFIO.AsyncListener {

	private transient final HDFIO hdfio;
	private transient final Broadcaster broadcaster;
	private transient final JamStatus status;
	private transient final Frame frame;

	@Inject
	OpenAdditionalHDF(final HDFIO hdfio, final Broadcaster broadcaster,
			final JamStatus status, final Frame frame) {
		super();
		putValue(NAME, "Open Additional\u2026");
		this.hdfio = hdfio;
		this.broadcaster = broadcaster;
		this.status = status;
		this.frame = frame;
		final Icon iOpenAdd = loadToolbarIcon("jam/ui/OpenAddHDF.png");
		putValue(Action.SMALL_ICON, iOpenAdd);
		putValue(Action.SHORT_DESCRIPTION, "Open an additional hdf data file");
	}

	/*
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		File file = null;
		if (cmdParams != null && cmdParams.length > 0) {
			file = (File) cmdParams[0];
		}
		readAdditionalHDFFile(file);
	}

	/*
	 * Read in an HDF file
	 */
	private void readAdditionalHDFFile(final File file) {
		hdfio.setListener(this);
		final boolean isReading;
		if (file == null) {// No file given
			final JFileChooser jfile = new JFileChooser(HDFIO
					.getLastValidFile());
			jfile.setFileFilter(new HDFileFilter(true));
			final int option = jfile.showOpenDialog(frame);
			// dont do anything if it was cancel
			if (option == JFileChooser.APPROVE_OPTION
					&& jfile.getSelectedFile() != null) {
				final File selectedFile = jfile.getSelectedFile();
				isReading = hdfio
						.readFile(FileOpenMode.OPEN_MORE, selectedFile);
			} else {
				isReading = false;
			}
		} else {
			isReading = hdfio.readFile(FileOpenMode.OPEN_MORE, file);
		}
		if (!isReading) {// File was read in so no callback
			notifyApp();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	@Override
	protected void executeParse(final String[] cmdTokens) {
		Object[] cmdParams = new Object[1];
		if (cmdTokens.length == 0) {
			execute(null);
		} else {
			final File file = new File(cmdTokens[0]);
			cmdParams[0] = file;
			execute(cmdParams);
		}
	}

	private void notifyApp() {
		Group firstGroup;
		AbstractHistogram firstHist = null;

		// Update application status
		AbstractControl.setupAll();
		this.broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);

		// Set the current histogram to the first opened histogram
		firstGroup = hdfio.getFirstLoadGroup();
		if (firstGroup != null) {
			this.status.setCurrentGroup(firstGroup);

			/* Set the current histogram to the first opened histogram. */
			if (firstGroup.histograms.getList().size() > 0) {
				firstHist = firstGroup.histograms.getList().get(0);
			}
			SelectionTree.setCurrentHistogram(firstHist);
			this.broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
					firstHist);
		}
	}

	/**
	 * Called by HDFIO when asynchronized IO is completed
	 */
	public void completedIO(final String message, final String errorMessage) {
		hdfio.removeListener();
		notifyApp();
	}
}
