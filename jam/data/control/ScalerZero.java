package jam.data.control;

import jam.global.BroadcastEvent;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Control dialog for zeroing scalers.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 3, 2004
 */
public class ScalerZero extends AbstractControl {

	private transient final JCheckBox disable;
	private transient final JButton bzero2;

	/**
	 * Constructs a new dialog for zeroing scaler values.
	 */
	public ScalerZero() {
		super("Zero Scalers", true);
		Container dzc = getContentPane();
		setResizable(false);
		setLocation(20, 50);
		final JPanel pZero = new JPanel(new GridLayout(1, 0, 20, 20));
		Border border = new EmptyBorder(20, 20, 20, 20);
		pZero.setBorder(border);
		bzero2 = new JButton("Zero");
		bzero2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				disable.setSelected(true);
				bzero2.setEnabled(false);
				dispose();
			}
		});
		bzero2.setEnabled(false);
		pZero.add(bzero2);
		disable = new JCheckBox("Disable Zero", true);
		disable.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent event){
				if(disable.isSelected()){
					bzero2.setEnabled(false);
				} else {
					bzero2.setEnabled(true);
				}
			}
		});
		pZero.add(disable);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				dispose();
			}
		});
		dzc.add(pZero);
		pack();
	}

	/**
	 * zero scalers, call broadcast which will sent it to
	 * the class that will zero the camac crate scalers.
	 */
	static void zero() {
		if (!STATUS.isOnline()) {
			throw new IllegalStateException("Can't Zero Scalers when not in Online mode.");
		}
		BROADCASTER.broadcast(BroadcastEvent.Command.SCALERS_CLEAR);
		BROADCASTER.broadcast(BroadcastEvent.Command.SCALERS_READ);
	}

	/**
	 * @see jam.data.control.AbstractControl#doSetup()
	 */
	public void doSetup() {
		/* nothing to set up */
	}
}
