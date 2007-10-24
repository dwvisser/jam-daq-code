package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.BroadcastUtilities;

/**
 * Command for scalers
 * 
 * @author Ken Swartz
 */
public final class ScalersCmd extends AbstractCommand {

	private static final int READ = 1;

	private static final int ZERO = 2;

	/**
	 * Default constructor.
	 * 
	 */
	public ScalersCmd() {
		super();
		putValue(SHORT_DESCRIPTION,
				"Read or zero scalers, depending on parameter.");
	}

	protected void execute(final Object[] cmdParams) {
		final int param = ((Integer) cmdParams[0]).intValue();
		if (param == READ) {
			readScalers();
		} else if (param == ZERO) {
			BroadcastUtilities.zeroScalers();
		} else {
			LOGGER
					.severe("Incomplete command: need 'scaler zero' or 'scaler read'.");
		}
	}

	protected void executeParse(final String[] cmdTokens) {
		final Object[] params = new Object[1];
		if (cmdTokens[0].equals("read")) {
			params[0] = Integer.valueOf(READ);
		} else if (cmdTokens[0].equals("zero")) {
			params[0] = Integer.valueOf(ZERO);
		}
		execute(params);
	}

	/**
	 * Does the scaler reading.
	 */
	private void readScalers() {
		if (STATUS.isOnline()) {
			BROADCASTER.broadcast(BroadcastEvent.Command.SCALERS_READ);
		}
	}
}
