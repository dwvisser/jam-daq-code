package jam.commands;

import jam.fit.LoadFit;
import jam.global.CommandListenerException;

/**
 * Command to add fit.
 * 
 * @author Ken Swartz
 */
final class ShowDialogAddFit extends AbstractCommand {
	
	private LoadFit loadfit;

	public void initCommand(){
		putValue(NAME, "Load Fit\u2026");
		loadfit = new LoadFit();		
	}
	
	protected void execute(Object[] cmdParams) {				
		loadfit.showLoad();
	}

	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		execute(null);
	}
}
