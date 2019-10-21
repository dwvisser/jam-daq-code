package jam.data.control;

import com.google.inject.Inject;
import jam.data.Monitor;
import jam.global.AcquisitionStatus;
import jam.global.BroadcastEvent;
import jam.global.BroadcastEvent.Command;
import jam.global.Broadcaster;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.List;

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
	 * @param frame
	 *            application frame
	 * 
	 * @param status
	 *            acquisition status
	 * @param broadcaster
	 *            broadcasts state changes
	 */
	@Inject
	public MonitorDisplay(final Frame frame, final AcquisitionStatus status,
			final Broadcaster broadcaster) {
		super(frame, "Monitors Disabled", false, broadcaster);
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final Command command = ((BroadcastEvent) evt).getCommand();
		if (command == BroadcastEvent.Command.MONITORS_UPDATE) {
			displayMonitors();
		} else if (command == BroadcastEvent.Command.MONITORS_ENABLED) {
			enableMonitors();
		} else if (command == BroadcastEvent.Command.MONITORS_DISABLED) {
			disableMonitors();
		}
	}
}
