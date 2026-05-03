package jam.data.control;

import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.LEFT;
import static javax.swing.SwingConstants.RIGHT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jam.data.Monitor;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GoodThread;
import jam.sort.ThreadPriorities;
import jam.ui.Canceller;
import jam.ui.WindowCancelAction;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Reads and displays the monitors.
 *
 * @version September 2000
 * @author Ken Swartz
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @since JDK1.1
 */
@Singleton
public final class MonitorControl extends AbstractControl implements Runnable {

  private static final int THRESHOLD_INDEX = 1;
  private static final int MAX_VALUE_INDEX = 2;
  private static final int CHECKBOX_INDEX = 3;

  private final class BroadcastMonitorUpdate implements Runnable {
    public void run() {
      broadcaster.broadcast(BroadcastEvent.Command.MONITORS_UPDATE);
    }
  }

  private final transient BroadcastMonitorUpdate monitorUpdate = new BroadcastMonitorUpdate();

  private static final int BORDER_HEIGHT = 5;

  private transient boolean configured = false; // monitors have been
  // configured

  private transient int interval; // update interval

  private transient GoodThread loopThread; // loop to update monitors

  private final transient JPanel pMonitors;

  private final transient JSpinner spinnerUpdate;

  @Inject
  MonitorControl(final Frame frame, final Broadcaster broadcaster) {
    super(frame, "Monitors Setup", false, broadcaster);
    setResizable(true);
    setLocation(20, 50);
    final Container contents = getContentPane();
    contents.setLayout(new BorderLayout());
    /* Panel with column titles */
    final JPanel pUpper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
    Border border = new EmptyBorder(10, 0, 5, 5);
    pUpper.setBorder(border);
    contents.add(pUpper, BorderLayout.NORTH);
    /* Add labels of columns */
    pUpper.add(new JLabel("Name", CENTER));
    pUpper.add(new JLabel("Threshold", CENTER));
    pUpper.add(new JLabel("Maximum", CENTER));
    pUpper.add(new JLabel("Alarm", LEFT));
    /* Panel to fill in with monitors */
    pMonitors = new JPanel(new GridLayout(0, 1, 5, BORDER_HEIGHT));
    border = new EmptyBorder(BORDER_HEIGHT, 0, BORDER_HEIGHT, 0);
    pMonitors.setBorder(border);
    // Scroll Panel
    final JScrollPane scrollPane = new JScrollPane(pMonitors);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    contents.add(scrollPane, BorderLayout.CENTER);
    /* Lower panel has widgets that do not change */
    final JPanel pLower = new JPanel(new GridLayout(0, 1, 0, 0));
    contents.add(pLower, BorderLayout.SOUTH);
    /* panel for update time */
    final JPanel updatePanel = new JPanel();
    updatePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    pLower.add(updatePanel);
    updatePanel.add(new JLabel("Update every", RIGHT));
    final Integer one = 1;
    final Integer init = 1;
    spinnerUpdate = new JSpinner(new SpinnerNumberModel(init, one, null, one));
    spinnerUpdate.setPreferredSize(new Dimension(50, spinnerUpdate.getPreferredSize().height));
    updatePanel.add(spinnerUpdate);
    updatePanel.add(new JLabel("sec", LEFT));
    /* panel for buttons */
    final JPanel buttons = new JPanel(new GridLayout(1, 0, 5, 5));
    pLower.add(buttons);
    final JButton recall = new JButton("Recall");
    recall.addActionListener(event -> recall());
    buttons.add(recall);
    final JButton bok = new JButton("OK");
    bok.addActionListener(
        event -> {
          configure();
          startLoopThread();
          dispose();
        });
    buttons.add(bok);
    final JButton apply = new JButton("Apply");
    apply.addActionListener(
        event -> {
          configure();
          startLoopThread();
        });
    buttons.add(apply);
    final Canceller canceller =
        () -> {
          configured = false;
          /* stop monitor thread if running */
          if (loopThread != null) {
            loopThread.setState(GoodThread.State.STOP);
          }
        };
    final JButton cancel = new JButton(new WindowCancelAction(canceller));
    buttons.add(cancel);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    /* setup monitors */
    doSetup();
  }

  /** Configure the monitors, i.e. set the values their parameters according to the input fields. */
  protected void configure() {
    try {
      /* set update interval */
      interval = (Integer) spinnerUpdate.getValue();
      if (interval < 1) {
        throw new IllegalArgumentException("Update interval must be greater than 1");
      }

      Monitor.setInterval(interval);

      /* get the Monitor parameters */
      final List<Monitor> monitors = Monitor.getMonitorList();
      for (int i = 0; i < monitors.size(); i++) {
        final JPanel monitorPanel = (JPanel) pMonitors.getComponent(i);
        final JTextField textThreshold = (JTextField) monitorPanel.getComponent(THRESHOLD_INDEX);
        final JTextField textMaximum = (JTextField) monitorPanel.getComponent(MAX_VALUE_INDEX);
        final JCheckBox checkAlarm = (JCheckBox) monitorPanel.getComponent(CHECKBOX_INDEX);
        final double threshold = Double.parseDouble(textThreshold.getText().trim());
        final Monitor monitor = monitors.get(i);
        monitor.setThreshold(threshold);
        final double maximum = Double.parseDouble(textMaximum.getText().trim());
        monitor.setMaximum(maximum);
        monitor.setAlarm(checkAlarm.isSelected());
      }
    } catch (NumberFormatException nfe) {
      LOGGER.log(Level.SEVERE, "Invalid number input.", nfe);
    }

    configured = true;
  }

  /** Setup all monitors. */
  @Override
  public void doSetup() {
    int numberMonitors;
    final int cols = 6;
    JPanel pRow = null;
    numberMonitors = Monitor.getMonitorList().size();
    /* Clear panel */
    pMonitors.removeAll();
    /*
     * for each monitor make a panel with label, threshold, maximum, and
     * alarm
     */
    for (Monitor monitor : Monitor.getMonitorList()) {
      pRow = updateMonitor(cols, monitor);
    }
    pack();
    if (numberMonitors > 0) {
      final Dimension dialogDim =
          calculateScrollDialogSize(this, pRow, BORDER_HEIGHT, numberMonitors);
      setSize(dialogDim);
    }
  }

  /** Stop monitors interval updating loop */
  private void loopThreadStopped() {
    loopThread = null;
    for (Monitor monitor : Monitor.getMonitorList()) {
      monitor.reset();
    }
    broadcaster.broadcast(BroadcastEvent.Command.MONITORS_DISABLED);
  }

  /** Recall the monitor's parameters and set the input fields. */
  protected void recall() {
    /* update interval */
    spinnerUpdate.setValue(interval);
    /* get the Monitor parameters */
    final List<Monitor> monitors = Monitor.getMonitorList();
    for (int i = 0; i < monitors.size(); i++) {
      final JPanel monitorPanel = (JPanel) pMonitors.getComponent(i);
      final JTextField textThreshold = (JTextField) monitorPanel.getComponent(THRESHOLD_INDEX);
      final JTextField textMaximum = (JTextField) monitorPanel.getComponent(MAX_VALUE_INDEX);
      final JCheckBox checkAlarm = (JCheckBox) monitorPanel.getComponent(CHECKBOX_INDEX);
      final Monitor monitor = monitors.get(i);
      textThreshold.setText(String.valueOf(monitor.getThreshold()));
      textMaximum.setText(String.valueOf(monitor.getMaximum()));
      checkAlarm.setSelected(monitor.isAlarmActivated());
    }
  }

  /** method to run and update monitors */
  public void run() {
    try {
      while (loopThread.checkState()) { // loop until stopped
        updatePeriodically();
      } // loop until stopped
    } catch (InterruptedException ie) {
      LOGGER.log(Level.SEVERE, "Monitor Interrupted", ie);
    }
    loopThreadStopped();
  }

  /**
   * Start monitors interval updating loop.
   *
   * @throws IllegalStateException if the monitors aren't configured yet
   */
  private void startLoopThread() {
    if (configured) {
      if (loopThread == null) {
        loopThread = new GoodThread(this); // defaults to RUN state
        loopThread.setPriority(ThreadPriorities.MESSAGING);
        loopThread.setName("Monitor Thread");
        loopThread.setDaemon(true);
        loopThread.start();
      }
      broadcaster.broadcast(BroadcastEvent.Command.MONITORS_ENABLED);
    } else {
      throw new IllegalStateException(
          getClass().getName() + ".start(): " + "called before the monitors were configured.");
    }
  }

  private JPanel updateMonitor(final int index, final Monitor monitor) {
    JPanel pRow;
    pRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    pMonitors.add(pRow);
    final JLabel labelConfig = new JLabel(monitor.getName(), CENTER);
    final JPanel pLabel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    pLabel.add(labelConfig);
    pRow.add(pLabel);
    final JTextField textThreshold = new JTextField();
    textThreshold.setColumns(index);
    textThreshold.setEditable(true);
    final int defaultThreshold = 10;
    textThreshold.setText(Integer.toString(defaultThreshold));
    pRow.add(textThreshold);
    final JTextField textMaximum = new JTextField();
    textMaximum.setColumns(index);
    textMaximum.setEditable(true);
    final int defaultMax = 100;
    textMaximum.setText(Integer.toString(defaultMax));
    pRow.add(textMaximum);
    final JCheckBox checkAlarm = new JCheckBox();
    checkAlarm.setSelected(false);
    pRow.add(checkAlarm);
    return pRow;
  }

  private void updatePeriodically() throws InterruptedException {
    final int waitForResults = 500;
    final int waitAfterRepaint = interval * 1000 - waitForResults;

    /* read scalers and wait */
    broadcaster.broadcast(BroadcastEvent.Command.SCALERS_READ);
    Thread.sleep(waitForResults);

    // loop for each monitor
    for (Monitor monitor : Monitor.getMonitorList()) {
      monitor.update(); // update the monitor
    } // end loop monitors

    /* Broadcast event on UI thread */
    SwingUtilities.invokeLater(monitorUpdate);
    Thread.sleep(waitAfterRepaint);
  }
}
