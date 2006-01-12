package jam.commands;

import jam.data.DataParameter;
import jam.data.control.ParameterControl;

import java.util.List;
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

	private final List paramList=DataParameter.getParameterList();

	/**
	 * Initialize command
	 */
	public void initCommand(){
		putValue(NAME, "Parameters\u2026");
		dialog = new ParameterControl();
	}
		
	public void update(Observable observe, Object obj){
		setEnabled(!paramList.isEmpty());
	}	
}
