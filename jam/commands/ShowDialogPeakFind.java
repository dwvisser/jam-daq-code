package jam.commands;

import jam.PeakFindDialog;


/**
 * Command to add fit
 * @author Ken Swartz
 *
 */
public class ShowDialogPeakFind extends AbstractShowDialog {
	
	public void initCommand(){
		putValue(NAME, "Peak Find Properties\u2026");
		dialog = new PeakFindDialog();		
	}
	
}
