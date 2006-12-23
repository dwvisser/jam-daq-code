package jam.sort.stream;

final class CAEN_StreamFields {
	/**
	 * at end of normal buffers
	 */
	static final int BUFFER_END = 0x01bbbbbb;

	/**
	 * buffer padding when user stops acquisition
	 */
	static final int STOP_PAD = 0x01DDDDDD;

	/**
	 * buffer padding when user ends run
	 */
	static final int END_PAD = 0x01EEEEEE;

	/**
	 * default buffer padding
	 */
	static final int BUFFER_PAD = 0x01FFFFFF;

	/**
	 * scaler block header
	 */
	static final int SCALER_BLOCK = 0x01CCCCCC;

	/** 20 slots in a VME crate - 1 controller slot */
	static final int N_V7X5_UNITS = 19;

	/**
	 * total number of channels in all V7x5 units
	 */
	static final int NUM_CHANNELS = N_V7X5_UNITS * 32;

	/**
	 * reconstruction buffer size
	 */
	static final int BUFFER_DEPTH = 100;
}
