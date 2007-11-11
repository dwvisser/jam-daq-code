package jam.global;

import java.util.logging.Logger;

public class BroadcastUtilities {
	
	private static final Broadcaster INSTANCE = Broadcaster.getSingletonInstance();
	
	private static final Logger LOGGER = Logger.getLogger(BroadcastUtilities.class
			.getPackage().getName());
	/**
	 * Does the scaler zeroing.
	 */
	public static void zeroScalers() {
		if (JamStatus.getSingletonInstance().isOnline()) {
			INSTANCE.broadcast(BroadcastEvent.Command.SCALERS_CLEAR);
			INSTANCE.broadcast(BroadcastEvent.Command.SCALERS_READ);
		} else {
			LOGGER.severe("Can only Zero Scalers when in Online mode.");
		}
	}

	private BroadcastUtilities(){
		// Only static methods, so suppress creation of instances.
	}

}
