package jam.commands;

import jam.global.CommandListenerException;

import jam.fit.LoadFit;
import javax.swing.JFrame;
/**
 * Command to add fit
 * @author Ken Swartz
 *
 */
public class ShowDialogAddFit extends AbstractCommand {
	
	private LoadFit loadfit;

	protected void initCommand(){
		putValue(NAME, "Load Fit\u2026");
		JFrame frame = status.getFrame();
		loadfit = new LoadFit(msghdlr);		
	}
	
	/* * 
	 *  Execute command
	 */
	protected void execute(Object[] cmdParams) {				
		loadfit.showLoad();
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		execute(null);

	}

}
