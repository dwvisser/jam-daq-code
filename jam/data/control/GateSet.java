package jam.data.control;
import jam.data.DataException;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;
import jam.plot.Bin;
import jam.ui.GateComboBoxModel;
import jam.ui.GateListCellRenderer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

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
public class GateSet extends DataControl implements Observer {

	private static final int ONE_DIMENSION = 1;
	private static final int TWO_DIMENSION = 2;
	private static final int NONE = -1;
	private boolean newGate = false; //a gate has been chosen
	private final Frame frame;
	private final MessageHandler messageHandler;
	private Histogram currentHistogram;
	private Gate currentGate;
	private int type;
	private java.util.List gatePoints;
	//number intial points, increment increase
	private int numberPoints;
	private final JComboBox cgate;
	private final JLabel lLower, lUpper;
	private final JTextField textLower, textUpper;
	private final JButton addP, removeP, unset, save, cancel;

	/**
	 * Creates an instance of the GateControl class.
	 */
	public GateSet() {
		super("Gate setting <none>", false);
		messageHandler = status.getMessageHandler();
		frame = status.getFrame();
		setResizable(false);
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout());
		setLocation(20, 50);

		//panel with chooser
		JPanel pc = new JPanel();
		pc.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
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
		pc.add(cgate);
		// panel with data fields
		final JPanel pf = new JPanel();
		pf.setLayout(new GridLayout(2, 1));
		final JPanel p1 = new JPanel(new FlowLayout());
		lLower = new JLabel("lower", Label.RIGHT);
		p1.add(lLower);
		textLower = new JTextField("", 4);
		p1.add(textLower);
		final JPanel p2 = new JPanel(new FlowLayout());
		lUpper = new JLabel("upper", Label.RIGHT);
		p2.add(lUpper);
		textUpper = new JTextField("", 4);
		p2.add(textUpper);
		pf.add(p1);
		pf.add(p2);

		// panel with buttons add remove buttons
		final JPanel pedit = new JPanel();
		pedit.setLayout(new GridLayout(3, 1, 5, 5));
		Border border = new EmptyBorder(0, 0, 10, 30);
		pedit.setBorder(border);

		addP = new JButton("Add");
		addP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				addPoint();
			}
		});
		addP.setEnabled(false);
		pedit.add(addP);
		removeP = new JButton("Remove");
		removeP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				removePoint();
			}
		});
		removeP.setEnabled(false);
		pedit.add(removeP);
		unset = new JButton("Unset");
		unset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				unset();
			}
		});
		unset.setEnabled(false);
		pedit.add(unset);

		// panel with OK, Cancel buttons
		final JPanel pokcancel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final Panel pb = new Panel();
		pb.setLayout(new GridLayout(1, 0, 5, 5));
		pokcancel.add(pb);
		save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				save();
			}
		});
		save.setEnabled(false);
		pb.add(save);
		cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				cancel();
			}
		});
		cancel.setEnabled(false);
		pb.add(cancel);

		contents.add(pc, BorderLayout.NORTH);
		contents.add(pf, BorderLayout.CENTER);
		contents.add(pedit, BorderLayout.EAST);
		contents.add(pokcancel, BorderLayout.SOUTH);
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				checkHistogram();
			}

			public void windowClosing(WindowEvent e) {
				cancel();
				dispose();
			}

			public void windowOpened(WindowEvent e) {
				setup();
			}
		});
		pack();
	}

	void selectGate(Gate gate) {
		cancel(); //cancel current state
		synchronized (this) {
			currentGate = gate;
		}
		if (currentGate != null) {
			synchronized (this) {
				newGate = true; //setting a new gate
				numberPoints = 0;
			}
			if (currentHistogram.getDimensionality() == 1) {
				lLower.setText("lower");
				lUpper.setText("upper");
				synchronized (this) {
					type = ONE_DIMENSION;
					gatePoints = new ArrayList(2);
				}
			} else {
				lLower.setText("x");
				lUpper.setText("y");
				synchronized (this) {
					type = TWO_DIMENSION;
					gatePoints = new ArrayList();
				}
				addP.setEnabled(true);
				removeP.setEnabled(true);
			}
			unset.setEnabled(true);
			broadcaster.broadcast(BroadcastEvent.GATE_SET_ON);
			//change the title of the dialog
			setTitle("Gate setting " + currentGate.getName());
			//make fields and buttons active
			textLower.setText("");
			textLower.setEditable(true);
			textUpper.setText("");
			textUpper.setEditable(true);
			save.setEnabled(true);
			cancel.setEnabled(true);
		}
	}

	/**
	 * Implementation of Observable interface
	 * To receive broadcast events.
	 *
	 * @param observable the event sender
	 * @param o the message
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		if (be.getCommand() == BroadcastEvent.HISTOGRAM_SELECT) {
			cancel();
		} else if (be.getCommand() == BroadcastEvent.HISTOGRAM_NEW) {
			setup();
		} else if (be.getCommand() == BroadcastEvent.HISTOGRAM_ADD) {
			setup();
		} else if (be.getCommand() == BroadcastEvent.GATE_ADD) {
			setup();
		} else if (be.getCommand() == BroadcastEvent.GATE_SET_POINT) {
			addPoint((Bin) be.getContent());
		}
	}
	
	private synchronized void setType(int m){
		type=m;
	}

	/**
	 * Loads the list of gates and
	 * set co-ordinates as x y if 2d
	 * or lower upper if 1 d
	 *
	 */
	public void setup() {
		/* get current state */
		synchronized (this) {
			currentHistogram =
				Histogram.getHistogram(status.getCurrentHistogramName());
		}
		if (currentHistogram == null) {
			/* There are many normal situations with no current histogram. */
			setType(NONE); //undefined type
		} else if (
			(currentHistogram.getType() == Histogram.ONE_DIM_INT)
				|| (currentHistogram.getType() == Histogram.ONE_DIM_DOUBLE)) {
			setType(Gate.ONE_DIMENSION);
		} else if (
			(currentHistogram.getType() == Histogram.TWO_DIM_INT)
				|| (currentHistogram.getType() == Histogram.TWO_DIM_DOUBLE)) {
			setType(Gate.TWO_DIMENSION);
		} else {
			messageHandler.errorOutln(
				getClass().getName() + ".setup(): undefined histogram type.");
			setType(NONE);
		}
		cgate.setSelectedIndex(0);
		//change labels depending if we have a one or two D histogram
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
	 * Add a point from the text fields.
	 *
	 * @throws DataException if there's a problem with the
	 * number format
	 * @throws GlobalException if there's additional problems
	 */
	private void addPoint() {
		try {
			final int x = Integer.parseInt(textLower.getText().trim());
			final int y = Integer.parseInt(textUpper.getText().trim());
			final Bin p = Bin.Factory.create(x, y); 
			addPoint(p);
			broadcaster.broadcast(BroadcastEvent.GATE_SET_ADD, p);
		} catch (NumberFormatException ne) {
			messageHandler.errorOutln("Invalid input not a number [GateSet]");
		}
	}
	/**
	 * Remove a point in setting a 2d gate.
	 */
	private void removePoint() {
		if (!gatePoints.isEmpty()) {
			gatePoints.remove(gatePoints.size() - 1);
			broadcaster.broadcast(BroadcastEvent.GATE_SET_REMOVE);
			if (!gatePoints.isEmpty()) {
				final Bin lastBin =
					(Bin) gatePoints.get(gatePoints.size() - 1);
				textLower.setText(String.valueOf(lastBin.getX()));
				textUpper.setText(String.valueOf(lastBin.getY()));
			} else {
				textLower.setText("");
				textUpper.setText("");
			}
		}
	}

	private void unset() {
		currentGate.unsetLimits();
		cgate.repaint();
		messageHandler.messageOutln("Gate UnSet: " + currentGate.getName());
		cancel();
		broadcaster.broadcast(BroadcastEvent.GATE_SET_OFF);
	}

	/**
	 * Add a point to the gate
	 * when we are setting a new gate.
	 *
	 * @param pChannel the point corresponding to the channel to add
	 */
	private void addPoint(Bin pChannel) {
		if (newGate) { //do nothing if no gate chosen
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
					messageHandler.errorOutln(
						getClass().getName()
							+ ".addPoint(): setting 1 d gate should not be here.");
				}
			} else if (type == TWO_DIMENSION) {
				gatePoints.add(pChannel);
				textLower.setText(String.valueOf(pChannel.getX()));
				textUpper.setText(String.valueOf(pChannel.getY()));
			}
		} else {
			messageHandler.errorOutln(
				getClass().getName()
					+ ".addPoint(Point): an expected condition was not true. "
					+ "Contact the developer.");
		}
	}

	/**
	 * Save the gate value.
	 *
	 * @throws DataException if there's a problem
	 * @throws GlobalException if there's a problem
	 */
	private void save() {
		checkHistogram(); //check we have same histogram
		try { //check fields are numbers
			if (currentGate != null) {
				if (type == ONE_DIMENSION) {
					final int x1 = Integer.parseInt(textLower.getText());
					final int x2 = Integer.parseInt(textUpper.getText());
					currentGate.setLimits(x1, x2);
					messageHandler.messageOutln(
						"Gate Set "
							+ currentGate.getName()
							+ " Limits="
							+ x1
							+ ","
							+ x2);
				} else if (type == TWO_DIMENSION) {
					/* complete gate, adding a last point = first point */
					gatePoints.add(gatePoints.get(0));
					/* make a polygon from data points */
					final Polygon gatePoly2d = new Polygon();
					for (int i = 0; i < gatePoints.size(); i++) {
						final int pointX = ((Bin) gatePoints.get(i)).getX();
						final int pointY = ((Bin) gatePoints.get(i)).getY();
						gatePoly2d.addPoint(pointX, pointY);
					}
					currentGate.setLimits(gatePoly2d);
					messageHandler.messageOutln(
						"Gate Set " + currentGate.getName());
					printPoints(gatePoly2d);
				}
				broadcaster.broadcast(BroadcastEvent.GATE_SELECT, currentGate);
				cgate.repaint();
				broadcaster.broadcast(BroadcastEvent.GATE_SET_SAVE);
			}
		} catch (NumberFormatException ne) {
			messageHandler.errorOutln("Invalid input not a number [GateSet]");
		}
		cancel();
	}

	/**
	 * Output the list of gate points to the console.
	 *
	 * @param poly the points defining the gate
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
	 * Cancel the setting of the gate and
	 * disable editting of all fields.
	 *
	 * @throws GlobalException if there's a problem
	 */
	private void cancel() {
		checkHistogram();
		broadcaster.broadcast(BroadcastEvent.GATE_SET_OFF);
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
	 * Check that plot's current histogram has not changed.
	 * If so, cancel and make the plot's current histogram
	 * our current histogram.
	 *
	 * @author Ken Swartz
	 * @throws GlobalException if there's a problem
	 */
	private void checkHistogram() {
		/* has histogram changed? */
		if (currentHistogram
			!= Histogram.getHistogram(status.getCurrentHistogramName())) {
			setup(); //setup chooser list
			cancel(); //cancel current gate if was setting
		}
	}
}
