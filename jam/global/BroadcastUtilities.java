package jam.global;

import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Global utility methods for broadcasting messages.
 * 
 * @author Dale Visser
 * 
 */
@Singleton
public final class BroadcastUtilities {

	private transient final Broadcaster broadcaster;

	private static final Logger LOGGER = Logger
			.getLogger(BroadcastUtilities.class.getPackage().getName());

	private transient final JamStatus status;

	/**
	 * @param status
	 *            application status
	 * @param broadcaster
	 *            broadcaster
	 */
	@Inject
	public BroadcastUtilities(final JamStatus status,
			final Broadcaster broadcaster) {
		this.status = status;
		this.broadcaster = broadcaster;
	}

	/**
	 * Does the scaler zeroing.
	 */
	public void zeroScalers() {
		if (this.status.isOnline()) {
			broadcaster.broadcast(BroadcastEvent.Command.SCALERS_CLEAR);
			broadcaster.broadcast(BroadcastEvent.Command.SCALERS_READ);
		} else {
			LOGGER.severe("Can only Zero Scalers when in Online mode.");
		}
	}
}
