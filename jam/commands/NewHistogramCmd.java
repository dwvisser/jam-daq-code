package jam.commands;

import jam.data.Histogram;

/**
 * Command to create a histogram
 * 
 * @author Ken Swartz
 *  
 */
final class NewHistogramCmd extends AbstractCommand {

	NewHistogramCmd() {
		super();
	}

	/**
	 * Execute the command
	 */
	protected void execute(Object[] cmdParams) {
		final String name = (String) cmdParams[0];
		final String title = (String) cmdParams[1];
		final int type = ((Integer)cmdParams[2]).intValue();
		final Histogram.Type hType = type == 1 ? Histogram.Type.ONE_D_DOUBLE
				: Histogram.Type.TWO_D_DOUBLE;
		final int sizeX = ((Integer) cmdParams[3]).intValue();
		final int sizeY = ((Integer) cmdParams[4]).intValue();
		Histogram.createHistogram(hType.getSampleArray(sizeX,sizeY), name, title);
	}

	/**
	 * Execute the command
	 */
	protected void executeParse(String[] cmdParams) {
		final String name = cmdParams[0];
		final String title = cmdParams[1];
		final int type = Integer.parseInt(cmdParams[2]);
		final Histogram.Type hType = type == 1 ? Histogram.Type.ONE_D_DOUBLE
				: Histogram.Type.TWO_D_DOUBLE;
		final int sizeX = Integer.parseInt(cmdParams[3]);
		final int sizeY = Integer.parseInt(cmdParams[4]);
		Histogram.createHistogram(hType.getSampleArray(sizeX,sizeY), name, title);
	}

}