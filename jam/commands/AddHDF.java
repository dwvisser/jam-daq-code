package jam.commands;

import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;

import java.io.File;
import java.util.Observable;

import com.google.inject.Inject;

/**
 * Add counts to histograms in memory from histograms in an HDF file.
 * 
 * @author Ken Swartz
 */
final class AddHDF extends AbstractLoaderHDF {

	private transient final JamStatus status;

	@Inject
	AddHDF(final HDFIO hdfio, final JamStatus status,
			final Broadcaster broadcaster) {
		super(hdfio, broadcaster);
		this.status = status;
	}

	@Override
	public void initCommand() {
		putValue(NAME, "Add Group Counts\u2026");
		fileOpenMode = FileOpenMode.ADD;
	}

	@Override
	protected void execute(final Object[] cmdParams) {
		File file = null;
		loadGroup = (Group) status.getCurrentGroup();
		// Parse command parameters if given
		if (cmdParams != null) {
			if (cmdParams.length > 0) {
				final Object param0 = cmdParams[0];
				file = (File) param0;
			}
			if (cmdParams.length > 1) {
				final Object param1 = cmdParams[1];
				loadGroup = (Group) param1;
			}
		}
		loadHDFFile(file, loadGroup);
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			setEnabled(status.getSortMode() != SortMode.REMOTE);
		}
	}
}
