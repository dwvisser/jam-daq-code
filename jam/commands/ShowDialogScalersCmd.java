package jam.commands;

import jam.data.control.ScalerControl;

/**
 * Show the scalers dialog box
 * 
 * @author Ken Swartz
 *
 */
final class ShowDialogScalersCmd extends AbstractShowDataControlCmd {
	
	ShowDialogScalersCmd(){
		putValue(NAME,"Display Scalers\u2026");
		dataControl=new ScalerControl(msghdlr);
	}
}
