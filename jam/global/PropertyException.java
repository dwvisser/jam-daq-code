/*
 */
package jam.global;
/**
 * Exceptions thrown in the <code>jam.util</code> package are converted to this.
 *
 * @version 19 May 1999
 * @author Dale Visser
 */
public class PropertyException extends Exception {

	public PropertyException(String errorMessage) {
		super(errorMessage);
	}
}
