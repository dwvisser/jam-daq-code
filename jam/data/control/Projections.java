package jam.data.control;

import jam.data.DataException;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;
import jam.ui.HistogramComboBoxModel;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Class for projecting 2-D histograms.
 * 
 * @author Dale Visser, Ken Swartz
 */
public class Projections extends AbstractManipulation implements Observer {

	private static final String FULL = "Full Histogram";

	private static final String BETWEEN = "Between Channels";

	private final MessageHandler console;

	private final JComboBox cfrom, cto, cchan;

	private final JCheckBox cacross, cdown;

	private final JTextField tlim1, tlim2, ttextto;

	private final JLabel lname, lChannels, lAnd;
	
	private String hfromname;
	
	private Histogram hto;

	/**
	 * Constructs a new projections dialog.
	 * 
	 * @param msgHandler where to print messages
	 */
	public Projections(MessageHandler msgHandler) {
		super("Project 2D Histogram", false);
		console = msgHandler;
		setResizable(false);
	
		Dimension dim;
		final int hgap = 5;
		final int vgap = 10;
		int meanCharWidth;
		
		final Container cdproject = getContentPane();
		cdproject.setLayout(new BorderLayout(hgap, vgap));
		setLocation(20, 50);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				doSetup();
			}
		});
		JPanel pLabels = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pLabels.setBorder(new EmptyBorder(20, 10, 0, 0));
		cdproject.add(pLabels, BorderLayout.WEST);
		pLabels.add(new JLabel("Project histogram", JLabel.RIGHT));
		pLabels.add(new JLabel("Direction", JLabel.RIGHT));
		pLabels.add(new JLabel("Region", JLabel.RIGHT));
		pLabels.add(new JLabel("To histogram", JLabel.RIGHT));
		/* Entries Panel */
		final JPanel pEntries = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pEntries.setBorder(new EmptyBorder(20, 0, 0, 10));
		cdproject.add(pEntries, BorderLayout.CENTER);
		/* From histogram */
		final JPanel phist = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		cfrom = new JComboBox(new HistogramComboBoxModel(
				HistogramComboBoxModel.Mode.TWO_D));
		meanCharWidth= getMeanCharWidth(cfrom.getFontMetrics(cfrom.getFont()));
		dim = cfrom.getPreferredSize();
		dim.width = CHOOSER_CHAR_LENGTH*meanCharWidth;				
		cfrom.setPreferredSize(dim);
		cfrom.setEditable(false);
		phist.add(cfrom);
		pEntries.add(phist);
		/* Direction panel */
		final JPanel pradio = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		final ButtonGroup cbg = new ButtonGroup();
		cacross = new JCheckBox("Across", true);
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
		cchan = new JComboBox();
		
		dim = cchan.getPreferredSize();
		dim.width = CHOOSER_SIZE;
		cchan.setPreferredSize(dim);
		cchan.addItem(FULL);
		cchan.addItem(BETWEEN);
		cchan.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if (cchan.getSelectedItem() != null) {
					setUseLimits(cchan.getSelectedItem().equals(BETWEEN));
				}
			}
		});
		pchannel.add(cchan);
		setUseLimits(false);
		lChannels = new JLabel("Channels");
		pchannel.add(lChannels);
		pchannel.add(tlim1);
		lAnd = new JLabel("and");
		pchannel.add(lAnd);
		pchannel.add(tlim2);
		pEntries.add(pchannel);
		/* To histogram */
		final JPanel ptextto = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		cto = new JComboBox();
		meanCharWidth= getMeanCharWidth(cfrom.getFontMetrics(cfrom.getFont()));
		dim = cto.getPreferredSize();
		dim.width = CHOOSER_CHAR_LENGTH*meanCharWidth;				
		cto.setPreferredSize(dim);
		cto.addItem("1DHISTOGRAM");
		cto.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if (cto.getSelectedItem() != null) {
					setUseHist((String)cto.getSelectedItem());
				}
			}
		});
		ptextto.add(cto);
		lname = new JLabel("Name");
		ptextto.add(lname);
		ttextto = new JTextField("projection", NEW_NAME_LENGTH);
		setUseHist(NEW_HIST);
		ptextto.add(ttextto);
		pEntries.add(ptextto);
		final PanelOKApplyCancelButtons.Listener listener = 
		    new PanelOKApplyCancelButtons.DefaultListener(this){
			public void apply(){
				try {
					project();
					BROADCASTER.broadcast(BroadcastEvent.Command.REFRESH);
					STATUS.setCurrentHistogram(hto);
					BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hto );
				} catch (DataException de) {
					console.errorOutln(de.getMessage());
				}
			}
		};
		final PanelOKApplyCancelButtons buttons = new PanelOKApplyCancelButtons(listener);
		cdproject.add(buttons.getComponent(), BorderLayout.SOUTH);		
		cfrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				final Object selected = cfrom.getSelectedItem();
				if (selected == null || selected instanceof String) {
					hfromname = "";
					buttons.setButtonsEnabled(false,false,true);
				} else {
					hfromname = ((Histogram) selected).getFullName();
					buttons.setButtonsEnabled(true,true,true);
					setupCuts(FULL);
				}
			}
		});
		cfrom.setSelectedIndex(0);
		pack();
	}
	

	/**
	 * Implementation of Observable interface Listners for broadcast events.
	 * Broadcast events: histograms new and histogram added
	 */
	public void update(Observable observable, Object object) {
		final BroadcastEvent event = (BroadcastEvent) object;
		final BroadcastEvent.Command com = event.getCommand();
		if (com == BroadcastEvent.Command.HISTOGRAM_NEW) {
			doSetup();
		} else if (com == BroadcastEvent.Command.HISTOGRAM_ADD || 
				com == BroadcastEvent.Command.GATE_ADD ||
				com == BroadcastEvent.Command.GATE_SET_OFF ||
				com == BroadcastEvent.Command.GATE_SET_SAVE) {
			doSetup();
		} 
	}

	/**
	 * Loads the name of the current histogram and the list of gates for that
	 * histogram. For a 1d histogram set co-ordinates as x y if 2d or lower and
	 * upper if 1 d
	 *  
	 */
	public void doSetup() {
		cfrom.setSelectedIndex(0);
		setUseHist(NEW_HIST); //default use new histogram
		loadAllHists(cto, true, Histogram.Type.ONE_D);//setup "to" histogram
		final String lastCut = (String) cchan.getSelectedItem();
		setupCuts(lastCut);//default setup channels
	}
	

	/* non-javadoc:
	 * Setups up the channel and gate selector.
	 *  
	 */
	private void setupCuts(String newSelect) {
		cchan.removeAllItems();
		/* add default options */
		cchan.addItem(FULL);
		cchan.addItem(BETWEEN);
		/* add gates to chooser */
		final Histogram hfrom=Histogram.getHistogram(hfromname);
		if (hfrom != null) {
			final Iterator iterator = hfrom.getGates().iterator();
			while (iterator.hasNext()) {
				final Gate gate = (Gate) iterator.next();
				if (gate.isDefined()){
					cchan.addItem(gate.getName());
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

	/* non-javadoc:
	 * setup if using a new histogram
	 */
	private void setUseHist(String name) {
		if (isNewHistogram(name)){
			lname.setEnabled(true);
			ttextto.setEnabled(true);
			ttextto.setEditable(true);
		} else {
			lname.setEnabled(false);
			ttextto.setEnabled(true);
			ttextto.setEditable(false);
			
		}
	}

	/* non-javadoc:
	 * setup if using limits
	 */
	private void setUseLimits(boolean state) {
		tlim1.setEnabled(state);
		tlim2.setEnabled(state);
		tlim1.setEditable(state);
		tlim2.setEditable(state);
	}

	/* non-javadoc:
	 * Does the work of projecting a histogram
	 */
	private void project() throws DataException {
		final String typeProj;
		double [] countsDouble=null;
		final double[][] counts2d;
		final String state = (String) cchan.getSelectedItem();
		final Histogram hfrom=Histogram.getHistogram(hfromname);
		counts2d = (hfrom.getType() == Histogram.Type.TWO_D_DOUBLE) ? (double[][]) hfrom
                .getCounts()
                : intToDouble2DArray((int[][]) hfrom.getCounts());
        final String name = (String) cto.getSelectedItem();
		final boolean between = state.equals(BETWEEN);
		final int[] limits = between ? getLimits() : new int[2];
        if (state.equals(FULL)) {
            limits[0] = 0;
            if (cdown.isSelected()) {
                limits[1] = counts2d[0].length - 1;
            } else {
                limits[1] = counts2d.length - 1;
            }
        }
        if (isNewHistogram(name)) {
			final String histName = ttextto.getText().trim();
			final String groupName = parseGroupName(name);
			final int size=cdown.isSelected() ? hfrom.getSizeX() : hfrom.getSizeY();
			hto = createNewHistogram(name, histName, size);
			console
			.messageOutln("New Histogram created: '" + groupName+"/"+histName + "'");
		} else {
			hto = Histogram.getHistogram(name);
		}
		if (cdown.isSelected()) {
			if (state.equals(FULL) || state.equals(BETWEEN)) {
				typeProj = "counts between Y channels " + limits[0] + " and "
						+ limits[1];
				countsDouble= projectX(counts2d, hto.getSizeX(), limits[0],
						limits[1]);
			} else {
				typeProj = "using gate " + state;
				countsDouble= projectX(counts2d, hto.getSizeX(), Gate
						.getGate(state));
			}
		} else { // cacross is true
			if (state.equals(FULL) || state.equals(BETWEEN)) {
				typeProj = "counts between X channels " + limits[0] + " and "
						+ limits[1];
				countsDouble = projectY(counts2d, hto.getSizeX(), limits[0],
						limits[1]);
				
			} else {
				typeProj = "using gate " + state.trim();
				countsDouble= projectY(counts2d, hto.getSizeX(), Gate
						.getGate(state));
			}
		}
		if(hto.getType() ==Histogram.Type.ONE_D_DOUBLE) {
			hto.setCounts(countsDouble);
		} else if (hto.getType() ==Histogram.Type.ONE_DIM_INT) {
			hto.setCounts(doubleToIntArray(countsDouble));
		} else {
			throw new DataException(
			"Need to project to 1 dimension histogram");
		}
		console.messageOutln("Project " + hfrom.getFullName().trim()
				+ " to " + hto.getFullName() + " " + typeProj);
	}


	double[] projectX(double[][] inArray, int outLength, int _ll, int _ul) {
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

	double[] projectY(double[][] inArray, int outLength, int _ll, int _ul) {
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

	double[] projectX(double[][] inArray, int outLength, Gate gate) {
		double[] out = new double[outLength];
		for (int k = 0; k < outLength; k++) {
			out[k] = 0;
		}
		for (int i = 0; i < inArray.length; i++) {
			for (int j = 0; j < inArray[0].length; j++) {
				if (gate.inGate(i, j)) {
					if (i < out.length){
						out[i] += inArray[i][j];
					}
				}
			}
		}
		return out;
	}

	double[] projectY(double[][] inArray, int outLength, Gate gate) {
		double[] out = new double[outLength];
		for (int k = 0; k < outLength; k++) {
			out[k] = 0;
		}
		for (int i = 0; i < inArray.length; i++) {
			for (int j = 0; j < inArray[0].length; j++) {
				if (gate.inGate(i, j)) {
					if (j < out.length){
						out[j] += inArray[i][j];
					}
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
			throw new DataException(
					"Invalid channel not a valid numbers");
		}
		return out;
	}	
}

