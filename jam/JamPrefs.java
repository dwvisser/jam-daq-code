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
	Preferences PREFS=Preferences.userNodeForPackage(JamPrefs.class);
	String VERBOSE="verbose";
	String DEBUG="debug";
}
