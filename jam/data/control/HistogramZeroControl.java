package jam.data.control;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jam.global.*;
import jam.data.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Zero histograms dialog
 */
public class HistogramZeroControl extends DataControl implements ActionListener {

	private final Frame frame;
	private static final Broadcaster broadcaster=Broadcaster.getSingletonInstance();
	private final MessageHandler msghdlr;

	private final JDialog dialogZero;
	Histogram currentHistogram;

	/**
	 * Constructor
	 */
	public HistogramZeroControl(Frame frame, MessageHandler msghdlr){
		super();
		this.frame=frame;        
		this.msghdlr=msghdlr;
	
		//zero histogram dialog box
		dialogZero=new JDialog(frame,"Zero Histograms",false);
		Container dzc = dialogZero.getContentPane();
		dialogZero.setResizable(false);
		dzc.setLayout(new FlowLayout(FlowLayout.CENTER));
		JPanel pButton = new JPanel(new GridLayout(1,0,5,5));
		dialogZero.setLocation(20,50);
		JButton one =new JButton("Displayed");
		one.setActionCommand("onezero");
		one.addActionListener(this);
		pButton.add(one);
		JButton all =new JButton("   All   ");
		all.setActionCommand("allzero");
		all.addActionListener(this);
		pButton.add(all);
		JButton cancel=new JButton(" Cancel ");
		cancel.setActionCommand("cancelzero");
		cancel.addActionListener(this);
		pButton.add(cancel);
		dzc.add(pButton);
		dialogZero.pack();
		
		dialogZero.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				dialogZero.dispose();
			}
		});
		
	}
	/**
	 * Receive actions from Dialog Boxes
	 *
	 */
	public void actionPerformed(ActionEvent ae){
		String command=ae.getActionCommand();
		currentHistogram=Histogram.getHistogram(JamStatus.instance().getCurrentHistogramName());

			/* commands for zero histogram */
			if (command=="onezero") {
				currentHistogram.setZero();
				broadcaster.broadcast(BroadcastEvent.REFRESH);
				msghdlr.messageOutln("Zero Histogram: "+currentHistogram.getTitle());
				dialogZero.dispose();
			} else if (command=="allzero") {
				zeroAll();
				dialogZero.dispose();
			} else if (command=="cancelzero") {
				dialogZero.dispose();
			} else {
				/* just so at least a exception is thrown for now */
				throw new UnsupportedOperationException("Unregonized command: "+command);
			}
	}
	
	/**
	 * Zero all the histograms
	 * Loops through the histograms zeroing them in turn
	 */
	public void zeroAll() {
		msghdlr.messageOut("Zero All", MessageHandler.NEW);
		final Iterator allHistograms=Histogram.getHistogramList().iterator();
		while(allHistograms.hasNext()){
			final Histogram hist = ( (Histogram) allHistograms.next() );
			msghdlr.messageOut(" .", MessageHandler.CONTINUE);
			hist.setZero();
		}
		broadcaster.broadcast(BroadcastEvent.REFRESH);
		msghdlr.messageOut(" done!", MessageHandler.END);
	}
	
	/* (non-Javadoc)
	 * @see jam.data.control.DataControl#setup()
	 */
	public void setup() {
		//NOOP

	}

	/* (non-Javadoc)
	 * @see jam.data.control.DataControl#show()
	 */
	public void show() {
		dialogZero.show();
	}

}
