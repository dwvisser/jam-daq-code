/*
 * Created on June 4, 2004
 */
package jam.commands;

import jam.data.Monitor;
import jam.data.control.MonitorDisplay;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 4, 2004
 */
final class ShowMonitorDisplay extends AbstractShowDialog implements Observer {

	@Inject
	ShowMonitorDisplay(final MonitorDisplay monitorDisplay) {
		super("Display Monitors\u2026");
		dialog = monitorDisplay;
	}

	public void update(final Observable observe, final Object obj) {
		setEnabled(!Monitor.getMonitorList().isEmpty());
	}
}
