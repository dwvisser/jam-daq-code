package jam.data.control;
import jam.data.DataException;
import jam.data.Histogram;
import jam.data.func.CalibrationComboBoxModel;
import jam.data.func.CalibrationFunction;
import jam.data.func.CalibrationListCellRenderer;
import jam.data.func.LinearFunction;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Displays a calibration function.
 *
 * @author Ken Swartz
 * @version 0.5
 */
public class CalibrationDisplay extends DataControl implements ActionListener,
ItemListener, WindowListener {

    private final static int MAX_NUMBER_TERMS=5;
    private final static String BLANK_TITLE=" Histogram not calibrated ";
    private final static String BLANK_LABEL="    --     ";

    private Frame frame;
    private Broadcaster broadcaster;
    private MessageHandler msghdlr;

    private JDialog dialogCalib;
    private JComboBox cFunc;
    private JLabel lcalibEq;
    private JPanel pcoeff [];
    private JLabel lcoeff [];
    private JTextField tcoeff[];

    private Histogram currentHistogram;

    //calibrate histogram
    private CalibrationFunction calibFunction;
    int numberTerms;
    private NumberFormat numFormat;
    private JamStatus status;

    /**
     * Constructor
     */
    public CalibrationDisplay(Frame frame, Broadcaster broadcaster, MessageHandler msghdlr){
        super();
        this.frame=frame;
        this.broadcaster=broadcaster;
        this.msghdlr=msghdlr;
        status=JamStatus.instance();

        dialogCalib =new JDialog(frame,"Histogram Calibration",false);
        dialogCalib.setForeground(Color.black);
        dialogCalib.setBackground(Color.lightGray);
        dialogCalib.setResizable(false);
        dialogCalib.setLocation(30,30);
        Container cdialogCalib = dialogCalib.getContentPane();
        cdialogCalib.setLayout(new GridLayout(0, 1, 10,10));
        dialogCalib.addWindowListener(this);

        //function choose dialog panel
        JPanel pChoose = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
        pChoose.add(new JLabel("Function: "));
        cFunc = new JComboBox(new CalibrationComboBoxModel());
        cFunc.setRenderer(new CalibrationListCellRenderer());
        cFunc.setSelectedIndex(0);
        pChoose.add(cFunc);
        cFunc.addItemListener(this);
        cdialogCalib.add(pChoose);

        calibFunction=new LinearFunction();
        lcalibEq=new JLabel(calibFunction.getTitle(), JLabel.CENTER);
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

        JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cdialogCalib.add(pbutton);
        JPanel pbCal= new JPanel(new GridLayout(1,0,5,5));
        pbutton.add(pbCal);


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
            lcalibEq.setText(calibFunction.getTitle());
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
        currentHistogram=Histogram.getHistogram(status.getCurrentHistogramName());
        try {
            //commands for calibration
            if ((command=="okcalib")||(command=="applycalib")) {
                setCoefficients();
                msghdlr.messageOutln("Calibrated histogram "+currentHistogram.getName().trim()+" ");
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
        if (ie.getSource()==cFunc) {
            final Class calClass = (Class)cFunc.getSelectedItem();
            try {
				calibFunction = (CalibrationFunction)calClass.newInstance();
            } catch (Exception e) {
				msghdlr.errorOutln(getClass().getName()+
				".itemStateChanged(): "+ie.toString());            	
            } 
            lcalibEq.setText(calibFunction.getTitle());
        } else {
            msghdlr.errorOutln(getClass().getName()+
			".itemStateChanged(): unknown source: "+ie.toString());
        }
    }

    /**
     * set the calibration coefficients for a histogram
     */
    private void setCoefficients() throws DataException {
        double coeff [] =new double[numberTerms];
        int i=0;
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
        Histogram currentHist=Histogram.getHistogram(status.getCurrentHistogramName());
        currentHist.setCalibration(null);
    }

    /**
     *  Process window events
     *  If the window is active check that the histogram been displayed
     *  has not changed. If it has cancel the gate setting.
     */
    public void windowActivated(WindowEvent e){
        Histogram hist=Histogram.getHistogram(status.getCurrentHistogramName());
        //have we changed histograms
        if (hist != currentHistogram){
            currentHistogram=hist;
            calibFunction = currentHistogram==null ? null :
            currentHistogram.getCalibration();
            /*if (currentHistogram != null){
				calibFunction=currentHistogram.getCalibration();
            }*/
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
