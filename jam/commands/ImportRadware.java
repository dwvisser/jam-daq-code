package jam.commands;

import jam.global.MessageHandler;
import jam.io.ImpExpSPE;


/**
 * Export data to file
 * 
 * @author Dale Visser
 */
final class ImportRadware extends AbstractImportFile{
	
	ImportRadware(){
		super();
		putValue(NAME,"Radware gf3");
	}
	
	public void init(MessageHandler mh){
		super.init(mh);
		importExport=new ImpExpSPE(status.getFrame(),msghdlr);		
	}
	
}
