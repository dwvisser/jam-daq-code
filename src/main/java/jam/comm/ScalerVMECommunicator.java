package jam.comm;

import com.google.inject.Inject;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Communicates about scalers with VME.
 * 
 * @author Dale Visser
 * 
 */
public class ScalerVMECommunicator implements ScalerCommunication, PropertyChangeListener {
	private transient final VMECommunication vme;

	@Inject
	ScalerVMECommunicator(final VMECommunication vme,
			final Broadcaster broadcaster) {
		this.vme = vme;
		broadcaster.addPropertyChangeListener(this);
	}

	/**
	 * Tell the VME to read the scalers, and send back two packets: the packet
	 * with the scaler values, and a status packet (read OK or ERROR) with a
	 * message.
	 */
	public void readScalers() {
		final String RUN_SCALER = "list scaler";
		this.vme.sendMessage(RUN_SCALER);
	}

	/**
	 * Tells the VME to clear the scalers and send a reply: OK or ERROR.
	 */
	public void clearScalers() {
		final String RUN_CLEAR = "list clear";
		this.vme.sendMessage(RUN_CLEAR);
	}

	/**
	 * Send the interval in seconds between scaler blocks in the event stream.
	 * 
	 * @param seconds
	 *            the interval between scaler blocks
	 */
	public void sendScalerInterval(final int seconds) {
		final String message = seconds + "\n";
		this.vme.sendToVME(PacketTypes.INTERVAL, message);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final BroadcastEvent.Command command = ((BroadcastEvent) evt).getCommand();
		if (command == BroadcastEvent.Command.SCALERS_READ) {
			this.readScalers();
		} else if (command == BroadcastEvent.Command.SCALERS_CLEAR) {
			this.clearScalers();
		}
	}
}
