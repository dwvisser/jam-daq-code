package jam.commands;

import jam.global.MessageHandler;
import jam.io.ImpExpASCII;

/**
 * Export data to file.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
final class ExportTextFileCmd extends AbstractExportFile {
	
	ExportTextFileCmd(){
		super();
		putValue(NAME,"Text File");
	}
	
	public void init(MessageHandler mh){
		super.init(mh);
		exporter=new ImpExpASCII(status.getFrame(),msghdlr);		
	}
}
