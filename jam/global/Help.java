package jam.global;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

/**
 * Utility class for JavaHelp usage.
 * 
 * @author Dale Visser
 * 
 */
public final class Help {

	private transient HelpSet helpset;

	private transient HelpBroker broker;

	private static Help instance = new Help();

	private static final Logger LOGGER = Logger.getLogger(Help.class
			.getPackage().getName());

	private Help() {
		final String helpsetName = "help/HelpSet.xml";
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

	/**
	 * @return the singleton instance of this type
	 */
	public static Help getInstance() {
		return instance;
	}

	/**
	 * @return the help set
	 */
	public HelpSet getHelpSet() {
		return helpset;
	}

	/**
	 * @return the help broker
	 */
	public HelpBroker getHelpBroker() {
		return broker;
	}
}
