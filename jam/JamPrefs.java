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
public interface JamPrefs {
    
    /**
     * The preferences node for the <code>jam</code> package.
     */
	Preferences PREFS=Preferences.userNodeForPackage(JamPrefs.class);
	
	/**
	 * Name for the verbosity preference.
	 */
	String VERBOSE="verbose";
	
	/**
	 * Name for the preference whether to show debug
	 * messages.
	 */
	String DEBUG="debug";
}
