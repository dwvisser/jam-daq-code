package jam.ui;

import jam.commands.CommandManager;
import jam.global.CommandNames;

import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Main Toolbar for Jam.
 * 
 * @author Ken Swartz
 * 
 */
public final class JamToolBar extends JToolBar implements CommandNames {

	/**
	 * Constructor.
	 * 
	 */
	public JamToolBar() {
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
