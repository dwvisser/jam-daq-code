package jam.commands;

import jam.data.control.Projections;
/**
 * Command to show project histgram dialog
 * 
 * @author Ken Swartz
 */
public class ShowDialogHistProjectionCmd extends AbstractShowDataControlCmd {
	
	protected void initCommand(){
		putValue(NAME,"Projections\u2026");
		dataControl=new Projections(msghdlr);
	}

}
