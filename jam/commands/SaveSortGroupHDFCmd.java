package jam.commands;

import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;

/**
 * Command to save the sort group of histograms.
 * 
 * @author Ken Swartz
 * 
 */
final class SaveSortGroupHDFCmd extends AbstractCommand implements Observer {

	SaveSortGroupHDFCmd() {
		super("Save sort group as\u2026");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(final Object[] cmdParams) {
		File file = null;
		if (cmdParams != null && cmdParams.length > 0) {
			file = (File) cmdParams[0];
		}
		saveSortGroup(file);

	}

	private void saveSortGroup(final File file) {
		final HDFIO hdfio = new HDFIO(STATUS.getFrame());
		final QuerySortMode mode = STATUS.getSortMode();
		if (mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK
				|| mode == SortMode.OFFLINE) {
			/* find sort group */
			final Group sortGroup = Group.getSortGroup();
			if (sortGroup != null) {
				if (file == null) { // No file given
					final JFileChooser jfile = new JFileChooser(HDFIO
							.getLastValidFile());
					jfile.setFileFilter(new HDFileFilter(true));
					final int option = jfile.showSaveDialog(STATUS.getFrame());
					/* don't do anything if it was cancel */
					if (option == JFileChooser.APPROVE_OPTION
							&& jfile.getSelectedFile() != null) {
						final File selectedFile = jfile.getSelectedFile();
						hdfio.writeFile(selectedFile, sortGroup);
					}
				} else {
					hdfio.writeFile(file, sortGroup);
				}
			}
		} else {// No sort group
			throw new IllegalStateException(
					"Need to be in a sort mode to save sort group.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(final String[] cmdTokens) {
		execute(null);
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
		setEnabled(mode == SortMode.OFFLINE || mode == SortMode.ONLINE_DISK
				|| mode == SortMode.ON_NO_DISK);
	}
}
