package jam.commands;

import jam.global.MessageHandler;
import jam.io.ImpExpORNL;

/**
 * Export data to file.
 * 
 * @author Dale Visser
 */
final class ExportDamm extends AbstractExportFile {
	
	ExportDamm(){
		super();
		putValue(NAME,"Oak Ridge DAMM");
	}
	
	public void init(MessageHandler mh){
		super.init(mh);
		importExport=new ImpExpORNL(status.getFrame(),msghdlr);		
	}
}
