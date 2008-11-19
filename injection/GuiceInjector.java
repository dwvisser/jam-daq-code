package injection;

import jam.JamInitialization;
import jam.commands.Commandable;
import jam.global.JamStatus;
import jam.io.AbstractImpExp;
import jam.script.Session;

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

}
