package jam.commands;
import jam.data.control.HistogramControl;

/**
 * Show the histogram dialog
 * 
 * @author Ken
 *
 */
final class ShowDialogNewHistogramCmd extends AbstractCommand {

	ShowDialogNewHistogramCmd(){
		super();
	}
	
	/**
	 * Execute the command
	 */
	protected void execute(Object [] cmdParams){
		HistogramControl histogramControl= new HistogramControl(status.getFrame(), msghdlr);
		histogramControl.showNew();		
	}
	
	/**
	 * Execute the command
	 */
	protected void executeParse(String [] cmdParams){
		execute(null);
	}
}
