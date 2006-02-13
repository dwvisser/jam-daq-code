package jam.commands;

import jam.PeakFindDialog;


/**
 * Command to add fit
 * @author Ken Swartz
 *
 */
final class ShowDialogPeakFind extends AbstractShowDialog {
	
	ShowDialogPeakFind(){
		super("Peak Find Properties\u2026");
		dialog = new PeakFindDialog();		
	}
	
}
