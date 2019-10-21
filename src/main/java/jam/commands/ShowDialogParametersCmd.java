package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.inject.Inject;

import jam.data.DataParameter;
import jam.data.control.ParameterControl;

/**
 * Show parameters dialog.
 * 
 * @author Ken Swartz
 * 
 */
@SuppressWarnings("serial")
final class ShowDialogParametersCmd extends AbstractShowDialog implements PropertyChangeListener {

	/**
	 * Initialize command
	 */
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
