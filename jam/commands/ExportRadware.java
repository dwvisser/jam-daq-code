package jam.commands;

import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.io.ImpExpSPE;

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
		final BroadcastEvent.Command command=be.getCommand();
		if (command==BroadcastEvent.Command.HISTOGRAM_SELECT){
			enable();
		}
	}
	protected void enable(){
		final Histogram h=status.getCurrentHistogram();
		if (h!=null){
			setEnabled(h.getDimensionality()==1);	
		}
	}
}
