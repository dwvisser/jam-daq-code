/*
 * Created on Jul 14, 2004
 */
package jam.plot;

import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 * The tool bar that goes with the display.
 *
 * @author Dale Visser
 */
final class Toolbar extends JToolBar implements ActionListener {

  /** Class to render re-bin selections */
  class ReBinComboBoxRenderer extends JLabel implements ListCellRenderer<Integer> {

    ReBinComboBoxRenderer() {
      super();
      setOpaque(true);
      setHorizontalAlignment(LEFT);
    }

    /*
     * This method finds the image and text corresponding to the selected
     * value and returns the label, set up to display the text and image.
     */
    public Component getListCellRendererComponent(
        final JList<? extends Integer> list,
        final Integer value,
        final int index,
        final boolean isSelected,
        final boolean cellHasFocus) {
      // Get the selected index. (The index param isn't
      // always valid, so just use the value.)
      final int selectedIndex = value;
      // Set foreground and background
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      // Set font
      setFont(list.getFont());

      // Set the icon and text.
      setIcon(iRebin);
      setText(REBIN_RATIOS[selectedIndex]);

      return this;
    }
  }

  private static final Logger LOGGER = Logger.getLogger(Toolbar.class.getPackage().getName());

  public static final String LOCATION_KEY = "toolbarLocation";

  private static final String[] REBIN_RATIOS = {"1", "2", "4", "8", "16"};

  private final transient Action action;

  private transient JButton bNetArea;
  private transient JButton bGoto;

  private transient JComboBox<Integer> comboBinRatio;

  private final transient Icon iRebin;

  /** Is a synchronize event, so don't fire events */
  private transient boolean isSyncEvent;

  /*
   * non-javadoc: Adds the tool bar the left hand side of the plot.
   *
   * @since Version 0.5
   */
  @Inject
  Toolbar(final Action action) {
    super("Actions");
    this.action = action;
    isSyncEvent = false;
    final Icon iUpdate = loadToolbarIcon("jam/plot/Update.png");
    final Icon iLinLog = loadToolbarIcon("jam/plot/LinLog.png");
    final Icon iAutoScale = loadToolbarIcon("jam/plot/AutoScale.png");
    final Icon iRange = loadToolbarIcon("jam/plot/Range.png");
    iRebin = loadToolbarIcon("jam/plot/Rebin.png");
    final Icon iExpand = loadToolbarIcon("jam/plot/ZoomRegion.png");
    final Icon iFullScale = loadToolbarIcon("jam/plot/FullScale.png");
    final Icon iZoomIn = loadToolbarIcon("jam/plot/ZoomIn.png");
    final Icon iZoomOut = loadToolbarIcon("jam/plot/ZoomOut.png");
    final Icon iGoto = loadToolbarIcon("jam/plot/Goto.png");
    final Icon iArea = loadToolbarIcon("jam/plot/Area.png");
    final Icon iNetArea = loadToolbarIcon("jam/plot/NetArea.png");
    final Icon iCancel = loadToolbarIcon("jam/plot/Cancel.png");
    setToolTipText("Underlined letters are shortcuts for the console.");
    setRollover(false);
    try {
      final JButton update = getButton(iUpdate, "<u>U</u>pdate");
      update.setToolTipText(getHTML("<u>U</u>pdate display with most current data."));
      update.setActionCommand(PlotCommands.UPDATE);
      update.addActionListener(this);
      add(update);
      final JButton toggleLinearLog = getButton(iLinLog, "<u>Li</u>near/<u>Lo</u>g");
      toggleLinearLog.setToolTipText(getHTML("<u>Li</u>near/<u>Lo</u>g scale toggle."));
      toggleLinearLog.setActionCommand(PlotCommands.SCALE);
      toggleLinearLog.addActionListener(this);
      add(toggleLinearLog);
      final JButton autoscale = getButton(iAutoScale, "<u>A</u>utoscale");
      autoscale.setToolTipText(getHTML("<u>A</u>utomatically set the counts scale."));
      autoscale.setActionCommand(PlotCommands.AUTO);
      autoscale.addActionListener(this);
      add(autoscale);
      final JButton range = getButton(iRange, "<u>Ra</u>nge");
      range.setToolTipText(getHTML("<u>Ra</u>nge set counts scale."));
      range.setActionCommand(PlotCommands.RANGE);
      range.addActionListener(this);
      add(range);
      // ComboBox for Re Bin
      Vector<Integer> intVector = new Vector<>(REBIN_RATIOS.length); // NOPMD
      for (int i = 0; i < REBIN_RATIOS.length; i++) {
        intVector.add(i, i);
      }
      comboBinRatio = new JComboBox<>(intVector);
      final Dimension dimMax = comboBinRatio.getMaximumSize();
      final Dimension dimPref = comboBinRatio.getPreferredSize();
      dimMax.width = dimPref.width + 40;
      comboBinRatio.setMaximumSize(dimMax);
      comboBinRatio.setRenderer(new ReBinComboBoxRenderer());
      comboBinRatio.setToolTipText(getHTML("<u>Re</u>bin, enter a bin width in the console."));
      comboBinRatio.addActionListener(
          ae -> {
            @SuppressWarnings("unchecked")
            final Integer item = (Integer) ((JComboBox<Integer>) ae.getSource()).getSelectedItem();
            selectionReBin(item);
          });
      add(comboBinRatio);
      addSeparator();
      final JButton fullScale = getButton(iFullScale, "<u>F</u>ull");
      fullScale.setActionCommand(PlotCommands.FULL);
      fullScale.setToolTipText(getHTML("<u>F</u>ull plot view."));
      fullScale.addActionListener(this);
      add(fullScale);
      final JButton expand = getButton(iExpand, "<u>E</u>xpand");
      expand.setToolTipText(getHTML("<u>E</u>xpand plot region."));
      expand.setActionCommand(PlotCommands.EXPAND);
      expand.addActionListener(this);
      add(expand);
      final JButton zoomIn = getButton(iZoomIn, "<u>Z</u>oom<u>i</u>n");
      zoomIn.setToolTipText(getHTML("<u>Z</u>oom<u>i</u>n plot."));
      zoomIn.setActionCommand(PlotCommands.ZOOMIN);
      zoomIn.addActionListener(this);
      add(zoomIn);
      final JButton zoomOut = getButton(iZoomOut, "<u>Z</u>oom<u>o</u>ut");
      zoomOut.setToolTipText(getHTML("<u>Z</u>oom<u>o</u>ut plot."));
      zoomOut.setActionCommand(PlotCommands.ZOOMOUT);
      zoomOut.addActionListener(this);
      add(zoomOut);
      bGoto = getButton(iGoto, "<u>G</u>oto");
      bGoto.setActionCommand(PlotCommands.GOTO);
      bGoto.setToolTipText(getHTML("<u>G</u>oto selected."));
      bGoto.addActionListener(this);
      add(bGoto);
      addSeparator();
      final JButton area = getButton(iArea, "<u>Ar</u>ea");
      area.setToolTipText(getHTML("<u>Ar</u>ea display."));
      area.setActionCommand(PlotCommands.AREA);
      area.addActionListener(this);
      add(area);
      bNetArea = getButton(iNetArea, "<u>N</u>et Area");
      bNetArea.setToolTipText(getHTML("<u>N</u>et Area display."));
      bNetArea.setActionCommand(PlotCommands.NETAREA);
      bNetArea.addActionListener(this);
      add(bNetArea);
      addSeparator();
      final JButton cancel = getButton(iCancel, "<u>C</u>ancel");
      cancel.setActionCommand(PlotCommands.CANCEL);
      cancel.setToolTipText(getHTML("<u>C</u>ancel plot action."));
      cancel.addActionListener(this);
      add(cancel);
      /* Listen for changes in orientation */
      addPropertyChangeListener(
          "orientation",
          evt -> {
            /* Get the new orientation */
            final Integer newValue = (Integer) evt.getNewValue();
            /* place an appropriate value in the user prefs */
            PlotPreferences.PREFS.put(
                LOCATION_KEY,
                (newValue == SwingConstants.HORIZONTAL) ? BorderLayout.NORTH : BorderLayout.WEST);
            fitToolbar();
          });
      fitToolbar();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Routine called by pressing a button on the action toolbar.
   *
   * @param actionEvent the event created by the button press
   */
  public void actionPerformed(final ActionEvent actionEvent) {
    final String command = actionEvent.getActionCommand();
    action.commandable.doCommand(command, false);
  }

  private void fitToolbar() {
    final boolean vertical = getOrientation() == SwingConstants.VERTICAL;
    if (vertical) {
      final int height = getPreferredSize().height;
      final Dimension oldMinimumSize = getMinimumSize();
      if (height > oldMinimumSize.height) {
        final Dimension newMin = new Dimension(oldMinimumSize.width, height);
        setMinimumSize(newMin);
      }
    } else {
      final int width = getPreferredSize().width;
      final Dimension oldMinimumSize = getMinimumSize();
      if (width > oldMinimumSize.width) {
        final Dimension newMin = new Dimension(width, oldMinimumSize.height);
        setMinimumSize(newMin);
      }
    }
  }

  private JButton getButton(final Icon icon, final String altText) {
    return icon == null ? new JButton(getHTML(altText)) : new JButton(icon);
  }

  private String getHTML(final String body) {
    return "<html><body>" + body + "</html></body>";
  }

  /*
   * non-javadoc: Load icons for tool bar
   */
  private Icon loadToolbarIcon(final String path) {
    Icon rval = null;
    final ClassLoader classLoader = this.getClass().getClassLoader();
    final URL urlResource = classLoader.getResource(path);
    if (urlResource == null) {
      JOptionPane.showMessageDialog(
          this, "Can't load resource: " + path, "Missing Icon", JOptionPane.ERROR_MESSAGE);
    } else {
      rval = new ImageIcon(urlResource);
    }
    return rval;
  }

  /**
   * Called by a combo box rebin selection
   *
   * @param index rebin width index from combo box list
   */
  public void selectionReBin(final int index) {
    final String ratio = REBIN_RATIOS[index];
    final List<Double> parameters = Collections.singletonList(Double.parseDouble(ratio));
    if (!isSyncEvent) {
      action.commandable.doCommand(PlotCommands.REBIN, parameters, false);
    }
  }

  protected void setHistogramProperties(final int dimension, final double binWidth) {
    final boolean enable1D = dimension == 1;
    bGoto.setEnabled(enable1D);
    bNetArea.setEnabled(enable1D);
    isSyncEvent = true;
    /* Convert double to int string */
    final String strBinWidth = String.valueOf((int) binWidth);
    comboBinRatio.setSelectedIndex(0);
    for (int i = 0; i < REBIN_RATIOS.length; i++) {
      if (strBinWidth.equals(REBIN_RATIOS[i])) {
        comboBinRatio.setSelectedIndex(i);
      }
    }
    comboBinRatio.setEnabled(enable1D);
    isSyncEvent = false;
  }
}
