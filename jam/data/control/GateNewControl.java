package jam.data.control;

import jam.data.DataException;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.MessageHandler;
import jam.global.JamStatus;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;

/**
 * @author Ken
 *
 */
public class GateNewControl extends DataControl implements ActionListener{

	/* new gate dialog box */
	private final JDialog dnew;
	private final JTextField textNew;
	private  Broadcaster broadcaster;
	private  MessageHandler messageHandler;
	

	public GateNewControl(){
		
		super( "New Gate",false);
		//>>new gate dialog box

		dnew=new JDialog(status.getFrame() ,"New Gate",false);
		final Container cdnew=dnew.getContentPane();
		dnew.setResizable(false);
		cdnew.setLayout(new BorderLayout(5,5));
		dnew.setLocation(20,50);

		/* panel with chooser */
		final JPanel ptnew =new JPanel();
		ptnew.setLayout(new FlowLayout(FlowLayout.LEFT,20,20));
		cdnew.add(ptnew,BorderLayout.CENTER);
		ptnew.add(new JLabel("Name"));
		textNew=new JTextField("",20);
		//textNew.setBackground(Color.white);
		ptnew.add(textNew);

		/*  panel for buttons */
		final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cdnew.add(pbutton,BorderLayout.SOUTH);

		final JPanel pbnew= new JPanel();
		pbnew.setLayout(new GridLayout(1,0,5,5));
		pbutton.add(pbnew,BorderLayout.SOUTH);

		final JButton bok  =   new JButton("OK");
		bok.setActionCommand("oknew");
		bok.addActionListener(this);
		pbnew.add(bok);

		final JButton bapply = new JButton("Apply");
		bapply.setActionCommand("applynew");
		bapply.addActionListener(this);
		pbnew.add(bapply);

		final JButton bcancel =new JButton("Cancel");
		bcancel.setActionCommand("cancelnew");
		bcancel.addActionListener(this);
		pbnew.add(bcancel);
		dnew.pack();
		
	}
	
	/**
	 * Respond to user action.
	 *
	 * @param e event caused by user
	 */
	public void actionPerformed(ActionEvent e){
		final String command=e.getActionCommand();
		//try {
			if(("oknew".equals(command))|| "applynew".equals(command)){
				makeGate();
				if ("oknew".equals(command)){
					dnew.dispose();
				}
			} else if  ("cancelnew".equals(command)){
				dnew.dispose();
			} else  {
				messageHandler.errorOutln(getClass().getName()+
				".actionPerformed(): '"+command+
				"' not a recognized command.");
			}
		//} catch (DataException je) {
		//	messageHandler.errorOutln( je.getMessage() );
		//} 
	}
	
	/**
	 * Make a new gate, and add it to the current histogram.
	 *
	 * @throws GlobalException if there's a problem
	 */
	private void makeGate() {
		final Histogram hist=Histogram.getHistogram(
		status.getCurrentHistogramName());
		new Gate(textNew.getText(),hist);
		broadcaster.broadcast(BroadcastEvent.GATE_ADD);
		messageHandler.messageOutln("New gate "+textNew.getText()+
		" created for histogram "+hist.getName());
	}
	
	public void setup() {
		// TODO Auto-generated method stub

	}

}
