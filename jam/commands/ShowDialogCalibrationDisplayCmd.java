package jam.commands;

import jam.data.control.CalibrationDisplay;;
/**
 * @author Ken Swartz
 *
 */
public class ShowDialogCalibrationDisplayCmd
	extends AbstractShowDataControlCmd {

		protected void initCommand(){
			putValue(NAME,"Fit\u2026");			
			dataControl=new CalibrationDisplay(msghdlr);
		}

}
