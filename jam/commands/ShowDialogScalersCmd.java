package jam.commands;

import javax.swing.JFrame;
import jam.data.control.ScalerControl;

/**
 * Show the scalers dialog box
 * 
 * @author Ken Swartz
 *
 */
public class ShowDialogScalersCmd
	extends AbstractCommand
	implements Commandable {

	/* *
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	public void execute(Object[] cmdParams) {
		JFrame frame =status.getFrame();
		
		final ScalerControl scalerControl = new ScalerControl(frame, broadcaster, msghdlr);		
		scalerControl.showDisplay();	

	}

	/* *
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	public void executeParse(String[] cmdTokens) {
		execute(null);

	}

}
