package jam.commands;

import jam.io.ImpExpASCII;

/**
 * Export data to file.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
final class ImportTextFile extends AbstractImportFile {
		
	protected void initCommand(){
		putValue(NAME,"Text File");		
		importExport=new ImpExpASCII(status.getFrame(),msghdlr);		
	}
}
