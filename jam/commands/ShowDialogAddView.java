package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.plot.PlotDisplay;
import jam.plot.View;
import jam.ui.WindowCancelAction;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

/**
 * Command to add view.
 * 
 * @author Ken Swartz
 */
public class ShowDialogAddView extends AbstractShowDialog {

	@Inject
	ShowDialogAddView(final ViewNew viewNew) {
		super("New\u2026");
		dialog = viewNew;
	}

	/**
	 * Create a view dialog
	 * 
	 * @author Ken Swartz
	 */
	static class ViewNew extends JDialog {

		private transient final JTextField textName;

		private transient final JComboBox comboRows;

		private transient final JComboBox comboCols;

		private transient final PlotDisplay display;

		private transient final Broadcaster broadcaster;

		private static final Frame parent = null;

		@Inject
		ViewNew(final PlotDisplay display, final Broadcaster broadcaster) {
			super(parent, "New View", false);
			this.display = display;
			this.broadcaster = broadcaster;
			final Container cdnew = getContentPane();
			setResizable(false);
			cdnew.setLayout(new BorderLayout(5, 5));
			setLocation(20, 50);
			/* Labels on the left */
			final JPanel pLabels = new JPanel(new GridLayout(0, 1, 5, 5));
			pLabels.setBorder(new EmptyBorder(10, 10, 0, 0));
			cdnew.add(pLabels, BorderLayout.WEST);
			pLabels.add(new JLabel("Name", SwingConstants.RIGHT));
			final JLabel lrw = new JLabel("Rows", SwingConstants.RIGHT);
			pLabels.add(lrw);
			final JLabel lcl = new JLabel("Columns", SwingConstants.RIGHT);
			pLabels.add(lcl);
			/* Entries */
			final JPanel pEntires = new JPanel(new GridLayout(0, 1, 5, 5));
			pEntires.setBorder(new EmptyBorder(10, 0, 0, 10));
			cdnew.add(pEntires, BorderLayout.CENTER);
			/* Name field */
			final JPanel pName = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,
					0));
			pEntires.add(pName);
			textName = new JTextField("");
			textName.setColumns(15);
			pName.add(textName);
			/* Rows Combo */
			final JPanel pRows = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,
					0));
			pEntires.add(pRows);
			final String[] DEFAULT_NUMBERS = { "1", "2", "3", "4", "5", "7",
					"8" };
			comboRows = new JComboBox(DEFAULT_NUMBERS);
			pRows.add(comboRows);
			/* Cols Combo */
			final JPanel pCols = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,
					0));
			pEntires.add(pCols);
			comboCols = new JComboBox(DEFAULT_NUMBERS);
			pCols.add(comboCols);

			/* panel for buttons */
			final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
			cdnew.add(pbutton, BorderLayout.SOUTH);
			final JPanel pbnew = new JPanel();
			pbnew.setLayout(new GridLayout(1, 0, 5, 5));
			pbutton.add(pbnew, BorderLayout.SOUTH);
			final JButton bok = new JButton("OK");
			bok.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					makeView();
					dispose();

				}
			});
			pbnew.add(bok);
			final JButton bapply = new JButton("Apply");
			bapply.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					makeView();
				}
			});
			pbnew.add(bapply);
			final JButton bcancel = new JButton(new WindowCancelAction(this));
			pbnew.add(bcancel);
			pack();
		}

		/**
		 * Make a new view
		 */
		private void makeView() {
			String name = textName.getText();
			View viewNew;
			final int nRows = Integer.parseInt(((String) comboRows
					.getSelectedItem()).trim());
			final int nCols = Integer.parseInt(((String) comboCols
					.getSelectedItem()).trim());
			/* Check for blank name */
			if (name.trim().equals("")) {
				name = "View " + nRows + "x" + nCols;
			}
			viewNew = new View(name, nRows, nCols);
			this.broadcaster.broadcast(BroadcastEvent.Command.VIEW_NEW);
			this.display.setView(viewNew);
		}
	}
}