package jam.commands;

import jam.io.ImpExpORNL;

/**
 * Export data to file.
 * 
 * @author Dale Visser
 */
final class ImportDamm extends AbstractImportFile {
	
	public void initCommand(){
		putValue(NAME,"Oak Ridge DAMM");		
		importExport=new ImpExpORNL();		
	}
}
