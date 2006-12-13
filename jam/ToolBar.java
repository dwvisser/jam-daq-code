package jam;

import jam.commands.CommandManager;
import jam.global.CommandNames;

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
		final JButton rval = new JButton();
		rval.setAction(commands.getAction(command));
		rval.setText("");
		return rval;
	}
}
