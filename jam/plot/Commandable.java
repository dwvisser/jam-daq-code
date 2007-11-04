package jam.plot;

import java.util.List;

interface Commandable {
	boolean getIsCursorCommand();
	
	void doCommand(final String inCommand, final List<Double> inParams,
			final boolean console);
	
	void doCommand(final String inCommand, final boolean console);
	
	String getCurrentCommand();
	
	void setCursor(final Bin cursorIn);
}
