package jam;

import com.google.inject.Inject;
import jam.comm.CommunicationPreferences;
import jam.commands.CommandManager;
import jam.commands.CommandNames;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.io.hdf.HDFPrefs;
import jam.plot.PlotPreferences;
import jam.plot.color.ColorPrefs;

import javax.swing.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Jam's menu bar. Separated from JamMain to reduce its size and separate
 * responsibilities.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser </a>
 * @version 1.4
 * @since 30 December 2003
 */
final class MenuBar implements PropertyChangeListener {

    /** Fit menu needed as members so we can add a fit */
    final transient private JMenu fitting = new JMenu("Fitting");

    final transient private JMenuBar menus = new JMenuBar();

    private transient final CommandManager commandManager;

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
     * @author Dale Visser
     * @author Ken Swartz
     */
    @Inject
    protected MenuBar(final ViewMenu viewMenu, final ImportMenu importMenu,
            final Broadcaster broadcaster, final CommandManager commandManager) {
        super();
        broadcaster.addPropertyChangeListener(this);
        this.commandManager = commandManager;
        menus.add(createFileMenu(importMenu));
        menus.add(this.commandManager.createMenu("Setup",
                CommandNames.SHOW_SETUP_ONLINE, CommandNames.SHOW_SETUP_OFF,
                CommandNames.SHOW_SETUP_REMOTE, CommandNames.SHOW_CONFIG));
        menus.add(createControlMenu());
        menus.add(createHistogramMenu());
        menus.add(this.commandManager.createMenu("Gate",
                CommandNames.SHOW_NEW_GATE, CommandNames.SHOW_ADD_GATE,
                CommandNames.SHOW_SET_GATE));
        menus.add(createScalerMenu());
        menus.add(viewMenu.getMenu());
        menus.add(createPreferencesMenu());
        menus.add(createFitMenu());
        menus.add(this.commandManager.createMenu("Help",
                CommandNames.HELP_ABOUT, CommandNames.USER_GUIDE,
                CommandNames.HELP_LICENSE));
    }

    private JMenu createFileMenu(final ImportMenu importMenu) {
        final JMenu file = this.commandManager.createMenu("File",
                CommandNames.CLEAR, CommandNames.OPEN_HDF);
        file.add(this.commandManager.createMenu("Open Special",
                CommandNames.OPEN_MULTIPLE_HDF, CommandNames.OPEN_ADD_HDF,
                CommandNames.OPEN_SELECTED));
        file.add(this.commandManager.getMenuItem(CommandNames.RELOAD_HDF));
        file.add(this.commandManager.getMenuItem(CommandNames.ADD_HDF));
        file.add(this.commandManager.getMenuItem(CommandNames.SAVE_HDF));
        file.add(this.commandManager.getMenuItem(CommandNames.SAVE_AS_HDF));

        final JMenuItem saveSpecial = this.commandManager.createMenu(
                "Save Special", CommandNames.SAVE_SORT,
                CommandNames.SAVE_GROUP, CommandNames.SAVE_HISTOGRAMS,
                CommandNames.SAVE_GATES);

        file.add(saveSpecial);
        file.addSeparator();

        final JMenuItem utilities = this.commandManager.createMenu(
                "Scaler Utilities", CommandNames.OPEN_SCALERS,
                CommandNames.SHOW_SCALER_SCAN);
        file.add(utilities);
        file.addSeparator();

        file.add(importMenu.getMenu());

        final JMenu expHist = this.commandManager.createMenu("Export",
                CommandNames.EXPORT_TABLE, CommandNames.EXPORT_TEXT,
                CommandNames.EXPORT_SPE, CommandNames.EXPORT_DAMM,
                CommandNames.SHOW_BATCH_EXPORT);
        file.add(expHist);

        file.addSeparator();
        file.add(this.commandManager.getMenuItem(CommandNames.PRINT));
        file.add(this.commandManager.getMenuItem(CommandNames.PAGE_SETUP));
        file.addSeparator();
        file.add(this.commandManager.getMenuItem(CommandNames.EXIT));

        return file;
    }

    private JMenu createControlMenu() {
        final JMenu mcontrol = new JMenu("Control");
        mcontrol.add(this.commandManager.getMenuItem(CommandNames.START));
        mcontrol.add(this.commandManager.getMenuItem(CommandNames.STOP));
        mcontrol.add(this.commandManager.getMenuItem(CommandNames.FLUSH));
        mcontrol.addSeparator();
        mcontrol.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_RUN_CONTROL));
        mcontrol.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_SORT_CONTROL));
        mcontrol.add(this.commandManager.getMenuItem(CommandNames.PARAMETERS));
        mcontrol.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_BUFFER_COUNT));
        return mcontrol;
    }

    private JMenu createHistogramMenu() {
        final JMenu histogram = new JMenu("Histogram");
        final JMenuItem group = new JMenu("Group");
        histogram.add(group);
        group
                .add(this.commandManager
                        .getMenuItem(CommandNames.SHOW_NEW_GROUP));
        group.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_RENAME_GROUP));
        group.add(this.commandManager.getMenuItem(CommandNames.DELETE_GROUP));

        histogram.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_NEW_HIST));
        histogram.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_HIST_ZERO));
        histogram.add(this.commandManager
                .getMenuItem(CommandNames.DELETE_HISTOGRAM));
        histogram.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_HIST_FIT));
        histogram.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_HIST_PROJECT));
        histogram.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_HIST_COMBINE));
        histogram.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_GAIN_SHIFT));

        return histogram;
    }

    private JMenu createScalerMenu() {
        final JMenu scalers = new JMenu("Scaler");
        menus.add(scalers);
        scalers.add(this.commandManager
                .getMenuItem(CommandNames.DISPLAY_SCALERS));
        scalers.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_ZERO_SCALERS));
        scalers.addSeparator();
        scalers.add(this.commandManager
                .getMenuItem(CommandNames.DISPLAY_MONITORS));
        scalers.add(this.commandManager
                .getMenuItem(CommandNames.DISPLAY_MON_CFG));
        return scalers;
    }

    private JMenu createFitMenu() {
        fitting
                .add(this.commandManager
                        .getMenuItem(CommandNames.SHOW_FIT_NEW));
        fitting.addSeparator();
        return fitting;
    }

    private JMenu createPreferencesMenu() {
        final JMenu mPrefer = this.commandManager.createMenu("Preferences",
                PlotPreferences.AUTO_IGNORE_ZERO,
                PlotPreferences.AUTO_IGNORE_FULL,
                PlotPreferences.AUTO_ON_EXPAND);
        mPrefer.addSeparator();
        mPrefer.add(this.commandManager
                .getMenuItem(PlotPreferences.HIGHLIGHT_GATE));
        mPrefer.add(this.commandManager.getMenuItem(ColorPrefs.SMOOTH_SCALE));
        mPrefer.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_GRADIENT));
        mPrefer.add(this.commandManager
                .getMenuItem(PlotPreferences.ENABLE_SCROLLING));
        mPrefer.add(this.commandManager
                .getMenuItem(PlotPreferences.DISPLAY_LABELS));
        mPrefer.add(this.commandManager
                .getMenuItem(PlotPreferences.BLACK_BACKGROUND));
        mPrefer.addSeparator();
        mPrefer.add(this.commandManager
                .getMenuItem(PlotPreferences.AUTO_PEAK_FIND));
        mPrefer.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_PEAK_FIND));
        mPrefer.addSeparator();
        mPrefer.add(this.commandManager.getMenuItem(HDFPrefs.SUPPRES_EMPTY));
        mPrefer.addSeparator();
        mPrefer.add(this.commandManager
                .getMenuItem(CommunicationPreferences.VERBOSE));
        mPrefer.add(this.commandManager
                .getMenuItem(CommunicationPreferences.DEBUG));
        return mPrefer;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final BroadcastEvent event = (BroadcastEvent) evt;
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