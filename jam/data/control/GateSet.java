package jam.data.control;

import jam.data.DataException;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;
import jam.global.Nameable;
import jam.global.UnNamed;
import jam.plot.Bin;
import jam.ui.GateComboBoxModel;
import jam.ui.GateListCellRenderer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Class to set 1 D and 2 D gates.
 * 
 * @version 0.5 April 1998
 * @author Ken Swartz
 */
public class GateSet extends AbstractControl implements Observer {

	private static final int NONE = -1;

	private static final int ONE_DIMENSION = 1;

	private static final int TWO_DIMENSION = 2;

	private final JButton addP, removeP, unset, save, cancel;

	private final JComboBox cgate;

	private Gate currentGate;

	private Histogram currentHistogram;

	private List<Bin> gatePoints;

	private final JLabel lLower, lUpper;

	private final MessageHandler messageHandler;

	private boolean newGate = false; // a gate has been chosen

	// number intial points, increment increase
	private int numberPoints;

	private final JTextField textLower, textUpper;

	private int type;

	/**
	 * Creates an instance of the GateControl class.
	 */
	public GateSet() {
		super("Gate setting <none>", false);
		messageHandler = STATUS.getMessageHandler();
		setResizable(false);
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout());
		setLocation(20, 50);
		/* panel with chooser */
		JPanel pChooser = new JPanel();
		pChooser.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
		cgate = new JComboBox(new GateComboBoxModel());
		Dimension dimset = cgate.getPreferredSize();
		dimset.width = 200;
		cgate.setPreferredSize(dimset);
		cgate.setRenderer(new GateListCellRenderer());
		cgate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Object item = cgate.getSelectedItem();
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
		lLower = new JLabel("lower", Label.RIGHT);
		panel1.add(lLower);
		textLower = new JTextField("", 4);
		panel1.add(textLower);
		final JPanel panel2 = new JPanel(new FlowLayout());
		lUpper = new JLabel("upper", Label.RIGHT);
		panel2.add(lUpper);
		textUpper = new JTextField("", 4);
		panel2.add(textUpper);
		pFields.add(panel1);
		pFields.add(panel2);
		/* panel with buttons add remove buttons */
		final JPanel pedit = new JPanel();
		pedit.setLayout(new GridLayout(3, 1, 5, 5));
		Border border = new EmptyBorder(0, 0, 10, 30);
		pedit.setBorder(border);
		addP = new JButton("Add");
		addP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				addPoint();
			}
		});
		addP.setEnabled(false);
		pedit.add(addP);
		removeP = new JButton("Remove");
		removeP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				removePoint();
			}
		});
		removeP.setEnabled(false);
		pedit.add(removeP);
		unset = new JButton("Unset");
		unset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				unset();
			}
		});
		unset.setEnabled(false);
		pedit.add(unset);

		// panel with OK, Cancel buttons
		final JPanel pokcancel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final Panel pButtons = new Panel();
		pButtons.setLayout(new GridLayout(1, 0, 5, 5));
		pokcancel.add(pButtons);
		save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				save();
			}
		});
		save.setEnabled(false);
		pButtons.add(save);
		cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				cancel();
			}
		});
		cancel.setEnabled(false);
		pButtons.add(cancel);

		contents.add(pChooser, BorderLayout.NORTH);
		contents.add(pFields, BorderLayout.CENTER);
		contents.add(pedit, BorderLayout.EAST);
		contents.add(pokcancel, BorderLayout.SOUTH);
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent windowEvent) {
				checkHistogram();
			}

			public void windowClosing(WindowEvent windowEvent) {
				cancel();
				dispose();
			}

			public void windowOpened(WindowEvent windowEvent) {
				doSetup();
			}
		});
		pack();
	}

	/**
	 * Add a point from the text fields.
	 * 
	 * @throws DataException
	 *             if there's a problem with the number format
	 * @throws GlobalException
	 *             if there's additional problems
	 */
	private void addPoint() {
		try {
			final int xbin = Integer.parseInt(textLower.getText().trim());
			final int ybin = Integer.parseInt(textUpper.getText().trim());
			final Bin bin = Bin.create(xbin, ybin);
			addPoint(bin);
			BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SET_ADD, bin);
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
	private void addPoint(Bin pChannel) {
		if (newGate) { // do nothing if no gate chosen
			if (type == ONE_DIMENSION) {
				if (numberPoints == 0) {
					synchronized (this) {
						numberPoints = 1;
					}
					gatePoints.add(pChannel);
					textLower.setText(String.valueOf(pChannel.getX()));
				} else if (numberPoints == 1) {
					synchronized (this) {
						numberPoints = 0;
					}
					gatePoints.add(pChannel);
					textUpper.setText(String.valueOf(pChannel.getX()));
				} else {
					LOGGER.severe(getClass().getName()
									+ ".addPoint(): setting 1 d gate should not be here.");
				}
			} else if (type == TWO_DIMENSION) {
				gatePoints.add(pChannel);
				textLower.setText(String.valueOf(pChannel.getX()));
				textUpper.setText(String.valueOf(pChannel.getY()));
			}
		} else {
			LOGGER.severe(getClass().getName()
					+ ".addPoint(Point): an expected condition was not true. "
					+ "Contact the developer.");
		}
	}

	/**
	 * Cancel the setting of the gate and disable editting of all fields.
	 * 
	 * @throws GlobalException
	 *             if there's a problem
	 */
	private void cancel() {
		checkHistogram();
		BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SET_OFF);
		setTitle("Gate setting <none>");
		synchronized (this) {
			newGate = false;
			gatePoints = null;
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

	/**
	 * Check that plot's current histogram has not changed. If so, cancel and
	 * make the plot's current histogram our current histogram.
	 * 
	 * @author Ken Swartz
	 * @throws GlobalException
	 *             if there's a problem
	 */
	private void checkHistogram() {
		/* has histogram changed? */
		final Nameable named = STATUS.getCurrentHistogram();
		final boolean doIt = (named == UnNamed.getSingletonInstance()) ?
			currentHistogram != null : currentHistogram != named;
		if (doIt) {
			doSetup(); // setup chooser list
			cancel(); // cancel current gate if was setting
		}
	}

	/**
	 * Loads the list of gates and set co-ordinates as x y if 2d or lower upper
	 * if 1 d
	 * 
	 */
	public void doSetup() {
		/* get current state */
		synchronized (this) {
			final Nameable named = STATUS.getCurrentHistogram();
			currentHistogram = (named instanceof Histogram) ? (Histogram) named
					: null;
		}
		if (currentHistogram == null) {
			/* There are many normal situations with no current histogram. */
			setType(NONE); // undefined type
		} else if (currentHistogram.getDimensionality() == 1) {
			setType(1);
		} else if (currentHistogram.getDimensionality() == 2) {
			setType(2);
		} else {
			LOGGER.severe(getClass().getName()
					+ ".setup(): undefined histogram type.");
			setType(NONE);
		}
		cgate.setSelectedIndex(0);
		// change labels depending if we have a one or two D histogram
		if (currentHistogram != null
				&& currentHistogram.getDimensionality() == 1) {
			setType(ONE_DIMENSION);
			lLower.setText(" lower");
			lUpper.setText(" upper");
		} else {
			setType(TWO_DIMENSION);
			lLower.setText("  x  ");
			lUpper.setText("  y  ");
		}
	}

	/**
	 * Output the list of gate points to the console.
	 * 
	 * @param poly
	 *            the points defining the gate
	 */
	private void printPoints(Polygon poly) {
		final int x[] = poly.xpoints;
		final int y[] = poly.ypoints;
		messageHandler.messageOut("Gate points: ", MessageHandler.NEW);
		for (int i = 0; i < poly.npoints; i++) {
			messageHandler.messageOut("[" + x[i] + "," + y[i] + "] ");
		}
		messageHandler.messageOut("", MessageHandler.END);
	}

	/**
	 * Remove a point in setting a 2d gate.
	 */
	private void removePoint() {
		if (!gatePoints.isEmpty()) {
			gatePoints.remove(gatePoints.size() - 1);
			BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SET_REMOVE);
			if (!gatePoints.isEmpty()) {
				final Bin lastBin = gatePoints.get(gatePoints.size() - 1);
				textLower.setText(String.valueOf(lastBin.getX()));
				textUpper.setText(String.valueOf(lastBin.getY()));
			} else {
				textLower.setText("");
				textUpper.setText("");
			}
		}
	}

	/**
	 * Save the gate value.
	 * 
	 * @throws DataException
	 *             if there's a problem
	 * @throws GlobalException
	 *             if there's a problem
	 */
	private void save() {
		checkHistogram(); // check we have same histogram
		try { // check fields are numbers
			if (currentGate != null) {
				if (type == ONE_DIMENSION) {
					final int lim1 = Integer.parseInt(textLower.getText());
					final int lim2 = Integer.parseInt(textUpper.getText());
					currentGate.setLimits(lim1, lim2);
					messageHandler.messageOutln("Gate Set "
							+ currentGate.getName() + " Limits=" + lim1 + ","
							+ lim2);
				} else if (type == TWO_DIMENSION) {
					/* complete gate, adding a last point = first point */
					gatePoints.add(gatePoints.get(0));
					/* make a polygon from data points */
					final Polygon gatePoly2d = new Polygon();
					for (int i = 0; i < gatePoints.size(); i++) {
						final int pointX = gatePoints.get(i).getX();
						final int pointY = gatePoints.get(i).getY();
						gatePoly2d.addPoint(pointX, pointY);
					}
					currentGate.setLimits(gatePoly2d);
					messageHandler.messageOutln("Gate Set "
							+ currentGate.getName());
					printPoints(gatePoly2d);
				}
				BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SELECT,
						currentGate);
				cgate.repaint();
				BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SET_SAVE);
			}
		} catch (NumberFormatException ne) {
			LOGGER.log(Level.SEVERE, "Invalid input: not a number.", ne);
		}
		cancel();
	}

	void selectGate(Gate gate) {
		cancel(); // cancel current state
		synchronized (this) {
			currentGate = gate;
		}
		if (currentGate != null) {
			synchronized (this) {
				newGate = true; // setting a new gate
				numberPoints = 0;
			}
			if (currentHistogram.getDimensionality() == 1) {
				lLower.setText("lower");
				lUpper.setText("upper");
				synchronized (this) {
					type = ONE_DIMENSION;
					gatePoints = new ArrayList<Bin>(2);
				}
			} else {
				lLower.setText("x");
				lUpper.setText("y");
				synchronized (this) {
					type = TWO_DIMENSION;
					gatePoints = new ArrayList<Bin>();
				}
				addP.setEnabled(true);
				removeP.setEnabled(true);
			}
			unset.setEnabled(true);
			BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SET_ON);
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

	private synchronized void setType(int m) {
		type = m;
	}

	private void unset() {
		currentGate.unsetLimits();
		cgate.repaint();
		messageHandler.messageOutln("Gate UnSet: " + currentGate.getName());
		cancel();
		BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SET_OFF);
	}

	/**
	 * Implementation of Observable interface To receive broadcast events.
	 * 
	 * @param observable
	 *            the event sender
	 * @param o
	 *            the message
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final BroadcastEvent.Command com = be.getCommand();
		if (com == BroadcastEvent.Command.HISTOGRAM_SELECT) {
			cancel();
		} else if (com == BroadcastEvent.Command.HISTOGRAM_NEW
				|| com == BroadcastEvent.Command.HISTOGRAM_ADD
				|| com == BroadcastEvent.Command.GATE_ADD) {
			doSetup();
		} else if (com == BroadcastEvent.Command.GATE_SET_POINT) {
			addPoint((Bin) be.getContent());
		}
	}
}