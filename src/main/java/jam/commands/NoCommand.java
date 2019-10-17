package jam.commands;

import jam.global.CommandListenerException;

/**
 * Command that does nothing.
 * @author Dale Visser
 *
 */
public final class NoCommand extends AbstractCommand {
	
	NoCommand(){
		super();
	}

	@Override
	protected void execute(final Object[] cmdParams) throws CommandException {
		// do nothing
	}

	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		// do nothing
	}

}
