package jam.io.hdf;

import jam.JamPrefs;
import java.util.prefs.Preferences;

/**
* The preferences node for the <code>hdf</code> package.
* 
* @author Ken Swartz
*/
public interface HDFPrefs {
	   
	   /**
	    * The preferences node for the <code>jam</code> package.
	    */
		Preferences PREFS=Preferences.userNodeForPackage(JamPrefs.class);
		
		/**
		 * Name for the empty write preference.
		 */
		String SUPPRESS_WRITE_EMPTY="Write Empty Hisograms/Gates";
		
}
