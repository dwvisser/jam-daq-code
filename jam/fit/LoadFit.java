package jam.fit;
import jam.JamException;
import jam.JamMain;
import jam.MainMenuBar; 
import jam.global.MessageHandler;
import jam.global.RTSI;
import jam.plot.Display;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Load a fit routine..
 * Draw the fit routines interface window
 *
 * @version 1.1
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public class LoadFit extends WindowAdapter implements ActionListener {

	private static final String OK="OK";
	private static final String APPLY="Apply";
	private static final String CANCEL="Cancel";

	private final JamMain jamMain;
	private final Display display;
	private final MessageHandler msgHandler;
	private final MainMenuBar menubar;

	private final JDialog dl;
	private final JComboBox chooseFit;
	
	/**
	 * Create the fit routine loading dialog.
	 *
	 * @param jm the main window
	 * @param d the histogram display
	 * @param mh the place to output text
	 */
	public LoadFit(JamMain jm, Display d, MessageHandler mh, 
	MainMenuBar mb) {
		super();
		this.jamMain = jm;
		this.display = d;
		this.msgHandler = mh;
		menubar=mb;
		final String dialogName="Load Fit Routine";
		dl = new JDialog(jamMain, dialogName, false);
		final Container cp = dl.getContentPane();
		dl.setResizable(false);
		final int posx=20;
		final int posy=50;
		dl.setLocation(posx, posy);
		cp.setLayout(new BorderLayout());
		final JPanel pf = new JPanel();// panel for fit file
		pf.setLayout(new FlowLayout(FlowLayout.CENTER));
		final JLabel lf = new JLabel("Pick a Fit class: ", JLabel.RIGHT);
		pf.add(lf);
		chooseFit = new JComboBox(this.getFitClasses());
		pf.add(chooseFit);
		final JPanel pb = new JPanel();// panel for buttons 
		pb.setLayout(new GridLayout(1,0));
		final JButton bok = new JButton(OK);
		pb.add(bok);
		bok.setActionCommand(OK);
		bok.addActionListener(this);
		final JButton bapply = new JButton(APPLY);
		pb.add(bapply);
		bapply.setActionCommand(APPLY);
		bapply.addActionListener(this);
		final JButton bcancel = new JButton(CANCEL);
		pb.add(bcancel);
		bcancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				dl.dispose();
			}
		});
		cp.add(pf,BorderLayout.CENTER);
		cp.add(pb,BorderLayout.SOUTH);
		dl.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dl.dispose();
			}
		});
		dl.pack();
	}

	/**
	 * Show the load fit routine dialog box
	 */
	public void showLoad() {
		dl.show();
	}

	/**
	 * Perform an action in the load FitRoutine dialog box. 
	 * Actions are:
	 * <ul>
	 * <li>OK</li>
	 * <li>Apply</li>
	 * <li>Cancel</li>
	 * </ul>
	 *
	 * @param ae notification ok, apply, or cancel 
	 */
	public void actionPerformed(ActionEvent ae) {
		final String command = ae.getActionCommand();
		try {
			if (OK.equals(command) || APPLY.equals(command)) {
				final Class fit = (Class)chooseFit.getSelectedItem();
				makeFit(fit);
				if (OK.equals(command)) {
					dl.dispose();
				}
			}
		} catch (JamException je) {
			msgHandler.errorOutln(je.getMessage());
		}
	}
	
	private void makeFit(Class fitClass) throws JamException {
	 	final String fitName=fitClass.getName();
		try {
			final Fit fit = (Fit) fitClass.newInstance();
			final int indexPeriod = fitName.lastIndexOf('.');
			final String fitNameFront = fitName.substring(indexPeriod + 1);
			fit.createDialog(jamMain, display, msgHandler);
			fit.show();
			menubar.addFit(new AbstractAction(fitNameFront) {
				public void actionPerformed(ActionEvent ae) {
					fit.show();
				}
			});
		} catch (InstantiationException ie) {
			throw new JamException(" Fit Class cannot instantize: " + fitName);
		} catch (IllegalAccessException iae) {
			throw new JamException(" Fit Class cannot Access: " + fitName);
		} catch (FitException fe) {
			fe.printStackTrace();
			throw new JamException(
				"FitException during makeFit(): " + fe.getMessage());
		}
	}
	
	private Object [] getFitClasses() {
		final String package1="jam.fit";
		final String package2="fit";
		final Set set = RTSI.find(package1, Fit.class,false);
		set.addAll(RTSI.find(package2, Fit.class,false));
		return set.toArray();
	}
}