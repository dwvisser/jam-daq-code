package jam;

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

final class ViewMenu implements Observer {
	/** Fit menu needed as members so we can add a fit */
	private final transient JMenu view = new JMenu("View");

	ViewMenu() {
		this.updateViews();
	}

	protected JMenu getMenu() {
		return this.view;
	}

	private void updateViews() {
		view.removeAll();
		view.add(MenuBar.getMenuItem(CommandNames.SHOW_VIEW_NEW));
		view.add(MenuBar.getMenuItem(CommandNames.SHOW_VIEW_DELETE));
		view.addSeparator();
		for (final String name : View.getNameList()) {
			view.add(namedMenuItem(name));
		}
	}

	private JMenuItem namedMenuItem(final String name) {
		final JMenuItem rval = new JMenuItem(name);
		rval.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				PlotDisplay.getDisplay().setView(View.getView(name));
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
