package jam.ui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Panel with OK, Apply and Cancel buttons for dialogs
 * 
 * @author Ken Swartz
 *
 */
public class PanelOKApplyCancelButtons extends JPanel {

	private JButton bok;
	
	private Callback callback;
	
	public PanelOKApplyCancelButtons(Callback callback) {
		
		this.callback=callback;
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
	
		
		final JPanel pb = new JPanel(new GridLayout(1, 0, 5, 5));
		this.add(pb);
		
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeOk();
			}
		});
		pb.add(bok);
		final JButton bapply = new JButton("Apply");
		bapply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeApply();
			}
		});
		pb.add(bapply);
		final JButton bcancel = new JButton("Cancel");
		bcancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeCancel();
			}
		});
		pb.add(bcancel);
	}
	
	public void executeOk() {
		callback.ok();
	}
	
	public void executeApply() {
		callback.apply();
	}
	
	public void executeCancel() {
		callback.cancel();
	}
	
	public interface Callback {
		void ok();
		void apply();
		void cancel();
	}
	
}


