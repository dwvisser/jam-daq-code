package jam.global;

/**
 * "Null" object for Nameable interface.
 * 
 * @author Dale Visser
 * 
 */
public final class UnNamed implements Nameable {

	private static final UnNamed INSTANCE = new UnNamed();

	private UnNamed() {
		super();
	}

	public String getName() {
		return "Unnamed Item.";
	}

	/**
	 * @return the singleton instance
	 */
	static public UnNamed getSingletonInstance() {
		return INSTANCE;
	}

}
