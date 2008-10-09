package jam.global;

/**
 * Interface for establishing validity of data objects.
 * 
 * @author Dale Visser
 * 
 */
public interface Validator {

	/**
	 * @param candidate
	 *            to validate
	 * @return whether an object is valid, i.e., is actually associated with a
	 *         current Jam database.
	 */
	boolean isValid(Nameable candidate);
}
