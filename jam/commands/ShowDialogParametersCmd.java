package jam.commands;

import javax.swing.JFrame;
import jam.data.control.ParameterControl;
/**
 * Show parameters dialog
 * 
 * @author Ken Swartz
 *
 */
final class ShowDialogParametersCmd extends AbstractCommand {

	/** 
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		final JFrame frame =status.getFrame();
		final ParameterControl paramControl = new ParameterControl(frame, msghdlr);
		paramControl.show();		
	}

	/* *
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) {
		execute(null);
	}

}
