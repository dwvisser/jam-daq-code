package jam.data.control;
import jam.data.DataException;
import jam.data.Histogram;
import jam.data.func.CalibrationFunction;
import jam.data.func.LinearFunction;
import jam.data.func.CalibrationComboBoxModel;
import jam.data.func.CalibrationListCellRenderer;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GlobalException;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.util.FileUtilities;

import java.awt.Container;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Class to control the histograms
 * Allows one to zero the histograms
 * and create new histograms
 *
 * @author Ken Swartz
 * @version 0.5
 */
public class CalibrationFit extends DataControl implements ActionListener, ItemListener{

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
    private JamStatus status;
	private JButton bokCal =  new JButton("OK");
	private JButton bapplyCal = new JButton("Apply");
	private JButton bcancelCal =new JButton("Cancel");

    /**
     * Constructor
     */
    public CalibrationFit(Frame frame, Broadcaster broadcaster,
    MessageHandler msghdlr) {
        super();
        this.frame=frame;
        this.broadcaster=broadcaster;
        this.msghdlr=msghdlr;
        status=JamStatus.instance();
        // calibration dialog box
        dialogCalib =new JDialog(frame,"Calibration Fit",false);
        dialogCalib.setResizable(false);
        dialogCalib.setLocation(30,30);
        Container cdialogCalib=dialogCalib.getContentPane();
        cdialogCalib.setLayout(new GridLayout(0, 1, 10,10));
        //function choose dialog panel
        JPanel pChoose = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
        pChoose.add(new JLabel("Function: "));
        cFunc = new JComboBox(new CalibrationComboBoxModel());
        cFunc.setRenderer(new CalibrationListCellRenderer());
        cFunc.setSelectedIndex(0);
        pChoose.add(cFunc);
//        cFunc.addItem("Linear");
//        cFunc.addItem("Polynomial");
//        cFunc.addItem("Sqrt(E)");
        cFunc.addItemListener(this);
        cdialogCalib.add(pChoose);
        calibFunction=new LinearFunction();
        lcalibEq=new JLabel(calibFunction.getTitle(), JLabel.CENTER);
        cdialogCalib.add(lcalibEq);
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
            pPoint[i].add(tEnergy[i]);
            pPoint[i].add(new JLabel("Channel"));
            tChannel[i] =new JTextField(" ");
            tChannel[i].setColumns(6);
            pPoint[i].add(tChannel[i]);
            cUse[i] =new JCheckBox("use");
            cUse[i].setSelected(true);
            pPoint[i].add(cUse[i]);
            cUse[i].addItemListener(this);
        }
        JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel pbCal= new JPanel();
        pbCal.setLayout(new GridLayout(1,0,5,5));
        pbutton.add(pbCal);
        cdialogCalib.add(pbutton);

        bokCal.setActionCommand("okcalib");
        bokCal.addActionListener(this);
        pbCal.add(bokCal);
        bapplyCal.setActionCommand("applycalib");
        bapplyCal.addActionListener(this);
        pbCal.add(bapplyCal);
        bcancelCal.setActionCommand("cancelcalib");
        bcancelCal.addActionListener(this);
        pbCal.add(bcancelCal);
        dialogCalib.pack();

        dialogCalib.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                dialogCalib.dispose();
            }
			public void windowActivated(WindowEvent e) {
				setup();
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
        currentHistogram=Histogram.getHistogram(status.getCurrentHistogramName());
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
        if (ie.getSource()==cFunc) {
            setFunctionType();//choose the function
        } else {
            for(int i=0;i<NUMBER_POINTS;i++){
                if(ie.getSource()==cUse[i]){
                    setFieldActive(i, cUse[i].isSelected());
                }
            }
        }
    }

    private void setFunctionType() {
		final Class calClass = (Class)cFunc.getSelectedItem();
		try {
			calibFunction = (CalibrationFunction)calClass.newInstance();
		} catch (Exception e) {
			msghdlr.errorOutln(getClass().getName()+
			".setFunctionType(): "+e.toString());            	
		} 
        lcalibEq.setText(calibFunction.getTitle());
    }

    /**
     *sets fields to be active
     */
    private void setFieldActive(int number, boolean state){
		tChannel[number].setEnabled(state);
		tEnergy[number].setEnabled(state);
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

        Histogram currentHist=Histogram.getHistogram(status.getCurrentHistogramName());
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
                msghdlr.messageOutln("Calibrated histogram "+currentHistogram.getName().trim()+" with "+
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
        Histogram currentHist=Histogram.getHistogram(status.getCurrentHistogramName());
        currentHist.setCalibration(null);
        broadcaster.broadcast(BroadcastEvent.REFRESH);
    }
    /**
     * Load a list of energies from a file
     */
    private void loadCalib() throws Exception {
        openFile("Load Calibration Energies", FileDialog.LOAD);
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
    

    public void setup(){
		final Histogram hist=Histogram.getHistogram(
		status.getCurrentHistogramName());
		if (hist!=currentHistogram){
			currentHistogram=hist;
			calibFunction = currentHistogram==null ? null :
			currentHistogram.getCalibration();
		} 
		final boolean histExists = currentHistogram!=null;
		if(histExists && 
		calibFunction!=currentHistogram.getCalibration()){
			calibFunction=currentHistogram.getCalibration();
			if (calibFunction == null){
				setFunctionType();
			}
		}	 
		cFunc.setEnabled(histExists);
		bokCal.setEnabled(histExists);
		bapplyCal.setEnabled(histExists);
		bcancelCal.setEnabled(histExists);		
		for (int i=0; i<NUMBER_POINTS; i++){
			final boolean enable=histExists && cUse[i].isSelected();
			tEnergy[i].setEnabled(enable);
			tChannel[i].setEnabled(enable);
			cUse[i].setEnabled(histExists);
		}
    }
    
}
