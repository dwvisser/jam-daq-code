package help.sortfiles;

import jam.data.Gate;
import jam.data.HistInt1D;
import jam.data.Scaler;
import jam.data.DataParameter;
import jam.sort.SortException;
import jam.sort.SortRoutine;

/**
 * Template sort routine for Jam
 * 
 * @author Ken Swartz
 * @version 1 June 99
 */
public final class CamacSortTemplate extends SortRoutine {

	/** variables declarations */
	static final int PARAM_ID = 0; //id number for event word from cnaf

	static final int SCALER_ID = 0; //id number for scaler from cnaf

	transient final HistInt1D myHist; //declare histogram myHist

	transient final HistInt1D myHistGated; //declare histogram myHistGated

	transient final Gate myGate; //declare gate myGate;

	transient final Scaler myScal; //declare scaler myScal;
	
	transient final DataParameter myParam;

	/**
	 * Constructor, not usually used. 
	 *
	 * @see #initialize()
	 */
	public CamacSortTemplate() {
		super();
		final int hist1d = 1024;
		/* initialize histograms, gates, and scalers */
		myHist = createHist1D(hist1d, "detector1", "my detector");
		myHistGated = createHist1D(hist1d, "detecGated", "my detector gated");
		myGate = new Gate("detector1", myHist);
		myScal = new Scaler("scaler1", SCALER_ID);
		myParam = new DataParameter("ParamAdjust");
	}

	/**
	 * The initialization method code to define camac commands, variables and
	 * classes.
	 * 
	 * @see SortRoutine#initialize()
	 */
	public void initialize() throws SortException {
		/*
		 * uncomment to setup camac commands here
		 * cnafCommands.init(c,n,a,f);//initialize crate cnafs
		 * idEvnt=cnafCommands.eventRead(c,n,a,f);//event cnafs
		 * cnafCommands.eventCommand(c,n,a,f);//non-read command to issue
		 * idScal=cnafCommands.scaler(c,n,a,f);//scaler read cnafs
		 * cnafCommands.clear(c,n,a,f);//scaler clear cnaf
		 */

		/*
		 * comment out setEventSize() if you actually put in the CAMAC stuff
		 */
		setEventSize(2);
	}

	/**
	 * @see SortRoutine#sort(int[])
	 */
	public void sort(int[] eventData) {
		myHist.inc(eventData[PARAM_ID]); //increment myHist with word idHist;
		if (myGate.inGate(eventData[PARAM_ID])) { //if event word is in myGate
			myHistGated.inc(eventData[PARAM_ID]); //increment myHistGate
		}
	}
}