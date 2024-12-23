package jam.data.control;

import static javax.swing.SwingConstants.RIGHT;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

import jam.data.AbstractHistogram;
import jam.data.DataElement;
import jam.data.DataException;
import jam.data.DataUtility;
import jam.data.Gate;
import jam.data.HistDouble2D;
import jam.data.HistInt2D;
import jam.data.HistogramType;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.ui.PanelOKApplyCancelButtons;
import jam.ui.SelectionTree;
import jam.util.NumberUtilities;

/**
 * Class for projecting 2-D histograms.
 * 
 * @author Dale Visser, Ken Swartz
 */
public final class Projections extends AbstractManipulation {

	private static final int CHOOSER_SIZE = 200;

	private static final String FULL = "Full Histogram";

	private static final String BETWEEN = "Between Channels";

	private transient final JComboBox<Object> cfrom, cto, cchan;

	private transient final JCheckBox cdown;

	private transient final JTextField tlim1, tlim2, ttextto;

	private transient final JLabel lname;

	private transient String hfromname;

	private transient AbstractHistogram hto;

	private transient final NumberUtilities numberUtilities;

	/**
	 * Constructs a new projections dialog.
	 * 
	 * @param frame
	 *            application frame
	 * 
	 * @param status
	 *            application status
	 * @param broadcaster
	 *            broadcasts state changes
	 * @param numberUtilities
	 *            number utility object
	 */
	@Inject
	public Projections(final Frame frame, final JamStatus status,
			final Broadcaster broadcaster, final NumberUtilities numberUtilities) {
		super(frame, "Project 2D Histogram", false, broadcaster);
		this.numberUtilities = numberUtilities;
		setResizable(false);
		final Container cdproject = getContentPane();
		int hgap = 5;
		int vgap = 10;
		cdproject.setLayout(new BorderLayout(hgap, vgap));
		setLocation(20, 50);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowOpenListener();
		final JPanel pLabels = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pLabels.setBorder(new EmptyBorder(20, 10, 0, 0));
		cdproject.add(pLabels, BorderLayout.WEST);
		pLabels.add(new JLabel("Project histogram", RIGHT));
		pLabels.add(new JLabel("Direction", RIGHT));
		pLabels.add(new JLabel("Region", RIGHT));
		pLabels.add(new JLabel("To histogram", RIGHT));
		/* Entries Panel */
		final JPanel pEntries = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pEntries.setBorder(new EmptyBorder(20, 0, 0, 10));
		cdproject.add(pEntries, BorderLayout.CENTER);
		/* From histogram */
		final JPanel phist = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		cfrom = new JComboBox<>(new HistogramComboBoxModel(
				HistogramComboBoxModel.Mode.TWO_D));
		int meanWidth = getMeanCharWidth(cfrom.getFontMetrics(cfrom.getFont()));
		Dimension dim = cfrom.getPreferredSize();
		dim.width = CHAR_LENGTH * meanWidth;
		cfrom.setPreferredSize(dim);
		cfrom.setEditable(false);
		phist.add(cfrom);
		pEntries.add(phist);
		/* Direction panel */
		final JPanel pradio = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		final ButtonGroup cbg = new ButtonGroup();
		final JCheckBox cacross = new JCheckBox("Across", true);
		cdown = new JCheckBox("Down", false);
		cbg.add(cacross);
		cbg.add(cdown);
		pradio.add(cacross);
		pradio.add(cdown);
		pEntries.add(pradio);
		/* Channels panel */
		final JPanel pchannel = new JPanel(
				new FlowLayout(FlowLayout.LEFT, 5, 0));
		tlim1 = new JTextField(5);
		tlim2 = new JTextField(5);
		cchan = new JComboBox<>();
		dim = cchan.getPreferredSize();
		dim.width = CHOOSER_SIZE;
		cchan.setPreferredSize(dim);
		cchan.addItem(FULL);
		cchan.addItem(BETWEEN);
		cchan.addItemListener(new ChannelListener());
		pchannel.add(cchan);
		setUseLimits(false);
		JLabel lChannels = new JLabel("Channels");
		pchannel.add(lChannels);
		pchannel.add(tlim1);
		JLabel lAnd = new JLabel("and");
		pchannel.add(lAnd);
		pchannel.add(tlim2);
		pEntries.add(pchannel);
		/* To histogram */
		final JPanel ptextto = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		cto = new JComboBox<>();
		meanWidth = getMeanCharWidth(cfrom.getFontMetrics(cfrom.getFont()));
		dim = cto.getPreferredSize();
		dim.width = CHAR_LENGTH * meanWidth;
		cto.setPreferredSize(dim);
		cto.addItem("1DHISTOGRAM");
		cto.addItemListener(itemEvent -> {
            if (cto.getSelectedItem() != null) {
                setUseHist((String) cto.getSelectedItem());
            }
        });
		ptextto.add(cto);
		lname = new JLabel("Name");
		ptextto.add(lname);
		ttextto = new JTextField("projection", TEXT_LENGTH);
		setUseHist(NEW_HIST);
		ptextto.add(ttextto);
		pEntries.add(ptextto);
		final PanelOKApplyCancelButtons.Listener listener = new PanelOKApplyCancelButtons.AbstractListener(
				this) {
			public void apply() {
				try {
					project();
					broadcaster.broadcast(BroadcastEvent.Command.REFRESH);
					SelectionTree.setCurrentHistogram(hto);
					status.setCurrentGroup(DataUtility.getGroup(hto));
					broadcaster.broadcast(
							BroadcastEvent.Command.HISTOGRAM_SELECT, hto);
				} catch (DataException de) {
					LOGGER.log(Level.SEVERE, de.getMessage(), de);
				}
			}
		};
		final PanelOKApplyCancelButtons buttons = new PanelOKApplyCancelButtons(
				listener);
		cdproject.add(buttons.getComponent(), BorderLayout.SOUTH);
		cfrom.addActionListener(actionEvent -> {
            final Object selected = cfrom.getSelectedItem();
            if (selected == null || selected instanceof String) {
                hfromname = "";
                buttons.setButtonsEnabled(false, false, true);
            } else {
                hfromname = ((AbstractHistogram) selected).getFullName();
                buttons.setButtonsEnabled(true, true, true);
                setupCuts(FULL);
            }
        });
		cfrom.setSelectedIndex(0);
		pack();
	}

	private class ChannelListener implements ItemListener {

		ChannelListener() {
			super();
		}

		public void itemStateChanged(final ItemEvent itemEvent) {
			if (cchan.getSelectedItem() != null) {
				setUseLimits(cchan.getSelectedItem().equals(BETWEEN));
			}
		}
	}

    /**
	 * 
	 */
	private void addWindowOpenListener() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(final WindowEvent windowEvent) {
				doSetup();
			}
		});
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final BroadcastEvent.Command com = ((BroadcastEvent) evt).getCommand();
		if (com == BroadcastEvent.Command.HISTOGRAM_NEW
				|| com == BroadcastEvent.Command.HISTOGRAM_ADD
				|| com == BroadcastEvent.Command.GATE_ADD
				|| com == BroadcastEvent.Command.GATE_SET_OFF
				|| com == BroadcastEvent.Command.GATE_SET_SAVE) {
			doSetup();
		}
	}

	/**
	 * Loads the name of the current histogram and the list of gates for that
	 * histogram. For a 1d histogram set co-ordinates as x y if 2d or lower and
	 * upper if 1 d
	 * 
	 */
	@Override
	public void doSetup() {
		cfrom.setSelectedIndex(0);
		setUseHist(NEW_HIST); // default use new histogram
		loadAllHists(cto, true, HistogramType.ONE_D);// setup "to" histogram
		final String lastCut = (String) cchan.getSelectedItem();
		setupCuts(lastCut);// default setup channels
	}

	/*
	 * non-javadoc: Setups up the channel and gate selector.
	 */
	private void setupCuts(final String newSelect) {
		cchan.removeAllItems();
		/* add default options */
		cchan.addItem(FULL);
		cchan.addItem(BETWEEN);
		/* add gates to chooser */
		final AbstractHistogram hfrom = AbstractHistogram
				.getHistogram(hfromname);
		if (hfrom != null) {
			for (DataElement gate : hfrom.getGateCollection().getGates()) {
				if (((Gate) gate).isDefined()) {
					cchan.addItem(gate);
				}
			}
		}
		cchan.setSelectedItem(newSelect);
		if (newSelect.equals(BETWEEN)) {
			setUseLimits(true);
		} else {
			setUseLimits(false);
		}
	}

	/*
	 * non-javadoc: setup if using a new histogram
	 */
	private void setUseHist(final String name) {
		if (isNewHistogram(name)) {
			lname.setEnabled(true);
			ttextto.setEnabled(true);
			ttextto.setEditable(true);
		} else {
			lname.setEnabled(false);
			ttextto.setEnabled(true);
			ttextto.setEditable(false);

		}
	}

	/*
	 * non-javadoc: setup if using limits
	 */
	private void setUseLimits(final boolean state) {
		tlim1.setEnabled(state);
		tlim2.setEnabled(state);
		tlim1.setEditable(state);
		tlim2.setEditable(state);
	}

	/*
	 * non-javadoc: Does the work of projecting a histogram
	 */
	private void project() throws DataException {
		double[][] counts2d;
		final AbstractHistogram hfrom = AbstractHistogram
				.getHistogram(hfromname);
		counts2d = (hfrom.getType() == HistogramType.TWO_D_DOUBLE) ? ((HistDouble2D) hfrom)
				.getCounts() : this.numberUtilities
				.intToDouble2DArray(((HistInt2D) hfrom).getCounts());
		final String name = (String) cto.getSelectedItem();
		final Object selected = cchan.getSelectedItem();
		final boolean between = BETWEEN.equals(selected);
		final boolean full = FULL.equals(selected);
		final int[] limits = setLimits(counts2d, between, full);
		getDestinationHistogram(hfrom, name);
		final boolean gateSelected = selected instanceof Gate;
		final Gate gate = gateSelected ? (Gate) selected : null; // NOPMD
		internalProject(counts2d, hfrom, limits, gateSelected, gate);
	}

	private void internalProject(final double[][] counts2d,
			final AbstractHistogram hfrom, final int[] limits,
			final boolean gateSelected, final Gate gate) throws DataException {
		double[] countsDouble;
		final StringBuffer typeProj = new StringBuffer();
		if (cdown.isSelected()) {
			countsDouble = calculateXprojection(counts2d, limits, gateSelected,
					gate, typeProj);
		} else { // cacross is true
			countsDouble = calculateYprojection(counts2d, limits, gateSelected,
					gate, typeProj);
		}
		setProjectionCounts(countsDouble);
		LOGGER.info("Project " + hfrom.getFullName().trim() + " to "
				+ hto.getFullName() + " " + typeProj);
	}

	private double[] calculateYprojection(final double[][] counts2d,
			final int[] limits, final boolean gateSelected, final Gate gate,
			final StringBuffer typeProj) {
		double[] countsDouble;
		if (gateSelected) {
			typeProj.append("using gate ").append(gate.getName());
			countsDouble = projectY(counts2d, hto.getSizeX(), gate);
		} else {
			typeProj.append("counts between X channels ").append(limits[0])
					.append(" and ").append(limits[1]);
			countsDouble = projectY(counts2d, hto.getSizeX(), limits[0],
					limits[1]);
		}
		return countsDouble;
	}

	private double[] calculateXprojection(final double[][] counts2d,
			final int[] limits, final boolean gateSelected, final Gate gate,
			final StringBuffer typeProj) {
		double[] countsDouble;
		if (gateSelected) {
			typeProj.append("using gate ").append(gate.getName());
			countsDouble = projectX(counts2d, hto.getSizeX(), gate);
		} else {
			typeProj.append("counts between Y channels ").append(limits[0])
					.append(" and ").append(limits[1]);
			countsDouble = projectX(counts2d, hto.getSizeX(), limits[0],
					limits[1]);
		}
		return countsDouble;
	}

	private void setProjectionCounts(final double[] countsDouble)
			throws DataException {
		if (hto.getType() == HistogramType.ONE_D_DOUBLE) {
			hto.setCounts(countsDouble);
		} else if (hto.getType() == HistogramType.ONE_DIM_INT) {
			hto.setCounts(this.numberUtilities.doubleToIntArray(countsDouble));
		} else {
			throw new DataException("Need to project to 1 dimension histogram");
		}
	}

	private int[] setLimits(final double[][] counts2d, final boolean between,
			final boolean full) throws DataException {
		final int[] limits = between ? getLimits() : new int[2];
		if (full) {
			setLimitsFull(counts2d, limits);
		}
		return limits;
	}

	private void getDestinationHistogram(final AbstractHistogram hfrom,
			final String name) {
		if (isNewHistogram(name)) {
			final int size = cdown.isSelected() ? hfrom.getSizeX() : hfrom
					.getSizeY();
			final String histName = ttextto.getText().trim();
			final String groupName = parseGroupName(name);
			hto = createNewDoubleHistogram(groupName, histName, size);
			LOGGER.info("New Histogram created: '" + groupName + "/" + histName
					+ "'");
		} else {
			hto = AbstractHistogram.getHistogram(name);
		}
	}

	private void setLimitsFull(final double[][] counts2d, final int[] limits) {
		limits[0] = 0;
		if (cdown.isSelected()) {
			limits[1] = counts2d[0].length - 1;
		} else {
			limits[1] = counts2d.length - 1;
		}
	}

	protected double[] projectX(final double[][] inArray, final int outLength,
			final int _ll, final int _ul) {
		final double[] out = new double[outLength];
		final int lower = Math.max(0, _ll);
		final int upper = Math.min(inArray[0].length - 1, _ul);
		final int xul = Math.min(inArray.length, outLength);
		for (int i = 0; i < xul; i++) {
			for (int j = lower; j <= upper; j++) {
				out[i] += inArray[i][j];
			}
		}
		return out;
	}

	protected double[] projectY(final double[][] inArray, final int outLength,
			final int _ll, final int _ul) {
		double[] out = new double[outLength];
		final int lower = Math.max(0, _ll);
		final int upper = Math.min(inArray.length - 1, _ul);
		final int yul = Math.min(inArray[0].length, outLength);
		for (int i = lower; i <= upper; i++) {
			for (int j = 0; j < yul; j++) {
				out[j] += inArray[i][j];
			}
		}
		return out;
	}

	private double[] projectX(final double[][] inArray, final int outLength,
			final Gate gate) {
		double[] out = new double[outLength];
		for (int i = 0; i < inArray.length; i++) {
			for (int j = 0; j < inArray[0].length; j++) {
				if (gate.inGate(i, j) && i < out.length) {
					out[i] += inArray[i][j];
				}
			}
		}
		return out;
	}

	private double[] projectY(final double[][] inArray, final int outLength,
			final Gate gate) {
		double[] out = new double[outLength];
		for (int i = 0; i < inArray.length; i++) {
			for (int j = 0; j < inArray[0].length; j++) {
				if (gate.inGate(i, j) && j < out.length) {
					out[j] += inArray[i][j];
				}
			}
		}
		return out;
	}

	private int[] getLimits() throws DataException {
		int[] out = new int[2];
		try {
			out[0] = Integer.parseInt(tlim1.getText().trim());
			out[1] = Integer.parseInt(tlim2.getText().trim());
			if (out[0] > out[1]) {
				final int temp = out[0];
				out[0] = out[1];
				out[1] = temp;
			}
		} catch (NumberFormatException ne) {
			throw new DataException("Invalid channel not a valid number.", ne);
		}
		return out;
	}
}
