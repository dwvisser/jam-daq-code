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

	private static final Broadcaster INSTANCE = Broadcaster
			.getSingletonInstance();

	private static final Logger LOGGER = Logger
			.getLogger(BroadcastUtilities.class.getPackage().getName());

	private transient final JamStatus status;

	/**
	 * @param status
	 *            application status
	 */
	@Inject
	public BroadcastUtilities(final JamStatus status) {
		this.status = status;
	}

	/**
	 * Does the scaler zeroing.
	 */
	public void zeroScalers() {
		if (this.status.isOnline()) {
			INSTANCE.broadcast(BroadcastEvent.Command.SCALERS_CLEAR);
			INSTANCE.broadcast(BroadcastEvent.Command.SCALERS_READ);
		} else {
			LOGGER.severe("Can only Zero Scalers when in Online mode.");
		}
	}
}
