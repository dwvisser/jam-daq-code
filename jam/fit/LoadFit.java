package jam.fit;
import jam.*;
import jam.global.*;
import jam.plot.Display;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.lang.reflect.Modifier;

import javax.swing.*;

/**
 * Load a fit routine..
 * Draw the fit routines interface window
 *
 */
public class LoadFit extends WindowAdapter implements ActionListener {

	final static String DEFAULT_FIT = "jam.fit.GaussianFit";

	private JamMain jamMain;
	private Display display;
	private MessageHandler msgHandler;

	/** 
	 * list of the fitting classes added to the menu
	 */
	private Hashtable fitList = new Hashtable(3);

	/** 
	 * current fit class 
	 */
	private Fit fitClass;

	private JDialog dl;
	private String fitDirectory;
	private JComboBox chooseFit;

	public LoadFit(JamMain jamMain, Display display, MessageHandler console) {
		this.jamMain = jamMain;
		this.display = display;
		this.msgHandler = console;
		//dialog box loading a fit class
		dl = new JDialog(jamMain, "Load Fit class ", false);
		Container cp = dl.getContentPane();
		dl.setForeground(Color.black);
		dl.setBackground(Color.lightGray);
		dl.setResizable(false);
		dl.setLocation(20, 50);
		dl.setSize(400, 150);
		cp.setLayout(new BorderLayout());
		// panel for fit file
		JPanel pf = new JPanel();
		pf.setLayout(new FlowLayout(FlowLayout.CENTER));
		JLabel lf = new JLabel("Pick a Fit class: ", Label.RIGHT);
		pf.add(lf);
		chooseFit = new JComboBox(this.getFitClassNames());
		pf.add(chooseFit);
		// panel for buttons 				
		JPanel pb = new JPanel();
		pb.setLayout(new GridLayout(1,0));
		JButton bok = new JButton("OK");
		pb.add(bok);
		bok.setActionCommand("ok");
		bok.addActionListener(this);
		JButton bapply = new JButton("Apply");
		pb.add(bapply);
		bapply.setActionCommand("apply");
		bapply.addActionListener(this);
		JButton bcancel = new JButton("Cancel");
		pb.add(bcancel);
		bcancel.setActionCommand("cancel");
		bcancel.addActionListener(this);
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
	 * <li>Browse</li>
	 * </ul>
	 */
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();
		String fitName;
		try {
			if (command == "ok" || command == "apply") {
				fitName = (String)chooseFit.getSelectedItem();
				makeFit(fitName);
				if (command == "ok") {
					dl.dispose();
				}
			} else if (command == "cancel") {
				dl.dispose();
			} else {
				showFitDialog(command);
			}
		} catch (JamException je) {
			msgHandler.errorOutln(je.getMessage());
		}
	}
	
	/**
	 * Show a loaded fit dialog box
	 */
	public void showFitDialog(String fitName) {
		((Fit) fitList.get(fitName)).show();
	}

	/**
	  * Is the Browse for the fit class file 
	  * which showed be in ../jam/fit subdirectory
	  * part of the <code>sort</code> Package
	  *
	  * @author Ken Swartz
	  */
	private String getFitFile() {
		String msg = "Load Fit file";
		int state = FileDialog.LOAD;
		FileDialog fd = new FileDialog(jamMain, msg, state);
		fd.setFile("*.class");
		if (fitDirectory != null) {
			fd.setDirectory(fitDirectory);
		}
		fd.show();
		//save current values
		fitDirectory = fd.getDirectory(); //save current directory
		String fitName = fd.getFile();
		fd.dispose();
		return fitName;
	}

	/**
	 * Load a fit routine.
	 */
	private void makeFit(String _fitName) throws JamException {
		int indexPeriod;

		try {
			// create fit class
			fitClass = (Fit) Class.forName(_fitName).newInstance();
		} catch (ClassNotFoundException ce) {
			fitClass = null;
			throw new JamException(" Fit Class not found : " + _fitName);
		} catch (InstantiationException ie) {
			fitClass = null;
			throw new JamException(" Fit Class cannot instantize: " + _fitName);
		} catch (IllegalAccessException iae) {
			fitClass = null;
			throw new JamException(" Fit Class cannot Access: " + _fitName);
		}
		//add fit function to menu
		indexPeriod = _fitName.lastIndexOf(".");
		String fitNameFront = _fitName.substring(indexPeriod + 1);
		fitList.put(fitNameFront, fitClass);
		jamMain.addFit(fitNameFront);
		try {
			fitClass.createDialog((Frame) jamMain, display, msgHandler);
		} catch (FitException fe) {
			fitClass = null;
			fe.printStackTrace();
			throw new JamException(
				"FitException during makeFit(): " + fe.getMessage());
		}
		fitClass.show();
	}
	
	private Object [] getFitClassNames() {
		//Class temp=null;
		Set set = RTSI.find("jam.fit", Fit.class,false);
		set.addAll(RTSI.find("fit", Fit.class,false));
		Set abstractClasses=new HashSet();
		for (Iterator it=set.iterator(); it.hasNext(); ){
			Class temp=(Class)it.next();
			boolean isAbstract = !((temp.getModifiers() & Modifier.ABSTRACT) == 0);
			if (isAbstract){
				abstractClasses.add(temp);
			}
		}
		set.removeAll(abstractClasses);
		Collection rval=new HashSet();
		for (Iterator i=set.iterator(); i.hasNext(); ) {
			rval.add(((Class)i.next()).getName());
		}
		return rval.toArray();
	}
}