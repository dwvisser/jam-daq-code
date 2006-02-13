package jam.commands;

import jam.fit.LoadFit;
import jam.global.CommandListenerException;

/**
 * Command to add fit.
 * 
 * @author Ken Swartz
 */
final class ShowDialogAddFit extends AbstractCommand {
	

	ShowDialogAddFit(){
		super("Load Fit\u2026");
	}
	
	protected void execute(final Object[] cmdParams) {				
		final LoadFit loadfit=new LoadFit();
		loadfit.showLoad();
	}

	protected void executeParse(final String[] cmdTokens)
		throws CommandListenerException {
		execute(null);
	}
}
