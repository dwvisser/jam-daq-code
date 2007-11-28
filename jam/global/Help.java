package jam.global;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

public final class Help {

	private transient HelpSet helpset = null;

	private transient HelpBroker broker = null;

	private static Help instance = new Help();

	private static final Logger LOGGER = Logger.getLogger(Help.class
			.getPackage().getName());

	private Help() {
		final String helpsetName = "help/jam.hs";
		final URL hsURL = Thread.currentThread().getContextClassLoader()
				.getResource(helpsetName);
		try {
			helpset = new HelpSet(null, hsURL);
			broker = helpset.createHelpBroker();
		} catch (HelpSetException e) {
			final String message = "HelpSet " + helpsetName + " not found";
			LOGGER.log(Level.WARNING, message, e);
		}
	}

	public static Help getInstance() {
		return instance;
	}

	public HelpSet getHelpSet() {
		return helpset;
	}

	public HelpBroker getHelpBroker() {
		return broker;
	}
}
