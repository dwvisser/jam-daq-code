/*
 * Created on Jun 7, 2004
 */
package jam.commands;

import jam.RunControl;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * Start data acquisition.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 7, 2004
 */
final class StartAcquisition extends AbstractCommand implements Observer {

	private RunControl control;

	public void initCommand(){
		putValue(NAME, "start");
		putValue(SHORT_DESCRIPTION, "Start data acquisition.");
		control=RunControl.getSingletonInstance();
		enable();
	}
	
	protected void execute(Object[] cmdParams) {
		control.startAcq();
	}

	protected void executeParse(String[] cmdTokens) {
		execute(null);
	}

	public void update(Observable o, Object arg) {
		enable();
	}

	private void enable() {
		final SortMode mode=STATUS.getSortMode();
		setEnabled(mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ON_NO_DISK);
	}
}
