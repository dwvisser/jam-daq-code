package jam.commands;

import jam.data.control.CalibrationFit;
/**
 * @author Ken Swartz
 * 
 * Show histgrom Calibartion fit dialgo
 */
public class ShowDialogCalibrationFitCmd extends AbstractShowDataControlCmd {

	protected void initCommand(){
		putValue(NAME,"Enter Coefficients\u2026");
		dataControl=new CalibrationFit(msghdlr);
	}

}
