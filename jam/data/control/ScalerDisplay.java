package jam.data.control;

import jam.data.DataBase;
import jam.data.DataElement;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.BroadcastUtilities;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.Nameable;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

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
import javax.swing.text.JTextComponent;

import com.google.inject.Inject;

/**
 * Reads and displays the scaler values.
 * 
 * @version 0.5 April 98
 * @author Ken Swartz
 * @since JDK1.1
 */

public final class ScalerDisplay extends AbstractControl {

	private static final int BORDER_HEIGHT = 5;

	private transient final JButton bupdate = new JButton("Read");

	private transient final JButton bzero = new JButton("Zero");

	private transient final JCheckBox checkDisabled = new JCheckBox(
			"Disable Zero", true);

	private transient final Object monitor = new Object();

	private transient final JPanel pScalers;

	private transient final JamStatus status;

	private transient final List<JTextField> textScaler = new ArrayList<>();

	private transient final BroadcastUtilities broadcast;

	/**
	 * Creates the dialog box for reading and zeroing scalers.
	 * 
	 * @param frame
	 *            application frame
	 * 
	 * @param broadcast
	 *            for broadcasting scaler commands
	 * @param status
	 *            application status
	 * @param broadcaster
	 *            broadcasts state changes
	 */
	@Inject
	public ScalerDisplay(final Frame frame, final BroadcastUtilities broadcast,
			final JamStatus status, final Broadcaster broadcaster) {
		super(frame, "Scalers", false, broadcaster);
		this.broadcast = broadcast;
		this.status = status;
		broadcaster.addObserver(this);
		/* dialog box to display scalers */
		final Container cddisp = getContentPane();
		setLocation(20, 50);
		cddisp.setLayout(new BorderLayout());
		pScalers = new JPanel(new GridLayout(0, 1, BORDER_HEIGHT, 5));
		final Border borderScalers = new EmptyBorder(BORDER_HEIGHT, 10,
				BORDER_HEIGHT, 10);
		pScalers.setBorder(borderScalers);
		final JScrollPane scrollPane = new JScrollPane(pScalers);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		cddisp.add(scrollPane, BorderLayout.CENTER);
		cddisp.add(scrollPane, BorderLayout.CENTER);
		addScalerControlPanel(cddisp);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(final WindowEvent event) {
				displayScalers();
			}

			@Override
			public void windowClosing(final WindowEvent event) {
				dispose();
			}
		});
		doSetup();
	}

	/**
	 * @param cddisp
	 */
	private void addScalerControlPanel(final Container cddisp) {
		final JPanel plower = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,
				10));
		final JPanel buttonPanel = new JPanel(); // buttons for display
		// dialog
		buttonPanel.setLayout(new GridLayout(1, 0, 10, 10));
		cddisp.add(plower, BorderLayout.SOUTH);
		bupdate.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				read();
			}
		});
		buttonPanel.add(bupdate);
		bzero.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				checkDisabled.setSelected(true);
				bzero.setEnabled(false);
				ScalerDisplay.this.broadcast.zeroScalers();
			}
		});
		bzero.setEnabled(false);
		buttonPanel.add(bzero);
		plower.add(buttonPanel);
		checkDisabled.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
				bzero.setEnabled(!checkDisabled.isSelected());
			}
		});
		plower.add(checkDisabled);
	}

	/**
	 * @param currentScaler
	 *            to creat label for
	 * @return label for scaler panel
	 */
	private JLabel createScalerLabel(final DataElement currentScaler) {
		return new JLabel(currentScaler.getName().trim(), SwingConstants.RIGHT);
	}

	/**
	 * @return new panel for scaler value
	 */
	private JPanel createScalerPanel() {
		return new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
	}

	/**
	 * @param count
	 *            index of scaler
	 * @param currentScaler
	 *            scaler to use value of
	 */
	private JTextField createScalerTextField(final DataElement currentScaler) {
		final JTextField rval = new JTextField("  ");
		textScaler.add(rval);
		rval.setColumns(12);
		rval.setEditable(false);
		rval.setText(String.valueOf((int) currentScaler.getCount()));
		return rval;
	}

	/**
	 * Get the values from the Scalers and display them
	 */
	public void displayScalers() {
		synchronized (monitor) {
			final Nameable nameable = status.getCurrentGroup();
			if (DataBase.getInstance().isValid(nameable)) {
				final Group currentGroup = (Group) nameable;
				final List<? extends DataElement> scalerList = currentGroup
						.getScalerList();
				if (textScaler.size() != scalerList.size()) {
					doSetup();
				}
				final Iterator<JTextField> txtIterator = textScaler.iterator();
				for (DataElement currentScaler : currentGroup.getScalerList()) {
					final JTextComponent text = txtIterator.next();
					if (text == null) {
						throw new IllegalStateException(
								"Text fields don't exist for scalers when they should.");
					}
					text
							.setText(String.valueOf((int) currentScaler
									.getCount()));
				}
			}
		}
	}

	/**
	 * Setup the display dialog box. Needs to be called if the list of
	 * <code>Scaler</code> objects changes, such as after opening a file, or
	 * initializing a sort routine.
	 */
	@Override
	public void doSetup() {
		synchronized (monitor) {
			final Nameable nameable = status.getCurrentGroup();
			if (DataBase.getInstance().isValid(nameable)) {
				final Group currentGroup = (Group) nameable;
				JPanel panelS = null;
				final List<? extends DataElement> scalerList = currentGroup
						.getScalerList();
				final int numberScalers = scalerList.size();
				pScalers.removeAll();
				textScaler.clear();
				if (numberScalers != 0) {
					/* We have some elements in the scaler list. */
					for (DataElement currentScaler : scalerList) {
						panelS = createScalerPanel();
						final JLabel labelScaler = createScalerLabel(currentScaler);
						final JTextField text = createScalerTextField(currentScaler);
						panelS.add(labelScaler);
						panelS.add(text);
						pScalers.add(panelS);
					}
				}
				final boolean online = status.isOnline();
				bupdate.setEnabled(online);
				bzero.setEnabled(online && bzero.isEnabled());
				checkDisabled.setEnabled(online);
				pack();
				if (numberScalers > 0) {
					final Dimension dialogDim = calculateScrollDialogSize(this,
							panelS, BORDER_HEIGHT, numberScalers);
					setSize(dialogDim);
				}
				displayScalers();
			}
		}
	}

	/**
	 * Read the scaler values send out command to read scalers, which should be
	 * received by VMECommunication. VME should then send a command to CAMAC to
	 * read the scalers, when VME receives back the scaler values it calls
	 * distribute event which will call our update method.
	 */
	public void read() {
		if (this.status.isOnline()) {
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
	 * @param event
	 *            not sure
	 */
	@Override
	public void update(final Observable observable, final Object event) {
		final BroadcastEvent jamEvent = (BroadcastEvent) event;
		if ((jamEvent.getCommand() == BroadcastEvent.Command.HISTOGRAM_NEW)
				|| (jamEvent.getCommand() == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (jamEvent.getCommand() == BroadcastEvent.Command.GROUP_SELECT)) {
			doSetup();
		}
		if (jamEvent.getCommand() == BroadcastEvent.Command.SCALERS_UPDATE) {
			displayScalers();
		}
	}
}