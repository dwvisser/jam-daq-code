package help.sortfiles;

import jam.data.Gate;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.data.Monitor;
import jam.data.Scaler;
import jam.sort.AbstractSortRoutine;
import jam.sort.SortException;

/**
 * Sort file for 3 singles channels of data
 * 
 * Each event is 2 channel numbered 0 through 7 and for each event we increment
 * all 2 histograms. We do not do zero suppression so will would be a lot of
 * zeros in the histograms convention for 2 d Histograms x first then y (x vs y)
 * 
 * @Author Ken Swartz , Erik Swanson & Cristina Bordeanu October 2004
 * @Edited by Tom Brown March 2006
 * @version Jam version 0.5 November 98
 */
public class CamacScalerTest extends AbstractSortRoutine {// NOPMD

	// histograms
	private transient HistInt1D hCh0, hCh1, hCh2, hCh3, hCh4;
	private transient HistInt1D hCh5; // TAC from detector #1
	private transient HistInt1D hCh6; // TAC from detector #2
	private transient HistInt1D ampGateP1;
	private transient HistInt1D apgGateP2;
	private transient HistInt1D ampGateR1;
	private transient HistInt1D ampGateR2;
	private transient HistInt2D gevsTAC1;
	private transient HistInt2D gevsTAC2;
	private transient HistInt1D ampGateSpectrum1;
	private transient HistInt1D ampGateSpectrum2;
	private transient HistInt1D rejectGeP1;
	private transient HistInt1D rejectGeP2;
	private transient HistInt1D rejectGeR1;
	private transient HistInt1D rejectGeR2;

	private transient Gate tacGateP1; // prompt TAC peak from detector #1
	private transient Gate tacGateR1; // random TAC region from detector #1
	private transient Gate tacCateP2; // prompt TAC peak from detector #2
	private transient Gate tacGateR2; // random TAC region from detector #2
	private transient Gate ampGate1;// gates on Ge #1 to generate TAC
	private transient Gate ampGate2;// gates on Ge #2 to generate TAC

	// event position id's
	private transient int idCh0, idCh1, idCh2, idCh4, idCh5;

	/**
	 * Initialize the data areas, histograms
	 * 
	 */
	@Override
	public void initialize() throws SortException {
		defineCamacInitSequence();
		// event return id number to be used in sort
		cnafCommands.eventRead(1, 9, 0, 2); // read FIFO ms time reg
		cnafCommands.eventRead(1, 9, 0, 2); // read FIFO ls time reg
		idCh0 = cnafCommands.eventRead(1, 9, 0, 2); // read FIFO 1st ADC ch 0
		idCh1 = cnafCommands.eventRead(1, 9, 0, 2); // read FIFO 1st ADC ch 1
		idCh2 = cnafCommands.eventRead(1, 9, 0, 2); // read FIFO 1st ADC ch 2
		cnafCommands.eventRead(1, 9, 0, 2); // read FIFO 1st ADC ch 3
		idCh4 = cnafCommands.eventRead(1, 9, 0, 2); // read FIFO 2nd ADC ch 0
		idCh5 = cnafCommands.eventRead(1, 9, 0, 2); // read FIFO 2nd ADC ch 1
		cnafCommands.eventRead(1, 9, 0, 2); // read FIFO 2nd ADC ch 2
		cnafCommands.eventRead(1, 9, 0, 2); // read FIFO 2nd ADC ch 3
		cnafCommands.scaler(1, 16, 0, 0); // read pulser scaler
		cnafCommands.clear(1, 16, 0, 9); // clear scaler

		// create histogram
		hCh0 = createHist1D(8192, "Ge #1", "Ge signal from detector #1");
		hCh1 = createHist1D(8192, "Ge #2", "Ge signal from detector #2");
		rejectGeP1 = createHist1D(8192, "Ge #1 after reject:P",
				"Ge signal from detector #1 after rejection from prompt");
		rejectGeP2 = createHist1D(8192, "Ge #2 after reject:P",
				"Ge signal from detector #2 after rejection from prompt");
		rejectGeR1 = createHist1D(8192, "Ge #1 after reject:R",
				"Ge signal from detector #1 after rejection from random");
		rejectGeR2 = createHist1D(8192, "Ge #2 after reject:R",
				"Ge signal from detector #2 after rejection from random");
		hCh2 = createHist1D(8192, "Pulser #1",
				"Pulser signal through detector #1");
		hCh3 = createHist1D(8192, "Pulser #2",
				"Pulser signal through detector #2");
		hCh4 = createHist1D(8192, "Direct_Pulser", "Direct pulser signal");
		hCh5 = createHist1D(8192, "TAC #1", "TAC signal from detector #1");
		hCh6 = createHist1D(8192, "TAC #2", "TAC signal from detector #2");
		ampGateP1 = createHist1D(8192, "AmpGatePrompt #1",
				"Ampl Gate Prompt from detector #1");
		apgGateP2 = createHist1D(8192, "AmpGatePrompt #2",
				"Ampl Gate Prompt from detector #2");
		ampGateR1 = createHist1D(8192, "AmpGateRandom #1",
				"Ampl Gate Random from detector #1");
		ampGateR2 = createHist1D(8192, "AmpGateRandom #2",
				"Ampl Gate Random from detector #2");
		gevsTAC1 = createHist2D(512, 128, "Ampl vs TAC #1", "Ampl vs TAC #1",
				"TAC #1", "Ampl");
		gevsTAC2 = createHist2D(512, 128, "Ampl vs TAC #2", "Ampl vs TAC #2",
				"TAC #2", "Ampl");
		ampGateSpectrum1 = createHist1D(8192, "TAC from Ge #1",
				"TAC signal from gate on Ge #1");
		ampGateSpectrum2 = createHist1D(8192, "TAC from Ge #2",
				"TAC signal from gate on Ge #2");

		tacGateP1 = new Gate("Prompt TAC peak from detector #1", hCh5);
		tacGateR1 = new Gate("Random TAC region from detector #1", hCh5);
		tacCateP2 = new Gate("Prompt TAC peak from detector #2", hCh6);
		tacGateR2 = new Gate("Random TAC region from detector #2", hCh6);

		ampGate1 = new Gate("Gate for TAC 0", hCh0);
		ampGate2 = new Gate("Gate for TAC 1", hCh1);

		new Gate("Gate for TAC", ampGateSpectrum1);
		new Gate("Gate for TAC", ampGateSpectrum2);

		new Gate("Gate for Energy 0", hCh0);
		new Gate("Gate for Energy 1", hCh1);

		new Gate("Gate for Energy 1", ampGateP1);
		new Gate("Gate for Energy 2", apgGateP2);

		new Gate("Narrow", hCh2);
		new Gate("Narrow", hCh3);
		new Gate("Wide", hCh2);
		new Gate("Wide", hCh3);

		new Gate("Gate for Energy", rejectGeP1);
		new Gate("Gate for Energy", rejectGeP2);

		final Scaler sPulser = createScaler("Direct pulser", 0);

		new Monitor("Direct Pulser", sPulser);
	}

	private void defineCamacInitSequence() {
		/* Slot 40 is virtual CAMAC device in CC32 process. */
		cnafCommands.init(1, 40, 9, 16, 2); // CMC203 in slot 9, mode - 2
		cnafCommands.init(1, 40, 0, 17, 50); // check buffer every 50ms
		cnafCommands.init(1, 40, 0, 18, 0x0011); // CMC203 Header 0x0011
		cnafCommands.init(1, 0, 1, 16, 0); // crate dataway Z
		cnafCommands.init(1, 0, 0, 16, 0); // crate dataway C
		cnafCommands.init(1, 27, 0, 16, 0); // crate I
		cnafCommands.init(1, 12, 0, 9, 0); // adc 413 clear
		cnafCommands.init(1, 14, 0, 9, 0); // adc 413 clear

		/* Set up CMC203 FERA bus controller in FIFO mode slot 8. */
		cnafCommands.init(1, 9, 1, 16, 0x0099B); // cmc203 ctrl reg 0000 1001
		/*
		 * 1001 1011 - insert gate header
		 */
		cnafCommands.init(1, 9, 1, 9); // cmc203 Clear FIFO and counters
		cnafCommands.init(1, 9, 7, 16, 900); // cmc203 Gate to REQ timeout
		cnafCommands.init(1, 9, 14, 16, 100); // cmc203 Event to Clear timeout
		cnafCommands.init(1, 9, 1, 26); // cmc203 Enable if no inhibit
		/*
		 * cmc203 Ext Output sel reg:S for busy
		 */
		cnafCommands.init(1, 9, 13, 16, 1472);
		/*
		 * cmc203 Request delay reg(13->0.5usec)
		 */
		cnafCommands.init(1, 9, 2, 16, 50);
		/*
		 * cmc203 12 bit gate header value
		 */
		cnafCommands.init(1, 9, 9, 16, 0x011);
		cnafCommands.init(1, 9, 6, 17, 4000); // cmc203 clock tick - 80 usecs
		cnafCommands.init(1, 12, 0, 16, 33024); // reg1 adc 413 no-LAM, Coinc,
		// FERA readout
		cnafCommands.init(1, 12, 1, 16, 15); // reg2 adc 413 enable all gates
		// (1,2,3,4 & master)
		cnafCommands.init(1, 14, 0, 16, 33024);
		cnafCommands.init(1, 14, 1, 16, 15);
		cnafCommands.init(1, 12, 0, 17, 40); // adc 413 (1) LLD threshold at
		// 80 mV
		cnafCommands.init(1, 12, 1, 17, 40); // adc 413 (1) LLD threshold at
		// 80 mV
		cnafCommands.init(1, 12, 2, 17, 40); // adc 413 (1) LLD threshold at
		// 80 mV
		cnafCommands.init(1, 12, 3, 17, 40); // adc 413 (1) LLD threshold at
		// 80 mV
		cnafCommands.init(1, 14, 0, 17, 40); // adc 413 (1) LLD threshold at
		// 80 mV
		cnafCommands.init(1, 14, 1, 17, 40); // adc 413 (1) LLD threshold at
		// 80 mV
		cnafCommands.init(1, 14, 2, 17, 40); // adc 413 (1) LLD threshold at
		// 80 mV
		cnafCommands.init(1, 14, 3, 17, 40); // adc 413 (1) LLD threshold at
		// 80 mV
	}

	@Override
	public void sort(final int[] dataEvent) {

		// put event data into easy to remember names
		final int eCh0 = dataEvent[idCh0];// Ge #1
		final int eCh1 = dataEvent[idCh1];// TAC #1
		final int eCh2 = dataEvent[idCh2];// pulser
		final int eCh4 = dataEvent[idCh4];// Ge #2
		final int eCh5 = dataEvent[idCh5];// TAC #2

		// increment the TAC spectra
		hCh5.inc(eCh1);
		hCh6.inc(eCh5);

		if (eCh2 <= 0) {
			hCh0.inc(eCh0);
			hCh1.inc(eCh4);

			this.incrementIfInGate(tacGateP1, ampGateP1, eCh0, eCh1);
			this.incrementIfInGate(tacGateR1, ampGateR1, eCh0, eCh1);
			this.incrementIfInGate(tacCateP2, apgGateP2, eCh4, eCh5);
			this.incrementIfInGate(tacGateR2, ampGateR2, eCh4, eCh5);
			this.incrementIfInGate(ampGate1, ampGateSpectrum1, eCh1, eCh0);
			this.incrementIfInGate(ampGate2, ampGateSpectrum2, eCh5, eCh4);
			if (!tacGateP1.inGate(eCh1)) {
				rejectGeP1.inc(eCh0);
			}

			if (!tacCateP2.inGate(eCh5)) {
				rejectGeP2.inc(eCh4);
			}

			if (!tacGateR1.inGate(eCh1)) {
				rejectGeR1.inc(eCh0);
			}

			if (!tacGateR2.inGate(eCh5)) {
				rejectGeR2.inc(eCh4);
			}
		} else {
			hCh2.inc(eCh0);
			hCh3.inc(eCh4);
			hCh4.inc(eCh2);
		}
		// increment the 2d spectra
		if (eCh1 > 0) {
			gevsTAC1.inc(eCh1 >> 4, eCh0 >> 6);
		}
		if (eCh5 > 0) {
			gevsTAC2.inc(eCh5 >> 4, eCh4 >> 6);
		}

	}

	private void incrementIfInGate(final Gate gate, final HistInt1D histogram,
			final int incrementChannel, final int checkChannel) {
		if (gate.inGate(checkChannel)) {
			histogram.inc(incrementChannel);
		}
	}
}
