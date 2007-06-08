package jam.commands;

import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.ui.SelectionTree;

import java.awt.Event;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Observable;

import javax.swing.KeyStroke;

/**
 * Reload data from a hdf file
 * 
 * @author Ken Swartz
 * 
 */
final class ReloadHDFCmd extends AbstractLoaderHDF {

	ReloadHDFCmd() {
		super();
	}

	public void initCommand() {
		putValue(NAME, "Reload\u2026");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
				CTRL_MASK | Event.SHIFT_MASK));
		fileOpenMode = FileOpenMode.RELOAD;
	}

	protected void execute(final Object[] cmdParams) {
		final File file = null;
		/*
		 * FIXME KBS parse correctly if (cmdParams!=null) { file
		 * =(File)cmdParams[0]; //loadGroup=(Group)cmdParams[1]; }
		 */
		final Group load = Group.getSortGroup();
		loadHDFFile(file, load);
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			enable();
		}
	}

	private void enable() {
		final SortMode mode = STATUS.getSortMode();
		final boolean online = mode == SortMode.ONLINE_DISK
				|| mode == SortMode.ON_NO_DISK;
		final boolean offline = mode == SortMode.OFFLINE;
		final boolean sorting = online || offline;
		setEnabled(sorting);
	}

	private void notifyApp() {
		Histogram firstHist = null;
		/* Set to sort group. */
		final Group currentGroup = Group.getSortGroup();
		if (currentGroup != null) {
			STATUS.setCurrentGroup(currentGroup);
			/* Set the current histogram to the first opened histogram. */
			if (currentGroup.getHistogramList().size() > 0) {
				firstHist = currentGroup.getHistogramList().get(0);
			}
			SelectionTree.setCurrentHistogram(firstHist);
			BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
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
