package jam.commands;

import jam.global.CommandListenerException;
import jam.plot.ViewDelete;

/**
 * Command to delete view.
 * 
 * @author Ken Swartz
 */
public class ShowDialogDeleteView extends AbstractCommand {

	
	private ViewDelete viewDelete;
	
	public void initCommand(){
		putValue(NAME, "Delete\u2026");
		viewDelete = new ViewDelete();		
	}
	
	protected void execute(Object[] cmdParams) throws CommandException {
		viewDelete.showView();

	}

	protected void executeParse(String[] cmdTokens)
			throws CommandListenerException {
			//NOP
	}

}
