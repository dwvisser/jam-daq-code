package jam.data.control;

import jam.data.Monitor;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

/**
 * 
 * Displays the monitors
 * 
 * @author Ken Swartz
 *
 */
public class MonitorDisplay extends DataControl implements Observer {

	private JToggleButton checkAudio;
	private JPanel pTitles;
	private JPanel pBars;

	public MonitorDisplay() {
		super("Monitors Disabled", false);
		
		//>> dialog box to display Monitors
		setResizable(true);
		setLocation(20, 50);
		Container cddisp = this.getContentPane();
		cddisp.setLayout(new BorderLayout());

		//Panel for the bars
		pBars = new JPanel(new GridLayout(0, 1, 5, 5));
		pBars.setBorder(new EmptyBorder(10, 0, 10, 0));
		cddisp.add(pBars, BorderLayout.CENTER);
		pTitles = new JPanel(new GridLayout(0, 1, 5, 5));
		pTitles.setBorder(new EmptyBorder(10, 0, 10, 0));
		cddisp.add(pTitles, BorderLayout.WEST);

		// alarm panel for display dialog
		final JPanel pal = new JPanel();
		cddisp.add(pal, BorderLayout.SOUTH);
		pal.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		checkAudio = new JCheckBox("Audio Alarm", true);
		pal.add(checkAudio);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	}
	/** 
	 * Setup the display of monitors, inherited for DataControl
	 */	
	public void setup(){
		Iterator monitors = Monitor.getMonitorList().iterator();
		pTitles.removeAll();
		pBars.removeAll();
		while (monitors.hasNext()) {
			Monitor monitor = (Monitor) monitors.next();
			final JPanel pm = new JPanel();
			pm.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
			pTitles.add(pm);
			final JLabel labelDisp =
				new JLabel(monitor.getName(), JLabel.RIGHT);
			pm.add(labelDisp);
			final PlotBar plotBar = new PlotBar(monitor);
			pBars.add(plotBar);
		}
		pack();
	}
	
	private void displayMonitors() {
		//loop for each monitor check if we should sound alarm
		for (Iterator it = Monitor.getMonitorList().iterator();
			it.hasNext();
			) {
			final Monitor monitor = (Monitor) it.next();		
			//If the audio on and are we taking data
			if (checkAudio.isSelected()
				&& JamStatus.instance().isAcqOn()
				&& monitor.getAlarm()
				&& (!monitor.isAcceptable())) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
		//display monitors
		pBars.repaint();

	}
	
	private void enableMonitors() {
		setTitle("Monitors Enabled");
		pBars.repaint();		
	}
	private void disableMonitors() {
		setTitle("Monitors Disabled");
		pBars.repaint();		
	}
	
	/** Implementation of Observable interface.
	 * @param observable not sure
	 * @param o not sure
	 */
	public void update(Observable observable, Object o) {
		BroadcastEvent be = (BroadcastEvent) o;
		if (be.getCommand() == BroadcastEvent.MONITORS_UPDATE) {
			displayMonitors();
		} else if (be.getCommand() == BroadcastEvent.MONITORS_ENABLED)  {
			enableMonitors();
		}else if (be.getCommand() == BroadcastEvent.MONITORS_DISABLED)  {
			disableMonitors();
		}
	}

}
