package jam.commands;

import jam.data.control.HistogramZero;

/**
 * Show the zero histograms dialog
 *  
 * @author Ken Swartz
 *
 */
public class ShowDialogZeroHistogram extends AbstractShowDataControlCmd {

	/**
	 * Initialize command
	 */
	protected void initCommand(){
		putValue(NAME, "Zero\u2026");
		dataControl= new HistogramZero(msghdlr);
	}

}
 