package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.plot.PlotDisplay;
import jam.plot.View;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

/**
 * Command to delete view.
 * 
 * @author Ken Swartz
 */
class ShowDialogDeleteView extends AbstractShowDialog {

	private static class ViewDelete extends JDialog {

		private static final String CHOOSE_NAME = "Choose Name";

		private transient final JComboBox comboNames;

		private transient final PanelOKApplyCancelButtons pbutton = new PanelOKApplyCancelButtons(
				new PanelOKApplyCancelButtons.AbstractListener(this) {
					public void apply() {
						deleteView();
					}
				});

		private transient final PlotDisplay display;

		private static final Frame frame = null;

		@Inject
		ViewDelete(final PlotDisplay display) {
			super(frame, "Delete View", false);
			this.display = display;
			setModal(false);
			final Container cdnew = getContentPane();
			setResizable(false);
			cdnew.setLayout(new BorderLayout(5, 5));
			setLocation(20, 50);
			final JPanel pNames = new JPanel(new FlowLayout(FlowLayout.LEFT,
					20, 20));
			pNames.setBorder(new EmptyBorder(0, 10, 0, 10));
			cdnew.add(pNames, BorderLayout.CENTER);
			pNames.add(new JLabel("Name", SwingConstants.RIGHT));
			/* Name combo box */
			comboNames = new JComboBox();
			final Dimension dim = comboNames.getPreferredSize();
			dim.width = 200;
			comboNames.setPreferredSize(dim);
			pNames.add(comboNames);
			/* panel for buttons */
			cdnew.add(pbutton.getComponent(), BorderLayout.SOUTH);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowActivated(final WindowEvent event) {
					updateViewNames();
				}

				@Override
				public void windowOpened(final WindowEvent event) {
					updateViewNames();
				}
			});
			pack();
		}

		/**
		 * Delete a view.
		 */
		private void deleteView() {
			final String name = (String) comboNames.getSelectedItem();
			if (!(name.equals(CHOOSE_NAME) || name
					.equals(View.SINGLE.getName()))) {
				View.removeView(name);
			}
			Broadcaster.getSingletonInstance().broadcast(
					BroadcastEvent.Command.VIEW_NEW);
			this.display.setView(View.SINGLE);
			updateViewNames();
		}

		private void updateViewNames() {
			final List<String> namesList = new ArrayList<String>();
			namesList.add(CHOOSE_NAME);
			namesList.addAll(View.getNameList());
			comboNames.removeAllItems();
			for (String next : namesList) {
				if (!next.equals(View.SINGLE.getName())) {
					comboNames.addItem(next);
				}
			}
			final boolean enable = comboNames.getModel().getSize() > 1;
			comboNames.setEnabled(enable);
			pbutton.setButtonsEnabled(enable, enable, true);
		}
	}

	@Inject
	ShowDialogDeleteView(final ViewDelete viewDelete) {
		super("Delete\u2026");
		dialog = viewDelete;
	}

}
