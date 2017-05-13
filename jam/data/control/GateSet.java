package jam.data.control;

import jam.data.AbstractHist1D;
import jam.data.AbstractHist2D;
import jam.data.Gate;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.Nameable;
import jam.global.UnNamed;
import jam.plot.Bin;
import jam.ui.Canceller;
import jam.ui.SelectionTree;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

/**
 * Class to set 1 D and 2 D gates.
 * 
 * @version 0.5 April 1998
 * @author Ken Swartz
 */
public final class GateSet extends AbstractControl {

	private transient final JButton addP, removeP, unset, save, cancel;

	private transient final JComboBox<Object> cgate;

	private transient Gate currentGate;

	private transient Nameable currentHistogram = UnNamed
			.getSingletonInstance();

	private transient final List<Bin> gatePoints = new ArrayList<Bin>();

	private transient final JLabel lLower, lUpper;

	private transient boolean newGate = false; // a gate has been chosen

	private transient final JTextField textLower, textUpper;

	/**
	 * Creates an instance of the GateControl class.
	 * 
	 * @param frame
	 *            application frame
	 * @param broadcaster
	 *            broadcasts state changes
	 * @param gateListRender
	 *            renders the gate list elements
	 */
	@Inject
	public GateSet(final Frame frame, final Broadcaster broadcaster,
			final GateListCellRenderer gateListRender) {
		super(frame, "Gate setting <none>", false, broadcaster);
		setResizable(false);
		final java.awt.Container contents = getContentPane();
		contents.setLayout(new BorderLayout());
		setLocation(20, 50);
		/* panel with chooser */
		final JPanel pChooser = new JPanel();
		pChooser.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
		cgate = new JComboBox<Object>(new GateComboBoxModel());
		final java.awt.Dimension dimset = cgate.getPreferredSize();
		dimset.width = 200;
		cgate.setPreferredSize(dimset);
		cgate.setRenderer(gateListRender);
		cgate.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				final Object item = cgate.getSelectedItem();
				if (item instanceof Gate) {
					selectGate((Gate) item);
				}
			}
		});
		pChooser.add(cgate);
		// panel with data fields
		final JPanel pFields = new JPanel();
		pFields.setLayout(new GridLayout(2, 1));
		final JPanel panel1 = new JPanel(new FlowLayout());
		lLower = new JLabel("lower", SwingConstants.RIGHT);
		panel1.add(lLower);
		textLower = new JTextField("", 4);
		panel1.add(textLower);
		final JPanel panel2 = new JPanel(new FlowLayout());
		lUpper = new JLabel("upper", SwingConstants.RIGHT);
		panel2.add(lUpper);
		textUpper = new JTextField("", 4);
		panel2.add(textUpper);
		pFields.add(panel1);
		pFields.add(panel2);
		/* panel with buttons add remove buttons */
		final JPanel pedit = new JPanel();
		pedit.setLayout(new GridLayout(3, 1, 5, 5));
		final Border border = new EmptyBorder(0, 0, 10, 30);
		pedit.setBorder(border);
		addP = new JButton("Add");
		addP.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				addPoint();
			}
		});
		addP.setEnabled(false);
		pedit.add(addP);
		removeP = new JButton("Remove");
		removeP.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				removePoint();
			}
		});
		removeP.setEnabled(false);
		pedit.add(removeP);
		unset = new JButton("Unset");
		unset.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				unset();
			}
		});
		unset.setEnabled(false);
		pedit.add(unset);
		// panel with OK, Cancel buttons
		final JPanel pokcancel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridLayout(1, 0, 5, 5));
		pokcancel.add(pButtons);
		save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				save();
			}
		});
		save.setEnabled(false);
		pButtons.add(save);
		cancel = new JButton(new jam.ui.WindowCancelAction(canceller));
		cancel.setEnabled(false);
		pButtons.add(cancel);
		contents.add(pChooser, BorderLayout.NORTH);
		contents.add(pFields, BorderLayout.CENTER);
		contents.add(pedit, BorderLayout.EAST);
		contents.add(pokcancel, BorderLayout.SOUTH);
		setWindowHandlers();
		pack();
	}

	private void setWindowHandlers() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(final WindowEvent windowEvent) {
				checkHistogram();
			}

			@Override
			public void windowClosing(final WindowEvent windowEvent) {
				canceller.cancel();
				dispose();
			}

			@Override
			public void windowOpened(final WindowEvent windowEvent) {
				doSetup();
			}
		});
	}

	/**
	 * Add a point from the text fields.
	 */
	private void addPoint() {
		try {
			final int xbin = Integer.parseInt(textLower.getText().trim());
			final int ybin = Integer.parseInt(textUpper.getText().trim());
			final Bin bin = Bin.create(xbin, ybin);
			addPoint(bin);
			broadcaster.broadcast(BroadcastEvent.Command.GATE_SET_ADD, bin);
		} catch (NumberFormatException ne) {
			LOGGER.log(Level.SEVERE, "Invalid input: not a number.", ne);
		}
	}

	/**
	 * Add a point to the gate when we are setting a new gate.
	 * 
	 * @param pChannel
	 *            the point corresponding to the channel to add
	 */
	private void addPoint(final Bin pChannel) {
		if (newGate) { // do nothing if no gate chosen
			synchronized (this) {
				if (currentHistogram instanceof AbstractHist1D) {
					synchronized (gatePoints) {
						if (gatePoints.isEmpty()) {
							gatePoints.add(pChannel);
							textLower.setText(String.valueOf(pChannel.getX()));
						} else if (gatePoints.size() == 1) {
							gatePoints.add(pChannel);
							setLowerUpperText();
						} else if (gatePoints.size() == 2) {
							gatePoints.remove(0);
							gatePoints.add(pChannel);
							setLowerUpperText();
						} else {
							LOGGER.severe(getClass().getName()
									+ ".addPoint(): setting 1 d gate should not be here.");
						}
					}
				} else if (currentHistogram instanceof AbstractHist2D) {
					synchronized (gatePoints) {
						gatePoints.add(pChannel);
						textLower.setText(String.valueOf(pChannel.getX()));
						textUpper.setText(String.valueOf(pChannel.getY()));
					}
				}
			}
		} else {
			LOGGER.severe(getClass().getName()
					+ ".addPoint(Point): an expected condition was not true. "
					+ "Contact the developer.");
		}
	}

	/**
	 * Cancel the setting of the gate and disable editting of all fields.
	 */
	private transient final Canceller canceller = new Canceller() {
		public void cancel() {
			checkHistogram();
			broadcaster.broadcast(BroadcastEvent.Command.GATE_SET_OFF);
			setTitle("Gate setting <none>");
			synchronized (this) {
				newGate = false;
				gatePoints.clear();
			}
			textLower.setText(" ");
			textLower.setEditable(false);
			textUpper.setText(" ");
			textUpper.setEditable(false);
			addP.setEnabled(false);
			removeP.setEnabled(false);
			save.setEnabled(false);
			cancel.setEnabled(false);
			unset.setEnabled(false);
		}
	};

	/**
	 * Check that plot's current histogram has not changed. If so, cancel and
	 * make the plot's current histogram our current histogram.
	 * 
	 * @author Ken Swartz
	 */
	private void checkHistogram() {
		/* has histogram changed? */
		final Nameable named = SelectionTree.getCurrentHistogram();
		if (!currentHistogram.equals(named)) {
			doSetup(); // setup chooser list
			canceller.cancel(); // cancel current gate if was setting
		}
	}

	/**
	 * Loads the list of gates and set co-ordinates as x y if 2d or lower upper
	 * if 1 d
	 * 
	 */
	@Override
	public void doSetup() {
		synchronized (this) {
			currentHistogram = SelectionTree.getCurrentHistogram();
			setupType();
			cgate.setSelectedIndex(0);
		}
	}

	/**
	 * Output the list of gate points to the console.
	 * 
	 * @param poly
	 *            the points defining the gate
	 */
	private void printPoints(final Polygon poly) {
		final int xcoord[] = poly.xpoints;
		final int ycoord[] = poly.ypoints;
		final StringBuilder msg = new StringBuilder("Gate points: ");
		for (int i = 0; i < poly.npoints; i++) {
			msg.append('[').append(xcoord[i]).append(',').append(ycoord[i])
					.append("] ");
		}
		LOGGER.info(msg.toString());
	}

	/**
	 * Remove a point in setting a 2d gate.
	 */
	private void removePoint() {
		if (!gatePoints.isEmpty()) {
			gatePoints.remove(gatePoints.size() - 1);
			broadcaster.broadcast(BroadcastEvent.Command.GATE_SET_REMOVE);
			if (gatePoints.isEmpty()) {
				textLower.setText("");
				textUpper.setText("");
			} else {
				final Bin lastBin = gatePoints.get(gatePoints.size() - 1);
				textLower.setText(String.valueOf(lastBin.getX()));
				textUpper.setText(String.valueOf(lastBin.getY()));
			}
		}
	}

	/**
	 * Save the gate value.
	 */
	private void save() {
		checkHistogram(); // check we have same histogram
		try { // check fields are numbers
			if (currentGate != null) {
				saveGate();
				broadcaster.broadcast(BroadcastEvent.Command.GATE_SELECT,
						currentGate);
				cgate.repaint();
				broadcaster.broadcast(BroadcastEvent.Command.GATE_SET_SAVE);
			}
		} catch (NumberFormatException ne) {
			LOGGER.log(Level.SEVERE, "Invalid input: not a number.", ne);
		}
		canceller.cancel();
	}

	private void saveGate() {
		if (currentHistogram instanceof AbstractHist1D) {
			final int lim1 = Integer.parseInt(textLower.getText());
			final int lim2 = Integer.parseInt(textUpper.getText());
			currentGate.setLimits(lim1, lim2);
			LOGGER.info("Gate Set " + currentGate.getName() + " Limits=" + lim1
					+ "," + lim2);
		} else if (currentHistogram instanceof AbstractHist2D) {
			saveTwoDimensionalGate();
		}
	}

	private void saveTwoDimensionalGate() {
		/* complete gate, adding a last point = first point */
		gatePoints.add(gatePoints.get(0));
		/* make a polygon from data points */
		final Polygon gatePoly2d = new Polygon();
		for (Bin gatePoint : gatePoints) {
			gatePoly2d.addPoint(gatePoint.getX(), gatePoint.getY());
		}
		currentGate.setLimits(gatePoly2d);
		LOGGER.info("Gate Set " + currentGate.getName());
		printPoints(gatePoly2d);
	}

	protected void selectGate(final Gate gate) {
		canceller.cancel(); // cancel current state
		synchronized (this) {
			currentGate = gate;
		}
		if (currentGate != null) {
			synchronized (this) {
				newGate = true; // setting a new gate
				gatePoints.clear();
				if (currentHistogram instanceof AbstractHist2D) {
					addP.setEnabled(true);
					removeP.setEnabled(true);
				}
			}
			unset.setEnabled(true);
			broadcaster.broadcast(BroadcastEvent.Command.GATE_SET_ON);
			// change the title of the dialog
			setTitle("Gate setting " + currentGate.getName());
			// make fields and buttons active
			textLower.setText("");
			textLower.setEditable(true);
			textUpper.setText("");
			textUpper.setEditable(true);
			save.setEnabled(true);
			cancel.setEnabled(true);
		}
	}

	private void setLowerUpperText() {
		final int limA = gatePoints.get(0).getX();
		final int limB = gatePoints.get(1).getX();
		final int min = Math.min(limA, limB);
		final int max = Math.max(limA, limB);
		textLower.setText(String.valueOf(min));
		textUpper.setText(String.valueOf(max));
	}

	private void setupType() {
		synchronized (this) {
			gatePoints.clear();
			if (currentHistogram instanceof AbstractHist1D) {
				lLower.setText(" lower");
				lUpper.setText(" upper");
			} else if (currentHistogram instanceof AbstractHist2D) {
				lLower.setText("  x  ");
				lUpper.setText("  y  ");
			} else if (currentHistogram instanceof UnNamed) {
				lLower.setText(null);
				lUpper.setText(null);
			} else {
				LOGGER.severe(getClass().getName()
						+ ".setup(): undefined histogram type "
						+ currentHistogram.getClass().getName());
			}
		}
	}

	private void unset() {
		currentGate.unsetLimits();
		cgate.repaint();
		LOGGER.info("Gate UnSet: " + currentGate.getName());
		canceller.cancel();
		broadcaster.broadcast(BroadcastEvent.Command.GATE_SET_OFF);
	}

	/**
	 * Implementation of Observable interface To receive broadcast events.
	 * 
	 * @param observable
	 *            the event sender
	 * @param object
	 *            the message
	 */
	@Override
	public void update(final Observable observable, final Object object) {
		final BroadcastEvent event = (BroadcastEvent) object;
		final BroadcastEvent.Command com = event.getCommand();
		if (com == BroadcastEvent.Command.HISTOGRAM_SELECT) {
			canceller.cancel();
		} else if (com == BroadcastEvent.Command.HISTOGRAM_NEW
				|| com == BroadcastEvent.Command.HISTOGRAM_ADD
				|| com == BroadcastEvent.Command.GATE_ADD) {
			doSetup();
		} else if (com == BroadcastEvent.Command.GATE_SET_POINT) {
			addPoint((Bin) event.getContent());
		}
	}
}
