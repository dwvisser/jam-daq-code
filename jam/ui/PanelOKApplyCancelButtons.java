package jam.ui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
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

	private final Callback callback;
	private final JPanel panel=new JPanel(new FlowLayout(FlowLayout.CENTER));
	
	/**
	 * Constructs a Swing component which has OK, Apply, and Cancel buttons.
	 * 
	 * @param cb object with methods to be called when the buttons get pressed
	 */
	public PanelOKApplyCancelButtons(Callback cb) {
		callback=cb;
		final JPanel pb = new JPanel(new GridLayout(1, 0, 5, 5));
		panel.add(pb);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				callback.ok();
			}
		});
		pb.add(bok);
		final JButton bapply = new JButton("Apply");
		bapply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				callback.apply();
			}
		});
		pb.add(bapply);
		final JButton bcancel = new JButton("Cancel");
		bcancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				callback.cancel();
			}
		});
		pb.add(bcancel);
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
	 * Handler for OK, apply and cancel methods which are called when
	 * their associated buttons are pressed.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
	 */
	public interface Callback {
	    
	    /**
	     * To be called when the user clicks the OK button.
	     */
		void ok();
		
	    /**
	     * To be called when the user clicks the Apply button.
	     */
		void apply();

	    /**
	     * To be called when the user clicks the Cancel button.
	     */
		void cancel();
	}
	
}


