package jam.commands;

import jam.global.CommandListenerException;

import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.help.CSH; 
import javax.help.HelpSet;

/**
 * @author Ken Swartz
 *
 */
public class ShowUserGuide extends AbstractCommand implements Commandable {

	ShowUserGuide(){
		putValue(NAME,"User Guide\u2026");
	}
		
		
	/**
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams)  {
		final HelpSet hs;
		final String helpsetName = "help/jam.hs";
		try {
			final URL hsURL =
				getClass().getClassLoader().getResource(helpsetName);
			hs = new HelpSet(null, hsURL);
			ActionListener ac= new CSH.DisplayHelpFromSource(hs.createHelpBroker());	
			ac.actionPerformed(new ActionEvent(this, 0, ""));		
		} catch (Exception ee) {
			final String message = "HelpSet " + helpsetName + " not found";
			showErrorMessage(message, ee);

		}


	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		execute(null);

	}
	private void showErrorMessage(String title, Exception e) {
		final JFrame frame =status.getFrame();
		JOptionPane.showMessageDialog(
			frame,
			e.getMessage(),
			title,
			JOptionPane.ERROR_MESSAGE);
	}

}
