package jam.commands;

import jam.io.ImpExpORNL;

/**
 * Export data to DAMM histogram file.
 * 
 * @author Dale Visser
 */
final class ExportDamm extends AbstractExportFile {
		
	public void initCommand(){
		putValue(NAME,"Oak Ridge DAMM");
		importExport=new ImpExpORNL();		
	}
}
