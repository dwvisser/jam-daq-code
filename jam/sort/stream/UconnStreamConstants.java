package jam.sort.stream;

final class UconnStreamConstants {
	
	UconnStreamConstants(){
		super();
	}

	/**
	 * 
	 */
	static public final int ADC_CHAN_MASK = 0x07;

	/**
	 * 
	 */
	static public final int ADC_CHAN_SHFT = 12;

	/**
	 * 
	 */
	static public final int ADC_DATA_MASK = 0xFFF;

	/**
	 * how much to offset data for
	 */
	static public final int ADC_OFFSET = 8;

	/**
	 * size of header in 2 byte words
	 */
	static public final int HEAD_SIZE = 5; 

	/**
	 * 
	 */
	static public final int NUMBER_SCALERS = 12;

	/**
	 * 
	 */
	static public final int SCALER_MASK = 0x00ffffff;

	/**
	 * 
	 */
	static public final int TDC_CHAN_MASK = 0x1F;

	/**
	 * 
	 */
	static public final int TDC_CHAN_SHFT = 10;

	/**
	 * 
	 */
	static public final int TDC_DATA_MASK = 0x3FF;

	/**
	 * how much to offset data for
	 */
	static public final int TDC_OFFSET = 32;

	/**
	 * 
	 */
	static public final int VSN_MARKER = 0x8000;

	/**
	 * 
	 */
	static public final int VSN_MASK = 0xFF;

	/**
	 * 
	 */
	static public final int VSN_TDC = 0x4;
}
