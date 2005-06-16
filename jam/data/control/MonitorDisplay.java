package jam.data.control;

import jam.data.Monitor;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

/**
 * 
 * Displays the monitors
 * 
 * @author Ken Swartz
 * 
 */
public class MonitorDisplay extends AbstractControl implements Observer {

	private final int borderHeight = 5;

	private JToggleButton checkAudio;

	private JPanel pBars;

	/**
	 * Constructs a new monitor display dialog.
	 */
	public MonitorDisplay() {
		super("Monitors Disabled", false);
		// >> dialog box to display Monitors
		setResizable(true);
		setLocation(20, 50);
		Container cddisp = this.getContentPane();
		cddisp.setLayout(new BorderLayout());
		// Panel for the bars
		pBars = new JPanel(new GridLayout(0, 1, borderHeight, 5));
		pBars.setBorder(new EmptyBorder(borderHeight, 0, borderHeight, 0));
		// Scroll Panel
		JScrollPane scrollPane = new JScrollPane(pBars);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		cddisp.add(scrollPane, BorderLayout.CENTER);
		// Panel for alarm
		final JPanel pal = new JPanel();
		cddisp.add(pal, BorderLayout.SOUTH);
		pal.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		checkAudio = new JCheckBox("Audio Alarm", true);
		pal.add(checkAudio);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void disableMonitors() {
		setTitle("Monitors Disabled");
		pBars.repaint();
	}

	private void displayMonitors() {
		// loop for each monitor check if we should sound alarm
		for (Iterator it = Monitor.getMonitorList().iterator(); it.hasNext();) {
			final Monitor monitor = (Monitor) it.next();
			// If the audio on and are we taking data
			if (checkAudio.isSelected()
					&& JamStatus.getSingletonInstance().isAcqOn()
					&& monitor.getAlarm() && (!monitor.isAcceptable())) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
		// display monitors
		pBars.repaint();
	}

	/**
	 * Setup the display of monitors, inherited for AbstractControl
	 */
	public void doSetup() {
		JPanel pm = null;
		int numberMonitors;
		numberMonitors = Monitor.getMonitorList().size();
		Iterator monitorsIter = Monitor.getMonitorList().iterator();
		pBars.removeAll();
		while (monitorsIter.hasNext()) {
			Monitor monitor = (Monitor) monitorsIter.next();
			pm = new JPanel();
			pm.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
			pBars.add(pm);
			final JLabel labelDisp = new JLabel(monitor.getName(), JLabel.RIGHT);
			pm.add(labelDisp);
			final PlotBar plotBar = new PlotBar(monitor);
			pm.add(plotBar);
		}
		pack();
		if (numberMonitors > 0) {
			Dimension dialogDim = calculateScrollDialogSize(this, pm,
					borderHeight, numberMonitors);
			setSize(dialogDim);
		}
	}

	private void enableMonitors() {
		setTitle("Monitors Enabled");
		pBars.repaint();
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
		if (be.getCommand() == BroadcastEvent.Command.MONITORS_UPDATE) {
			displayMonitors();
		} else if (be.getCommand() == BroadcastEvent.Command.MONITORS_ENABLED) {
			enableMonitors();
		} else if (be.getCommand() == BroadcastEvent.Command.MONITORS_DISABLED) {
			disableMonitors();
		}
	}
}
