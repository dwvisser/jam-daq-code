package injection;

import jam.JamInitialization;
import jam.Version;
import jam.commands.CommandManager;
import jam.commands.Commandable;
import jam.fit.ValueAndUncertaintyFormatter;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.RuntimeSubclassIdentifier;
import jam.io.AbstractImpExp;
import jam.io.hdf.HDFIO;
import jam.plot.Action;
import jam.plot.PlotContainer;
import jam.script.Session;
import jam.sort.RingBufferFactory;
import jam.ui.ConsoleLog;
import jam.ui.Icons;
import jam.util.FileUtilities;
import jam.util.NumberUtilities;
import jam.util.StringUtilities;

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
	private static final Injector INJECTOR = Guice.createInjector(new Module());

	private GuiceInjector() {
		// static class
	}

	/**
	 * @return a Session object
	 */
	public static Session getSession() {
		return INJECTOR.getInstance(Session.class);
	}

	/**
	 * @return the Jam Frame
	 */
	public static JamInitialization getJamInitialization() {
		return INJECTOR.getInstance(JamInitialization.class);
	}

	/**
	 * @return application status
	 */
	public static JamStatus getJamStatus() {
		return INJECTOR.getInstance(JamStatus.class);
	}

	/**
	 * @return the application frame
	 */
	public static JFrame getFrame() {
		return INJECTOR.getInstance(JFrame.class);
	}

	/**
	 * @param <T>
	 *            type to return
	 * @param clazz
	 *            class to return an instance of
	 * @return instance
	 */
	public static <T extends Commandable> T getInstance(final Class<T> clazz) {
		return INJECTOR.getInstance(clazz);
	}

	/**
	 * @param <T>
	 *            type to return
	 * @param clazz
	 *            class to return an instance of
	 * @return instance
	 */
	public static <T extends AbstractImpExp> T getInstance(final Class<T> clazz) {
		return INJECTOR.getInstance(clazz);
	}

	/**
	 * @return object for reading/writing Jam HDF files
	 */
	public static HDFIO getHDFIO() {
		return INJECTOR.getInstance(HDFIO.class);
	}

	/**
	 * @return object for handling plot actions
	 */
	public static Action getAction() {
		return INJECTOR.getInstance(Action.class);
	}

	/**
	 * @return the command manager
	 */
	public static CommandManager getCommandManager() {
		return INJECTOR.getInstance(CommandManager.class);
	}

	/**
	 * @return the broadcaster object
	 */
	public static Broadcaster getBroadcaster() {
		return INJECTOR.getInstance(Broadcaster.class);
	}

	/**
	 * @return the console log
	 */
	public static ConsoleLog getConsoleLog() {
		return INJECTOR.getInstance(ConsoleLog.class);
	}

	/**
	 * @return the string utilities object
	 */
	public static StringUtilities getStringUtilities() {
		return INJECTOR.getInstance(StringUtilities.class);
	}

	/**
	 * 
	 * @return the plot container object
	 */
	public static PlotContainer getPlotContainer() {
		return INJECTOR.getInstance(PlotContainer.class);
	}

	/**
	 * @return the ring buffer factory
	 */
	public static RingBufferFactory getRingBufferFactory() {
		return INJECTOR.getInstance(RingBufferFactory.class);
	}

	/**
	 * @return the subclass identifier helper object
	 */
	public static RuntimeSubclassIdentifier getRuntimeSubclassIdentifier() {
		return INJECTOR.getInstance(RuntimeSubclassIdentifier.class);
	}

	/**
	 * @return the jam version object
	 */
	public static Version getVersion() {
		return INJECTOR.getInstance(Version.class);
	}

	/**
	 * @return the file utility object
	 */
	public static FileUtilities getFileUtilities() {
		return INJECTOR.getInstance(FileUtilities.class);
	}

	/**
	 * @return repository of Jam's icons
	 */
	public static Icons getIcons() {
		return INJECTOR.getInstance(Icons.class);
	}

	/**
	 * @return a math and number utility object
	 */
	public static NumberUtilities getNumberUtilities() {
		return INJECTOR.getInstance(NumberUtilities.class);
	}

	/**
	 * @return object that formats numeric values and uncertainties together
	 */
	public static ValueAndUncertaintyFormatter getValueAndUncertaintyFormatter() {
		return INJECTOR.getInstance(ValueAndUncertaintyFormatter.class);
	}
}
