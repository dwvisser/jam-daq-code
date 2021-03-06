package help.sortfiles;

import jam.data.Gate;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.data.Monitor;
import jam.data.Scaler;
import jam.sort.SortException;
import jam.sort.AbstractSortRoutine;

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
public class EvsDE extends AbstractSortRoutine {

	/* histograms */
	private transient final HistInt1D hEnergy, hDE, hSum, hSumGate;
	
	private transient final HistInt2D hEvsDE;

	/* gates */
	private transient final Gate gEvsDE;

	//id numbers for the signals;
	private transient int idE, idDE; //NOPMD

	/**
	 * Constructor, not usually necessary, but be sure to
	 * call <code>super()</code>.
	 *
	 * @see #initialize()
	 */
	public EvsDE() {
		super();
		final int oneD=2048;
		final int twoD=256;
		hEnergy = createHist1D(oneD,"E", "Energy");
		/* delta-E signal */
		hDE = createHist1D(oneD, "DE", "Delta-E");
		/* Energy vs. delta-E 2-d histogram */
		hEvsDE = createHist2D(twoD, "EvsDE", "E vs Delta E", "Energy",
				"Delta Energy");
		/* Energy plus delta-E */
		hSum = createHist1D(oneD, "sum", "Energy Sum");
		/* Energy plus delta-E gated on particle ID */
		hSumGate = createHist1D(oneD, "sumGate", "Gated Energy Sum");
		/* Particle ID gate */
		gEvsDE = new Gate("PID", hEvsDE);
		/* Integrated beam current (BIC) */
		final Scaler sBeam = createScaler("Beam", 0);
		/* A clock */
		final Scaler sClck = createScaler("Clock", 1);
		/* Total events seen */
		final Scaler sEvntRaw = createScaler("Event Raw", 2);
		/* Total events used */
		createScaler("Event Accept", 3);
		/* Monitor of rate of the BIC scaler */
		new Monitor("Beam ", sBeam);
		/* Monitor of the rate of the clock */
		new Monitor("Clock", sClck);
		/* Moniter of the rate of accepted events */
		new Monitor("Event Rate", sEvntRaw);
	}

	/**
	 * @see AbstractSortRoutine#initialize()
	 */
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
	 * @see AbstractSortRoutine#sort(int[])
	 */
	public void sort(final int[] dataEvent) {
		/* Variables for the raw signal values. */
		final int energy = dataEvent[idE];
		final int eDE = dataEvent[idDE];
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