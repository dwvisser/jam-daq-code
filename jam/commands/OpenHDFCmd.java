package jam.commands;

import injection.GuiceInjector;
import jam.data.AbstractHistogram;
import jam.data.DataBase;
import jam.data.Group;
import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.ui.SelectionTree;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import com.google.inject.Inject;

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

	private transient final Frame frame;

	private transient final Broadcaster broadcaster;

	@Inject
	OpenHDFCmd(final Frame frame, final HDFIO hdfio,
			final Broadcaster broadcaster) {
		super("Open\u2026");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
				CTRL_MASK));
		this.hdfio = hdfio;
		this.frame = frame;
		this.broadcaster = broadcaster;
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
			final int option = jfile.showOpenDialog(frame);
			// Don't do anything if it was cancel
			if (option == JFileChooser.APPROVE_OPTION
					&& jfile.getSelectedFile() != null) {
				openFile = jfile.getSelectedFile();
				DataBase.getInstance().clearAllLists();
				this.broadcaster
						.broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);
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
		AbstractHistogram firstHist = null;
		/* Set general status. */
		GuiceInjector.getJamStatus().setOpenFile(file);
		AbstractControl.setupAll();
		this.broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		/*
		 * Set selection of group and histogram. Set to first group and first
		 * histogram
		 */
		final List<Group> groups = jam.data.Warehouse.getGroupCollection()
				.getList();
		if (!groups.isEmpty()) {
			firstGroup = groups.get(0);
			GuiceInjector.getJamStatus().setCurrentGroup(firstGroup);
			/* Set the current histogram to the first opened histogram. */
			if (firstGroup.histograms.getList().size() > 0) {
				firstHist = firstGroup.histograms.getList().get(0);
				SelectionTree.setCurrentHistogram(firstHist);
			}
		}
		this.broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
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
		final QuerySortMode mode = GuiceInjector.getJamStatus().getSortMode();
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
