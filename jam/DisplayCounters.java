package jam;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GlobalException;
import jam.global.MessageHandler;
import jam.sort.NetDaemon;
import jam.sort.SortDaemon;
import jam.sort.StorageDaemon;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Displays buffer countesr of sort threads.
 * Gives the number of buffers and events received
 * and sorted.
 *
 * @author Ken Swartz
 * @version 05 newest done 9-98
 */
class DisplayCounters implements Observer {

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
	private JDialog d;
	private SortDaemon sortDaemon;
	private NetDaemon netDaemon;
	private StorageDaemon storageDaemon;
	private int mode;
	private final Frame jamMain;
	private final Broadcaster broadcaster;
	private final MessageHandler messageHandler;
	private static final String hyphen=" - ";

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

	/**
	 * @param jm the main window
	 * @param b to broadcast counter "read" and "zero" requests
	 * @param mh where to print console output
	 */
	DisplayCounters(Frame jm, Broadcaster b, MessageHandler mh) {
		final int xpos = 20;
		final int ypos = 50;
		final int flowgap = 5;
		final int maingap = 10;
		this.jamMain = jm;
		this.broadcaster = b;
		this.messageHandler = mh;
		d = new JDialog(jamMain, "Buffer Counters", false);
		d.setResizable(false);
		d.setLocation(xpos, ypos);
		final Container cd = d.getContentPane();
		cd.setLayout(new GridLayout(0, 1, maingap, maingap));
		pBuffSent = new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgap, flowgap));
		pBuffSent.add(new JLabel("Packets sent", JLabel.RIGHT));
		textBuffSent = newTextField();
		pBuffSent.add(textBuffSent);
		pBuffRecv = new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgap, flowgap));
		pBuffRecv.add(new JLabel("Packets received", JLabel.RIGHT));
		textBuffRecv = newTextField();
		pBuffRecv.add(textBuffRecv);
		pBuffSort = new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgap, flowgap));
		pBuffSort.add(new JLabel("Buffers sorted", JLabel.RIGHT));
		textBuffSort = newTextField();
		pBuffSort.add(textBuffSort);
		pBuffWrit = new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgap, flowgap));
		pBuffWrit.add(new JLabel("Buffers written", JLabel.RIGHT));
		textBuffWrit = newTextField();
		pBuffWrit.add(textBuffWrit);
		pEvntSent = new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgap, flowgap));
		pEvntSent.add(new JLabel("Events sent", JLabel.RIGHT));
		textEvntSent = newTextField();
		pEvntSent.add(textEvntSent);
		pEvntSort = new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgap, flowgap));
		pEvntSort.add(new JLabel("Events sorted", JLabel.RIGHT));
		textEvntSort = newTextField();
		pEvntSort.add(textEvntSort);
		pFileRead = new JPanel(new FlowLayout(FlowLayout.RIGHT, flowgap, flowgap));
		pFileRead.add(new JLabel("Files read", JLabel.RIGHT));
		textFileRead = newTextField();
		pFileRead.add(textFileRead);
		/* panel for buttons */
		pButton = new JPanel(new GridLayout(1, 0, flowgap, flowgap));
		pButton.add(getUpdateButton());
		pButton.add(getClearButton());
		/*Recieves events for closing the dialog box and closes it. */
		d.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				d.dispose();
			}
		});
	}

	private JTextField newTextField(){
		final String emptyString="";
		final int cols =8;
		final JTextField rval=new JTextField(emptyString);
		rval.setColumns(cols);
		rval.setEditable(false);
		return rval;
	}

	private JButton getUpdateButton() {
		final JButton bupdate = new JButton("Update");
		bupdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
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
				} catch (GlobalException ge) {
					messageHandler.errorOutln(
						getClass().getName() + hyphen + ge);
				}
			}
		});
		return bupdate;
	}

	private JButton getClearButton() {
		final JButton bclear = new JButton("Clear");
		bclear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final String space=" ";
				try {
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
				} catch (GlobalException ge) {
					messageHandler.errorOutln(
						getClass().getName() + hyphen + ge);
				}

			}
		});
		return bclear;
	}

	/**
	 * Show online sorting dialog Box
	 */
	public void show() {
		d.show();
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
			this.netDaemon = nd;
			this.sortDaemon = sod;
			this.storageDaemon = std;
		}
		/* make dialog box */
		final Container cd = d.getContentPane();
		cd.removeAll();
		d.setTitle("Online Buffer Count");
		cd.add(pBuffSent);
		cd.add(pBuffRecv);
		cd.add(pBuffSort);
		cd.add(pBuffWrit);
		cd.add(pEvntSent);
		cd.add(pEvntSort);
		cd.add(pButton);
		d.pack();
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
			this.sortDaemon = sod;
			this.storageDaemon = std;
		}
		final Container cd = d.getContentPane();
		cd.removeAll();
		d.setTitle("Offline Buffer Count");
		cd.add(pFileRead);
		cd.add(pBuffSort);
		cd.add(pEvntSort);
		cd.add(pButton);
		d.pack();
	}

	/**
	 * Receive a broadcast event in order to update counters.
	 *
	 * @author Ken Swartz
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