package help.sortfiles;

import jam.data.DataParameter;
import jam.data.Gate;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.sort.SortException;
import jam.sort.SortRoutine;

/**
 * Sort routine for neutron counting with the aid of an anti-coincidence muon
 * shield and a NaI Annulus.
 * 
 * @author R. Longland
 */
public class YaleCAENTestSortRoutine extends SortRoutine {

	// -----------------------------------------------
	// General Definitions
	// -----------------------------------------------

	private static final String NA_I_TDC = "NaI TDC ";

	// Number of channels for spectra and per dimension in 2D spectra

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

	private transient DataParameter pcenNaI1sum;
	private transient DataParameter pcenNaI2sum;
	private transient final DataParameter pcchNaI1[] = new DataParameter[16];
	private transient final DataParameter pcchNaI2[] = new DataParameter[16];

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

	// BC523A spectrum gated on NaI 477 keV gamma-ray
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
		pcenNaI1sum = createParameter("cenNaI1");
		pcenNaI2sum = createParameter("cenNaI2");

		for (int i = 0; i < 16; i++) {
			pcchNaI1[i] = createParameter("cchNaI1_" + i);
			pcchNaI2[i] = createParameter("cchNaI2_" + i);
		}
	}

	private void setupHistograms() {
		// ------------------------------------------
		// Set up the histograms in order they will be seen
		// args = (spectrum name,1d/2d, no. of channels, title, x-label,
		// y-label)
		// -----------------------------------------

		final int SPC_CHANNELS = 4096;
		hnE = createHist1D(SPC_CHANNELS, "Neutron E", "Neutron E Singles");
		hnPSD = createHist1D(SPC_CHANNELS, "Neutron PSD", "Neutron PSD");
		hnTDC = createHist1D(SPC_CHANNELS, "Capture Times",
				"Neutron Capture Peak");

		final int TWO_D_CHANNELS = 512;
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
		final int TDC_BASE = 0xfac00000;
		final int TIME_RANGE = 1200; // ns
		vmeMap.setV775Range(TDC_BASE, TIME_RANGE);

		// --------------------------------------------------------------
		// Set up the mapping for each used channel of the ADC and TDC
		// to the appropriate id eventParameters,
		// args = (slot, base address, channel, threshold channel)
		// --------------------------------------------------------------

		final int ADC_BASE_FIRST = 0xfaa00000;
		final int THRESHOLDS = 50;
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
		final int ADC_BASE_SECOND = 0xfa800000;
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

		final double eNaISum = sumNaI(calculateCalibratedEnergies());
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
		// Compress Neutron energies by factor of 20 for 2D spectra
		// Change cnE to enE once parameters have been found.
		final int calibnE = (int) Math.round(cnE * 0.1);
		final int calibnPSD = (int) Math.round(cnPSD * 0.1);
		final int calibnTDC = (int) Math.round(cnTDC * 0.1);
		h2dnEvsPSD.inc(calibnE, calibnPSD);
		h2dnEvsTDC.inc(calibnE, calibnTDC);

		final boolean goodTimeNaITDCa = goodTimeNaITDCa();

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

	private double[] calculateCalibratedEnergies() {
		final double eNaIADC[] = new double[16]; // NOPMD
		// Parameters for energy calibrations
		final double cenNaI1 = pcenNaI1sum.getValue();
		final double cenNaI2 = pcenNaI2sum.getValue();
		// Calibrate NaI
		final double diff12 = (cenNaI2 - cenNaI1);
		for (int i = 0; i < 16; i++) {
			final double cchNaI1 = pcchNaI1[i].getValue();
			final double cchNaI2 = pcchNaI2[i].getValue();
			final double segmentDiff12 = cchNaI2 - cchNaI1;
			eNaIADC[i] = (diff12 / segmentDiff12) * cNaIADC[i]
					+ (cenNaI1 - cchNaI1 * (diff12 / segmentDiff12));
		}
		return eNaIADC;
	}

	private boolean goodTimeNaITDCa() {
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
		return goodTimeNaITDCa;
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

	private double sumNaI(final double[] eNaIADC) {
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
