package jam.commands;

import jam.io.ImpExpORNL;

/**
 * Export data to DAMM histogram file.
 * 
 * @author Dale Visser
 */
final class ExportDamm extends AbstractExportFile {
	
	ExportDamm() {
		super("Oak Ridge DAMM");
		importExport=new ImpExpORNL();		
	}
}
