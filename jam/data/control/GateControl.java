package jam.data.control;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import jam.global.*;
import jam.data.*;
import javax.swing.*;

/**
 * Class to set 1 D and 2 D gates.
 *
 * @version 0.5 April 1998
 * @author Ken Swartz
 */
public class GateControl extends DataControl implements ActionListener, WindowListener,
Observer  {

    static final int ONE_DIMENSION=1;
    static final int TWO_DIMENSION=2;
    static final int NONE=-1;

    private Frame frame;
    private Broadcaster broadcaster;
    private MessageHandler messageHandler;

    private Histogram currentHistogram;
    private Gate currentGate;
    private Gate currentGateAdd;

    private int type;
    private java.util.List gatePoints;      //number intial points, increment increase
    private Polygon gatePoly2d;
    private int numberPoints;

    /* set gate dialog box */
    private JDialog dgate;
    private JComboBox cgate;
    private GateControlComboBoxModel cgateModel;
    private JLabel lLower;
    private JTextField textLower;
    private JLabel lUpper;
    private JTextField textUpper;
    private JButton addP;
    private JButton removeP;
    private JButton unset;
    private JButton save;
    private JButton cancel;

    /* new gate dialog box */
    private JDialog dnew;
    private JTextField textNew;
	private GateControlComboBoxModel2 caddModel;
	
    /* add gate dialog box */
    private JDialog dadd;
    private JComboBox cadd;

    boolean newGate=false;//a gate has been chosen
    
    private JamStatus status;

    /**
     *
     */
    public GateControl(Frame frame, Broadcaster broadcaster,  MessageHandler messageHandler){
        super();
        this.frame=frame;
        this.broadcaster=broadcaster;
        this.messageHandler=messageHandler;
        status = JamStatus.instance();
        dgate=new JDialog(frame,"Gate setting <none>",false);
        dgate.setResizable(false);
        //dgate.setSize(300, 250);
        Container contents=dgate.getContentPane();
        contents.setLayout(new BorderLayout());
        dgate.setLocation(20,50);
        //panel with chooser
        JPanel pc =new JPanel();
        pc.setLayout(new GridLayout(1,0));
        cgateModel = new GateControlComboBoxModel(this);
        cgate=new JComboBox(cgateModel);
        pc.add(cgate);
        //panel with data fields
        JPanel pf =new JPanel();
        pf.setLayout(new GridLayout(2,1));
        JPanel p1= new JPanel(new FlowLayout());
        lLower=new JLabel("lower",Label.RIGHT);
        p1.add(lLower);
        textLower=new JTextField("",4);
        textLower.setBackground(Color.lightGray);
        textLower.setForeground(Color.black);
        p1.add(textLower);
        JPanel p2= new JPanel(new FlowLayout());
        lUpper=new JLabel("upper",Label.RIGHT);
        p2.add(lUpper);
        textUpper=new JTextField("",4);
        textUpper.setBackground(Color.lightGray);
        textUpper.setForeground(Color.black);
        p2.add(textUpper);
        pf.add(p1); pf.add(p2);
        //panel with buttons
        JPanel pedit =new JPanel();
        pedit.setLayout(new GridLayout(0,1));
        addP = new JButton("Add");
        addP.setActionCommand("add");
        addP.addActionListener(this);
        addP.setEnabled(false);
        pedit.add(addP);
        removeP=new JButton("Remove");
        removeP.setActionCommand("remove");
        removeP.addActionListener(this);
        removeP.setEnabled(false);
        pedit.add(removeP);
        unset=new JButton("Unset");
        unset.setActionCommand("unset");
        unset.addActionListener(this);
        unset.setEnabled(false);
        pedit.add(unset);
        //panel with buttons
        Panel pb =new Panel();
        pb.setLayout(new GridLayout(1,0));
        save = new JButton("Save");
        save.setActionCommand("save");
        save.addActionListener(this);
        save.setEnabled(false);
        pb.add(save);
        cancel=new JButton("Cancel");
        cancel.setActionCommand("cancel");
        cancel.addActionListener(this);
        cancel.setEnabled(false);
        pb.add(cancel);
        contents.add(pc,BorderLayout.NORTH);
        contents.add(pf,BorderLayout.CENTER);
        contents.add(pedit,BorderLayout.EAST);
        contents.add(pb,BorderLayout.SOUTH);
        dgate.addWindowListener(this);
        dgate.pack();

        //new gate dialog box
        dnew=new JDialog(frame,"New Gate",false);
        Container cdnew=dnew.getContentPane();
        dnew.setResizable(false);
        //dnew.setSize(300, 150);
        cdnew.setLayout(new BorderLayout());
        dnew.setLocation(20,50);

        //panel with chooser
        JPanel ptnew =new JPanel();
        ptnew.setLayout(new GridLayout(1,1));
        cdnew.add(ptnew,BorderLayout.CENTER);

        JLabel lnew =new JLabel("Name");
        cdnew.add(lnew,BorderLayout.WEST);

        textNew=new JTextField("",12);
        textNew.setBackground(Color.white);
        ptnew.add(textNew);

        // panel for buttons
        JPanel pbnew= new JPanel();
        pbnew.setLayout(new GridLayout(1,3));
        cdnew.add(pbnew,BorderLayout.SOUTH);

        JButton bok  =   new JButton("OK");
        bok.setActionCommand("oknew");
        bok.addActionListener(this);
        pbnew.add(bok);

        JButton bapply = new JButton("Apply");
        bapply.setActionCommand("applynew");
        bapply.addActionListener(this);
        pbnew.add(bapply);

        JButton bcancel =new JButton("Cancel");
        bcancel.setActionCommand("cancelnew");
        bcancel.addActionListener(this);
        pbnew.add(bcancel);
        dnew.pack();

        //add gate dialog box
        dadd=new JDialog(frame,"Add Gate",false);
        Container cdadd=dadd.getContentPane();
        dadd.setResizable(false);
        dadd.setLocation(20,50);

        //panel with chooser
        JPanel ptadd =new JPanel();
        ptadd.setLayout(new GridLayout(1,0));
        cdadd.add(ptadd,BorderLayout.CENTER);

		caddModel = new GateControlComboBoxModel2(this);
        cadd=new JComboBox(caddModel);
        
        ptadd.add(cadd);

        // panel for buttons
        JPanel pbadd= new JPanel();
        pbadd.setLayout(new GridLayout(1,0));
        cdadd.add(pbadd,BorderLayout.SOUTH);

        JButton bokadd  =   new JButton("OK");
        bokadd.setActionCommand("okadd");
        bokadd.addActionListener(this);
        pbadd.add(bokadd);

        JButton bapplyadd = new JButton("Apply");
        bapplyadd.setActionCommand("applyadd");
        bapplyadd.addActionListener(this);
        pbadd.add(bapplyadd);

        JButton bcanceladd =new JButton("Cancel");
        bcanceladd.setActionCommand("canceladd");
        bcanceladd.addActionListener(this);
        pbadd.add(bcanceladd);

        dadd.addWindowListener(this);
        dadd.pack();

    }
    /**
     * Are we done setting gate and should we save it
     * or has the gate setting been canceled.
     *
     */
    public void actionPerformed(ActionEvent e){

        String command=e.getActionCommand();

        try {
            if (command=="save"){
                save();
            } else if(command=="cancel") {
                cancel();
            } else if(command=="add") {
                addPoint();
            } else if(command=="remove") {
                removePoint();
            } else if(command.equals("unset")){
            	unset();
            //commands for new dialog box
            } else if((command=="oknew")|| (command=="applynew")){
                makeGate();
                if (command=="oknew"){
                    dnew.dispose();
                }
            } else if  (command=="cancelnew"){
                dnew.dispose();

                //commands for add dialog box
            } else if((command=="okadd")|| (command=="applyadd")){
                addGate();
                if (command=="okadd"){
                    dadd.dispose();
                }
            } else if  (command=="canceladd"){
                dadd.dispose();
            } else  {

                System.err.println(" Error Not a recognized command [GateControl]");
            }
        } catch (DataException je) {
            messageHandler.errorOutln( je.getMessage() );
        } catch (GlobalException ge) {
            messageHandler.errorOutln(getClass().getName()+
            ".actionPerformed(): "+ge);
        }
    }

    void selectGate(String name) {
        try {
            cancel();      //cancel current state
            currentGate=Gate.getGate(name);
            if ( currentGate != null) {
                newGate=true;            //setting a new gate
                numberPoints=0;
                if (currentHistogram.getDimensionality()==1) {
                    type=ONE_DIMENSION;
                    lLower.setText("lower");
                    lUpper.setText("upper");
                    gatePoints = new ArrayList(2);
                } else {
                    type=TWO_DIMENSION;
                    lLower.setText("x");
                    lUpper.setText("y");
                    gatePoints = new ArrayList();
                    addP.setEnabled(true);
                    //FIXME
                    removeP.setEnabled(true);
                }
                unset.setEnabled(true);
                broadcaster.broadcast(BroadcastEvent.GATE_SET_ON);
                //change the title of the dialog
                dgate.setTitle("Gate setting "+ currentGate.getName());
                //make fields and buttons active
                textLower.setText("");
                textLower.setEditable(true);
                textLower.setBackground(Color.white);
                textUpper.setText("");
                textUpper.setEditable(true);
                textUpper.setBackground(Color.white);
                save.setEnabled(true);
                cancel.setEnabled(true);
            }
        } catch (GlobalException ge) {
            messageHandler.errorOutln(getClass().getName()+
            ".selectGate(): "+ge);
        }
    }

    void selectGateAdd(String name) {
        currentGateAdd=Gate.getGate(name);
    }

    /**
     *Implementation of Observable interface
     * To receive broadcast events
     */
    public void update(Observable observable, Object o){
        BroadcastEvent be=(BroadcastEvent)o;
        try {
            if(be.getCommand()==BroadcastEvent.HISTOGRAM_SELECT){
                cancel();
            } else if(be.getCommand()==BroadcastEvent.HISTOGRAM_NEW){
                setup();
            } else if(be.getCommand()==BroadcastEvent.HISTOGRAM_ADD){
                setup();
            } else if(be.getCommand()==BroadcastEvent.GATE_ADD){
                setup();
            } else if(be.getCommand()==BroadcastEvent.GATE_SET_POINT){
                addPoint((Point)be.getContent());
            }
        } catch (GlobalException ge) {
            messageHandler.errorOutln(getClass().getName()+
            ".update(): "+ge);
        }
    }

    /**
     * Show gate setter dialog box
     */
    public void showSet(){
        dgate.show();
    }

    /**
     * Show new gate dialog box
     */
    public void showNew(){
        dnew.show();
    }
    /**
     * Show add gate dialog box
     */
    public void showAdd(){
        dadd.show();
    }

    /**
     * Loads the list of gates and
     * set co-ordinates as x y if 2d
     * or lower upper if 1 d
     *
     */
    public void setup(){
        String typeGate="gate";
        cgateModel.changeOccured();
        caddModel.changeOccured();
        /* get current state */
        currentHistogram = Histogram.getHistogram(status.getCurrentHistogramName());
        if (currentHistogram == null) {
            /* There are many normal situations with no current histogram. */
            type=NONE;//undefined type
        } else if ((currentHistogram.getType()==Histogram.ONE_DIM_INT)||
        (currentHistogram.getType()==Histogram.ONE_DIM_DOUBLE)) {
            type=Gate.ONE_DIMENSION;
            typeGate="gate 1-D";
        } else if ((currentHistogram.getType()==Histogram.TWO_DIM_INT)||
        (currentHistogram.getType()==Histogram.TWO_DIM_DOUBLE)) {
            type=Gate.TWO_DIMENSION;
            typeGate="gate 2-D";
        } else {
            System.err.println("GateControl undefined histogram type ");
            type=NONE;
        }
        cgate.setSelectedIndex(0);
        cadd.setSelectedIndex(0);
        //change labels depending if we have a one or two D histogram
        if (currentHistogram != null && currentHistogram.getDimensionality()==1) {
            type=ONE_DIMENSION;
            lLower.setText(" lower");
            lUpper.setText(" upper");
        } else {
            type=TWO_DIMENSION;
            lLower.setText("  x  ");
            lUpper.setText("  y  ");
        }
    }

    /**
     * make a new gate
     */
    private void makeGate() throws GlobalException {
        Histogram hist=Histogram.getHistogram(status.getCurrentHistogramName());
        new Gate(textNew.getText(),hist);
        broadcaster.broadcast(BroadcastEvent.GATE_ADD);
        messageHandler.messageOutln("New gate "+textNew.getText()+" created for histogram "+hist.getName());
    }

    /**
     * Add a gate
     *
     */
    private void addGate() throws DataException,GlobalException {
        if(currentGateAdd!=null) {
            Histogram hist=Histogram.getHistogram(status.getCurrentHistogramName());
            hist.addGate(currentGateAdd);
            broadcaster.broadcast(BroadcastEvent.GATE_ADD);
            messageHandler.messageOutln("Added gate '"+currentGateAdd.getName().trim()+"' to histogram '"+hist.getName()+"'");
        } else {
            messageHandler.errorOutln("Need to choose a gate to add ");
        }
    }

    /**
     * add a point from the text field
     */
    private void addPoint() throws DataException,GlobalException {
        int x;
        int y;
        Point p;

        try {
            x=Integer.parseInt(textLower.getText().trim());
            y=Integer.parseInt(textUpper.getText().trim());
            p=new Point(x,y);
            addPoint(p);
            broadcaster.broadcast(BroadcastEvent.GATE_SET_ADD, p);
        } catch (NumberFormatException ne) {
            throw new DataException("Invalid input not a number [GateSet]");
        }
    }
    /**
     * remove a point in setting a 2d gate
     */
    private void removePoint() throws GlobalException {
        if(!gatePoints.isEmpty()) {
            gatePoints.remove(gatePoints.size()-1);
            broadcaster.broadcast(BroadcastEvent.GATE_SET_REMOVE);
            if(!gatePoints.isEmpty()) {
            	Point lastPoint=(Point)gatePoints.get(gatePoints.size()-1);
                textLower.setText(""+lastPoint.x);
                textUpper.setText(""+lastPoint.y);
            } else {
                textLower.setText("");
                textUpper.setText("");
            }
        }
    }
    
	private void unset(){
		currentGate.unsetLimits();
		messageHandler.messageOutln("Gate UnSet: "+currentGate.getName());
	}
    
    /**
     * Add a point to the gate
     * when we are setting a new gate,
     */
    private void addPoint(Point pChannel){

        if (newGate) {    //do nothing if no gate choicen

            if (type==ONE_DIMENSION){
                if (numberPoints==0) {
                    numberPoints=1;
                    gatePoints.add(pChannel);
                    textLower.setText(""+pChannel.x);
                } else if (numberPoints==1) {
                    numberPoints=0;
                    gatePoints.add(pChannel);
                    textUpper.setText(""+pChannel.x);
                } else {
                    System.err.println("Error: setting 1 d gate should not be here [GateControl]");
                }

            } else if (type==TWO_DIMENSION){
                gatePoints.add(pChannel);
                textLower.setText(""+pChannel.x);
                textUpper.setText(""+pChannel.y);
            }
        } else {
            //should not be here
            System.err.println("Error: Gatesetter no new gate for plotMousePressed [GateControl]");
        }

    }

    /**
     * Save the gate value.
     */
    private void save() throws DataException, GlobalException {
        int x1,x2,pointX,pointY;

        gatePoly2d= new Polygon();
        checkHistogram();    //check we have same histogram
        //check fields are numbers
        try {
            if (currentGate!=null) {
                if (type==ONE_DIMENSION) {
                    x1=Integer.parseInt(textLower.getText());
                    x2=Integer.parseInt(textUpper.getText());
                    currentGate.setLimits(x1, x2);
                    messageHandler.messageOutln("Gate Set "+currentGate.getName()+" Limits="+x1+","+x2);
                } else if (type==TWO_DIMENSION) {
                    /* complete gate, adding a last point = first point */
                    gatePoints.add(gatePoints.get(0));
                    /* make a polygon from data points */
                    for (int i=0; i<gatePoints.size();i++ ) {
                        pointX=((Point)gatePoints.get(i)).x;
                        pointY=((Point)gatePoints.get(i)).y;
                        gatePoly2d.addPoint(pointX,pointY);
                    }
                    currentGate.setLimits(gatePoly2d);
                    messageHandler.messageOutln("Gate Set "+currentGate.getName());
                    printPoints(gatePoly2d);
                }
                broadcaster.broadcast(BroadcastEvent.GATE_SET_SAVE);
            }
        } catch (NumberFormatException ne) {
            throw new DataException("Invalid input not a number [GateSet]");
        }
        cancel();
    }

    /**
     * output the list of gate points
     */
    private void printPoints(Polygon gatePoly2d){
        int x[]=gatePoly2d.xpoints;
        int y[]=gatePoly2d.ypoints;
        messageHandler.messageOut("Gate points: ",MessageHandler.NEW);
        for (int i=0; i<gatePoly2d.npoints; i++){
            messageHandler.messageOut("["+x[i]+","+y[i]+"] ");
        }
        messageHandler.messageOut("",MessageHandler.END);
    }

    /**
     *  Cancel the setting of the gate and
     * disable editting of all fields
     */
    private void cancel() throws GlobalException {
        checkHistogram();
        broadcaster.broadcast(BroadcastEvent.GATE_SET_OFF);
        dgate.setTitle("Gate setting <none>");
        newGate=false;
        gatePoints=null;
        textLower.setText(" ");
        textLower.setEditable(false);
        textLower.setBackground(Color.lightGray);
        textUpper.setText(" ");
        textUpper.setEditable(false);
        textUpper.setBackground(Color.lightGray);
        addP.setEnabled(false);
        removeP.setEnabled(false);
        save.setEnabled(false);
        cancel.setEnabled(false);
        unset.setEnabled(false);
    }
    
    
    /**
     * Check that plot's current histogram has not changed.
     * If so, cancel and make the plot's current histogram
     * our current histogram.
     *
     * @Author Ken Swartz
     */
    private void checkHistogram() throws GlobalException {
        //has histogram changed
        if(currentHistogram != Histogram.getHistogram(status.getCurrentHistogramName())) {
            //setup chooser list
            setup();
            //cancel current gate if was setting
            cancel();
        }
    }

    /**
     *  Process window events
     *  If the window is active check that the histogram been displayed
     *  has not changed. If it has cancel the gate setting.
     *
     */
    public void windowActivated(WindowEvent e) {
        try {
            checkHistogram();
        } catch (GlobalException ge) {
            messageHandler.errorOutln(getClass().getName()+
            ".windowActivated(): "+ge);
        }
    }

    /**
     * Window Events
     *  windowClosing only one used.
     */
    public void windowClosing(WindowEvent e){
        if(e.getSource()==dnew) {
            dnew.dispose();
        } else if(e.getSource()==dgate)  {
            try {
                cancel();
            } catch (GlobalException ge) {
                messageHandler.errorOutln(getClass().getName()+
                ".windowClosing(): "+ge);
            }
            dgate.dispose();
        } else if (e.getSource()==dadd) {
            dadd.dispose();
        }
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
