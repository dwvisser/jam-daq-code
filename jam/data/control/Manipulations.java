
package jam.data.control;

import jam.data.AbstractHist1D;
import jam.data.DataException;
import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
 * Class for gain adjusting and doing arithmetic with 1-D histograms.
 * 
 * @author Dale Visser
 */
public class Manipulations extends AbstractControl implements WindowListener,
        Observer {

	private static final String NEW_HIST = "NEW: ";
	
	private final MessageHandler messageHandler;

	private JComboBox cfrom1, cfrom2, cto;

	private JCheckBox cnorm, cplus, cminus, ctimes, cdiv;

	private JTextField ttextto, ttimes1, ttimes2;

	private JLabel lname, lWith;
	
	private AbstractHist1D hto;
	
	/**
	 * Construct a new "manipilate histograms" dialog.
	 * @param mh where to print messages
	 */
	public Manipulations(MessageHandler mh) {
		super("Manipulate 1-D Histograms", false);
		messageHandler = mh;
		setResizable(false);
		final int CHOOSER_SIZE = 200;
		Dimension dim;
		final int hgap = 5;
		final int vgap = 5;
		//UI
		final Container cdmanip = getContentPane();
		cdmanip.setLayout(new BorderLayout(hgap, vgap));
		setLocation(20, 50);
		addWindowListener(this);

		//Labels panel
		JPanel pLabels = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pLabels.setBorder(new EmptyBorder(10, 10, 0, 0));
		cdmanip.add(pLabels, BorderLayout.WEST);
		lWith = new JLabel("With histogram", JLabel.RIGHT);
		pLabels.add(new JLabel("From  histogram", JLabel.RIGHT));
		pLabels.add(new JLabel("Operation", JLabel.RIGHT));
		pLabels.add(lWith);
		pLabels.add(new JLabel("To histogram", JLabel.RIGHT));

		//Entries Panel
		JPanel pEntries = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pEntries.setBorder(new EmptyBorder(10, 0, 0, 10));
		cdmanip.add(pEntries, BorderLayout.CENTER);

		//From Panel
		JPanel pfrom1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pfrom1);
		cfrom1 = new JComboBox();
		dim = cfrom1.getPreferredSize();
		dim.width = CHOOSER_SIZE;
		cfrom1.setPreferredSize(dim);

		cfrom1.addItem("1DHISTOGRAM1");
		pfrom1.add(cfrom1);
		pfrom1.add(new JLabel("x"));
		ttimes1 = new JTextField("1.0", 8);
		pfrom1.add(ttimes1);

		//Operation Panel
		JPanel pradio = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pradio);
		ButtonGroup cbg = new ButtonGroup();
		
		cnorm = new JCheckBox("Renormalize", true);
		cnorm.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				enableInputWith(false);
			}
		});
		cbg.add(cnorm);
		pradio.add(cnorm);
		
		cplus = new JCheckBox("Add", false);
		cplus.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				enableInputWith(true);
			}
		});
		cbg.add(cplus);		
		pradio.add(cplus);

		cminus = new JCheckBox("Subtract", false);
		cminus.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				enableInputWith(true);
			}
		});
		cbg.add(cminus);		
		pradio.add(cminus);

		ctimes = new JCheckBox("Multiply", false);
		ctimes.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				enableInputWith(true);
			}
		});		
		cbg.add(ctimes);		
		pradio.add(ctimes);

		cdiv = new JCheckBox("Divide", false);
		cdiv.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				enableInputWith(true);
			}
		});				
		cbg.add(cdiv);
		pradio.add(cdiv);

		//With panel
		JPanel pfrom2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pfrom2);
		cfrom2 = new JComboBox();
		dim = cfrom2.getPreferredSize();
		dim.width = CHOOSER_SIZE;
		cfrom2.setPreferredSize(dim);
		cfrom2.addItem("1DHISTOGRAM2");
		ttimes2 = new JTextField("1.0", 8);
		pfrom2.add(cfrom2);
		pfrom2.add(new JLabel("x"));
		pfrom2.add(ttimes2);
		enableInputWith(true);

		//To panel
		JPanel pto = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pto);
		cto = new JComboBox();
		dim = cto.getPreferredSize();
		dim.width = CHOOSER_SIZE;
		cto.setPreferredSize(dim);
		cto.addItem(NEW_HIST);
		cto.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if (cto.getSelectedItem() != null) {
					setUseHist((String)cto.getSelectedItem());
				}
			}
		});
		ttextto = new JTextField("combine", 20);
		pto.add(cto);
		lname = new JLabel("Name");
		pto.add(lname);
		pto.add(ttextto);
		/* button panel */
        final PanelOKApplyCancelButtons pButtons = new PanelOKApplyCancelButtons(
                new PanelOKApplyCancelButtons.Listener() {
                    public void ok() {
                        apply();
                        dispose();
                    }

                    public void apply() {
                        try {
                            manipulate();
                            BROADCASTER
                                    .broadcast(BroadcastEvent.Command.REFRESH);
                            STATUS.setCurrentHistogram(hto);
                            BROADCASTER.broadcast(
                                    BroadcastEvent.Command.HISTOGRAM_SELECT,
                                    hto);
                        } catch (DataException je) {
                            messageHandler.errorOutln(je.getMessage());
                        }
                    }

                    public void cancel() {
                        dispose();
                    }
                });
		cdmanip.add(pButtons.getComponent(), BorderLayout.SOUTH);
		pack();
	}

	/**
	 * Implementation of Observable interface listeners for broadcast events.
	 * broadcast events where there are new histograms or histograms added.
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final BroadcastEvent.Command command=be.getCommand();
		if (command == BroadcastEvent.Command.HISTOGRAM_NEW ||
		        command == BroadcastEvent.Command.HISTOGRAM_ADD) {
			doSetup();
		}
	}

	/**
	 * Loads the list of gates and set co-ordinates as x y if 2d or lower upper
	 * if 1 d
	 *  
	 */
	public void doSetup() {
		String lfrom1, lfrom2, lto;

		lfrom1 = (String) cfrom1.getSelectedItem();
		lfrom2 = (String) cfrom2.getSelectedItem();
		lto = (String) cto.getSelectedItem();

		cfrom1.removeAllItems();
		addChooserHists(cfrom1, false, Histogram.Type.ONE_D);
		cfrom1.setSelectedItem(lfrom1);
		cfrom2.removeAllItems();
		addChooserHists(cfrom2, false, Histogram.Type.ONE_D);

		cfrom2.setSelectedItem(lfrom2);
		cto.removeAllItems();
		cto.addItem(NEW_HIST);
		addChooserHists(cto, true, Histogram.Type.ONE_D);
		cto.setSelectedItem(lto);
		setUseHist((String)cto.getSelectedItem());

	}

	/* non-javadoc:
	 * A second histogram is needed
	 */
	private void enableInputWith(boolean state) {
		cfrom2.setEnabled(state);
		ttimes2.setEnabled(state);
		lWith.setEnabled(state);
	}

	/* non-javadoc:
	 * add histograms of type type1 and type2 to chooser
	 */
	private void addChooserHists(JComboBox comboBox, boolean addNew, int histDim) {
		comboBox.removeAllItems();
		//Add working group new
		if(addNew) {
			comboBox.addItem(NEW_HIST+Group.WORKING_NAME+"/.");
			//Add new histograms
			for (Iterator iter = Group.getGroupList().iterator();iter.hasNext();) {
				Group group = (Group)iter.next();
				if (group.getType() != Group.Type.SORT &&
					!Group.WORKING_NAME.equals(group.getName())) {
					comboBox.addItem(NEW_HIST+group.getName()+"/.");
				}
			}
		}
		/* Add Existing hisograms */
		for (Iterator grpiter = Group.getGroupList().iterator(); grpiter.hasNext();) {
			Group group = (Group)grpiter.next();
			for  (Iterator histiter = group.getHistogramList().iterator(); histiter.hasNext();) {
				Histogram hist =(Histogram)histiter.next();
				if (hist.getType().getDimensionality() == histDim) {
					comboBox.addItem(hist.getFullName());
				}
			}
		}

		comboBox.setSelectedIndex(0);
	}

	/* non-javadoc:
	 * Set dialog box for new histogram to be created
	 */
	private void setUseHist(String name) {
		if (isNewHistogram(name)){
			lname.setEnabled(true);
			ttextto.setEnabled(true);
		} else {
			lname.setEnabled(false);
			ttextto.setEnabled(false);
			
		}		
	}
	
	private boolean isNewHistogram(String name){
		return name.startsWith(NEW_HIST);
	}

	/* non-javadoc:
	 * Does the work of manipulating histograms
	 */
	private void manipulate() throws DataException {
		final double fac1;
		try {//read information for first histogram
			fac1 = Double.valueOf(ttimes1.getText().trim()).doubleValue();
		} catch (NumberFormatException nfe) {
			throw new DataException(
					"First factor is not a valid number [Manipulations]");
		}
		final AbstractHist1D hfrom1 = (AbstractHist1D)Histogram.getHistogram((String) cfrom1
				.getSelectedItem());
		final double[] in1;
		if (hfrom1.getType() == Histogram.Type.ONE_DIM_INT) {
			in1 = toDoubleArray((int[]) hfrom1.getCounts());
		} else {
			in1 = (double[]) hfrom1.getCounts();
		}
		final double[] err1 = hfrom1.getErrors();
		final double fac2;
		try {//read in information for second histogram
			fac2 = Double.valueOf(ttimes2.getText().trim()).doubleValue();
		} catch (NumberFormatException nfe) {
			throw new DataException(
					"Second factor is not a valid number [Manipulations]");
		}
		final double[] in2, err2;
		if (cfrom2.isEnabled()) {
			final AbstractHist1D hfrom2 = (AbstractHist1D)Histogram.getHistogram((String) cfrom2
					.getSelectedItem());
			if (hfrom2.getType() == Histogram.Type.ONE_DIM_INT) {
				in2 = toDoubleArray((int[]) hfrom2.getCounts());
			} else {
				in2 = (double[]) hfrom2.getCounts();
			}
			err2 = hfrom2.getErrors();
		} else {
			in2 = null;
			err2 = null;
		}

		//read in information for to histogram
		String name = (String) cto.getSelectedItem();
		
		if (isNewHistogram(name)) {
			name = ttextto.getText().trim();
			/*hto = new Histogram(name, Histogram.Type.ONE_D_DOUBLE, hfrom1
					.getSizeX(), name);*/
			Group.createGroup("Working", Group.Type.FILE);
			hto = (AbstractHist1D)Histogram.createHistogram(
					new double[hfrom1.getSizeX()],name);
			BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
			messageHandler
					.messageOutln("New histogram of type double created, name: '"
							+ name + "'");
		} else {
			hto = (AbstractHist1D)Histogram.getHistogram(name);
		}
		hto.setZero();
		final double[] out = (hto.getType() == Histogram.Type.ONE_DIM_INT) ? toDoubleArray((int[]) hto
				.getCounts())
				: (double[]) hto.getCounts();
		double[] errOut = hto.getErrors();

		if (cnorm.isSelected()) {
			for (int i = 0; i < out.length; i++) {
				out[i] = fac1 * in1[i];
				errOut[i] = fac1 * err1[i];
			}
		} else if (cplus.isSelected()) {
			for (int i = 0; i < out.length; i++) {
				out[i] = fac1 * in1[i] + fac2 * in2[i];
				errOut[i] = Math.sqrt(fac1 * fac1 * err1[i] * err1[i] + fac2
						* fac2 * err2[i] * err2[i]);
			}
		} else if (cminus.isSelected()) {
			for (int i = 0; i < out.length; i++) {
				out[i] = fac1 * in1[i] - fac2 * in2[i];
				errOut[i] = Math.sqrt(fac1 * fac1 * err1[i] * err1[i] + fac2
						* fac2 * err2[i] * err2[i]);
			}
		} else if (ctimes.isSelected()) {
			for (int i = 0; i < out.length; i++) {
				out[i] = fac1 * in1[i] * fac2 * in2[i];
				errOut[i] = Math.sqrt(fac2 * fac2 * err1[i] * err1[i] + fac1
						* fac1 * err2[i] * err2[i]);
			}
		} else if (cdiv.isSelected()) {
			for (int i = 0; i < out.length; i++) {
				out[i] = fac1 * in1[i] / (fac2 * in2[i]);
				errOut[i] = in1[i]
						/ in2[i]
						* Math.sqrt(fac1 * fac1 / (err1[i] * err1[i]) + fac2
								* fac2 / (err2[i] * err2[i]));
			}
		}
		hto.setErrors(errOut);
		/* cast to int array if needed */
		if (hto.getType() == Histogram.Type.ONE_DIM_INT) {
			hto.setCounts(toIntArray(out));
		} else {
			hto.setCounts(out);
		}
		
	}

	/* non-javadoc:
	 * Converts int array to double array
	 */
	private double[] toDoubleArray(int[] in) {
		final double[] out = new double[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = in[i];
		}
		return out;
	}

	/* non-javadoc:
	 * Converts double array to int array
	 */
	private int[] toIntArray(double[] in) {
		final int[] out = new int[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = (int) Math.round(in[i]);
		}
		return out;
	}

	/**
	 * Process window events If the window is active check that the histogram
	 * been displayed has not changed. If it has cancel the gate setting.
	 */
	public void windowActivated(WindowEvent e) {
		//no action
	}

	/**
	 * Window Events windowClosing only one used.
	 */
	public void windowClosing(WindowEvent e) {
		dispose();
	}

	/**
	 * Does nothing only windowClosing used.
	 */
	public void windowClosed(WindowEvent e) {
		/* does nothing for now */
	}

	/**
	 * Does nothing only windowClosing used.
	 */
	public void windowDeactivated(WindowEvent e) {
		/* does nothing for now */
	}

	/**
	 * Does nothing only windowClosing used.
	 */
	public void windowDeiconified(WindowEvent e) {
		/* does nothing for now */
	}

	/**
	 * Does nothing only windowClosing used.
	 */
	public void windowIconified(WindowEvent e) {
		/* does nothing for now */
	}

	/**
	 * removes list of gates when closed only windowClosing used.
	 */
	public void windowOpened(WindowEvent e) {
		doSetup();
	}

}