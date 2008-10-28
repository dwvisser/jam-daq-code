package jam;

import jam.commands.CommandManager;
import jam.commands.CommandNames;

import javax.swing.Action;
import javax.swing.JButton;

/**
 * Main Toolbar for Jam.
 * 
 * @author Ken Swartz
 * 
 */
final class ToolBar extends javax.swing.JToolBar {

	/**
	 * Constructor.
	 * 
	 */
	ToolBar() {
		super();
		add(createButton(CommandNames.OPEN_HDF));
		add(createButton(CommandNames.OPEN_ADD_HDF));
		add(createButton(CommandNames.OPEN_MULTIPLE_HDF));
		add(createButton(CommandNames.SAVE_HDF));
		add(createButton(CommandNames.SAVE_AS_HDF));
		add(createButton(CommandNames.PRINT));
		add(createButton(CommandNames.SHOW_RUN_CONTROL));
		add(createButton(CommandNames.START));
		add(createButton(CommandNames.STOP));
		add(createButton(CommandNames.SHOW_SORT_CONTROL));
		add(createButton(CommandNames.SHOW_HIST_ZERO));
		add(createButton(CommandNames.SHOW_SET_GATE));
	}

	private JButton createButton(final String command) {
		final CommandManager commands = CommandManager.getInstance();
		final Action action = commands.getAction(command);
		if (null == action) {
			throw new IllegalArgumentException("Couldn't find action for '"
					+ command + "'.");
		}

		final JButton rval = new JButton(action);
		rval.setText("");
		return rval;
	}
}
