package jam.commands;

import jam.data.DataBase;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandListenerException;
import jam.global.JamStatus;
import jam.global.Nameable;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.inject.Inject;

/**
 * Delete a group
 * 
 * @author Ken Swartz
 * 
 */
public class DeleteGroup extends AbstractCommand {

	private transient final JFrame frame;
	private transient final JamStatus status;
	private transient final Broadcaster broadcaster;

	@Inject
	DeleteGroup(final JFrame frame, final JamStatus status,
			final Broadcaster broadcaster) {
		super();
		this.frame = frame;
		this.status = status;
		this.broadcaster = broadcaster;
		putValue(NAME, "Delete Group\u2026");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) throws CommandException {
		final Nameable nameable = status.getCurrentGroup();
		if (!DataBase.getInstance().isValid(nameable)) {
			LOGGER.severe("Need to select a group.");
			return;
		}
		final Group group = (Group) nameable;
		final Group.Type type = group.getType();
		final String name = group.getName();
		/* Cannot delete sort histograms */
		if (type == Group.Type.SORT) {
			LOGGER.severe("Cannot delete '" + name + "', it is sort group.");
		} else {
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame,
					"Delete " + name + "?", "Delete group",
					JOptionPane.YES_NO_OPTION)) {
				jam.data.Warehouse.getGroupCollection().remove(group);
				this.broadcaster
						.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		// nothing to do
	}

}
