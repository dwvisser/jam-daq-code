package jam.commands;

import jam.data.control.GainShift;

/**
 * Show the gain shift dialog
 * @author Ken
 */
public class ShowDialogGainShiftCmd extends AbstractShowDialog {

	public void initCommand(){
		putValue(NAME,"Gain Shift\u2026");
		dialog=new GainShift(msghdlr);
	}
}
