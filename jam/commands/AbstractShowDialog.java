package jam.commands;

import jam.global.CommandListenerException;

import javax.swing.JDialog;


/**
 * Commands that are for showing DataControl dialogs simply extend this
 * and assign a reference to <code>dataControl</control> in their constructor.
 * 
 * @author Ken Swartz
 */
public class AbstractShowDialog
	extends AbstractCommand {

	protected JDialog dialog;
	
	protected void execute(Object[] cmdParams) {
		dialog.show();
	}

	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		execute(null);
	}
	
	public final void setEnabled(boolean state){
		super.setEnabled(state);
		if (!state){
			dialog.dispose();
		}
	}
}
