package jam.commands;

import jam.io.ImpExp;

/**
 * Export data to file
 * @author Ken Swartz
 *
 */
abstract class AbstractImportExport extends AbstractCommand {

	protected ImpExp importExport;

	AbstractImportExport() {
		super();
	}
}
