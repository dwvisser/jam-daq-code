package jam;

import jam.commands.CommandManager;

import javax.swing.JButton;

/**
 * Main Toolbar for Jam.
 * 
 * @author Ken Swartz
 * 
 */
final class ToolBar extends javax.swing.JToolBar implements
		jam.global.CommandNames {

	/**
	 * Constructor.
	 * 
	 */
	ToolBar() {
		super();
		add(createButton(OPEN_HDF));
		add(createButton(OPEN_ADD_HDF));
		add(createButton(OPEN_MULTIPLE_HDF));
		add(createButton(SAVE_HDF));
		add(createButton(SAVE_AS_HDF));
		add(createButton(PRINT));
		add(createButton(SHOW_RUN_CONTROL));
		add(createButton(START));
		add(createButton(STOP));
		add(createButton(SHOW_SORT_CONTROL));
		add(createButton(SHOW_HIST_ZERO));
		add(createButton(SHOW_SET_GATE));
	}

	private JButton createButton(final String command) {
		final CommandManager commands = CommandManager.getInstance();
		final JButton rval = new JButton();
		rval.setAction(commands.getAction(command));
		rval.setText("");
		return rval;
	}
}
