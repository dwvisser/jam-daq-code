package jam.fit;
import jam.JamException;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.RTSI;
import jam.plot.Display;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


/**
 * Load a fit routine..
 * Draw the fit routines interface window
 *
 * @version 1.1
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public class LoadFit {

	private final Frame jamMain;
	private final Display display;
	private final Broadcaster broadcaster;	

	private final JDialog dl;

	/**
	 * Create the fit routine loading dialog.
	 */
	public LoadFit() {
		super();
		broadcaster=Broadcaster.getSingletonInstance();		
		final JamStatus jamStatus = JamStatus.getSingletonInstance(); 
		final MessageHandler msgHandler = jamStatus.getMessageHandler();
		jamMain = jamStatus.getFrame();
		display = jamStatus.getDisplay();
		final String dialogName="Load Fit Routine";
		dl = new JDialog(jamMain, dialogName, false);
		final Container cp = dl.getContentPane();
		dl.setResizable(false);
		final int posx=20;
		final int posy=50;
		dl.setLocation(posx, posy);
		cp.setLayout(new BorderLayout());
		// panel for fit file
		final JPanel pf = new JPanel(new FlowLayout(FlowLayout.CENTER));
		Border border = new EmptyBorder(20,20,20,20);
		pf.setBorder(border);
		final JLabel lf = new JLabel("Fit class: ", JLabel.RIGHT);
		pf.add(lf);
		final JComboBox chooseFit = new JComboBox(this.getFitClasses());
		Dimension dim = chooseFit.getPreferredSize();
		dim.width=200;
		chooseFit.setPreferredSize(dim);
		pf.add(chooseFit);
		final PanelOKApplyCancelButtons.Callback callback = new 
		PanelOKApplyCancelButtons.Callback(){
		    public void ok(){
		        apply();
		        dl.dispose();
		    }
		    
		    public void apply(){
				final Class fit = (Class)chooseFit.getSelectedItem();
				try {
				    makeFit(fit);
				} catch (JamException je){
				    msgHandler.errorOutln(je.getMessage());
				    je.printStackTrace();
				}
		    }
		    
		    public void cancel(){
		        dl.dispose();
		    }
		};
		final PanelOKApplyCancelButtons buttons=new PanelOKApplyCancelButtons(callback);
		cp.add(pf,BorderLayout.CENTER);
		cp.add(buttons.getComponent(),BorderLayout.SOUTH);
		dl.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dl.pack();
	}

	/**
	 * Show the load fit routine dialog box
	 */
	public void showLoad() {
		dl.setVisible(true);
	}

	private void makeFit(Class fitClass) throws JamException {
	 	final String fitName=fitClass.getName();
		try {
			
			final AbstractFit fit = (AbstractFit) fitClass.newInstance();
			final int indexPeriod = fitName.lastIndexOf('.');
			final String fitNameFront = fitName.substring(indexPeriod + 1);
			fit.createDialog(jamMain, display);
			fit.show();
			//Create action for menu
			final Action fitAction = new AbstractAction(fitNameFront) {
				public void actionPerformed(ActionEvent ae) {
					fit.show();
				}
			}; 
			broadcaster.broadcast(BroadcastEvent.Command.FIT_NEW, fitAction);
		} catch (InstantiationException ie) {
			throw new JamException(" Fit Class cannot instantize: " + fitName, ie);
		} catch (IllegalAccessException iae) {
			throw new JamException(" Fit Class cannot Access: " + fitName, iae);
		} 
	}

	private Object [] getFitClasses() {
		final String package1="jam.fit";
		final String package2="fit";
		final Set set = RTSI.find(package1, AbstractFit.class,false);
		set.addAll(RTSI.find(package2, AbstractFit.class,false));
		return set.toArray();
	}
}