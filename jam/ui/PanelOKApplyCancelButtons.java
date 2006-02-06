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
public final class PanelOKApplyCancelButtons {

	private transient final Listener callback;
	private transient final JPanel panel=new JPanel(new FlowLayout(FlowLayout.CENTER));
	private transient final JButton bok = new JButton("OK");
	private transient final JButton bapply = new JButton("Apply");
	private transient final JButton bcancel;// = new JButton("Cancel");
	
	/**
	 * Constructs a Swing component which has OK, Apply, and Cancel buttons.
	 * 
	 * @param listener object with methods to be called when the buttons get pressed
	 */
	public PanelOKApplyCancelButtons(Listener listener) {
		super();
		callback=listener;
		final JPanel grid = new JPanel(new GridLayout(1, 0, 5, 5));
		panel.add(grid);
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				callback.doOK();
			}
		});
		grid.add(bok);
		bapply.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				callback.apply();
			}
		});
		grid.add(bapply);
		bcancel = new JButton(new WindowCancelAction(callback));
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
	public void setButtonsEnabled(final boolean okEnable, final boolean apply, final boolean cancel){
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
	public interface Listener extends Canceller {
	    
	    /**
	     * To be called when the user clicks the OK button.
	     */
		void doOK();
		
	    /**
	     * To be called when the user clicks the Apply button.
	     */
		void apply();
	}
	
	/**
	 * Default implementation of <code>Listener</code> has <code>doOK()</code>
	 * exectute <code>apply()</code>, then <code>cancel</code>, which makes
	 * the given <code>Window</code> invisible.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
	 */
	public static abstract class AbstractListener implements Listener {
	    private transient final Window parent;
	    
	    /**
	     * Constructs a listener.
	     * 
	     * @param window the ultimate container we wish to make disappear
	     * on cancel and OK
	     */
	    public AbstractListener(Window window){
	    	super();
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


