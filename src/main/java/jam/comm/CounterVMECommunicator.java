package jam.comm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

final class CounterVMECommunicator implements PropertyChangeListener {

	private transient final VmeSender vme;

	CounterVMECommunicator(final VmeSender vme, final Broadcaster broadcaster) {
		this.vme = vme;
		broadcaster.addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final BroadcastEvent.Command command = ((BroadcastEvent) evt).getCommand();
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
		this.vme.sendMessage(COUNT_ZERO);
	}

	/**
	 * Tells the VME to read the counters, and send back two packets: the packet
	 * with the counter values, and a status packet (read OK or ERROR) with a
	 * message.
	 */
	private void readCounters() {
		final String COUNT_READ = "count read";
		this.vme.sendMessage(COUNT_READ);
	}
}
