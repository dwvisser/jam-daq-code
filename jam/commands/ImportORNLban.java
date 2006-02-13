package jam.commands;

import jam.io.ImportBanGates;

/**
 * Import a DAMM banana gate file.
 * 
 * @author Dale Visser
 */
final class ImportORNLban extends AbstractImportFile {
		
	ImportORNLban(){
		super("ORNL Banana Gates");
		importExport=new ImportBanGates();		
	}
}
