package jam;

import jam.comm.CommunicationPreferences;
import jam.commands.CommandManager;
import jam.commands.CommandNames;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.io.hdf.HDFPrefs;
import jam.plot.PlotDisplay;
import jam.plot.PlotPrefs;
import jam.plot.View;
import jam.plot.color.ColorPrefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * 
 * Jam's menu bar. Separated from JamMain to reduce its size and separate
 * responsibilities.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @version 1.4
 * @since 30 Dec 2003
 */
final class MenuBar implements Observer {

	final transient private JMenuItem impHist = new JMenu("Import");

	/** Fit menu needed as members so we can add a fit */
	final transient private JMenu fitting = new JMenu("Fitting");

	/** Fit menu needed as members so we can add a fit */
	final transient private JMenu view = new JMenu("View");

	final transient private JMenuBar menus = new JMenuBar();

	private static final MenuBar INSTANCE = new MenuBar();

	/**
	 * Jam's menu bar. It has the following menus:
	 * <ul>
	 * <li>File</li>
	 * <li>Setup</li>
	 * <li>Control</li>
	 * <li>Histogram</li>
	 * <li>Gate</li>
	 * <li>Scalers</li>
	 * <li>Preferencs</li>
	 * <li>Fitting</li>
	 * <li>Help</li>
	 * </ul>
	 * 
	 * @author Dale Visser
	 * @author Ken Swartz
	 */
	private MenuBar() {
		super();
		Broadcaster.getSingletonInstance().addObserver(this);
		menus.add(createFileMenu());
		menus.add(createSetupMenu());
		menus.add(createControlMenu());
		menus.add(createHistogramMenu());
		menus.add(createGateMenu());
		menus.add(createScalerMenu());
		menus.add(createViewMenu());
		menus.add(createPreferencesMenu());
		menus.add(createFitMenu());
		menus.add(createHelp());
	}

	private JMenu createFileMenu() {

		final JMenu file = new JMenu("File");

		file.add(getMenuItem(CommandNames.CLEAR));
		file.add(getMenuItem(CommandNames.OPEN_HDF));

		final JMenuItem openSpecial = new JMenu("Open Special");
		file.add(openSpecial);
		openSpecial.add(getMenuItem(CommandNames.OPEN_MULTIPLE_HDF));
		openSpecial.add(getMenuItem(CommandNames.OPEN_ADD_HDF));
		openSpecial.add(getMenuItem(CommandNames.OPEN_SELECTED));

		file.add(getMenuItem(CommandNames.RELOAD_HDF));
		file.add(getMenuItem(CommandNames.ADD_HDF));
		file.add(getMenuItem(CommandNames.SAVE_HDF));
		file.add(getMenuItem(CommandNames.SAVE_AS_HDF));

		final JMenuItem saveSpecial = new JMenu("Save Special");
		saveSpecial.add(getMenuItem(CommandNames.SAVE_SORT));
		saveSpecial.add(getMenuItem(CommandNames.SAVE_GROUP));
		saveSpecial.add(getMenuItem(CommandNames.SAVE_HISTOGRAMS));
		saveSpecial.add(getMenuItem(CommandNames.SAVE_GATES));

		file.add(saveSpecial);
		file.addSeparator();

		final JMenuItem utilities = new JMenu("Scaler Utilities");
		file.add(utilities);
		utilities.add(getMenuItem(CommandNames.OPEN_SCALERS));
		utilities.add(getMenuItem(CommandNames.SHOW_SCALER_SCAN));
		file.addSeparator();

		file.add(impHist);
		impHist.add(getMenuItem(CommandNames.IMPORT_TEXT));
		impHist.add(getMenuItem(CommandNames.IMPORT_SPE));
		impHist.add(getMenuItem(CommandNames.IMPORT_DAMM));
		impHist.add(getMenuItem(CommandNames.IMPORT_XSYS));
		impHist.add(getMenuItem(CommandNames.IMPORT_BAN));

		final JMenu expHist = new JMenu("Export");
		file.add(expHist);
		expHist.add(getMenuItem(CommandNames.EXPORT_TABLE));
		expHist.add(getMenuItem(CommandNames.EXPORT_TEXT));
		expHist.add(getMenuItem(CommandNames.EXPORT_SPE));
		expHist.add(getMenuItem(CommandNames.EXPORT_DAMM));
		expHist.add(getMenuItem(CommandNames.SHOW_BATCH_EXPORT));

		file.addSeparator();
		file.add(getMenuItem(CommandNames.PRINT));
		file.add(getMenuItem(CommandNames.PAGE_SETUP));
		file.addSeparator();
		file.add(getMenuItem(CommandNames.EXIT));

		return file;
	}

	private JMenu createSetupMenu() {
		final JMenu setup = new JMenu("Setup");
		setup.add(getMenuItem(CommandNames.SHOW_SETUP_ONLINE));
		setup.add(getMenuItem(CommandNames.SHOW_SETUP_OFF));
		setup.add(getMenuItem(CommandNames.SHOW_SETUP_REMOTE));
		setup.add(getMenuItem(CommandNames.SHOW_CONFIG));
		return setup;
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

	private JMenu createGateMenu() {

		final JMenu gate = new JMenu("Gate");
		menus.add(gate);
		gate.add(getMenuItem(CommandNames.SHOW_NEW_GATE));
		gate.add(getMenuItem(CommandNames.SHOW_ADD_GATE));
		gate.add(getMenuItem(CommandNames.SHOW_SET_GATE));
		return gate;
	}

	private JMenu createViewMenu() {

		updateViews();
		return view;

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

	private JMenu createHelp() {

		final JMenu helpMenu = new JMenu("Help");
		menus.add(helpMenu);
		helpMenu.add(getMenuItem(CommandNames.HELP_ABOUT));
		helpMenu.add(getMenuItem(CommandNames.USER_GUIDE));
		helpMenu.add(getMenuItem(CommandNames.HELP_LICENSE));
		return helpMenu;
	}

	private JMenu createPreferencesMenu() {
		final JMenu mPrefer = new JMenu("Preferences");
		mPrefer.add(getMenuItem(PlotPrefs.AUTO_IGNORE_ZERO));
		mPrefer.add(getMenuItem(PlotPrefs.AUTO_IGNORE_FULL));
		mPrefer.add(getMenuItem(PlotPrefs.AUTO_ON_EXPAND));
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
	private JMenuItem getMenuItem(final String name) {
		return new JMenuItem(CommandManager.getInstance().getAction(name));
	}

	/**
	 * @see Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			sortModeChanged();
		} else if (command == BroadcastEvent.Command.FIT_NEW) {
			final Action fitAction = (Action) (event.getContent());
			fitting.add(new JMenuItem(fitAction));
		} else if (command == BroadcastEvent.Command.VIEW_NEW) {
			updateViews();
		}
	}

	private void sortModeChanged() {
		final JamStatus status = JamStatus.getSingletonInstance();
		final QuerySortMode mode = status.getSortMode();
		final boolean file = mode == SortMode.FILE || mode == SortMode.NO_SORT;
		impHist.setEnabled(file);
	}

	private void updateViews() {
		view.removeAll();
		view.add(getMenuItem(CommandNames.SHOW_VIEW_NEW));
		view.add(getMenuItem(CommandNames.SHOW_VIEW_DELETE));
		view.addSeparator();
		for (final String name : View.getNameList()) {
			view.add(namedMenuItem(name));
		}
	}
	
	private JMenuItem namedMenuItem(final String name) {
		final JMenuItem rval = new JMenuItem(name);
		rval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				PlotDisplay.getDisplay().setView(View.getView(name));
			}
		});
		return rval;
	}

	/**
	 * @return the only menubar created by this class
	 */
	static JMenuBar getMenuBar() {
		return INSTANCE.menus;
	}
}