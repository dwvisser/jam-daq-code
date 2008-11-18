package jam.commands;

import injection.GuiceInjector;
import jam.global.BroadcastEvent;

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

	@Override
	/*
	 * If gets used as an Action, i.e., gets passed null, defaults to READ.
	 */
	protected void execute(final Object[] cmdParams) {
		final int param = (null == cmdParams) ? READ : ((Integer) cmdParams[0])
				.intValue();
		if (param == READ) {
			readScalers();
		} else if (param == ZERO) {
			GuiceInjector.getBroadcastUtilitities().zeroScalers();
		} else {
			LOGGER
					.severe("Incomplete command: need 'scaler zero' or 'scaler read'.");
		}
	}

	@Override
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
		if (GuiceInjector.getJamStatus().isOnline()) {
			BROADCASTER.broadcast(BroadcastEvent.Command.SCALERS_READ);
		}
	}
}
