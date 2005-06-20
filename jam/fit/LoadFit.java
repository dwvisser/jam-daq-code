package jam.fit;
import jam.JamException;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.RTSI;
import jam.plot.PlotDisplay;
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
	private final PlotDisplay display;
	private final Broadcaster broadcaster;	

	private final JDialog dialog;

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
		dialog = new JDialog(jamMain, dialogName, false);
		final Container contents = dialog.getContentPane();
		dialog.setResizable(false);
		final int posx=20;
		final int posy=50;
		dialog.setLocation(posx, posy);
		contents.setLayout(new BorderLayout());
		// panel for fit file
		final JPanel pFit = new JPanel(new FlowLayout(FlowLayout.CENTER));
		Border border = new EmptyBorder(20,20,20,20);
		pFit.setBorder(border);
		final JLabel lFit = new JLabel("Fit class: ", JLabel.RIGHT);
		pFit.add(lFit);
		final JComboBox chooseFit = new JComboBox(this.getFitClasses());
		Dimension dim = chooseFit.getPreferredSize();
		dim.width=200;
		chooseFit.setPreferredSize(dim);
		pFit.add(chooseFit);
		final PanelOKApplyCancelButtons.Listener callback = new 
		PanelOKApplyCancelButtons.DefaultListener(dialog){
		    public void apply(){
				final Class fit = (Class)chooseFit.getSelectedItem();
				try {
				    makeFit(fit);
				} catch (JamException je){
				    msgHandler.errorOutln(je.getMessage());
				    je.printStackTrace();
				}
		    }
		};
		final PanelOKApplyCancelButtons buttons=new PanelOKApplyCancelButtons(callback);
		contents.add(pFit,BorderLayout.CENTER);
		contents.add(buttons.getComponent(),BorderLayout.SOUTH);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
	}

	/**
	 * Show the load fit routine dialog box
	 */
	public void showLoad() {
		dialog.setVisible(true);
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
		final Set<Class<?>> set = RTSI.find(package1, AbstractFit.class,false);
		set.addAll(RTSI.find(package2, AbstractFit.class,false));
		return set.toArray();
	}
}


