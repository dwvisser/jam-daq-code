package jam.commands;

import jam.io.ImportBanGates;

/**
 * Export data to file.
 * 
 * @author Dale Visser
 */
final class ImportORNLban extends AbstractImportFile {
		
	protected void initCommand(){
		putValue(NAME,"ORNL Banana Gates");
		importExport=new ImportBanGates(status.getFrame(),msghdlr);		
	}
}
