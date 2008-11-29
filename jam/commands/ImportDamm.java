package jam.commands;

import jam.global.Broadcaster;
import jam.io.ImpExpORNL;

import com.google.inject.Inject;

/**
 * Export data to file.
 * 
 * @author Dale Visser
 */
final class ImportDamm extends AbstractImportFile {

	@Inject
	ImportDamm(final ImpExpORNL impExpORNL, final Broadcaster broadcaster) {
		super("Oak Ridge DAMM", broadcaster);
		importExport = impExpORNL;
	}
}
