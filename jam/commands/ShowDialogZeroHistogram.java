package jam.commands;

import jam.data.Histogram;
import jam.data.control.HistogramZero;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Show the zero histograms dialog
 *  
 * @author Ken Swartz
 *
 */
final class ShowDialogZeroHistogram extends AbstractShowDialog 
implements Observer {

	private final List histogramList=Histogram.getHistogramList();

	/**
	 * Initialize command
	 */
	public void initCommand(){
		putValue(NAME, "Zero\u2026");
		dialog= new HistogramZero(msghdlr);
	}

	public void update(Observable observe, Object obj){
		setEnabled(!histogramList.isEmpty());
	}	
}
 