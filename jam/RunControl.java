package jam;
import jam.data.control.HistogramControl;
import jam.data.control.ScalerControl;
import jam.global.GlobalException;
import jam.global.GoodThread;
import jam.global.RunInfo;
import jam.io.DataIO;
import jam.sort.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Date;

import javax.swing.*;

/**
 * Class for data acquistion and run control.
 * This class
 * <ul>
 * <li>starts and stops acquisition,
 * <li>begins and ends runs
 * </ul>
 * <p><b>begin run</b>:</p>
 * <ul><li>starts acquisition</li>
 * <li>opens event file</li></ul>
 * <p><b>end run</b>:</p>
 * <ul><li>stops acquisition</li>
 * <li>closes event file</li>
 * <li>writes out summary data file</li></ul>
 *
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public class RunControl implements Controller, ActionListener {

    /** Indicates running to or from disk.
     */
    public static final int DISK=0;
    /** Indicates running to or from tape.
     */
    public static final int TAPE=1;
    /**
     * Indicates events being stored by front end.
     */
    public static final int FRONT_END=2;
    
    final static String EVENT_FILE_EXTENSION=".evn";
    /**
     *device we are writing to DISK or TAPE
     */
    private int device;

    // handles to class we need
    private JamMain		jamMain;
    private HistogramControl	histogramControl;
    private ScalerControl	scalerControl;
    private DataIO		dataio;
    private VMECommunication	vmeComm;
    private JamConsole		console;

    // daemon threads
    private NetDaemon		netDaemon;
    private StorageDaemon	storageDaemon;
    private DiskDaemon		diskDaemon;
    private TapeDaemon		tapeDaemon;
    private SortDaemon		sortDaemon;

    /**
     * event file information
     */
    private String experimentName;
    private String dataPath;
    //private String dataFileName;
    private File dataFile;
    /**
     *histogram file information
     */
    private String histFilePath;
    private String histFileName;
    private File histFile;


    /**
     * run Number, is append to experiment name to create event file
     */
    private int runNumber;
    /**
     * run Title
     */
    private String runTitle;
    /**
     * Are we currently in a run, saving event data
     */
    private boolean runOn=false;
    /**
     *Run dialog box
     */
    private JDialog d;
    private JButton bbegin;
    private JButton bend;
    private JTextField textRunNumber, textRunTitle, textExptName;
    private JCheckBox checkHistogramZero;
    private JCheckBox zeroScalers;

    /** Creates the run control dialog box.
     * @param jamMain launching point of Jam application
     * @param histogramControl
     * @param scalerControl dialog for reading and zeroing scalers
     * @param vmeComm object which sends and receives messages to/from the VME computer
     * @param dataio object in control of reading/writing data to/from disk
     * @param console
     */
    public RunControl(JamMain jamMain, HistogramControl histogramControl, ScalerControl scalerControl,
    VMECommunication vmeComm, DataIO dataio, JamConsole console){
        this.jamMain=jamMain;
        this.histogramControl=histogramControl;
        this.scalerControl=scalerControl;
        this.vmeComm=vmeComm;
        this.dataio=dataio;
        this.console=console;
        runNumber=100;
        // dialog box
        d=new JDialog(jamMain," Run ",false);
        d.setResizable(false);
        d.setLocation(20,50);
        d.setSize(400, 250);
        Container cp=d.getContentPane();
        cp.setLayout(new GridLayout(0,1,5,5));
        // panel for sort file
        /*JPanel pm1= new JPanel();
        pm1.setForeground(Color.black);
        pm1.setBackground(Color.lightGray);
        pm1.setLayout(new FlowLayout(FlowLayout.RIGHT,20,30));	*/
        JPanel pn= new JPanel();
        pn.setLayout(new FlowLayout(FlowLayout.LEFT,0,5));
        JLabel ln=new JLabel("",Label.RIGHT);
        ln.setText(" Run ");
        textRunNumber =new JTextField("");
        textRunNumber.setBackground(Color.white);
        textRunNumber.setColumns(3);
        textRunNumber.setText(Integer.toString(runNumber));
        JLabel len=new JLabel(" Experiment Name ");
        textExptName= new JTextField("            ");
        textExptName.setColumns(20);
        textExptName.setEditable(false);
        pn.add(len);
        pn.add(textExptName);
        pn.add(ln);
        pn.add(textRunNumber);
        // panel histogram path
        JPanel pt= new JPanel();
        pt.setLayout(new FlowLayout(FlowLayout.LEFT,0,5));
        JLabel lt=new JLabel("",Label.RIGHT);
        lt.setText("Title ");
        pt.add(lt);
        textRunTitle =new JTextField("");
        textRunTitle.setColumns(40);
        textRunTitle.setBackground(Color.white);
        pt.add(textRunTitle);
        JPanel pz= new JPanel();
        pz.setLayout(new FlowLayout(FlowLayout.RIGHT,10,5));
        checkHistogramZero =new JCheckBox("Zero Histograms", true );
        pz.add(checkHistogramZero);
        zeroScalers =new JCheckBox("Zero Scalers", true );
        pz.add(zeroScalers);
        JPanel pb= new JPanel();// panel for begin button
        pb.setLayout(new FlowLayout(FlowLayout.RIGHT,30,10));
        bbegin	=   new JButton("  Begin  ");
        bbegin.setBackground(Color.green);
        pb.add(bbegin);
        bbegin.setActionCommand("begin");
        bbegin.addActionListener(this);
        bbegin.setEnabled(false);
        JPanel pe= new JPanel();//panel for end button
        pe.setLayout(new FlowLayout(FlowLayout.RIGHT,30,10));
        bend = new JButton("   End   ");
        bend.setBackground(Color.red);
        pe.add(bend);
        bend.setActionCommand("end");
        bend.addActionListener(this);
        bend.setEnabled(false);
        //add all panels to dialog
        cp.add(pn);
        cp.add(pt);
        cp.add(pz);
        cp.add(pb);
        cp.add(pe);
        //pack in
        d.pack();
        d.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                d.dispose();
            }
        });
    }

    /**
     * Show RunControl dialog box
     */
    public void show(){
        d.show();
    }

    /**
     * Handles buttons in RunControl dialog box.
     */
    public void actionPerformed(ActionEvent ae){
        String command=ae.getActionCommand();
        try {
            if (command=="begin") {
                runTitle=textRunTitle.getText().trim();
                boolean confirm = (JOptionPane.showConfirmDialog(d,"Is this title OK? :\n"+runTitle,
                "Run Title Confirmation",
                JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION);
                if (confirm) {
                    beginRun();
                }
            } else if (command == "end") {
                endRun();
            }
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        } catch (SortException se) {
            console.errorOutln(se.getMessage());
        } catch (GlobalException ge) {
            console.errorOutln(getClass().getName()+".actionPerformed(\""+command+"\"): "+
            "GlobalException with message \""+ge.getMessage()+"\"");
        }
    }

    /**
     * Setup up called by SetupSort
     *
     */
    public void setupOn(String experimentName, String dataPath, String histFilePath,
    SortDaemon sortDaemon, NetDaemon netDaemon, StorageDaemon storageDaemon) {
        this.experimentName=experimentName;
        this.dataPath=dataPath;
        this.histFilePath=histFilePath;
        this.sortDaemon=sortDaemon;
        this.netDaemon=netDaemon;
        this.storageDaemon=storageDaemon;
        textExptName.setText(experimentName);
        if (storageDaemon instanceof DiskDaemon) {
            diskDaemon=(DiskDaemon)storageDaemon;
            device=DISK;
        } else if (storageDaemon instanceof TapeDaemon) {
            tapeDaemon=(TapeDaemon)storageDaemon;
            device=TAPE;
        } else if (storageDaemon == null) {//case if front end is taking care of storing events
            device=FRONT_END;
        } else {
            System.err.println("Error unknown storageDaemon type [RunControl]");
        }
        bbegin.setEnabled(true);
    }

    /**
     * Starts acquisition of data.
     * Figure out if online or offline an run appropriate method.
     *
     * @exception   JamException    all exceptions given to <code>JamException</code> go to the console
     */
    public void startAcq() throws JamException, GlobalException {
        netDaemon.setState(GoodThread.RUN);
        vmeComm.VMEstart();
        // if we are in a run, display run number
        if (runOn) {//runOn is true if the current state is a run
            jamMain.setRunState(RunState.RUN_ON(runNumber));
        	//see stopAcq() for reason for this next line.
        	bend.setEnabled(true);
        	console.messageOutln("Started Acquisition, continuing Run #"+runNumber);
        } else {//just viewing events, not running to disk
            jamMain.setRunState(RunState.ACQ_ON);
            this.bbegin.setEnabled(false);//don't want to try to begin run while going
        	console.messageOutln("Started Acquisition...to begin a run, first stop acquisition.");
        }
    }

    /**
     * Tells VME to stop acquisition, and suspends the net listener.
     */
    public void stopAcq() throws JamException, GlobalException {
        vmeComm.VMEstop();
        /*Commented out next line to see if this stops our problem of "leftover"
         *buffers DWV 15 Nov 2001 */
        //netDaemon.setState(GoodThread.SUSPEND);
        jamMain.setRunState(RunState.ACQ_OFF);
        //done to avoid "last buffer in this run becomes first and last buffer in 
        //next run" problem
        bend.setEnabled(false);
        if (!runOn) {//not running to disk
        	bbegin.setEnabled(true);//since it was disabled during start
        }
        console.warningOutln("Stopped Acquisition...if you are doing a run, "+
        "you will need to start again before clicking \"End Run\".");
    }

    /**
     * flush the vme buffer
     */
    public void flushAcq(){
        vmeComm.flush();
    }

    /**
     *	Begin taking data taking run.
     * <OL>
     *	<LI>Get run number and title</LI>
     *	<LI>Open file</LI>
     *	<LI>Tell disk Daemon the file</LI>
     *	<LI>Tell disk Daemon to write header</LI>
     *	<LI>Start disk Daemon</LI>
     *	<LI>Tell net daemon to send events into pipe</LI>
     *	<LI>Tell vme to start</LI>
     * </OL>
     *
     * @exception   JamException    all exceptions given to <code>JamException</code> go to the console
     */
    private void beginRun() throws JamException, SortException, GlobalException  {
        String dataFileName="";
        try {//get run number and title
            runNumber=Integer.parseInt(textRunNumber.getText().trim());
            runTitle=textRunTitle.getText().trim();
            RunInfo.runNumber=runNumber;
            RunInfo.runTitle=runTitle;
            RunInfo.runStartTime=getTime();
        } catch (NumberFormatException nfe){
            throw new JamException("Run number not an integer [RunControl]");
        }
        if (device==DISK){//saving to disk
            dataFileName=dataPath+experimentName+runNumber+EVENT_FILE_EXTENSION;
            dataFile=new File(dataFileName);
            if(dataFile.exists()){// Do not allow file overwrite
                throw new JamException("Event file already exits, File: "+dataFileName+", Jam Cannot overwrite. [RunControl]");
            }
            diskDaemon.openEventOutputFile(dataFile);
            diskDaemon.writeHeader();
        } else if (device==TAPE) {//saving to tape
            //FIXME -- tape option doesn't work right now 15-Sep-2002
            //tapeDaemon.openEventOutputFile(dataFile);
            tapeDaemon.writeHeader();
        } 
        if (checkHistogramZero.isSelected()) {// should we zero histograms
            histogramControl.zeroAll();
        }
        if (zeroScalers.isSelected()) {//should we zero scalers
            vmeComm.clearScalers();
        }
        if (device != FRONT_END) netDaemon.setWriter(true);//tell net daemon to write events to storage daemon
        // enable end button, display run number
        bend.setEnabled(true);
        bbegin.setEnabled(false);
        jamMain.setRunState(RunState.RUN_ON(runNumber));
        if(device==DISK){
            console.messageOutln("Began run "+runNumber+", events being written to file: "+dataFileName);
        } else if (device==TAPE) {
            console.messageOutln("Began run, events written to Tape ");
        } else {
            console.messageOutln("Began run, events being written out be front end.");
        }
        runOn=true;
        netDaemon.setState(GoodThread.RUN);
        vmeComm.VMEstart();//VME start last because other thread have higher priority
    }

    /**
     * End a data taking run
     * tell VME to end, which flushes buffer with a end of run marker
     * When the storageDaemon gets end of run character,
     * it will turn the netDaemon's eventWriter off
     * which flushs and close event file.
     *
     * sort calls back isEndRun when it sees the end of run marker
     * and write out histogram, gates and scalers if requested
     */
    private void endRun() throws GlobalException{
        RunInfo.runEndTime=getTime();
        vmeComm.end();			    //stop Acq. flush buffer
        vmeComm.readScalers();		    //read scalers
        bend.setEnabled(false);	    		    //toggle button states
        jamMain.setRunState(RunState.RUN_OFF);
        console.messageOutln("Ending run "+runNumber+", waiting for sorting to finish.");
        do {//wait for sort to catch up
            try {
                Thread.sleep(1000);		//sleep 1 second
            } catch(InterruptedException ie){
                System.err.println(getClass().getName()+".endRun(), Error: Interrupted while"
                +" waiting for sort to finish.");
            }
        } while(!sortDaemon.caughtUp());
        netDaemon.setState(GoodThread.SUSPEND);
        // histogram file name constructed using run name and number
        histFileName=histFilePath+experimentName+runNumber+".hdf";
        // only write a histogram file
        console.messageOutln("Sorting finished writing out histogram file: "+histFileName);
        histFile=new File(histFileName);
        dataio.writeFile(histFile);
        runNumber++;//increment run number
        textRunNumber.setText(Integer.toString(runNumber));
        runOn=false;
        bbegin.setEnabled(true);//set begin button state for next run
    }

    /**
     * Called by sorter when it starts up.
     * Nnot used for online data taking.
     */
    public void atSortStart() {
        /* does nothing for on line */
    }

    /**
     * Called back by <code>SortDaemon</code>.
     * When sort encounters a end-run-marker, writes out histograms, gates,
     * and scalers if so requested. This method is <i>deprecated</i>.
     * 
     * @deprecated
     */
    public void atSortEnd()  {
    	//found with empty if so commenting out code altogether
    	//dwvisser 12 August 2002
        /*if(runOn){
        }*/
    }
    
    /**
     * Called by sorter for each new file
     * not used for online data taking
     */
    public boolean isSortNext() {
        /* does nothing for online */
        return true;
    }

    /**
     * Method called back from sort package when we are done
     * writing out the event data and have closed the event file.
     */
    public void atWriteEnd()  {
        try {
            //
            if (device != FRONT_END) netDaemon.setWriter(false);
            //we are writting to tape
            if (device==DISK){
                diskDaemon.closeEventOutputFile();
                console.messageOutln("Event file closed "+dataFile.getPath());
            } else if(device==TAPE) {
                tapeDaemon.closeEventOutputFile();
                console.messageOutln(" Tape record ended");
            } else  if (device ==FRONT_END) {
            	System.out.println(getClass().getName()+".atWriteEnd()"+
            	" device=FRONT_END not implemented");
                // **** send message to indicate end of run file? ****
            } else {
                System.err.println("Error Should not be [RunControl]");
            }
        } catch (SortException je){
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * get current date and time
     */
    private Date getTime(){
        return new java.util.Date();
    }
}