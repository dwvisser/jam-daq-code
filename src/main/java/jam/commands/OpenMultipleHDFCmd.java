package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;

import com.google.inject.Inject;

import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.io.control.OpenMultipleFiles;

/**
 * Shows the open multiple files dialog
 * 
 * @author Ken Swartz
 * 
 */

public class OpenMultipleHDFCmd extends AbstractCommand implements PropertyChangeListener {

	private transient final OpenMultipleFiles openMultiple;
	private transient final JamStatus status;

	@Inject
	OpenMultipleHDFCmd(final OpenMultipleFiles openMultipleFiles,
			final JamStatus status) {
		super("Open Multiple\u2026");
		openMultiple = openMultipleFiles;
		this.status = status;
		final Icon iOpen = loadToolbarIcon("jam/ui/OpenMultiHDF.png");
		putValue(Action.SMALL_ICON, iOpen);
		putValue(Action.SHORT_DESCRIPTION, "Open multiple hdf files.");
	}

	@Override
	protected void execute(final Object[] cmdParams) throws CommandException {
		openMultiple.show();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		// do nothing
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (((BroadcastEvent) evt).getCommand() == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			enable();
		}
	}

	private void enable() {
		final QuerySortMode mode = this.status.getSortMode();
		setEnabled(mode == SortMode.FILE || mode == SortMode.NO_SORT);
	}

}
