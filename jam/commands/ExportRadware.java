package jam.commands;

import jam.global.MessageHandler;
import jam.io.ImpExpSPE;


/**
 * Export data to file
 * 
 * @author Dale Visser
 */
final class ExportRadware extends AbstractExportFile {
	
	ExportRadware(){
		super();
		putValue(NAME,"Radware gf3");
	}
	
	public void init(MessageHandler mh){
		super.init(mh);
		exporter=new ImpExpSPE(status.getFrame(),msghdlr);		
	}
}
