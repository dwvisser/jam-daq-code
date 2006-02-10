package jam.comm;

import jam.util.StringUtilities;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Worker class that creates standard Jam UDP packets.
 * 
 * @author Dale Visser
 * 
 */
public final class PacketBuilder {
	private static final PacketBuilder INSTANCE = new PacketBuilder();

	private PacketBuilder() {
		super();
	}

	static PacketBuilder getInstance() {
		return INSTANCE;
	}

	DatagramPacket message(final PacketTypes status, final String message,
			final InetAddress address, final int port) {
		final byte[] byteMessage = new byte[message.length() + 5];
		final ByteBuffer byteBuff = ByteBuffer.wrap(byteMessage);
		byteBuff.putInt(status.intValue());
		byteBuff.put(StringUtilities.getInstance().getASCIIarray(message));
		byteBuff.put(Constants.STRING_NULL);
		return new DatagramPacket(byteMessage, byteMessage.length, address,
				port);
	}

}
