package jam.commands;

import jam.global.BroadcastEvent;
import jam.io.ImpExpSPE;
import jam.data.Histogram;
import java.util.Observable;
import java.util.Observer;


/**
 * Export data to a Radware gf3 spectrum file.
 * 
 * @author Dale Visser
 */
final class ExportRadware extends AbstractExportFile implements Observer{
		
	public void initCommand(){
		putValue(NAME,"Radware gf3");
		importExport=new ImpExpSPE();		
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
