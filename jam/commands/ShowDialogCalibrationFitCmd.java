package jam.commands;

import jam.data.control.CalibrationFit;
/**
 * @author Ken Swartz
 * 
 * Show histgrom Calibartion fit dialgo
 */
public class ShowDialogCalibrationFitCmd extends AbstractShowDialog {

	protected void initCommand(){
		putValue(NAME,"Enter Coefficients\u2026");
		dialog=new CalibrationFit(msghdlr);
	}

}
