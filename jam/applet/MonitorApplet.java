/*
*/
package jam.applet;
import java.applet.Applet;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.rmi.*;
import jam.global.*;
import jam.util.*;
import jam.data.control.*;
import jam.data.*;
import jam.*;
/**
 * An applet to allow remote viewing of Jam Histograms
 *
 * @author Ken Swartz
 * @version 0.5
 *
 */
public class MonitorApplet extends Applet implements ActionListener, ItemListener, Runnable {

    private final boolean DEBUG=false;

    private Panel pMonitors;
    private RemoteData remoteData;

    private TextField textHost;

    private Panel pm[];
    private Label labelDisp[];
    private TextField textValue[];
    private PlotBar plotBar[];
    private Panel pal;
    private Checkbox checkAudio;

    private TextField textError;

    private URL localPath;
    private String documentHost;
    private String expname;
    private String hostName;

    private Vector monitorList;
    private Monitor monitor[];
    private int numberMonitors;
    private double monitorValues[];
    private int interval;
    private boolean audioOn=false;

    private int sizeX;
    private int sizeY;

    private Thread loopThread;
    //first time applet is loaded
    private boolean first=true;


    /**
     * Initializes the applet.  You never need to call this directly; it is
     * called automatically by the system once the applet is created.
     */
    public void init() {

  System.out.println("MonitorApplet init");
  //setup applet size
  try {

      sizeY=Integer.parseInt(this.getParameter("height"));
      sizeX=Integer.parseInt(this.getParameter("width"));
      expname=this.getParameter("expname");

  } catch (NumberFormatException nfe){
      System.err.println("height and width not numbers");
  }

  //applet layout
  this.setLayout(new BorderLayout(0,0));
  resize(sizeX,sizeY);
  
  setBackground(Color.lightGray);
        setForeground(Color.black);

  Panel ptop=new Panel();
      ptop.setLayout(new GridLayout(0,1,3,5));
      this.add(BorderLayout.NORTH, ptop);  
      
      Panel pHost=new Panel();      
      pHost.setLayout(new FlowLayout(FlowLayout.LEFT,3,5));  
      ptop.add(pHost);
          
      Label llink= new Label("Link to URL: ",Label.RIGHT);
      pHost.add(llink);

      textHost=new TextField("rmi:// ");
      textHost.setColumns(30);  
      textHost.setBackground(Color.white);  
      textHost.setEditable(false);        
      textHost.addActionListener(this);  
      pHost.add(textHost);

      
      // alarm panel for display dialog        
      pal= new Panel();
    pal.setLayout(new FlowLayout(FlowLayout.CENTER,10,5));
    
    checkAudio =new Checkbox("Audio Alarm", true );
    checkAudio.addItemListener(this);  
    pal.add(checkAudio);
        this.add(pal);

  textError=new TextField();
  textError.setEditable(false);
  this.add(BorderLayout.SOUTH, textError);

  //display in middle
  pMonitors=new Panel();
  pMonitors.setLayout(new GridLayout(0,2,5,5));
  this.add(pMonitors);

  //where did we come from, set host url
  //setup applet document path
  localPath=this.getDocumentBase();
  documentHost=this.getDocumentBase().getHost();
  if(documentHost==null){
      documentHost="hostname";
  }
  if(expname==null) {
      expname="expname";
  }
  hostName="rmi://"+documentHost+"/"+expname;
  textHost.setText(hostName);


    }

    /**
     * Called to start the applet.  You never need to call this directly; it
     * is called when the applet's document is visited.
     */
    public void start() {
      System.out.println("start");
      if(DEBUG){
        try{
          createExample();
        } catch (DataException de) {
          System.out.println("DataException: "+de);
        }
      } else {
        link(hostName);
      }
      setupDisplay();

      if (loopThread==null){
        loopThread=new Thread(this);
        loopThread.setPriority(2);      //lower priority than display and sort
        loopThread.setDaemon(true);
        loopThread.start();
      }
    }

    /**
     * Called to stop the applet.  This is called when the applet's document is
     * no longer on the screen.  It is guaranteed to be called before destroy()
     * is called.  You never need to call this method directly
     */
    public void stop() {
    }

    /**
     * Cleans up whatever resources are being held.  If the applet is active
     * it is stopped.
     */
    public void destroy() {
    }
    /**
     * Receive action frow awt widgets
     */
    public void actionPerformed(ActionEvent e){
/*
        String incommand;
  String hostName;
  String file;
  URL histogramURL;

  incommand=e.getActionCommand();


  if ((e.getSource()==textHost)) {
      incommand="link";
  }

  try {

      if (incommand=="link"){
    hostName=textHost.getText().trim();
    textError.setText("Trying "+hostName);
    link(hostName);

      }

  } catch (JamException je) {
      textError.setText(je.getMessage());
  } catch (SecurityException se){
      textError.setText("Security Exception: "+se.getMessage());
  }
  */
    }
    /**
     * Recieves the inputs from the pull down menus
     * that are choice changes
     *
     * @param  e    Action event from pull down menus
     * @since Version 0.5
     */
    public void itemStateChanged(ItemEvent ie){
  if(ie.getSource()==checkAudio) {
      audioOn=checkAudio.getState();
  }

    }
    /**
     * Setup the display dialog box.
     *
     */
    private void setupDisplay(){

  pMonitors.removeAll();

  //widgets for dislay page
  pm=new Panel[numberMonitors];
  labelDisp=new Label[numberMonitors];
  textValue =new TextField [numberMonitors];
  plotBar=new PlotBar[numberMonitors];

  if (numberMonitors!=0) {
      for (int i=0;i<numberMonitors;i++) {
    pm[i]= new Panel();
        pm[i].setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        pMonitors.add(pm[i]);

    labelDisp[i]=new Label  ("          ",Label.RIGHT);
    labelDisp[i].setText(monitor[i].getName());
        pm[i].add(labelDisp[i]);

      textValue[i] =new TextField("           ");
    textValue[i].setColumns(6);
    textValue[i].setEditable(false);
    textValue[i].setBackground(Color.white);
    textValue[i].setText( String.valueOf(0) );
    pm[i].add(textValue[i]);

      plotBar[i]=new PlotBar(monitor[i]);
    pMonitors.add(plotBar[i]);

      }
  }

  pMonitors.add(pal);

    }
    /**
     * link to host with rmi
     */
    private void link(String stringURL) {
  System.out.println("open a link to "+stringURL);

  Vector monitorList;

      try{

      remoteData=(RemoteData)Naming.lookup(stringURL);

  } catch (RemoteException re){
      textError.setText("Error: Remote lookup up failed URL: "+stringURL+ "Excpetion:"+re.getMessage());

  } catch (java.net.MalformedURLException mue){
      textError.setText("Error: Remote look up malformed URL: "+stringURL);

  } catch (NotBoundException nbe){
      textError.setText("Error: Remote look up could not find name "+stringURL);

  }

  try {
      System.out.println("get monitors");

      //load monitor list
      monitorList=remoteData.getMonitorList();
      numberMonitors=monitorList.size();
      monitor=new Monitor[numberMonitors];
      for (int i=0;i<numberMonitors;i++){
    monitor[i]=(Monitor)monitorList.elementAt(i);
      }
      interval=10;
      //FIXME
      //FIXMEinterval=((Monitor)monitorList.elementAt(0)).getInterval();
      System.out.println("interval "+interval);


  } catch (RemoteException re){
      System.out.println(re.getMessage());
      textError.setText("Error: Getting monitors "+hostName);
  }
  textError.setText("link made ");
    }
    /**
     * creat a set of example monitors for debugging
     */
    private void createExample() throws DataException {
  numberMonitors=3;
  monitor=new Monitor[numberMonitors];
  monitorValues=new double [numberMonitors];
  Scaler scal=new Scaler("ex",0);
  monitor[0]=new Monitor("test",scal);
  monitor[0].setThreshold(10);
  monitor[1]=new Monitor("help",scal);
  monitor[2]=new Monitor("damn",scal);
  interval=5;
    }
    /**
     *
     */
    public void run(){
  int count=0;
  try {
      //infinite loop
      while(count<10){
    count++;
    System.out.println("loop");

    //update the monitor
    if(!DEBUG){
        monitorValues=remoteData.getMonitorValues();

    }                
                                  
    //is the alarm for this monitor set set
    for(int i=0;i<numberMonitors;i++){
        System.out.println("mon value "+i+" "+monitorValues[i]);    
        if ( ((monitor[i].getAlarm())&&
      (monitorValues[i])<monitor[i].getThreshold())){
      //FIXME  audioClip=get
          if (audioOn) {
          System.out.println("beep for "+i);
          Toolkit.getDefaultToolkit().beep();      
      }            
        }
    }          
    //end loop monitors                        
    Thread.sleep(interval*1000);    
      }
      //infinite loop
  } catch (RemoteException re){
      textError.setText("Error: Remote Exception "+re.getMessage());        
  } catch(InterruptedException ie){
      textError.setText("Monitor Interupted ");
  }      
    }
}      

