package jam.commands;

import jam.global.CommandListenerException;
import jam.global.JamStatus;

import jam.fit.LoadFit;
import javax.swing.JFrame;
import javax.swing.JMenu;
/**
 * Command to add fit
 * @author Ken Swartz
 *
 */
public class ShowDialogAddFit extends AbstractCommand {
	
	private JMenu fittingMenu;

	protected void initCommand(){
		putValue(NAME, "Load Fit\u2026");
		JFrame frame = status.getFrame();
		//JMenu fittingMenu = frame.getMenuBar().getFitMenu();		
		//dialog = new AboutDialog(JamStatus.instance().getFrame());		
	}
	
	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {		

		final LoadFit loadfit;		
		loadfit = new LoadFit(msghdlr, fittingMenu);		
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
