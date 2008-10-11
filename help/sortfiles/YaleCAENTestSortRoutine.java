package help.sortfiles;

import jam.data.DataParameter;
import jam.data.Gate;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.sort.SortException;
import jam.sort.SortRoutine;

/**
 * Sort routine for neutron counting with the aid of an anti-coincidence cosmic
 * muon shield and a NaI Annulus.
 * 
 * @author R. Longland
 */
public class YaleCAENTestSortRoutine extends SortRoutine {

	// -----------------------------------------------
	// General Definitions
	// -----------------------------------------------

	private static final String NA_I_TDC = "NaI TDC ";
	// Set VME addresses for modules in VME crate
	private static final int ADC_BASE_FIRST = 0xfaa00000;
	private static final int ADC_BASE_SECOND = 0xfa800000;
	private static final int TDC_BASE = 0xfac00000;

	// Global thresholds for ADC and TDC
	private static final int THRESHOLDS = 50;

	// Time range for TDC(141 to 1200 ns)
	private static final int TIME_RANGE = 1200; // ns

	// Number of channels for spectra and per dimension in 2D spectra
	private static final int SPC_CHANNELS = 4096;
	private static final int TWO_D_CHANNELS = 512;

	// IDs of signals read in by jam
	private transient int idnE; // The neutron energy signal
	private transient int idnPSD; // Neutron PSD signal
	private transient int idnTDC; // Neutron capture TDC signal
	private transient final int idNaITDC[] = new int[16]; // NaI timing
	private transient final int idNaIADC[] = new int[16]; // NaI energy
	private transient int idSciTAC; // Veto shield times
	private transient final int idSciADC[] = new int[5]; // Veto shield
	// energies

	// The data channels. Raw data is assigned to these when it is read
	private transient final int cNaITDC[] = new int[16]; // NOPMD
	private transient final int cNaIADC[] = new int[16]; // NOPMD
	private transient final int cSciADC[] = new int[5]; // NOPMD

	// Some floating point variables for energies
	private transient final double eNaIADC[] = new double[16]; // NOPMD

	private transient DataParameter pcenNaI1;
	private transient DataParameter pcenNaI2;
	private transient DataParameter pcchNaI1_0;
	private transient DataParameter pcchNaI2_0;
	private transient DataParameter pcchNaI1_1;
	private transient DataParameter pcchNaI2_1;
	private transient DataParameter pcchNaI1_2;
	private transient DataParameter pcchNaI2_2;
	private transient DataParameter pcchNaI1_3;
	private transient DataParameter pcchNaI2_3;
	private transient DataParameter pcchNaI1_4;
	private transient DataParameter pcchNaI2_4;
	private transient DataParameter pcchNaI1_5;
	private transient DataParameter pcchNaI2_5;
	private transient DataParameter pcchNaI1_6;
	private transient DataParameter pcchNaI2_6;
	private transient DataParameter pcchNaI1_7;
	private transient DataParameter pcchNaI2_7;
	private transient DataParameter pcchNaI1_8;
	private transient DataParameter pcchNaI2_8;
	private transient DataParameter pcchNaI1_9;
	private transient DataParameter pcchNaI2_9;
	private transient DataParameter pcchNaI1_10;
	private transient DataParameter pcchNaI2_10;
	private transient DataParameter pcchNaI1_11;
	private transient DataParameter pcchNaI2_11;
	private transient DataParameter pcchNaI1_12;
	private transient DataParameter pcchNaI2_12;
	private transient DataParameter pcchNaI1_13;
	private transient DataParameter pcchNaI2_13;
	private transient DataParameter pcchNaI1_14;
	private transient DataParameter pcchNaI2_14;
	private transient DataParameter pcchNaI1_15;
	private transient DataParameter pcchNaI2_15;

	// -------------------------------------------------
	// HISTOGRAMS
	// -------------------------------------------------

	// Ungated spectra taken directly from ADCs and TDCs
	private transient HistInt1D hnE;
	private transient HistInt1D hnPSD;
	private transient HistInt1D hnTDC;
	private transient final HistInt1D hNaIADC[] = new HistInt1D[16];
	private transient final HistInt1D hNaITDC[] = new HistInt1D[16];
	private transient final HistInt1D hSciADC[] = new HistInt1D[5];
	private transient HistInt1D hSciTAC;

	// Capture times gated on 2D PSD spectrum
	private transient HistInt2D h2dnEvsTDC_PSD;

	// Summed NaI spectra (gated on NaI TDC)
	private transient HistInt1D hNaIsumE;

	// BC523A E vs. PSD Spectrum
	private transient HistInt2D h2dnEvsPSD;

	// BC523A E vs. capture peak TDC
	private transient HistInt2D h2dnEvsTDC;

	// BC523A vetoed by scintillators or NaI
	private transient HistInt1D hnE_NaIVeto;
	private transient HistInt1D hnE_SciVetoE; // energy only
	private transient HistInt1D hnE_SciVetoT; // time only
	private transient HistInt1D hnE_SciNaIVeto; // NaI and Scintillators

	// BC523A spectrum gated on 2D PSD gate (a and b)
	private transient HistInt1D hnE_PSDGatea;
	private transient HistInt1D hnE_PSDGateb;

	// BC523A spectrum gated on capture peak TDC (a and b)
	private transient HistInt1D hnE_TDCGatea;
	private transient HistInt1D hnE_TDCGateb;

	// BC523A spectrum gated on 2D capture peak
	private transient HistInt1D hnE_ETDCGate;

	// BC523A spectrum gated on NaI 477 kev gamma-ray
	private transient HistInt1D hnE_NaIE;

	// ----------------------------------------------------
	// GATES
	// ----------------------------------------------------

	// BC523A Capture TDC gate
	private transient Gate gnTDCa;
	private transient Gate gnTDCb;

	// 2D Capture peak gate
	private transient Gate gnEvsTDC;

	// BC523A 2D PSD Gate
	private transient Gate gnEvsPSDa;
	private transient Gate gnEvsPSDb;

	// NaI TDC Gates (a and b)
	private transient final Gate gNaITDCa[] = new Gate[16];

	// NaI ADC Gates
	private transient final Gate gNaIADC[] = new Gate[16];

	// Gate on summed NaI for 477 keV gamma
	private transient Gate gNaIsum;

	// Scintillator energy gates
	private transient final Gate gSciADC[] = new Gate[5];

	// Scintillator TAC gate
	private transient Gate gSciTAC;

	// *****************************************************
	// INITIALISATIONS
	// *****************************************************

	@Override
	public void initialize() throws SortException {

		setupVmeMap();

		setupHistograms();

		// ----------------------------------------------
		// Set up Gates
		// args = name, spectrum to put the gate in
		// ----------------------------------------------

		// Capture TDC gate
		gnTDCa = new Gate("Cap T Gate a", hnTDC);
		gnTDCb = new Gate("Cap T Gate b", hnTDC);

		// 2D capture gate
		gnEvsTDC = new Gate("2D Cap T", h2dnEvsTDC);

		// 2 PSD gates
		gnEvsPSDa = new Gate("PSD Gate a", h2dnEvsPSD);
		gnEvsPSDb = new Gate("PSD Gate b", h2dnEvsPSD);

		// NaI ADC Gates
		for (int i = 0; i < 16; i++) {
			gNaIADC[i] = new Gate("NaI " + i, hNaIADC[i]);// NOPMD
		}
		// NaI TDC Gates
		for (int i = 0; i < 16; i++) {
			gNaITDCa[i] = new Gate(NA_I_TDC + i + " a", hNaITDC[i]);// NOPMD
		}

		// NaI Sum gate
		gNaIsum = new Gate("NaI Sum", hNaIsumE);

		// Scintillator TAC
		gSciTAC = new Gate("Sci Time", hSciTAC);

		// Scintillator ADC
		for (int i = 0; i < 5; i++) {
			gSciADC[i] = new Gate("Sci ADC " + i, hSciADC[i]);// NOPMD
		}

		// ---------------------------------------
		// Scaler Information
		// --------------------------------------
		createScaler("Beam Charge", 0);
		createScaler("Real Time", 1);
		createScaler("Common Gates", 2);
		createScaler("NaI Gates", 3);
		createScaler("Pulser", 4);
		createScaler("Spare 1", 5);
		createScaler("Spare 2", 6);
		createScaler("Spare 3", 7);

		// ------------------------------------------
		// Sort parameters
		// ------------------------------------------
		pcenNaI1 = createParameter("cenNaI1");
		pcenNaI2 = createParameter("cenNaI2");

		pcchNaI1_0 = createParameter("cchNaI1_0");
		pcchNaI2_0 = createParameter("cchNaI2_0");
		pcchNaI1_1 = createParameter("cchNaI1_1");
		pcchNaI2_1 = createParameter("cchNaI2_1");
		pcchNaI1_2 = createParameter("cchNaI1_2");
		pcchNaI2_2 = createParameter("cchNaI2_2");
		pcchNaI1_3 = createParameter("cchNaI1_3");
		pcchNaI2_3 = createParameter("cchNaI2_3");
		pcchNaI1_4 = createParameter("cchNaI1_4");
		pcchNaI2_4 = createParameter("cchNaI2_4");
		pcchNaI1_5 = createParameter("cchNaI1_5");
		pcchNaI2_5 = createParameter("cchNaI2_5");
		pcchNaI1_6 = createParameter("cchNaI1_6");
		pcchNaI2_6 = createParameter("cchNaI2_6");
		pcchNaI1_7 = createParameter("cchNaI1_7");
		pcchNaI2_7 = createParameter("cchNaI2_7");
		pcchNaI1_8 = createParameter("cchNaI1_8");
		pcchNaI2_8 = createParameter("cchNaI2_8");
		pcchNaI1_9 = createParameter("cchNaI1_9");
		pcchNaI2_9 = createParameter("cchNaI2_9");
		pcchNaI1_10 = createParameter("cchNaI1_10");
		pcchNaI2_10 = createParameter("cchNaI2_10");
		pcchNaI1_11 = createParameter("cchNaI1_11");
		pcchNaI2_11 = createParameter("cchNaI2_11");
		pcchNaI1_12 = createParameter("cchNaI1_12");
		pcchNaI2_12 = createParameter("cchNaI2_12");
		pcchNaI1_13 = createParameter("cchNaI1_13");
		pcchNaI2_13 = createParameter("cchNaI2_13");
		pcchNaI1_14 = createParameter("cchNaI1_14");
		pcchNaI2_14 = createParameter("cchNaI2_14");
		pcchNaI1_15 = createParameter("cchNaI1_15");
		pcchNaI2_15 = createParameter("cchNaI2_15");
	}

	private void setupHistograms() {
		// ------------------------------------------
		// Set up the histograms in order they will be seen
		// args = (spectrum name,1d/2d, no. of channels, title, x-label,
		// y-label)
		// -----------------------------------------

		hnE = createHist1D(SPC_CHANNELS, "Neutron E", "Neutron E Singles");
		hnPSD = createHist1D(SPC_CHANNELS, "Neutron PSD", "Neutron PSD");
		hnTDC = createHist1D(SPC_CHANNELS, "Capture Times",
				"Neutron Capture Peak");

		h2dnEvsPSD = createHist2D(TWO_D_CHANNELS, "E vs PSD",
				"Neutron E Singles vs PSD", "E", "PSD");
		h2dnEvsTDC = createHist2D(TWO_D_CHANNELS, "E vs Cap Time",
				"Neutron E Singles vs Capture Time", "E", "Time");
		h2dnEvsTDC_PSD = createHist2D(TWO_D_CHANNELS, "E vs Cap Time, PSD",
				"Neutron E Singles vs Capture Time, Gated on PSD Gate a", "E",
				"Time");

		hnE_NaIVeto = createHist1D(SPC_CHANNELS, "E NaI Veto",
				"Neutron E Vetoed by NaI");
		hnE_SciVetoE = createHist1D(SPC_CHANNELS, "E Sci Veto E",
				"Neutron E Vetoed by Scintillator Energy");
		hnE_SciVetoT = createHist1D(SPC_CHANNELS, "E Sci Veto T",
				"Neutron E Vetoed by Scintillator Times");
		hnE_SciNaIVeto = createHist1D(SPC_CHANNELS, "E NaI and Sci Veto",
				"Neutron E Vetoed by NaI and Scintillators");

		hnE_PSDGatea = createHist1D(SPC_CHANNELS, "E, PSD Gate a",
				"Neutron E Gated on PSD Gate a");
		hnE_PSDGateb = createHist1D(SPC_CHANNELS, "E, PSD Gate b",
				"Neutron E Gated on PSD Gate b");

		hnE_TDCGatea = createHist1D(SPC_CHANNELS, "E, Capture Gate a",
				"Neutron E Gated on Capture Gate a");
		hnE_TDCGateb = createHist1D(SPC_CHANNELS, "E, Capture Gate b",
				"Neutron E Gated on Capture Gate b");
		hnE_ETDCGate = createHist1D(SPC_CHANNELS, "E, 2D Capture Gate",
				"Neutron E Gated on 2D Capture Gate");

		hnE_NaIE = createHist1D(SPC_CHANNELS, "E, NaIE Gate",
				"Neutron E Gated on Summed Neutron Energy");

		hNaIsumE = createHist1D(SPC_CHANNELS, "NaI Summed Energy",
				"NaI Summed Energy");

		for (int i = 0; i < 16; i++) {
			hNaIADC[i] = createHist1D(SPC_CHANNELS, "NaI " + i, "NaI " + i);
		}
		for (int i = 0; i < 16; i++) {
			hNaITDC[i] = createHist1D(SPC_CHANNELS, NA_I_TDC + i, NA_I_TDC + i);
		}

		hSciTAC = createHist1D(SPC_CHANNELS, "Sci TAC", "Scintillators TAC");
		for (int i = 0; i < 5; i++) {
			hSciADC[i] = createHist1D(SPC_CHANNELS, "Sci " + i,
					"Scintillator E " + i);
		}
	}

	private void setupVmeMap() throws SortException {
		vmeMap.setScalerInterval(3);

		// Set up time range of TDC
		vmeMap.setV775Range(TDC_BASE, TIME_RANGE);

		// --------------------------------------------------------------
		// Set up the mapping for each used channel of the ADC and TDC
		// to the appropriate id eventParameters,
		// args = (slot, base address, channel, threshold channel)
		// --------------------------------------------------------------

		idnE = vmeMap.eventParameter(3, ADC_BASE_FIRST, 14, THRESHOLDS);
		idnPSD = vmeMap.eventParameter(3, ADC_BASE_FIRST, 13, THRESHOLDS);
		idnTDC = vmeMap.eventParameter(3, ADC_BASE_FIRST, 12, THRESHOLDS);

		// Scintillators
		idSciTAC = vmeMap.eventParameter(3, ADC_BASE_FIRST, 6, THRESHOLDS);
		for (int i = 0; i < 5; i++) {
			idSciADC[i] = vmeMap.eventParameter(3, ADC_BASE_FIRST, i + 16,
					THRESHOLDS);
		}

		// NaI
		for (int i = 0; i < 16; i++) {
			idNaIADC[i] = vmeMap.eventParameter(5, ADC_BASE_SECOND, i,
					THRESHOLDS);
			idNaITDC[i] = vmeMap.eventParameter(7, TDC_BASE, i, THRESHOLDS);
		}
	}

	@Override
	public void sort(final int[] data) {

		// Read data and give appropriate assignment

		final int cnE = data[idnE];
		final int cnPSD = data[idnPSD];
		final int cnTDC = data[idnTDC];

		for (int i = 0; i < 16; i++) {
			cNaIADC[i] = data[idNaIADC[i]];
			cNaITDC[i] = data[idNaITDC[i]];
		}

		final int cSciTAC = data[idSciTAC];
		for (int i = 0; i < 5; i++) {
			cSciADC[i] = data[idSciADC[i]];
		}

		// Parameters for energy calibrations
		final double cenNaI1 = pcenNaI1.getValue();
		final double cenNaI2 = pcenNaI2.getValue();
		final double cchNaI1x0 = pcchNaI1_0.getValue();
		final double cchNaI2x0 = pcchNaI2_0.getValue();
		final double cchNaI1x1 = pcchNaI1_1.getValue();
		final double cchNaI2x1 = pcchNaI2_1.getValue();
		final double cchNaI1x2 = pcchNaI1_2.getValue();
		final double cchNaI2x2 = pcchNaI2_2.getValue();
		final double cchNaI1x3 = pcchNaI1_3.getValue();
		final double cchNaI2x3 = pcchNaI2_3.getValue();
		final double cchNaI1x4 = pcchNaI1_4.getValue();
		final double cchNaI2x4 = pcchNaI2_4.getValue();
		final double cchNaI1x5 = pcchNaI1_5.getValue();
		final double cchNaI2x5 = pcchNaI2_5.getValue();
		final double cchNaI1x6 = pcchNaI1_6.getValue();
		final double cchNaI2x6 = pcchNaI2_6.getValue();
		final double cchNaI1x7 = pcchNaI1_7.getValue();
		final double cchNaI2x7 = pcchNaI2_7.getValue();
		final double cchNaI1x8 = pcchNaI1_8.getValue();
		final double cchNaI2x8 = pcchNaI2_8.getValue();
		final double cchNaI1x9 = pcchNaI1_9.getValue();
		final double cchNaI2x9 = pcchNaI2_9.getValue();
		final double cchNaI1x10 = pcchNaI1_10.getValue();
		final double cchNaI2x10 = pcchNaI2_10.getValue();
		final double cchNaI1x11 = pcchNaI1_11.getValue();
		final double cchNaI2x11 = pcchNaI2_11.getValue();
		final double cchNaI1x12 = pcchNaI1_12.getValue();
		final double cchNaI2x12 = pcchNaI2_12.getValue();
		final double cchNaI1x13 = pcchNaI1_13.getValue();
		final double cchNaI2x13 = pcchNaI2_13.getValue();
		final double cchNaI1x14 = pcchNaI1_14.getValue();
		final double cchNaI2x14 = pcchNaI2_14.getValue();
		final double cchNaI1x15 = pcchNaI1_15.getValue();
		final double cchNaI2x15 = pcchNaI2_15.getValue();

		// Compress Neutron energies by factor of 20 for 2D spectra
		// Change cnE to enE once parameters have been found.
		final int calibnE = (int) Math.round(cnE * 0.1);
		final int calibnPSD = (int) Math.round(cnPSD * 0.1);
		final int calibnTDC = (int) Math.round(cnTDC * 0.1);

		// Calibrate NaI
		eNaIADC[0] = ((cenNaI2 - cenNaI1) / (cchNaI2x0 - cchNaI1x0))
				* cNaIADC[0]
				+ (cenNaI1 - cchNaI1x0
						* ((cenNaI2 - cenNaI1) / (cchNaI2x0 - cchNaI1x0)));
		eNaIADC[1] = ((cenNaI2 - cenNaI1) / (cchNaI2x1 - cchNaI1x1))
				* cNaIADC[1]
				+ (cenNaI1 - cchNaI1x1
						* ((cenNaI2 - cenNaI1) / (cchNaI2x1 - cchNaI1x1)));
		eNaIADC[2] = ((cenNaI2 - cenNaI1) / (cchNaI2x2 - cchNaI1x2))
				* cNaIADC[2]
				+ (cenNaI1 - cchNaI1x2
						* ((cenNaI2 - cenNaI1) / (cchNaI2x2 - cchNaI1x2)));
		eNaIADC[3] = ((cenNaI2 - cenNaI1) / (cchNaI2x3 - cchNaI1x3))
				* cNaIADC[3]
				+ (cenNaI1 - cchNaI1x3
						* ((cenNaI2 - cenNaI1) / (cchNaI2x3 - cchNaI1x3)));
		eNaIADC[4] = ((cenNaI2 - cenNaI1) / (cchNaI2x4 - cchNaI1x4))
				* cNaIADC[4]
				+ (cenNaI1 - cchNaI1x4
						* ((cenNaI2 - cenNaI1) / (cchNaI2x4 - cchNaI1x4)));
		eNaIADC[5] = ((cenNaI2 - cenNaI1) / (cchNaI2x5 - cchNaI1x5))
				* cNaIADC[5]
				+ (cenNaI1 - cchNaI1x5
						* ((cenNaI2 - cenNaI1) / (cchNaI2x5 - cchNaI1x5)));
		eNaIADC[6] = ((cenNaI2 - cenNaI1) / (cchNaI2x6 - cchNaI1x6))
				* cNaIADC[6]
				+ (cenNaI1 - cchNaI1x6
						* ((cenNaI2 - cenNaI1) / (cchNaI2x6 - cchNaI1x6)));
		eNaIADC[7] = ((cenNaI2 - cenNaI1) / (cchNaI2x7 - cchNaI1x7))
				* cNaIADC[7]
				+ (cenNaI1 - cchNaI1x7
						* ((cenNaI2 - cenNaI1) / (cchNaI2x7 - cchNaI1x7)));
		eNaIADC[8] = ((cenNaI2 - cenNaI1) / (cchNaI2x8 - cchNaI1x8))
				* cNaIADC[8]
				+ (cenNaI1 - cchNaI1x8
						* ((cenNaI2 - cenNaI1) / (cchNaI2x8 - cchNaI1x8)));
		eNaIADC[9] = ((cenNaI2 - cenNaI1) / (cchNaI2x9 - cchNaI1x9))
				* cNaIADC[9]
				+ (cenNaI1 - cchNaI1x9
						* ((cenNaI2 - cenNaI1) / (cchNaI2x9 - cchNaI1x9)));
		eNaIADC[10] = ((cenNaI2 - cenNaI1) / (cchNaI2x10 - cchNaI1x10))
				* cNaIADC[10]
				+ (cenNaI1 - cchNaI1x10
						* ((cenNaI2 - cenNaI1) / (cchNaI2x10 - cchNaI1x10)));
		eNaIADC[11] = ((cenNaI2 - cenNaI1) / (cchNaI2x11 - cchNaI1x11))
				* cNaIADC[11]
				+ (cenNaI1 - cchNaI1x11
						* ((cenNaI2 - cenNaI1) / (cchNaI2x11 - cchNaI1x11)));
		eNaIADC[12] = ((cenNaI2 - cenNaI1) / (cchNaI2x12 - cchNaI1x12))
				* cNaIADC[12]
				+ (cenNaI1 - cchNaI1x12
						* ((cenNaI2 - cenNaI1) / (cchNaI2x12 - cchNaI1x12)));
		eNaIADC[13] = ((cenNaI2 - cenNaI1) / (cchNaI2x13 - cchNaI1x13))
				* cNaIADC[13]
				+ (cenNaI1 - cchNaI1x13
						* ((cenNaI2 - cenNaI1) / (cchNaI2x13 - cchNaI1x13)));
		eNaIADC[14] = ((cenNaI2 - cenNaI1) / (cchNaI2x14 - cchNaI1x14))
				* cNaIADC[14]
				+ (cenNaI1 - cchNaI1x14
						* ((cenNaI2 - cenNaI1) / (cchNaI2x14 - cchNaI1x14)));
		eNaIADC[15] = ((cenNaI2 - cenNaI1) / (cchNaI2x15 - cchNaI1x15))
				* cNaIADC[15]
				+ (cenNaI1 - cchNaI1x15
						* ((cenNaI2 - cenNaI1) / (cchNaI2x15 - cchNaI1x15)));

		final double eNaISum = sumNaI();
		// scale NaI sum
		final int calibNaIsum1d = (int) Math.round(eNaISum * 0.1);

		// ----------------------------------------------------
		// Increment Spectra
		// ---------------------------------------------------

		// Singles Spectra
		hnE.inc(cnE);
		hnPSD.inc(cnPSD);
		hnTDC.inc(cnTDC);

		incrementNaI(calibNaIsum1d);

		incrementScintillator(cSciTAC);

		// Increment the 2D Neutron spectra
		h2dnEvsPSD.inc(calibnE, calibnPSD);
		h2dnEvsTDC.inc(calibnE, calibnTDC);

		// Define timing events for NaI Annulus
		// do we make it through NaI TDC gates a?
		final boolean goodTimeNaITDCa = gNaITDCa[0].inGate(cNaITDC[0])
				|| gNaITDCa[1].inGate(cNaITDC[1])
				|| gNaITDCa[2].inGate(cNaITDC[2])
				|| gNaITDCa[3].inGate(cNaITDC[3])
				|| gNaITDCa[4].inGate(cNaITDC[4])
				|| gNaITDCa[5].inGate(cNaITDC[5])
				|| gNaITDCa[6].inGate(cNaITDC[6])
				|| gNaITDCa[7].inGate(cNaITDC[7])
				|| gNaITDCa[8].inGate(cNaITDC[8])
				|| gNaITDCa[9].inGate(cNaITDC[9])
				|| gNaITDCa[10].inGate(cNaITDC[10])
				|| gNaITDCa[11].inGate(cNaITDC[11])
				|| gNaITDCa[12].inGate(cNaITDC[12])
				|| gNaITDCa[13].inGate(cNaITDC[13])
				|| gNaITDCa[14].inGate(cNaITDC[14])
				|| gNaITDCa[15].inGate(cNaITDC[15]);

		// define veto events for Scintillator
		// Energy
		final boolean vetoSciE = (gSciADC[0].inGate(cSciADC[0])
				|| gSciADC[1].inGate(cSciADC[1])
				|| gSciADC[2].inGate(cSciADC[2])
				|| gSciADC[3].inGate(cSciADC[3]) || gSciADC[4]
				.inGate(cSciADC[4]));
		// Time
		final boolean vetoSciT = gSciTAC.inGate(cSciTAC);

		incrementVetoSpectra(cnE, goodTimeNaITDCa, vetoSciE, vetoSciT);
		incrementWithin2Dgates(cnE, calibnE, calibnPSD, calibnTDC);
		// Increment neutron spectra gated on capture peak TDC (a and b)
		if (gnTDCa.inGate(cnTDC)) {
			hnE_TDCGatea.inc(cnE);
		}
		if (gnTDCb.inGate(cnTDC)) {
			hnE_TDCGateb.inc(cnE);
		}
		// And 2D gate
		if (gnEvsTDC.inGate(calibnE, calibnTDC)) {
			hnE_ETDCGate.inc(cnE);
		}
		// Increment spectrum gates on 477 gamma in NaI AND in the NaI TDC
		if (goodTimeNaITDCa && gNaIsum.inGate(calibNaIsum1d)) {
			hnE_NaIE.inc(cnE);
		}
	}

	private void incrementWithin2Dgates(final int cnE, final int calibnE,
			final int calibnPSD, final int calibnTDC) {
		// Now increment spectra depending on 2D PSD gates
		if (gnEvsPSDa.inGate(calibnE, calibnPSD)) {
			hnE_PSDGatea.inc(cnE);
		}
		if (gnEvsPSDb.inGate(calibnE, calibnPSD)) {
			hnE_PSDGateb.inc(cnE);
		}
		if (gnEvsPSDa.inGate(calibnE, calibnPSD)) {
			h2dnEvsTDC_PSD.inc(calibnE, calibnTDC);
		}
	}

	private void incrementVetoSpectra(final int cnE,
			final boolean goodTimeNaITDCa, final boolean vetoSciE,
			final boolean vetoSciT) {
		// Now increment the vetoed spectra if no veto flags
		if (!goodTimeNaITDCa) {
			hnE_NaIVeto.inc(cnE);
		}

		if (!vetoSciE) {
			hnE_SciVetoE.inc(cnE);
		}
		if (!vetoSciT) {
			hnE_SciVetoT.inc(cnE);
		}
		if (!vetoSciE && !goodTimeNaITDCa) {
			hnE_SciNaIVeto.inc(cnE);
		}
	}

	private void incrementScintillator(final int cSciTAC) {
		hSciTAC.inc(cSciTAC);
		for (int i = 0; i < 5; i++) {
			hSciADC[i].inc(cSciADC[i]);
		}
	}

	private void incrementNaI(final int calibNaIsum1d) {
		for (int i = 0; i < 16; i++) {
			hNaIADC[i].inc(cNaIADC[i]);
			hNaITDC[i].inc(cNaITDC[i]);
		}

		// Increment NaI sum energy
		hNaIsumE.inc(calibNaIsum1d);
	}

	private double sumNaI() {
		double eNaISum = 0.0;
		// Sum NaI energies only if they are within individual NaI gates
		for (int i = 0; i < 16; i++) {
			if (gNaIADC[i].inGate(cNaIADC[i])) {
				eNaISum += eNaIADC[i];
			}
		}
		return eNaISum;
	}
}
