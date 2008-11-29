package jam.commands;

import jam.global.Broadcaster;
import jam.io.ImportBanGates;

import com.google.inject.Inject;

/**
 * Import a DAMM banana gate file.
 * 
 * @author Dale Visser
 */
final class ImportORNLban extends AbstractImportFile {

	@Inject
	ImportORNLban(final ImportBanGates importBanGates,
			final Broadcaster broadcaster) {
		super("ORNL Banana Gates", broadcaster);
		importExport = importBanGates;
	}
}
