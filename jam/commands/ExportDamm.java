package jam.commands;

import jam.io.ImpExpORNL;

import com.google.inject.Inject;

/**
 * Export data to DAMM histogram file.
 * 
 * @author Dale Visser
 */
final class ExportDamm extends AbstractExportFile {

	@Inject
	ExportDamm(final ImpExpORNL impExpORNL) {
		super("Oak Ridge DAMM");
		importExport = impExpORNL;
	}
}
