package jam.commands;

import com.google.inject.Inject;

import jam.global.Broadcaster;
import jam.io.ImpExpSPE;

/**
 * Import a gf3 spectrum file.
 * 
 * @author Dale Visser
 */

final class ImportRadware extends AbstractImportFile {

	@Inject
	ImportRadware(final ImpExpSPE impExpSPE, final Broadcaster broadcaster) {
		super("Radware gf3", broadcaster);
		importExport = impExpSPE;
	}

}
