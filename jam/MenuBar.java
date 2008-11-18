package jam;

import jam.comm.CommunicationPreferences;
import jam.commands.CommandManager;
import jam.commands.CommandNames;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.io.hdf.HDFPrefs;
import jam.plot.PlotPrefs;
import jam.plot.color.ColorPrefs;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.google.inject.Inject;

/**
 * 
 * Jam's menu bar. Separated from JamMain to reduce its size and separate
 * responsibilities.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @version 1.4
 * @since 30 December 2003
 */
final class MenuBar implements Observer {

	/** Fit menu needed as members so we can add a fit */
	final transient private JMenu fitting = new JMenu("Fitting");

	final transient private JMenuBar menus = new JMenuBar();

	/**
	 * Jam's menu bar. It has the following menus:
	 * <ul>
	 * <li>File</li>
	 * <li>Setup</li>
	 * <li>Control</li>
	 * <li>Histogram</li>
	 * <li>Gate</li>
	 * <li>Scalers</li>
	 * <li>Preferences</li>
	 * <li>Fitting</li>
	 * <li>Help</li>
	 * </ul>
	 * 
	 * @author Dale Visser
	 * @author Ken Swartz
	 */
	@Inject
	protected MenuBar(final ViewMenu viewMenu) {
		super();
		Broadcaster.getSingletonInstance().addObserver(this);
		menus.add(createFileMenu());
		menus.add(createMenu("Setup", CommandNames.SHOW_SETUP_ONLINE,
				CommandNames.SHOW_SETUP_OFF, CommandNames.SHOW_SETUP_REMOTE,
				CommandNames.SHOW_CONFIG));
		menus.add(createControlMenu());
		menus.add(createHistogramMenu());
		menus.add(createMenu("Gate", CommandNames.SHOW_NEW_GATE,
				CommandNames.SHOW_ADD_GATE, CommandNames.SHOW_SET_GATE));
		menus.add(createScalerMenu());
		menus.add(viewMenu.getMenu());
		menus.add(createPreferencesMenu());
		menus.add(createFitMenu());
		menus.add(createMenu("Help", CommandNames.HELP_ABOUT,
				CommandNames.USER_GUIDE, CommandNames.HELP_LICENSE));
	}

	private JMenu createFileMenu() {
		final JMenu file = createMenu("File", CommandNames.CLEAR,
				CommandNames.OPEN_HDF);
		file.add(createMenu("Open Special", CommandNames.OPEN_MULTIPLE_HDF,
				CommandNames.OPEN_ADD_HDF, CommandNames.OPEN_SELECTED));
		file.add(getMenuItem(CommandNames.RELOAD_HDF));
		file.add(getMenuItem(CommandNames.ADD_HDF));
		file.add(getMenuItem(CommandNames.SAVE_HDF));
		file.add(getMenuItem(CommandNames.SAVE_AS_HDF));

		final JMenuItem saveSpecial = createMenu("Save Special",
				CommandNames.SAVE_SORT, CommandNames.SAVE_GROUP,
				CommandNames.SAVE_HISTOGRAMS, CommandNames.SAVE_GATES);

		file.add(saveSpecial);
		file.addSeparator();

		final JMenuItem utilities = createMenu("Scaler Utilities",
				CommandNames.OPEN_SCALERS, CommandNames.SHOW_SCALER_SCAN);
		file.add(utilities);
		file.addSeparator();

		final ImportMenu importMenu = new ImportMenu();
		file.add(importMenu.getMenu());

		final JMenu expHist = createMenu("Export", CommandNames.EXPORT_TABLE,
				CommandNames.EXPORT_TEXT, CommandNames.EXPORT_SPE,
				CommandNames.EXPORT_DAMM, CommandNames.SHOW_BATCH_EXPORT);
		file.add(expHist);

		file.addSeparator();
		file.add(getMenuItem(CommandNames.PRINT));
		file.add(getMenuItem(CommandNames.PAGE_SETUP));
		file.addSeparator();
		file.add(getMenuItem(CommandNames.EXIT));

		return file;
	}

	protected static JMenu createMenu(final String name,
			final String... commandNames) {
		final JMenu result = new JMenu(name);
		for (String commandName : commandNames) {
			result.add(getMenuItem(commandName));
		}

		return result;
	}

	private JMenu createControlMenu() {
		final JMenu mcontrol = new JMenu("Control");
		mcontrol.add(getMenuItem(CommandNames.START));
		mcontrol.add(getMenuItem(CommandNames.STOP));
		mcontrol.add(getMenuItem(CommandNames.FLUSH));
		mcontrol.addSeparator();
		mcontrol.add(getMenuItem(CommandNames.SHOW_RUN_CONTROL));
		mcontrol.add(getMenuItem(CommandNames.SHOW_SORT_CONTROL));
		mcontrol.add(getMenuItem(CommandNames.PARAMETERS));
		mcontrol.add(getMenuItem(CommandNames.SHOW_BUFFER_COUNT));
		return mcontrol;
	}

	private JMenu createHistogramMenu() {
		final JMenu histogram = new JMenu("Histogram");
		final JMenuItem group = new JMenu("Group");
		histogram.add(group);
		group.add(getMenuItem(CommandNames.SHOW_NEW_GROUP));
		group.add(getMenuItem(CommandNames.SHOW_RENAME_GROUP));
		group.add(getMenuItem(CommandNames.DELETE_GROUP));

		histogram.add(getMenuItem(CommandNames.SHOW_NEW_HIST));
		histogram.add(getMenuItem(CommandNames.SHOW_HIST_ZERO));
		histogram.add(getMenuItem(CommandNames.DELETE_HISTOGRAM));
		histogram.add(getMenuItem(CommandNames.SHOW_HIST_FIT));
		histogram.add(getMenuItem(CommandNames.SHOW_HIST_PROJECT));
		histogram.add(getMenuItem(CommandNames.SHOW_HIST_COMBINE));
		histogram.add(getMenuItem(CommandNames.SHOW_GAIN_SHIFT));

		return histogram;
	}

	private JMenu createScalerMenu() {
		final JMenu scalers = new JMenu("Scaler");
		menus.add(scalers);
		scalers.add(getMenuItem(CommandNames.DISPLAY_SCALERS));
		scalers.add(getMenuItem(CommandNames.SHOW_ZERO_SCALERS));
		scalers.addSeparator();
		scalers.add(getMenuItem(CommandNames.DISPLAY_MONITORS));
		scalers.add(getMenuItem(CommandNames.DISPLAY_MON_CFG));
		return scalers;
	}

	private JMenu createFitMenu() {
		fitting.add(getMenuItem(CommandNames.SHOW_FIT_NEW));
		fitting.addSeparator();
		return fitting;
	}

	private JMenu createPreferencesMenu() {
		final JMenu mPrefer = createMenu("Preferences",
				PlotPrefs.AUTO_IGNORE_ZERO, PlotPrefs.AUTO_IGNORE_FULL,
				PlotPrefs.AUTO_ON_EXPAND);
		mPrefer.addSeparator();
		mPrefer.add(getMenuItem(PlotPrefs.HIGHLIGHT_GATE));
		mPrefer.add(getMenuItem(ColorPrefs.SMOOTH_SCALE));
		mPrefer.add(getMenuItem(CommandNames.SHOW_GRADIENT));
		mPrefer.add(getMenuItem(PlotPrefs.ENABLE_SCROLLING));
		mPrefer.add(getMenuItem(PlotPrefs.DISPLAY_LABELS));
		mPrefer.add(getMenuItem(PlotPrefs.BLACK_BACKGROUND));
		mPrefer.addSeparator();
		mPrefer.add(getMenuItem(PlotPrefs.AUTO_PEAK_FIND));
		mPrefer.add(getMenuItem(CommandNames.SHOW_PEAK_FIND));
		mPrefer.addSeparator();
		mPrefer.add(getMenuItem(HDFPrefs.SUPPRES_EMPTY));
		mPrefer.addSeparator();
		mPrefer.add(getMenuItem(CommunicationPreferences.VERBOSE));
		mPrefer.add(getMenuItem(CommunicationPreferences.DEBUG));
		return mPrefer;
	}

	/**
	 * Produce a menu item that invokes the action given by the lookup table in
	 * <code>jam.commands.CommandManager</code>
	 * 
	 * @param name
	 *            name of the command
	 * @return JMenuItem that invokes the associated action
	 */
	protected static JMenuItem getMenuItem(final String name) {
		final Action action = CommandManager.getInstance().getAction(name);
		if (null == action) {
			throw new IllegalArgumentException("Couldn't find action for '"
					+ name + "'.");
		}
		return new JMenuItem(action);
	}

	/**
	 * @see Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.FIT_NEW) {
			final Action fitAction = (Action) (event.getContent());
			fitting.add(new JMenuItem(fitAction));
		}
	}

	/**
	 * @return the only menubar created by this class
	 */
	protected JMenuBar getMenuBar() {
		return menus;
	}
}