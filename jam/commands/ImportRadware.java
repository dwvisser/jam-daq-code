package jam.commands;

import jam.io.ImpExpSPE;


/**
 * Export data to file
 * 
 * @author Dale Visser
 */
final class ImportRadware extends AbstractImportFile{
		
	protected void initCommand(){
		putValue(NAME,"Radware gf3");
		importExport=new ImpExpSPE(status.getFrame(),msghdlr);		
	}
	
}
