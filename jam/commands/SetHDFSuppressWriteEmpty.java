package jam.commands;

import jam.io.hdf.HDFPrefs;

/**
 * 
 * Write emty  Axis Labels
 * @author ken
 *
 */
public class SetHDFSuppressWriteEmpty extends AbstractSetBooleanPreference {

	SetHDFSuppressWriteEmpty(){
		super();
		putValue(NAME, "Suppress writing empty Histograms");
		putValue(SHORT_DESCRIPTION,
		"Don't write out empty Histograms");
		prefsNode=HDFPrefs.PREFS;
		key=HDFPrefs.SUPPRESS_WRITE_EMPTY;
		defaultState=true;
	}

}
