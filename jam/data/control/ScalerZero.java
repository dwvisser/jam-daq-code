package jam.data.control;

import jam.commands.ScalersCmd;
import jam.global.MessageHandler;

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

	private transient final JCheckBox chkDisable;
	private transient final JButton bzero2;
	private final ScalersCmd scalersCmd;
	private MessageHandler msgHandler;

	/**
	 * Constructs a new dialog for zeroing scaler values.
	 */
	public ScalerZero() {
		super("Zero Scalers", true);
		msgHandler = STATUS.getMessageHandler();
		scalersCmd =new ScalersCmd();
		Container dzc = getContentPane();
		setResizable(false);
		setLocation(20, 50);
		final JPanel pZero = new JPanel(new GridLayout(1, 0, 20, 20));
		Border border = new EmptyBorder(20, 20, 20, 20);
		pZero.setBorder(border);
		bzero2 = new JButton("Zero");
		bzero2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				chkDisable.setSelected(true);
				bzero2.setEnabled(false);
				zero();
				dispose();
			}
		});
		bzero2.setEnabled(false);
		pZero.add(bzero2);
		chkDisable = new JCheckBox("Disable Zero", true);
		chkDisable.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent event){
				if(chkDisable.isSelected()){
					bzero2.setEnabled(false);
				} else {
					bzero2.setEnabled(true);
				}
			}
		});
		pZero.add(chkDisable);
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
	private void zero() {
		scalersCmd.zeroScalers();
	}

	/**
	 * @see jam.data.control.AbstractControl#doSetup()
	 */
	public void doSetup() {
		/* nothing to set up */
	}
}
