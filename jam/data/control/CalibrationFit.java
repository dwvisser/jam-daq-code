package jam.data.control;
import jam.data.AbstractHist1D;
import jam.data.DataException;
import jam.data.Histogram;
import jam.data.func.CalibrationComboBoxModel;
import jam.data.func.CalibrationFunction;
import jam.data.func.CalibrationListCellRenderer;
import jam.data.func.LinearFunction;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;
import jam.global.BroadcastEvent;
import jam.io.hdf.HDFileFilter;
import jam.ui.MultipleFileChooser;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Observable;

/**
 * Class to control the histograms
 * Allows one to zero the histograms
 * and create new histograms
 *
 * @author Ken Swartz
 * @version 0.5
 */
public class CalibrationFit extends AbstractControl implements ActionListener {

    private static final int NUMBER_POINTS=5;
	private final static int MAX_NUMBER_TERMS = 5;
	private final static String BLANK_TITLE = " Histogram not calibrated ";
	private final static String BLANK_LABEL = "    --     ";	

    private final MessageHandler msghdlr;

    //calibrate histogram
    private AbstractHist1D currentHistogram;
    // calibration function
    private CalibrationFunction calibFunction=new LinearFunction();

    //GUI stuff
    JTabbedPane tabPane;
    private final JComboBox comboBoxFunction;
    private final JLabel lcalibEq=new JLabel("Select a function.", JLabel.CENTER);
    private JPanel pPoint [];
    private JTextField [] tEnergy, tChannel;
    private JCheckBox cUse[];
    
	private JPanel pcoeff[];
	private JLabel lcoeff[];
	private JTextField tcoeff[];    

    JRadioButton rbFitPoints;
    JRadioButton rbSetCoeffs;
	int numberTerms;
    private final NumberFormat numFormat;
	private final JButton bokCal =  new JButton("OK");
	private final JButton bapplyCal = new JButton("Apply");
	private final JButton bcancelCal =new JButton("Cancel");

    /**
     * Constructs a calibration fitting dialog.
     * 
     * @param mh the console for printing text output
     */
    public CalibrationFit(MessageHandler mh) {
        super("Calibration Fit",false);
        msghdlr=mh;
        setResizable(false);
        setLocation(30,30);
        final Container cdialogCalib=getContentPane();
        cdialogCalib.setLayout(new BorderLayout(5, 5));       
        
        //Selection panel at the top
        JPanel pSelection = new JPanel(new GridLayout(0,1,5,0));
        pSelection.setBorder(new EmptyBorder(10,0,0,0));
        cdialogCalib.add(pSelection, BorderLayout.NORTH);
        
        //Equation chooser
        JPanel pChoose = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        pChoose.add(new JLabel("Function: "));
        comboBoxFunction = new JComboBox(new CalibrationComboBoxModel());
        comboBoxFunction.setRenderer(new CalibrationListCellRenderer());
		comboBoxFunction.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent ie){
				final String calClass = (String)comboBoxFunction.getSelectedItem();
				try {
					calibFunction = (CalibrationFunction)Class.forName(calClass).newInstance();
					updateCoefficients();
				} catch (Exception e) {
					msghdlr.errorOutln(getClass().getName()+
					".setFunctionType(): "+e.toString());            	
				} 
				lcalibEq.setText(calibFunction.getTitle());
			}
		});
		comboBoxFunction.setSelectedItem(LinearFunction.class.getName());
        pChoose.add(comboBoxFunction);
        pSelection.add(pChoose);
        
        //Equation 
        JPanel pEquation = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pSelection.add(lcalibEq);        
        
        //Fit using points or coeffs 
        final JPanel pFitType = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        final ButtonGroup gFitType=new ButtonGroup();
        rbFitPoints = new JRadioButton("Fit Points", true);
        rbFitPoints.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				if (rbFitPoints.isSelected()) {
					setFitTypePoints(true);					
				}
			}
		});
        gFitType.add(rbFitPoints);        
        pFitType.add(rbFitPoints);
        rbSetCoeffs = new JRadioButton("Set Coefficients", false);
        rbSetCoeffs.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				if (rbSetCoeffs.isSelected()) {
					setFitTypePoints(false);
				}
			}
		});
        
        gFitType.add(rbSetCoeffs);
        pFitType.add(rbSetCoeffs);        
        pSelection.add(pFitType);
        
		//Tabbed panel with fit points an coefficients
		tabPane = new JTabbedPane();
		cdialogCalib.add(tabPane, BorderLayout.CENTER);
		JPanel ptsPanel = createPointsPanel();
		tabPane.addTab("Points", null, ptsPanel, "Channels and Energies to fit.");		
		JPanel histPanel = createCoeffPanel();
		tabPane.addTab("Coefficients", null, histPanel, "Fit coefficients.");		
		tabPane.addChangeListener(new ChangeListener() {
	        // This method is called whenever the selected tab changes
	        public void stateChanged(ChangeEvent evt) {
	            final JTabbedPane pane = (JTabbedPane)evt.getSource();
	            //changeSelectedTab(pane.getSelectedIndex());
	        }
	    });		
        
        
		/* button panel */
        final PanelOKApplyCancelButtons pButtons = new PanelOKApplyCancelButtons(
                new PanelOKApplyCancelButtons.DefaultListener(this) {

                    public void apply() {
                        if(calibFunction==null){
                            msghdlr.errorOutln("Need to choose a function [CalibrationFit]");
                        } else {
                            doFitCalibration();
                        }
                    }
                    public void cancel() {
                    	cancelCalib();
                    }
                });
        cdialogCalib.add(pButtons.getComponent(), BorderLayout.SOUTH);
		pack();
		
		//Initially points
		rbFitPoints.setSelected(true);
		
		/*
        JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel pbCal= new JPanel();
        pbCal.setLayout(new GridLayout(1,0,5,5));
        pbutton.add(pbCal);
        //cdialogCalib.add(pbutton, BorderLayout.SOUTH);

        bokCal.setActionCommand("okcalib");
        bokCal.addActionListener(this);
        pbCal.add(bokCal);
        bapplyCal.setActionCommand("applycalib");
        bapplyCal.addActionListener(this);
        pbCal.add(bapplyCal);
        bcancelCal.setActionCommand("cancelcalib");
        bcancelCal.addActionListener(this);
        pbCal.add(bcancelCal);
        pack();
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                dispose();
            }
			public void windowActivated(WindowEvent e) {
				doSetup();
			}
        });
        */
        //formating enery output
        numFormat=NumberFormat.getInstance();
        numFormat.setGroupingUsed(false);
        numFormat.setMinimumFractionDigits(4);
        numFormat.setMaximumFractionDigits(4);
    }

    //Create panel with the points to fit
    private JPanel createPointsPanel() {
    	JPanel pAllPoints = new JPanel(new GridLayout(0, 1, 10,2));
        pPoint=new JPanel[NUMBER_POINTS];
        tEnergy =new JTextField[NUMBER_POINTS];
        tChannel =new JTextField[NUMBER_POINTS];
        cUse =new JCheckBox[NUMBER_POINTS];
        for ( int i=0; i<NUMBER_POINTS; i++ ){
            pPoint[i]=new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
            pAllPoints.add(pPoint[i]);
            pPoint[i].add(new JLabel("Energy"));
            tEnergy[i] =new JTextField("");
            tEnergy[i].setColumns(6);
            pPoint[i].add(tEnergy[i]);
            pPoint[i].add(new JLabel("Channel"));
            tChannel[i] =new JTextField("");
            tChannel[i].setColumns(6);
            pPoint[i].add(tChannel[i]);
            cUse[i] =new JCheckBox("use");
            cUse[i].setSelected(true);
            pPoint[i].add(cUse[i]);
            //cUse[i].addItemListener(this);
            final int index=i;//silly, but necessary for anonymous class
            cUse[i].addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent ie){
					setPointFieldActive(index, cUse[index].isSelected());
				}				
            });
        }
        return pAllPoints;

    }
    
    //Create panel with the coefficients    
    private JPanel createCoeffPanel() {    
    	JPanel pCoeff = new JPanel(new GridLayout(0, 1, 10,2));
		pcoeff = new JPanel[MAX_NUMBER_TERMS];
		lcoeff = new JLabel[MAX_NUMBER_TERMS];
		tcoeff = new JTextField[MAX_NUMBER_TERMS];
	
		for (int i = 0; i < MAX_NUMBER_TERMS; i++) {
			pcoeff[i] = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
			pCoeff.add(pcoeff[i]);
			lcoeff[i] = new JLabel(BLANK_LABEL, JLabel.RIGHT);
			pcoeff[i].add(lcoeff[i]);
			tcoeff[i] = new JTextField("");
			tcoeff[i].setColumns(10);
			pcoeff[i].add(tcoeff[i]);
		}
		return pCoeff;
    }
    
    private void setFitTypePoints(boolean state){
    	if (state) {
    		tabPane.setSelectedIndex(0);
    	} else {
    		tabPane.setSelectedIndex(1);
    	}
    	for (int i =0; i<NUMBER_POINTS; i++) {    	
    		tEnergy[i].setEnabled(state);
    		tChannel[i].setEnabled(state);    		
    		cUse[i].setEnabled(state);    		
    	}
    	for (int i =0; i<MAX_NUMBER_TERMS; i++) {
    		tcoeff[i].setEnabled(!state);
    	}		
    }
    
	/**
	 *sets fields to be active
	 */
	private void setPointFieldActive(int number, boolean state){
		tChannel[number].setEnabled(state);
		tEnergy[number].setEnabled(state);
	}
    
    /**
     * Receive actions from Dialog Boxes
     *
     */
    public void actionPerformed(ActionEvent ae){
        String command=ae.getActionCommand();
        currentHistogram=getCurrentHistogram();
            //commands for calibration
            if ((command=="okcalib")||(command=="applycalib")) {
                if(calibFunction==null){
                    msghdlr.errorOutln("Need to choose a function [CalibrationFit]");
                } else {
                    doFitCalibration();
                    if (command=="okcalib") {
                        dispose();
                    }
                }
            } else if (command=="cancelcalib") {
                cancelCalib();
                msghdlr.messageOutln("Uncalibrated histogram "+currentHistogram.getFullName());
                dispose();
            } else {
                //just so at least a exception is thrown for now
                throw new UnsupportedOperationException("Unregonized command: "+command);
            }
    }

    /**
     * Call the fitting method of the chosen function to calibrate a histogram
     */
    private void doFitCalibration(){
    	
        double energy [] =new double[NUMBER_POINTS];
        double channel [] =new double[NUMBER_POINTS];
        int numberPoints=0;
        double x[];
        double y[];
        String fitText;

        if(calibFunction==null){
        	msghdlr.errorOutln("Need to choose a function");
        	return;
        } 
        
        AbstractHist1D currentHist=getCurrentHistogram();
        if (currentHist==null){//silently ignore if histogram null
            msghdlr.errorOutln("null histogram [Calibrate]");
        }
        try {
            for(int i=0;i<NUMBER_POINTS;i++){
                //is entry marked
                if(cUse[i].isSelected()) {
                    //is there text in enery field
                    if (tEnergy[i].getText().trim().length()!=0){
                        energy[numberPoints]=( new Double(tEnergy[i].getText()) ).doubleValue();
                        channel[numberPoints]=( new Double(tChannel[i].getText()) ).doubleValue();
                        numberPoints++;
                    }
                }
            }
            if(numberPoints>=2){
                //setFunctionType();//ensures a fresh CalibrationFit instance is created
                x=new double [numberPoints];
                y=new double [numberPoints];
                //put valid energy and channel into arrays
                for (int i=0;i<numberPoints;i++){
                    y[i]=energy[i];
                    x[i]=channel[i];
                }
                calibFunction.fit(x,y);
                fitText=calibFunction.getFormula();
                currentHist.setCalibration(calibFunction);
                BROADCASTER.broadcast(BroadcastEvent.Command.REFRESH);
                msghdlr.messageOutln("Calibrated histogram "+currentHist.getFullName().trim()+" with "+
                fitText);
            } else {
                msghdlr.errorOutln("Need at least 2 points [Calibrate]");
            }
        } catch (NumberFormatException nfe){
            msghdlr.errorOutln("Invalid input, not a number [Calibrate]");
        } catch (DataException de) {
            msghdlr.errorOutln(de.getMessage());
        } 
    }
    
	/**
	 * set the calibration coefficients for a histogram
	 */
	private void setCoefficients() {
		double coeff[] = new double[numberTerms];
		int i = 0;
		CalibrationFunction calibFunction = currentHistogram.getCalibration();
		/* silently ignore if histogram null */
		if (calibFunction != null) {
			try {
				for (i = 0; i < numberTerms; i++) {
					coeff[i] = (new Double(tcoeff[i].getText())).doubleValue();
				}
				calibFunction.setCoeff(coeff);
			} catch (NumberFormatException nfe) {
				msghdlr.errorOutln("Invalid input, coefficient "
						+ calibFunction.getLabels()[i]);
			}
			doSetup();
		} else {
			msghdlr
					.errorOutln("Calibration function not defined [CalibrationDisplay]");
		}
	}
    
	/**
	 * setups up the dialog box
	 */
	public void updateCoefficients() {
		CalibrationFunction hcf=null;
		final boolean isHist1d = currentHistogram != null
				&& currentHistogram.getDimensionality() == 1;
		
		if (currentHistogram!=null)
			hcf = currentHistogram.getCalibration();
		
		final boolean exists = isHist1d && hcf != null;
		//comboBoxFunction.setEnabled(exists);
		if (exists) {
			numberTerms = hcf.getNumberTerms();
			lcalibEq.setText(hcf.getTitle());
			String[] labels = hcf.getLabels();
			double[] coeff = hcf.getCoeff();
			for (int i = 0; i < numberTerms; i++) {
				lcoeff[i].setText(labels[i]);
				tcoeff[i].setText(String.valueOf(coeff[i]));
				tcoeff[i].setEnabled(true);
			}
			for (int i = numberTerms; i < MAX_NUMBER_TERMS; i++) {
				lcoeff[i].setText(BLANK_LABEL);
				tcoeff[i].setText("");
				tcoeff[i].setEnabled(false);
			}
		} else {// histogram not calibrated
			//lcalibEq.setText(BLANK_TITLE);
			for (int i = 0; i < MAX_NUMBER_TERMS; i++) {
				lcoeff[i].setText(BLANK_LABEL);
				tcoeff[i].setText("");
				tcoeff[i].setEnabled(false);
			}
		}
	}

	private AbstractHist1D getCurrentHistogram() {
		final AbstractHist1D rval;
		final Histogram hist = STATUS.getCurrentHistogram();
		if (hist != currentHistogram && hist instanceof AbstractHist1D) {
			rval = (AbstractHist1D) hist;
		} else {
			rval=null;
		}
		return rval;
	}

    /**
     * cancel the histogram calibration
     *
     */
    private void cancelCalib() {
        final AbstractHist1D currentHist=getCurrentHistogram();
        if (currentHist != null){
        	currentHist.setCalibration(null);
        }
        BROADCASTER.broadcast(BroadcastEvent.Command.REFRESH);
    }

    public void doSetup(){
    	CalibrationFunction hcf=null;
		final AbstractHist1D hist=getCurrentHistogram();
		if (hist!=currentHistogram){
			currentHistogram=hist;
		} 
		final boolean histIs1d = currentHistogram!=null && 
		currentHistogram.getDimensionality()==1;
		if(histIs1d) {
			hcf=currentHistogram.getCalibration();
			final String name= hcf==null ? null : hcf.getClass().getName();
			comboBoxFunction.setSelectedItem(name);
		}	 
		//comboBoxFunction.setEnabled(histIs1d);
		bokCal.setEnabled(histIs1d);
		bapplyCal.setEnabled(histIs1d);
		bcancelCal.setEnabled(histIs1d);		
		
		//Points fields
		for (int i=0; i<NUMBER_POINTS; i++){
			final boolean enable=histIs1d && cUse[i].isSelected();
			//tChannel[i].setText(enable);
		}
		final boolean exists = histIs1d && hcf != null;
		
		//Coefficient fields
		if (exists) {
			numberTerms = hcf.getNumberTerms();
			lcalibEq.setText(hcf.getTitle());
			String[] labels = hcf.getLabels();
			double[] coeff = hcf.getCoeff();
			for (int i = 0; i < MAX_NUMBER_TERMS; i++) {
				if (i<numberTerms) {
					lcoeff[i].setText(labels[i]);
					tcoeff[i].setText(String.valueOf(coeff[i]));
					tcoeff[i].setEnabled(true);
				} else {
					lcoeff[i].setText(BLANK_LABEL);
					tcoeff[i].setText("");
					tcoeff[i].setEnabled(false);					
				}
			}
		} else {// histogram not calibrated
			//lcalibEq.setText(BLANK_TITLE);
			for (int i = 0; i < MAX_NUMBER_TERMS; i++) {
				lcoeff[i].setText(BLANK_LABEL);
				tcoeff[i].setText("");
				tcoeff[i].setEnabled(false);
			}
		}
		
    }
    public void update(Observable observable, Object object) {
    	
    	final BroadcastEvent be = (BroadcastEvent) object;
		final BroadcastEvent.Command com=be.getCommand();
		if (com == BroadcastEvent.Command.HISTOGRAM_SELECT ||
			com == BroadcastEvent.Command.HISTOGRAM_ADD) {
			doSetup();     	            
			updateCoefficients();
            //cancel(); //cancel current gate if was setting
		}
	}    
    
    
}
