package jam.commands;
import jam.data.control.MonitorControl;

import java.util.Observable;
import java.util.Observer;

/**
 * Command that shows the monitor config dialog.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowMonitorConfig extends AbstractShowDialog implements 
Observer {

	public void initCommand(){
		putValue(NAME,"Configure Monitors\u2026");
		dialog=MonitorControl.getSingletonInstance();
	}

	public void update(Observable observe, Object obj){
		setEnabled(STATUS.isOnline());
	}	
}
