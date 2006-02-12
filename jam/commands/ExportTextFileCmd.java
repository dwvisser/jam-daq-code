package jam.commands;

import jam.io.ImpExpASCII;

/**
 * Export data to an ASCII text file.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
final class ExportTextFileCmd extends AbstractExportFile {
	
	ExportTextFileCmd(){
		super("Text File");
		importExport=new ImpExpASCII();		
	}
}
