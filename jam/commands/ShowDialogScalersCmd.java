package jam.commands;

import javax.swing.JFrame;
import jam.data.control.ScalerControl;

/**
 * Show the scalers dialog box
 * 
 * @author Ken Swartz
 *
 */
final class ShowDialogScalersCmd extends AbstractCommand {

	/* *
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		final JFrame frame =status.getFrame();
		final ScalerControl scalerControl = new ScalerControl(frame, msghdlr);		
		scalerControl.showDisplay();	
	}

	/* *
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) {
		execute(null);
	}
}
