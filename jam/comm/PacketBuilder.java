package jam.comm;

import jam.util.StringUtilities;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import com.google.inject.Inject;

/**
 * Worker class that creates standard Jam UDP packets.
 * 
 * @author Dale Visser
 * 
 */
final class PacketBuilder {
	private transient final StringUtilities stringUtilities;

	@Inject
	protected PacketBuilder(final StringUtilities stringUtilities) {
		super();
		this.stringUtilities = stringUtilities;
	}

	protected DatagramPacket message(final PacketTypes status,
			final String message, final InetAddress address, final int port) {
		final byte[] byteMessage = new byte[message.length() + 5];
		final ByteBuffer byteBuff = ByteBuffer.wrap(byteMessage);
		byteBuff.putInt(status.intValue());
		byteBuff.put(this.stringUtilities.getASCIIarray(message));
		byteBuff.put(Constants.STRING_NULL);
		return new DatagramPacket(byteMessage, byteMessage.length, address,
				port);
	}

}
