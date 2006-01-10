package jam.global;

public final class UnNamed implements Nameable {
	
	private static final UnNamed INSTANCE = new UnNamed();
	
	private UnNamed(){
		super();
	}
	
	public String getName(){
		return "Unnamed Item.";
	}
	
	static public UnNamed getSingletonInstance(){
		return INSTANCE;
	}

}
