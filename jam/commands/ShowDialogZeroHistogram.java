package jam.commands;

import jam.data.control.HistogramZeroControl;

/**
 * Show the zero histograms dialog
 *  
 * @author Ken Swartz
 *
 */
public class ShowDialogZeroHistogram extends AbstractShowDataControlCmd {

	ShowDialogZeroHistogram() {
		putValue(NAME, "Zero\u2026");
		//Super class member
		dataControl= new HistogramZeroControl(status.getFrame(), msghdlr);		
	}
	

}
 