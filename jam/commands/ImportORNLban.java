package jam.commands;

import jam.global.MessageHandler;
import jam.io.ImportBanGates;

/**
 * Export data to file.
 * 
 * @author Dale Visser
 */
final class ImportORNLban extends AbstractImportFile {
	
	ImportORNLban(){
		super();
		putValue(NAME,"ORNL Banana Gates");
	}
	
	public void init(MessageHandler mh){
		super.init(mh);
		importExport=new ImportBanGates(status.getFrame(),msghdlr);		
	}
}
