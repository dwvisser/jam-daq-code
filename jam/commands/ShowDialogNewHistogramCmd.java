package jam.commands;
import jam.data.control.HistogramNew;

/**
 * Show the histogram dialog
 * 
 * @author Ken
 *
 */
final class ShowDialogNewHistogramCmd extends AbstractCommand {
	
	final private HistogramNew histogramControl;

	ShowDialogNewHistogramCmd(){
		super();
		putValue(NAME, "New\u2026");
		histogramControl= new HistogramNew(msghdlr);
	}
	
	/**
	 * Execute the command
	 */
	protected void execute(Object [] cmdParams){
		histogramControl.show();		
	}
	
	/**
	 * Execute the command
	 */
	protected void executeParse(String [] cmdParams){
		execute(null);
	}
}
