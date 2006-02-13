package jam.commands;

import jam.io.ImpExpXSYS;

/**
 * Export data to an XSYS file.
 * 
 * @author Dale Visser
 */
final class ImportXSYS extends AbstractImportFile {
	
	ImportXSYS(){
		super("TUNL's XSYS");
		importExport=new ImpExpXSYS();		
	}
}
