package jam.commands;

import jam.io.ImpExpXSYS;

/**
 * Export data to an XSYS file.
 * 
 * @author Dale Visser
 */
final class ImportXSYS extends AbstractImportFile {
	
	public void initCommand(){
		putValue(NAME,"TUNL's XSYS");
		importExport=new ImpExpXSYS();		
	}
}
