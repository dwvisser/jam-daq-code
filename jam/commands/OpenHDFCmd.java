package jam.commands;

import jam.data.DataBase;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.ui.SelectionTree;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

/**
 * Open a hdf file
 * 
 * @author Ken Swartz
 * 
 */
final class OpenHDFCmd extends AbstractCommand implements Observer,
		HDFIO.AsyncListener {

	private transient File openFile = null;

	private transient final HDFIO hdfio;

	OpenHDFCmd() {
		super("Open\u2026");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
				CTRL_MASK));
		final Frame frame = STATUS.getFrame();
		hdfio = new HDFIO(frame);
		final Icon iOpen = loadToolbarIcon("jam/ui/OpenHDF.png");
		putValue(Action.SMALL_ICON, iOpen);
		putValue(Action.SHORT_DESCRIPTION, "Open an hdf data file");
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
		readHDFFile(file);
	}

	/*
	 * Read in a HDF file
	 */
	private void readHDFFile(final File file) {
		final boolean isReading;
		if (file == null) {// No file given
			final JFileChooser jfile = new JFileChooser(HDFIO
					.getLastValidFile());
			jfile.setFileFilter(new HDFileFilter(true));
			final int option = jfile.showOpenDialog(STATUS.getFrame());
			// dont do anything if it was cancel
			if (option == JFileChooser.APPROVE_OPTION
					&& jfile.getSelectedFile() != null) {
				openFile = jfile.getSelectedFile();
				DataBase.getInstance().clearAllLists();
				BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);
				hdfio.setListener(this);
				isReading = hdfio.readFile(FileOpenMode.OPEN, openFile);
			} else {
				isReading = false;
			}
		} else {
			isReading = hdfio.readFile(FileOpenMode.OPEN, file);
		}
		// File was not read in so no call back do notify here
		if (!isReading) {
			hdfio.removeListener();
			notifyApp(null);
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

	private void notifyApp(final File file) {
		Group firstGroup;
		Histogram firstHist = null;
		/* Set general status. */
		STATUS.setOpenFile(file);
		AbstractControl.setupAll();
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		/*
		 * Set selection of group and histogram. Set to first group and first
		 * histogram
		 */
		if (Group.getGroupList().size() > 0) {
			firstGroup = Group.getGroupList().get(0);
			STATUS.setCurrentGroup(firstGroup);
			/* Set the current histogram to the first opened histogram. */
			if (firstGroup.getHistogramList().size() > 0) {
				firstHist = firstGroup.getHistogramList().get(0);
				SelectionTree.setCurrentHistogram(firstHist);
			}
		}
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
				firstHist);
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			enable();
		}
	}

	private void enable() {
		final QuerySortMode mode = STATUS.getSortMode();
		setEnabled(mode == SortMode.FILE || mode == SortMode.NO_SORT);
	}

	/**
	 * Called by HDFIO when asynchronized IO is completed
	 */
	public void completedIO(final String message, final String errorMessage) {
		hdfio.removeListener();
		notifyApp(openFile);
		openFile = null;// NOPMD
	}
}
