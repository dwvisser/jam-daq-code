package jam.commands;

import jam.io.hdf.HDFPrefs;

/**
 * 
 * Write emty  Axis Labels
 * @author ken
 *
 */
@SuppressWarnings("serial")
public class SetHDFSuppressSaveEmpty extends AbstractSetBooleanPreference {

	SetHDFSuppressSaveEmpty(){
		super();
		putValue(NAME, "Suppress saving empty Histograms");
		putValue(SHORT_DESCRIPTION,
		"Don't save empty Histograms");
		prefsNode=HDFPrefs.PREFS;
		key=HDFPrefs.SUPPRES_EMPTY;
		defaultState=true;
	}

}
