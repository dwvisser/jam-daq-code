package jam.commands;

import jam.data.control.Projections;
/**
 * Command to show project histgram dialog
 * 
 * @author Ken Swartz
 */
final class ShowDialogHistProjectionCmd extends AbstractShowDialog {
	
	ShowDialogHistProjectionCmd(){
		super("Projections\u2026");
		dialog=new Projections();
	}

}
