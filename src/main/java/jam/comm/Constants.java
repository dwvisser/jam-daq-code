package jam.comm;

/**
 * Parameters defining the communications protocol.
 * @author Dale Visser
 */
public final class Constants {
	/**
	 * maximum message size in bytes
	 */
	public static final int MAX_MESSAGE_SIZE = 80;

	/**
	 * maximum data packet size in bytes
	 */
	public static final int MAX_DATA_SIZE = 8192;

	/**
	 * byte for null-terminating ASCII strings in packets
	 */
	public static final byte STRING_NULL = (byte) 0x0;
	
	/**
	 * maximum size in bytes of a single UDP packet
	 */
	public static final int MAX_PACKET_SIZE = 1024;


	private Constants() {
		super();
	}
}
