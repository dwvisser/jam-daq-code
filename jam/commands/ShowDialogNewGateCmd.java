package jam.commands;

import jam.data.control.GateNew;
/**
 * Show the new gate dialog
 */
public class ShowDialogNewGateCmd extends AbstractShowDataControlCmd {


	protected void initCommand(){
		putValue(NAME, "New\u2026");
		//Super class member
		dataControl= new GateNew(msghdlr);		
	}
	
}
