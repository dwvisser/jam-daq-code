package jam;

/**
 * Factory methods for jam package.
 * 
 * @author Dale Visser
 * 
 */
public final class Factory {

	private Factory() {
		// meant to be static
	}

	/**
	 * Creates a help dialog.
	 * 
	 * @return help dialog
	 */
	public static Help createHelp() {
		return new Help(new LicenseReader());
	}
}
