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
public class ShowMonitorDisplay extends AbstractShowDialog implements 
Observer {

	private final List monitorList=Monitor.getMonitorList();

	protected void initCommand(){
		putValue(NAME,"Display Monitors\u2026");
		dialog=MonitorControl.getSingletonInstance(msghdlr).display;
	}

	public void update(Observable observe, Object obj){
		setEnabled(!monitorList.isEmpty());
	}	
}
