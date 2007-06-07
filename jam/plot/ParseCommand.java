package jam.plot;

import jam.global.CommandListener;
import jam.global.JamStatus;
import jam.ui.Console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 
 * Parse command for plotting
 * 
 * @author Ken Swartz
 * 
 */
final class ParseCommand implements CommandListener {

	private static final Map<String, String> CMD_MAP = new HashMap<String, String>();

	static {
		CMD_MAP.put("help", PlotCommands.HELP);
		CMD_MAP.put("ex", PlotCommands.EXPAND);
		CMD_MAP.put("zi", PlotCommands.ZOOMIN);
		CMD_MAP.put("zo", PlotCommands.ZOOMOUT);
		CMD_MAP.put("f", PlotCommands.FULL);
		CMD_MAP.put("li", PlotCommands.LINEAR);
		CMD_MAP.put("lo", PlotCommands.LOG);
		CMD_MAP.put("ar", PlotCommands.AREA);
		CMD_MAP.put("g", PlotCommands.GOTO);
		CMD_MAP.put("n", PlotCommands.NETAREA);
		CMD_MAP.put("u", PlotCommands.UPDATE);
		CMD_MAP.put("a", PlotCommands.AUTO);
		CMD_MAP.put("o", PlotCommands.OVERLAY);
		CMD_MAP.put("c", PlotCommands.CANCEL);
		CMD_MAP.put("x", PlotCommands.EXPAND);
		CMD_MAP.put("y", PlotCommands.EXPAND);
		CMD_MAP.put("ra", PlotCommands.RANGE);
		CMD_MAP.put("d", PlotCommands.DISPLAY);
		CMD_MAP.put("re", PlotCommands.REBIN);
		CMD_MAP.put("s", PlotCommands.SCALE);
	}

	private transient final Action action;

	ParseCommand(Action action) {
		super();
		this.action = action;
	}

	/**
	 * Parse a string go a number
	 * 
	 * @param string
	 *            string rep of the number
	 * @return the number
	 * @throws NumberFormatException
	 */
	private double convertNumber(final String string)
			throws NumberFormatException {
		return (string.indexOf('.') >= 0) ? Double.parseDouble(string)
				: Integer.parseInt(string);
	}

	/*
	 * non-javadoc: Convert the parameters to doubles.
	 */
	private List<Double> convertParameters(final String[] cmdParams) {
		final int numberParams = cmdParams.length;
		final List<Double> rval = new ArrayList<Double>(numberParams);
		/*
		 * The parameters must be numbers or a NumberFormatException can be
		 * thrown.
		 */
		int countParam = 0;
		while (countParam < numberParams) {
			rval.add(countParam, convertNumber(cmdParams[countParam]));
			countParam++;
		}
		return rval;
	}

	/*
	 * non-javadoc: Accepts integer input and does a command if one is present.
	 * 
	 * @param parameters the integers
	 */
	private void cursorChannel(final List<Double> parameters,
			final boolean vertical) {
		final int numParam = parameters.size();
		/* Must have at least 1 parameter */
		if (numParam > 0) {
			final Bin cursor = Bin.create();
			if (PlotDisplay.getDisplay()
					.getPlotContainer().getDimensionality() == 1) {
				/* we have a 1D plot: only x dimension */
				for (double dVal : parameters) {
					final int iVal = (int) dVal;
					if (vertical) {
						cursor.setChannel(0, iVal);
					} else {
						cursor.setChannel(iVal, 0);
					}
					action.setCursor(cursor);
					action.doCommand(PlotCommands.CURSOR, true);
				}
			} else {
				if (vertical) {// use 0,counts
					for (double dVal : parameters) {
						cursor.setChannel(0, (int) dVal);
						action.setCursor(cursor);
						action.doCommand(PlotCommands.CURSOR, true);
					}
				} else {
					/* 2D: x and y dimensions */
					for (int i = 0; i < numParam - 1; i = i + 2) {
						cursor.setChannel(parameters.get(i).intValue(),
								parameters.get(i + 1).intValue());
						action.setCursor(cursor);
						action.doCommand(PlotCommands.CURSOR, true);
					}
				}
			}
		}
	}

	/**
	 * @see CommandListener#performParseCommand(String, String[])
	 */
	public boolean performParseCommand(final String _command,
			final String[] cmdParams) {
		boolean accept = false; // is the command accepted
		final String command = _command.toLowerCase(Locale.US);
		/*
		 * int is a special case meaning no command and just parameters
		 */
		final List<Double> parameters = convertParameters(cmdParams);
		if (command.equals(Console.NUMBERS_ONLY)) {
			accept = true;
			if (action.getIsCursorCommand()) {
				final boolean vertical = PlotCommands.RANGE.equals(action
						.getCurrentCommand());
				cursorChannel(parameters, vertical);
			} else {
				action.doCommand(null, parameters, true);
			}
		} else if (CMD_MAP.containsKey(command)) {
			accept = true;
			final String inCommand = CMD_MAP.get(command);
			action.doCommand(inCommand, parameters, true);
			if (action.getIsCursorCommand()) {
				final boolean vertical = PlotCommands.RANGE.equals(inCommand);
				cursorChannel(parameters, vertical);
			} else {
				action.doCommand(null, parameters, true);
			}
		} else {
			accept = false;
		}
		return accept;
	}
}
