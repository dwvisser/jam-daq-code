package jam.data.control;

import com.google.inject.Inject;
import jam.data.AbstractHistogram;
import jam.data.Gate;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.ui.SelectionTree;
import jam.ui.WindowCancelAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A dialog for adding existing gates to histograms.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Jun 4, 2004
 */
public final class GateAdd extends AbstractControl {

	private transient final JComboBox<Object> cadd;

	private transient Gate currentGateAdd;

	/**
	 * Create a new "add gate" dialog.
	 * 
	 * @param frame
	 *            application frame
	 * @param broadcaster
	 *            broadcasts state changes
	 * @param gateListRender
	 *            a renderer for the list of gates
	 * 
	 */
	@Inject
	public GateAdd(final Frame frame, final Broadcaster broadcaster,
			final GateListCellRenderer gateListRender) {
		super(frame, "Add Gate", false, broadcaster);
		final Container cdadd = getContentPane();
		setResizable(false);
		setLocation(20, 50);
		/* panel with chooser */
		final JPanel ptadd = new JPanel();
		ptadd.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
		cdadd.add(ptadd, BorderLayout.CENTER);
		cadd = new JComboBox<>(new GateComboBoxModel(
                GateComboBoxModel.Mode.ALL));
		cadd.setRenderer(gateListRender);
		cadd.addActionListener(event -> {
            final Object item = cadd.getSelectedItem();
            if (item instanceof Gate) {
                selectGateAdd((Gate) item);
            }
        });
		final Dimension dimadd = cadd.getPreferredSize();
		dimadd.width = 200;
		cadd.setPreferredSize(dimadd);
		ptadd.add(cadd);
		/* panel for buttons */
		final JPanel pbuttonAdd = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cdadd.add(pbuttonAdd, BorderLayout.SOUTH);
		final JPanel pbadd = new JPanel();
		pbadd.setLayout(new GridLayout(1, 0, 5, 5));
		pbuttonAdd.add(pbadd);
		final JButton bokadd = new JButton("OK");
		bokadd.addActionListener(event -> {
            addGate();
            dispose();
        });
		pbadd.add(bokadd);
		final JButton bapplyadd = new JButton("Apply");
		bapplyadd.addActionListener(event -> addGate());
		pbadd.add(bapplyadd);
		final JButton bcanceladd = new JButton(new WindowCancelAction(this));
		pbadd.add(bcanceladd);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				dispose();
			}
		});
		pack();
	}

	/**
	 * Add a gate.
	 * 
	 */
	private void addGate() {
		if (currentGateAdd == null) {
			LOGGER.severe("Need to choose a gate to add ");
		} else {
			final AbstractHistogram hist = (AbstractHistogram) SelectionTree
					.getCurrentHistogram();
			hist.getGateCollection().addGate(currentGateAdd);
			broadcaster.broadcast(BroadcastEvent.Command.GATE_ADD);
			LOGGER.info("Added gate '" + currentGateAdd.getName().trim()
					+ "' to histogram '" + hist.getFullName() + "'");
		}
	}

	/**
	 * @see jam.data.control.AbstractControl#doSetup()
	 */
	@Override
	public void doSetup() {
		cadd.setSelectedIndex(0);
	}

	protected void selectGateAdd(final Gate gate) {
		synchronized (this) {
			currentGateAdd = gate;
		}
	}

}
