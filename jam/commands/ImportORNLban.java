package jam.commands;

import jam.io.ImportBanGates;

/**
 * Import a DAMM banana gate file.
 * 
 * @author Dale Visser
 */
final class ImportORNLban extends AbstractImportFile {
		
	public void initCommand(){
		putValue(NAME,"ORNL Banana Gates");
		importExport=new ImportBanGates();		
	}
}
