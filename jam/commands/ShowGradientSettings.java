package jam.commands;
import jam.plot.color.ColorSettingsFrame;

/**
 * Show the histogram dialog
 * 
 * @author Ken
 *
 */
final class ShowGradientSettings extends AbstractShowDialog {
	
	/**
	 * Initialize command
	 */
	public void initCommand(){
		putValue(NAME, "Adjust color gradient\u2026");
		dialog=ColorSettingsFrame.getInstance();
	}
}
