package jam.data.control;
import jam.data.Scaler;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Reads and displays the scaler values.
 *
 * @version	0.5 April 98
 * @author 	Ken Swartz
 * @since       JDK1.1
 */

public final class ScalerDisplay
	extends DataControl
	implements ActionListener, ItemListener, Observer {

	private final Broadcaster broadcaster = Broadcaster.getSingletonInstance();
	private final MessageHandler messageHandler;

	private JPanel[] ps;
	private JLabel[] labelScaler;
	private JTextField[] textScaler;
	private final JPanel plower;
	private final JPanel pScalers;
	private final JCheckBox checkDisabled;
	private final JButton bzero;

	private boolean sortScalers; //have scalers been added by sort
	private final JamStatus status = JamStatus.instance();

	/** Creates the dialog box for reading and zeroing scalers.
	 * @param frame main window for application that this dialog is attached to
	 * @param messageHandler object to send text output to user to
	 */
	public ScalerDisplay(MessageHandler messageHandler) {
		super("Scalers", false);
		broadcaster.addObserver(this);
		this.messageHandler = messageHandler;
		sortScalers = false;

		// dialog box to display scalers
		final Container cddisp = getContentPane();
		setLocation(20, 50);
		cddisp.setLayout(new BorderLayout());

		pScalers = new JPanel(new GridLayout(0, 1, 10, 5));
		Border borderScalers = new EmptyBorder(10, 10, 10, 10);
		pScalers.setBorder(borderScalers);

		cddisp.add(pScalers, BorderLayout.CENTER);

		plower = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		JPanel pb = new JPanel(); // buttons for display dialog
		pb.setLayout(new GridLayout(1, 0, 10, 10));
		cddisp.add(plower, BorderLayout.SOUTH);

		JButton bupdate = new JButton("Read");
		bupdate.setActionCommand("scalerread");
		bupdate.addActionListener(this);
		pb.add(bupdate);
		bzero = new JButton("Zero");
		bzero.setActionCommand("scalzero");
		bzero.addActionListener(this);
		bzero.setEnabled(false);
		pb.add(bzero);
		plower.add(pb);

		checkDisabled = new JCheckBox("Disable Zero", true);
		checkDisabled.addItemListener(this);
		plower.add(checkDisabled);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		setup();
	}

	/** Action either read scalers or zero scalers.
	 * @param ae event from dialog box
	 */
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();
		if (command == "scalerread") {
			read();
		} else if (command == "scalzero") {
			checkDisabled.setSelected(true);
			bzero.setEnabled(false);
			ScalerZero.zero();
		} else {
			throw new UnsupportedOperationException(
				"Error Unregonized command: " + command);
		}
	}

	/** Handles events from checkboxes.
	 *
	 * @param ie a changed checkbox state
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getItemSelectable() == checkDisabled) {
			if (checkDisabled.isSelected()) {
				bzero.setEnabled(false);
			} else {
				bzero.setEnabled(true);
			}
		}
	}

	/**
	 * Default show dialog, shows the display dialog
	 */
	public void show() {
		displayScalers();
		super.show();
	}

	/**
	 * Setup the display dialog box.  Needs to be called if the list of <code>Scaler</code> objects
	 * changes, such as after opening a file, or initializing a sort routine.
	 */
	public void setup() {
		sortScalers = true;
		int numberScalers = Scaler.getScalerList().size();
		pScalers.removeAll();
		if (numberScalers != 0) { // we have some elements in the scaler list
			//gui widgets for each scaler
			ps = new JPanel[numberScalers];
			labelScaler = new JLabel[numberScalers];
			textScaler = new JTextField[numberScalers];
			Iterator enumScaler = Scaler.getScalerList().iterator();
			int count = 0;
			while (enumScaler.hasNext()) {
				Scaler currentScaler = (Scaler) enumScaler.next();
				/* right justified, hgap, vgap */
				ps[count] = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
				labelScaler[count] =
					new JLabel(currentScaler.getName().trim(), JLabel.RIGHT);
				textScaler[count] = new JTextField("  ");
				textScaler[count].setColumns(12);
				textScaler[count].setEditable(false);
				textScaler[count].setText(
					String.valueOf(currentScaler.getValue()));
				ps[count].add(labelScaler[count]);
				ps[count].add(textScaler[count]);
				pScalers.add(ps[count]);
				count++;
			}
		}
		pack();
		displayScalers();
	}

	/** Sets the text fields in the scaler display dialog box.
	 * @param scalValue array of scaler values, which <b>must</b> map to the array of text fields in the dialog box
	 */
	public void setScalers(int[] scalValue) {
		for (int i = 0; i < scalValue.length; i++) {
			textScaler[i].setText(String.valueOf(scalValue[i]));
		}
	}

	/**
	 * Read the scaler values send out command to read scalers
	 * which sould be recieved by VMECommunication
	 * VME should then send a command to CAMAC to read the
	 * scalers, when VME recieves back the scaler values it
	 * calls Distribute event which will call our update method.
	 */
	public void read() {
		if (status.isOnLine()) {
			broadcaster.broadcast(BroadcastEvent.SCALERS_READ);
		} else {
			displayScalers();
		}
	}


	/** Implementation of Observable interface.
	 * @param observable not sure
	 * @param o not sure
	 */
	public void update(Observable observable, Object o) {
		BroadcastEvent be = (BroadcastEvent) o;
		if (be.getCommand() == BroadcastEvent.SCALERS_UPDATE) {
			displayScalers();
		}
	}

	/**
	 * Get the values from the Scalers and
	 * display them
	 */
	public void displayScalers() {
		// we have some elements in the scaler list
		if (Scaler.getScalerList().size() != 0) {
			Iterator enumScaler = Scaler.getScalerList().iterator();
			sortScalers = true;
			int count = 0;
			while (enumScaler.hasNext()) {
				Scaler currentScaler = (Scaler) enumScaler.next();
				textScaler[count].setText(
					String.valueOf(currentScaler.getValue()));
				count++;
			}
		}
	}
}
