package jam.commands;

/**
 * Exception thrown when an error occurs in the <code>jam.commands</code>
 * package.
 * 
 * @author Ken Swartz
 */
@SuppressWarnings("serial")
public class CommandException extends Exception {

	/**
	 * @see Exception#Exception(java.lang.Throwable)
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
	 */
	public CommandException(final Throwable cause) {
		super(cause);
	}
}