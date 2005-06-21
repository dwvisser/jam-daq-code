package jam;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * The About Dialog
 * 
 * @author Ken Swartz
 */
public class AboutDialog {
    
    private transient final JDialog dialog;
    
	/**
	 * Constructs the "about" dialog.
	 * 
	 * @param frame parent of the dialog
	 */
	public AboutDialog(Frame frame) {
		dialog=new JDialog(frame, "About Jam", false);
		final Container cad = dialog.getContentPane();
		dialog.setResizable(false);
		final int POS_X=20;
		final int POS_Y=50;
		dialog.setLocation(POS_X, POS_Y);
		cad.setLayout(new BorderLayout());
		final JPanel pcenter = new JPanel(new GridLayout(0, 1));
		Border border = new EmptyBorder(20,20,20,20);
		pcenter.setBorder(border);
		cad.add(pcenter, BorderLayout.CENTER);
		pcenter.add(
			new JLabel("Jam v" + Version.getInstance().getName(), SwingConstants.CENTER));
		pcenter.add(new JLabel("by", SwingConstants.CENTER));
		pcenter.add(
			new JLabel(
				"Ken Swartz, Dale Visser, and John Baris",
				SwingConstants.CENTER));
		final String HOME_URL="http://jam-daq.sourceforge.net/";
		pcenter.add(
			new JLabel(HOME_URL, SwingConstants.CENTER));
		final JPanel pbut = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cad.add(pbut, BorderLayout.SOUTH);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener(){
			public void actionPerformed(final ActionEvent event){
				dialog.dispose();
			}
		});
		pbut.add(bok);
		dialog.pack();
		/* Receives events for closing the dialog box and closes it. */
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent event) {
				dialog.dispose();
			}
		});
	}
	
	/**
	 * Gets the dialog.
	 * 
	 * @return the dialog
	 */
	public JDialog getDialog(){
	    return dialog;
	}
}
