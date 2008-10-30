package jam.commands;

import jam.data.AbstractHistogram;
import jam.data.Group;
import jam.data.SortGroupGetter;
import jam.data.Warehouse;
import jam.global.BroadcastEvent;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.ui.SelectionTree;

import java.awt.Event;
import java.awt.event.KeyEvent;
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

	@Override
	public void initCommand() {
		putValue(NAME, "Reload\u2026");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
				CTRL_MASK | Event.SHIFT_MASK));
		fileOpenMode = FileOpenMode.RELOAD;
	}

	private static final SortGroupGetter SORT_GROUP_GETTER = Warehouse
			.getSortGroupGetter();

	@Override
	protected void execute(final Object[] cmdParams) {
		/*
		 * FIXME KBS parse correctly if (cmdParams!=null) { file
		 * =(File)cmdParams[0]; //loadGroup=(Group)cmdParams[1]; }
		 */
		final Group load = SORT_GROUP_GETTER.getSortGroup();
		loadHDFFile(null, load);
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
		final boolean online = mode == SortMode.ONLINE_DISK
				|| mode == SortMode.ON_NO_DISK;
		final boolean offline = mode == SortMode.OFFLINE;
		final boolean sorting = online || offline;
		setEnabled(sorting);
	}

	private void notifyApp() {
		AbstractHistogram firstHist = null;
		/* Set to sort group. */
		final Group currentGroup = SORT_GROUP_GETTER.getSortGroup();
		if (currentGroup != null) {
			STATUS.setCurrentGroup(currentGroup);
			/* Set the current histogram to the first opened histogram. */
			if (currentGroup.histograms.getList().size() > 0) {
				firstHist = currentGroup.histograms.getList().get(0);
			}
			SelectionTree.setCurrentHistogram(firstHist);
			BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
					firstHist);
		}
	}

	/**
	 * Called by HDFIO when asynchronized IO is completed
	 */
	@Override
	public void completedIO(final String message, final String errorMessage) {
		hdfio.removeListener();
		notifyApp();
	}

}
