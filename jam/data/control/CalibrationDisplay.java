/*
 */
package jam.data.control;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.NumberFormat;
import jam.global.*;
import jam.data.*;
import jam.data.func.*;
import javax.swing.*;

/**
 * Displays a calibration function.
 *
 * @author Ken Swartz
 * @version 0.5
 */
public class CalibrationDisplay extends DataControl implements ActionListener,
ItemListener, WindowListener {
    
    private final int MAX_NUMBER_TERMS=5;
    private final String BLANK_TITLE=" Histogram not calibrated ";
    private final String BLANK_LABEL="    --     ";
    
    private Frame frame;
    private Broadcaster broadcaster;
    private MessageHandler msghdlr;
    
    private JDialog dialogCalib;
    private JComboBox cFunc;
    private JLabel lcalibEq;
    private JPanel pbCal;
    private JPanel pcoeff [];
    private JLabel lcoeff [];
    private JTextField tcoeff[];
    
    private Histogram currentHistogram;
    
    //calibrate histogram
    private CalibrationFunction calibFunction;
    int numberTerms;
    private NumberFormat numFormat;
    
    /**
     * Constructor
     */
    public CalibrationDisplay(Frame frame, Broadcaster broadcaster, MessageHandler msghdlr){
        super();
        this.frame=frame;
        this.broadcaster=broadcaster;
        this.msghdlr=msghdlr;
        
        dialogCalib =new JDialog(frame,"Histogram Calibration",false);
        dialogCalib.setForeground(Color.black);
        dialogCalib.setBackground(Color.lightGray);
        dialogCalib.setResizable(false);
        dialogCalib.setLocation(30,30);
        //dialogCalib.setSize(300, 220+35*MAX_NUMBER_TERMS);
        Container cdialogCalib = dialogCalib.getContentPane();
        cdialogCalib.setLayout(new GridLayout(0, 1, 10,10));
        dialogCalib.addWindowListener(this);
        
        //function choose dialog panel
        JPanel pChoose = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
        pChoose.add(new JLabel("FunctionType"));
        cFunc = new JComboBox();
        pChoose.add(cFunc);
        cFunc.addItem("Linear");
        cFunc.addItem("Polynomial");
        cFunc.addItem("Sqrt(E)");
        cFunc.addItemListener(this);
        cdialogCalib.add(pChoose);
        
        //fuction equation
        try {
            calibFunction=new LinearFunction();
        } catch (DataException de){
            msghdlr.errorOutln(getClass().getName()+" constructor: "+de.getMessage());
        }
        lcalibEq=new JLabel("Function: "+calibFunction.getTitle(), JLabel.CENTER);
        cdialogCalib.add(lcalibEq);
        
        pcoeff=new JPanel[MAX_NUMBER_TERMS];
        lcoeff=new JLabel[MAX_NUMBER_TERMS];
        tcoeff =new JTextField[MAX_NUMBER_TERMS];
        
        for (int i=0; i<MAX_NUMBER_TERMS; i++ )	{
            pcoeff[i]=new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
            cdialogCalib.add(pcoeff[i]);
            lcoeff[i]=new JLabel(BLANK_LABEL, JLabel.RIGHT);
            pcoeff[i].add(lcoeff[i]);
            tcoeff[i] =new JTextField(" ");
            tcoeff[i].setColumns(10);
            tcoeff[i].setForeground(Color.black);
            tcoeff[i].setBackground(Color.lightGray);
            tcoeff[i].setEditable(false);
            pcoeff[i].add(tcoeff[i]);
        }
        
        pbCal= new JPanel(new GridLayout(1,0,5,5));
        cdialogCalib.add(pbCal);
        
        JButton bRecal =  new JButton("Recall");
        bRecal.setActionCommand("recalcalib");
        bRecal.addActionListener(this);
        pbCal.add(bRecal);
        
        JButton bokCal =  new JButton("OK");
        bokCal.setActionCommand("okcalib");
        bokCal.addActionListener(this);
        pbCal.add(bokCal);
        
        JButton bapplyCal = new JButton("Apply");
        bapplyCal.setActionCommand("applycalib");
        bapplyCal.addActionListener(this);
        pbCal.add(bapplyCal);
        
        JButton bcancelCal =new JButton("Cancel");
        bcancelCal.setActionCommand("cancelcalib");
        bcancelCal.addActionListener(this);
        pbCal.add(bcancelCal);
        
        dialogCalib.pack();
        
        //formating  output
        numFormat=NumberFormat.getInstance();
        numFormat.setGroupingUsed(false);
        numFormat.setMinimumFractionDigits(2);
        numFormat.setMaximumFractionDigits(5);
    }
    
    /**
     * setups up the dialog box
     */
    public void setup() {        
        if (calibFunction!=null) {
            numberTerms=calibFunction.getNumberTerms();
            lcalibEq.setText("Function: "+calibFunction.getTitle());
            String [] labels = calibFunction.getLabels();
            double [] coeff = calibFunction.getCoeff();
            for ( int i=0; i<numberTerms; i++ ){
                lcoeff[i].setText(labels[i]);
                tcoeff[i].setText(Double.toString(coeff[i]));
                tcoeff[i].setBackground(Color.white);
                tcoeff[i].setEditable(true);
            }
            for ( int i=numberTerms; i<MAX_NUMBER_TERMS; i++ ){
                lcoeff[i].setText(BLANK_LABEL);
                tcoeff[i].setText(" ");
                tcoeff[i].setBackground(Color.lightGray);
                tcoeff[i].setEditable(false);
            }                        
        } else {// histogram not calibrated
            lcalibEq.setText(BLANK_TITLE);
            for ( int i=0; i<MAX_NUMBER_TERMS; i++ ){
                lcoeff[i].setText(BLANK_LABEL);
                tcoeff[i].setText(" ");
                tcoeff[i].setBackground(Color.lightGray);
                tcoeff[i].setEditable(false);                
            }
        }
    }
    
    /**
     * Show histogram calibration dialog box
     */
    public void show(){
        dialogCalib.show();
    }
    
    /**
     * Receive actions from Dialog Boxes
     *
     */
    public void actionPerformed(ActionEvent ae){        
        String command=ae.getActionCommand();
        currentHistogram=Histogram.getHistogram(JamStatus.getCurrentHistogramName());        
        try {            
            //commands for calibration
            if ((command=="okcalib")||(command=="applycalib")) {
                setCoefficients();
                msghdlr.messageOutln("Calibrated histogram "+currentHistogram.getName());
                if (command=="okcalib") {
                    dialogCalib.dispose();
                }                
            } else if (command=="recalcalib") {
                getCalibration();                
            } else if (command=="cancelcalib") {
                cancelCalib();
                msghdlr.messageOutln("Uncalibrated histogram "+currentHistogram.getName());
                dialogCalib.dispose();                                
            } else {
                //just so at least a exception is thrown for now
                throw new DataException("Unregonized command [HistogramControl]");
            }
        } catch (DataException je) {
            msghdlr.errorOutln(je.getMessage());
        }        
    }
    
    /**
     * A item state change indicates that a gate has been choicen
     *
     */
    public void itemStateChanged(ItemEvent ie){
        try {
            if (ie.getSource()==cFunc) {
                //choose the function
                if(cFunc.getSelectedItem().equals("Linear")){
                    calibFunction=new LinearFunction();
                } else if(cFunc.getSelectedItem().equals("Polynomial")){
                    calibFunction=new PolynomialFunction(4);
                } else if(cFunc.getSelectedItem().equals("Sqrt(E)")){
                    calibFunction=new SqrtEnergyFunction();
                }
                lcalibEq.setText("Function: "+calibFunction.getTitle());
            } else {
                System.err.println("Error Unknown item for CalibrationDisplay");
            }
        } catch (DataException de){
            msghdlr.errorOutln(de.getMessage());
        }
    }
    
    /**
     * set the calibration coefficients for a histogram
     */
    private void setCoefficients() throws DataException {
        double coeff [] =new double[numberTerms];
        int i=0;
        Histogram currentHist=Histogram.getHistogram(JamStatus.getCurrentHistogramName());
        CalibrationFunction calibFunction=currentHistogram.getCalibration();
        //silently ignore if histogram null
        if (calibFunction!=null){
            try {
                for( i=0;i<numberTerms;i++){
                    coeff[i]=( new Double(tcoeff[i].getText()) ).doubleValue();
                }
                calibFunction.setCoeff(coeff);
            } catch (NumberFormatException nfe){
                msghdlr.errorOutln("Invalid input, coefficient "+
                calibFunction.getLabels()[i]);
            } catch (DataException de) {
                msghdlr.errorOutln(de.getMessage());
            }
            getCalibration();
        } else {
            msghdlr.errorOutln("Calibration function not defined [CalibrationDisplay]");
        }
    }
    
    /**
     *
     */
    private void getCalibration() {
        double coeff [];
        
        setup();
        if (calibFunction!=null) {
            coeff=calibFunction.getCoeff();
            for(int i=0;i<numberTerms;i++){
                tcoeff[i].setText(numFormat.format(coeff[i]));
            }
        } else {
            msghdlr.errorOutln("Calibration not set [HistogramControl]");
        }
    }
    
    /**
     * cancel the histogram calibration
     *
     */
    private void cancelCalib(){
        Histogram currentHist=Histogram.getHistogram(JamStatus.getCurrentHistogramName());
        currentHist.setCalibration(null);
    }
    
    /**
     *  Process window events
     *  If the window is active check that the histogram been displayed
     *  has not changed. If it has cancel the gate setting.
     */
    public void windowActivated(WindowEvent e){
        Histogram hist=Histogram.getHistogram(JamStatus.getCurrentHistogramName());
        //have we changed histograms
        if (hist!=currentHistogram){
            currentHistogram=hist;
            calibFunction=currentHistogram.getCalibration();
            setup();
            //has calib function changed
        } else if (calibFunction!=currentHistogram.getCalibration()) {
            calibFunction=currentHistogram.getCalibration();
            setup();
        }
    }
    
    /**
     * Window Events
     *  windowClosing only one used.
     */
    public void windowClosing(WindowEvent e){
        dialogCalib.dispose();
    }
    
    /**
     * Does nothing
     *  only windowClosing used.
     */
    public void windowClosed(WindowEvent e){
        /* does nothing for now */
    }
    /**
     * Does nothing
     *  only windowClosing used.
     */
    public void windowDeactivated(WindowEvent e){
        /* does nothing for now */
    }
    
    /**
     * Does nothing
     *  only windowClosing used.
     */
    public void windowDeiconified(WindowEvent e){
        /* does nothing for now */
    }
    
    /**
     * Does nothing
     *  only windowClosing used.
     */
    public void windowIconified(WindowEvent e){
        /* does nothing for now */
    }
    
    /**
     * removes list of gates when closed
     *  only windowClosing used.
     */
    public void windowOpened(WindowEvent e){
        setup();
    }
    
}
