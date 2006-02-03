package jam.sort.stream;

interface UconnStreamConstants {

	/**
	 * 
	 */
	int ADC_CHAN_MASK = 0x07;

	/**
	 * 
	 */
	int ADC_CHAN_SHFT = 12;

	/**
	 * 
	 */
	int ADC_DATA_MASK = 0xFFF;

	/**
	 * how much to offset data for
	 */
	int ADC_OFFSET = 8;

	/**
	 * size of header in 2 byte words
	 */
	int HEAD_SIZE = 5; 

	/**
	 * 
	 */
	int NUMBER_SCALERS = 12;

	/**
	 * 
	 */
	int SCALER_MASK = 0x00ffffff;

	/**
	 * 
	 */
	int TDC_CHAN_MASK = 0x1F;

	/**
	 * 
	 */
	int TDC_CHAN_SHFT = 10;

	/**
	 * 
	 */
	int TDC_DATA_MASK = 0x3FF;

	/**
	 * how much to offset data for
	 */
	int TDC_OFFSET = 32;

	/**
	 * 
	 */
	int VSN_MARKER = 0x8000;

	/**
	 * 
	 */
	int VSN_MASK = 0xFF;

	/**
	 * 
	 */
	int VSN_TDC = 0x4;

}
