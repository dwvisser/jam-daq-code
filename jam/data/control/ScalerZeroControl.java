package jam.data.control;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;

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
public class ScalerZeroControl extends DataControl {

	private static final Broadcaster broadcaster = Broadcaster.getSingletonInstance();
	private static final JamStatus status = JamStatus.instance();
	private final JCheckBox checkDisabled2;
	private final JButton bzero2;

	public ScalerZeroControl() {
		super("Zero Scalers", true);
		Container dzc = getContentPane();
		setResizable(false);
		setLocation(20, 50);
		final JPanel pZero = new JPanel(new GridLayout(1, 0, 20, 20));
		Border border = new EmptyBorder(20, 20, 20, 20);
		pZero.setBorder(border);
		bzero2 = new JButton("Zero");
		bzero2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				checkDisabled2.setSelected(true);
				bzero2.setEnabled(false);
				dispose();
			}
		});
		bzero2.setEnabled(false);
		pZero.add(bzero2);
		checkDisabled2 = new JCheckBox("Disable Zero", true);
		checkDisabled2.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent ie){
				if(checkDisabled2.isSelected()){
					bzero2.setEnabled(false);
				} else {
					bzero2.setEnabled(true);
				}
			}
		});
		pZero.add(checkDisabled2);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
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
		if (!status.isOnLine()) {
			throw new IllegalStateException("Can't Zero Scalers when not in Online mode.");
		}
		broadcaster.broadcast(BroadcastEvent.SCALERS_CLEAR);
		broadcaster.broadcast(BroadcastEvent.SCALERS_READ);
	}

	/**
	 * @see jam.data.control.DataControl#setup()
	 */
	public void setup() {
		/* nothing to set up */
	}
}
