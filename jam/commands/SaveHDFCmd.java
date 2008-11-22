package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.io.hdf.HDFIO;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import com.google.inject.Inject;

/**
 * Save to a hdf file
 * 
 * @author Ken Swartz
 * 
 */
final class SaveHDFCmd extends AbstractCommand implements Observer {

	private transient final JamStatus status;
	private transient final HDFIO hdfio;

	@Inject
	SaveHDFCmd(final JamStatus status, final HDFIO hdfio) {
		super("Save");
		this.status = status;
		this.hdfio = hdfio;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
				CTRL_MASK | InputEvent.SHIFT_MASK));
		final Icon iSave = loadToolbarIcon("jam/ui/SaveHDF.png");
		putValue(Action.SMALL_ICON, iSave);
		putValue(Action.SHORT_DESCRIPTION,
				"Save histograms to a hdf data file.");

		enable(); // depending on sort mode
	}

	/**
	 * Save to the last file opened.
	 * 
	 * @param cmdParams
	 *            not used
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		// No command options used
		final File file = this.status.getOpenFile();
		if (file == null) { // File null, shouldn't be.
			throw new IllegalStateException(
					"Expected a reference for the previously accessed file.");
		}
		hdfio.writeFile(file, true, true);
	}

	/**
	 * Save to the last file opened.
	 * 
	 * @param cmdTokens
	 *            not used
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	@Override
	protected void executeParse(final String[] cmdTokens) {
		execute(null);
	}

	/**
	 * Listens to messages to enable/disable this action.
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			enable();
		}
	}

	private void enable() {
		final QuerySortMode mode = status.getSortMode();
		final boolean file = status.getOpenFile() != null;
		setEnabled(file && (mode == SortMode.FILE || mode == SortMode.NO_SORT));
	}
}
