package jam.sort.stream;

final class CAEN_StreamFields {
	
	private CAEN_StreamFields(){
		super();
	}
	
	/**
	 * at end of normal buffers
	 */
	static final int BUFFER_END = 0x01bbbbbb;//NOPMD

	/**
	 * buffer padding when user stops acquisition
	 */
	static final int STOP_PAD = 0x01DDDDDD;//NOPMD

	/**
	 * buffer padding when user ends run
	 */
	static final int END_PAD = 0x01EEEEEE;//NOPMD

	/**
	 * default buffer padding
	 */
	static final int BUFFER_PAD = 0x01FFFFFF;//NOPMD

	/**
	 * scaler block header
	 */
	static final int SCALER_BLOCK = 0x01CCCCCC;//NOPMD

	/** 20 slots in a VME crate - 1 controller slot */
	private static final int N_V7X5_UNITS = 19;

	/**
	 * total number of channels in all V7x5 units
	 */
	static final int NUM_CHANNELS = N_V7X5_UNITS * 32;//NOPMD

	/**
	 * reconstruction buffer size
	 */
	static final int BUFFER_DEPTH = 100;//NOPMD
}
