package jam.data.control;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jam.global.*;
import jam.util.*;
import jam.data.*;
import javax.swing.*;

/**
 * Reads and displays the monitors.
 *
 * @version  September 2000
 * @author   Ken Swartz
 * @author Dale Visser
 * @since       JDK1.1
 */
public class MonitorControl extends DataControl implements ActionListener, ItemListener,
Runnable  {

    /**
     * Master frame
     */
    private Frame frame;
    /**
     *
     */
    private Broadcaster broadcaster;
    private MessageHandler msgHandler;

    //widgets for configuration
    private JDialog dconfig;
    private JPanel plabel;
    private JPanel[] pc;      //panel for configurations
    private JLabel[] labelConfig;
    private JTextField[] textThreshold;
    private JTextField[] textMaximum;
    private JCheckBox [] checkAlarm;
    private JPanel pupdate;        //panel with update time
    private JTextField textUpdate;
    private JPanel  pb;          //panel with buttons
    private JButton brecall, bok, bapply, bcancel;

    //widgits for display
    private JDialog ddisp;
    private JPanel [] pm;    //panel for monitors
    private JLabel[] labelDisp;
    private JTextField[] textValue;
    private PlotBar [] plotBar;

    //panel with alarm enable
    private JPanel pal;
    private JCheckBox checkAudio;
    private JButton bstop;

    //array of monitors loaded at setuptime
    private Monitor [] monitor;
    private int numberMonitors;

    //general variables
    private boolean sortMonitors;      //have Monitors been added by sort

    private int interval;        //update interval
    private boolean audioAlarm;        //is the audio alarm on
    private GoodThread loopThread;        //loop to update monitors
    private boolean configured;        //monitors have been configured

    public MonitorControl(Frame frame, Broadcaster broadcaster, MessageHandler msgHandler){
        super();
        this.frame=frame;
        this.broadcaster=broadcaster;
        this.msgHandler=msgHandler;
        //dialog panel and widgets for configure Monitors
        dconfig=new JDialog(frame," Monitors Setup ",false);
        Container cdconfig=dconfig.getContentPane();
        dconfig.setResizable(false);
        dconfig.setLocation(20,50);
        //dconfig.setSize(300,150);
        int spacing=5;
        cdconfig.setLayout(new GridLayout(0,1,spacing,spacing));

        plabel=new JPanel(new FlowLayout(FlowLayout.RIGHT,spacing,spacing));
        //plabel=new JPanel(new GridLayout(1,4,spacing,spacing));
        JLabel labelTemp=new JLabel("Name",JLabel.LEFT);
        plabel.add(labelTemp);
        labelTemp=new JLabel("Threshold",JLabel.LEFT);
        plabel.add(labelTemp);
        labelTemp=new JLabel("Maximum",JLabel.LEFT);
        plabel.add(labelTemp);
        labelTemp=new JLabel("Alarm",JLabel.RIGHT);
        plabel.add(labelTemp);

        //panel for input fields
        pupdate=new JPanel();
        pupdate.setLayout(new FlowLayout(FlowLayout.CENTER,2,2));
        cdconfig.add(pupdate);

        JLabel lUpdate=new JLabel("Update",JLabel.RIGHT);
        pupdate.add(lUpdate);

        textUpdate =new JTextField("          ");
        textUpdate.setColumns(4);
        textUpdate.setEditable(true);
        textUpdate.setBackground(Color.white);
        pupdate.add(textUpdate);

        JLabel lunit=new JLabel("sec",JLabel.LEFT);
        pupdate.add(lunit);

        //panel for buttons
        pb=new JPanel();
        pb.setLayout(new FlowLayout(FlowLayout.CENTER,2,2));

        brecall =  new JButton("Recall");
        brecall.setActionCommand("recall");
        brecall.addActionListener(this);
        pb.add(brecall);

        bok =  new JButton("OK");
        bok.setActionCommand("ok");
        bok.addActionListener(this);
        pb.add(bok);

        bapply =  new JButton("Apply");
        bapply.setActionCommand("apply");
        bapply.addActionListener(this);
        pb.add(bapply);

        bcancel =  new JButton("Cancel");
        bcancel.setActionCommand("cancel");
        bcancel.addActionListener(this);
        pb.add(bcancel);
        cdconfig.add(pb);

        // dialog box to display Monitors
        ddisp=new JDialog(frame,"Monitors Disabled", false);
        ddisp.setResizable(false);
        ddisp.setLocation(20,50);
        Container cddisp=ddisp.getContentPane();
        cddisp.setLayout(new GridLayout(0,2,5,5));

        // alarm panel for display dialog
        pal= new JPanel();
        pal.setLayout(new FlowLayout(FlowLayout.CENTER,10,5));

        checkAudio =new JCheckBox("Audio Alarm", true );
        checkAudio.addItemListener(this);
        pal.add(checkAudio);

        //variable initialization
        sortMonitors=false;
        configured=false;

        //window listeners for dispose
        ddisp.addWindowListener(
        new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                ddisp.dispose();
            }
        }
        );

        dconfig.addWindowListener(
        new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                dconfig.dispose();
            }
        }
        );
        //setup monitors
        setup();
    }

    /**
     * Action events in configuration window
     */
    public void actionPerformed(ActionEvent ae){

        String command=ae.getActionCommand();

        try {
            if (command=="recall") {
                recall();

            } else if ((command=="ok")||(command=="apply")) {

                configure();
                //lock monitor parameters
                start();
                if(command=="ok"){
                    dconfig.dispose();
                }

            } else if (command=="cancel") {
                configured=false;
                //stop monitor thread if running
                stop();
                ddisp.setTitle("Monitors Disabled");

            } else if (command=="start"){
                start();

            } else if (command=="stop"){
                stop();
            }

        } catch (DataException je) {
            msgHandler.errorOutln(je.getMessage());
        }
    }
    /**
     * Checks the alarm states and sets them
     *
     *
     */
    public void itemStateChanged(ItemEvent ie){
        if (ie.getItemSelectable()==checkAudio) {
            audioAlarm=checkAudio.isSelected();
        }
    }

    /**
     * Setup the monitors
     *
     */
    public void setup(){
        //Monitor currentMonitor;
        //Enumeration enumMonitor;
        //int count;

        numberMonitors=Monitor.getMonitorList().size();
        if (numberMonitors!=0){// we have monitors in the Monitor list
            sortMonitors=true;
            Enumeration enumMonitor=Monitor.getMonitorList().elements();
            monitor = new Monitor[numberMonitors];
            int count=0;
            while(enumMonitor.hasMoreElements()) {//put montitors into the monitor array
                Monitor currentMonitor=(Monitor)enumMonitor.nextElement();
                monitor[count]=currentMonitor;
                count++;
            }
        }
        //setup dialog boxes
        setupConfig();
        setupDisplay();
    }

    /**
     * Setup the display dialog box.
     *
     */
    private void setupDisplay(){
        //ddisp.setResizable(true);
        //ddisp.setSize(400, 150+35*numberMonitors);
        Container cddisp=ddisp.getContentPane();
        cddisp.removeAll();
        //widgets for dislay page
        pm=new JPanel[numberMonitors];
        labelDisp=new JLabel[numberMonitors];
        textValue =new JTextField [numberMonitors];
        plotBar=new PlotBar[numberMonitors];
        if (numberMonitors!=0) {
            for (int i=0;i<numberMonitors;i++) {
                pm[i]= new JPanel();
                pm[i].setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
                cddisp.add(pm[i]);
                labelDisp[i]=new JLabel  ("          ",JLabel.RIGHT);
                labelDisp[i].setText(monitor[i].getName());
                pm[i].add(labelDisp[i]);
                textValue[i] =new JTextField("           ");
                textValue[i].setColumns(6);
                textValue[i].setEditable(false);
                textValue[i].setBackground(Color.white);
                textValue[i].setText( String.valueOf(0) );
                pm[i].add(textValue[i]);
                plotBar[i]=new PlotBar(monitor[i]);
                cddisp.add(plotBar[i]);
            }
        }
        cddisp.add(pal);
        ddisp.pack();
        //ddisp.setResizable(false);
    }
    /**
     * setup configuration dialog box
     *
     */
    private void setupConfig(){
        //dconfig.setResizable(true);
        //dconfig.setSize(350, 260+35*numberMonitors);
        Container cdconfig=dconfig.getContentPane();
        cdconfig.removeAll();

        cdconfig.add(plabel);

        //widgets for configuration page
        pc=new JPanel[numberMonitors];
        labelConfig=new JLabel[numberMonitors];
        textThreshold =new JTextField [numberMonitors];
        textMaximum = new JTextField [numberMonitors];
        checkAlarm=new JCheckBox[numberMonitors];

        //for each monintor make a panel with label, threshold, maximum, and alarm
        if (numberMonitors!=0) {
            for (int i=0;i<numberMonitors;i++) {
                pc[i]= new JPanel();
                pc[i].setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
                //pc[i].setLayout(new GridLayout(1,4,0,0));
                cdconfig.add(pc[i]);

                labelConfig[i]=new JLabel  ("          ",JLabel.RIGHT);
                labelConfig[i].setText(monitor[i].getName());
                pc[i].add(labelConfig[i]);

                textThreshold[i] =new JTextField("          ");
                textThreshold[i].setColumns(6);
                textThreshold[i].setEditable(true);
                textThreshold[i].setBackground(Color.white);
                textThreshold[i].setText("10");
                pc[i].add(textThreshold[i]);

                textMaximum[i] =new JTextField("          ");
                textMaximum[i].setColumns(6);
                textMaximum[i].setEditable(true);
                textMaximum[i].setBackground(Color.white);
                textMaximum[i].setText("100");
                pc[i].add(textMaximum[i]);

                checkAlarm[i]= new JCheckBox(/*"Alarm"*/);
                checkAlarm[i].setSelected(false);
                pc[i].add(checkAlarm[i]);
            }
        }
        cdconfig.add(pupdate);
        cdconfig.add(pb);
        //dconfig.setResizable(false);
        dconfig.pack();
    }
    /**
     * Show the Display dialog box
     *
     */
    public void showDisplay(){
        ddisp.show();
    }
    /**
     * Show the configuration dialog box
     *
     */
    public void showConfig(){
        dconfig.show();
    }
    /**
     * recal the monitors parameters
     * and set the input fields
     */
    void recall() {

        //update interval
        textUpdate.setText(""+interval);

        //get the Monitor parameters
        for (int i=0;i<numberMonitors;i++) {
            textThreshold[i].setText(""+monitor[i].getThreshold());
            textMaximum[i].setText(""+monitor[i].getMaximum());
            checkAlarm[i].setSelected( monitor[i].getAlarm() );
            plotBar[i].repaint();
        }
    }
    /**
     *  configure the monitors
     *  That is set the values their parameters according to the input fields
     *
     * @exception   DataException     <code>DataException</code> tried to add gate to wrong type of histogram
     */
    void configure() throws DataException {
        double threshold, maximum;

        try {
            //set update interval
            interval=Integer.parseInt(textUpdate.getText().trim());
            if (interval<1){
                throw new DataException("Update interval must be greater than 1");
            }
            Monitor.setInterval(interval);

            //set Monitor parameters
            for (int i=0;i<numberMonitors;i++) {
                threshold=Double.parseDouble(textThreshold[i].getText().trim());
                monitor[i].setThreshold(threshold);

                maximum=Double.parseDouble(textMaximum[i].getText().trim());
                monitor[i].setMaximum(maximum);

                monitor[i].setAlarm(checkAlarm[i].isSelected());
                plotBar[i].repaint();
            }
            //set audio alarm
            audioAlarm=checkAudio.isSelected();
        } catch (NumberFormatException nfe){
            throw new DataException("Invalid number input [MonitorControl]");
        }
        configured=true;
    }

    /**
     * Display monitors
     */
    private void display() {
        double value;

        for(int i=0; i<numberMonitors; i++){
            value=monitor[i].getValue();
            textValue[i].setText(""+value);
            plotBar[i].repaint();
        }

    }

    /**
     * Start monitors interval updating loop
     */
    private void start() throws DataException {

        if(configured){
            if (loopThread==null){
                loopThread=new GoodThread(this);
                loopThread.setPriority(2);      //lower priority than display and sort
                loopThread.setDaemon(true);
                loopThread.start();

            }
            ddisp.setTitle("Monitors Enabled");

        } else {
            throw new DataException("Monitors not configured ");

        }

    }

    /**
     * Stop monitors interval updating loop
     */
    private void stop(){
        if(loopThread!=null){
            //XXXstop() is deprecated;
            //loopThread.stop();
            loopThread=null;
        }
        //clear numbers and graphs
        for (int i=0;i<numberMonitors; i++){
            textValue[i].setText("");
            monitor[i].reset();
            plotBar[i].repaint();
        }
        ddisp.setTitle("Monitors Disabled");
    }

    /** FIXME
     * Set the Monitor values
     * This routine can be called back by
     *  VME communication
     *
     */
    public void setMonitors(int []scalValue){

        for (int i=0;i<scalValue.length;i++ ) {
            textValue[i].setText(String.valueOf(scalValue[i]));
        }
    }
    /**
     * method to run and update monitors
     *
     */
    public void run (){
        try {
            //infinite loop
            while(loopThread!=null){//attempted fix for stop() deprecation

                //read scalers and wait
                broadcaster.broadcast(BroadcastEvent.SCALERS_READ);
                Thread.sleep(500);
                //loop for each monitor
                for(int i=0; i<numberMonitors; i++){
                    //update the monitor
                    monitor[i].update();
                    //If the audio on and are we taking data
                    if(audioAlarm&&JamStatus.isAcqOn()){
                        //is the alarm for this monitor set set
                        if (monitor[i].getAlarm()){
                            //is the value out of bounds
                            if (monitor[i].getValue()<monitor[i].getThreshold() ||
                                monitor[i].getValue()>monitor[i].getMaximum()){
                                //FIXME  audioClip=get
                                //if (audioClip==null)
                                Toolkit.getDefaultToolkit().beep();
                            }
                        }
                    }
                }
                //display monitors
                display();
                //end loop monitors
                Thread.sleep(interval*1000-500);
            }
            //infinite loop
        } catch(InterruptedException ie){
            msgHandler.errorOutln("Monitor Interupted ");
        } catch (GlobalException ge) {
            msgHandler.errorOutln(getClass().getName()+"run(): "+ge);
        }
    }
}

