package jam.commands;

import jam.data.control.Manipulations;

/**
 *  Command to show project histgram dialog
 * 
 * @author Ken Swartz
 */
public class ShowDialogHistManipulationsCmd
	extends AbstractShowDataControlCmd {

		protected void initCommand(){
			putValue(NAME,"Combine\u2026");
			dataControl=new Manipulations(msghdlr);
		}

}
