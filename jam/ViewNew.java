/*
 * Created on Nov 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jam;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.plot.View;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Create a view dialog
 * @author ken
 */
public class ViewNew extends JDialog {

	private final static String[] DEFAULT_NUMBERS = { "1", "2","3", "4","5", "7","8"};
	
	private final JTextField textName;
	
	private final JComboBox comboRows;
	
	private final JComboBox comboCols;
	
	private JamStatus status = JamStatus.instance();
 

	public ViewNew(){
		 //super(, "New View", true);
		 super.setTitle( "New View");
		 super.setModal(true);

		final Container cdnew = getContentPane();
		setResizable(false);
		cdnew.setLayout(new BorderLayout(5, 5));
		setLocation(20, 50);
		/* Labels on the left */
		final JPanel pLabels = new JPanel(new GridLayout(0, 1, 5, 5));
		pLabels.setBorder(new EmptyBorder(10, 10, 0, 0));
		cdnew.add(pLabels, BorderLayout.WEST);
		final JLabel ln = new JLabel("Name", JLabel.RIGHT);
		pLabels.add(ln);
		final JLabel lrw = new JLabel("Rows", JLabel.RIGHT);
		pLabels.add(lrw);
		final JLabel lcl = new JLabel("Columns", JLabel.RIGHT);
		pLabels.add(lcl);
		/* Entries */
		final JPanel pEntires = new JPanel(new GridLayout(0, 1, 5, 5));
		pEntires.setBorder(new EmptyBorder(10, 0, 0, 10));
		cdnew.add(pEntires, BorderLayout.CENTER);
		/* Name field */
		final JPanel pName = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pEntires.add(pName);
		final String space = " ";
		textName = new JTextField(space);
		textName.setColumns(15);
		pName.add(textName);
		/* Rows Combo */		
		final JPanel pRows = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pEntires.add(pRows);
		comboRows = new JComboBox(DEFAULT_NUMBERS);
		pRows.add(comboRows);
		/* Cols Combo */		
		final JPanel pCols = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pEntires.add(pCols);
		comboCols = new JComboBox(DEFAULT_NUMBERS);	
		pCols.add(comboCols);
		
	
		/*  panel for buttons */
		final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cdnew.add(pbutton, BorderLayout.SOUTH);
		final JPanel pbnew = new JPanel();
		pbnew.setLayout(new GridLayout(1, 0, 5, 5));
		pbutton.add(pbnew, BorderLayout.SOUTH);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				makeView();
				close();
				
			}
		});
		pbnew.add(bok);
		final JButton bapply = new JButton("Apply");
		bapply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				makeView();
			}
		});
		pbnew.add(bapply);
		final JButton bcancel = new JButton("Cancel");
		bcancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				close();
			}
		});
		pbnew.add(bcancel);
		pack();
	}
		
	public void showView(){
		this.show();
	}
	private void makeView(){
		String name =textName.getText();
		int nRows = Integer.parseInt(((String) comboRows.getSelectedItem()).trim());
		int nCols = Integer.parseInt(((String) comboCols.getSelectedItem()).trim());		
		new View(name, nRows, nCols);
		Broadcaster broadcaster=Broadcaster.getSingletonInstance();
		broadcaster.broadcast(BroadcastEvent.Command.VIEW_NEW);
	}
	private void close(){
		dispose();
	}
}
