/*
 */
package jam.data.control;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jam.global.*;
import jam.data.*;
import java.text.NumberFormat;
import javax.swing.*;

/**
 * Class for adjusting the gain of 1d spectra.
 *
 * @author Dale Visser
 * @version JDK 1.1
 */
public class GainShift extends DataControl implements ActionListener, ItemListener, WindowListener,
Observer {
    private Frame frame;
    private Broadcaster broadcaster;
    private MessageHandler messageHandler;

    private int chmin,chmax;

    private JDialog dgain;
    private JComboBox cto;
    private JTextField tfrom;
    private JLabel lname;
    private JCheckBox cchan,ccoeff;
    private JTextField  text1,text2,text3,text4,ttextto;
    private JLabel label1,label2,label3,label4;
    private JButton bOK, bApply;

    private double chan1i,chan2i,chan1f,chan2f,a1,b1,a2,b2;

    private Histogram hfrom;

    public GainShift(Frame frame, Broadcaster broadcaster, MessageHandler messageHandler){
        super();
        chan1i=0.0;
        chan2i=1.0;
        chan1f=0.0;
        chan2f=1.0;
        this.frame=frame;
        this.broadcaster=broadcaster;
        this.messageHandler=messageHandler;
        dgain=new JDialog(frame,"Gain Shift 1-D Histogram",false);
        dgain.setResizable(false);
        int rows=6;
        int cols=1;
        int hgap=10;
        int vgap=10;
        Container cdgain=dgain.getContentPane();
        cdgain.setLayout(new GridLayout(rows,cols,hgap,vgap));
        dgain.setLocation(20,50);
        dgain.addWindowListener(this);
        JPanel pfrom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfrom = new JTextField("1DHISTOGRAM", 16);
        tfrom.setEditable(false);
        pfrom.add(new JLabel("Shift from"));
        pfrom.add(tfrom);
        cdgain.add(pfrom);
        JPanel pto = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pto.add(new JLabel("to  histogram"));
        cto   = new JComboBox();
        cto.addItem("New Histogram");
        cto.addItemListener(this);
        pto.add(cto);
        lname = new JLabel("Name");
        pto.add(lname);
        ttextto = new JTextField("new",8);
        ttextto.setForeground(Color.black);
        pto.add(ttextto);
        cdgain.add(pto);
        JPanel pradio = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ButtonGroup cbg = new ButtonGroup();
        cchan = new JCheckBox("Using Channels",false);
        cbg.add(cchan);
        cchan.addItemListener(this);
        ccoeff = new JCheckBox("Using Coeffiecients",true);
        cbg.add(ccoeff);
        ccoeff.addItemListener(this);
        pradio.add(cchan);
        pradio.add(ccoeff);
        cdgain.add(pradio);
        JPanel pinfields = new JPanel(new FlowLayout(FlowLayout.CENTER));
        label1 = new JLabel("Input Gain = a1");
        text1 = new JTextField("0.0",8);
        text1.setBackground(Color.white);
        label2 = new JLabel(" + b1 * ");
        text2 = new JTextField("1.0",8);
        text2.setBackground(Color.white);
        pinfields.add(label1);
        pinfields.add(text1);
        pinfields.add(label2);
        pinfields.add(text2);
        cdgain.add(pinfields);
        JPanel poutfields = new JPanel(new FlowLayout(FlowLayout.CENTER));
        label3 = new JLabel("Output Gain = a2");
        text3 = new JTextField("0.0",8);
        text3.setBackground(Color.white);
        label4 = new JLabel(" + b2 * ");
        text4 = new JTextField("1.0",8);
        text4.setBackground(Color.white);
        poutfields.add(label3);
        poutfields.add(text3);
        poutfields.add(label4);
        poutfields.add(text4);
        cdgain.add(poutfields);
        JPanel pcontrol = new JPanel(new GridLayout(1,0,5,5));
        bOK =new JButton("OK");
        bOK.setActionCommand("ok");
        bOK.addActionListener(this);
        pcontrol.add(bOK);
        bApply =new JButton("Apply");
        bApply.setActionCommand("apply");
        bApply.addActionListener(this);
        pcontrol.add(bApply);
        JButton bCancel =new JButton("Cancel");
        bCancel.setActionCommand("cancel");
        bCancel.addActionListener(this);
        pcontrol.add(bCancel);
        cdgain.add(pcontrol);
        dgain.pack();
    }

    /**
     * Are we done setting gate and should we save it
     * or has the gate setting been canceled.
     *
     */
    public void actionPerformed(ActionEvent e){

        try {
            if (e.getActionCommand()=="ok"||e.getActionCommand()=="apply"){
                doGainShift();
                if (e.getActionCommand()=="ok") {
                    cancel();
                }
            } else if(e.getActionCommand()=="cancel") {
                cancel();
            } else  {
                throw new DataException("Not a recognized command {GateControl]");
            }
        } catch (DataException je) {
            messageHandler.errorOutln( je.getMessage() );
        } catch (GlobalException ge) {
            messageHandler.errorOutln(getClass().getName()+
            ".actionPerformed(): "+ge);
        }
    }
    /**
     * A item state change indicates that a gate has been choicen
     *
     */
    public void itemStateChanged(ItemEvent ie){
        if(ie.getSource()==ccoeff||ie.getSource()==cchan){
            try{
                setUseCoeff(ccoeff.isSelected());
            } catch (DataException de) {
                messageHandler.errorOutln(de.getMessage());
            }
        }  else if (ie.getSource()==cto) {
            if (cto.getSelectedItem() != null) {
                setUseNewHist (cto.getSelectedItem().equals("New Histogram"));
            }
        }
    }

    /**
     * Implementation of Observable interface
     * to receive broad cast events.
     * Listen for histograms new, histogram added
     */
    public void update(Observable observable, Object o){
        BroadcastEvent be=(BroadcastEvent)o;

        if(be.getCommand()==BroadcastEvent.HISTOGRAM_NEW){
            setup();
        } else if(be.getCommand()==BroadcastEvent.HISTOGRAM_ADD){
            setup();
        }
    }

    /**
     * Loads the list of gates and
     * set co-ordinates as x y if 2d
     * or lower upper if 1 d
     *
     */
    public void setup(){
        String lfrom=JamStatus.getCurrentHistogramName();
        String lto=(String)cto.getSelectedItem();
        setFromHist(lfrom);
        cto.removeAllItems();
        cto.addItem("New Histogram");
        addChooserHists(cto,Histogram.ONE_DIM_INT,Histogram.ONE_DIM_DOUBLE);
        cto.setSelectedItem(lto);
        if (lto.equals("New Histogram")){
            setUseNewHist(true);
        } else {
            setUseNewHist(false);
        }
    }

    /**
     * Show gate setter dialog box
     */
    public void show(){
        dgain.show();
    }

    /**
     *	Cancel the setting of the gate and
     *  disable editting of all fields
     *
     */
    private void cancel(){
        dgain.dispose();
    }

    /**
     *
     */
    private void setUseCoeff(boolean state) throws DataException{

        if (state) {
            label1.setText("Input Gain: ");
            label2.setText("+ channel x");
            label3.setText("Output Gain");
            label4.setText("+ channel x");
            getChannels();
            calculateCoefficients();
            text1.setText(format(a1));
            text2.setText(format(b1));
            text3.setText(format(a2));
            text4.setText(format(b2));
        } else {
            label1.setText("Map Channel");
            label2.setText("to");
            label3.setText("Map Channel");
            label4.setText("to");
            getCoefficients();
            calculateChannels();
            text1.setText(format(chan1i));
            text2.setText(format(chan1f));
            text3.setText(format(chan2i));
            text4.setText(format(chan2f));
        }
    }

    private void addChooserHists(JComboBox c, int type1, int type2){
        for (Enumeration e=Histogram.getHistogramList().elements();e.hasMoreElements();){
            Histogram h=(Histogram)e.nextElement();
            if (h.getType()==type1||h.getType()==type2) c.addItem(h.getName());
        }
    }

    /**
     * set dialog box for a new histogram to
     * be writtern out.
     */
    private void setUseNewHist(boolean state){
        if (state) {
            ttextto.setBackground(Color.white);
        } else {
            ttextto.setBackground(Color.lightGray);
        }
        lname.setEnabled(state);
        ttextto.setEnabled(state);
    }

    private void setFromHist(String name){
        hfrom = Histogram.getHistogram(name);
        if (hfrom == null || hfrom.getDimensionality()==2){
            tfrom.setText("Need 1D Hist!");
            hfrom=null;
            bOK.setEnabled(false);
            bApply.setEnabled(false);
        } else {
            tfrom.setText(name);
            bOK.setEnabled(true);
            bApply.setEnabled(true);
        }
    }

    /**
     * Does the work of manipulating histograms
     */
    private void doGainShift() throws DataException,GlobalException {

        Histogram hto;
        String name;
        double [] in,out;
        double [] errIn,errOut;
        int j;

        //get coeff. or channels
        if (cchan.isSelected()) {
            getChannels();
            calculateCoefficients();
        } else {
            getCoefficients();
        }

        //get input histogram
        if (hfrom.getType()==Histogram.ONE_DIM_INT){
            in = toDoubleArray((int[])hfrom.getCounts());
        } else {
            in = (double [])hfrom.getCounts();
        }
        errIn = hfrom.getErrors();

        //get or create output histogram
        name = (String)cto.getSelectedItem();
        if (name.equals("New Histogram")){
            name  = ttextto.getText().trim();
            hto = new Histogram(name, Histogram.ONE_DIM_DOUBLE, hfrom.getSizeX(),name);
            broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
            messageHandler.messageOutln("New Histogram created: '"+name+"'");
        } else {
            hto = Histogram.getHistogram(name);
        }
        hto.setZero();

        if (hto.getType()==Histogram.ONE_DIM_INT){
            out = toDoubleArray((int[])hto.getCounts());
        } else {
            out = ((double[])hto.getCounts());
        }
        errOut = hto.getErrors();

        out = gainShift(in,a1,b1,a2,b2,out.length);
        errOut = errorGainShift(errIn,a1,b1,a2,b2,errOut.length);
        if (hto.getType()==Histogram.ONE_DIM_INT){
            hto.setCounts(toIntArray(out));
        } else {
            hto.setCounts(out);
        }
        hto.setErrors(errOut);

        messageHandler.messageOutln("GainShift "+hfrom.getName()+" to "+ name);
        messageHandler.messageOutln("Initial gain: "+format(a1)+" + "+format(b1)+" x ch; Final gain: "+
        format(a2)+" + "+format(b2) + " x ch");
    }
    /**
     * get the coeff. from the text fields
     */
    private void getCoefficients() throws DataException{
        try {
            a1 = Double.valueOf(text1.getText().trim()).doubleValue();
            b1 = Double.valueOf(text2.getText().trim()).doubleValue();
            a2 = Double.valueOf(text3.getText().trim()).doubleValue();
            b2 = Double.valueOf(text4.getText().trim()).doubleValue();
        } catch (NumberFormatException nfe){
            throw new DataException("A Coefficient is not a valid number [GainShift]");
        }

    }
    /**
     * get the channels from the text fields
     */
    private void getChannels() throws DataException{
        try {
            chan1i = Double.valueOf(text1.getText().trim()).doubleValue();
            chan1f = Double.valueOf(text2.getText().trim()).doubleValue();
            chan2i = Double.valueOf(text3.getText().trim()).doubleValue();
            chan2f = Double.valueOf(text4.getText().trim()).doubleValue();
        } catch (NumberFormatException nfe){
            throw new DataException("A Channel is not a valid number [GainShift]");
        }

    }
    /**
     * calculate coeff. if given channels
     */
    private void calculateCoefficients() {

        a1 = 0.0; //if using channels, gain just gives channel
        b1 = 1.0; //see line above
        a2 = (chan2f*chan1i-chan1f*chan2i)/(chan2f-chan1f);
        if (chan1f != 0.0){//avoid divide by zero errors
            b2 = (chan1i-a2)/chan1f;
        } else {
            b2 = (chan2i-a2)/chan2f;
        }

    }
    /**
     * calculate channels if given coeff.
     */
    private void calculateChannels() {
        chan1f = (a1+b1*chan1i-a2)/b2;
        chan2f = (a1+b1*chan2i-a2)/b2;
    }

    /**
     * Gain-shifting subroutine adapted from Fortran code written and used at the Nuclear Physics
     * Laboratory at University of
     * Washigton, Seattle.
     *
     * @param	y1	input array of counts
     * @param	a1	constant calibration coefficient of y1
     * @param	b1	linear calibration coefficient of y1
     * @param	a2	constant calibration coefficient for output array
     * @param	b2	linear calibration coefficient for output array
     * @param	npts2	desired size of output array
     * @return	new array of size <code>npts</code> re-binned for new gain coefficients
     */
    private double [] gainShift(double [] y1,double a1,double b1,double a2, double b2,int npts2)
    throws DataException{

        double [] y2;
        double e1lo,e1hi,x2lo,x2hi;
        int mlo,mhi;

        //create and zero new array
        y2 = new double[npts2];
        for (int i=0;i<y2.length;i++) {
            y2[i] = 0.0;
        }

        //loop for each channel of original array
        for (int n=0;n<y1.length;n++) {
            e1lo=a1+b1*((double)n-0.5);	    //energy at lower edge of spec#1 channel
            e1hi=a1+b1*((double)n+0.5);	    //energy at upper edge of spec#1 channel
            x2lo=(e1lo-a2)/b2;		    //fractional chan#2 corresponding to e1lo
            x2hi=(e1hi-a2)/b2;		    //fractional chan#2 corresponding to e1hi
            mlo=(int)(x2lo+0.5);		    //channel corresponding to x2low
            mhi=(int)(x2hi+0.5);		    //channel corresponding to x2hi


            //if beyond limits of array set to limit.
            if(mlo < 0) mlo=0;
            if(mhi < 0) mhi=0;
            if(mlo >= npts2) mlo=npts2-1;
            if(mhi >= npts2) mhi=npts2-1;

            //treat the 3 cases below
            if((mlo >= 0) && (mhi < npts2)){

                // sp#1 chan fits within one sp#2 chan
                if(mhi == mlo){
                    y2[mlo]=y2[mlo]+y1[n];

                    //sp#1 chan falls into two sp#2 chans
                } else if(mhi == (mlo+1)){
                    y2[mlo]=y2[mlo]+y1[n]*(mlo+0.5-x2lo)/(x2hi-x2lo);
                    y2[mhi]=y2[mhi]+y1[n]*(x2hi-mhi+0.5)/(x2hi-x2lo);

                    //sp#1 chan covers several sp#2 chans
                } else if(mhi > mlo+1){
                    for(int i=mlo+1;i<=mhi-1;i++){
                        y2[i]=y2[i]+y1[n]/(x2hi-x2lo);
                    }
                    y2[mlo]=y2[mlo]+y1[n]*(mlo+0.5-x2lo)/(x2hi-x2lo);
                    y2[mhi]=y2[mhi]+y1[n]*(x2hi-mhi+0.5)/(x2hi-x2lo);
                } else {
                    throw new DataException("Something is wrong: n = "+n+", mlo = "+mlo+", mhi = "+mhi);
                }
            }
        }
        //test debug
        double sum=0;
        for(int i=0;i<y2.length;i++){
            sum=sum+y2[i];
        }

        return y2;
    }

    /**
     * Error terms gain-shifting subroutine adapted from Fortran code written and used at
     * the Nuclear Physics
     * Laboratory at University of
     * Washigton, Seattle.
     *
     * @param	y1	input array of counts
     * @param	a1	constant calibration coefficient of y1
     * @param	b1	linear calibration coefficient of y1
     * @param	a2	constant calibration coefficient for output array
     * @param	b2	linear calibration coefficient for output array
     * @param	npts2	desired size of output array
     * @return	new array of size <code>npts</code> re-binned for new gain coefficients
     */
    private double [] errorGainShift(double [] y1,double a1,double b1,double a2, double b2,int npts2)
    throws DataException{

        int i,m,n;
        //int npts1 = y1.length		//size of y1 array
        //int npts2 = y2.length		// size of y2 array
        double [] y2;
        double e1lo,e1hi,x2lo,x2hi;
        int mlo,mhi;

        y2 = new double[npts2];
        for (m=0;m<y2.length;m++) {
        	y2[m] = 0.0;
        }
        for (n=0;n<y1.length;n++) {
            e1lo=a1+b1*(n-0.5);	//energy at lower edge of spec#1 channel
            e1hi=a1+b1*(n+0.5);	//energy at upper edge of spec#1 channel
            x2lo=(e1lo-a2)/b2;	//fractional chan#2 corresponding to e1lo
            x2hi=(e1hi-a2)/b2;	//fractional chan#2 corresponding to e1hi
            mlo=(int)(x2lo+0.5);
            mhi=(int)(x2hi+0.5);
            if(mlo < 0) mlo=0;
            if(mhi < 0) mhi=0;
            if(mlo >= npts2) mlo=npts2-1;
            if(mhi >= npts2) mhi=npts2-1;
            if((mlo >= 0) && (mhi < npts2)){
                if(mhi == mlo){		// sp#1 chan fits within one sp#2 chan
                    y2[mlo]=y2[mlo]+y1[n];
                } else if(mhi == mlo+1){	//sp#1 chan falls into two sp#2 chans
                    y2[mlo]=Math.sqrt(y2[mlo]*y2[mlo]+Math.pow(y1[n]*(mlo+0.5-x2lo)/(x2hi-x2lo),2.0));
                    y2[mhi]=y2[mhi]+y1[n]*(x2hi-mhi+0.5)/(x2hi-x2lo);
                } else if(mhi > mlo+1){	//sp#1 chan covers several sp#2 chans
                    for(i=mlo+1;i<=mhi-1;i++){
                        y2[i]=y2[i]+y1[n]/(x2hi-x2lo);
                    }
                    y2[mlo]=y2[mlo]+y1[n]*(mlo+0.5-x2lo)/(x2hi-x2lo);
                    y2[mhi]=y2[mhi]+y1[n]*(x2hi-mhi+0.5)/(x2hi-x2lo);
                } else {
                    throw new DataException("Something is wrong: n = "+n+", mlo = "+mlo+", mhi = "+mhi);
                }
            }
        }
        return y2;
    }
    /**
     * Converts int array to double array
     */
    private double [] toDoubleArray(int [] in){
        double [] out;

        out = new double[in.length];
        for (int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }

    /**
     * Converts double array to int array
     */
    private int [] toIntArray(double [] in){
        int [] out;

        out = new int[in.length];
        for (int i = 0; i < in.length; i++){
            out[i] = (int)Math.round(in[i]);
        }
        return out;
    }

    /*
     * format a number
     */
    private String format(double value){
        int integer,fraction;
        NumberFormat fval;

        integer = (int)log10(Math.abs(value));
        integer = Math.max(integer,1);
        fraction = Math.max(7-integer,0);
        fval=NumberFormat.getInstance();
        fval.setGroupingUsed(false);
        fval.setMinimumFractionDigits(fraction);
        fval.setMinimumFractionDigits(fraction);
        fval.setMinimumIntegerDigits(integer);
        return fval.format(value);
    }

    private double log10(double x){
        return Math.log(x)/Math.log(10.0);
    }
    /**
     *  Process window events
     *  If the window is active check that the histogram been displayed
     *  has not changed. If it has cancel the gate setting.
     */
    public void windowActivated(WindowEvent e){
        String name;

        name=JamStatus.getCurrentHistogramName();
        if (!name.equals(tfrom.getText())){
            setFromHist(name);
            //setupChannels();
        }
    }

    /**
     * Window Events
     *  windowClosing only one used.
     */
    public void windowClosing(WindowEvent e){
        cancel();
        dgain.dispose();
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
