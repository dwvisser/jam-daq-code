package jam;

import javax.swing.Action;
import javax.swing.JButton;

import com.google.inject.Inject;

import jam.commands.CommandManager;
import jam.commands.CommandNames;

/**
 * Main Toolbar for Jam.
 * 
 * @author Ken Swartz
 * 
 */
@SuppressWarnings("serial")
final class ToolBar extends javax.swing.JToolBar {

	private transient final CommandManager commands;

	/**
	 * Constructor.
	 * 
	 */
	@Inject
	ToolBar(final CommandManager commandManager) {
		super();
		this.commands = commandManager;
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
