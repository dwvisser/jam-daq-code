package help.sortfiles;

import jam.data.Gate;
import jam.data.Histogram;
import jam.data.Monitor;
import jam.data.Scaler;
import jam.sort.SortException;
import jam.sort.SortRoutine;

/**
 * This is an example sort routine for Jam. It sorts for a delta-E vs. E
 * telescope. The histograms consist of 1-d histograms for both Delta E and E,
 * and a 2-d histogram which is gated on. The event data are delta-E and E pulse
 * heights. The convention for 2-d Histograms is x first, then y (x vs y).
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 0.5
 * @since JDK 1.1
 */
public class EvsDE extends SortRoutine {

	/* histograms */
	transient final Histogram hEnergy, hDE, hEvsDE, hSum, hSumGate;

	/* gates */
	transient final Gate gEvsDE;

	/* scalers */
	transient final Scaler sBeam, sClck, sEvntRaw, sEvntAccpt;

	//rate monitors
	transient final Monitor mBeam, mClck, mEvntRt;

	//id numbers for the signals;
	transient int idE;

	transient int idDE;

	public EvsDE() {
		super();
		hEnergy = new Histogram("E", HIST_1D, 2048, "Energy");
		/* delta-E signal */
		hDE = new Histogram("DE", HIST_1D, 2048, "Delta-E");
		/* Energy vs. delta-E 2-d histogram */
		hEvsDE = new Histogram("EvsDE", HIST_2D, 256, "E vs Delta E", "Energy",
				"Delta Energy");
		/* Energy plus delta-E */
		hSum = new Histogram("sum", HIST_1D, 2048, "Energy Sum");
		/* Energy plus delta-E gated on particle ID */
		hSumGate = new Histogram("sumGate", HIST_1D, 2048, "Gated Energy Sum");
		/* Particle ID gate */
		gEvsDE = new Gate("PID", hEvsDE);
		/* Integrated beam current (BIC) */
		sBeam = new Scaler("Beam", 0);
		/* A clock */
		sClck = new Scaler("Clock", 1);
		/* Total events seen */
		sEvntRaw = new Scaler("Event Raw", 2);
		/* Total events used */
		sEvntAccpt = new Scaler("Event Accept", 3);
		/* Monitor of rate of the BIC scaler */
		mBeam = new Monitor("Beam ", sBeam);
		/* Monitor of the rate of the clock */
		mClck = new Monitor("Clock", sClck);
		/* Moniter of the rate of accepted events */
		mEvntRt = new Monitor("Event Rate", sEvntRaw);
	}

	public void initialize() throws SortException {
		cnafCommands.init(1, 28, 8, 26); //crate dataway Z
		cnafCommands.init(1, 28, 9, 26); //crate dataway C
		cnafCommands.init(1, 30, 9, 26); //crate I
		cnafCommands.init(1, 3, 12, 11); //adc 811 clear

		//event return id number to be used in sort
		idE = cnafCommands.eventRead(1, 3, 0, 0); //read Energy signal
		idDE = cnafCommands.eventRead(1, 3, 1, 0); //read Delta E signal

		cnafCommands.eventCommand(1, 3, 12, 11); //clear adc

		cnafCommands.scaler(1, 5, 0, 0); //read beam scalers Joerger S12
		cnafCommands.scaler(1, 5, 1, 0); //read clock scalers Joerger S12
		cnafCommands.scaler(1, 5, 2, 0); //read event raw scalers Joerger S12
		/* read event accept scalers Joerger S12 */
		cnafCommands.scaler(1, 5, 3, 0);
		cnafCommands.clear(1, 5, 0, 9); //clear scaler
	}

	/**
	 * Sort routine
	 */
	public void sort(int[] dataEvent) {
		/* Variables for the raw signal values. */
		final int energy = dataEvent[0];
		final int eDE = dataEvent[1];
		/* Variables for the compressed version of the signal values. */
		final int ecE = energy >> 3; //compress by 8
		final int ecDE = eDE >> 3;
		/*
		 * Add the raw E and dE signal values, and renormalize to the spectrum
		 * size.
		 */
		final int sum = (energy + eDE) / 2;
		/* Increment the ungated spectra in the appropriate channels. */
		hEnergy.inc(energy);
		hDE.inc(eDE);
		hSum.inc(sum);
		/* singles 2d spectra */
		hEvsDE.inc(ecE, ecDE);
		/*
		 * Check if event is in the PID gate. If so, increment the gated
		 * histogram.
		 */
		if (gEvsDE.inGate(ecE, ecDE)) {
			hSumGate.inc(sum);
		}
	}
}