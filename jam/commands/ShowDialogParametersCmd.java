package jam.commands;

import jam.data.DataParameter;
import jam.data.control.ParameterControl;

import java.util.Observable;
import java.util.Observer;

/**
 * Show parameters dialog.
 * 
 * @author Ken Swartz
 *
 */
final class ShowDialogParametersCmd extends AbstractShowDialog 
implements Observer {


	/**
	 * Initialize command
	 */
	ShowDialogParametersCmd(){
		super("Parameters\u2026");
		dialog = new ParameterControl();
	}
		
	public void update(final Observable observe, final Object obj){
		setEnabled(!DataParameter.getParameterList().isEmpty());
	}	
}
