/*
 * Created on Jun 7, 2004
 */
package jam.commands;

import jam.RunControl;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * Stop data acquisition.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 7, 2004
 */
final class StopAcquisition extends AbstractCommand implements Observer {

	private RunControl control;

	public void initCommand(){
		putValue(NAME, "stop");
		putValue(SHORT_DESCRIPTION, "Pause data acquisition.");
		control=RunControl.getSingletonInstance();
		enable();
	}
	
	protected void execute(Object[] cmdParams) {
		control.stopAcq();
	}

	protected void executeParse(String[] cmdTokens) {
		execute(null);
	}

	public void update(Observable o, Object arg) {
		enable();
	}

	private void enable() {
		final SortMode mode=status.getSortMode();
		setEnabled(mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ON_NO_DISK);
	}
}
