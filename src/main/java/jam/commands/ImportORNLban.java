package jam.commands;

import com.google.inject.Inject;

import jam.global.Broadcaster;
import jam.io.ImportBanGates;

/**
 * Import a DAMM banana gate file.
 * 
 * @author Dale Visser
 */
@SuppressWarnings("serial")
final class ImportORNLban extends AbstractImportFile {

	@Inject
	ImportORNLban(final ImportBanGates importBanGates,
			final Broadcaster broadcaster) {
		super("ORNL Banana Gates", broadcaster);
		importExport = importBanGates;
	}
}
