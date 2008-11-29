package jam.commands;

import jam.global.Broadcaster;
import jam.io.ImpExpASCII;

import com.google.inject.Inject;

/**
 * Export data to an ASCII text file.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
final class ImportTextFile extends AbstractImportFile {

	@Inject
	ImportTextFile(final ImpExpASCII impExpASCII, final Broadcaster broadcaster) {
		super("Text File", broadcaster);
		importExport = impExpASCII;
	}
}
