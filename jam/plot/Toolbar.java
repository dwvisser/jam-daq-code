/*
 * Created on Jul 14, 2004
 */
package jam.plot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

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
 * The toolbar that goes with the display.
 * 
 * @author Dale Visser
 */
final class Toolbar extends JToolBar implements ActionListener {

	/**
	 * Class to render rebin selections
	 */
	class ReBinComboBoxRenderer extends JLabel implements ListCellRenderer {

		ReBinComboBoxRenderer() {
			super();
			setOpaque(true);
			setHorizontalAlignment(LEFT);
		}

		/*
		 * This method finds the image and text corresponding to the selected
		 * value and returns the label, set up to display the text and image.
		 */
		public Component getListCellRendererComponent(final JList list,
				final Object value, final int index, final boolean isSelected,
				final boolean cellHasFocus) {
			// Get the selected index. (The index param isn't
			// always valid, so just use the value.)
			final int selectedIndex = ((Integer) value).intValue();
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

	private static final String LOCATION, KEY;

	private static final Logger LOGGER = Logger.getLogger(Toolbar.class
			.getPackage().getName());

	private static final int ORIENTATION;

	private static final Preferences PREFS;

	private static final String[] REBIN_RATIOS = { "1", "2", "4", "8", "16" };

	static {
		final String defaultVal = BorderLayout.NORTH;
		KEY = "toolbarLocation";
		PREFS = Preferences.userNodeForPackage(Toolbar.class);
		LOCATION = PREFS.get(KEY, defaultVal);
		ORIENTATION = (BorderLayout.NORTH.equals(LOCATION) || BorderLayout.SOUTH
				.equals(LOCATION)) ? SwingConstants.HORIZONTAL
				: SwingConstants.VERTICAL;
	}

	private transient final Action action;

	private transient JButton bnetarea, bgoto;

	private transient JComboBox comboBinRatio;

	private final transient Icon iRebin;

	/** Is a syncronize event, so don't fire events */
	private transient boolean isSyncEvent;

	/*
	 * non-javadoc: Adds the tool bar the left hand side of the plot.
	 * 
	 * @since Version 0.5
	 */
	Toolbar(final Container container, final Action action) {
		super("Actions", ORIENTATION);
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
		container.add(this, LOCATION);
		setRollover(false);
		try {
			final JButton bupdate = getButton(iUpdate, "<u>U</u>pdate");
			bupdate
					.setToolTipText(getHTML("<u>U</u>pdate display with most current data."));
			bupdate.setActionCommand(PlotCommands.UPDATE);
			bupdate.addActionListener(this);
			add(bupdate);
			final JButton blinear = getButton(iLinLog,
					"<u>Li</u>near/<u>Lo</u>g");
			blinear
					.setToolTipText(getHTML("<u>Li</u>near/<u>Lo</u>g scale toggle."));
			blinear.setActionCommand(PlotCommands.SCALE);
			blinear.addActionListener(this);
			add(blinear);
			final JButton bauto = getButton(iAutoScale, "<u>A</u>utoscale");
			bauto
					.setToolTipText(getHTML("<u>A</u>utomatically set the counts scale."));
			bauto.setActionCommand(PlotCommands.AUTO);
			bauto.addActionListener(this);
			add(bauto);
			final JButton brange = getButton(iRange, "<u>Ra</u>nge");
			brange.setToolTipText(getHTML("<u>Ra</u>nge set counts scale."));
			brange.setActionCommand(PlotCommands.RANGE);
			brange.addActionListener(this);
			add(brange);
			// Combox for Re Bin
			Vector<Integer> intVector = new Vector<Integer>(REBIN_RATIOS.length);// NOPMD
			for (int i = 0; i < REBIN_RATIOS.length; i++) {
				intVector.add(i, i);
			}
			comboBinRatio = new JComboBox(intVector);
			final Dimension dimMax = comboBinRatio.getMaximumSize();
			final Dimension dimPref = comboBinRatio.getPreferredSize();
			dimMax.width = dimPref.width + 40;
			comboBinRatio.setMaximumSize(dimMax);
			comboBinRatio.setRenderer(new ReBinComboBoxRenderer());
			comboBinRatio
					.setToolTipText(getHTML("<u>Re</u>bin, enter a bin width in the console."));
			comboBinRatio.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent ae) {
					final Object item = ((JComboBox) ae.getSource())
							.getSelectedItem();
					selectionReBin((Integer) item);
				}
			});
			add(comboBinRatio);
			addSeparator();
			final JButton bfull = getButton(iFullScale, "<u>F</u>ull");
			bfull.setActionCommand(PlotCommands.FULL);
			bfull.setToolTipText(getHTML("<u>F</u>ull plot view."));
			bfull.addActionListener(this);
			add(bfull);
			final JButton bexpand = getButton(iExpand, "<u>E</u>xpand");
			bexpand.setToolTipText(getHTML("<u>E</u>xpand plot region."));
			bexpand.setActionCommand(PlotCommands.EXPAND);
			bexpand.addActionListener(this);
			add(bexpand);
			final JButton bzoomin = getButton(iZoomIn, "<u>Z</u>oom<u>i</u>n");
			bzoomin.setToolTipText(getHTML("<u>Z</u>oom<u>i</u>n plot."));
			bzoomin.setActionCommand(PlotCommands.ZOOMIN);
			bzoomin.addActionListener(this);
			add(bzoomin);
			final JButton bzoomout = getButton(iZoomOut,
					"<u>Z</u>oom<u>o</u>ut");
			bzoomout.setToolTipText(getHTML("<u>Z</u>oom<u>o</u>ut plot."));
			bzoomout.setActionCommand(PlotCommands.ZOOMOUT);
			bzoomout.addActionListener(this);
			add(bzoomout);
			/*
			 * TODO KBS still to add final JButton bzoomvert = iZoomVert == null
			 * ? new JButton(getHTML("<u>Z</u>oom<u>H</u>orizontal")) : new
			 * JButton(iZoomVert);
			 * bzoomvert.setToolTipText(getHTML("<u>Z</u>oom<u>H</u>orizontal
			 * plot.")); bzoomvert.setActionCommand(Action.ZOOMVERT);
			 * bzoomvert.addActionListener(this); add(bzoomvert);
			 * 
			 * final JButton bzoomhorz = iZoomHorz == null ? new
			 * JButton(getHTML("<u>Z</u>oom<u>v</u>ertical")) : new
			 * JButton(iZoomHorz);
			 * bzoomhorz.setToolTipText(getHTML("<u>Z</u>oom<u>v</u>ertical
			 * plot.")); bzoomhorz.setActionCommand(Action.ZOOMHORZ);
			 * bzoomhorz.addActionListener(this); add(bzoomhorz);
			 */
			bgoto = getButton(iGoto, "<u>G</u>oto");
			bgoto.setActionCommand(PlotCommands.GOTO);
			bgoto.setToolTipText(getHTML("<u>G</u>oto selected."));
			bgoto.addActionListener(this);
			add(bgoto);
			addSeparator();
			final JButton barea = getButton(iArea, "<u>Ar</u>ea");
			barea.setToolTipText(getHTML("<u>Ar</u>ea display."));
			barea.setActionCommand(PlotCommands.AREA);
			barea.addActionListener(this);
			add(barea);
			bnetarea = getButton(iNetArea, "<u>N</u>et Area");
			bnetarea.setToolTipText(getHTML("<u>N</u>et Area display."));
			bnetarea.setActionCommand(PlotCommands.NETAREA);
			bnetarea.addActionListener(this);
			add(bnetarea);
			addSeparator();
			final JButton bcancel = getButton(iCancel, "<u>C</u>ancel");
			bcancel.setActionCommand(PlotCommands.CANCEL);
			bcancel.setToolTipText(getHTML("<u>C</u>ancel plot action."));
			bcancel.addActionListener(this);
			add(bcancel);
			/* Listen for changes in orientation */
			addPropertyChangeListener("orientation",
					new java.beans.PropertyChangeListener() {
						public void propertyChange(
								final java.beans.PropertyChangeEvent evt) {
							/* Get the new orientation */
							final Integer newValue = (Integer) evt
									.getNewValue();
							/* place an appropriate value in the user prefs */
							PREFS
									.put(
											KEY,
											(newValue.intValue() == SwingConstants.HORIZONTAL) ? BorderLayout.NORTH
													: BorderLayout.WEST);
							fitToolbar();
						}
					});
			fitToolbar();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * Routine called by pressing a button on the action toolbar.
	 * 
	 * @param actionEvent
	 *            the event created by the button press
	 */
	public void actionPerformed(final ActionEvent actionEvent) {
		final String command = actionEvent.getActionCommand();
		action.doCommand(command, false);
	}

	private void fitToolbar() {
		final boolean vertical = getOrientation() == SwingConstants.VERTICAL;
		if (vertical) {
			final int height = getPreferredSize().height;
			final Dimension oldmin = getMinimumSize();
			if (height > oldmin.height) {
				final Dimension newMin = new Dimension(oldmin.width, height);
				setMinimumSize(newMin);
			}
		} else {
			final int width = getPreferredSize().width;
			final Dimension oldmin = getMinimumSize();
			if (width > oldmin.width) {
				final Dimension newMin = new Dimension(width, oldmin.height);
				setMinimumSize(newMin);
			}
		}
	}

	private JButton getButton(final Icon icon, final String altText) {
		final JButton rval;
		if (icon == null) {
			rval = new JButton(getHTML(altText));
		} else {
			rval = new JButton(icon);
		}
		return rval;

	}

	private String getHTML(final String body) {
		final StringBuffer rval = new StringBuffer("<html><body>").append(body)
				.append("</html></body>");
		return rval.toString();
	}

	/*
	 * non-javadoc: Load icons for tool bar
	 */
	private Icon loadToolbarIcon(final String path) {
		Icon rval = null;
		final ClassLoader classLoader = this.getClass().getClassLoader();
		final URL urlResource = classLoader.getResource(path);
		if (urlResource == null) {
			JOptionPane.showMessageDialog(this, "Can't load resource: " + path,
					"Missing Icon", JOptionPane.ERROR_MESSAGE);
		} else {
			rval = new ImageIcon(urlResource);
		}
		return rval;
	}

	/**
	 * Called by a combo box rebin selection
	 * 
	 * @param index
	 *            rebin width index from combo box list
	 */
	public void selectionReBin(final int index) {
		final String ratio = REBIN_RATIOS[index];
		final List<Double> parameters = Collections.singletonList(Double
				.parseDouble(ratio));
		if (!isSyncEvent) {
			action.doCommand(PlotCommands.REBIN, parameters, false);
		}
	}

	protected void setHistogramProperties(final int dimension,
			final double binWidth) {
		final boolean enable1D = dimension == 1;
		bgoto.setEnabled(enable1D);
		bnetarea.setEnabled(enable1D);
		isSyncEvent = true;
		/* Convert double to int string */
		final String strBinWidth = Integer.valueOf((int) binWidth).toString();
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
