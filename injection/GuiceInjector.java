package injection;

import jam.JamInitialization;
import jam.commands.CommandManager;
import jam.commands.Commandable;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.io.AbstractImpExp;
import jam.io.hdf.HDFIO;
import jam.plot.Action;
import jam.script.Session;
import jam.ui.ConsoleLog;

import javax.swing.JFrame;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Injects objects.
 * 
 * @author Dale Visser
 * 
 */
public final class GuiceInjector {
	private static final Injector injector = Guice.createInjector(new Module());

	private GuiceInjector() {
		// static class
	}

	/**
	 * @return a Session object
	 */
	public static Session getSession() {
		return injector.getInstance(Session.class);
	}

	/**
	 * @return the Jam Frame
	 */
	public static JamInitialization getJamInitialization() {
		return injector.getInstance(JamInitialization.class);
	}

	/**
	 * @return application status
	 */
	public static JamStatus getJamStatus() {
		return injector.getInstance(JamStatus.class);
	}

	/**
	 * @return the application frame
	 */
	public static JFrame getFrame() {
		return injector.getInstance(JFrame.class);
	}

	/**
	 * @param <T>
	 *            type to return
	 * @param clazz
	 *            class to return an instance of
	 * @return instance
	 */
	public static <T extends Commandable> T getInstance(final Class<T> clazz) {
		return injector.getInstance(clazz);
	}

	/**
	 * @param <T>
	 *            type to return
	 * @param clazz
	 *            class to return an instance of
	 * @return instance
	 */
	public static <T extends AbstractImpExp> T getInstance(final Class<T> clazz) {
		return injector.getInstance(clazz);
	}

	/**
	 * @return object for reading/writing Jam HDF files
	 */
	public static HDFIO getHDFIO() {
		return injector.getInstance(HDFIO.class);
	}

	/**
	 * @return object for handling plot actions
	 */
	public static Action getAction() {
		return injector.getInstance(Action.class);
	}

	/**
	 * @return the command manager
	 */
	public static CommandManager getCommandManager() {
		return injector.getInstance(CommandManager.class);
	}

	/**
	 * @return the broadcaster object
	 */
	public static Broadcaster getBroadcaster() {
		return injector.getInstance(Broadcaster.class);
	}

	/**
	 * @return the console log
	 */
	public static ConsoleLog getConsoleLog() {
		return injector.getInstance(ConsoleLog.class);
	}

}
