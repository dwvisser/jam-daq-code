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
	ShowDialogNewHistogramCmd(){
		super("New\u2026");
		dialog= new HistogramNew();
	}
}
