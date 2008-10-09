package jam.data.func;

/**
 * Special exception for issues during calibration fits.
 * 
 * @author Dale Visser
 * 
 */
public class CalibrationFitException extends Exception {

	/**
	 * @param arg0
	 *            message
	 */
	public CalibrationFitException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 *            wrapped exception
	 */
	public CalibrationFitException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 *            message
	 * @param arg1
	 *            wrapped exception
	 */
	public CalibrationFitException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
