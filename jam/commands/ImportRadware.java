package jam.commands;

import jam.io.ImpExpSPE;

import com.google.inject.Inject;

/**
 * Import a gf3 spectrum file.
 * 
 * @author Dale Visser
 */
final class ImportRadware extends AbstractImportFile {

	@Inject
	ImportRadware(final ImpExpSPE impExpSPE) {
		super("Radware gf3");
		importExport = impExpSPE;
	}

}
