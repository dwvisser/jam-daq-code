package jam.commands;

import jam.data.control.ParameterControl;
import jam.data.DataParameter;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

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
		final JFrame frame =status.getFrame();
		dialog = new ParameterControl(frame, msghdlr);
	}
		
	public void update(Observable observe, Object obj){
		setEnabled(!paramList.isEmpty());
	}	
}
