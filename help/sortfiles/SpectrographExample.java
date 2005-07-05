package help.sortfiles;

import jam.data.Gate;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.data.Monitor;
import jam.data.Scaler;
import jam.sort.SortException;
import jam.sort.SortRoutine;

/**
 * Online sort routine for YLSA coincidence with Enge Spectrometer. This was
 * modified from <CODE>sort.coinc.SPplus3LEDA_v3</CODE>, which was used in
 * the January 2001 test run. Changed 10 Aug 2001 to calculate the scintillator
 * event the right way; also added gate to cathAnde
 * 
 * @author Dale Visser
 * @created March 24, 2004
 * @since 26 July 2001
 */
public final class SpectrographExample extends SortRoutine {

	private final static String DEAD_TIME = "Dead Time %";

	/*
	 * VME properties
	 */
	private final static int[] ADC_BASE = { 0x20000000 };

	private final static int[] TDC_BASE = { 0x30000000, 0x30020000 };

	/*
	 * ADC lower threshold in channels
	 */
	private final static int THRESHOLDS = 128;

	/*
	 * TDC lower threshold in channels
	 */
	private final static int TIME_THRESH = 30;

	/*
	 * in nanoseconds
	 */
	private final static int TIME_RANGE = 1200;

	/*
	 * num of channels per ADC
	 */
	private final static int ADC_CHANNELS = 4096;

	/*
	 * 2D histograms
	 */
	private final static int CHAN_2D = 256;

	/*
	 * hi-res 2D histograms
	 */
	private final static int TWO_D_HIRES = 2 * CHAN_2D;

	/*
	 * 2D bits to shift >>
	 */
	private final static int TWO_D_FACTOR = Math.round((float) (Math
			.log(ADC_CHANNELS / CHAN_2D) / Math.log(2.0)));

	/*
	 * 2D hi-res bits to shift >>
	 */
	private final static int HIRES_FACTOR = Math.round((float) (Math
			.log(ADC_CHANNELS / TWO_D_HIRES) / Math.log(2.0)));

	/*
	 * ungated spectra
	 */
	private transient final HistInt1D hCthd, hSntrSum, hFrntPsn;

	/*
	 * Rear Wire Pulse Height
	 */
	private transient final HistInt2D hFrntPH;

	/*
	 * position x height y
	 */
	private transient final HistInt2D hRearPH;

	private transient final HistInt2D hSntrCthd, hFrntCthd, hFrntSntr,
			hFrntPRearP;

	/*
	 * gate by scintillator cathode
	 */
	private transient final HistInt2D hFrntSntrGSC, hFrntCthdGSC;

	/*
	 * gate by Front wire Cathode
	 */
	private transient final HistInt2D hSntrCthdGFC, hFrntSntrGFC;

	private transient final HistInt2D hSntrCthdGFS, hFrntCthdGFS;

	/*
	 * gate by Front wire Scintillator
	 */
	private transient final HistInt1D hFrntGAll;

	/*
	 * 2D gates
	 */
	private transient final Gate gSntrCthd, gFrntSntr, gFrntCthd, gFrntRear;

	/*
	 * number of FCLR's that went to ADC's
	 */
	private transient final Monitor mEvntRaw, mEvntAccept;

	/*
	 * id numbers for the signals;
	 */
	private transient int idCthd, idScintR, idScintL, idFrntPsn, idRearPsn,
			idFrntHgh, idRearHgh;

	/**
	 * Constructors for sort routines not necessary.
	 * 
	 * @see #initialize()
	 */
	public SpectrographExample() {
		super();
		hCthd = createHist1D(ADC_CHANNELS, "Cathode     ", "Cathode Raw ");
		hSntrSum = createHist1D(ADC_CHANNELS, "ScintSum    ",
				"Scintillator Sum");
		hFrntPsn = createHist1D(ADC_CHANNELS, "FrontPosn    ",
				"Front Wire Position");
		final String FRONT_POS = "Front Position";
		hFrntPH = createHist2D(CHAN_2D, "FrontPvsHeight",
				"Pulse Height of FrontFront wire vs Front Position", FRONT_POS,
				"Pulse Height");
		hRearPH = createHist2D(CHAN_2D, "RearPvsHeight ",
				"Pulse Height of RearMiddle wire vs Rear Position",
				"Rear Position", "Pulse Height");
		final String SCINT = "Scintillator";
		final String CATH = "Cathode";
		hSntrCthd = createHist2D(CHAN_2D, "ScintCathode  ",
				"Cathode vs Scintillator", SCINT, CATH);
		hFrntCthd = createHist2D(CHAN_2D, "FrontCathode  ",
				"Cathode vs Front Position", FRONT_POS, CATH);
		hFrntSntr = createHist2D(CHAN_2D, "FrontScint ",
				"Scintillator vs Front Position", FRONT_POS, SCINT);
		hFrntPRearP = createHist2D(TWO_D_HIRES, "FrontRear  ",
				"Rear Position vs Front Position", FRONT_POS, "Rear Position");
		// ScintCathode Gated on other
		hSntrCthdGFC = createHist2D(CHAN_2D, "ScintCathodeGFC",
				"Cathode vs Scintillator - FwCa gate", SCINT, CATH);
		hSntrCthdGFS = createHist2D(CHAN_2D, "ScintCathodeGFS",
				"Cathode vs Scintillator - FwSc gate", SCINT, CATH);
		// FrontCathode Gated on other
		hFrntCthdGSC = createHist2D(CHAN_2D, "FrontCathodeGSC",
				"Cathode vs Front Position - ScCa gate", FRONT_POS, CATH);
		hFrntCthdGFS = createHist2D(CHAN_2D, "FrontCathodeGFS ",
				"Cathode vs Front Position - FwSc gate ", FRONT_POS, CATH);
		// FrontScint Gated on other
		hFrntSntrGSC = createHist2D(CHAN_2D, "FrontScintGSC ",
				"Scintillator vs Front Position - ScCa gate", FRONT_POS, SCINT);
		hFrntSntrGFC = createHist2D(CHAN_2D, "FrontScintGFC",
				"Scintillator vs Front Position - FwCa gate", FRONT_POS, SCINT);
		// gated on 4 gates
		hFrntGAll = createHist1D(ADC_CHANNELS, "FrontGAll    ",
				"Front Position - ScCa,FwCa,FwSc,FwRw gates");
		// gates 2d
		gSntrCthd = new Gate("Ca-Sc", hSntrCthd);
		// gate on Scintillator Cathode
		gFrntSntr = new Gate("Fw-Sc", hFrntSntr);
		// gate on Front Scintillator
		gFrntCthd = new Gate("Fw-Ca", hFrntCthd);
		// gate on Front Cathode
		gFrntRear = new Gate("Fw-Rw", hFrntPRearP);
		hFrntSntrGSC.addGate(gFrntSntr);
		hFrntCthdGSC.addGate(gFrntCthd);
		hSntrCthdGFC.addGate(gSntrCthd);
		hFrntSntrGFC.addGate(gFrntSntr);
		hSntrCthdGFS.addGate(gSntrCthd);
		hFrntCthdGFS.addGate(gFrntCthd);
		/* scalers */
		final Scaler sBic = createScaler("BIC", 0);
		final Scaler sClck = createScaler("Clock", 1);
		final Scaler sEvntRaw = createScaler("Event Raw", 2);
		final Scaler sEvntAccpt = createScaler("Event Accept", 3);
		final Scaler sScint = createScaler(SCINT, 4);
		final Scaler sCathode = createScaler(CATH, 5);
		final Scaler sFCLR = createScaler("FCLR", 6);
		final Scaler sNMR = createScaler("NMR", 14);
		/* monitors */
		new Monitor("Beam ", sBic);
		new Monitor("Clock", sClck);
		mEvntRaw = new Monitor("Raw Events", sEvntRaw);
		mEvntAccept = new Monitor("Accepted Events", sEvntAccpt);
		new Monitor(SCINT, sScint);
		new Monitor(CATH, sCathode);
		new Monitor("FCLR", sFCLR);
		new Monitor("NMR", sNMR);
		new Monitor(DEAD_TIME, this);
	}

	public void initialize() throws SortException {
		vmeMap.setScalerInterval(3);
		for (int i = 0; i < TDC_BASE.length; i++) {
			vmeMap.setV775Range(TDC_BASE[i], TIME_RANGE);
		}

		vmeMap.eventParameter(2, ADC_BASE[0], 0, 0);
		/* anode */
		vmeMap.eventParameter(2, ADC_BASE[0], 1, THRESHOLDS);
		idScintR = vmeMap.eventParameter(2, ADC_BASE[0], 2, THRESHOLDS);
		idScintL = vmeMap.eventParameter(2, ADC_BASE[0], 3, THRESHOLDS);
		idFrntPsn = vmeMap.eventParameter(2, ADC_BASE[0], 4, THRESHOLDS);
		idRearPsn = vmeMap.eventParameter(2, ADC_BASE[0], 5, THRESHOLDS);
		idFrntHgh = vmeMap.eventParameter(2, ADC_BASE[0], 6, THRESHOLDS);
		idRearHgh = vmeMap.eventParameter(2, ADC_BASE[0], 7, THRESHOLDS);
		/* front y */
		vmeMap.eventParameter(2, ADC_BASE[0], 8, THRESHOLDS);
		/* rear y */
		vmeMap.eventParameter(2, ADC_BASE[0], 9, THRESHOLDS);
		idCthd = vmeMap.eventParameter(2, ADC_BASE[0], 10, THRESHOLDS);
		/* FW bias */
		vmeMap.eventParameter(2, ADC_BASE[0], 12, THRESHOLDS);
		/* RW bias */
		vmeMap.eventParameter(2, ADC_BASE[0], 13, THRESHOLDS);
		/* BCI range */
		vmeMap.eventParameter(2, ADC_BASE[0], 14, 16);
		/* front left */
		vmeMap.eventParameter(2, ADC_BASE[0], 16 + 3, THRESHOLDS);
		/* front right */
		vmeMap.eventParameter(2, ADC_BASE[0], 16 + 4, THRESHOLDS);
		/* rear left */
		vmeMap.eventParameter(2, ADC_BASE[0], 16 + 6, THRESHOLDS);
		/* rear right */
		vmeMap.eventParameter(2, ADC_BASE[0], 16 + 7, THRESHOLDS);

		/*
		 * new pulse height signals from TAJ
		 */
		/* rear back pulse height */
		vmeMap.eventParameter(2, ADC_BASE[0], 16 + 10, THRESHOLDS);
		/* rear front pulse height */
		vmeMap.eventParameter(2, ADC_BASE[0], 16 + 11, THRESHOLDS);
		/* front mid pulse height */
		vmeMap.eventParameter(2, ADC_BASE[0], 16 + 12, THRESHOLDS);
		/* front back pulse height */
		vmeMap.eventParameter(2, ADC_BASE[0], 16 + 13, THRESHOLDS);

		/*
		 * TDC based position parameters
		 */
		/* front left TDC */
		vmeMap.eventParameter(5, TDC_BASE[0], 16 + 0, TIME_THRESH);
		/* front right TDC */
		vmeMap.eventParameter(5, TDC_BASE[0], 16 + 1, TIME_THRESH);
		/* rear left TDC */
		vmeMap.eventParameter(7, TDC_BASE[1], 0, TIME_THRESH);
		/* rear right TDC */
		vmeMap.eventParameter(7, TDC_BASE[1], 1, TIME_THRESH);

	}

	public void sort(final int[] dataEvent) throws SortException {
		/*
		 * unpack data into convenient names
		 */
		final int eCthd = dataEvent[idCthd];
		final int SCINTR = dataEvent[idScintR];
		final int SCINTL = dataEvent[idScintL];
		final int FPOS = dataEvent[idFrntPsn];
		final int RPOS = dataEvent[idRearPsn];
		final int FHEIGHT = dataEvent[idFrntHgh];
		final int RHEIGHT = dataEvent[idRearHgh];

		/*
		 * proper way to add for 2 phototubes at the ends of scintillating rod
		 * see Knoll
		 */
		final int SCINT = (int) Math.round(Math.sqrt(SCINTR * SCINTL));
		final int FPOS_COMPR = FPOS >> TWO_D_FACTOR;
		final int RPOS_COMPR = RPOS >> TWO_D_FACTOR;
		final int FHEIGHT_COMP = FHEIGHT >> TWO_D_FACTOR;
		final int RHEIGHT_COMP = RHEIGHT >> TWO_D_FACTOR;
		final int SCINT_COMPR = SCINT >> TWO_D_FACTOR;

		final int ECCTHD = eCthd >> TWO_D_FACTOR;

		// singles spectra
		hCthd.inc(eCthd);
		hSntrSum.inc(SCINT);
		hFrntPsn.inc(FPOS);

		final int FPOS_COMP_HI = FPOS >> HIRES_FACTOR;
		final int RPOS_COMP_HI = RPOS >> HIRES_FACTOR;
		hFrntPH.inc(FPOS_COMPR, FHEIGHT_COMP);
		hRearPH.inc(RPOS_COMPR, RHEIGHT_COMP);
		hSntrCthd.inc(SCINT_COMPR, ECCTHD);
		hFrntCthd.inc(FPOS_COMPR, ECCTHD);
		hFrntSntr.inc(FPOS_COMPR, SCINT_COMPR);
		hFrntPRearP.inc(FPOS_COMP_HI, RPOS_COMP_HI);

		final boolean SC_INGATE = gSntrCthd.inGate(SCINT_COMPR, ECCTHD);
		final boolean FC_INGATE = gFrntCthd.inGate(FPOS_COMPR, ECCTHD);
		final boolean FS_INGATE = gFrntSntr.inGate(FPOS_COMPR, SCINT_COMPR);
		final boolean IN_PID_GATES = SC_INGATE && FC_INGATE && FS_INGATE;
		final boolean FR_RE_INGATE = gFrntRear.inGate(FPOS_COMP_HI,
				RPOS_COMP_HI);
		final boolean GOOD_DIREC = FR_RE_INGATE;
		final boolean GOOD = GOOD_DIREC && IN_PID_GATES;

		if (SC_INGATE) {
			// gate on Scintillator vs Cathode
			hFrntSntrGSC.inc(FPOS_COMPR, SCINT_COMPR);
			hFrntCthdGSC.inc(FPOS_COMPR, ECCTHD);
		}
		if (FC_INGATE) {
			// gate on Front Wire Position vs Cathode
			hSntrCthdGFC.inc(SCINT_COMPR, ECCTHD);
			hFrntSntrGFC.inc(FPOS_COMPR, SCINT_COMPR);
		}
		if (FS_INGATE) {
			// gate on Front Wire Position vs Scintillator
			hSntrCthdGFS.inc(SCINT_COMPR, ECCTHD);
			hFrntCthdGFS.inc(FPOS_COMPR, ECCTHD);
		}
		if (IN_PID_GATES && GOOD) {
			writeEvent(dataEvent);
			hFrntGAll.inc(FPOS);
		}
	}

	/**
	 * Called so the dead time can be calculated.
	 * 
	 * @param name
	 *            name of monitor to calculate
	 * @return floating point value of monitor
	 */
	public double monitor(final String name) {
		double rval = 0.0;
		if (name.equals(DEAD_TIME)) {
			final double ACCEPTRATE = mEvntAccept.getValue();
			final double RAWRATE = mEvntRaw.getValue();
			if (ACCEPTRATE > 0.0 && ACCEPTRATE <= RAWRATE) {
				rval = 100.0 * (1.0 - ACCEPTRATE / RAWRATE);
			}
		}
		return rval;
	}
}
