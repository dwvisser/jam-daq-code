package jam.data.control;

import jam.data.DataException;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Class for projecting 2-D histograms.
 * 
 * @author Dale Visser
 */
public class Projections extends AbstractControl implements Observer {

	private static final String FULL = "Full Histogram";

	private static final String BETWEEN = "Between Channels";

	private final String NEW_HIST = "NEW: ";
	
	private final String HIST_WILD_CARD="/.";

	private final Broadcaster broadcaster;

	private final MessageHandler messageHandler;

	private final JComboBox cfrom, cto, cchan;

	private final JCheckBox cacross, cdown;

	private final JTextField tlim1, tlim2, ttextto;

	private String hfromname;
	
	private Histogram hto;

	/**
	 * Constructs a new projections dialog.
	 * 
	 * @param mh where to print messages
	 */
	public Projections(MessageHandler mh) {
		super("Project 2D Histogram", false);
		messageHandler = mh;
		broadcaster = Broadcaster.getSingletonInstance();
		setResizable(false);
		final int CHOOSER_SIZE = 200;
		Dimension dim;
		final int hgap = 5;
		final int vgap = 10;
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
		dim = cfrom.getPreferredSize();
		dim.width = CHOOSER_SIZE;
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
		final JComponent channels = new JLabel("Channels");
		pchannel.add(channels);
		pchannel.add(tlim1);
		final JComponent and = new JLabel("and");
		pchannel.add(and);
		pchannel.add(tlim2);
		pEntries.add(pchannel);
		/* To histogram */
		final JPanel ptextto = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		cto = new JComboBox();
		dim = cto.getPreferredSize();
		dim.width = CHOOSER_SIZE;
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
		final JComponent lname = new JLabel("Name");
		ptextto.add(lname);
		ttextto = new JTextField("projection", 20);
		setUseHist(NEW_HIST);
		ptextto.add(ttextto);
		pEntries.add(ptextto);
		final PanelOKApplyCancelButtons.Listener listener = new PanelOKApplyCancelButtons.Listener(){
			public void ok(){
				apply();
				dispose();
			}
			
			public void apply(){
				try {
					project();
					BROADCASTER.broadcast(BroadcastEvent.Command.REFRESH);
					STATUS.setCurrentHistogram(hto);
					BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hto );
				} catch (DataException de) {
					messageHandler.errorOutln(de.getMessage());
				}
			}
			
			public void cancel(){
				dispose();
			}
		};
		final PanelOKApplyCancelButtons buttons = new PanelOKApplyCancelButtons(listener);
		cdproject.add(buttons.getComponent(), BorderLayout.SOUTH);		
		cfrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selected = cfrom.getSelectedItem();
				if (selected == null || selected instanceof String) {
					hfromname = null;
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
	public void update(Observable observable, Object o) {
		BroadcastEvent be = (BroadcastEvent) o;
		final BroadcastEvent.Command com = be.getCommand();
		if (com == BroadcastEvent.Command.HISTOGRAM_NEW) {
			doSetup();
		} else if (com == BroadcastEvent.Command.HISTOGRAM_ADD || 
				com == BroadcastEvent.Command.GATE_ADD ||
				com == BroadcastEvent.Command.GATE_SET_OFF ||
				com == BroadcastEvent.Command.GATE_SET_SAVE) {
			setupAdd();
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
		setupToHist();//setup "to" histogram
		setupCuts(FULL);//default setup channels
	}

	/**
	 * a new histogram or gate has been added to the database
	 */
	private void setupAdd() {
		final String lastCut = (String) cchan.getSelectedItem();
		/* setup to histogram */
		setupToHist();
		/* setup channels */
		setupCuts(lastCut);
	}

	/* non-javadoc:
	 * Adds a list of histograms to a choose
	 */
	private void setupToHist() {
		cto.removeAllItems();
		/* Add working group new */
		cto.addItem(NEW_HIST+Group.WORKING_NAME+"/.");
		//Add new histograms
		for (Iterator iter = Group.getGroupList().iterator();iter.hasNext();) {
			Group group = (Group)iter.next();
			if (group.getType() != Group.Type.SORT &&
				!Group.WORKING_NAME.equals(group.getName())	) {
				cto.addItem(NEW_HIST+group.getName()+"/.");
			}
		}
		/* Add Existing hisograms */
		for (Iterator grpiter = Group.getGroupList().iterator(); grpiter.hasNext();) {
			Group group = (Group)grpiter.next();
			for  (Iterator histiter = group.getHistogramList().iterator(); histiter.hasNext();) {
				Histogram hist =(Histogram)histiter.next();
				if (hist.getType() == Histogram.Type.ONE_D_DOUBLE) {
					cto.addItem(hist.getFullName());
				}
			}
		}

		cto.setSelectedIndex(0);
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
			final Iterator it = hfrom.getGates().iterator();
			while (it.hasNext()) {
				final Gate gate = (Gate) it.next();
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
			ttextto.setEnabled(true);
			ttextto.setEditable(true);
		} else {
			ttextto.setEnabled(false);
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
		final String state = (String) cchan.getSelectedItem();
		final double[][] counts2d;
		final Histogram hfrom=Histogram.getHistogram(hfromname);
		if (hfrom.getType() == Histogram.Type.TWO_D_DOUBLE) {
			counts2d = (double[][]) hfrom.getCounts();
		} else {
			counts2d = intToDouble((int[][]) hfrom.getCounts());
		}
		String name = (String) cto.getSelectedItem();
		final int[] limits;
		if (state.equals(BETWEEN)) {
			limits = getLimits();
		} else {
			limits = new int[2];
			if (state.equals(FULL)) {
				limits[0] = 0;
				if (cdown.isSelected()) {
					limits[1] = counts2d[0].length - 1;
				} else {
					limits[1] = counts2d.length - 1;
				}
			}
		}

		if (isNewHistogram(name)) {
			String histName = ttextto.getText().trim();
			final int size=cdown.isSelected() ? hfrom.getSizeX() : hfrom.getSizeY();
			/*if (cdown.isSelected()) {//project down
				hto = new Histogram(name, Histogram.Type.ONE_D_DOUBLE, hfrom
						.getSizeX(), name);
			} else {//project across
				hto = new Histogram(name, Histogram.Type.ONE_D_DOUBLE, hfrom
						.getSizeY(), name);
			}*/

			String groupName = parseGroupName(name);
			if (groupName.equals(Group.WORKING_NAME))
			{
				Group.createGroup(groupName, Group.Type.FILE);
			}
			hto = Histogram.createHistogram(new double[size],histName);
			broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
			messageHandler
					.messageOutln("New Histogram created: '" + groupName+"/"+histName + "'");
		} else {
			hto = Histogram.getHistogram(name);

		}
		final String typeProj;
		if (cdown.isSelected()) {
			if (state.equals(FULL) || state.equals(BETWEEN)) {
				typeProj = "counts between Y channels " + limits[0] + " and "
						+ limits[1];
				hto.setCounts(projectX(counts2d, hto.getSizeX(), limits[0],
						limits[1]));
			} else {
				typeProj = "using gate " + state;
				hto.setCounts(projectX(counts2d, hto.getSizeX(), Gate
						.getGate(state)));
			}
		} else { // cacross is true
			if (state.equals(FULL) || state.equals(BETWEEN)) {
				typeProj = "counts between X channels " + limits[0] + " and "
						+ limits[1];
				hto.setCounts(projectY(counts2d, hto.getSizeX(), limits[0],
						limits[1]));
			} else {
				typeProj = "using gate " + state.trim();
				hto.setCounts(projectY(counts2d, hto.getSizeX(), Gate
						.getGate(state)));
			}
		}
		messageHandler.messageOutln("Project " + hfrom.getFullName().trim()
				+ " to " + hto.getFullName() + " " + typeProj);
	}

	private double[][] intToDouble(int[][] in) {
		double[][] rval = new double[in.length][in[0].length];
		for (int i = 0; i < in.length; i++) {
			for (int j = 0; j < in[0].length; j++) {
				rval[i][j] = in[i][j];
			}
		}
		return rval;
	}

	double[] projectX(double[][] inArray, int outLength, int _ll, int _ul) {
		final double[] out = new double[outLength];
		int ll = Math.max(0, _ll);
		int ul = Math.min(inArray[0].length - 1, _ul);
		final int xul = Math.min(inArray.length, outLength);
		for (int i = 0; i < xul; i++) {
			for (int j = ll; j <= ul; j++) {
				out[i] += inArray[i][j];
			}
		}
		return out;
	}

	double[] projectY(double[][] inArray, int outLength, int _ll, int _ul) {
		double[] out = new double[outLength];
		int ll = Math.max(0, _ll);
		int ul = Math.min(inArray.length - 1, _ul);
		final int yul = Math.min(inArray[0].length, outLength);
		for (int i = ll; i <= ul; i++) {
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
					if (i < out.length)
						out[i] += inArray[i][j];
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
					if (j < out.length)
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
				int temp = out[0];
				out[0] = out[1];
				out[1] = temp;
			}
		} catch (NumberFormatException ne) {
			throw new DataException(
					"Invalid channel not a valid number [Projections]");
		}
		return out;
	}
	
	private boolean isNewHistogram(String name){
		return name.startsWith(NEW_HIST);
	}
	private String parseGroupName(String name){
		
		StringBuffer sb = new StringBuffer(name);
		String groupName=sb.substring(NEW_HIST.length(), name.length()-HIST_WILD_CARD.length());
		return groupName;

	}
	
}