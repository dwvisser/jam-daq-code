package jam.global;

public interface Validator {
	
	/**
	 * Gives whether an object is valid, i.e., is actually associated
	 * with a current Jam database.
	 * 
	 * @param object to validate
	 */
	boolean isValid(Object object);

}
