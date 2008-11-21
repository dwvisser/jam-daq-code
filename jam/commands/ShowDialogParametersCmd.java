package jam.commands;

import jam.data.DataParameter;
import jam.data.control.ParameterControl;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * Show parameters dialog.
 * 
 * @author Ken Swartz
 * 
 */
final class ShowDialogParametersCmd extends AbstractShowDialog implements
		Observer {

	/**
	 * Initialize command
	 */
	@Inject
	ShowDialogParametersCmd(final ParameterControl parameterControl) {
		super("Parameters\u2026");
		dialog = parameterControl;
	}

	public void update(final Observable observe, final Object obj) {
		setEnabled(!DataParameter.getParameterList().isEmpty());
	}
}
