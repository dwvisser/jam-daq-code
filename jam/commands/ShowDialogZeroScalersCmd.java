package jam.commands;

import javax.swing.JFrame;
import jam.data.control.ScalerControl;

/**
 * Show the zero scalers dialog
 * 
 * @author Ken Swartz
 *
 */
public class ShowDialogZeroScalersCmd
	extends AbstractCommand
	implements Commandable {

	/* *
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	public void execute(Object[] cmdParams) {
		JFrame frame =status.getFrame();
		
		final ScalerControl scalerControl = new ScalerControl(frame, broadcaster, msghdlr);		
		scalerControl.showZero();	
		
	}

	/* *
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	public void executeParse(String[] cmdTokens) {
		execute(null);

	}

}
