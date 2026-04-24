package jam.commands;

import com.google.inject.Inject;
import jam.data.Scaler;
import jam.data.control.ScalerZero;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Show the zero scalers dialog.
 *
 * @author Ken Swartz
 */
final class ShowDialogZeroScalersCmd extends AbstractShowDialog implements PropertyChangeListener {

  @Inject
  ShowDialogZeroScalersCmd(final ScalerZero scalerZero) {
    super("Zero Scalers\u2026");
    dialog = scalerZero;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    setEnabled(!Scaler.getScalerList().isEmpty());
  }
}
