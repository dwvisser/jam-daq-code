package jam.commands;

import jam.data.control.CalibrationDisplay;;
/**
 * @author Ken Swartz
 *
 */
public class ShowDialogCalibrationDisplayCmd
	extends AbstractShowDialog {

		protected void initCommand(){
			putValue(NAME,"Fit\u2026");			
			dialog=new CalibrationDisplay(msghdlr);
		}

}
