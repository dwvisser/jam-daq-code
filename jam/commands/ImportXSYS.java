package jam.commands;

import jam.io.ImpExpXSYS;

/**
 * Export data to file.
 * 
 * @author Dale Visser
 */
final class ImportXSYS extends AbstractImportFile {
	
	protected void initCommand(){
		putValue(NAME,"TUNL's XSYS");
		importExport=new ImpExpXSYS(status.getFrame(),msghdlr);		
	}
}
