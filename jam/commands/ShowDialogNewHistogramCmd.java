package jam.commands;
import jam.data.control.HistogramNew;

/**
 * Show the histogram dialog
 * 
 * @author Ken
 *
 */
final class ShowDialogNewHistogramCmd extends AbstractShowDialog {
	
	/**
	 * Initialize command
	 */
	public void initCommand(){
		putValue(NAME, "New\u2026");
		dialog= new HistogramNew(msghdlr);
	}
}
