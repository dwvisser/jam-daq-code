package jam.commands;

import jam.io.ImpExpSPE;


/**
 * Import a gf3 spectrum file.
 * 
 * @author Dale Visser
 */
final class ImportRadware extends AbstractImportFile{
		
	public void initCommand(){
		putValue(NAME,"Radware gf3");
		importExport=new ImpExpSPE();		
	}
	
}
