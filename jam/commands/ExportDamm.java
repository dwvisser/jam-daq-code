package jam.commands;

import jam.io.ImpExpORNL;

/**
 * Export data to file.
 * 
 * @author Dale Visser
 */
final class ExportDamm extends AbstractExportFile {
		
	protected void initCommand(){
		putValue(NAME,"Oak Ridge DAMM");
		importExport=new ImpExpORNL(status.getFrame(),msghdlr);		
	}
}
