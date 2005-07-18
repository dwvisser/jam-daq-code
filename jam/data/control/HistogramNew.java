package jam.data.control;

import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static javax.swing.SwingConstants.RIGHT;
import javax.swing.border.EmptyBorder;

/**
 * Class to control the histograms Allows one to zero the histograms and create
 * new histograms
 * 
 * @author Ken Swartz
 * @version 0.5
 */
public class HistogramNew extends AbstractControl {

	final int CHOOSER_SIZE = 200;
	
	private final MessageHandler msghdlr;

	private final JComboBox comboGroup;	
	
	private DefaultComboBoxModel comboGroupModel;	
	
	private final JTextField textName;

	private final JTextField textTitle;

	private final JComboBox comboSize;

	private final JCheckBox coneInt, coneDbl, ctwoInt, ctwoDbl;

	private final static String[] DEFAULT_SIZES = { "64", "128", "256", "512",
			"1024", "2048", "4096", "8192", };

	/**
	 * Construct a new "new histogram" dialog.
	 * 
	 * @param msghdlr where to print messages
	 */
	public HistogramNew(MessageHandler msghdlr) {
		super("New Histogram ", false);
		this.msghdlr = msghdlr;
		/* dialog box */
		setResizable(false);
		setLocation(30, 30);
		final Container cdialogNew = getContentPane();
		cdialogNew.setLayout(new BorderLayout(10, 10));
		/* Labels on the left */
		final JPanel pLabels = new JPanel(new GridLayout(0, 1, 5, 5));
		pLabels.setBorder(new EmptyBorder(10, 10, 0, 0));
		cdialogNew.add(pLabels, BorderLayout.WEST);
		final JLabel lg = new JLabel("Group", RIGHT);
		pLabels.add(lg);		
		final JLabel ln = new JLabel("Name", RIGHT);
		pLabels.add(ln);
		final JLabel lti = new JLabel("Title", RIGHT);
		pLabels.add(lti);
		final JLabel lt = new JLabel("Type", RIGHT);
		pLabels.add(lt);
		final JLabel ls = new JLabel("Size", RIGHT);
		pLabels.add(ls);
		/* Entires panel */
		final JPanel pEntires = new JPanel(new GridLayout(0, 1, 5, 5));
		pEntires.setBorder(new EmptyBorder(10, 0, 0, 10));
		cdialogNew.add(pEntires, BorderLayout.CENTER);
		final JPanel pGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pEntires.add(pGroup);
		comboGroupModel = new DefaultComboBoxModel();
		comboGroup = new JComboBox(comboGroupModel);
		Dimension dim = comboGroup.getPreferredSize();
		dim.width = CHOOSER_SIZE;
		comboGroup.setPreferredSize(dim);
		/*
		comboGroup.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
			}
		});
		*/
		

		pGroup.add(comboGroup);
		
		final JPanel pName = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pEntires.add(pName);
		textName = new JTextField("");
		textName.setColumns(15);
		pName.add(textName);
		final JPanel pTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pEntires.add(pTitle);
		textTitle = new JTextField("");
		textTitle.setColumns(30);
		pTitle.add(textTitle);
		final Panel pradio = new Panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		final ButtonGroup cbg = new ButtonGroup();
		coneInt = new JCheckBox(Histogram.Type.ONE_DIM_INT.toString(), true);
		coneDbl = new JCheckBox(Histogram.Type.ONE_D_DOUBLE.toString(), false);
		ctwoInt = new JCheckBox(Histogram.Type.TWO_DIM_INT.toString(), false);
		ctwoDbl = new JCheckBox(Histogram.Type.TWO_D_DOUBLE.toString(), false);
		cbg.add(coneInt);
		cbg.add(coneDbl);
		cbg.add(ctwoInt);
		cbg.add(ctwoDbl);
		pradio.add(coneInt);
		pradio.add(coneDbl);
		pradio.add(ctwoInt);
		pradio.add(ctwoDbl);
		pEntires.add(pradio);
		final JPanel pSize = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pEntires.add(pSize);
		comboSize = new JComboBox(DEFAULT_SIZES);
		comboSize.setEditable(true);
		pSize.add(comboSize);
		/* panel for buttons */
		final JPanel pbOuter = new JPanel();
		pbOuter.setLayout(new FlowLayout(FlowLayout.CENTER));
		cdialogNew.add(pbOuter, BorderLayout.SOUTH);
		final JPanel pb = new JPanel(new GridLayout(1, 0, 5, 5));
		pbOuter.add(pb);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				makeHistogram();
				dispose();
			}
		});
		pb.add(bok);
		final JButton bapply = new JButton("Apply");
		bapply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				makeHistogram();
			}
		});
		pb.add(bapply);
		final JButton bcancel = new JButton("Cancel");
		bcancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		pb.add(bcancel);
		pack();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}
	
	/**
	 * Show the dialog.
	 */
	public void setVisible(final boolean show) {
		if (show) {
			doSetup();
		}
		super.setVisible(show);
	}

	/**
	 * Initializes chooser properly.
	 */
	public void doSetup() {
		comboGroupModel.removeAllElements();
		final Iterator iter = Group.getGroupList().iterator();		
		/* Add working group first */
		comboGroupModel.addElement( Group.WORKING_NAME);
		while(iter.hasNext()) {
			final Group group =(Group)iter.next();
			/* Don't add sort group or working group that was already added */
			if (group.getType()!=Group.Type.SORT && 
				!Group.WORKING_NAME.equals(group.getName()) ){
				comboGroupModel.addElement( group.getName() );
			}			
		}
	}

	/**
	 * Make a new histogram from the field inputs
	 */
	private void makeHistogram() {
		Group histGroup;
		final String groupName = (String)comboGroupModel.getSelectedItem();		
		final String name = textName.getText().trim();
		final String title = textTitle.getText().trim();
		final int size = Integer
		.parseInt(((String) comboSize.getSelectedItem()).trim());
		final Object array;
		if (coneInt.isSelected()) {
			array = new int[size];
		} else if (coneDbl.isSelected()) {
			array=new double[size];
		} else if (ctwoInt.isSelected()) {
			array=new int[size][size];
		} else {
			array = new double[size][size];
		}
		if (null==Group.getGroup(groupName)) {
			histGroup =Group.createGroup(groupName, Group.Type.TEMP);
		} else {
			histGroup =Group.getGroup(groupName);
			STATUS.setCurrentGroup(histGroup);			
		}
		final Histogram hist= Histogram.createHistogram(histGroup, array, name, title);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		final JamStatus status = JamStatus.getSingletonInstance();
		status.setCurrentHistogram(hist);
		status.setCurrentGroup(histGroup);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
		final StringBuffer msg=new StringBuffer("New histogram created, ");
		msg.append(name).append(", type: ");
		if (coneInt.isSelected()) {
			msghdlr.messageOutln(msg.append(coneInt.getText()).toString());
		} else if (coneDbl.isSelected()) {
			msghdlr.messageOutln(msg.append(coneDbl.getText()).toString());
		} else if (ctwoInt.isSelected()) {
			msghdlr.messageOutln(msg.append(ctwoInt.getText()).toString());
		} else  {
			msghdlr.messageOutln(msg.append(ctwoDbl.getText()).toString());
		}
	}
}