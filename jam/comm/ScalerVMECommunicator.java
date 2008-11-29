package jam.comm;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * Communicates about scalers with VME.
 * 
 * @author Dale Visser
 * 
 */
public class ScalerVMECommunicator implements ScalerCommunication, Observer {
	private transient final VMECommunication vme;

	@Inject
	ScalerVMECommunicator(final VMECommunication vme,
			final Broadcaster broadcaster) {
		this.vme = vme;
		broadcaster.addObserver(this);
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

	/**
	 * Receives distributed events. Can listen for broadcasted event.
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            object being observed
	 * @param message
	 *            additional parameter from <CODE>Observable</CODE> object
	 */
	public void update(final Observable observable, final Object message) {
		final BroadcastEvent event = (BroadcastEvent) message;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SCALERS_READ) {
			this.readScalers();
		} else if (command == BroadcastEvent.Command.SCALERS_CLEAR) {
			this.clearScalers();
		}
	}
}
