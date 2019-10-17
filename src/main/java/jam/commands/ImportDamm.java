package jam.commands;

import com.google.inject.Inject;
import jam.global.Broadcaster;
import jam.io.ImpExpORNL;

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
