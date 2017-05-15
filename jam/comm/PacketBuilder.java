package jam.comm;

import com.google.inject.Inject;
import jam.util.StringUtilities;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Worker class that creates standard Jam UDP packets.
 * @author Dale Visser
 */
final class PacketBuilder {
    private final transient StringUtilities stringUtilities;

    @Inject
    protected PacketBuilder(final StringUtilities stringUtilities) {
        super();
        this.stringUtilities = stringUtilities;
    }

    protected DatagramPacket message(final PacketTypes status,
            final String message, final InetAddress address, final int port) {
        final int intAndByteLen = 5;
        final byte[] byteMessage = new byte[message.length() + intAndByteLen];
        final ByteBuffer byteBuff = ByteBuffer.wrap(byteMessage);
        byteBuff.putInt(status.intValue());
        byteBuff.put(this.stringUtilities.getASCIIarray(message));
        byteBuff.put(Constants.STRING_NULL);
        return new DatagramPacket(byteMessage, byteMessage.length, address,
                port);
    }

}
