package jam.commands;

import jam.io.ImpExpASCII;

/**
 * Export data to file.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
final class ExportTextFileCmd extends AbstractExportFile {
	
	protected void initCommand(){
		putValue(NAME,"Text File");
		importExport=new ImpExpASCII(status.getFrame(),msghdlr);		
	}
}
