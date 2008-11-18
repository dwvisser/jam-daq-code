package jam.commands;

import injection.GuiceInjector;
import jam.data.control.MonitorControl;

import java.util.Observable;
import java.util.Observer;

/**
 * Command that shows the monitor config dialog.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowMonitorConfig extends AbstractShowDialog implements Observer {

	ShowMonitorConfig() {
		super("Configure Monitors\u2026");
		dialog = MonitorControl.getSingletonInstance();
	}

	public void update(final Observable observe, final Object obj) {
		setEnabled(GuiceInjector.getJamStatus().isOnline());
	}
}
