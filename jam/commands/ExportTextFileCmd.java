package jam.commands;

import jam.io.ImpExpASCII;

import com.google.inject.Inject;

/**
 * Export data to an ASCII text file.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
final class ExportTextFileCmd extends AbstractExportFile {

	@Inject
	ExportTextFileCmd(final ImpExpASCII impExpASCII) {
		super("Text File");
		importExport = impExpASCII;
	}
}
