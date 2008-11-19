package jam.commands;

import jam.io.ImportBanGates;

import com.google.inject.Inject;

/**
 * Import a DAMM banana gate file.
 * 
 * @author Dale Visser
 */
final class ImportORNLban extends AbstractImportFile {

	@Inject
	ImportORNLban(final ImportBanGates importBanGates) {
		super("ORNL Banana Gates");
		importExport = importBanGates;
	}
}
