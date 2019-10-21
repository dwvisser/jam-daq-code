package jam.commands;
import jam.plot.color.ColorSettingsFrame;

/**
 * Show the histogram dialog
 * 
 * @author Ken
 *
 */
@SuppressWarnings("serial")
final class ShowGradientSettings extends AbstractShowDialog {
	
	/**
	 * Initialize command
	 */
	ShowGradientSettings(){
		super("Adjust color gradient\u2026");
		dialog=ColorSettingsFrame.getInstance();
	}
}
