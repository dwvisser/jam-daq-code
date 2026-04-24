package jam.commands;

import com.google.inject.Inject;
import jam.data.Scaler;
import jam.data.control.ScalerDisplay;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Show the scalers dialog box
 *
 * @author Ken Swartz
 */
final class ShowDialogScalersCmd extends AbstractShowDialog implements PropertyChangeListener {

  @Inject
  ShowDialogScalersCmd(final ScalerDisplay scalerDisplay) {
    super("Display Scalers\u2026");
    dialog = scalerDisplay;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    setEnabled(!Scaler.getScalerList().isEmpty());
  }
}
