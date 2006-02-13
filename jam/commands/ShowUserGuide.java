package jam.commands;

import jam.global.CommandListenerException;

import java.net.URL;
import java.util.logging.Level;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.JButton;

/**
 * @author Ken Swartz
 */
final class ShowUserGuide extends AbstractCommand {

	private transient final JButton proxy = new JButton();

	ShowUserGuide() {
		super("User Guide\u2026");
		final String helpsetName = "help/jam.hs";
		try {
			final URL hsURL = getClass().getClassLoader().getResource(
					helpsetName);
			final HelpSet help = new HelpSet(null, hsURL);
			proxy.addActionListener(new CSH.DisplayHelpFromSource(help
					.createHelpBroker()));
		} catch (Exception ee) {
			final String message = "HelpSet " + helpsetName + " not found";
			LOGGER.log(Level.WARNING, message, ee);
		}
	}

	/**
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(final Object[] cmdParams) {
		proxy.doClick();
	}

	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		execute(null);
	}
}
