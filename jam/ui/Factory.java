package jam.ui;

import jam.commands.CommandManager;
import jam.global.LoggerConfig;

/**
 * C
 * 
 * @author Dale Visser
 * 
 */
public final class Factory {
	private Factory() {
		// mean to be static
	}

	/**
	 * @param packageName
	 *            package name for logger config
	 * @return a new console
	 */
	public static Console createConsole(final String packageName) {
		final CommandManager manager = CommandManager.getInstance();
		final Console result = new Console(manager, manager);
		new LoggerConfig(packageName, result.getLog());
		return result;
	}
}
