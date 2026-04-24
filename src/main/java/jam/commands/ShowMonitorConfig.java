package jam.commands;

import com.google.inject.Inject;
import jam.data.control.MonitorControl;
import jam.global.JamStatus;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Command that shows the monitor configuration dialog.
 *
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 4, 2004
 */
final class ShowMonitorConfig extends AbstractShowDialog implements PropertyChangeListener {

  private final transient JamStatus status;

  @Inject
  ShowMonitorConfig(final MonitorControl monitorControl, final JamStatus status) {
    super("Configure Monitors\u2026");
    this.dialog = monitorControl;
    this.status = status;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    setEnabled(this.status.isOnline());
  }
}
