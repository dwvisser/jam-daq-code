/*
 * Created on Dec 30, 2003
 */
package jam;

/**
 * Constants representing which version of Jam this is.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public final class Version {
	/**
	 * Possible pre-release candidate version types.
	 */
	static final char beta='\u03b2';
	static final char alpha='\u03b1';
	
	/**
	 * The main version label.
	 */
	static final String JAM_VERSION = "1.4";
	
	/**
	 * Should be empty string if no text for version type is
	 * desired.
	 */
	static final String VERSION_TYPE = "Release Candidate 7";
	
	/**
	 * @return a string representing the build version of Jam running
	 */
	static public String getName(){
		final StringBuffer rval=new StringBuffer(Version.JAM_VERSION);
		if (Version.VERSION_TYPE.length()>0){
			final String leftparen=" (";
			rval.append(leftparen);
			rval.append(Version.VERSION_TYPE);
			rval.append(')');
		}
		return rval.toString();
	}

}
