package jam.sort.control;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.sort.AbstractStorageDaemon;
import jam.sort.NetDaemon;
import jam.sort.SortDaemon;

/**
 * Displays buffer counters of sort threads. Gives the number of buffers and
 * events received and sorted.
 * 
 * @author Ken Swartz
 * @version 05 newest done 9-98
 */
@Singleton

public final class DisplayCounters extends JDialog implements PropertyChangeListener {// NOPMD

	private transient final JamStatus status;

	private transient final Broadcaster broadcaster;

	private transient NetDaemon netDaemon;

	private transient final CounterPanel pBuffRecv = new CounterPanel(
			"Packets received");

	private transient final CounterPanel pBuffSent = new CounterPanel(
			"Packets sent");

	private transient final CounterPanel pBuffSort = new CounterPanel(
			"Buffers sorted");

	private transient final CounterPanel pBuffWrit = new CounterPanel(
			"Buffers written");

	private transient final JPanel pCenter;

	private transient final CounterPanel pEvntRecv = new CounterPanel(
			"Events received");

	private transient final CounterPanel pEvntSent = new CounterPanel(
			"Events sent");

	private transient final CounterPanel pEvntSort = new CounterPanel(
			"Events sorted");

	private transient final CounterPanel pFileRead = new CounterPanel(
			"Files read");

	private transient final CounterPanel pSortSample = new CounterPanel(
			"Current sampling fraction");

	private transient SortDaemon sortDaemon;

	private transient AbstractStorageDaemon storeDaemon;

	@Inject
	private DisplayCounters(final JamStatus status, final Frame frame,
			final Broadcaster broadcaster) {
		super(frame, "Buffer Counters", false);
		this.status = status;
		final int xpos = 20;
		final int ypos = 50;
		final int maingap = 10;
		final int hgap = 5;
		final int vgap = 10;
		this.broadcaster = broadcaster;
		broadcaster.addPropertyChangeListener(this);
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

	private JButton getClearButton() {
		final JButton bclear = new JButton("Clear");
		bclear.addActionListener(event -> {
            final String space = " ";
            if (status.getSortMode().isOnline()) {
                broadcaster.broadcast(BroadcastEvent.Command.COUNTERS_ZERO);
                pBuffSort.setText(space);
                sortDaemon.setBufferCount(0);
                pBuffSort.setText(String.valueOf(sortDaemon
                        .getBufferCount()));
                pEvntSent.setText(space);
                sortDaemon.setEventCount(0);
                sortDaemon.setSortedCount(0);
                pEvntRecv.setText(String
                        .valueOf(sortDaemon.getEventCount()));
                pEvntSort.setText(String.valueOf(sortDaemon
                        .getSortedCount()));
                pBuffSent.setText(space); // value update method
                pEvntSent.setText(space); // value update method
                pBuffRecv.setText(space);
                netDaemon.setPacketCount(0);
                pBuffRecv.setText(String
                        .valueOf(netDaemon.getPacketCount()));
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
                pEvntSort.setText(String
                        .valueOf(sortDaemon.getEventCount()));
                pFileRead.setText(space);
                storeDaemon.setFileCount(0);
                pFileRead.setText(String
                        .valueOf(storeDaemon.getFileCount()));
            }
        });
		return bclear;
	}

	private JButton getUpdateButton() {
		final JButton bupdate = new JButton("Update");
		bupdate.addActionListener(event -> {
            if (status.getSortMode().isOnline()) {
                broadcaster.broadcast(BroadcastEvent.Command.COUNTERS_READ);
                pBuffRecv.setText(String
                        .valueOf(netDaemon.getPacketCount()));
                pBuffSort.setText(String.valueOf(sortDaemon
                        .getBufferCount()));
                pBuffWrit.setText(String.valueOf(storeDaemon
                        .getBufferCount()));
                pEvntRecv.setText(String
                        .valueOf(sortDaemon.getEventCount()));
                pEvntSort.setText(String.valueOf(sortDaemon
                        .getSortedCount()));
                updateSample();
            } else { // offline
                pEvntSort.setText(String.valueOf(sortDaemon
                        .getSortedCount()));
                pBuffSort.setText(String.valueOf(sortDaemon
                        .getBufferCount()));
                pFileRead.setText(String
                        .valueOf(storeDaemon.getFileCount()));
            }
        });
		return bupdate;
	}

	/**
	 * Setup the dialog for offline sorting.
	 * 
	 * @param sod
	 *            the sorting process
	 * @param std
	 *            the event record storage process
	 */
	public void setupOff(final SortDaemon sod, final AbstractStorageDaemon std) {
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
			final AbstractStorageDaemon std) {
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final int iEventCount = 1;
		final int iBufferCt = 2;
		final BroadcastEvent event = (BroadcastEvent) evt;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.COUNTERS_UPDATE) {
			if (status.getSortMode().isOnline()) {
				/* update remote fields */
				final int[] vmeCounters = (int[]) event.getContent();
				pBuffSent.setText(String.valueOf(vmeCounters[iBufferCt]));
				pEvntSent.setText(String.valueOf(vmeCounters[iEventCount]));
				updateSample();
			} else {
				/* update fields used in OFFLINE mode */
				pBuffSort.setText(String.valueOf(sortDaemon.getBufferCount()));
				pEvntSort.setText(String.valueOf(sortDaemon.getSortedCount()));
				pFileRead.setText(String.valueOf(storeDaemon.getFileCount()));
			}
		}
	}

	private void updateSample() {
		final int sample = sortDaemon.getSortInterval();
		final StringBuilder buffer = new StringBuilder();
		if (sample > 1) {
			buffer.append("1/");
		}
		buffer.append(sample);
		pSortSample.setText(buffer.toString());
	}
}