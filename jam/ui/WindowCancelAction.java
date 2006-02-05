package jam.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action for a cancel button on a dialog window.
 * @author Dale Visser
 *
 */
public final class WindowCancelAction extends AbstractAction {

	private transient final Window window;
	
	/**
	 * 
	 * @param window to dispose of when action is fired
	 */
	public WindowCancelAction(Window window){
		super("Cancel");
		this.window=window;
	}
	
	public void actionPerformed(final ActionEvent event) {
		window.dispose();
	}

}
