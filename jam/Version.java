package jam;

/**
 * Constants representing which version of Jam this is.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 30 Dec 2003
 */
public final class Version {
	
	/* When building, And replaces the version fields with the right
	 * values. */
	
	/**
	 * Increments only for changes which impact backward and/or 
	 * forward compatibility in a big way. 
	 */
	public static final String MAJOR="@MAJOR@";
	
	/**
	 * Incremented whenever work starts on new features to be
	 * included in a future release. Normally, these go into 
	 * a new development branch, so that "subminor" level fixes,
	 * etc. may go into the current release without committing
	 * users to the new, untested stuff yet.
	 */
	public static final String MINOR="@MINOR@";
	
	/**
	 * Incremented every time a bugfix or patch is performed for
	 * release back to the users. 
	 */
	public static final String SUBMINOR="@SUBMINOR@";
	
	public static final String PLATFORM=System.getProperty("os.name");
		
	private static final StringBuffer NAME=new StringBuffer();	
	static {
		NAME.append(MAJOR).append('.').append(MINOR).append('.').append(
		SUBMINOR).append('-').append(PLATFORM);
	}
		
	/**
	 * @return a string representing the build version of Jam running
	 */
	static public String getName(){
		return NAME.toString();
	}
	

}
