package jam.commands;

import jam.data.control.Projections;
/**
 * Command to show project histgram dialog
 * 
 * @author Ken Swartz
 */
public class ShowDialogHistProjectionCmd extends AbstractShowDialog {
	
	protected void initCommand(){
		putValue(NAME,"Projections\u2026");
		dialog=new Projections(msghdlr);
	}

}