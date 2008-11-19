package jam.data.control;

import jam.data.Monitor;
import jam.global.AcquisitionStatus;
import jam.global.BroadcastEvent;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.List;
import java.util.Observable;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

/**
 * 
 * Displays the monitors
 * 
 * @author Ken Swartz
 * 
 */
public class MonitorDisplay extends AbstractControl {

	private final static int BORDER_HEIGHT = 5;

	private transient final JToggleButton checkAudio;

	private transient final JPanel pBars;

	private transient final AcquisitionStatus status;

	/**
	 * Constructs a new monitor display dialog.
	 * 
	 * @param status
	 *            acquisition status
	 */
	@Inject
	public MonitorDisplay(final AcquisitionStatus status) {
		super("Monitors Disabled", false);
		this.status = status;
		setResizable(true);
		setLocation(20, 50);
		final Container cddisp = this.getContentPane();
		cddisp.setLayout(new BorderLayout());
		// Panel for the bars
		pBars = new JPanel(new GridLayout(0, 1, BORDER_HEIGHT, 5));
		pBars.setBorder(new EmptyBorder(BORDER_HEIGHT, 0, BORDER_HEIGHT, 0));
		// Scroll Panel
		final JScrollPane scrollPane = new JScrollPane(pBars);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		cddisp.add(scrollPane, BorderLayout.CENTER);
		// Panel for alarm
		final JPanel pal = new JPanel();
		cddisp.add(pal, BorderLayout.SOUTH);
		pal.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		checkAudio = new JCheckBox("Audio Alarm", true);
		pal.add(checkAudio);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * @param monitor
	 * @return
	 */
	private JPanel createPanel(final Monitor monitor) {
		JPanel pMonitors;
		pMonitors = new JPanel();
		pMonitors.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pBars.add(pMonitors);
		final JLabel labelDisp = new JLabel(monitor.getName(),
				SwingConstants.RIGHT);
		pMonitors.add(labelDisp);
		final PlotBar plotBar = new PlotBar(monitor);
		pMonitors.add(plotBar);
		return pMonitors;
	}

	private void disableMonitors() {
		setTitle("Monitors Disabled");
		pBars.repaint();
	}

	private void displayMonitors() {
		// loop for each monitor check if we should sound alarm
		for (Monitor monitor : Monitor.getMonitorList()) {
			// If the audio on and are we taking data
			if (checkAudio.isSelected() && this.status.isAcqOn()
					&& monitor.isAlarmActivated() && (!monitor.isAcceptable())) {
				Toolkit.getDefaultToolkit().beep();
				break;
			}
		}
		// display monitors
		pBars.repaint();
	}

	/**
	 * Setup the display of monitors, inherited for AbstractControl
	 */
	@Override
	public void doSetup() {
		JPanel monitorPanel = null;
		final List<Monitor> mlist = Monitor.getMonitorList();
		final int numberMonitors = mlist.size();
		pBars.removeAll();
		for (Monitor monitor : mlist) {
			monitorPanel = createPanel(monitor);
		}
		pack();
		if (numberMonitors > 0) {
			final Dimension dialogDim = calculateScrollDialogSize(this,
					monitorPanel, BORDER_HEIGHT, numberMonitors);
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
	 * @param object
	 *            not sure
	 */
	@Override
	public void update(final Observable observable, final Object object) {
		final BroadcastEvent event = (BroadcastEvent) object;
		if (event.getCommand() == BroadcastEvent.Command.MONITORS_UPDATE) {
			displayMonitors();
		} else if (event.getCommand() == BroadcastEvent.Command.MONITORS_ENABLED) {
			enableMonitors();
		} else if (event.getCommand() == BroadcastEvent.Command.MONITORS_DISABLED) {
			disableMonitors();
		}
	}
}
