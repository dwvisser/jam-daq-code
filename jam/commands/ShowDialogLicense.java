package jam.commands;

import jam.Help;

/**
 * 
 * Show the license dialog
 * @author Ken Swartz
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ShowDialogLicense extends AbstractShowDialog {

	protected void initCommand() {
		putValue(NAME, "License\u2026");
		dialog = new Help(status.getFrame(), status.getMessageHandler());
	}

}
