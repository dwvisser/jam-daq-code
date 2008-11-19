package jam.commands;

import jam.io.ImpExpXSYS;

import com.google.inject.Inject;

/**
 * Export data to an XSYS file.
 * 
 * @author Dale Visser
 */
final class ImportXSYS extends AbstractImportFile {

	@Inject
	ImportXSYS(final ImpExpXSYS impExpXSYS) {
		super("TUNL's XSYS");
		importExport = impExpXSYS;
	}
}
