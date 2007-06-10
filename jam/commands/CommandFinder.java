package jam.commands;

import java.util.Collection;

public interface CommandFinder {

	/**
	 * Help the user by getting similar commands.
	 * 
	 * @param string
	 *            what the user typed
	 * @param onlyEnabled
	 *            <code>true</code> means only return enabled commands
	 * @return list of similar commands
	 */
	Collection<String> getSimilarCommnands(final String string,
			final boolean onlyEnabled);
}