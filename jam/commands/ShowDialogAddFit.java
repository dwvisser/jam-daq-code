package jam.commands;

import jam.global.CommandListenerException;

import jam.fit.LoadFit;

/**
 * Command to add fit.
 * 
 * @author Ken Swartz
 */
public class ShowDialogAddFit extends AbstractCommand {
	
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
