package jam;

import jam.commands.CommandManager;
import jam.commands.CommandNames;
import jam.global.BroadcastEvent;
import jam.plot.PlotDisplay;
import jam.plot.View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.google.inject.Inject;

final class ViewMenu implements Observer {
	/** Fit menu needed as members so we can add a fit */
	private final transient JMenu view = new JMenu("View");

	private transient final PlotDisplay display;

	private transient final CommandManager commandManager;

	@Inject
	ViewMenu(final PlotDisplay display, final CommandManager commandManager) {
		this.display = display;
		this.commandManager = commandManager;
		this.updateViews();
	}

	protected JMenu getMenu() {
		return this.view;
	}

	private void updateViews() {
		view.removeAll();
		view.add(this.commandManager.getMenuItem(CommandNames.SHOW_VIEW_NEW));
		view
				.add(this.commandManager
						.getMenuItem(CommandNames.SHOW_VIEW_DELETE));
		view.addSeparator();
		for (final String name : View.getNameList()) {
			view.add(namedMenuItem(name));
		}
	}

	private JMenuItem namedMenuItem(final String name) {
		final JMenuItem rval = new JMenuItem(name);
		rval.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				ViewMenu.this.display.setView(View.getView(name));
			}
		});
		return rval;
	}

	/**
	 * @see Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.VIEW_NEW) {
			updateViews();
		}
	}

}
