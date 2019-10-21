/*
 * Created on June 4, 2004
 */
package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.inject.Inject;

import jam.data.Monitor;
import jam.data.control.MonitorDisplay;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 4, 2004
 */
@SuppressWarnings("serial")
final class ShowMonitorDisplay extends AbstractShowDialog implements PropertyChangeListener {

	@Inject
	ShowMonitorDisplay(final MonitorDisplay monitorDisplay) {
		super("Display Monitors\u2026");
		dialog = monitorDisplay;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		setEnabled(!Monitor.getMonitorList().isEmpty());
	}
}
