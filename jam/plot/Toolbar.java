/*
 * Created on Jul 14, 2004
 */
package jam.plot;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;


/**
 * The toolbar that goes with the display.
 * 
 * @author Dale Visser
 */
public class Toolbar extends JToolBar implements ActionListener {

	private Action action;
	
	private JButton bnetarea, brebin, bgoto;
	
	private static final int orientation;
	private static final String location, key;
	private static final Preferences helpnode;
	static {
		final String defaultVal = BorderLayout.NORTH;
		key = "toolbarLocation";
		helpnode =
			Preferences.userNodeForPackage(Toolbar.class);
		location = helpnode.get(key, defaultVal);
		orientation =
			(BorderLayout.NORTH.equals(location)
				|| BorderLayout.SOUTH.equals(location))
				? JToolBar.HORIZONTAL
				: JToolBar.VERTICAL;
	}
	
	/**
	 * Adds the tool bar the left hand side of the plot.
	 *
	 * @since Version 0.5
	 */
	Toolbar(Container container, Action action) {
		super("Actions", orientation);
		this.action=action;
		final Icon iUpdate = loadToolbarIcon("jam/plot/Update.png");
		final Icon iLinLog = loadToolbarIcon("jam/plot/LinLog.png");
		final Icon iAutoScale = loadToolbarIcon("jam/plot/AutoScale.png");
		final Icon iRange = loadToolbarIcon("jam/plot/Range.png");
		final Icon iRebin = loadToolbarIcon("jam/plot/Rebin.png");
		final Icon iExpand = loadToolbarIcon("jam/plot/ZoomRegion.png");
		final Icon iFullScale = loadToolbarIcon("jam/plot/FullScale.png");
		final Icon iZoomIn = loadToolbarIcon("jam/plot/ZoomIn.png");
		final Icon iZoomOut = loadToolbarIcon("jam/plot/ZoomOut.png");
		final Icon iGoto = loadToolbarIcon("jam/plot/Goto.png");
		final Icon iArea = loadToolbarIcon("jam/plot/Area.png");
		final Icon iNetArea = loadToolbarIcon("jam/plot/NetArea.png");
		final Icon iCancel = loadToolbarIcon("jam/plot/Cancel.png");
		setToolTipText(
			"Underlined letters are shortcuts for the console.");
		container.add(this, location);
		try {
			setRollover(true);
			final JButton bupdate = iUpdate == null ? 
					new JButton(getHTML("<u>U</u>pdate")) : new JButton(iUpdate);
			bupdate.setToolTipText(
				getHTML("<u>U</u>pdate display with most current data."));
			bupdate.setActionCommand(Action.UPDATE);
			bupdate.addActionListener(this);
			add(bupdate);
			final JButton blinear = iLinLog==null ?
					new JButton(getHTML("<u>Li</u>near/<u>Lo</u>g")) : new JButton(iLinLog);
			blinear.setToolTipText(
				getHTML("<u>Li</u>near/<u>Lo</u>g scale toggle."));
			blinear.setActionCommand(Action.SCALE);
			blinear.addActionListener(this);
			add(blinear);
			final JButton bauto = iAutoScale == null ?
					new JButton(getHTML("<u>A</u>utoscale")) : new JButton(iAutoScale);
			bauto.setToolTipText(
				getHTML("<u>A</u>utomatically set the counts scale."));
			bauto.setActionCommand(Action.AUTO);
			bauto.addActionListener(this);
			add(bauto);
			final JButton brange = iRange == null ? 
					new JButton(getHTML("<u>Ra</u>nge")) : new JButton(iRange);
			brange.setToolTipText(getHTML("<u>Ra</u>nge set counts scale."));
			brange.setActionCommand(Action.RANGE);
			brange.addActionListener(this);
			add(brange);
			brebin = iRebin == null ? 
					new JButton(getHTML("<u>Re</u>bin")) : new JButton(iRebin);
			brebin.setToolTipText(
				getHTML("<u>Re</u>bin, enter a bin width in the console."));
			brebin.setActionCommand(Action.REBIN);
			brebin.addActionListener(this);
			add(brebin);
			addSeparator();
			final JButton bexpand = iExpand==null ? 
					new JButton(getHTML("<u>E</u>xpand")) : new JButton(iExpand);
			bexpand.setToolTipText(getHTML("<u>E</u>xpand plot region."));
			bexpand.setActionCommand(Action.EXPAND);
			bexpand.addActionListener(this);
			add(bexpand);
			final JButton bfull = iFullScale==null ? 
				new JButton(getHTML("<u>F</u>ull")) : new JButton(iFullScale);
			bfull.setActionCommand(Action.FULL);
			bfull.setToolTipText(getHTML("<u>F</u>ull plot view."));
			bfull.addActionListener(this);
			add(bfull);
			final JButton bzoomin = iZoomIn==null ? 
					new JButton(getHTML("<u>Z</u>oom<u>i</u>n")) : new JButton(iZoomIn);
			bzoomin.setToolTipText(getHTML("<u>Z</u>oom<u>i</u>n plot."));
			bzoomin.setActionCommand(Action.ZOOMIN);
			bzoomin.addActionListener(this);
			add(bzoomin);
			final JButton bzoomout = iZoomOut == null ? 
					new JButton(getHTML("<u>Z</u>oom<u>o</u>ut")) : new JButton(iZoomOut);
			bzoomout.setToolTipText(getHTML("<u>Z</u>oom<u>o</u>ut plot."));
			bzoomout.setActionCommand(Action.ZOOMOUT);
			bzoomout.addActionListener(this);
			add(bzoomout);
			bgoto = iGoto==null ? 
					new JButton(getHTML("<u>G</u>oto")) : new JButton(iGoto);
			bgoto.setActionCommand(Action.GOTO);
			bgoto.setToolTipText(getHTML("<u>G</u>oto selected."));
			bgoto.addActionListener(this);
			add(bgoto);
			addSeparator();
			final JButton barea = iArea==null ? 
					new JButton(getHTML("<u>Ar</u>ea")) : new JButton(iArea);
			barea.setToolTipText(getHTML("<u>Ar</u>ea display."));
			barea.setActionCommand(Action.AREA);
			barea.addActionListener(this);
			add(barea);
			bnetarea = iNetArea==null ?
					new JButton(getHTML("<u>N</u>et Area")) : new JButton(iNetArea);
			bnetarea.setToolTipText(getHTML("<u>N</u>et Area display."));
			bnetarea.setActionCommand(Action.NETAREA);
			bnetarea.addActionListener(this);
			add(bnetarea);
			addSeparator();
			final JButton bcancel = iCancel==null ?
					new JButton(getHTML("<u>C</u>ancel")) : new JButton(iCancel);
			bcancel.setActionCommand(Action.CANCEL);
			bcancel.setToolTipText(getHTML("<u>C</u>ancel plot action."));
			bcancel.addActionListener(this);
			add(bcancel);
			/* Listen for changes in orientation */
			addPropertyChangeListener(
					"orientation",
					new java.beans.PropertyChangeListener() {
				public void propertyChange(
					java.beans.PropertyChangeEvent evt) {
					/* Get the new orientation */
					Integer newValue = (Integer) evt.getNewValue();
					/* place an appropriate value in the user prefs */
					helpnode.put(
						key,
						(newValue.intValue() == JToolBar.HORIZONTAL)
							? BorderLayout.NORTH
							: BorderLayout.WEST);
					fitToolbar();
				}
			});
			fitToolbar();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fitToolbar() {
		final boolean vertical = getOrientation() == JToolBar.VERTICAL;
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

	/**
	 * Load icons for tool bar
	 */
	private Icon loadToolbarIcon(String path) {
		final Icon toolbarIcon;
		final ClassLoader cl = this.getClass().getClassLoader();
		final URL urlResource = cl.getResource(path);
		if (!(urlResource == null)) {
			toolbarIcon = new ImageIcon(urlResource);
		} else { //instead use path, ugly but lets us see button
			JOptionPane.showMessageDialog(
				this,
				"Can't load resource: " + path,
				"Missing Icon",
				JOptionPane.ERROR_MESSAGE);
			toolbarIcon = null; //buttons initialized with text if icon==null
		}
		return toolbarIcon;
	}

	private String getHTML(String body) {
		final StringBuffer rval =
			new StringBuffer("<html><body>").append(body).append(
				"</html></body>");
		return rval.toString();
	}

	void setHistogramDimension(int dimension){
		final boolean enable = dimension==1;
		bgoto.setEnabled(enable);
		brebin.setEnabled(enable);
		bnetarea.setEnabled(enable);
	}
	/**
	 * Routine called by pressing a button on the action toolbar.
	 * 
	 * @param e
	 *            the event created by the button press
	 */
	public void actionPerformed(ActionEvent e) {
		final String command = e.getActionCommand();
		action.doCommand(command,false);
	}

}
