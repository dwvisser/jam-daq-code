package jam;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.sort.NetDaemon;
import jam.sort.SortDaemon;
import jam.sort.StorageDaemon;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Displays buffer countesr of sort threads.
 * Gives the number of buffers and events received
 * and sorted.
 *
 * @author Ken Swartz
 * @version 05 newest done 9-98
 */
public final class DisplayCounters extends JDialog implements Observer {

	/**
	 * We are sorting online when the internal mode variable equals
	 * this.
	 */
	public static final int ONLINE = 1;

	/**
	* We are sorting offline when the internal mode variable equals
	* this.
	*/
	public static final int OFFLINE = 2;

	//stuff for dialog box
	private JPanel pCenter;
	private SortDaemon sortDaemon;
	private NetDaemon netDaemon;
	private StorageDaemon storageDaemon;
	private int mode;
	private final Broadcaster broadcaster;
	private final MessageHandler messageHandler;

	//text fields
	private JTextField textFileRead,
		textBuffSent,
		textBuffRecv,
		textBuffSort,
		textBuffWrit,
		textEvntSent,
		textEvntSort;
	private JPanel pFileRead,
		pBuffSent,
		pBuffRecv,
		pBuffWrit,
		pBuffSort,
		pEvntSent,
		pEvntSort,
		pButton;

	static private DisplayCounters instance = null;

	static public DisplayCounters getSingletonInstance() {
		if (instance == null) {
			instance = new DisplayCounters();
		}
		return instance;
	}

	private final static JamStatus status = JamStatus.instance();

	/**
	 * @param jm the main window
	 * @param b to broadcast counter "read" and "zero" requests
	 * @param mh where to print console output
	 */
	private DisplayCounters() {
		super(status.getFrame(), "Buffer Counters", false);
		final int xpos = 20;
		final int ypos = 50;
		final int flowgaph = 10;
		final int flowgapv = 0;
		final int maingap = 10;
		final int hgap = 5;
		final int vgap = 10;

		broadcaster = Broadcaster.getSingletonInstance();
		broadcaster.addObserver(this);
		messageHandler = status.getMessageHandler();
		setResizable(false);
		setLocation(xpos, ypos);
		final Container cd = getContentPane();
		cd.setLayout(new BorderLayout(maingap, maingap));

		//Center Panels
		pCenter = new JPanel(new GridLayout(0, 1, hgap, vgap));
		cd.add(pCenter, BorderLayout.CENTER);
		pCenter.setBorder(new EmptyBorder(20, 10, 0, 10));

		pBuffSent =
			new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgaph, flowgapv));
		pBuffSent.add(new JLabel("Packets sent", JLabel.RIGHT));
		textBuffSent = newTextField();
		pBuffSent.add(textBuffSent);
		pBuffRecv =
			new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgaph, flowgapv));
		pBuffRecv.add(new JLabel("Packets received", JLabel.RIGHT));
		textBuffRecv = newTextField();
		pBuffRecv.add(textBuffRecv);
		pBuffSort =
			new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgaph, flowgapv));
		pBuffSort.add(new JLabel("Buffers sorted", JLabel.RIGHT));
		textBuffSort = newTextField();
		pBuffSort.add(textBuffSort);
		pBuffWrit =
			new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgaph, flowgapv));
		pBuffWrit.add(new JLabel("Buffers written", JLabel.RIGHT));
		textBuffWrit = newTextField();
		pBuffWrit.add(textBuffWrit);
		pEvntSent =
			new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgaph, flowgapv));
		pEvntSent.add(new JLabel("Events sent", JLabel.RIGHT));
		textEvntSent = newTextField();
		pEvntSent.add(textEvntSent);
		pEvntSort =
			new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgaph, flowgapv));
		pEvntSort.add(new JLabel("Events sorted", JLabel.RIGHT));
		textEvntSort = newTextField();
		pEvntSort.add(textEvntSort);
		pFileRead =
			new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgaph, flowgapv));
		pFileRead.add(new JLabel("Files read", JLabel.RIGHT));
		textFileRead = newTextField();
		pFileRead.add(textFileRead);

		/* panel for buttons */
		pButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cd.add(pButton, BorderLayout.SOUTH);
		JPanel pb = new JPanel(new GridLayout(1, 0, 5, 5));
		pButton.add(pb);
		pb.add(getUpdateButton());
		pb.add(getClearButton());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private JTextField newTextField() {
		final String emptyString = "";
		final int cols = 8;
		final JTextField rval = new JTextField(emptyString);
		rval.setColumns(cols);
		rval.setEditable(false);
		return rval;
	}

	private JButton getUpdateButton() {
		final JButton bupdate = new JButton("Update");
		bupdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (mode == ONLINE) {
					broadcaster.broadcast(BroadcastEvent.COUNTERS_READ);
					textEvntSort.setText(
						String.valueOf(sortDaemon.getEventCount()));
					textBuffSort.setText(
						String.valueOf(sortDaemon.getBufferCount()));
					textBuffRecv.setText(
						String.valueOf(netDaemon.getPacketCount()));
					textBuffWrit.setText(
						String.valueOf(storageDaemon.getBufferCount()));
				} else { //offline
					textEvntSort.setText(
						String.valueOf(sortDaemon.getEventCount()));
					textBuffSort.setText(
						String.valueOf(sortDaemon.getBufferCount()));
					textFileRead.setText(
						String.valueOf(storageDaemon.getFileCount()));
				}
			}
		});
		return bupdate;
	}

	private JButton getClearButton() {
		final JButton bclear = new JButton("Clear");
		bclear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final String space = " ";
				if (mode == ONLINE) {
					broadcaster.broadcast(BroadcastEvent.COUNTERS_ZERO);
					textBuffSort.setText(space);
					sortDaemon.setBufferCount(0);
					textBuffSort.setText(
						String.valueOf(sortDaemon.getBufferCount()));
					textEvntSent.setText(space);
					sortDaemon.setEventCount(0);
					textEvntSort.setText(
						String.valueOf(sortDaemon.getEventCount()));
					textBuffSent.setText(space); //value update method
					textEvntSent.setText(space); //value update method
					textBuffRecv.setText(space);
					netDaemon.setPacketCount(0);
					textBuffRecv.setText(
						String.valueOf(netDaemon.getPacketCount()));
					textBuffWrit.setText(space);
					storageDaemon.setBufferCount(0);
					textBuffWrit.setText(
						String.valueOf(storageDaemon.getBufferCount()));
					broadcaster.broadcast(BroadcastEvent.COUNTERS_READ);

				} else { //offline
					textBuffSort.setText(space);
					sortDaemon.setBufferCount(0);
					textBuffSort.setText(
						String.valueOf(sortDaemon.getBufferCount()));
					textEvntSent.setText(space);
					sortDaemon.setEventCount(0);
					textEvntSort.setText(
						String.valueOf(sortDaemon.getEventCount()));
					textFileRead.setText(space);
					storageDaemon.setFileCount(0);
					textFileRead.setText(
						String.valueOf(storageDaemon.getFileCount()));
				}
			}
		});
		return bclear;
	}

	/**
	 * Setup for online
	 *
	 * @param nd network process
	 * @param sod sorting process
	 * @param std event record storage process
	 */
	public void setupOn(NetDaemon nd, SortDaemon sod, StorageDaemon std) {
		synchronized (this) {
			mode = ONLINE;
			netDaemon = nd;
			sortDaemon = sod;
			storageDaemon = std;
		}
		setTitle("Online Buffer Count");
		pCenter.removeAll();
		pCenter.add(pBuffSent);
		pCenter.add(pBuffRecv);
		pCenter.add(pBuffSort);
		pCenter.add(pBuffWrit);
		pCenter.add(pEvntSent);
		pCenter.add(pEvntSort);
		pack();
	}

	/**
	 * Setup the dialog for offline sorting.
	 *
	 * @param sod the sorting process
	 * @param std the event record storage process
	 */
	public void setupOff(SortDaemon sod, StorageDaemon std) {
		synchronized (this) {
			mode = OFFLINE;
			sortDaemon = sod;
			storageDaemon = std;
		}
		setTitle("Offline Buffer Count");
		pCenter.removeAll();
		pCenter.add(pFileRead);
		pCenter.add(pBuffSort);
		pCenter.add(pEvntSort);
		pack();
	}

	/**
	 * Receive a broadcast event in order to update counters.
	 *
	 * @param observable the observed object
	 * @param o the communicated event
	 */
	public void update(Observable observable, Object o) {
		final int NUMBER_COUNTERS = 3;
		final int INDEX_CNT_EVNT = 1;
		final int INDEX_CNT_BUFF = 2;
		final BroadcastEvent be = (BroadcastEvent) o;
		final int command = be.getCommand();
		int vmeCounters[] = new int[NUMBER_COUNTERS];

		if (command == BroadcastEvent.COUNTERS_UPDATE) {

			//online only update remote fields
			if (mode == ONLINE) {
				vmeCounters = (int[]) be.getContent();
				textBuffSent.setText(
					String.valueOf(vmeCounters[INDEX_CNT_BUFF]));
				textEvntSent.setText(
					String.valueOf(vmeCounters[INDEX_CNT_EVNT]));

				//off line we have to update all fields
			} else {
				textBuffSort.setText(
					String.valueOf(sortDaemon.getBufferCount()));
				textEvntSort.setText(
					String.valueOf(sortDaemon.getEventCount()));
				textFileRead.setText(
					String.valueOf(storageDaemon.getFileCount()));
			}
		}
	}
}