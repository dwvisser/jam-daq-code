package help.sortfiles;

import jam.data.Gate;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.data.Monitor;
import jam.data.Scaler;
import jam.sort.SortException;
import jam.sort.SortRoutine;

/**
 * Test sort file for ADC at LENA. Modified 2 March 2002 for example purposes by
 * Dale Visser.
 * 
 * @author C. Iliadis 
 * @author Dale Visser
 */
public class CI extends SortRoutine {

	/** * GLOBAL DECLARATIONS ** */
	static final int ADC_BASE = 0xe0000000;

	static final int THRESHOLDS = 100;

	static final int ADC_CHANNELS = 4096; //num of channels per ADC

	/* number of channels per dimension in 2-d histograms */
	static final int TWO_D_CHANS = 512;

	/* amount of bits to shift for compression */
	static final int TWO_D_FACTOR = Math.round((float) (Math.log(ADC_CHANNELS
			/ TWO_D_CHANS) / Math.log(2.0)));

	transient int idGe, idNaI, idTAC;//  id numbers for the signals

	transient final HistInt1D hGe, hNaI, hTAC;//  ungated 1D spectra

	transient final HistInt2D hGeNaI;//  ungated 2D spectra

	transient final HistInt1D hGe_TAC;//	gated on TAC

	transient final HistInt1D hGe_g2d, hTAC_g2d;//  gated on Ge vs. NaI

	transient final Gate gTAC;//  1D gate

	transient final Gate gGeNaI;//2D gate

	transient final Scaler sClock, sBeam, sGe, sAccept, sNaI;//  scalers

	transient final Monitor mDeadTime;

	static final String DEAD_TIME = "Dead Time (%)";

	transient int lastGe, lastAccept;//for calculating dead time

	/** * END OF GLOBAL DECLARATIONS ** */
	
	public CI(){
		super();
		final String NAI="NaI";
		/** * HISTOGRAM SECTION ** */
		hGe = createHist1D(ADC_CHANNELS,"Ge", "Germanium");
		hNaI = createHist1D(ADC_CHANNELS, NAI);
		hTAC = createHist1D(ADC_CHANNELS, "TAC");
		hGe_TAC = createHist1D(ADC_CHANNELS, "Ge-TAC",
				"Germanium, gated on TAC");
		hGe_g2d = createHist1D(ADC_CHANNELS, "Ge-2dgate", 
				"Germanium--gated on NaI vs Ge");
		hTAC_g2d = createHist1D(ADC_CHANNELS, "TAC-2dgate",
				"TAC--gated on NaI vs Ge");
		hGeNaI = createHist2D(TWO_D_CHANS, "GeNaI",
				"NaI vs. Germanium", "Germanium", NAI);
		/** * GATE SECTION ** */
		gTAC = new Gate("TAC", hTAC);
		/*
		 * Monitor associated with Gate, window will show rate of new counts in
		 * Hz
		 */
		new Monitor("TAC window", gTAC);
		gGeNaI = new Gate("GeNaI", hGeNaI);
		/** * SCALER SECTION ** */
		sClock = new Scaler("Clock", 0);// (name, position in scaler unit)
		sBeam = new Scaler("Beam", 1);
		sGe = new Scaler("Ge", 2); //Ge provides trigger
		sAccept = new Scaler("Ge Accept", 3);
		sNaI = new Scaler(NAI, 4);
		/** * MONITOR SECTION ** */
		/*
		 * Monitors associated with scalers, window will return scaler rate in
		 * Hz
		 */
		new Monitor(sClock.getName(), sClock);
		new Monitor(sBeam.getName(), sBeam);
		new Monitor(sGe.getName(), sGe);
		new Monitor(sAccept.getName(), sAccept);
		new Monitor(sNaI.getName(), sNaI);
		//User-defined monitor which is calculated in this sort routine
		mDeadTime = new Monitor(DEAD_TIME, this);
	}

	/**
	 * Method called to initialize objects when the sort routine is loaded.
	 */
	public void initialize() throws SortException {
		/*
		 * insert scaler block in event data every 3 seconds
		 */
		vmeMap.setScalerInterval(3);
		/** * ADC CHANNELS SECTION ** */
		/*
		 * eventParameters, args = (slot, base address, channel, threshold
		 * channel)
		 */
		idGe = vmeMap.eventParameter(2, ADC_BASE, 0, THRESHOLDS);
		idNaI = vmeMap.eventParameter(2, ADC_BASE, 1, THRESHOLDS);
		idTAC = vmeMap.eventParameter(2, ADC_BASE, 2, THRESHOLDS);
		hTAC_g2d.addGate(gTAC);
	}//end of initialize()

	/**
	 * Method for sorting of data into spectra.
	 */
	public void sort(int[] data) {
		/** * EXTRACT DATA FROM ARRAY ** */
		int eGe = data[idGe];
		int eNaI = data[idNaI];
		int eTAC = data[idTAC];
		int ecGe = eGe >> TWO_D_FACTOR;//bit-shifts are faster than division
		int ecNaI = eNaI >> TWO_D_FACTOR;

		/** * INCREMENT UNGATED SPECTRA ** */
		hGe.inc(eGe);
		hNaI.inc(eNaI);
		hTAC.inc(eTAC);
		hGeNaI.inc(ecGe, ecNaI);// inc(x-channel, y-channel)

		/** * INCREMENT GATED SPECTRA ** */
		if (gTAC.inGate(eTAC)){
			hGe_TAC.inc(eGe);
		}
		if (gGeNaI.inGate(ecGe, ecNaI)) {
			hGe_g2d.inc(eGe);
			hTAC_g2d.inc(eTAC);
		}
	}

	/**
	 * Method for calculating values of user-defined monitors.
	 */
	public double monitor(String name) {
		double rval = 0.0;
		if (name.equals(DEAD_TIME)) {
			final double geVal = (double) sGe.getValue();
			final double acceptVal = (double) sAccept.getValue();
			rval = 100.0 * (1.0 - (lastAccept - acceptVal) / (geVal - lastGe));
			lastGe = (int) geVal;
			lastAccept = (int) acceptVal;
		}
		return rval;
	}

}//end of class CI
