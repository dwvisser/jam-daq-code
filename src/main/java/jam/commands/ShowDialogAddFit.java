package jam.commands;

import com.google.inject.Inject;

import jam.fit.LoadFit;
import jam.global.CommandListenerException;

/**
 * Command to add fit.
 * 
 * @author Ken Swartz
 */

final class ShowDialogAddFit extends AbstractCommand {

	private transient final LoadFit loadFit;

	@Inject
	ShowDialogAddFit(final LoadFit loadFit) {
		super("Load Fit\u2026");
		this.loadFit = loadFit;
	}

	@Override
	protected void execute(final Object[] cmdParams) {
		// final LoadFit loadfit = new LoadFit();
		loadFit.showLoad();
	}

	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		execute(null);
	}
}
