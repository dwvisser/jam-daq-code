package jam.commands;

import jam.global.MessageHandler;
import jam.io.ImpExpASCII;

/**
 * Export data to file.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
final class ImportTextFile extends AbstractImportFile {
	
	ImportTextFile(){
		super();
		putValue(NAME,"Text File");
	}
	
	public void init(MessageHandler mh){
		super.init(mh);
		importExport=new ImpExpASCII(status.getFrame(),msghdlr);		
	}
}
