package jam.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Class for containing a map of the ADC and TDC modules to be addressed, along
 * with channels and thresholds.
 * </p>
 */
public final class VME_Map {

	/** Contains all event parameters. */
	private transient final List<VME_Channel> eventParams = new ArrayList<>();

	private transient final EventSizeModeClient esmClient;

	/**
	 * Interval in milliseconds for the VME to insert scalers into the event
	 * stream.
	 */
	private transient int interval = 1;

	private transient final Map<Integer, Byte> V775ranges = new HashMap<>();

	private transient int maxParamNum = 0;

	/**
	 * Constructs a new VME map for the given <code>AbstractSortRoutine</code>.
	 * 
	 * @param client
	 *            the owner of this map
	 */
	VME_Map(final EventSizeModeClient client) {
		super();
		esmClient = client;
	}

	/**
	 * Creates and adds an event parameter to the list of event parameters. V785
	 * ADC's can have base addresses from 0x20000000-0x2FFF0000. V775 TDC's can
	 * have base addresses from 0x30000000-0x3FFF0000.
	 * 
	 * @param slot
	 *            slot in which the unit resides in the VME bus
	 * @param baseAddress
	 *            24-bit base address of ADC or TDC module ()
	 * @param channel
	 *            integer from 0 to 31 indicating channel in ADC or TDC
	 * @param threshold
	 *            integer from 0 to 4095 indicating lower threshold for
	 *            recording the value
	 * @return index in event array passed to <code>Sorter</code> containing
	 *         this parameter's data
	 * @throws SortException
	 *             if there's a problem defining the parameter
	 */
	public int eventParameter(final int slot, final int baseAddress,
			final int channel, final int threshold) throws SortException {
		final VME_Channel vmec = new VME_Channel(/* this.messages, */slot,
				baseAddress, channel, threshold);
		eventParams.add(vmec);
		esmClient.setEventSizeMode(EventSizeMode.VME_MAP);
		/*
		 * ADC's and TDC's in slots 2-7.
		 */
		final int paramNumber = vmec.getParameterNumber();
		if (paramNumber > maxParamNum) {
			maxParamNum = paramNumber;
		}
		return paramNumber;
	}

	/**
	 * Sets the time between when the front end is supposed to query the scaler
	 * module(s) and insert the scaler values as a block in the event stream.
	 * 
	 * @param seconds
	 *            between scaler blocks
	 */
	public void setScalerInterval(final int seconds) {
		interval = seconds;
	}

	/**
	 * Gets the time interval between scaler blocks in the event stream.
	 * 
	 * @return time interval in seconds
	 */
	public int getScalerInterval() {
		return interval;
	}

	/**
	 * Get the event size for a given stream.
	 * 
	 * @return number of parameters per event
	 */
	public int getEventSize() {
		return maxParamNum + 1;
	}

	/**
	 * Gets the event parameters in this map.
	 * 
	 * @return an array of the VME channels defined for this map
	 */
	public List<VME_Channel> getEventParameters() {
		return Collections.unmodifiableList(eventParams);
	}

	/**
	 * Called in a sort routine to set the range of a V775 TDC in nanoseconds.
	 * 
	 * @param baseAddress
	 *            must be 0x3???0000
	 * @param range
	 *            range in ns, between 141 and 1200
	 * @throws SortException
	 *             for an invalid given range
	 */
	public void setV775Range(final int baseAddress, final int range)
			throws SortException {
		if (range < 141 || range > 1200) {
			throw new SortException("Requested invalid TDC range: " + range
					+ " ns, must be 141 to 1200 ns.");
		}
		final byte fullScale = (byte) (36000 / range);
		V775ranges.put(baseAddress, fullScale);
	}

	/**
	 * Gets the ranges of the TDC modules as a <code>Map</code>. The keys in the
	 * map are the <code>Integer</code> base addresses of the TDC modules, and
	 * the values are the <code>Byte</code> that actually gets written to the
	 * range register in the module to set the requested range.
	 * 
	 * @return a <code>Map</code> to the time ranges in the TDC's
	 * @see #setV775Range(int, int)
	 * @see java.lang.Integer
	 * @see java.lang.Byte
	 */
	public Map<Integer, Byte> getV775Ranges() {
		return Collections.unmodifiableMap(V775ranges);
	}
}