package jam.data.control;

import jam.commands.ScalersCmd;

import java.awt.Container;
import java.awt.FlowLayout;
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

	private transient final JButton bzero2;

	private transient final JCheckBox chkDisable;

	private transient final ScalersCmd scalersCmd;

	/**
	 * Constructs a new dialog for zeroing scaler values.
	 */
	public ScalerZero() {
		super("Zero Scalers", true);
		scalersCmd = new ScalersCmd();
		Container dzc = getContentPane();
		setResizable(false);
		setLocation(20, 50);
		final JPanel pZero = new JPanel(new GridLayout(1, 0, 10, 10));
		Border border = new EmptyBorder(10, 10, 10, 10);
		pZero.setBorder(border);

		final JPanel pButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pZero.add(pButton);
		bzero2 = new JButton("Zero");
		bzero2.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				chkDisable.setSelected(true);
				bzero2.setEnabled(false);
				zero();
				dispose();
			}
		});
		bzero2.setEnabled(false);
		pButton.add(bzero2);

		chkDisable = new JCheckBox("Disable Zero", true);
		chkDisable.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
				if (chkDisable.isSelected()) {
					bzero2.setEnabled(false);
				} else {
					bzero2.setEnabled(true);
				}
			}
		});
		pZero.add(chkDisable);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent event) {
				dispose();
			}
		});
		dzc.add(pZero);
		pack();
	}

	/**
	 * @see jam.data.control.AbstractControl#doSetup()
	 */
	public void doSetup() {
		/* nothing to set up */
	}

	/**
	 * zero scalers, call broadcast which will sent it to the class that will
	 * zero the camac crate scalers.
	 */
	private void zero() {
		scalersCmd.zeroScalers();
	}
}
