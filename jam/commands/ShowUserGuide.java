package jam.commands;

import jam.global.CommandListenerException;

import java.awt.event.ActionListener;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @author Ken Swartz
 */
final class ShowUserGuide extends AbstractCommand {
	
	JButton proxy;

	public void initCommand(){
		putValue(NAME,"User Guide\u2026");
		final String helpsetName = "help/jam.hs";
		try {
			final URL hsURL =
				getClass().getClassLoader().getResource(helpsetName);
			final HelpSet hs = new HelpSet(null, hsURL);
			ActionListener ac= new CSH.DisplayHelpFromSource(hs.createHelpBroker());	
			proxy=new JButton();
			proxy.addActionListener(ac);
		} catch (Exception ee) {
			final String message = "HelpSet " + helpsetName + " not found";
			final JFrame frame =STATUS.getFrame();
			JOptionPane.showMessageDialog(
				frame,
				ee.getMessage(),
				message,
				JOptionPane.ERROR_MESSAGE);
		}
	}
		
		
	/**
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams)  {
		proxy.doClick();		
	}

	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		execute(null);
	}	
}
