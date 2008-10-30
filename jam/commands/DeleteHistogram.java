package jam.commands;

import jam.data.DataUtility;
import jam.data.Group;
import jam.data.AbstractHistogram;
import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.ui.SelectionTree;

import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Command for file menu new also clears
 * 
 * @author Ken Swartz
 * 
 */
final class DeleteHistogram extends AbstractCommand implements Observer {

	DeleteHistogram() {
		super("Delete\u2026");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D,
				CTRL_MASK));
	}

	/**
	 * Excecute command
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		final JFrame frame = STATUS.getFrame();
		final AbstractHistogram hist = (AbstractHistogram) SelectionTree.getCurrentHistogram();
		final String name = hist.getFullName().trim();
		final Group.Type type = DataUtility.getGroup(hist).getType();
		/* Cannot delete sort histograms */
		if (type == Group.Type.SORT) {
			LOGGER
					.severe("Cannot delete '" + name
							+ "', it is sort histogram.");
		} else {
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame,
					"Delete " + name + "?", "Delete histogram",
					JOptionPane.YES_NO_OPTION)) {
				hist.delete();
				BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
			}
		}
	}

	@Override
	protected void executeParse(final String[] cmdTokens) {
		execute(null);
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			final Nameable hist = SelectionTree.getCurrentHistogram();
			setEnabled(hist instanceof AbstractHistogram);
		}
	}

}
