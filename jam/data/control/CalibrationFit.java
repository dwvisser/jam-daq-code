/*
 */
package jam.data.control;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.NumberFormat;
import jam.global.*;
import jam.util.*;
import jam.data.*;
import jam.data.func.*;
import javax.swing.*;

/**
 * Class to control the histograms
 * Allows one to zero the histograms
 * and create new histograms
 *
 * @author Ken Swartz
 * @version 0.5
 */
public class CalibrationFit extends DataControl implements ActionListener, ItemListener {
    
    private static final int NUMBER_POINTS=5;
    
    private Frame frame;
    private Broadcaster broadcaster;
    private MessageHandler msghdlr;
    
    //calibrate histogram
    private Histogram currentHistogram;
    // calibration function
    private CalibrationFunction calibFunction;
    
    //GUI stuff
    private JDialog dialogCalib;
    private JComboBox cFunc;
    private JLabel lcalibEq;
    private JPanel pPoint [];
    private JTextField [] tEnergy, tChannel;
    private JCheckBox cUse[];
    
    private String fileName;
    private String directoryName;
    private NumberFormat numFormat;
    
    /**
     * Constructor
     */
    public CalibrationFit(Frame frame, Broadcaster broadcaster,
    MessageHandler msghdlr) {
        super();
        this.frame=frame;
        this.broadcaster=broadcaster;
        this.msghdlr=msghdlr;
        // calibration dialog box
        dialogCalib =new JDialog(frame,"Calibration Fit",false);
        dialogCalib.setForeground(Color.black);
        dialogCalib.setBackground(Color.lightGray);
        dialogCalib.setResizable(false);
        dialogCalib.setLocation(30,30);
        Container cdialogCalib=dialogCalib.getContentPane();
        cdialogCalib.setLayout(new GridLayout(0, 1, 10,10));
        //function choose dialog panel
        JPanel pChoose = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
        pChoose.add(new JLabel("FunctionType"));
        cFunc = new JComboBox();
        pChoose.add(cFunc);
        cFunc.addItem("Linear");
        cFunc.addItem("Polynomial");
        cFunc.addItem("Sqrt(E)");
        cFunc.addItemListener(this);
        cFunc.setSelectedIndex(0);
        cdialogCalib.add(pChoose);
        //fuction equation
        try {
            calibFunction=new LinearFunction();
        } catch (DataException de){
            msghdlr.errorOutln(getClass().getName()+" constructor: "+de.getMessage());
        }
        lcalibEq=new JLabel("Function: "+calibFunction.getTitle(), JLabel.CENTER);
        cdialogCalib.add(lcalibEq);
        //fields
        pPoint=new JPanel[NUMBER_POINTS];
        tEnergy =new JTextField[NUMBER_POINTS];
        tChannel =new JTextField[NUMBER_POINTS];
        cUse =new JCheckBox[NUMBER_POINTS];
        for ( int i=0; i<NUMBER_POINTS; i++ ){
            pPoint[i]=new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
            cdialogCalib.add(pPoint[i]);
            pPoint[i].add(new JLabel("Energy"));
            tEnergy[i] =new JTextField(" ");
            tEnergy[i].setColumns(6);
            tEnergy[i].setForeground(Color.black);
            tEnergy[i].setBackground(Color.white);
            pPoint[i].add(tEnergy[i]);
            pPoint[i].add(new JLabel("Channel"));
            tChannel[i] =new JTextField(" ");
            tChannel[i].setColumns(6);
            tChannel[i].setForeground(Color.black);
            tChannel[i].setBackground(Color.white);
            pPoint[i].add(tChannel[i]);
            cUse[i] =new JCheckBox("use");
            cUse[i].setSelected(true);
            pPoint[i].add(cUse[i]);
            cUse[i].addItemListener(this);
        }
        JPanel pbCal= new JPanel();
        pbCal.setLayout(new GridLayout(1,0));
        cdialogCalib.add(pbCal);
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
        dialogCalib.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                dialogCalib.dispose();
            }
        });
        //formating enery output
        numFormat=NumberFormat.getInstance();
        numFormat.setGroupingUsed(false);
        numFormat.setMinimumFractionDigits(4);
        numFormat.setMaximumFractionDigits(4);
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
                if(calibFunction==null){
                    msghdlr.errorOutln("Need to choose a function [CalibrationFit]");
                } else {
                    doCalibration();
                    if (command=="okcalib") {
                        dialogCalib.dispose();
                    }
                }
            } else if (command=="load") {
                try{
                    loadCalib();
                } catch (Exception e) {
                    msghdlr.errorOutln("Error loading calibration: "+e);
                }
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
        } catch (GlobalException ge) {
            msghdlr.errorOutln(getClass().getName()+
            ".actionPerformed(): "+ge);
        }
    }
    
    /**
     * A item state change indicates that a gate has been choicen
     *
     */
    public void itemStateChanged(ItemEvent ie){
        try {
            if (ie.getSource()==cFunc) {
                setFunctionType();//choose the function                
            } else {
                for(int i=0;i<NUMBER_POINTS;i++){
                    if(ie.getSource()==cUse[i]){
                        setFieldActive(i, cUse[i].isSelected());
                    }
                }
            }
        } catch (DataException de){
            msghdlr.errorOutln(de.getMessage());
        }
    }
    
    private void setFunctionType() throws DataException {
        Object item = cFunc.getSelectedItem();
        if(item.equals("Linear")){
            calibFunction=new LinearFunction();
        } else if(item.equals("Polynomial")){
            calibFunction=new PolynomialFunction(4);
        } else if(item.equals("Sqrt(E)")){
            calibFunction=new SqrtEnergyFunction();
        }
        lcalibEq.setText("Function: "+calibFunction.getTitle());
    }
    
    /**
     *sets fields to be active
     */
    private void setFieldActive(int number, boolean state){
        if(!state){
            tChannel[number].setBackground(Color.lightGray);
            tChannel[number].setEnabled(false);
            tEnergy[number].setBackground(Color.lightGray);
            tEnergy[number].setEnabled(false);
        } else {
            tChannel[number].setBackground(Color.white);
            tChannel[number].setEnabled(true);
            tEnergy[number].setBackground(Color.white);
            tEnergy[number].setEnabled(true);
        }
    }
    /**
     * Does nothing made to match other contollers.
     */
    public void setup(){
    }
    /**
     * Show histogram calibration dialog box
     */
    public void showLinFit(){
        dialogCalib.show();
    }
    
    /**
     * Call the fitting method of the chosen function to calibrate a histogram
     */
    private void doCalibration(){
        double energy [] =new double[NUMBER_POINTS];
        double channel [] =new double[NUMBER_POINTS];
        int numberPoints=0;
        double x[];
        double y[];
        String fitText;
        
        Histogram currentHist=Histogram.getHistogram(JamStatus.getCurrentHistogramName());
        if (currentHist==null){//silently ignore if histogram null
            System.err.println("Error null histogram [Calibrate]");
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
                setFunctionType();//ensures a fresh CalibrationFit instance is created
                x=new double [numberPoints];
                y=new double [numberPoints];
                //put valid energy and channel into arrays
                for (int i=0;i<numberPoints;i++){
                    y[i]=energy[i];
                    x[i]=channel[i];
                }
                fitText=calibFunction.fit(x,y);
                currentHist.setCalibration(calibFunction);
                broadcaster.broadcast(BroadcastEvent.REFRESH);
                msghdlr.messageOutln("Calibrated histogram "+currentHistogram.getName()+" with "+
                fitText);
            } else {
                msghdlr.errorOutln("Need at least 2 points [Calibrate]");
            }
        } catch (NumberFormatException nfe){
            msghdlr.errorOutln("Invalid input, not a number [Calibrate]");
        } catch (DataException de) {
            msghdlr.errorOutln(de.getMessage());
        } catch (GlobalException ge) {
            msghdlr.errorOutln(getClass().getName()+
            ".doCalibration(): "+ge);
        }
    }
    
    /**
     * cancel the histogram calibration
     *
     */
    private void cancelCalib() throws GlobalException {
        Histogram currentHist=Histogram.getHistogram(JamStatus.getCurrentHistogramName());
        currentHist.setCalibration(null);
        broadcaster.broadcast(BroadcastEvent.REFRESH);
    }
    /**
     * Load a list of energies from a file
     */
    private void loadCalib() throws Exception {
        openFile("Load Calibration Energies", FileDialog.LOAD);
    }
    
    private void saveCalib() throws Exception{
        openFile("Save Calibration Energies", FileDialog.SAVE);
    }
    
    /**
     * Open a file to read
     */
    private File openFile(String msg, int state) throws Exception {
        File fileIn=null;//default return value
        FileDialog fd =new FileDialog(frame, msg, state);
        String extension=".cal";
        if ( (fileName)!=null){//use previous file and directory as default
            fd.setFile(fileName);
        } else {
            fd.setFile(extension);
        }
        if (directoryName!=null){
            fd.setDirectory(directoryName);
        }
        fd.show();//show file dialoge box to get file
        directoryName=fd.getDirectory();//save current directory
        fileName=fd.getFile();
        fd.dispose();
        if(fileName!=null) {
            fileName=FileUtilities.setExtension(fileName,extension,FileUtilities.FORCE);
            fileIn = new File(directoryName ,fileName);
        } //else leave it null
        return fileIn;
    }
    
    /**
     *  Process window events
     *  If the window is active check that the histogram been displayed
     *  has not changed. If it has cancel the gate setting.
     */
    public void windowActivated(WindowEvent e){        
        Histogram  hist=Histogram.getHistogram(JamStatus.getCurrentHistogramName());
        //have we changed histograms
        if (hist!=currentHistogram){
            currentHistogram=hist;
            calibFunction=currentHistogram.getCalibration();
            setup();            
            //has calib function changed
        } else if(calibFunction!=currentHistogram.getCalibration()){
            calibFunction=currentHistogram.getCalibration();
            setup();            
        }        
    }    
}
