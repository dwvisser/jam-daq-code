package jam.ui;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.plot.Display;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * The selection tool bar the at the top of the plot.
 * 
 * @version 1.4
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @author Ken Swartz
 * @since 31 December 2003
 */
public final class SelectionToolbar extends JToolBar implements Observer {


	private final JComboBox histogramChooser = new JComboBox(
			new HistogramComboBoxModel());

	private final JToggleButton boverLay = new JToggleButton(
			getHTML("<u>O</u>verlay"));

	private final JComboBox gateChooser = new JComboBox(new GateComboBoxModel());

	private final MessageHandler console;

	private final JamStatus status;

	private final Broadcaster broadcaster;

	private final Display display;
	
	/** Is sync event so don't */
	private boolean isSync=false;
	

	public SelectionToolbar() {
		super("Selection", JToolBar.HORIZONTAL);
		this.setFloatable(false);
		final int chooserWidth = 200;

		//Global objects
		status = JamStatus.instance();
		broadcaster = Broadcaster.getSingletonInstance();
		console = status.getMessageHandler();
		display = status.getDisplay();
		broadcaster.addObserver(this);
		final DefaultComboBoxModel noGateComboBoxModel = new DefaultComboBoxModel();
		noGateComboBoxModel.addElement("NO GATES");
		JPanel pCenter = new JPanel();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		pCenter.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

		//Histogram Chooser
		histogramChooser.setRenderer(new HistogramListCellRenderer());
		histogramChooser.setMaximumRowCount(30);
		histogramChooser.setSelectedIndex(0);
		Dimension dim = histogramChooser.getPreferredSize();
		dim.width = chooserWidth;
		histogramChooser.setPreferredSize(dim);
		histogramChooser
				.setToolTipText(getHTML("<u>D</u>isplay chosen histogram."));
		histogramChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final Object item = ((JComboBox) ae.getSource())
						.getSelectedItem();
				if (item instanceof Histogram) {
					final Histogram h = (Histogram) item;
					selectHistogram(h);
				} else {
					selectHistogram(null);
				}
			}
		});
		pCenter.add(histogramChooser);
		//Overlay button
		boverLay.setToolTipText("Overlay next histogram choice.");
		boverLay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (isOverlaySelected()) {
					console.messageOut("Overlay Spectrum ", MessageHandler.NEW);
				}
			}
		});
		pCenter.add(boverLay);
		//Gate Chooser
		gateChooser.setRenderer(new GateListCellRenderer());
		dim = gateChooser.getPreferredSize();
		dim.width = chooserWidth;
		gateChooser.setPreferredSize(dim);
		gateChooser.setToolTipText("Display chosen gate.");
		gateChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final Object item = ((JComboBox) ae.getSource())
						.getSelectedItem();
				if (item instanceof Gate) {
					final Gate gate = (Gate) item;
					if (gate != null && gate.isDefined()) {
						selectGate(gate);
					}
				}
			}
		});
		pCenter.add(gateChooser);
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
	 * Should be called whenever the lists of gates and histograms change. It
	 * calls histogramsChanged() and gatesChanged(), each of which add to the
	 * event stack, so that histograms will be guaranteed (?) updated before
	 * gates get updated.
	 */
	private void dataChanged() {
		histogramsChanged();
		gatesChanged();
	}

	private void setOverlayEnabled(boolean state) {
		synchronized (boverLay) {
			boverLay.setEnabled(state);
			if (!state){//disabled
				setOverlaySelected(false);
			}
		}
	}
	
	private void setOverlaySelected(boolean state){
		synchronized (boverLay){
			boverLay.setSelected(state);
		}
	}

	/**
	 * @return whether histogram overlay mode is enabled
	 */
	private boolean isOverlaySelected() {
		synchronized (boverLay) {
			return boverLay.isSelected();
		}
	}
	
	

	private String getHTML(String body) {
		final StringBuffer rval = new StringBuffer("<html><body>").append(body)
				.append("</html></body>");
		return rval.toString();
	}

	/**
	 * Selects first items in histogram and gate choosers. Default priveleges
	 * allows JamCommand to call this as well.
	 */
	public void setChoosersToFirstItems() {
		histogramChooser.setSelectedIndex(0);
		gateChooser.setSelectedIndex(0);
	}

	/**
	 * A histogram has been selected so tell all applicable classes about it.
	 * 
	 * @param hist
	 *            The histogram to be selected and displayed
	 */
	private void selectHistogram(Histogram hist) {
		if (isSync){
			return;
		}
		if (hist == null) { 
			broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, null);
		} else {
			final boolean oneD = hist.getDimensionality() == 1;
			if ((isOverlaySelected() && oneD) &&
			    (display.getHistogram()!=null)){
					status.setOverlayHistogramName(hist.getName());
					console.messageOut(hist.getName(), MessageHandler.END);
					display.overlayHistogram(hist.getNumber());
			} else {
				synchronized (status) {
					status.setHistName(hist.getName());					
					gatesChanged();
					setOverlaySelected(false);
					setOverlayEnabled(oneD);
					broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
				}
			}
		}
	}

	private void syncHistChooser() {
		isSync=true;
		final Histogram hist = Histogram.getHistogram(status
				.getHistName());
		if (hist != null) {
			histogramChooser.setSelectedItem(hist);
			gatesChanged();
		}
		isSync=false;
	}

	/**
	 * A gate has been selected. Tell all appropriate classes, like Display and
	 * JamStatus.
	 * 
	 * @param gateObject
	 *            the object, which should be a <code>Gate</code>
	 * @see jam.data.Gate
	 */
	private void selectGate(Gate gate) {
		final String methodname = "selectGate(): ";
		try {
			status.setCurrentGateName(gate.getName());
			broadcaster.broadcast(BroadcastEvent.Command.GATE_SELECT, gate);
			final double area = gate.getArea();
			if (gate.getDimensionality() == 1) {
				final double centroid = (double) ((int) (gate.getCentroid() * 100.0)) / 100.0;
				final int lowerLimit = gate.getLimits1d()[0];
				final int upperLimit = gate.getLimits1d()[1];
				console.messageOut("Gate: " + gate.getName() + ", Ch. "
						+ lowerLimit + " to " + upperLimit, MessageHandler.NEW);
				console.messageOut("  Area = " + area + ", Centroid = "
						+ centroid, MessageHandler.END);
			} else {
				console
						.messageOut("Gate " + gate.getName(),
								MessageHandler.NEW);
				console.messageOut(", Area = " + area, MessageHandler.END);
			}
		} catch (Exception de) {
			String classname = getClass().getName() + "--";
			console.errorOutln(classname + methodname + de.getMessage());
		}
	}

	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            the sender
	 * @param o
	 *            the message
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final BroadcastEvent.Command command = be.getCommand();
		if (command == BroadcastEvent.Command.HISTOGRAM_NEW) {
			final String lastHistName = status.getHistName();
			selectHistogram(Histogram.getHistogram(lastHistName));
			dataChanged();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_ADD) {
			dataChanged();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_SELECT) {
			syncHistChooser();			
		} else if (command == BroadcastEvent.Command.GATE_ADD) {
			final String lastHistName = status.getHistName();
			selectHistogram(Histogram.getHistogram(lastHistName));
			gatesChanged();
		} else if (command == BroadcastEvent.Command.GATE_SET_SAVE
				|| command == BroadcastEvent.Command.GATE_SET_OFF) {
			gateChooser.repaint();
			histogramChooser.repaint();
		} else if (command==BroadcastEvent.Command.OVERLAY_OFF){
			setOverlaySelected(false);
		}
	}

}