package jam.commands;

import jam.global.BroadcastEvent;
import jam.io.ImpExpSPE;
import jam.data.Histogram;
import java.util.Observable;
import java.util.Observer;


/**
 * Export data to file
 * 
 * @author Dale Visser
 */
final class ExportRadware extends AbstractExportFile implements Observer{
		
	protected void initCommand(){
		putValue(NAME,"Radware gf3");
		importExport=new ImpExpSPE(status.getFrame(),msghdlr);		
	}
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final int command=be.getCommand();
		if (command==BroadcastEvent.HISTOGRAM_SELECT){
			enable();
		}
	}
	protected void enable(){
		final Histogram h=Histogram.getHistogram(
		status.getCurrentHistogramName());
		if (h!=null)
			setEnabled(h.getDimensionality()==1);	
	}
}
