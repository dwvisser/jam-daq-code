package jam.plot;

import java.util.HashMap;
import java.util.Map;

import jam.JamConsole;
import jam.commands.CommandManager;
import jam.global.JamStatus;
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
	
	private final Map commandMap = new HashMap();
	{	
		commandMap.put("help", Action.HELP);
		commandMap.put("ex", Action.EXPAND);
		commandMap.put("zi", Action.ZOOMIN);
		commandMap.put("zo", Action.ZOOMOUT);
		commandMap.put("f", Action.FULL);
		commandMap.put("li", Action.LINEAR);
		commandMap.put("lo", Action.LOG);
		commandMap.put("ar", Action.AREA);
		commandMap.put("g", Action.GOTO);
		commandMap.put("n", Action.NETAREA);
		commandMap.put("u", Action.UPDATE);
		commandMap.put("a", Action.AUTO);
		commandMap.put("o", Action.OVERLAY);
		commandMap.put("c", Action.CANCEL);
		commandMap.put("x", Action.EXPAND);
		commandMap.put("y", Action.EXPAND);
		commandMap.put("ra", Action.RANGE);
		commandMap.put("d", Action.DISPLAY);
		commandMap.put("re", Action.REBIN);
		commandMap.put("s", Action.SCALE);	
	}
	
	ParseCommand(Action action, JamConsole jc){
		this.action=action;
		textOut=jc;
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
		final double[] parameters = convertParameters(cmdParams);		
		if (command.equals(JamConsole.NUMBERS_ONLY)) {
			accept = true;
			if (action.getIsCursorCommand()) {
				cursorChannel(parameters);				
			}else {				
				action.doCommand(null, parameters);
			}
		} else if (commandMap.containsKey(command)) {
			accept = true;
			final String inCommand = (String) commandMap.get(command);
			action.doCommand(inCommand);			
			if (action.getIsCursorCommand()) {				
				cursorChannel(parameters);				
			}else {				
				action.doCommand(null, parameters);				
			}
		} else{
			accept = false;
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
	private void cursorChannel(double[] parameters) {
		final Display display =JamStatus.instance().getDisplay();
		final Plot currentPlot = display.getPlot();	
		final int numParam = parameters.length;
		//Must have at least 1 parameter
		if (numParam<1){
			return;
		}
		final Bin cursor = Bin.Factory.create();
		// we have a 1 d plot		
		if (action.getCursorDimension()== 1) {
			//Only x dimension
			for (int i=0; i< numParam; i++ ) {
				cursor.setChannel((int)parameters[i], 0);
				action.setCursor(cursor);				
				action.doCommand(Action.CURSOR);
			}
		}else {
			//x and y dimensions
			for (int i=0; i< numParam-1; i=i+2 ) {
				cursor.setChannel((int)parameters[i], (int)parameters[i+1]);
				action.setCursor(cursor);				
				action.doCommand(Action.CURSOR);				
			}
			//Error if only 1 co-ordinate
			//if i<	
		}

		
		/*
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
		*/
	}

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
}

