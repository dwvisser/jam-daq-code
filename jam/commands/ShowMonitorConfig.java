/*
 * Created on Jun 4, 2004
 */
package jam.commands;
import jam.data.Monitor;
import jam.data.control.MonitorControl;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowMonitorConfig extends AbstractShowDialog implements 
Observer {

	private final List monitorList=Monitor.getMonitorList();

	protected void initCommand(){
		putValue(NAME,"Configure Monitors\u2026");
		dialog=MonitorControl.getSingletonInstance();
	}

	public void update(Observable observe, Object obj){
		setEnabled(!monitorList.isEmpty());
	}	
}
