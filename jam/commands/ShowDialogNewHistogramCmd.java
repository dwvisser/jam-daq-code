package jam.commands;
import jam.data.control.*;
import java.awt.Frame;
//import jam.global.Broadcaster;
//import jam.global.MessageHandler;
//import jam.global.JamStatus;
/**
 * Show the histogram dialog
 * 
 * @author Ken
 *
 */
public class ShowDialogNewHistogramCmd extends AbstractCommand{

	//Null constructor needed
	public ShowDialogNewHistogramCmd(){
		
	}
	
	/**
	 * Execute the command
	 */
	public void execute(Object [] cmdParams){
		
		HistogramControl histogramControl= new HistogramControl((Frame)status.getFrame(), broadcaster, msghdlr);
		histogramControl.showNew();		
	}
	
	/**
	 * Execute the command
	 */
	public void executeParse(String [] cmdParams){
		execute(null);
	}
	
}
