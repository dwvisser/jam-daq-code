package jam.commands;
import jam.data.control.HistogramNew;

/**
 * Show the histogram dialog
 * 
 * @author Ken
 *
 */
final class ShowDialogNewHistogramCmd extends AbstractShowDataControlCmd {
	
	/**
	 * Initialize command
	 */
	protected void initCommand(){
		putValue(NAME, "New\u2026");
		dataControl= new HistogramNew(msghdlr);
	}
	
}
