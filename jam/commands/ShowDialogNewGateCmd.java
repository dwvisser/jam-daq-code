package jam.commands;

import jam.data.control.GateControl;
/**
 * @author Ken
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ShowDialogNewGateCmd extends AbstractShowDataControlCmd {


	ShowDialogNewGateCmd() {
		putValue(NAME, "New\u2026");
		//Super class member
		dataControl= new GateControl(status.getFrame(), broadcaster, msghdlr);		
	}
	
}
