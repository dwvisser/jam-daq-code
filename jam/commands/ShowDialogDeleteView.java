package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.plot.View;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


/**
 * Command to delete view.
 * 
 * @author Ken Swartz
 */
class ShowDialogDeleteView extends AbstractShowDialog {
	
	public void initCommand(){
		putValue(NAME, "Delete\u2026");
		dialog = new ViewDelete();		
	}
	
	private class ViewDelete extends JDialog {

		private final JComboBox comboNames;

		private static final String CHOOSE_NAME = "Choose Name";
		
		private final AbstractButton bok=new JButton("OK");
		private final AbstractButton bapply=new JButton("Apply");

		public ViewDelete() {
			setTitle("Delete View");
			setModal(false);
			final Container cdnew = getContentPane();
			setResizable(false);
			cdnew.setLayout(new BorderLayout(5, 5));
			setLocation(20, 50);
			final JPanel pNames = new JPanel(
					new FlowLayout(FlowLayout.LEFT, 20, 20));
			pNames.setBorder(new EmptyBorder(0, 10, 0, 10));
			cdnew.add(pNames, BorderLayout.CENTER);
			final JLabel ln = new JLabel("Name", JLabel.RIGHT);
			pNames.add(ln);
			/* Name combo box */
			comboNames = new JComboBox();
			Dimension dim = comboNames.getPreferredSize();
			dim.width = 200;
			comboNames.setPreferredSize(dim);
			pNames.add(comboNames);
			/* panel for buttons */
			final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
			cdnew.add(pbutton, BorderLayout.SOUTH);
			final JPanel pbnew = new JPanel();
			pbnew.setLayout(new GridLayout(1, 0, 5, 5));
			pbutton.add(pbnew, BorderLayout.SOUTH);
			bok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					deleteView();
					dispose();
				}
			});
			pbnew.add(bok);
			bapply.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					deleteView();
				}
			});
			pbnew.add(bapply);
			final JButton bcancel = new JButton("Cancel");
			bcancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					dispose();
				}
			});
			pbnew.add(bcancel);
			addWindowListener(new WindowAdapter(){
				public void windowActivated(WindowEvent e){
					updateViewNames();
				}
				
				public void windowOpened(WindowEvent e){
					updateViewNames();
				}
			});
			pack();
		}

		private void updateViewNames() {
			final List namesList = new ArrayList();
			namesList.add(CHOOSE_NAME);
			namesList.addAll(View.getNameList());
			comboNames.removeAllItems();
			final Iterator iter=namesList.iterator();
			while (iter.hasNext()){
				final Object next=iter.next();
				if (!next.equals(View.SINGLE.getName())){
					comboNames.addItem(next);
				}
			}
			final boolean enable = comboNames.getModel().getSize()>1;
			comboNames.setEnabled(enable);
			bok.setEnabled(enable);
			bapply.setEnabled(enable);
		}

		/**
		 * Delete a view.
		 */
		private void deleteView() {
			final String name = (String) comboNames.getSelectedItem();
			if (!(name.equals(CHOOSE_NAME) || name.equals(View.SINGLE.getName()))) {
				View.removeView(name);
			}
			Broadcaster.getSingletonInstance().broadcast(
					BroadcastEvent.Command.VIEW_NEW);
			status.getDisplay().setView(View.SINGLE);
			updateViewNames();
		}
	}
	
}
