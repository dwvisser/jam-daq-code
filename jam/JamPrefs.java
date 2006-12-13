/*
 * Created on Jun 10, 2004
 */
package jam;

import java.util.prefs.Preferences;

/**
 * Holds reference to the preferences node affecting the <code>jam</code>
 * package, as well as the preference names.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 10, 2004
 * @see java.util.prefs.Preferences
 */
public final class JamPrefs {
	
	JamPrefs(){
		super();
	}
    
    /**
     * The preferences node for the <code>jam</code> package.
     */
	public static final Preferences PREFS=Preferences.userNodeForPackage(JamPrefs.class);
	
	/**
	 * Name for the verbosity preference.
	 */
	public static final String VERBOSE="verbose";
	
	/**
	 * Name for the preference whether to show debug
	 * messages.
	 */
	public static final String DEBUG="debug";
}
