package jam.commands;

import jam.io.ImpExpASCII;

/**
 * Export data to an ASCII text file.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
final class ImportTextFile extends AbstractImportFile {
		
	public void initCommand(){
		putValue(NAME,"Text File");		
		importExport=new ImpExpASCII();		
	}
}
