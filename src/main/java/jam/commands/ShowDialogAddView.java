package jam.commands;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.plot.PlotDisplay;
import jam.plot.View;
import jam.ui.PanelOKApplyCancelButtons;

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
	 * Create a view dialog.
	 * 
	 * @author Ken Swartz
	 */
	static class ViewNew extends JDialog {

		private final transient JTextField textName;

		private final transient JComboBox<Integer> comboRows, comboCols;

		private final transient PlotDisplay display;

		private final transient Broadcaster broadcaster;

		private static final Frame PARENT = null;

		@Inject
		ViewNew(final PlotDisplay display, final Broadcaster broadcaster) {
			super(PARENT, "New View", false);
			this.display = display;
			this.broadcaster = broadcaster;
			final Container cdnew = getContentPane();
			setResizable(false);
			int smallGap = 5;
			cdnew.setLayout(new BorderLayout(smallGap, smallGap));
			setLocation(20, 50);

			/* Labels on the left */
			final JPanel pLabels = new JPanel(new GridLayout(0, 1, smallGap,
					smallGap));
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
			final Integer[] defaultNumbers = {1,
					2, 3, 4,
					4, 5, 6,
					7};
			comboRows = new JComboBox<>(defaultNumbers);
			pRows.add(comboRows);
			/* Cols Combo */
			final JPanel pCols = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,
					0));
			pEntires.add(pCols);
			comboCols = new JComboBox<>(defaultNumbers);
			pCols.add(comboCols);

			/* panel for buttons */
			final PanelOKApplyCancelButtons.Listener callback = new PanelOKApplyCancelButtons.AbstractListener(
					this) {
				public void apply() {
					makeView();
				}
			};

			final PanelOKApplyCancelButtons pbutton = new PanelOKApplyCancelButtons(
					callback);
			cdnew.add(pbutton.getComponent(), BorderLayout.SOUTH);
			pack();
		}

		/**
		 * Make a new view.
		 */
		private void makeView() {
			String name = textName.getText();
			View viewNew;
			final int nRows = (Integer) comboRows.getSelectedItem();
			final int nCols = (Integer) comboCols.getSelectedItem();
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
