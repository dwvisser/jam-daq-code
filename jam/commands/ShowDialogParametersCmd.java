package jam.commands;

import javax.swing.JFrame;
import jam.data.control.ParameterControl;

/**
 * Show parameters dialog.
 * 
 * @author Ken Swartz
 *
 */
final class ShowDialogParametersCmd extends AbstractShowDataControlCmd {

	/**
	 * Initialize command
	 */
	protected void initCommand(){
		putValue(NAME, "Parameters\u2026");
		final JFrame frame =status.getFrame();
		dataControl = new ParameterControl(frame, msghdlr);
		
	}	
}
