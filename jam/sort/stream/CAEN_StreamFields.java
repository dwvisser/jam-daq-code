package jam.sort.stream;

interface CAEN_StreamFields {
	/**
	 * at end of normal buffers
	 */
	int BUFFER_END = 0x01bbbbbb;

	/**
	 * buffer padding when user stops acquisition
	 */
	int STOP_PAD = 0x01DDDDDD;

	/**
	 * buffer padding when user ends run
	 */
	int END_PAD = 0x01EEEEEE;

	/**
	 * default buffer padding
	 */
	int BUFFER_PAD = 0x01FFFFFF;

	/**
	 * scaler block header
	 */
	int SCALER_BLOCK = 0x01CCCCCC;

	/** 20 slots in a VME crate - 1 controller slot */
	int N_V7X5_UNITS = 19;

	/**
	 * total number of channels in all V7x5 units
	 */
	int NUM_CHANNELS = N_V7X5_UNITS * 32;

	/**
	 * reconstruction buffer size
	 */
	int BUFFER_DEPTH = 100;
}
