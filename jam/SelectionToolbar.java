package jam;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.plot.Display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * The selection tool bar the at the top of the plot.
 *
 * @version 1.4
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @author Ken Swartz
 * @since 31 December 2003
 */
final class SelectionToolbar extends JToolBar implements Observer {

	final JLabel lrunState = new JLabel("   Welcome   ", SwingConstants.CENTER);
	final JPanel pCenter;
	private int previousLayout;
	final JComboBox histogramChooser = new JComboBox(new HistogramComboBoxModel());
	final JToggleButton boverLay = new JToggleButton(getHTML("<u>O</u>verlay"));
	final JComboBox gateChooser = new JComboBox(new GateComboBoxModel());
	final MessageHandler console;
	final JamStatus status;
	final Broadcaster broadcaster;
	final Display display;
	private boolean overlay = false;
	final String classname;
	final MainMenuBar mbar;

	SelectionToolbar(MessageHandler mh,
					 JamStatus js,
					 Broadcaster b,
					 Display d,
					 MainMenuBar mmb) {
		super("Selection", JToolBar.HORIZONTAL);
		this.setFloatable(false);
		final int chooserWidth=200;
		classname = getClass().getName() + "--";
		console = mh;
		status = js;
		broadcaster = b;
		display = d;
		mbar=mmb;
		final DefaultComboBoxModel noGateComboBoxModel = new DefaultComboBoxModel();
		noGateComboBoxModel.addElement("NO GATES");
		pCenter = new JPanel();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		pCenter.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));
		previousLayout=VERTICAL;
		/* Run status */
		final Box pRunState = new Box(BoxLayout.X_AXIS);
		pRunState.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		pRunState.add(new JLabel(" Status: "));
		lrunState.setOpaque(true);
		lrunState.setForeground(Color.BLACK);
		pRunState.add(lrunState);
		histogramChooser.setRenderer(new HistogramListCellRenderer());
		histogramChooser.setMaximumRowCount(30);
		histogramChooser.setSelectedIndex(0);

		Dimension dim=histogramChooser.getPreferredSize();
		dim.width=chooserWidth;
		histogramChooser.setPreferredSize(dim);
		histogramChooser.setToolTipText(getHTML("<u>D</u>isplay chosen histogram."));
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
		boverLay.setToolTipText("Overlay next histogram choice.");
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
		dim=gateChooser.getPreferredSize();
		dim.width=chooserWidth;
		gateChooser.setPreferredSize(dim);
		gateChooser.setToolTipText("Display chosen gate.");
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
		add(pRunState);
		addSeparator();
		add(pCenter);
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

	private String getHTML(String body){
		final StringBuffer rval=new StringBuffer("<html><body>").append(
		body).append("</html></body>");
		return rval.toString();
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
				broadcaster.broadcast(BroadcastEvent.HISTOGRAM_SELECT);
				final Histogram h =
					Histogram.getHistogram(
					status.getCurrentHistogramName());
				display.displayHistogram(h);
				gatesChanged();
				setOverlayEnabled(h.getDimensionality() == 1);
				mbar.adjustHistogramItems(h);
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
			mbar.adjustHistogramItems(null);
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
