package jam.sort;

/**
 * Represents a single channel of data that the acquisition electronics will
 * read out. Used by VME_Map to store its info.
 * 
 * @author <a href=mailto:dwvisser@users.sourceforge.net>Dale Visser</a>
 */
public final class VME_Channel {

	private transient final int slot, baseAddress, channel, threshold;

	private transient final Type type;

	private static final String S_INVALID = ".VMEChannel(): Invalid";

	/**
	 * Encapsulates whether a parameter is an event parameter or a scaler
	 * parameter.
	 */
	enum Type {
		/**
		 * event parameter type
		 */
		EVENT,
		/**
		 * scaler parameter type
		 */
		SCALER
	}

    /**
	 * Creates an event parameter. V775/V785 TDC's/ADC's can have base addresses
	 * from 0x20000000-0xe0ff0000.
	 *
	 * @param slot
	 *            the slot the module occupies in the VME crate
	 * @param baseAddress
	 *            24-bit base address of ADC or TDC module ()
	 * @param channel
	 *            integer from 0 to 31 indicating channel in ADC or TDC
	 * @param threshold
	 *            integer from 0 to 4095 indicating lower threshold for
	 *            recording the value
	 * @throws SortException
	 *             if passed invalid values
	 */
	VME_Channel(final int slot,
			final int baseAddress, final int channel, final int threshold)
			throws SortException {
		super();
		if (channel >= 0 && channel < 32) {
			this.channel = channel;
		} else {
			throw new SortException(getClass().getName() + S_INVALID
					+ " channel = " + channel);
		}
		if (slot >= 2 && slot <= 20) {// valid slots for ADC's and TDC's
			this.slot = slot;
		} else {
			throw new SortException(getClass().getName() + S_INVALID
					+ " slot = " + slot);
		}
		if (baseAddress < 0x20000000 || baseAddress > 0xe0ff0000) {// highest
			// hex digit
			// must = 2
			// or 3
			this.baseAddress = baseAddress;
		} else {
			throw new SortException(getClass().getName() + S_INVALID
					+ " base address = 0x" + Integer.toHexString(baseAddress));
		}
		if (threshold >= 0 && threshold < 4096) {
			this.threshold = (int) Math.round(threshold / 16.0);
		} else {
			throw new SortException(getClass().getName() + S_INVALID
					+ " threshold = " + threshold);
		}
		type = Type.EVENT;
	}

	/**
	 * Gets the index in the event stream that this parameter is associated
	 * with.
	 * 
	 * @return parameter number for this channel
	 */
	short getParameterNumber() {
		return (short) (channel + (slot - 2) * 32);
	}

	/**
	 * Gets the base address of the module associated with this channel.
	 * 
	 * @return the module base address
	 */
	public int getBaseAddress() {
		return baseAddress;
	}

	/**
	 * Gets the channel within the VME module for this parameter.
	 * 
	 * @return the channel within the module
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * Returns the type of parameter this is.
	 * 
	 * @return event or scaler
	 * @see Type#EVENT
	 * @see Type#SCALER
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Get the threshold channel defined for the event parameter. The threshold
	 * channel is a lower value threshold that the ADC or TDC requires for
	 * storing a parameter.
	 * 
	 * @return the lower value threshold
	 */
	public int getThreshold() {
		return threshold;
	}

	/**
	 * Gets the slot the VME module resides in.
	 * 
	 * @return the physical slot number
	 */
	public int getSlot() {
		return slot;
	}
}
