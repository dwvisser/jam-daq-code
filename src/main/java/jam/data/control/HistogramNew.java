package jam.data.control;

import static javax.swing.SwingConstants.RIGHT;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

import jam.data.AbstractHistogram;
import jam.data.Factory;
import jam.data.Group;
import jam.data.HistogramType;
import jam.data.NameValueCollection;
import jam.data.Warehouse;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.ui.SelectionTree;
import jam.ui.WindowCancelAction;

/**
 * Class to control the histograms Allows one to zero the histograms and create
 * new histograms
 * 
 * @author Ken Swartz
 * @version 0.5
 */

public class HistogramNew extends AbstractControl {

	private static final NameValueCollection<Group> GROUPS = Warehouse
			.getGroupCollection();

	private static final int CHOOSER_SIZE = 200;

	private transient final DefaultComboBoxModel<String> comboGroupModel;

	private transient final JTextField textName;

	private transient final JTextField textTitle;

	private transient final JComboBox<Integer> comboSize;

	private transient final JCheckBox coneInt, coneDbl, ctwoInt, ctwoDbl;

	private transient final JamStatus status;

	private final static Integer[] DEFAULT_SIZES = {64,
			128, 256, 512,
			1024, 2048,
			4096, 8192};

	/**
	 * Construct a new "new histogram" dialog.
	 * 
	 * @param frame
	 *            application frame
	 * 
	 * @param status
	 *            application status
	 * @param broadcaster
	 *            broadcasts state changes
	 */
	@Inject
	public HistogramNew(final Frame frame, final JamStatus status,
			final Broadcaster broadcaster) {
		super(frame, "New Histogram ", false, broadcaster);
		this.status = status;
		setResizable(false);
		setLocation(30, 30);
		final Container cdialogNew = getContentPane();
		cdialogNew.setLayout(new BorderLayout(10, 10));
		/* Labels on the left */
		final JPanel pLabels = new JPanel(new GridLayout(0, 1, 5, 5));
		pLabels.setBorder(new EmptyBorder(10, 10, 0, 0));
		cdialogNew.add(pLabels, BorderLayout.WEST);
		pLabels.add(new JLabel("Group", RIGHT));
		pLabels.add(new JLabel("Name", RIGHT));
		pLabels.add(new JLabel("Title", RIGHT));
		pLabels.add(new JLabel("Type", RIGHT));
		pLabels.add(new JLabel("Size", RIGHT));
		/* Entries panel */
		final JPanel pEntires = new JPanel(new GridLayout(0, 1, 5, 5));
		pEntires.setBorder(new EmptyBorder(10, 0, 0, 10));
		cdialogNew.add(pEntires, BorderLayout.CENTER);
		final JPanel pGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pEntires.add(pGroup);
		comboGroupModel = new DefaultComboBoxModel<>();
		final JComboBox<String> comboGroup = new JComboBox<>(
				comboGroupModel);
		final Dimension dim = comboGroup.getPreferredSize();
		dim.width = CHOOSER_SIZE;
		comboGroup.setPreferredSize(dim);
		/*
		 * comboGroup.addItemListener(new ItemListener() { public void
		 * itemStateChanged(ItemEvent ie) { } });
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
		coneInt = new JCheckBox(HistogramType.ONE_DIM_INT.toString(), true);
		coneDbl = new JCheckBox(HistogramType.ONE_D_DOUBLE.toString(), false);
		ctwoInt = new JCheckBox(HistogramType.TWO_DIM_INT.toString(), false);
		ctwoDbl = new JCheckBox(HistogramType.TWO_D_DOUBLE.toString(), false);
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
		comboSize = new JComboBox<>(DEFAULT_SIZES);
		comboSize.setEditable(true);
		pSize.add(comboSize);
		/* panel for buttons */
		final JPanel pbOuter = new JPanel();
		pbOuter.setLayout(new FlowLayout(FlowLayout.CENTER));
		cdialogNew.add(pbOuter, BorderLayout.SOUTH);
		final JPanel bottom = new JPanel(new GridLayout(1, 0, 5, 5));
		pbOuter.add(bottom);
		final JButton bok = new JButton("OK");
		bok.addActionListener(event -> {
            makeHistogram();
            dispose();
        });
		bottom.add(bok);
		final JButton bapply = new JButton("Apply");
		bapply.addActionListener(event -> makeHistogram());
		bottom.add(bapply);
		final JButton bcancel = new JButton(new WindowCancelAction(this));
		bottom.add(bcancel);
		pack();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				dispose();
			}
		});
	}

	/**
	 * Show the dialog.
	 */
	@Override
	public void setVisible(final boolean show) {
		if (show) {
			doSetup();
		}
		super.setVisible(show);
	}

	/**
	 * Initializes chooser properly.
	 */
	@Override
	public void doSetup() {
		comboGroupModel.removeAllElements();
		/* Add working group first */
		comboGroupModel.addElement(Group.WORKING_NAME);
		for (Group group : GROUPS.getList()) {
			/* Don't add sort group or working group that was already added */
			if (group.getType() != Group.Type.SORT
					&& !Group.WORKING_NAME.equals(group.getName())) {
				comboGroupModel.addElement(group.getName());
			}
		}
	}

	/**
	 * Make a new histogram from the field inputs
	 */
	private void makeHistogram() {
		Group histGroup;
		final String groupName = (String) comboGroupModel.getSelectedItem();
		final String name = textName.getText().trim();
		final String title = textTitle.getText().trim();
		final int size = (Integer) comboSize.getSelectedItem();
		Object array;
		if (coneInt.isSelected()) {
			array = new int[size];
		} else if (coneDbl.isSelected()) {
			array = new double[size];
		} else if (ctwoInt.isSelected()) {
			array = new int[size][size];
		} else {
			array = new double[size][size];
		}
		if (null == GROUPS.get(groupName)) {
			histGroup = Factory.createGroup(groupName, Group.Type.TEMP);
		} else {
			histGroup = GROUPS.get(groupName);
			this.status.setCurrentGroup(histGroup);
		}
		final AbstractHistogram hist = Factory.createHistogram(histGroup,
				array, name, title);
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		SelectionTree.setCurrentHistogram(hist);
		this.status.setCurrentGroup(histGroup);
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
		final StringBuilder msg = new StringBuilder("New histogram created, ");
		msg.append(name).append(", type: ");
		if (coneInt.isSelected()) {
			LOGGER.info(msg.append(coneInt.getText()).toString());
		} else if (coneDbl.isSelected()) {
			LOGGER.info(msg.append(coneDbl.getText()).toString());
		} else if (ctwoInt.isSelected()) {
			LOGGER.info(msg.append(ctwoInt.getText()).toString());
		} else {
			LOGGER.info(msg.append(ctwoDbl.getText()).toString());
		}
	}
}