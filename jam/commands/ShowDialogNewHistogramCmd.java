package jam.commands;
import jam.data.control.HistogramControl;

/**
 * Show the histogram dialog
 * 
 * @author Ken
 *
 */
final class ShowDialogNewHistogramCmd extends AbstractCommand {
	
	final private HistogramControl histogramControl;

	ShowDialogNewHistogramCmd(){
		super();
		putValue(NAME, "New\u2026");
		histogramControl= new HistogramControl(status.getFrame(), msghdlr);
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
