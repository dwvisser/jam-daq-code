package jam.ui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Panel with OK, Apply and Cancel buttons for dialogs
 * 
 * @author Ken Swartz
 *
 */
public class PanelOKApplyCancelButtons {

	private final Listener callback;
	private final JPanel panel=new JPanel(new FlowLayout(FlowLayout.CENTER));
	private final JButton bok = new JButton("OK");
	private final JButton bapply = new JButton("Apply");
	private final JButton bcancel = new JButton("Cancel");
	
	/**
	 * Constructs a Swing component which has OK, Apply, and Cancel buttons.
	 * 
	 * @param listener object with methods to be called when the buttons get pressed
	 */
	public PanelOKApplyCancelButtons(Listener listener) {
		callback=listener;
		final JPanel grid = new JPanel(new GridLayout(1, 0, 5, 5));
		panel.add(grid);
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				callback.doOK();
			}
		});
		grid.add(bok);
		bapply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				callback.apply();
			}
		});
		grid.add(bapply);
		bcancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				callback.cancel();
			}
		});
		grid.add(bcancel);
	}
	
	/**
	 * Returns the Swing button panel.
	 * 
	 * @return the Swing button panel
	 */
	public JComponent getComponent(){
	    return panel;
	}
	
	/**
	 * Set the enabled state of the buttons.
	 * @param okEnable the enable state of "OK" button
	 * @param apply the enable state of "Apply" button
	 * @param cancel the enable state of "Cancel" button
	 */
	public void setButtonsEnabled(boolean okEnable, boolean apply, boolean cancel){
	    bok.setEnabled(okEnable);
	    bapply.setEnabled(apply);
	    bcancel.setEnabled(cancel);
	}
	
	/**
	 * Handler for OK, apply and cancel methods which are called when
	 * their associated buttons are pressed.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
	 */
	public interface Listener {
	    
	    /**
	     * To be called when the user clicks the OK button.
	     */
		void doOK();
		
	    /**
	     * To be called when the user clicks the Apply button.
	     */
		void apply();

	    /**
	     * To be called when the user clicks the Cancel button.
	     */
		void cancel();
	}
	
	/**
	 * Default implementation of <code>Listener</code> has <code>doOK()</code>
	 * exectute <code>apply()</code>, then <code>cancel</code>, which makes
	 * the given <code>Window</code> invisible.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
	 */
	public static abstract class DefaultListener implements Listener {
	    private final Window parent;
	    
	    /**
	     * Constructs a listener.
	     * 
	     * @param window the ultimate container we wish to make disappear
	     * on cancel and OK
	     */
	    public DefaultListener(Window window){
	        parent = window; 
	    }
	    
	    public void doOK(){
	        apply();
	        cancel();
	    }
	    
	    public void cancel(){
	        parent.dispose();
	    }
	}
	
}


