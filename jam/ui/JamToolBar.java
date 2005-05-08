package jam.ui;

import jam.commands.CommandManager;
import jam.global.CommandNames;

import java.net.URL;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import java.util.Observable;


/**
 * Main Toolbar for Jam
 * s
 * @author Ken Swartz
 *
 */
public class JamToolBar extends JToolBar implements Observer, CommandNames {

	final transient private CommandManager commands = CommandManager.getInstance();
	
	public JamToolBar() {
		
	    add(createButton(OPEN_HDF));

	    add(createButton(OPEN_ADDITIONAL_HDF));
	    	    
	    add(createButton(OPEN_MULTIPLE_HDF));	    
	    	    
	    add(createButton(SAVE_HDF));	    
	    	    
	    add(createButton(SAVE_AS_HDF));	    
	    
	    add(createButton(PRINT));	    

	    add(createButton(SHOW_RUN_CONTROL));	
	    	    
	    add(createButton(START));
	    
	    add(createButton(STOP));

	    add(createButton(SHOW_SORT_CONTROL));

	    add(createButton(SHOW_HIST_ZERO));	    
	    
	    add(createButton(SHOW_SET_GATE));
	    
	}
	
	/* non-javadoc:
	 * Load icons for tool bar
	 */
	private Icon loadToolbarIcon(String path) {
		final Icon toolbarIcon;
		final ClassLoader cl = this.getClass().getClassLoader();
		final URL urlResource = cl.getResource(path);
		if (!(urlResource == null)) {
			toolbarIcon = new ImageIcon(urlResource);
		} else { //instead use path, ugly but lets us see button
			JOptionPane.showMessageDialog(
				this,
				"Can't load resource: " + path,
				"Missing Icon",
				JOptionPane.ERROR_MESSAGE);
			toolbarIcon = null; //buttons initialized with text if icon==null
		}
		return toolbarIcon;
	}
	
	private JButton createButton(String command) {
	    JButton buttonToolbar = new JButton();
	    buttonToolbar.setAction(commands.getAction(command));	    
	    buttonToolbar.setText("");	    
	    return buttonToolbar;
	}
	public void update(Observable observe, Object obj) {
		/* FIXME KBS remove
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			sortModeChanged();
		} 
		*/
	}	
}
