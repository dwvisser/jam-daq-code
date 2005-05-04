package jam.ui;

import java.net.URL;
import java.util.Observer;

import jam.commands.CommandManager;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.CommandNames;
import jam.global.CommandNames;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import java.util.Observable;
import java.util.Observer;

/**
 * Main Toolbar for Jam
 * s
 * @author Ken Swartz
 *
 */
public class JamToolBar extends JToolBar implements Observer, CommandNames {

	final transient private CommandManager commands = CommandManager.getInstance();
	
	public JamToolBar() {
		//final Icon iOpen = loadToolbarIcon("toolbarButtonGraphics/general/Open24.gif");
		//final Icon iOpen = loadToolbarIcon("jam/ui/OpenJam.png");
		//Action actionOpen=commands.getAction(OPEN_HDF);
		//JButton bOpen = new JButton("Open");
		JButton bOpen = new JButton();
		bOpen.setAction(commands.getAction(OPEN_HDF));
		bOpen.setText("");		
	    add(bOpen);

	    //final Icon iOpenAdd = loadToolbarIcon("jam/ui/OpenAddJam.png");
	    //JButton bOpenAdd = new JButton(iOpenAdd);
	    JButton bOpenAdd = new JButton();
	    bOpenAdd.setAction(commands.getAction(OPEN_ADDITIONAL_HDF));
	    bOpenAdd.setText("");
	    add(bOpenAdd);
	    
	    //final Icon iOpenMulti = loadToolbarIcon("jam/ui/OpenJamMulti.png");	    
	    //JButton bOpenMult = new JButton(iOpenMulti);	    
	    JButton bOpenMult = new JButton();	    
	    bOpenMult.setAction(commands.getAction(OPEN_MULTIPLE_HDF));
	    bOpenMult.setText("");	    
	    add(bOpenMult);	    
	    
	    //final Icon iSave = loadToolbarIcon("toolbarButtonGraphics/general/Save24.gif");
	    JButton bSave = new JButton();	    
	    bSave.setAction(commands.getAction(SAVE_HDF));	    
	    add(bSave);

	    //final Icon iSaveAs = loadToolbarIcon("toolbarButtonGraphics/general/SaveAs24.gif");	    
	    JButton bSaveAs = new JButton();
	    bSaveAs.setAction(commands.getAction(SAVE_AS_HDF));	    
	    add(bSaveAs);	    
	    
	    JButton bPrint = new JButton();	    
	    bPrint.setAction(commands.getAction(PRINT));	    
	    add(bPrint);
	    
	    JButton bSort = new JButton();
	    bSort.setAction(commands.getAction(SHOW_SORT_CONTROL));	    
	    add(bSort);
	    
	    JButton bStart = new JButton("Start");
	    bStart.setAction(commands.getAction(START));	    
	    add(bStart);
	    //STOP
	    	    
	    JButton bBuffers = new JButton("Buffers");
	    bBuffers.setAction(commands.getAction(SHOW_BUFFER_COUNT));	    
	    add(bBuffers);

	    JButton bZero = new JButton("Zero");
	    //bBuffers.setAction(commands.getAction(SHOW_BUFFER_COUNT));	    
	    add(bZero);
	    
	    JButton bGateSet = new JButton("Gate Set");
	    //bGateSet.setAction(commands.getAction(SHOW_SET_GATE));	    
	    add(bGateSet);
	    
	     //button.setToolTipText(toolbarLabels[i]);
	     //button.setMargin(margins);

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
	
	public void update(Observable observe, Object obj) {
		/*

		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			sortModeChanged();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_SELECT) {
			final Object content = event.getContent();
			final Histogram hist = content == null ? status.getCurrentHistogram()
					: (Histogram) content;
			adjustHistogramItems(hist);
		} else if (command == BroadcastEvent.Command.FIT_NEW) {
			Action fitAction = (Action) (event.getContent());
			fitting.add(new JMenuItem(fitAction));
		} else if (command == BroadcastEvent.Command.VIEW_NEW) {
			updateViews();
		}
		*/
	}	
}
