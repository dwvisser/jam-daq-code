package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.inject.Inject;

import jam.data.control.MonitorControl;
import jam.global.JamStatus;

/**
 * Command that shows the monitor configuration dialog.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 4, 2004
 */
@SuppressWarnings("serial")
final class ShowMonitorConfig extends AbstractShowDialog implements PropertyChangeListener {

	private transient final JamStatus status;

	@Inject
	ShowMonitorConfig(final MonitorControl monitorControl,
			final JamStatus status) {
		super("Configure Monitors\u2026");
		this.dialog = monitorControl;
		this.status = status;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		setEnabled(this.status.isOnline());
	}
}
