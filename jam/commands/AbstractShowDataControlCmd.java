package jam.commands;

import jam.data.control.DataControl; 
import jam.global.CommandListenerException;


/**
 * @author Ken
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AbstractShowDataControlCmd
	extends AbstractCommand
	implements Commandable {

	protected DataControl dataControl;
	
	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		dataControl.show();

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		execute(null);

	}

}
