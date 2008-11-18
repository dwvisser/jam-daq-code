/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.commands;

import injection.GuiceInjector;
import jam.global.QuerySortMode;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowRunControl extends AbstractShowDialog implements Observer {

	ShowRunControl() {
		super("Run\u2026");
		final Icon iRun = loadToolbarIcon("jam/ui/Run.png");
		putValue(Action.SMALL_ICON, iRun);
		putValue(Action.SHORT_DESCRIPTION, "Run Control.");
		dialog = GuiceInjector.getRunControl();
		enable();
	}

	private void enable() {
		final QuerySortMode mode = GuiceInjector.getJamStatus().getSortMode();
		setEnabled(mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK);
	}

	public void update(final Observable observe, final Object obj) {
		enable();
	}

}
