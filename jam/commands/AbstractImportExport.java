package jam.commands;

import jam.io.ImpExp;

/**
 * Export data to file.
 * 
 * @author Ken Swartz
 */
abstract class AbstractImportExport extends AbstractCommand {

	/**
	 * Must be assigned a real value by full implementations.
	 */
	protected ImpExp importExport;

	AbstractImportExport() {
		super();
	}
}
