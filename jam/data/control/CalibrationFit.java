package jam.data.control;
import jam.data.DataException;
import jam.data.Histogram;
import jam.data.func.CalibrationFunction;
import jam.data.func.LinearFunction;
import jam.data.func.CalibrationComboBoxModel;
import jam.data.func.CalibrationListCellRenderer;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
public class CalibrationFit extends DataControl implements ActionListener {

    private static final int NUMBER_POINTS=5;

    private final Frame frame;
    private final Broadcaster broadcaster;
    private final MessageHandler msghdlr;

    //calibrate histogram
    private Histogram currentHistogram;
    // calibration function
    private CalibrationFunction calibFunction=new LinearFunction();

    //GUI stuff
    private final JDialog dialogCalib;
    private final JComboBox cFunc;
    private final JLabel lcalibEq=new JLabel("Select a function.", JLabel.CENTER);
    private final JPanel pPoint [];
    private final JTextField [] tEnergy, tChannel;
    private final JCheckBox cUse[];

    private final NumberFormat numFormat;
    private final JamStatus status;
	private final JButton bokCal =  new JButton("OK");
	private final JButton bapplyCal = new JButton("Apply");
	private final JButton bcancelCal =new JButton("Cancel");

    /**
     * Constructor
     */
    public CalibrationFit(Frame fr, Broadcaster bc,
    MessageHandler mh) {
        super();
        frame=fr;
        broadcaster=bc;
        msghdlr=mh;
        status=JamStatus.instance();
        // calibration dialog box
        dialogCalib =new JDialog(frame,"Calibration Fit",false);
        dialogCalib.setResizable(false);
        dialogCalib.setLocation(30,30);
        Container cdialogCalib=dialogCalib.getContentPane();
        cdialogCalib.setLayout(new GridLayout(0, 1, 10,10));
        JPanel pChoose = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
        pChoose.add(new JLabel("Function: "));
        cFunc = new JComboBox(new CalibrationComboBoxModel());
        cFunc.setRenderer(new CalibrationListCellRenderer());
		cFunc.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent ie){
				final String calClass = (String)cFunc.getSelectedItem();
				try {
					calibFunction = (CalibrationFunction)Class.forName(calClass).newInstance();
				} catch (Exception e) {
					msghdlr.errorOutln(getClass().getName()+
					".setFunctionType(): "+e.toString());            	
				} 
				lcalibEq.setText(calibFunction.getTitle());
			}
		});
		cFunc.setSelectedItem(LinearFunction.class.getName());
        pChoose.add(cFunc);
        cdialogCalib.add(pChoose);
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
            //cUse[i].addItemListener(this);
            final int index=i;//silly, but necessary for anonymous class
            cUse[i].addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent ie){
					setFieldActive(index, cUse[index].isSelected());
				}
				
				/**
				 *sets fields to be active
				 */
				private void setFieldActive(int number, boolean state){
					tChannel[number].setEnabled(state);
					tEnergy[number].setEnabled(state);
				}
            });
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
            } else if (command=="cancelcalib") {
                cancelCalib();
                msghdlr.messageOutln("Uncalibrated histogram "+currentHistogram.getName());
                dialogCalib.dispose();
            } else {
                //just so at least a exception is thrown for now
                throw new UnsupportedOperationException("Unregonized command: "+command);
            }
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
        } 
    }

    /**
     * cancel the histogram calibration
     *
     */
    private void cancelCalib() {
        Histogram currentHist=Histogram.getHistogram(status.getCurrentHistogramName());
        currentHist.setCalibration(null);
        broadcaster.broadcast(BroadcastEvent.REFRESH);
    }

    public void setup(){
		final Histogram hist=Histogram.getHistogram(
		status.getCurrentHistogramName());
		if (hist!=currentHistogram){
			currentHistogram=hist;
		} 
		final boolean hist1d = currentHistogram!=null && 
		currentHistogram.getDimensionality()==1;
		if(hist1d) {
			final CalibrationFunction hcf=currentHistogram.getCalibration();
			final String name= hcf==null ? null : hcf.getClass().getName();
			cFunc.setSelectedItem(name);
		}	 
		cFunc.setEnabled(hist1d);
		bokCal.setEnabled(hist1d);
		bapplyCal.setEnabled(hist1d);
		bcancelCal.setEnabled(hist1d);		
		for (int i=0; i<NUMBER_POINTS; i++){
			final boolean enable=hist1d && cUse[i].isSelected();
			tEnergy[i].setEnabled(enable);
			tChannel[i].setEnabled(enable);
			cUse[i].setEnabled(hist1d);
		}
    }
    
    
}
