/*
 * Created on Jun 7, 2004
 */
package jam.commands;

import jam.global.SortMode;
import jam.sort.control.RunControl;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;

/**
 * Start data acquisition.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 7, 2004
 */
final class StartAcquisition extends AbstractCommand implements Observer {

	StartAcquisition() {
		super("start");
		final Icon iStart = loadToolbarIcon("jam/ui/Start.png");
		putValue(Action.SMALL_ICON, iStart);
		putValue(SHORT_DESCRIPTION, "Start data acquisition.");

	}

	protected void execute(final Object[] cmdParams) {
		RunControl.getSingletonInstance().startAcq();
	}

	protected void executeParse(final String[] cmdTokens) {
		execute(null);
	}

	public void update(final Observable obs, final Object arg) {
		enable();
	}

	private void enable() {
		final SortMode mode = STATUS.getSortMode();
		setEnabled(mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK);
	}
}
