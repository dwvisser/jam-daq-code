/*
 * Created on Dec 31, 2003
 */
package jam;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.*;
import jam.plot.Display;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * The selection tool bar the at the top of the plot.
 *
 * @version 1.4
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
final class SelectionToolbar extends JToolBar implements Observer {

	final JLabel lrunState = new JLabel("   Welcome   ", SwingConstants.CENTER);
	final JComboBox histogramChooser =
		new JComboBox(new HistogramComboBoxModel());
	final JToggleButton boverLay = new JToggleButton("Overlay");
	final JComboBox gateChooser = new JComboBox(new GateComboBoxModel());
	final MessageHandler console;
	final JamStatus status;
	final Broadcaster broadcaster;
	final Display display;
	private boolean overlay = false;
	final String classname;

	SelectionToolbar(
		MessageHandler mh,
		JamStatus js,
		Broadcaster b,
		Display d) {
		super("Selection", JToolBar.HORIZONTAL);
		classname = getClass().getName() + "--";
		console = mh;
		status = js;
		broadcaster = b;
		display = d;
		final DefaultComboBoxModel noGateComboBoxModel =
			new DefaultComboBoxModel();
		noGateComboBoxModel.addElement("NO GATES");
		/* panel with selection and print etc. */
		setLayout(new BorderLayout());
		final JPanel pRunState = new JPanel(new GridLayout(1, 1));
		pRunState.setBorder(
			BorderFactory.createTitledBorder(
				new BevelBorder(BevelBorder.LOWERED),
				"Status",
				TitledBorder.CENTER,
				TitledBorder.TOP));
		lrunState.setOpaque(true);
		lrunState.setForeground(Color.black);
		pRunState.add(lrunState);
		final JPanel pCenter = new JPanel(new GridLayout(1, 0));
		histogramChooser.setRenderer(new HistogramListCellRenderer());
		histogramChooser.setMaximumRowCount(30);
		histogramChooser.setSelectedIndex(0);
		histogramChooser.setToolTipText("Choose histogram to display.");
		histogramChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final Object item =
					((JComboBox) ae.getSource()).getSelectedItem();
				if (item instanceof Histogram) {
					final Histogram h = (Histogram) item;
					selectHistogram(h);
				} else {
					selectHistogram(null);
				}
			}
		});
		pCenter.add(histogramChooser);
		boverLay.setToolTipText("Click to overlay next histogram chosen.");
		boverLay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				synchronized (this) {
					overlay = overlaySelected();
				}
				if (overlay) {
					console.messageOut("Overlay Spectrum ", MessageHandler.NEW);
				}
			}
		});
		pCenter.add(boverLay);
		gateChooser.setRenderer(new GateListCellRenderer());
		gateChooser.setToolTipText("Click to choose gate to display.");
		gateChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final Object item =
					((JComboBox) ae.getSource()).getSelectedItem();
				if (item instanceof Gate) {
					final Gate gate = (Gate) item;
					if (gate != null  && gate.isDefined()) {
						selectGate(gate);
					}
				}
			}
		});
		pCenter.add(gateChooser);
		add(pRunState, BorderLayout.WEST);
		add(pCenter, BorderLayout.CENTER);
	}

	private void histogramsChanged() {
		histogramChooser.setSelectedIndex(0);
		histogramChooser.repaint();
	}

	private void gatesChanged() {
		gateChooser.setSelectedIndex(0);
		gateChooser.repaint();
	}

	/**
	 * Should be called whenever the lists of gates and histograms 
	 * change. It calls histogramsChanged() and gatesChanged(), 
	 * each of which add to the event stack, so that histograms will 
	 * be guaranteed (?) updated before gates get updated.
	 */
	private void dataChanged() {
		histogramsChanged();
		gatesChanged();
	}

	void setOverlayEnabled(boolean state) {
		this.boverLay.setEnabled(state);
	}

	/**
	 * @return whether histogram overlay mode is enabled
	 */
	public boolean overlaySelected() {
		return boverLay.isSelected();
	}

	/**
	 * De-select overlay mode.
	 */
	public void deselectOverlay() {
		if (boverLay.isSelected()) {
			boverLay.doClick();
		}
	}

	public void setRunState(RunState rs) {
		lrunState.setBackground(rs.getColor());
		lrunState.setText(rs.getLabel());
	}

	/**
	 * Selects first items in histogram and gate choosers.  Default 
	 * priveleges allows JamCommand to call this as well.
	 */
	void setChoosersToFirstItems() {
		histogramChooser.setSelectedIndex(0);
		gateChooser.setSelectedIndex(0);
	}

	/** 
	 * A histogram has been selected so tell all
	 * applicable classes about it.
	 *
	 * @param hist The histogram to be selected and displayed
	 */
	private void selectHistogram(Histogram hist) {
		if (hist != null) {
			if (!overlay) {
				status.setCurrentHistogramName(hist.getName());
				try {
					broadcaster.broadcast(BroadcastEvent.HISTOGRAM_SELECT);
					final Histogram h =
						Histogram.getHistogram(
							status.getCurrentHistogramName());
					display.displayHistogram(h);
					gatesChanged();
					setOverlayEnabled(h.getDimensionality() == 1);
				} catch (GlobalException ge) {
					console.errorOutln(
						getClass().getName() + ".selectHistogram(): " + ge);
				}
			} else {
				status.setOverlayHistogramName(hist.getName());
				console.messageOut(hist.getName(), MessageHandler.END);
				display.overlayHistogram(hist);
				synchronized (this) {
					overlay = false;
				}
				deselectOverlay();
			}
		} else { //null object passed
			display.displayHistogram(null);
		}
	}
	
	private void syncHistChooser(){
		Histogram hist=Histogram.getHistogram(status.getCurrentHistogramName());
		if (hist != null){
			if (!hist.equals(histogramChooser.getSelectedItem())){
				histogramChooser.setSelectedItem(hist);
				//histogramChooser.repaint();
				gatesChanged();
			}
		}
	}

	/**
	 * A gate has been selected. Tell all appropriate classes, like
	 * Display and JamStatus.
	 *
	 * @param gateObject the object, which should be a <code>Gate</code>
	 * @see jam.data.Gate
	 */
	private void selectGate(Gate gate) {
		final String methodname = "selectGate(): ";
		//final Gate gate = (Gate) gateObject;
		try {
			status.setCurrentGateName(gate.getName());
			broadcaster.broadcast(BroadcastEvent.GATE_SELECT);
			if (gate.getType() == Gate.ONE_DIMENSION) {
				final double area = gate.getArea();
				final double centroid =
					(double) ((int) (gate.getCentroid() * 100.0)) / 100.0;
				final int lowerLimit = gate.getLimits1d()[0];
				final int upperLimit = gate.getLimits1d()[1];
				console.messageOut(
					"Gate: "
						+ gate.getName()
						+ ", Ch. "
						+ lowerLimit
						+ " to "
						+ upperLimit,
					MessageHandler.NEW);
				console.messageOut(
					"  Area = " + area + ", Centroid = " + centroid,
					MessageHandler.END);
			} else {
				final double area = gate.getArea();
				console.messageOut(
					"Gate " + gate.getName(),
					MessageHandler.NEW);
				console.messageOut(", Area = " + area, MessageHandler.END);
			}
			display.displayGate(gate);
		} catch (Exception de) {
			console.errorOutln(classname + methodname + de.getMessage());
		}
	}

	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable the sender
	 * @param o the message
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final int command = be.getCommand();
		if (command == BroadcastEvent.HISTOGRAM_NEW) {
			final String lastHistName = status.getCurrentHistogramName();
			selectHistogram(Histogram.getHistogram(lastHistName));
			dataChanged();
		}
		if (command == BroadcastEvent.HISTOGRAM_ADD) {
			dataChanged();
		}
		if (command == BroadcastEvent.GATE_ADD) {
			final String lastHistName = status.getCurrentHistogramName();
			selectHistogram(Histogram.getHistogram(lastHistName));
			gatesChanged();
		}
		if (command == BroadcastEvent.GATE_SET_SAVE || 
		command == BroadcastEvent.GATE_SET_OFF){
			gateChooser.repaint();
			histogramChooser.repaint();
		}
		if (command==BroadcastEvent.HISTOGRAM_SELECT){
			syncHistChooser();
		}
	}

}
