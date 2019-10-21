package jam.commands;

import com.google.inject.Inject;

import jam.io.ImpExpORNL;

/**
 * Export data to DAMM histogram file.
 * 
 * @author Dale Visser
 */
@SuppressWarnings("serial")
final class ExportDamm extends AbstractExportFile {

	@Inject
	ExportDamm(final ImpExpORNL impExpORNL) {
		super("Oak Ridge DAMM");
		importExport = impExpORNL;
	}
}
