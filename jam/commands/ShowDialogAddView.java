package jam.commands;

import jam.ViewNew;
import jam.global.CommandListenerException;

/**
 * Command to add view.
 * 
 * @author Ken Swartz
 */
public class ShowDialogAddView extends AbstractCommand {

	
	private ViewNew viewNew;
	
	public void initCommand(){
		putValue(NAME, "New\u2026");
		viewNew = new ViewNew();		
	}
	
	protected void execute(Object[] cmdParams) throws CommandException {
		viewNew.showView();

	}

	protected void executeParse(String[] cmdTokens)
			throws CommandListenerException {
			//NOP
	}

}
