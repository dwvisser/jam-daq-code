package jam;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
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
 * Displays buffer counters of sort threads. Gives the number of buffers and
 * events received and sorted.
 * 
 * @author Ken Swartz
 * @version 05 newest done 9-98
 */
public final class DisplayCounters extends JDialog implements Observer {

	/**
	 * We are sorting online when the internal mode variable equals this.
	 */
	//public static final int ONLINE = 1;

	/**
	 * We are sorting offline when the internal mode variable equals this.
	 */
	//public static final int OFFLINE = 2;

	//stuff for dialog box
	private transient final JPanel pCenter;

	private transient SortDaemon sortDaemon;

	private transient NetDaemon netDaemon;

	private transient StorageDaemon storeDaemon;

	private transient final Broadcaster broadcaster;

	private transient final JTextField textFileRead = new JTextField();

	private transient final JTextField textBuffSent = new JTextField();

	private transient final JTextField textBuffRecv = new JTextField();

	private transient final JTextField textBuffSort = new JTextField();

	private transient final JTextField textBuffWrit = new JTextField();

	private transient final JTextField textEvntSent = new JTextField();

	private transient final JTextField textEvntRecv = new JTextField();

	private transient final JTextField textEvntSort = new JTextField();
	
	private transient final JTextField sortSample = new JTextField();
	
	private transient final JPanel pSortSample=new JPanel();

	private transient final JPanel pFileRead = new JPanel();

	private transient final JPanel pBuffSent = new JPanel();

	private transient final JPanel pBuffRecv = new JPanel();

	private transient final JPanel pBuffWrit = new JPanel();

	private transient final JPanel pBuffSort = new JPanel();

	private transient final JPanel pEvntSent = new JPanel();

	private transient final JPanel pEvntSort = new JPanel();

	private transient final JPanel pEvntRecv = new JPanel();

	private transient JPanel pButton;

	static private DisplayCounters instance = null;

	static public DisplayCounters getSingletonInstance() {
		if (instance == null) {
			instance = new DisplayCounters();
		}
		return instance;
	}

	private final static JamStatus STATUS = JamStatus.instance();

	/**
	 * @param jm
	 *            the main window
	 * @param b
	 *            to broadcast counter "read" and "zero" requests
	 * @param mh
	 *            where to print console output
	 */
	private DisplayCounters() {
		super(STATUS.getFrame(), "Buffer Counters", false);
		final int xpos = 20;
		final int ypos = 50;
		final int maingap = 10;
		final int hgap = 5;
		final int vgap = 10;
		broadcaster = Broadcaster.getSingletonInstance();
		broadcaster.addObserver(this);
		setResizable(false);
		setLocation(xpos, ypos);
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout(maingap, maingap));
		/* Center Panels */
		pCenter = new JPanel(new GridLayout(0, 1, hgap, vgap));
		contents.add(pCenter, BorderLayout.CENTER);
		pCenter.setBorder(new EmptyBorder(20, 10, 0, 10));
		/* Individual counter panels. */
		newPanel(pBuffSent, "Packets sent", textBuffSent);
		newPanel(pBuffRecv, "Packets received", textBuffRecv);
		newPanel(pBuffSort, "Buffers sorted", textBuffSort);
		newPanel(pBuffWrit, "Buffers written", textBuffWrit);
		newPanel(pEvntSent, "Events sent", textEvntSent);
		newPanel(pEvntRecv, "Events received", textEvntRecv);
		newPanel(pEvntSort, "Events sorted", textEvntSort);
		newPanel(pFileRead, "Files read", textFileRead);
		newPanel(pSortSample, "Current sampling fraction", sortSample);
		/* panel for buttons */
		pButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contents.add(pButton, BorderLayout.SOUTH);
		final JPanel tempPanel = new JPanel(new GridLayout(1, 0, 5, 5));
		pButton.add(tempPanel);
		tempPanel.add(getUpdateButton());
		tempPanel.add(getClearButton());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void newPanel(JPanel panel, String text, JTextField field) {
		final String emptyString = "";
		final int cols = 8;
		final int flowgaph = 10;
		final int flowgapv = 0;
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, flowgaph, flowgapv));
		final JLabel label = new JLabel(text, JLabel.RIGHT);
		field.setText(emptyString);
		field.setColumns(cols);
		field.setEditable(false);
		panel.add(label);
		panel.add(field);
	}

	private JButton getUpdateButton() {
		final JButton bupdate = new JButton("Update");
		bupdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (STATUS.getSortMode().isOnline()) {
					broadcaster.broadcast(BroadcastEvent.Command.COUNTERS_READ);
					textBuffRecv.setText(String.valueOf(netDaemon
							.getPacketCount()));
					textBuffSort.setText(String.valueOf(sortDaemon
							.getBufferCount()));
					textBuffWrit.setText(String.valueOf(storeDaemon
							.getBufferCount()));
					textEvntRecv.setText(String.valueOf(sortDaemon
							.getEventCount()));
					textEvntSort.setText(String.valueOf(sortDaemon
							.getSortedCount()));
					updateSample();
				} else { //offline
					textEvntSort.setText(String.valueOf(sortDaemon
							.getSortedCount()));
					textBuffSort.setText(String.valueOf(sortDaemon
							.getBufferCount()));
					textFileRead.setText(String.valueOf(storeDaemon
							.getFileCount()));
				}
			}
		});
		return bupdate;
	}

	private JButton getClearButton() {
		final JButton bclear = new JButton("Clear");
		bclear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final String space = " ";
				if (STATUS.getSortMode().isOnline()) {
					broadcaster.broadcast(BroadcastEvent.Command.COUNTERS_ZERO);
					textBuffSort.setText(space);
					sortDaemon.setBufferCount(0);
					textBuffSort.setText(String.valueOf(sortDaemon
							.getBufferCount()));
					textEvntSent.setText(space);
					sortDaemon.setEventCount(0);
					sortDaemon.setSortedCount(0);
					textEvntRecv.setText(String.valueOf(sortDaemon
							.getEventCount()));
					textEvntSort.setText(String.valueOf(sortDaemon
							.getSortedCount()));
					textBuffSent.setText(space); //value update method
					textEvntSent.setText(space); //value update method
					textBuffRecv.setText(space);
					netDaemon.setPacketCount(0);
					textBuffRecv.setText(String.valueOf(netDaemon
							.getPacketCount()));
					textBuffWrit.setText(space);
					storeDaemon.setBufferCount(0);
					textBuffWrit.setText(String.valueOf(storeDaemon
							.getBufferCount()));
					sortSample.setText(space);
					broadcaster.broadcast(BroadcastEvent.Command.COUNTERS_READ);

				} else { //offline
					textBuffSort.setText(space);
					sortDaemon.setBufferCount(0);
					textBuffSort.setText(String.valueOf(sortDaemon
							.getBufferCount()));
					textEvntSent.setText(space);
					sortDaemon.setEventCount(0);
					sortDaemon.setSortedCount(0);
					textEvntSort.setText(String.valueOf(sortDaemon
							.getEventCount()));
					textFileRead.setText(space);
					storeDaemon.setFileCount(0);
					textFileRead.setText(String.valueOf(storeDaemon
							.getFileCount()));
				}
			}
		});
		return bclear;
	}

	/**
	 * Setup for online
	 * 
	 * @param net
	 *            network process
	 * @param sod
	 *            sorting process
	 * @param std
	 *            event record storage process
	 */
	public void setupOn(NetDaemon net, SortDaemon sod, StorageDaemon std) {
		synchronized (this) {
			//mode = ONLINE;
			netDaemon = net;
			sortDaemon = sod;
			storeDaemon = std;
		}
		setTitle("Online Buffer Count");
		pCenter.removeAll();
		pCenter.add(pBuffSent);
		pCenter.add(pBuffRecv);
		pCenter.add(pBuffSort);
		pCenter.add(pBuffWrit);
		pCenter.add(pEvntSent);
		pCenter.add(pEvntRecv);
		pCenter.add(pEvntSort);
		pCenter.add(pSortSample);
		pack();
	}

	/**
	 * Setup the dialog for offline sorting.
	 * 
	 * @param sod
	 *            the sorting process
	 * @param std
	 *            the event record storage process
	 */
	public void setupOff(SortDaemon sod, StorageDaemon std) {
		synchronized (this) {
			//mode = OFFLINE;
			sortDaemon = sod;
			storeDaemon = std;
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
	 * @param observable
	 *            the observed object
	 * @param object
	 *            the communicated event
	 */
	public void update(Observable observable, Object object) {
		final int numCounters = 3;
		final int iEventCount = 1;
		final int iBufferCt = 2;
		final BroadcastEvent event = (BroadcastEvent) object;
		final BroadcastEvent.Command command = event.getCommand();
		int vmeCounters[] = new int[numCounters];
		if (command == BroadcastEvent.Command.COUNTERS_UPDATE) {
			if (STATUS.getSortMode().isOnline()) {
				/* update remote fields */
				vmeCounters = (int[]) event.getContent();
				textBuffSent.setText(String
						.valueOf(vmeCounters[iBufferCt]));
				textEvntSent.setText(String
						.valueOf(vmeCounters[iEventCount]));
				updateSample();
			} else {
				/* update fields used in OFFLINE mode */
				textBuffSort.setText(String
						.valueOf(sortDaemon.getBufferCount()));
				textEvntSort.setText(String
						.valueOf(sortDaemon.getSortedCount()));
				textFileRead.setText(String.valueOf(storeDaemon
						.getFileCount()));
			}
		}
	}
	
	private void updateSample(){
		final int sample=sortDaemon.getSortInterval();
		final StringBuffer buffer=new StringBuffer();
		if (sample>1){
			buffer.append("1/");
		}
		buffer.append(sample);
		sortSample.setText(buffer.toString());		
	}
}