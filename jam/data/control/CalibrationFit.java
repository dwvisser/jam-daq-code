package jam.data.control;
import jam.data.AbstractHist1D;
import jam.data.DataException;
import jam.data.Histogram;
import jam.data.func.AbstractCalibrationFunction;
import jam.data.func.CalibrationComboBoxModel;
import jam.data.func.CalibrationListCellRenderer;
import jam.data.func.LinearFunction;
import jam.data.func.SqrtEnergyFunction;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Observable;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Class to control the histograms
 * Allows one to zero the histograms
 * and create new histograms
 *
 * @author Ken Swartz
 * @version 0.5
 */
public class CalibrationFit extends AbstractControl {

    private static final int NUM_POINTS=5;
	private final static int MAX_TERMS = 5;
	private final static String BLANK_TITLE = "Histogram not calibrated";
	private final static String BLANK_LABEL = "    --     ";	
	
	//Avaliable functions load icons
	static {
		final ClassLoader loader = ClassLoader.getSystemClassLoader();
		AbstractCalibrationFunction linearFunc=new LinearFunction();
		AbstractCalibrationFunction sqrtEFunc=new SqrtEnergyFunction();
		/* Load icons. */
		URL urlLine=loader.getResource("jam/data/func/line.png");
		URL urlSqrtE =loader.getResource("jam/data/func/sqrt.png");
		if (urlLine!=null && urlSqrtE!=null) {
			AbstractCalibrationFunction.setIcon(linearFunc.getName(), new ImageIcon(urlLine));
			AbstractCalibrationFunction.setIcon(sqrtEFunc.getName(), new ImageIcon(urlSqrtE));
		} else {
			JOptionPane.showMessageDialog(null, "Can't load resource function icons");
		}
	}
	    
    // calibration function
    private AbstractCalibrationFunction calibrationFunction=null;

    //GUI stuff
    //Tabbed for fit type
    JTabbedPane tabPane;
    //Chooser for function type
    private final JComboBox comboBoxFunction;
    private final JLabel lcalibEq;
	//Radio buttons for fit type
    JRadioButton rbFitPoints;
    JRadioButton rbSetCoeffs;
    //Points panel
    private JPanel pPoint [];
    private JTextField [] tEnergy;
    private JTextField [] tChannel;
    private JCheckBox cUse[];
    //Coeff panel
	private JPanel pcoeff[];
	private JLabel lcoeff[];
	private JTextField tcoeff[];
	PanelOKApplyCancelButtons pButtons;
	
	int numberTerms;
	boolean isUpdate;
	
    private final NumberFormat numFormat;
    
    private final MessageHandler msghdlr;
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
				selectionChange();
			}
		});
        pChoose.add(comboBoxFunction);
        pSelection.add(pChoose);
        
        //Equation 
        JPanel pEquation = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        lcalibEq=new JLabel(BLANK_TITLE, JLabel.CENTER);
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
        pButtons = new PanelOKApplyCancelButtons(
                new PanelOKApplyCancelButtons.DefaultListener(this) {
                    public void apply() {
                    	if (rbFitPoints.isSelected()) {
                    		doApplyCalib();
                    	}else{
                    		doSetCoefficients();
                    	}
                    }
                    public void cancel() {
                    	doCancelCalib();
                    }
                });
        cdialogCalib.add(pButtons.getComponent(), BorderLayout.SOUTH);
		pack();
				
        //formating enery output
        numFormat=NumberFormat.getInstance();
        numFormat.setGroupingUsed(false);
        numFormat.setMinimumFractionDigits(4);
        numFormat.setMaximumFractionDigits(4);
        
		//Initially selections
        isUpdate=false;
//        comboBoxFunction.setSelectedIndex(0);
		rbFitPoints.setSelected(true);
        
    }

    //Create panel with the points to fit
    private JPanel createPointsPanel() {
    	JPanel pAllPoints = new JPanel(new GridLayout(0, 1, 10,2));
        pPoint=new JPanel[NUM_POINTS];
        tEnergy =new JTextField[NUM_POINTS];
        tChannel =new JTextField[NUM_POINTS];
        cUse =new JCheckBox[NUM_POINTS];
        for ( int i=0; i<NUM_POINTS; i++ ){
            pPoint[i]=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
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
		pcoeff = new JPanel[MAX_TERMS];
		lcoeff = new JLabel[MAX_TERMS];
		tcoeff = new JTextField[MAX_TERMS];
	
		for (int i = 0; i < MAX_TERMS; i++) {
			pcoeff[i] = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
			pCoeff.add(pcoeff[i]);
			lcoeff[i] = new JLabel(BLANK_LABEL, JLabel.RIGHT);
			pcoeff[i].add(lcoeff[i]);
			tcoeff[i] = new JTextField("");
			tcoeff[i].setColumns(10);
			pcoeff[i].add(tcoeff[i]);
		}
		return pCoeff;
    }
    
    private void selectionChange() {
		final String funcName = (String)comboBoxFunction.getSelectedItem();    	
    	if(!isUpdate) {
    		selectFunction(funcName);
    	}
    }
    /*
     * Function selected
     */
    private void selectFunction(String funcName) {
        Class calClass;
        try {
            calibrationFunction = getCurrentCalibrationFunction();
            if (!funcName.equals(AbstractCalibrationFunction.NOT_CALIB)) {
                calClass = (Class) AbstractCalibrationFunction
                        .getMapFunctions().get(funcName);
                boolean change = calClass.isInstance(calibrationFunction);
                if (calibrationFunction == null || !change) {
                    calibrationFunction = (AbstractCalibrationFunction) calClass
                            .newInstance();
                }
            } else {
                calibrationFunction = null;
            }
            updateFields(calibrationFunction, rbFitPoints.isSelected());
        } catch (InstantiationException e) {
            msghdlr.errorOutln("Creating fit function " + getClass().getName()
                    + " " + e.toString());
        } catch (IllegalAccessException e) {
            msghdlr.errorOutln("Creating fit function " + getClass().getName()
                    + " " + e.toString());
        }
    }
    
    /*
     * Set the fit type to be points instead of setting coefficients.
     */
    private void setFitTypePoints(boolean state) {
        if (state) {
            tabPane.setSelectedIndex(0);
        } else {
            tabPane.setSelectedIndex(1);
        }
        updateFields(calibrationFunction, state);
    }        
    
    private void updateSelection() {
        final String name;
        boolean isCalPts = true;
        calibrationFunction = getCurrentCalibrationFunction();
        /* Select name. */
        if (calibrationFunction == null) {
            name = AbstractCalibrationFunction.NOT_CALIB;
            rbFitPoints.setSelected(true);
        } else {
            name = calibrationFunction.getName();
            /* Set fit type. */
            if (calibrationFunction.isFitPoints()) {
                isCalPts = true;
                rbFitPoints.setSelected(true);
            } else {
                isCalPts = false;
                rbSetCoeffs.setSelected(true);
            }
        }
        //Change isUpdate state so we dont loop on item Change event
        isUpdate = true;
        comboBoxFunction.setSelectedItem(name);
        isUpdate = false;
        updateFields(calibrationFunction, isCalPts);
    }

    public void doSetup() {
        calibrationFunction = getCurrentCalibrationFunction();
        final boolean isCalPts = calibrationFunction == null ? true
                : calibrationFunction.isFitPoints();
        updateFields(calibrationFunction, isCalPts);
    }
    
    private void doApplyCalib(){
    	AbstractHist1D currentHistogram=getCurrentHistogram();
    	if (currentHistogram != null && calibrationFunction==null){
    		currentHistogram.setCalibration(null);
            msghdlr.messageOutln("Uncalibrated histogram "+currentHistogram.getFullName());
    	} else {
            if (currentHistogram==null){
                msghdlr.errorOutln("Need a 1 Dimension histogram");
            }
    		doFitCalibration();
    	}
    }

    /**
     * cancel the histogram calibration
     *
     */
    private void doCancelCalib() {
    	this.dispose();
        BROADCASTER.broadcast(BroadcastEvent.Command.REFRESH);
    }
    
	/**
	 *sets fields to be active
	 */
	private void setPointFieldActive(int number, boolean state){
		tChannel[number].setEnabled(state);
		tEnergy[number].setEnabled(state);
	}
    
    /**
     * Call the fitting method of the chosen function to calibrate a histogram
     * Already checked have a 1d histogram and a fit function 
     */
    private void doFitCalibration(){
        double energy [] =new double[NUM_POINTS];
        double channel [] =new double[NUM_POINTS];
        int numberPoints=0;
        double x[];
        double y[];
        String fitText;
        AbstractHist1D currentHistogram=getCurrentHistogram();
        try {
            for(int i=0;i<NUM_POINTS;i++){
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
                calibrationFunction.setPoints(x,y);
                calibrationFunction.fit();                
                fitText=calibrationFunction.getFormula();
                currentHistogram.setCalibration(calibrationFunction);
                BROADCASTER.broadcast(BroadcastEvent.Command.REFRESH);
                msghdlr.messageOutln("Calibrated histogram "+currentHistogram.getFullName().trim()+" with "+
                fitText);
            } else {
                msghdlr.errorOutln("Need at least 2 points");
            }
        } catch (NumberFormatException nfe){
        	currentHistogram.setCalibration(null);	//Make sure hisogram no longer has calibration
            msghdlr.errorOutln("Invalid input, not a number");
        } catch (DataException de) {
        	currentHistogram.setCalibration(null); //Make sure hisogram no longer has calibration       	
            msghdlr.errorOutln(de.getMessage());
        } 
    }
    
	/**
	 * set the calibration coefficients for a histogram
	 */
	private void doSetCoefficients() {
		double coeff[] = new double[numberTerms];
		int i = 0;
		AbstractHist1D currentHistogram=getCurrentHistogram();
 
		/* silently ignore if histogram null */
		if (calibrationFunction != null) {
			try {
				for (i = 0; i < numberTerms; i++) {
					coeff[i] = (new Double(tcoeff[i].getText())).doubleValue();
				}
				calibrationFunction.setCoeff(coeff);
                currentHistogram.setCalibration(calibrationFunction);
                BROADCASTER.broadcast(BroadcastEvent.Command.REFRESH);
                msghdlr.messageOutln("Calibrated histogram "+currentHistogram.getFullName().trim()+" with "+
                		calibrationFunction.getFormula());
				
			} catch (NumberFormatException nfe) {
				msghdlr.errorOutln("Invalid input, coefficient "
						+ calibrationFunction.getLabels()[i]);
			}
		} else {
			msghdlr.errorOutln("Calibration function not defined.");
		}
	}

	
	/**
     * Sets inputs fields, enables and diables them as appropricate depending of
     * if there is a calibration or what type it is.
     * 
     * @param hcf
     *            calibration, if any
     * @param isCalPts
     *            if <code>true</code>, calibration was fit to points
     */
	private void updateFields(AbstractCalibrationFunction hcf, boolean isCalPts) {		
		final boolean calNotNull = hcf != null;
        final String title = calNotNull ? hcf.getTitle() : BLANK_TITLE;
		lcalibEq.setText(title);	 
		/* Points fields */
		if (calNotNull) {
			final double [] ptsChannel=hcf.getPtsChannel();
			final double [] ptsEnergy=hcf.getPtsEnergy();
			numberTerms = hcf.getNumberTerms();
			final String[] labels = hcf.getLabels();
			final double[] coeff = hcf.getCoeff();			
			/* Calibrated with points */
			if (isCalPts) {
				for (int i=0; i<NUM_POINTS; i++){
					if (i<ptsChannel.length) {
						tChannel[i].setText(String.valueOf(ptsChannel[i]));
						tEnergy[i].setText(String.valueOf(ptsEnergy[i]));
					}else {
						tChannel[i].setText("");
						tEnergy[i].setText("");						
					}
					tChannel[i].setEditable(true);
					tChannel[i].setEnabled(true);
					tEnergy[i].setEditable(true);
					tEnergy[i].setEnabled(true);
				}
				for (int i = 0; i < MAX_TERMS; i++) {
					if (i<numberTerms) {
						lcoeff[i].setText(labels[i]);
						tcoeff[i].setText(String.valueOf(coeff[i]));
						tcoeff[i].setEnabled(false);
						tcoeff[i].setEditable(true);
					} else {
						lcoeff[i].setText(BLANK_LABEL);
						tcoeff[i].setText("");
						tcoeff[i].setEnabled(false);
						tcoeff[i].setEditable(false);					
					}
				}
			//Coeffients set for fit
			}else {
				for (int i=0; i<NUM_POINTS; i++){
					tChannel[i].setText("");
					tChannel[i].setEnabled(true);
					tChannel[i].setEditable(false);
					tEnergy[i].setText("");
					tEnergy[i].setEnabled(true);
					tEnergy[i].setEditable(false);
				}				
				for (int i = 0; i < MAX_TERMS; i++) {
					if (i<numberTerms) {
						lcoeff[i].setText(labels[i]);
						tcoeff[i].setText(String.valueOf(coeff[i]));
						tcoeff[i].setEnabled(true);
						tcoeff[i].setEditable(true);
					} else {
						lcoeff[i].setText(BLANK_LABEL);
						tcoeff[i].setText("");
						tcoeff[i].setEditable(false);
						tcoeff[i].setEnabled(false);					
					}
				}
				
			}
			
		//Not calibrated
		} else {
			for (int i=0; i<NUM_POINTS; i++){
				tChannel[i].setText("");
				tChannel[i].setEditable(false);
				tChannel[i].setEnabled(false);
				tEnergy[i].setText("");
				tEnergy[i].setEditable(false);
				tEnergy[i].setEnabled(false);
			}			
			for (int i = 0; i < MAX_TERMS; i++) {
				lcoeff[i].setText(BLANK_LABEL);
				tcoeff[i].setText("");
				tcoeff[i].setEnabled(false);
				tcoeff[i].setEditable(false);
		}				
			
		}
		
	}
    
	private AbstractHist1D getCurrentHistogram() {
		final AbstractHist1D rval;
		final Histogram hist = STATUS.getCurrentHistogram();
		if (hist instanceof AbstractHist1D) {
			rval = (AbstractHist1D) hist;
		} else {
			rval=null;
		}
		return rval;
	}
	private AbstractCalibrationFunction getCurrentCalibrationFunction() {	
		AbstractCalibrationFunction hcf;
		//Get histogram status
		AbstractHist1D currentHistogram=getCurrentHistogram();
		if (currentHistogram!=null) {
			hcf=currentHistogram.getCalibration();    		
		}else{
			hcf=null;    		
		}
		return hcf;
	}
	
	/**
	* Update selection then show dialog
	**/
    public void show() {
    	updateSelection(); 
    	super.show();
    }
    public void update(Observable observable, Object object) {
    	
    	final BroadcastEvent be = (BroadcastEvent) object;
		final BroadcastEvent.Command com=be.getCommand();
		if (com == BroadcastEvent.Command.HISTOGRAM_SELECT ||
			com == BroadcastEvent.Command.HISTOGRAM_ADD) {
			updateSelection();     	       
		}
	}    
        
}
