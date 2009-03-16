package jam.commands;

import jam.data.control.MonitorControl;
import jam.global.JamStatus;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * Command that shows the monitor configuration dialog.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 4, 2004
 */
final class ShowMonitorConfig extends AbstractShowDialog implements Observer {

	private transient final JamStatus status;

	@Inject
	ShowMonitorConfig(final MonitorControl monitorControl,
			final JamStatus status) {
		super("Configure Monitors\u2026");
		this.dialog = monitorControl;
		this.status = status;
	}

	public void update(final Observable observe, final Object obj) {
		setEnabled(this.status.isOnline());
	}
}
