package jam.io.hdf;

import java.util.prefs.Preferences;

/**
* The preferences node for the <code>hdf</code> package.
* 
* @author Ken Swartz
*/
public final class HDFPrefs {
	
		private HDFPrefs(){
			super();
		}
	   
	   /**
	    * The preferences node for the <code>jam</code> package.
	    */
		public static final Preferences PREFS=Preferences.userNodeForPackage(HDFPrefs.class);
		
		/**
		 * Name for the empty write preference.
		 */
		public static final String SUPPRES_EMPTY="Write Empty Histograms/Gates";
}
