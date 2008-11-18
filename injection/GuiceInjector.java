package injection;

import jam.JamInitialization;
import jam.script.Session;
import jam.sort.control.SetupSortOn;

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
	public static JamInitialization getJamFrame() {
		return injector.getInstance(JamInitialization.class);
	}

	/**
	 * @return the online setup dialog
	 */
	public static SetupSortOn getSetupSortOn() {
		return injector.getInstance(SetupSortOn.class);
	}
}
