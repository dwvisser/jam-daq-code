package jam.commands;

import jam.data.control.DataControl; 
import jam.global.CommandListenerException;


/**
 * Commands that are for showing DataControl dialogs simply extend this
 * and assign a reference to <code>dataControl</control> in their constructor.
 * 
 * @author Ken Swartz
 */
public class AbstractShowDataControlCmd
	extends AbstractCommand {

	protected DataControl dataControl;
	
	protected void execute(Object[] cmdParams) {
		dataControl.show();
	}

	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		execute(null);
	}
}
