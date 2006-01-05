package jam.plot;

import jam.global.CommandListener;
import jam.global.JamStatus;
import jam.ui.Console;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * Parse command for plotting
 * 
 * @author Ken Swartz
 *  
 */
final class ParseCommand implements CommandListener {

	private final Action action;

	private final Map<String,String> commandMap = new HashMap<String,String>();
	{
		commandMap.put("help", PlotCommands.HELP);
		commandMap.put("ex", PlotCommands.EXPAND);
		commandMap.put("zi", PlotCommands.ZOOMIN);
		commandMap.put("zo", PlotCommands.ZOOMOUT);
		commandMap.put("f", PlotCommands.FULL);
		commandMap.put("li", PlotCommands.LINEAR);
		commandMap.put("lo", PlotCommands.LOG);
		commandMap.put("ar", PlotCommands.AREA);
		commandMap.put("g", PlotCommands.GOTO);
		commandMap.put("n", PlotCommands.NETAREA);
		commandMap.put("u", PlotCommands.UPDATE);
		commandMap.put("a", PlotCommands.AUTO);
		commandMap.put("o", PlotCommands.OVERLAY);
		commandMap.put("c", PlotCommands.CANCEL);
		commandMap.put("x", PlotCommands.EXPAND);
		commandMap.put("y", PlotCommands.EXPAND);
		commandMap.put("ra", PlotCommands.RANGE);
		commandMap.put("d", PlotCommands.DISPLAY);
		commandMap.put("re", PlotCommands.REBIN);
		commandMap.put("s", PlotCommands.SCALE);
	}

	ParseCommand(Action action) {
		this.action = action;
	}

	/**
	 * @see CommandListener#performParseCommand(String, String[])
	 */
	public boolean performParseCommand(String _command, String[] cmdParams) {
		boolean accept = false; //is the command accepted
		final String command = _command.toLowerCase();
		/*
		 * int is a special case meaning no command and just parameters
		 */
		final double[] parameters = convertParameters(cmdParams);
		if (command.equals(Console.NUMBERS_ONLY)) {
			accept = true;
			if (action.getIsCursorCommand()) {
				boolean vertical = PlotCommands.RANGE.equals(action
						.getCurrentCommand());
				cursorChannel(parameters, vertical);
			} else {
				action.doCommand(null, parameters,true);
			}
		} else if (commandMap.containsKey(command)) {
			accept = true;
			final String inCommand = commandMap.get(command);
			action.doCommand(inCommand,true);
			if (action.getIsCursorCommand()) {
				boolean vertical = PlotCommands.RANGE.equals(inCommand);
				cursorChannel(parameters, vertical);
			} else {
				action.doCommand(null, parameters,true);
			}
		} else {
			accept = false;
		}
		return accept;
	}

	/* non-javadoc:
	 * Convert the parameters to doubles.
	 */
	private double[] convertParameters(String[] cmdParams) {
		final int numberParams = cmdParams.length;
		double[] parameters = new double[numberParams];
		/* The parameters must be numbers. */
		try {
			int countParam = 0;
			while (countParam < numberParams) {
				parameters[countParam] = convertNumber(cmdParams[countParam]);
				countParam++;
			}
		} catch (NumberFormatException nfe) {
			throw new NumberFormatException("Input parameter not a number.");
		}
		return parameters;
	}

	/*
     * non-javadoc: Accepts integer input and does a command if one is present.
     * 
     * @param parameters the integers
     */
    private void cursorChannel(double[] parameters, boolean vertical) {
        final int numParam = parameters.length;
        /* Must have at least 1 parameter */
        if (numParam > 0) {
            final Bin cursor = Bin.create();
            if (JamStatus.getSingletonInstance().getDisplay().getPlotContainer().getDimensionality() == 1) {
                /* we have a 1D plot: only x dimension */
                for (int i = 0; i < numParam; i++) {
                    if (vertical) {
                        cursor.setChannel(0, (int) parameters[i]);
                    } else {
                        cursor.setChannel((int) parameters[i], 0);
                    }
                    action.setCursor(cursor);
                    action.doCommand(PlotCommands.CURSOR, true);
                }
            } else {
                if (vertical) {//use 0,counts
                    for (int i = 0; i < numParam; i++) {
                        cursor.setChannel(0, (int) parameters[i]);
                        action.setCursor(cursor);
                        action.doCommand(PlotCommands.CURSOR, true);
                    }
                } else {
                    /* 2D: x and y dimensions */
                    for (int i = 0; i < numParam - 1; i = i + 2) {
                        cursor.setChannel((int) parameters[i],
                                (int) parameters[i + 1]);
                        action.setCursor(cursor);
                        action.doCommand(PlotCommands.CURSOR, true);
                    }
                }
            }
        }
    }

	/**
	 * Parse a string go a number
	 * 
	 * @param s string rep of the number
	 * @return the number
	 * @throws NumberFormatException
	 */
	private double convertNumber(String s) throws NumberFormatException {
		return (s.indexOf('.') >= 0) ? Double.parseDouble(s) : Integer
				.parseInt(s);
	}
}

