package jam.data.control;
import jam.data.Histogram;
import jam.data.func.CalibrationComboBoxModel;
import jam.data.func.CalibrationFunction;
import jam.data.func.CalibrationListCellRenderer;
import jam.global.MessageHandler;

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

    private final Frame frame;
    private final MessageHandler msghdlr;

    private final JComboBox cFunc = new JComboBox(new CalibrationComboBoxModel());

    private final JLabel lcalibEq;
    private final JPanel pcoeff [];
    private final JLabel lcoeff [];
    private final JTextField tcoeff[];

    private Histogram currentHistogram;

    //calibrate histogram
    int numberTerms;
    private final NumberFormat numFormat;
	JButton bokCal =  new JButton("OK");
	JButton bapplyCal = new JButton("Apply");
	JButton bcancelCal =new JButton("Cancel");
	JButton bRecal =  new JButton("Recall");

    /**
     * Constructor
     */
    public CalibrationDisplay(MessageHandler mh){
        super("Histogram Calibration",false);
        frame=status.getFrame();
        msghdlr=mh;

        setResizable(false);
        setLocation(30,30);
        final Container cdialogCalib = getContentPane();
        cdialogCalib.setLayout(new GridLayout(0, 1, 10,10));
        addWindowListener(this);

        //function choose dialog panel
        JPanel pChoose = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
        pChoose.add(new JLabel("Function: "));
        cFunc.setRenderer(new CalibrationListCellRenderer());
        cFunc.setSelectedIndex(0);
        pChoose.add(cFunc);
        cFunc.addItemListener(this);
        cdialogCalib.add(pChoose);

        //calibFunction=new LinearFunction();
        lcalibEq=new JLabel(BLANK_TITLE, JLabel.CENTER);
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
            pcoeff[i].add(tcoeff[i]);
        }

        JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cdialogCalib.add(pbutton);
        JPanel pbCal= new JPanel(new GridLayout(1,0,5,5));
        pbutton.add(pbCal);


        bRecal.setActionCommand("recalcalib");
        bRecal.addActionListener(this);
        pbCal.add(bRecal);

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
		final Histogram hist=Histogram.getHistogram(
		status.getHistName());
		if (hist!=currentHistogram){
			currentHistogram=hist;
		} 
		final boolean hist1d = currentHistogram!=null && 
		currentHistogram.getDimensionality()==1;
		CalibrationFunction hcf=null;
		if(hist1d) {
			hcf=currentHistogram.getCalibration();
			final String name= hcf==null ? null : hcf.getClass().getName();
			cFunc.setSelectedItem(name);
		}	 
		final boolean exists=hist1d && hcf!=null;
		cFunc.setEnabled(exists);
		bokCal.setEnabled(exists);
		bapplyCal.setEnabled(exists);
		bcancelCal.setEnabled(exists);	
		bRecal.setEnabled(exists);	
        if (exists) {
            numberTerms=hcf.getNumberTerms();
            lcalibEq.setText(hcf.getTitle());
            String [] labels = hcf.getLabels();
            double [] coeff = hcf.getCoeff();
            for ( int i=0; i<numberTerms; i++ ){
                lcoeff[i].setText(labels[i]);
                tcoeff[i].setText(String.valueOf(coeff[i]));
                tcoeff[i].setEnabled(true);
            }
            for (int i=numberTerms; i<MAX_NUMBER_TERMS; i++ ){
				lcoeff[i].setText(BLANK_LABEL);
				tcoeff[i].setText("");
				tcoeff[i].setEnabled(false);
			}
        } else {// histogram not calibrated
            lcalibEq.setText(BLANK_TITLE);
            for ( int i=0; i<MAX_NUMBER_TERMS; i++ ){
                lcoeff[i].setText(BLANK_LABEL);
                tcoeff[i].setText("");
                tcoeff[i].setEnabled(false);
            }
        }
    }

    /**
     * Receive actions from Dialog Boxes
     *
     */
    public void actionPerformed(ActionEvent ae){
        String command=ae.getActionCommand();
        currentHistogram=Histogram.getHistogram(status.getHistName());
            //commands for calibration
            if ((command=="okcalib")||(command=="applycalib")) {
                setCoefficients();
                msghdlr.messageOutln("Calibrated histogram "+currentHistogram.getName().trim()+
				" with "+currentHistogram.getCalibration().getFormula());
                if (command=="okcalib") {
                    dispose();
                }
            } else if (command=="recalcalib") {
                setup();
            } else if (command=="cancelcalib") {
                cancelCalib();
                msghdlr.messageOutln("Uncalibrated histogram "+currentHistogram.getName());
                dispose();
            } else {
                //just so at least a exception is thrown for now
                throw new UnsupportedOperationException("Unregonized command: "+command);
            }
    }

    /**
     * A item state change indicates that a gate has been choicen
     *
     */
    public void itemStateChanged(ItemEvent ie){
		CalibrationFunction calibFunction=null;      
		if (ie.getSource()==cFunc) {
            try {
				final Class calClass = Class.forName((String)cFunc.getSelectedItem());
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
    private void setCoefficients() {
        double coeff [] =new double[numberTerms];
        int i=0;
        CalibrationFunction calibFunction=currentHistogram.getCalibration();
        /* silently ignore if histogram null */
        if (calibFunction!=null){
            try {
                for( i=0;i<numberTerms;i++){
                    coeff[i]=( new Double(tcoeff[i].getText()) ).doubleValue();
                }
                calibFunction.setCoeff(coeff);
            } catch (NumberFormatException nfe){
                msghdlr.errorOutln("Invalid input, coefficient "+
                calibFunction.getLabels()[i]);
            }
            setup();
        } else {
            msghdlr.errorOutln("Calibration function not defined [CalibrationDisplay]");
        }
    }

    /**
     * cancel the histogram calibration
     *
     */
    private void cancelCalib(){
        Histogram currentHist=Histogram.getHistogram(status.getHistName());
        currentHist.setCalibration(null);
    }

    /**
     *  Process window events
     *  If the window is active check that the histogram been displayed
     *  has not changed. If it has cancel the gate setting.
     */
    public void windowActivated(WindowEvent e){
        setup();
    }

    /**
     * Window Events
     *  windowClosing only one used.
     */
    public void windowClosing(WindowEvent e){
        dispose();
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
