package jam.commands;

import com.google.inject.Inject;
import jam.global.Broadcaster;
import jam.io.ImpExpASCII;

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
