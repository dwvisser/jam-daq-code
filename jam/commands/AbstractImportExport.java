package jam.commands;

import jam.io.AbstractImpExp;

/**
 * Export data to file.
 * 
 * @author Ken Swartz
 */
abstract class AbstractImportExport extends AbstractCommand {//NOPMD

	/**
	 * Must be assigned a real value by full implementations.
	 */
	protected AbstractImpExp importExport;

	AbstractImportExport() {
		super();
	}
	
	/**
	 * 
	 * @see AbstractCommand#AbstractCommand(String)
	 */
	protected AbstractImportExport(String name) {
		super(name);
	}
}
