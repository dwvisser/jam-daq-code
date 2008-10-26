package jam.comm;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

import java.util.Observable;
import java.util.Observer;

final class CounterVMECommunicator implements Observer {

	private transient final VMECommunication vme;

	CounterVMECommunicator(final VMECommunication vme) {
		this.vme = vme;
		Broadcaster.getSingletonInstance().addObserver(this);
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
		if (command == BroadcastEvent.Command.COUNTERS_READ) {
			readCounters();
		} else if (command == BroadcastEvent.Command.COUNTERS_ZERO) {
			zeroCounters();
		}
	}

	/**
	 * Tells the VME to zero its counters and send a reply: OK or ERROR.
	 */
	private void zeroCounters() {
		final String COUNT_ZERO = "count zero";
		this.vme.sendToVME(COUNT_ZERO);
	}

	/**
	 * Tells the VME to read the counters, and send back two packets: the packet
	 * with the counter values, and a status packet (read OK or ERROR) with a
	 * message.
	 */
	private void readCounters() {
		final String COUNT_READ = "count read";
		this.vme.sendToVME(COUNT_READ);
	}
}
