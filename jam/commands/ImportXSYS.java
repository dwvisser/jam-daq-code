package jam.commands;

import jam.global.MessageHandler;
import jam.io.ImpExpXSYS;

/**
 * Export data to file.
 * 
 * @author Dale Visser
 */
final class ImportXSYS extends AbstractImportFile {
	
	ImportXSYS(){
		super();
		putValue(NAME,"TUNL's XSYS");
	}
	
	public void init(MessageHandler mh){
		super.init(mh);
		importExport=new ImpExpXSYS(status.getFrame(),msghdlr);		
	}
}
