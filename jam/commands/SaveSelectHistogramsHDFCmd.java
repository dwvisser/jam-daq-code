/*
 * Created on Jan 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jam.commands;

import jam.global.CommandListenerException;

/**
 * Save selected histograms to a file
 * 
 * @author Ken Swartz
 *
 */
public class SaveSelectHistogramsHDFCmd extends AbstractCommand {
	
	public void initCommand() {
		putValue(NAME, "Save select histograms\u2026");
	}
	
	/**
	 */
	protected void execute(Object[] cmdParams) throws CommandException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
			throws CommandListenerException {
		// TODO Auto-generated method stub

	}

}
