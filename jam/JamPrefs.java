/*
 * Created on Jun 10, 2004
 *
 */
package jam;

import java.util.prefs.Preferences;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 10, 2004
 */
public interface JamPrefs {
	final Preferences prefs=Preferences.systemNodeForPackage(JamPrefs.class);
	final String VERBOSE="verbose";
	final String DEBUG="debug";
}
