/*
 * Created on Dec 30, 2003
 */
package jam;

/**
 * Constants representing which version of Jam this is.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public interface Version {
	/**
	 * Possible pre-release candidate version types.
	 */
	char beta='\u03b2';
	char alpha='\u03b1';
	
	/**
	 * The main version label.
	 */
	String JAM_VERSION = "1.4";
	
	/**
	 * Should be empty string if no text for version type is
	 * desired.
	 */
	String VERSION_TYPE = "Release Candidate 4";
}
