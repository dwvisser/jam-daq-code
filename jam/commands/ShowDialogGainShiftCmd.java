package jam.commands;

import jam.data.control.GainShift;

/**
 * Show the gain shift dialog
 * @author Ken
 */
public class ShowDialogGainShiftCmd extends AbstractShowDataControlCmd {

	protected void initCommand(){
		putValue(NAME,"Gain Shift\u2026");
		dataControl=new GainShift(msghdlr);
	}

}
