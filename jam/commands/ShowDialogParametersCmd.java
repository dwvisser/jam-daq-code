package jam.commands;

import javax.swing.JFrame;
import jam.data.control.ParameterControl;
/**
 * Show parameters dialog
 * 
 * @author Ken Swartz
 *
 */
public class ShowDialogParametersCmd extends AbstractCommand implements Commandable {

	/** 
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	public void execute(Object[] cmdParams) {
		
		JFrame frame =status.getFrame();
		
		final ParameterControl paramControl = new ParameterControl(frame, broadcaster, msghdlr);
		paramControl.show();		
	}

	/* *
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	public void executeParse(String[] cmdTokens) {
		execute(null);

	}

}
