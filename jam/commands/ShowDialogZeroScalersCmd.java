package jam.commands;

import javax.swing.JFrame;
import jam.data.control.ScalerControl;

/**
 * Show the zero scalers dialog
 * 
 * @author Ken Swartz
 *
 */
final class ShowDialogZeroScalersCmd extends AbstractCommand {

	/* *
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		final JFrame frame =status.getFrame();
		final ScalerControl scalerControl = new ScalerControl(frame, msghdlr);		
		scalerControl.showZero();	
	}

	/* *
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) {
		execute(null);
	}
}
