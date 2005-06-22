package jam.data.control;

import jam.commands.ScalersCmd;
import jam.data.Group;
import jam.data.Scaler;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Reads and displays the scaler values.
 * 
 * @version 0.5 April 98
 * @author Ken Swartz
 * @since JDK1.1
 */

public final class ScalerDisplay extends AbstractControl implements Observer {
	
	private final JScrollPane scrollPane;	
	private final JPanel pScalers;
	private JTextField[] textScaler;
	
	private final int borderHeight=5;
	
	private final ScalersCmd scalersCmd;
	
	private final Broadcaster broadcaster = Broadcaster.getSingletonInstance();
	
	private final JamStatus status = JamStatus.getSingletonInstance();	

	/**
	 * Creates the dialog box for reading and zeroing scalers.
	 */
	public ScalerDisplay() {
		super("Scalers", false);
		broadcaster.addObserver(this);
		scalersCmd = new ScalersCmd();
		/* dialog box to display scalers */
		final Container cddisp = getContentPane();
		setLocation(20, 50);
		cddisp.setLayout(new BorderLayout());
		pScalers = new JPanel(new GridLayout(0, 1, borderHeight, 5));
		Border borderScalers = new EmptyBorder(borderHeight, 10, borderHeight, 10);
		pScalers.setBorder(borderScalers);
		scrollPane = new JScrollPane(pScalers);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);		
		cddisp.add(scrollPane, BorderLayout.CENTER);
		cddisp.add(scrollPane, BorderLayout.CENTER);
		final JPanel plower = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,
				10));
		final JPanel pb = new JPanel(); // buttons for display dialog
		pb.setLayout(new GridLayout(1, 0, 10, 10));
		cddisp.add(plower, BorderLayout.SOUTH);
		final JButton bupdate = new JButton("Read");
		bupdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				read();
			}
		});
		pb.add(bupdate);
		final JButton bzero = new JButton("Zero");
		final JCheckBox checkDisabled = new JCheckBox("Disable Zero", true);
		bzero.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkDisabled.setSelected(true);
				bzero.setEnabled(false);
				scalersCmd.zeroScalers();
			}
		});
		bzero.setEnabled(false);
		pb.add(bzero);
		plower.add(pb);
		checkDisabled.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				bzero.setEnabled(!checkDisabled.isSelected());
			}
		});
		plower.add(checkDisabled);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
			public void windowActivated(WindowEvent e) {
				displayScalers();
			}
		});
		doSetup();
	}

	/**
	 * Setup the display dialog box. Needs to be called if the list of
	 * <code>Scaler</code> objects changes, such as after opening a file, or
	 * initializing a sort routine.
	 */
	public void doSetup() {
		Group currentGroup = status.getCurrentGroup();
		if (currentGroup==null)
			return;
		JPanel ps=null;
		List scalerList = currentGroup.getScalerList();
		int numberScalers =scalerList.size();
		pScalers.removeAll();
		if (numberScalers != 0) { // we have some elements in the scaler list
			/* gui widgets for each scaler */
			//ps = new JPanel[numberScalers];
			//labelScaler = new JLabel[numberScalers];
			textScaler = new JTextField[numberScalers];
			Iterator enumScaler = scalerList.iterator();
			int count = 0;
			while (enumScaler.hasNext()) {
				Scaler currentScaler = (Scaler) enumScaler.next();
				/* right justified, hgap, vgap */
				ps = new JPanel(new FlowLayout(FlowLayout.RIGHT,
						10, 0));
				final JLabel labelScaler = new JLabel(currentScaler.getName()
						.trim(), SwingConstants.RIGHT);
				textScaler[count] = new JTextField("  ");
				textScaler[count].setColumns(12);
				textScaler[count].setEditable(false);
				textScaler[count].setText(String.valueOf(currentScaler
						.getValue()));
				ps.add(labelScaler);
				ps.add(textScaler[count]);
				pScalers.add(ps);
				count++;
			}
		}
		pack();
		if (numberScalers>0) {
			Dimension dialogDim=calculateScrollDialogSize(this, ps, borderHeight, numberScalers);
			setSize(dialogDim);
		}
		
		displayScalers();
	}

	/**
	 * Sets the text fields in the scaler display dialog box.
	 * 
	 * @param scalValue
	 *            array of scaler values, which <b>must </b> map to the array of
	 *            text fields in the dialog box
	 */
	public void setScalers(int[] scalValue) {
		for (int i = 0; i < scalValue.length; i++) {
			textScaler[i].setText(String.valueOf(scalValue[i]));
		}
	}

	/**
	 * Read the scaler values send out command to read scalers which sould be
	 * recieved by VMECommunication VME should then send a command to CAMAC to
	 * read the scalers, when VME recieves back the scaler values it calls
	 * Distribute event which will call our update method.
	 */
	public void read() {
		if (STATUS.isOnline()) {
			broadcaster.broadcast(BroadcastEvent.Command.SCALERS_READ);
		} else {
			displayScalers();
		}
	}

	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            not sure
	 * @param o
	 *            not sure
	 */
	public void update(Observable observable, Object o) {
		BroadcastEvent be = (BroadcastEvent) o;
		if ( (be.getCommand() == BroadcastEvent.Command.HISTOGRAM_NEW) ||
   		     (be.getCommand() == BroadcastEvent.Command.HISTOGRAM_SELECT) ||
			 (be.getCommand() == BroadcastEvent.Command.GROUP_SELECT) ) {
			doSetup();
			
		}
		
		if (be.getCommand() == BroadcastEvent.Command.SCALERS_UPDATE) { 
				displayScalers();
		}
	}

	/**
	 * Get the values from the Scalers and display them
	 */
	public void displayScalers() {
		Group currentGroup = status.getCurrentGroup(); 
		if (currentGroup !=null) {
			List scalerList = currentGroup.getScalerList();		
			final Iterator iter = scalerList.iterator();
			int count = 0;
			while (iter.hasNext()) {
				final Scaler currentScaler = (Scaler) iter.next();
				textScaler[count].setText(String.valueOf(currentScaler.getValue()));
				count++;
			}
		}
	}
}