package jam.commands;

import com.google.inject.Inject;

import jam.global.Broadcaster;
import jam.io.ImpExpXSYS;

/**
 * Export data to an XSYS file.
 * 
 * @author Dale Visser
 */

final class ImportXSYS extends AbstractImportFile {

	@Inject
	ImportXSYS(final ImpExpXSYS impExpXSYS, final Broadcaster broadcaster) {
		super("TUNL's XSYS", broadcaster);
		importExport = impExpXSYS;
	}
}
