package jam.data.control;

import jam.data.*;
import jam.global.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Class for projecting 2-D histograms.
 *
 * @author Dale Visser
 */
public class Projections extends DataControl implements ActionListener, ItemListener, WindowListener,
Observer {

    private static final String FULL="Full Histogram";
    private static final String BETWEEN="Between Channels";
    private static final String NEW_HIST="New Histogram";

    private Frame frame;
    private Broadcaster broadcaster;
    private MessageHandler messageHandler;

    private JDialog dproject;
    private JComboBox cto, cchan;
    private JCheckBox cacross,cdown;
    private JTextField tlim1, tlim2, ttextto,tfrom;
    private JLabel lname;
    JButton bOK =new JButton("OK");
    JButton bApply =new JButton("Apply");

    private Histogram hfrom;
    private JamStatus status;

    public Projections(Frame frame, Broadcaster broadcaster, MessageHandler messageHandler){
        super();
        this.frame=frame;
        this.broadcaster=broadcaster;
        this.messageHandler=messageHandler;
        status = JamStatus.instance();
        dproject=new JDialog(frame,"Project 2D Histogram",false);
        dproject.setResizable(false);

        final int CHOOSER_SIZE=200;
        Dimension dim;
        int rows=5;
        int cols=1;
        int hgap=5;
        int vgap=10;

        Container cdproject = dproject.getContentPane();
        cdproject.setLayout(new BorderLayout(hgap,vgap));
        dproject.setLocation(20,50);
        dproject.addWindowListener(this);

        JPanel pLabels = new JPanel(new GridLayout(0,1,hgap,vgap));
        pLabels.setBorder(new EmptyBorder(20,10,0,0));
        cdproject.add(pLabels, BorderLayout.WEST);
        pLabels.add(new JLabel("Project histogram", JLabel.RIGHT));
        pLabels.add(new JLabel("Direction", JLabel.RIGHT));
        pLabels.add(new JLabel("Region", JLabel.RIGHT));
        pLabels.add(new JLabel("To histogram", JLabel.RIGHT));

		//Entries Panel
        JPanel pEntries = new JPanel(new GridLayout(0,1,hgap,vgap));
        pEntries.setBorder(new EmptyBorder(20,0,0,10));
        cdproject.add(pEntries, BorderLayout.CENTER);

		//From histogram
        JPanel phist = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        tfrom = new JTextField("2DHISTOGRAM",20);
        dim= tfrom.getPreferredSize();
        dim.width=CHOOSER_SIZE;
        tfrom.setPreferredSize(dim);
        tfrom.setEditable(false);
        phist.add(tfrom);
        pEntries.add(phist);

		//Direction panel
        JPanel pradio = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        ButtonGroup cbg = new ButtonGroup();
        cacross = new JCheckBox("Across",true);
        cdown = new JCheckBox("Down",false);
        cbg.add(cacross);
        cbg.add(cdown);
        pradio.add(cacross);
        pradio.add(cdown);
        pEntries.add(pradio);

		//Channels panel
        JPanel pchannel = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        tlim1 = new JTextField(5);
        tlim1.setForeground(Color.black);
        tlim2 = new JTextField(5);
        tlim2.setForeground(Color.black);
        cchan = new JComboBox();
        dim= cchan.getPreferredSize();
        dim.width=CHOOSER_SIZE;
        cchan.setPreferredSize(dim);
        cchan.addItem(FULL);
        cchan.addItem(BETWEEN);
        cchan.addItemListener(this);
        pchannel.add(cchan);
        setUseLimits(false);
        pchannel.add(new JLabel("Channels"));
        pchannel.add(tlim1);
        pchannel.add(new JLabel("and"));
        pchannel.add(tlim2);
        pEntries.add(pchannel);


		//To histogram
        JPanel ptextto = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        cto   = new JComboBox();
        dim= cto.getPreferredSize();
        dim.width=CHOOSER_SIZE;
        cto.setPreferredSize(dim);
        cto.addItem("1DHISTOGRAM");
        cto.addItemListener(this);
        ptextto.add(cto);
        lname=new JLabel("Name");
        ptextto.add(lname);
        ttextto = new JTextField("projection",20);
        ttextto.setForeground(Color.black);
        setUseNewHist(false);
        ptextto.add(ttextto);
        pEntries.add(ptextto);


		//Buttons panel
		JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cdproject.add(pButtons,  BorderLayout.SOUTH);
        JPanel pcontrol = new JPanel(new GridLayout(1,0,5,5));
        pButtons.add(pcontrol);
        bOK.setActionCommand("ok");
        bOK.addActionListener(this);
        pcontrol.add(bOK);
        bApply.setActionCommand("apply");
        bApply.addActionListener(this);
        pcontrol.add(bApply);
        JButton bCancel =new JButton("Cancel");
        bCancel.setActionCommand("cancel");
        bCancel.addActionListener(this);
        pcontrol.add(bCancel);

        dproject.pack();
    }

    /**
     * Are we done setting gate and should we save it
     * or has the gate setting been canceled.
     *
     */
    public void actionPerformed(ActionEvent e){

        try {
            if (e.getActionCommand()=="ok"||e.getActionCommand()=="apply"){
                project();
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
        if (ie.getSource()==cto) {
            if (cto.getSelectedItem() != null) {
                setUseNewHist(cto.getSelectedItem().equals(NEW_HIST));
            }
        } else if (ie.getSource()==cchan) {
            if (cchan.getSelectedItem() != null) {
                setUseLimits(cchan.getSelectedItem().equals(BETWEEN));
            }
        }
    }

    /**
     *Implementation of Observable interface
     * Listners for broadcast events.
     * Broadcast events:
     * histograms new and histogram added
     */
    public void update(Observable observable, Object o){
        BroadcastEvent be=(BroadcastEvent)o;
        if(be.getCommand()==BroadcastEvent.HISTOGRAM_NEW){
            setup();
        } else if(be.getCommand()==BroadcastEvent.HISTOGRAM_ADD){
            setupAdd();
        } else if(be.getCommand()==BroadcastEvent.GATE_ADD){
            setupAdd();
        }
    }

    /**
     * Show gate setter dialog box
     */
    public void show(){
        dproject.show();
    }

    /**
     *	Cancel the setting of the gate and
     * disable editting of all fields
     *
     */
    private void cancel(){
        dproject.dispose();
    }

    /**
     * Loads the name of the current histogram and
     * the list of gates for that histogram.
     * For a 1d histogram set co-ordinates as x y if 2d
     * or lower and upper if 1 d
     *
     */
    public void setup(){
        setFromHist(status.getCurrentHistogramName());
        setUseNewHist(true);	//default use new histogram
        setupToHist(NEW_HIST);//setup "to" histogram
        setupCuts(FULL);//default setup channels
    }

    /**
     * a new histogram or gate has been added to the
     * database
     */
    private void setupAdd(){

        String lastCut = (String)cchan.getSelectedItem();
        String lastHist= (String)cto.getSelectedItem();

        //setup up to histogram
        setupToHist(lastHist);

        //setup channels
        setupCuts(lastCut);

    }
    /**
     * Adds a list of histograms to a choose
     */
    private void setupToHist(String newSelect){
        cto.removeAllItems();
        cto.addItem(NEW_HIST);
        for (Iterator e=Histogram.getHistogramList().iterator();e.hasNext();){
            Histogram h=(Histogram)e.next();
            if ((h.getType()==Histogram.ONE_DIM_INT)||(h.getType()==Histogram.ONE_DIM_DOUBLE)){
                cto.addItem(h.getName());
            }
        }
        cto.setSelectedItem(newSelect);
    }

    /**
     * Setups up the channel and gate selector.
     *
     */
    private void setupCuts(String newSelect){
        cchan.removeAllItems();
        //add default options
        cchan.addItem(FULL);
        cchan.addItem(BETWEEN);
        //add gates to chooser
        if (hfrom  != null){
            final java.util.List g=hfrom.getGates();
            for (Iterator it=g.iterator(); it.hasNext();){
            //for (int i = 0;i < len;i++){
      			//final Gate gate=(Gate)g.get(i);
      			final Gate gate=(Gate)it.next();
                cchan.addItem(gate.getName());
            }
        }
        cchan.setSelectedItem(newSelect);
        if (newSelect.equals(BETWEEN)){
            setUseLimits(true);
        } else {
            setUseLimits(false);
        }
    }

    /**
     * setup if using a new histogram
     */
    private void setUseNewHist(boolean state){
        lname.setEnabled(state);
        ttextto.setEnabled(state);
        if (state) {
            ttextto.setBackground(Color.white);
        } else {
            ttextto.setBackground(Color.lightGray);
        }
    }

    /**
     * setup if using limits
     */
    private void setUseLimits(boolean state){
        if (state) {
            tlim1.setBackground(Color.white);
            tlim2.setBackground(Color.white);
        } else {
            tlim1.setBackground(Color.lightGray);
            tlim2.setBackground(Color.lightGray);
        }
        tlim1.setEnabled(state);
        tlim2.setEnabled(state);
    }

    /**
     *
     */
    private void setFromHist(String name){
        hfrom = Histogram.getHistogram(name);
        if (hfrom == null || hfrom.getDimensionality()==1){
            tfrom.setText("Need 2D Hist!");
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
     * Does the work of projecting a histogram
     */
    private void project() throws DataException, GlobalException {
        Histogram hto;
        String name,state,typeProj;
        double [][] counts2d;
        int [] limits = new int[2];

        state = (String) cchan.getSelectedItem();
        if (hfrom.getType()==Histogram.TWO_DIM_DOUBLE){
        	counts2d=(double [][])hfrom.getCounts();
        } else {
        	counts2d=intToDouble((int [][])hfrom.getCounts());
        }
        name = (String) cto.getSelectedItem();
        if (state.equals(BETWEEN)) {
            limits = getLimits();
        } else if (state.equals(FULL)) {
            limits[0]=0;
            if (cdown.isSelected()) {
                limits[1] = counts2d[0].length-1;
            } else {
                limits[1] = counts2d.length-1;
            }
        }
        if (name.equals(NEW_HIST)){
            name  = ttextto.getText().trim();
            if (cdown.isSelected()){//project down
				hto = new Histogram(name, Histogram.ONE_DIM_DOUBLE, hfrom.getSizeX(),name);
            } else {//project across
				hto = new Histogram(name, Histogram.ONE_DIM_DOUBLE, hfrom.getSizeY(),name);
            }
            broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
            messageHandler.messageOutln("New Histogram created: '"+name+"'");
        } else {
            hto = Histogram.getHistogram(name);

        }
        if (cdown.isSelected()) {
            if (state.equals(FULL)||state.equals(BETWEEN)){
                typeProj="counts between Y channels "+limits[0]+" and "+limits[1];
                hto.setCounts(projectX(counts2d,((double [])hto.getCounts()).length,limits[0],limits[1]));
            } else {
                typeProj="using gate "+state;
                hto.setCounts(projectX(counts2d,((double [])hto.getCounts()).length,Gate.getGate(state)));
            }
        } else { // cacross is true
            if (state.equals(FULL)||state.equals(BETWEEN)){
                typeProj="counts between X channels "+limits[0]+" and "+limits[1];
                hto.setCounts(projectY(counts2d,((double [])hto.getCounts()).length,limits[0],limits[1]));
            } else {
                typeProj="using gate "+state.trim();
                hto.setCounts(projectY(counts2d,((double [])hto.getCounts()).length,Gate.getGate(state)));
            }
        }
        messageHandler.messageOutln("Project "+hfrom.getName().trim()+" to "+ name.trim()+" "+typeProj);
    }

    private double [][] intToDouble(int [][] in){
		double [][] rval = new double[in.length][in[0].length];
		for (int i=0; i<in.length; i++){
			for (int j=0; j<in[0].length; j++){
				rval[i][j]=in[i][j];
			}
		}
		return rval;
    }

    double [] projectX(double [][] inArray, int outLength, int _ll, int _ul){
        double [] out = new double [outLength];
        int ll = Math.max(0,_ll);
        int ul = Math.min(inArray[0].length-1,_ul);
        for (int k=0; k<outLength; k++) {
            out[k]=0;
        }
        for (int i=0; i < inArray.length; i++) {
            for (int j=ll; j<=ul; j++){
                out[i] += inArray[i][j];
            }
        }
        return out;
    }

    double [] projectY(double [][] inArray, int outLength, int _ll, int _ul){
		double [] out = new double [outLength];
        int ll = Math.max(0,_ll);
        int ul = Math.min(inArray.length-1,_ul);
        for (int k=0; k<outLength; k++) {
            out[k]=0;
        }
        for (int i=ll; i <=ul; i++) {
            for (int j=0; j<inArray[0].length; j++){
                out[j] += inArray[i][j];
            }
        }
        return out;
    }


    double [] projectX(double [][] inArray, int outLength, Gate gate) throws DataException {
		double [] out = new double [outLength];
        for (int k=0; k<outLength; k++) {
            out[k]=0;
        }
        for (int i=0;i<inArray.length;i++){
            for (int j=0;j<inArray[0].length;j++){
                if (gate.inGate(i,j)) {
                    if (i<out.length) out[i]+=inArray[i][j];
                }
            }
        }
        return out;
    }

    double [] projectY(double [][] inArray, int outLength, Gate gate) throws DataException {
		double [] out = new double [outLength];
        for (int k=0; k<outLength; k++) {
            out[k]=0;
        }
        for (int i=0;i<inArray.length;i++){
            for (int j=0;j<inArray[0].length;j++){
                if (gate.inGate(i,j)) {
                    if (j<out.length) out[j]+=inArray[i][j];
                }
            }
        }
        return out;
    }

    private int [] getLimits() throws DataException {
        int [] out = new int[2];
        try {
            out[0]=Integer.parseInt(tlim1.getText().trim());
            out[1]=Integer.parseInt(tlim2.getText().trim());
            if (out[0]>out[1]){
                int temp=out[0];
                out[0]=out[1];
                out[1]=temp;
            }
        } catch (NumberFormatException ne) {
            throw new DataException("Invalid channel not a valid number [Projections]");
        }
        return out;
    }

    /**
     *  Process window events
     *  If the window is active check that the histogram been displayed
     *  has not changed. If it has cancel the gate setting.
     */
    public void windowActivated(WindowEvent e){
        String name = status.getCurrentHistogramName();
        if (!name.equals(tfrom.getText())){
            setFromHist(name);
            setupCuts(FULL);
        }
    }

    /**
     * Window Events
     *  windowClosing only one used.
     */
    public void windowClosing(WindowEvent e){
        cancel();
        dproject.dispose();
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
