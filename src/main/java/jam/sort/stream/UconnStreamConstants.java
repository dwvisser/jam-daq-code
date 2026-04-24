package jam.sort.stream;

final class UconnStreamConstants {

  UconnStreamConstants() {
    super();
  }

  /** */
  public static final int ADC_CHAN_MASK = 0x07;

  /** */
  public static final int ADC_CHAN_SHFT = 12;

  /** */
  public static final int ADC_DATA_MASK = 0xFFF;

  /** how much to offset data for */
  public static final int ADC_OFFSET = 8;

  /** size of header in 2 byte words */
  public static final int HEAD_SIZE = 5;

  /** */
  public static final int NUMBER_SCALERS = 12;

  /** */
  public static final int SCALER_MASK = 0x00ffffff;

  /** */
  public static final int TDC_CHAN_MASK = 0x1F;

  /** */
  public static final int TDC_CHAN_SHFT = 10;

  /** */
  public static final int TDC_DATA_MASK = 0x3FF;

  /** how much to offset data for */
  public static final int TDC_OFFSET = 32;

  /** */
  public static final int VSN_MARKER = 0x8000;

  /** */
  public static final int VSN_MASK = 0xFF;

  /** */
  public static final int VSN_TDC = 0x4;
}
