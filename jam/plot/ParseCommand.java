package jam.plot;

import java.util.HashMap;
import java.util.Map;

import jam.JamConsole;
import jam.commands.CommandManager;
import jam.global.CommandListener;
import jam.global.MessageHandler;

/**
 * 
 * Parse command for plotting
 * 
 * @author Ken Swartz
 *
 */
public class ParseCommand implements CommandListener{

	
	private Action action;
	
	private final MessageHandler textOut;
	
	private final Map commandMap;
	
	private String inCommand;	
	
	private final Bin cursor;
	
	ParseCommand(Action action, JamConsole jc){
		this.action=action;
		textOut=jc;
		
		commandMap = createCommandMap();
		
		cursor =  Bin.Factory.create();
	}
	/**
	 * Parse a plot command
	 */
	public boolean performParseCommand(String _command, String[] cmdParams) {
		boolean accept = false; //is the command accepted
		boolean handleIt = false;
		final String command = _command.toLowerCase();
		/*
		 * int is a special case meaning no command and just parameters
		 */
		if (command.equals(JamConsole.NUMBERS_ONLY)) {
			final double[] parameters = convertParameters(cmdParams);
			if (Action.DISPLAY.equals(inCommand)) {
				action.display(parameters);
				accept = true;
			} else if (Action.OVERLAY.equals(inCommand)) {
				action.overlay(parameters);
				accept = true;
			} else {
				action.integerChannel(parameters);
				accept = true;
			}
			accept = true;
		} else if (commandMap.containsKey(command)) {
			inCommand = (String) commandMap.get(command);
			accept = true;
			handleIt = true;
		}
		if (accept && handleIt) {
			final double[] parameters = convertParameters(cmdParams);
			if (Action.DISPLAY.equals(inCommand)) {
				action.display(parameters);
			} else if (Action.OVERLAY.equals(inCommand)) {
				action.overlay(parameters);
			} else if (Action.HELP.equals(inCommand)) {
				help();				
			} else {
				action.doCommand(inCommand);
				action.integerChannel(parameters);
			}
		}
		return accept;
	}
	/**
	 * Convert the parameters to doubles.
	 * 
	 * @param parameters
	 * @return
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
	/**
	 * Accepts integer input and does a command if one is present.
	 * 
	 * @param parameters
	 *            the integers
	 */
/*
	void integerChannel(double[] parameters) {
		final int numPar = parameters.length;

		 // FIXME we should be better organized so range and rebin are not
		 //special cases
		if (parameters.length==1)		
			cursor.setChannel((int)parameters[0], 0);
		else if	(parameters.length>1)
			cursor.setChannel((int)parameters[0], (int)parameters[1]);
		
		action.doCommand(Action.CURSOR);
		
		if ((commandPresent)) {
			if (RANGE.equals(inCommand)) {
				synchronized (cursor) {
					final int len = Math.min(numPar, 2);
					for (int i = 0; i < len; i++) {
						rangeList.add(new Integer((int) parameters[i]));
						doCommand(inCommand);
					}
				}
				return;
			} else if (REBIN.equals(inCommand)) {
				if (numPar > 0) {
					parameter = new double[numPar];
					System.arraycopy(parameters, 0, parameter, 0, numPar);
				}
			}
		}
		// we have a 1 d plot 
		final Plot currentPlot = display.getPlot();
		if (currentPlot.getDimensionality() == 1) {
			if (commandPresent) {
				final int loopMax = Math.min(numPar, 2);
				for (int i = 0; i < loopMax; i++) {
					if (GOTO.equals(inCommand)) {
						cursor.setChannel((int) parameters[i], 0);
					} else {
						cursor.setChannel((int) parameters[i], 0);
						cursor.shiftInsidePlot();
					}
					doCommand(inCommand);
				}
			} else { //no command so get channel
				if (numPar > 0) {
					// check for out of bounds 
					synchronized (cursor) {
						cursor.setChannel((int) parameters[0], 0);
						cursor.shiftInsidePlot();
						final double cursorCount = cursor.getCounts();
						currentPlot.markChannel(cursor);
						textOut.messageOutln("Bin " + cursor.getX()
								+ ":  Counts = " + cursorCount);
					}
					done();
				}
			}
		} else { //we have a 2d plot
			if (commandPresent) {
				final int loopMax = Math.min(numPar, 4);
				synchronized (this) {
					for (int i = 1; i < loopMax; i += 2) {
						cursor.setChannel((int) parameters[i - 1],
								(int) parameters[i]);
						cursor.shiftInsidePlot();
						doCommand(inCommand);
					}
				}
			} else { //no command so get channel
				if (numPar > 1) {
					cursor.setChannel((int) parameters[0], (int) parameters[1]);
					cursor.shiftInsidePlot();
					currentPlot.markChannel(cursor);
					textOut.messageOutln("Bin " + cursor.getCoordString()
							+ ":  Counts = " + cursor.getCounts());
					done();
				}
			}
		}
	}
	*/
	/**
	 * Parse a string go a number
	 * 
	 * @param s
	 * @return
	 * @throws NumberFormatException
	 */
	private double convertNumber(String s) throws NumberFormatException {
		return (s.indexOf('.') >= 0) ? Double.parseDouble(s) : Integer
				.parseInt(s);
	}
	
	/**
	 * Display help
	 */
	private void help() {
		final StringBuffer sb = new StringBuffer("Commands:\t");
		sb
				.append("li - Linear Scale\tlo - Log Scale\ta  - Auto Scale\tra - Range\t");
		sb
				.append("ex - Expand\tf  - Full view\t zi - Zoom In\tzo - Zoom Out\t");
		sb.append("d  - Display\to  - Overlay\tu  - Update\tg  - GoTo\t");
		sb.append("ar - Area\tn  - Net Area\tre - Rebin\tc  - Bin\t");
		final String[] commands = CommandManager.getInstance().getAllCommands();
		for (int i = 0; i < commands.length; i++) {
			sb.append(commands[i]).append("\t");
		}
		textOut.messageOutln(sb.toString());
	}
	
	final private Map createCommandMap() {
		final Map localMap = new HashMap();
		
		localMap.put("help", Action.HELP);
		localMap.put("ex", Action.EXPAND);
		localMap.put("zi", Action.ZOOMIN);
		localMap.put("zo", Action.ZOOMOUT);
		localMap.put("f", Action.FULL);
		localMap.put("li", Action.LINEAR);
		localMap.put("lo", Action.LOG);
		localMap.put("ar", Action.AREA);
		localMap.put("g", Action.GOTO);
		localMap.put("n", Action.NETAREA);
		localMap.put("u", Action.UPDATE);
		localMap.put("a", Action.AUTO);
		localMap.put("o", Action.OVERLAY);
		localMap.put("c", Action.CANCEL);
		localMap.put("x", Action.EXPAND);
		localMap.put("y", Action.EXPAND);
		localMap.put("ra", Action.RANGE);
		localMap.put("d", Action.DISPLAY);
		localMap.put("re", Action.REBIN);
		localMap.put("s", Action.SCALE);		
		
		return localMap;
	}
	
	
	
}

