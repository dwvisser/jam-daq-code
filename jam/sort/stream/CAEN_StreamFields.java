package jam.sort.stream;

interface CAEN_StreamFields {
	int BUFFER_END = 0x01bbbbbb;// at end of normal buffers

	int STOP_PAD = 0x01DDDDDD;

	int END_PAD = 0x01EEEEEE;

	int BUFFER_PAD = 0x01FFFFFF;

	int SCALER_BLOCK = 0x01CCCCCC;

	/**	 20 slots in a VME crate - 1 controller slot */
	int N_V7X5_UNITS = 19;

	int NUM_CHANNELS = N_V7X5_UNITS * 32;

	int BUFFER_DEPTH = 100;
	
	enum FifoPointer {PUT, GET};
}
