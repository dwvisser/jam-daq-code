package jam.commands;

import jam.data.control.CalibrationDisplay;;
/**
 * @author Ken Swartz
 *
 */
final class ShowDialogCalibrationDisplayCmd
	extends AbstractShowDialog {

		public void initCommand(){
			putValue(NAME,"Fit\u2026");			
			dialog=new CalibrationDisplay(msghdlr);
		}

}
