package jam.commands;

import jam.data.control.ScalerZero;

/**
 * Show the zero scalers dialog.
 * 
 * @author Ken Swartz
 *
 */
final class ShowDialogZeroScalersCmd extends AbstractShowDataControlCmd {
	
	ShowDialogZeroScalersCmd(){
		putValue(NAME,"Zero Scalers\u2026");
		dataControl=new ScalerZero();
	}
}
