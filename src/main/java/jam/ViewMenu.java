package jam;

import com.google.inject.Inject;
import jam.commands.CommandManager;
import jam.commands.CommandNames;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.plot.PlotDisplay;
import jam.plot.View;

import javax.swing.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

final class ViewMenu implements PropertyChangeListener {
    /** Fit menu needed as members so we can add a fit. */
    private final transient JMenu view = new JMenu("View");

    private transient final PlotDisplay display;

    private transient final CommandManager commandManager;

    @Inject
    ViewMenu(final PlotDisplay display, final CommandManager commandManager,
            final Broadcaster broadcaster) {
        this.display = display;
        this.commandManager = commandManager;
        broadcaster.addPropertyChangeListener(this);
        this.updateViews();
    }

    protected JMenu getMenu() {
        return this.view;
    }

    private void updateViews() {
        view.removeAll();
        view.add(this.commandManager.getMenuItem(CommandNames.SHOW_VIEW_NEW));
        view.add(this.commandManager
                .getMenuItem(CommandNames.SHOW_VIEW_DELETE));
        view.addSeparator();
        for (final String name : View.getNameList()) {
            view.add(namedMenuItem(name));
        }
    }

    private JMenuItem namedMenuItem(final String name) {
        final JMenuItem rval = new JMenuItem(name);
        rval.addActionListener(event -> ViewMenu.this.display.setView(View.getView(name)));
        return rval;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final BroadcastEvent.Command command = ((BroadcastEvent) evt).getCommand();
        if (command == BroadcastEvent.Command.VIEW_NEW) {
            updateViews();
        }
    }

}
