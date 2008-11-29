package jam.commands;

import jam.global.Broadcaster;
import jam.io.ImpExpSPE;

import com.google.inject.Inject;

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
