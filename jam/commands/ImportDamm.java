package jam.commands;

import jam.io.ImpExpORNL;

import com.google.inject.Inject;

/**
 * Export data to file.
 * 
 * @author Dale Visser
 */
final class ImportDamm extends AbstractImportFile {

	@Inject
	ImportDamm(final ImpExpORNL impExpORNL) {
		super("Oak Ridge DAMM");
		importExport = impExpORNL;
	}
}
