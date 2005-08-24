package jam;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.sort.NetDaemon;
import jam.sort.SortDaemon;
import jam.sort.StorageDaemon;
import jam.ui.CounterPanel;

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
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Displays buffer counters of sort threads. Gives the number of buffers and
 * events received and sorted.
 * 
 * @author Ken Swartz
 * @version 05 newest done 9-98
 */
public final class DisplayCounters extends JDialog implements Observer {

	private transient final JPanel pCenter;

	private transient SortDaemon sortDaemon;

	private transient NetDaemon netDaemon;

	private transient StorageDaemon storeDaemon;

	private transient final Broadcaster broadcaster;

	private transient final CounterPanel pSortSample = new CounterPanel("Current sampling fraction");

	private transient final CounterPanel pFileRead = new CounterPanel("Files read");

	private transient final CounterPanel pBuffSent = new CounterPanel("Packets sent");

	private transient final CounterPanel pBuffRecv = new CounterPanel("Packets received");

	private transient final CounterPanel pBuffWrit = new CounterPanel("Buffers written");

	private transient final CounterPanel pBuffSort = new CounterPanel("Buffers sorted");

	private transient final CounterPanel pEvntSent = new CounterPanel("Events sent");

	private transient final CounterPanel pEvntSort = new CounterPanel("Events sorted");

	private transient final CounterPanel pEvntRecv = new CounterPanel("Events received");

	static private DisplayCounters instance = null;

	/**
	 * @return the only instance of this class
	 */
	static public DisplayCounters getSingletonInstance() {
		if (instance == null) {
			instance = new DisplayCounters();
		}
		return instance;
	}

	private final static JamStatus STATUS = JamStatus.getSingletonInstance();

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
		/* panel for buttons */
		final JPanel pButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contents.add(pButton, BorderLayout.SOUTH);
		final JPanel tempPanel = new JPanel(new GridLayout(1, 0, 5, 5));
		pButton.add(tempPanel);
		tempPanel.add(getUpdateButton());
		tempPanel.add(getClearButton());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private JButton getUpdateButton() {
		final JButton bupdate = new JButton("Update");
		bupdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (STATUS.getSortMode().isOnline()) {
					broadcaster.broadcast(BroadcastEvent.Command.COUNTERS_READ);
					pBuffRecv.setText(String.valueOf(netDaemon
							.getPacketCount()));
					pBuffSort.setText(String.valueOf(sortDaemon
							.getBufferCount()));
					pBuffWrit.setText(String.valueOf(storeDaemon
							.getBufferCount()));
					pEvntRecv.setText(String.valueOf(sortDaemon
							.getEventCount()));
					pEvntSort.setText(String.valueOf(sortDaemon
							.getSortedCount()));
					updateSample();
				} else { // offline
					pEvntSort.setText(String.valueOf(sortDaemon
							.getSortedCount()));
					pBuffSort.setText(String.valueOf(sortDaemon
							.getBufferCount()));
					pFileRead.setText(String.valueOf(storeDaemon
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
					pBuffSort.setText(space);
					sortDaemon.setBufferCount(0);
					pBuffSort.setText(String.valueOf(sortDaemon
							.getBufferCount()));
					pEvntSent.setText(space);
					sortDaemon.setEventCount(0);
					sortDaemon.setSortedCount(0);
					pEvntRecv.setText(String.valueOf(sortDaemon
							.getEventCount()));
					pEvntSort.setText(String.valueOf(sortDaemon
							.getSortedCount()));
					pBuffSent.setText(space); // value update method
					pEvntSent.setText(space); // value update method
					pBuffRecv.setText(space);
					netDaemon.setPacketCount(0);
					pBuffRecv.setText(String.valueOf(netDaemon
							.getPacketCount()));
					pBuffWrit.setText(space);
					storeDaemon.setBufferCount(0);
					pBuffWrit.setText(String.valueOf(storeDaemon
							.getBufferCount()));
					pSortSample.setText(space);
					broadcaster.broadcast(BroadcastEvent.Command.COUNTERS_READ);

				} else { // offline
					pBuffSort.setText(space);
					sortDaemon.setBufferCount(0);
					pBuffSort.setText(String.valueOf(sortDaemon
							.getBufferCount()));
					pEvntSent.setText(space);
					sortDaemon.setEventCount(0);
					sortDaemon.setSortedCount(0);
					pEvntSort.setText(String.valueOf(sortDaemon
							.getEventCount()));
					pFileRead.setText(space);
					storeDaemon.setFileCount(0);
					pFileRead.setText(String.valueOf(storeDaemon
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
	public void setupOn(final NetDaemon net, final SortDaemon sod,
			final StorageDaemon std) {
		synchronized (this) {
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
	public void setupOff(final SortDaemon sod, final StorageDaemon std) {
		synchronized (this) {
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
	public void update(final Observable observable, final Object object) {
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
				pBuffSent.setText(String.valueOf(vmeCounters[iBufferCt]));
				pEvntSent.setText(String.valueOf(vmeCounters[iEventCount]));
				updateSample();
			} else {
				/* update fields used in OFFLINE mode */
				pBuffSort.setText(String
						.valueOf(sortDaemon.getBufferCount()));
				pEvntSort.setText(String
						.valueOf(sortDaemon.getSortedCount()));
				pFileRead
						.setText(String.valueOf(storeDaemon.getFileCount()));
			}
		}
	}

	private void updateSample() {
		final int sample = sortDaemon.getSortInterval();
		final StringBuffer buffer = new StringBuffer();
		if (sample > 1) {
			buffer.append("1/");
		}
		buffer.append(sample);
		pSortSample.setText(buffer.toString());
	}
}