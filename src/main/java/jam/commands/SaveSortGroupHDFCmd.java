package jam.commands;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;

import com.google.inject.Inject;

import jam.data.Group;
import jam.data.Warehouse;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

/**
 * Command to save the sort group of histograms.
 * 
 * @author Ken Swartz
 * 
 */

final class SaveSortGroupHDFCmd extends AbstractCommand implements PropertyChangeListener {

	private transient final Frame frame;
	private transient final JamStatus status;
	private transient final HDFIO hdfio;

	@Inject
	SaveSortGroupHDFCmd(final Frame frame, final JamStatus status,
			final HDFIO hdfio) {
		super("Save sort group as\u2026");
		this.frame = frame;
		this.status = status;
		this.hdfio = hdfio;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		File file = null;
		if (cmdParams != null && cmdParams.length > 0) {
			file = (File) cmdParams[0];
		}
		saveSortGroup(file);

	}

	private void saveSortGroup(final File file) {
		final QuerySortMode mode = this.status.getSortMode();
		if (mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK
				|| mode == SortMode.OFFLINE) {
			/* find sort group */
			final Group sortGroup = Warehouse.getSortGroupGetter()
					.getSortGroup();
			if (sortGroup != null) {
				if (file == null) { // No file given
					final JFileChooser jfile = new JFileChooser(HDFIO
							.getLastValidFile());
					jfile.setFileFilter(new HDFileFilter(true));
					final int option = jfile.showSaveDialog(frame);
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
	@Override
	protected void executeParse(final String[] cmdTokens) {
		execute(null);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (((BroadcastEvent) evt).getCommand() == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			enable();
		}
	}

	private void enable() {
		final QuerySortMode mode = this.status;
		setEnabled(mode == SortMode.OFFLINE || mode == SortMode.ONLINE_DISK
				|| mode == SortMode.ON_NO_DISK);
	}
}
