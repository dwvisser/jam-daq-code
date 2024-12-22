package jam.commands;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import com.google.inject.Inject;

import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.io.hdf.HDFIO;

/**
 * Save to a hdf file
 * 
 * @author Ken Swartz
 * 
 */

final class SaveHDFCmd extends AbstractCommand implements PropertyChangeListener {

	private transient final JamStatus status;
	private transient final HDFIO hdfio;

	@Inject
	SaveHDFCmd(final JamStatus status, final HDFIO hdfio) {
		super("Save");
		this.status = status;
		this.hdfio = hdfio;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
				CTRL_MASK | InputEvent.SHIFT_DOWN_MASK));
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (((BroadcastEvent) evt).getCommand() == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			enable();
		}
	}

	private void enable() {
		final QuerySortMode mode = status.getSortMode();
		final boolean file = status.getOpenFile() != null;
		setEnabled(file && (mode == SortMode.FILE || mode == SortMode.NO_SORT));
	}
}
