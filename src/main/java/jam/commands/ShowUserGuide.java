package jam.commands;

import jam.global.CommandListenerException;
import jam.global.Help;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.*;

/**
 * @author Ken Swartz
 */
final class ShowUserGuide extends AbstractCommand {

	private transient final JButton proxy = new JButton();

	ShowUserGuide() {
		super("User Guide\u2026");
		final HelpSet help = Help.getInstance().getHelpSet();
		proxy.addActionListener(new CSH.DisplayHelpFromSource(help
				.createHelpBroker()));
	}

	/**
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		proxy.doClick();
	}

	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		execute(null);
	}
}
