package jam.commands;

import com.google.inject.Inject;
import jam.data.DataParameter;
import jam.data.control.ParameterControl;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Show parameters dialog.
 *
 * @author Ken Swartz
 */
final class ShowDialogParametersCmd extends AbstractShowDialog implements PropertyChangeListener {

  /** Initialize command */
  @Inject
  ShowDialogParametersCmd(final ParameterControl parameterControl) {
    super("Parameters\u2026");
    dialog = parameterControl;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    setEnabled(!DataParameter.getParameterList().isEmpty());
  }
}
