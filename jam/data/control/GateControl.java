package jam.data.control;
import jam.GateComboBoxModel;
import jam.GateListCellRenderer;
import jam.data.DataException;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GlobalException;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.plot.Display;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;

/**
 * Class to set 1 D and 2 D gates.
 *
 * @version 0.5 April 1998
 * @author Ken Swartz
 */
public class GateControl extends DataControl implements ActionListener,
WindowListener,Observer  {

    static final int ONE_DIMENSION=1;
    static final int TWO_DIMENSION=2;
    static final int NONE=-1;

    private boolean newGate=false;//a gate has been chosen
    private final Frame frame;
    private final Broadcaster broadcaster;
    private final MessageHandler messageHandler;

    private Histogram currentHistogram;
    private Gate currentGate;
    private Gate currentGateAdd;

    private int type;
    private java.util.List gatePoints;//number intial points, increment increase
    private int numberPoints;

    /* set gate dialog box */
    private final JDialog dgate;
    final private JComboBox cgate;
    private final JLabel lLower;
    private final JTextField textLower;
    private final JLabel lUpper;
    private final JTextField textUpper;
    private final JButton addP;
    private final JButton removeP;
    private final JButton unset;
    private final JButton save;
    private final JButton cancel;

    /* new gate dialog box */
    private final JDialog dnew;
    private final JTextField textNew;

    /* add gate dialog box */
    private final JDialog dadd;
    final private JComboBox cadd;

    private final JamStatus status;
    private final Display display;

    /**
     * Creates an instance of the GateControl class.
     *
     * @param f the frame that GateControl's dialogs are children of
     * @param bro ???
     * @param mh the console for text output
     */
    public GateControl(Frame f, Broadcaster bro,
    MessageHandler mh, Display d){
        super();
        this.frame=f;
        this.broadcaster=bro;
        this.messageHandler=mh;
        display=d;
        status = JamStatus.instance();

        //>>Gate setting dialog
        dgate=new JDialog(frame,"Gate setting <none>",false);
        dgate.setResizable(false);
        final Container contents=dgate.getContentPane();
        contents.setLayout(new BorderLayout());
        dgate.setLocation(20,50);

        //panel with chooser
        JPanel pc =new JPanel();
        pc.setLayout(new FlowLayout(FlowLayout.CENTER,20,20));
        cgate=new JComboBox(new GateComboBoxModel());
		Dimension dimset = cgate.getPreferredSize();
		dimset.width=200;
		cgate.setPreferredSize(dimset);
        cgate.setRenderer(new GateListCellRenderer());
        cgate.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent ae){
        		Object item=cgate.getSelectedItem();
        		if (item instanceof Gate){
					selectGate((Gate)item);
        		}
        	}
        });
        pc.add(cgate);

        // panel with data fields
        final JPanel pf =new JPanel();
        pf.setLayout(new GridLayout(2,1));
        final JPanel p1= new JPanel(new FlowLayout());
        lLower=new JLabel("lower",Label.RIGHT);
        p1.add(lLower);
        textLower=new JTextField("",4);
        p1.add(textLower);
        final JPanel p2= new JPanel(new FlowLayout());
        lUpper=new JLabel("upper",Label.RIGHT);
        p2.add(lUpper);
        textUpper=new JTextField("",4);
        p2.add(textUpper);
        pf.add(p1); pf.add(p2);

        // panel with buttons add remove buttons
        final JPanel pedit =new JPanel();
        pedit.setLayout(new GridLayout(3,1,5,5));
        Border border = new EmptyBorder(0,0,10,30);
        pedit.setBorder(border);

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

        // panel with OK, Cancel buttons
        final JPanel pokcancel =new JPanel(new FlowLayout(FlowLayout.CENTER));
        final Panel pb =new Panel();
        pb.setLayout(new GridLayout(1,0,5,5));
        pokcancel.add(pb);
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
        contents.add(pokcancel,BorderLayout.SOUTH);

        dgate.addWindowListener(this);
        dgate.pack();

        //>>new gate dialog box
        dnew=new JDialog(frame,"New Gate",false);
        final Container cdnew=dnew.getContentPane();
        dnew.setResizable(false);
        cdnew.setLayout(new BorderLayout(5,5));
        dnew.setLocation(20,50);

        /* panel with chooser */
        final JPanel ptnew =new JPanel();
        ptnew.setLayout(new FlowLayout(FlowLayout.LEFT,20,20));
        cdnew.add(ptnew,BorderLayout.CENTER);
        ptnew.add(new JLabel("Name"));
        textNew=new JTextField("",20);
        //textNew.setBackground(Color.white);
        ptnew.add(textNew);

        /*  panel for buttons */
        final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cdnew.add(pbutton,BorderLayout.SOUTH);

        final JPanel pbnew= new JPanel();
        pbnew.setLayout(new GridLayout(1,0,5,5));
        pbutton.add(pbnew,BorderLayout.SOUTH);

        final JButton bok  =   new JButton("OK");
        bok.setActionCommand("oknew");
        bok.addActionListener(this);
        pbnew.add(bok);

        final JButton bapply = new JButton("Apply");
        bapply.setActionCommand("applynew");
        bapply.addActionListener(this);
        pbnew.add(bapply);

        final JButton bcancel =new JButton("Cancel");
        bcancel.setActionCommand("cancelnew");
        bcancel.addActionListener(this);
        pbnew.add(bcancel);
        dnew.pack();

        //>>Add gate dialog box
        dadd=new JDialog(frame,"Add Gate",false);
        final Container cdadd=dadd.getContentPane();
        dadd.setResizable(false);
        dadd.setLocation(20,50);

        //panel with chooser
        final JPanel ptadd =new JPanel();
        ptadd.setLayout(new FlowLayout(FlowLayout.CENTER,20,20));
        cdadd.add(ptadd,BorderLayout.CENTER);

        cadd=new JComboBox(new GateComboBoxModel(GateComboBoxModel.Mode.ALL));
        cadd.setRenderer(new GateListCellRenderer());
		cadd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				final Object item=cadd.getSelectedItem();
				if (item instanceof Gate){
					selectGateAdd((Gate)item);
				}
			}
		});
		Dimension dimadd = cadd.getPreferredSize();
		dimadd.width=200;
		cadd.setPreferredSize(dimadd);
        ptadd.add(cadd);

        // panel for buttons
        final JPanel pbuttonAdd = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cdadd.add(pbuttonAdd,BorderLayout.SOUTH);

        final JPanel pbadd= new JPanel();
        pbadd.setLayout(new GridLayout(1,0,5,5));
        pbuttonAdd.add(pbadd);

        final JButton bokadd  =   new JButton("OK");
        bokadd.setActionCommand("okadd");
        bokadd.addActionListener(this);
        pbadd.add(bokadd);

        final JButton bapplyadd = new JButton("Apply");
        bapplyadd.setActionCommand("applyadd");
        bapplyadd.addActionListener(this);
        pbadd.add(bapplyadd);

        final JButton bcanceladd =new JButton("Cancel");
        bcanceladd.setActionCommand("canceladd");
        bcanceladd.addActionListener(this);
        pbadd.add(bcanceladd);

        dadd.addWindowListener(this);
        dadd.pack();

    }
    /**
     * Respond to user action.
     *
     * @param e event caused by user
     */
    public void actionPerformed(ActionEvent e){
        final String command=e.getActionCommand();
        try {
            if ("save".equals(command)){
                save();
            } else if("cancel".equals(command)) {
                cancel();
            } else if("add".equals(command)) {
                addPoint();
            } else if("remove".equals(command)) {
                removePoint();
            } else if("unset".equals(command)){
            	unset();
            //commands for new dialog box
            } else if(("oknew".equals(command))|| "applynew".equals(command)){
                makeGate();
                if ("oknew".equals(command)){
                    dnew.dispose();
                }
            } else if  ("cancelnew".equals(command)){
                dnew.dispose();
            /* commands for add dialog box */
            } else if(("okadd".equals(command))|| ("applyadd".equals(command))){
                addGate();
                if ("okadd".equals(command)){
                    dadd.dispose();
                }
            } else if  ("canceladd".equals(command)){
                dadd.dispose();
            } else  {
				messageHandler.errorOutln(getClass().getName()+
				".actionPerformed(): '"+command+
				"' not a recognized command.");
            }
        } catch (DataException je) {
            messageHandler.errorOutln( je.getMessage() );
        } catch (GlobalException ge) {
            messageHandler.errorOutln(getClass().getName()+
            ".actionPerformed(): "+ge);
        }
    }

    void selectGate(Gate gate) {
        try {
            cancel();      //cancel current state
            synchronized(this){
            	currentGate=gate;
            }
            if ( currentGate != null) {
            	synchronized(this){
                	newGate=true;//setting a new gate
                	numberPoints=0;
                }
                if (currentHistogram.getDimensionality()==1) {
                    lLower.setText("lower");
                    lUpper.setText("upper");
                    synchronized (this) {
                    	type=ONE_DIMENSION;
                    	gatePoints = new ArrayList(2);
                    }
                } else {
                    lLower.setText("x");
                    lUpper.setText("y");
                    synchronized (this) {
                    	type=TWO_DIMENSION;
                    	gatePoints = new ArrayList();
                    }
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
                //textLower.setBackground(Color.white);
                textUpper.setText("");
                textUpper.setEditable(true);
                //textUpper.setBackground(Color.white);
                save.setEnabled(true);
                cancel.setEnabled(true);
            }
        } catch (GlobalException ge) {
            messageHandler.errorOutln(getClass().getName()+
            ".selectGate(): "+ge);
        }
    }

    void selectGateAdd(Gate gate) {
        synchronized (this) {
        	currentGateAdd=gate;
        }
    }

    /**
     * Implementation of Observable interface
     * To receive broadcast events.
     *
     * @param observable the event sender
     * @param o the message
     */
    public void update(Observable observable, Object o){
        final BroadcastEvent be=(BroadcastEvent)o;
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
        /* get current state */
        synchronized(this){
        	currentHistogram = Histogram.getHistogram(
        	status.getCurrentHistogramName());
        }
        if (currentHistogram == null) {
            /* There are many normal situations with no current histogram. */
            synchronized(this){
            	type=NONE;//undefined type
            }
        } else if ((currentHistogram.getType()==Histogram.ONE_DIM_INT)||
        (currentHistogram.getType()==Histogram.ONE_DIM_DOUBLE)) {
            synchronized (this) {
            	type=Gate.ONE_DIMENSION;
            }
        } else if ((currentHistogram.getType()==Histogram.TWO_DIM_INT)||
        (currentHistogram.getType()==Histogram.TWO_DIM_DOUBLE)) {
            synchronized (this) {
            	type=Gate.TWO_DIMENSION;
            }
        } else {
            messageHandler.errorOutln(getClass().getName()+
            ".setup(): undefined histogram type.");
            synchronized(this){
            	type=NONE;
            }
        }
        cgate.setSelectedIndex(0);
        cadd.setSelectedIndex(0);
        //change labels depending if we have a one or two D histogram
        if (currentHistogram != null &&
        currentHistogram.getDimensionality()==1) {
            synchronized(this){
            	type=ONE_DIMENSION;
            }
            lLower.setText(" lower");
            lUpper.setText(" upper");
        } else {
            synchronized(this){
            	type=TWO_DIMENSION;
            }
            lLower.setText("  x  ");
            lUpper.setText("  y  ");
        }
    }

    /**
     * Make a new gate, and add it to the current histogram.
     *
     * @throws GlobalException if there's a problem
     */
    private void makeGate() throws GlobalException {
        final Histogram hist=Histogram.getHistogram(
        status.getCurrentHistogramName());
        new Gate(textNew.getText(),hist);
        broadcaster.broadcast(BroadcastEvent.GATE_ADD);
        messageHandler.messageOutln("New gate "+textNew.getText()+
        " created for histogram "+hist.getName());
    }

    /**
     * Add a gate.
     *
     * @throws DataException if there's a problem
     * @throws GlobalException if there's a problem
     */
    private void addGate() throws DataException,GlobalException {
        if(currentGateAdd!=null) {
            final Histogram hist=Histogram.getHistogram(
            status.getCurrentHistogramName());
            hist.addGate(currentGateAdd);
            broadcaster.broadcast(BroadcastEvent.GATE_ADD);
            messageHandler.messageOutln("Added gate '"+
            currentGateAdd.getName().trim()+"' to histogram '"+hist.getName()+"'");
        } else {
            messageHandler.errorOutln("Need to choose a gate to add ");
        }
    }

    /**
     * Add a point from the text fields.
     *
     * @throws DataException if there's a problem with the
     * number format
     * @throws GlobalException if there's additional problems
     */
    private void addPoint() throws DataException,GlobalException {
        try {
            final int x=Integer.parseInt(textLower.getText().trim());
            final int y=Integer.parseInt(textUpper.getText().trim());
            final Point p=new Point(x,y);
            addPoint(p);
            broadcaster.broadcast(BroadcastEvent.GATE_SET_ADD, p);
        } catch (NumberFormatException ne) {
            throw new DataException("Invalid input not a number [GateSet]");
        }
    }
    /**
     * remove a point in setting a 2d gate
     *
     * @throws GlobalException if there's a problem
     */
    private void removePoint() throws GlobalException {
        if(!gatePoints.isEmpty()) {
            gatePoints.remove(gatePoints.size()-1);
            broadcaster.broadcast(BroadcastEvent.GATE_SET_REMOVE);
            if(!gatePoints.isEmpty()) {
            	final Point lastPoint=(Point)gatePoints.get(gatePoints.size()-1);
                textLower.setText(String.valueOf(lastPoint.x));
                textUpper.setText(String.valueOf(lastPoint.y));
            } else {
                textLower.setText("");
                textUpper.setText("");
            }
        }
    }

	private void unset(){
		currentGate.unsetLimits();
		try{
			cgate.repaint();
			messageHandler.messageOutln("Gate UnSet: "+currentGate.getName());
			cancel();
			broadcaster.broadcast(BroadcastEvent.GATE_SET_OFF);
		} catch (GlobalException ge){
			messageHandler.errorOutln(getClass().getName()+".unset(): "+
			ge.getMessage());
		}
	}

    /**
     * Add a point to the gate
     * when we are setting a new gate.
     *
     * @param pChannel the point corresponding to the channel to add
     */
    private void addPoint(Point pChannel){
        if (newGate) {    //do nothing if no gate chosen
            if (type==ONE_DIMENSION){
                if (numberPoints==0) {
                    synchronized(this){
                    	numberPoints=1;
                    }
                    gatePoints.add(pChannel);
                    textLower.setText(String.valueOf(pChannel.x));
                } else if (numberPoints==1) {
                    synchronized(this){
                    	numberPoints=0;
                    }
                    gatePoints.add(pChannel);
                    textUpper.setText(String.valueOf(pChannel.x));
                } else {
                    messageHandler.errorOutln(getClass().getName()+
                    ".addPoint(): setting 1 d gate should not be here.");
                }
            } else if (type==TWO_DIMENSION){
                gatePoints.add(pChannel);
                textLower.setText(String.valueOf(pChannel.x));
                textUpper.setText(String.valueOf(pChannel.y));
            }
        } else {
            messageHandler.errorOutln(getClass().getName()+
            ".addPoint(Point): an expected condition was not true. "+
            "Contact the developer.");
        }
    }

    /**
     * Save the gate value.
     *
     * @throws DataException if there's a problem
     * @throws GlobalException if there's a problem
     */
    private void save() throws DataException, GlobalException {
        final int x1,x2;
        int pointX,pointY;
    	Polygon gatePoly2d;

        synchronized(this){
        	gatePoly2d= new Polygon();
        }
        checkHistogram();    //check we have same histogram
        try {//check fields are numbers
            if (currentGate!=null) {
                if (type==ONE_DIMENSION) {
                    x1=Integer.parseInt(textLower.getText());
                    x2=Integer.parseInt(textUpper.getText());
                    currentGate.setLimits(x1, x2);
                    messageHandler.messageOutln("Gate Set "+
                    currentGate.getName()+" Limits="+x1+","+x2);
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
                    messageHandler.messageOutln("Gate Set "+
                    currentGate.getName());
                    printPoints(gatePoly2d);
                }
                display.displayGate(currentGate);
                cgate.repaint();
                cadd.repaint();
				broadcaster.broadcast(BroadcastEvent.GATE_SET_SAVE);
            }
        } catch (NumberFormatException ne) {
            throw new DataException("Invalid input not a number [GateSet]");
        }
        cancel();
    }

    /**
     * Output the list of gate points to the console.
     *
     * @param poly the points defining the gate
     */
    private void printPoints(Polygon poly){
        final int x[]=poly.xpoints;
        final int y[]=poly.ypoints;
        messageHandler.messageOut("Gate points: ",MessageHandler.NEW);
        for (int i=0; i<poly.npoints; i++){
            messageHandler.messageOut("["+x[i]+","+y[i]+"] ");
        }
        messageHandler.messageOut("",MessageHandler.END);
    }

    /**
     * Cancel the setting of the gate and
     * disable editting of all fields.
     *
     * @throws GlobalException if there's a problem
     */
    private void cancel() throws GlobalException {
        checkHistogram();
        broadcaster.broadcast(BroadcastEvent.GATE_SET_OFF);
        dgate.setTitle("Gate setting <none>");
        synchronized(this){
        	newGate=false;
        	gatePoints=null;
        }
        textLower.setText(" ");
        textLower.setEditable(false);
        //textLower.setBackground(Color.lightGray);
        textUpper.setText(" ");
        textUpper.setEditable(false);
        //textUpper.setBackground(Color.lightGray);
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
     * @author Ken Swartz
     * @throws GlobalException if there's a problem
     */
    private void checkHistogram() throws GlobalException {
        /* has histogram changed? */
        if(currentHistogram !=
        Histogram.getHistogram(status.getCurrentHistogramName())) {
            setup();//setup chooser list
            cancel();//cancel current gate if was setting
        }
    }

    /**
     * Process window events.
     * If the window is active check that the histogram been
     * displayed has not changed. If it has cancel the gate
     * setting.
     *
     * @param e event causing window to be activated
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
     * @param e event telling of window closing
     */
    public void windowClosing(WindowEvent e){
    	final Object source=e.getSource();
        if(source.equals(dnew)) {
            dnew.dispose();
        } else if(source.equals(dgate))  {
            try {
                cancel();
            } catch (GlobalException ge) {
                messageHandler.errorOutln(getClass().getName()+
                ".windowClosing(): "+ge);
            }
            dgate.dispose();
        } else if (source.equals(dadd)) {
            dadd.dispose();
        }
    }

    /**
     * @param e event causing window to be closed
     */
    public void windowClosed(WindowEvent e){
        /* does nothing for now */
    }

    /**
     * @param e event causing window to be deactivated
     */
    public void windowDeactivated(WindowEvent e){
        /* does nothing for now */
    }

    /**
     * @param e event causing window to be de-iconified
     */
    public void windowDeiconified(WindowEvent e){
        /* does nothing for now */
    }

    /**
     * @param e event causing window to be iconified
     */
    public void windowIconified(WindowEvent e){
        /* does nothing for now */
    }

    /**
     * @param e event causing window to be opened
     */
    public void windowOpened(WindowEvent e){
        setup();
    }
}
