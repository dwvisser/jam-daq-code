package jam.commands;

import jam.io.ImpExpASCII;

/**
 * Export the summary table 
 * 
 * @author Kennneth Swartz
 *
 */
public class ExportSummaryTableCmd extends AbstractExportFile {

	public void initCommand(){
		putValue(NAME,"Summary Table");
		//importExport=new ImpExpASCII();		
	}

}
