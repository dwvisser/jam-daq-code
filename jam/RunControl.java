package jam;
import jam.data.control.HistogramControl;
import jam.data.control.ScalerControl;
import jam.global.GoodThread;
import jam.global.RunInfo;
import jam.io.DataIO;
import jam.sort.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.*;
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
    private static final int DISK=0;

    /**
     * Indicates events being stored by front end.
     */
    private static final int FRONT_END=2;

    final static String EVENT_FILE_EXTENSION=".evn";
    
    /**
     * The device writing events: DISK or FRONT_END
     */
    private int device;

    // handles to class we need
    private final JamMain		jamMain;
    private final HistogramControl	histogramControl;
    private final ScalerControl	scalerControl;
    private final DataIO		dataio;
    private final VMECommunication	vmeComm;
    private final JamConsole		console;

    // daemon threads
    private NetDaemon		netDaemon;
    //private StorageDaemon	storageDaemon;
    private DiskDaemon		diskDaemon;
    private SortDaemon		sortDaemon;

    /**
     * event file information
     */
    private String experimentName;
    private String dataPath;
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
    private final JDialog d;
    private final JButton bbegin;
    private final JButton bend;
    private final JTextField textRunNumber, textRunTitle, textExptName;
    private final JCheckBox checkHistogramZero;
    private final JCheckBox zeroScalers;

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
        cp.setLayout(new BorderLayout(10,0));


		//Labels Panel
        JPanel pLabels= new JPanel(new GridLayout(0,1,5,5));
        pLabels.setBorder(new EmptyBorder(10,10,10,0));
        cp.add(pLabels, BorderLayout.WEST);
        JLabel len=new JLabel("Experiment Name",JLabel.RIGHT);
		pLabels.add(len);
        JLabel lrn=new JLabel("Run",JLabel.RIGHT);
		pLabels.add(lrn);
        JLabel lt=new JLabel("Title",JLabel.RIGHT);
		pLabels.add(lt);
        JLabel lc=new JLabel("Zero on Begin?",JLabel.RIGHT);
		pLabels.add(lc);

        // panel for text fields
        JPanel pCenter= new JPanel(new GridLayout(0,1,5,5));
        //Box pCenter= new Box(BoxLayout.Y_AXIS);
        pCenter.setBorder(new EmptyBorder(10,0,10,10));
        cp.add(pCenter, BorderLayout.CENTER);

		final JPanel pExptName = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		pCenter.add(pExptName);
        textExptName= new JTextField("");
        textExptName.setColumns(20);
        textExptName.setEditable(false);
        pExptName.add(textExptName);

		final JPanel pRunNumber = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		pCenter.add(pRunNumber);
        textRunNumber =new JTextField("");
        textRunNumber.setColumns(3);
        textRunNumber.setText(Integer.toString(runNumber));
        pRunNumber.add(textRunNumber);

		final JPanel pRunTitle = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		pCenter.add(pRunTitle);
        textRunTitle =new JTextField("");
        textRunTitle.setColumns(40);
        pRunTitle.add(textRunTitle);

		//Zero Panel
        JPanel pZero= new JPanel(new FlowLayout(FlowLayout.LEFT,0,-2));
        checkHistogramZero =new JCheckBox("Histograms", true );
        pZero.add(checkHistogramZero);
        zeroScalers =new JCheckBox("Scalers", true );
        pZero.add(zeroScalers);
        pCenter.add(pZero);

		//Panel for buttons
        JPanel pButtons =new JPanel(new FlowLayout(FlowLayout.CENTER));
        cp.add(pButtons, BorderLayout.SOUTH);
        JPanel pb= new JPanel(new GridLayout(1,0,50,5));
        pButtons.add(pb);
        bbegin	=   new JButton("Begin");
        bbegin.setBackground(Color.GREEN);
        bbegin.setActionCommand("begin");
        bbegin.addActionListener(this);
        bbegin.setEnabled(false);
        pb.add(bbegin);

        bend = new JButton("End");
        bend.setBackground(Color.RED);
        bend.setActionCommand("end");
        bend.addActionListener(this);
        bend.setEnabled(false);
        pb.add(bend);

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
        }
    }

    /**
     * Setup up called by SetupSort
     *
     */
    public void setupOn(String experimentName, String dataPath, String histFilePath,
    SortDaemon sortDaemon, NetDaemon netDaemon, DiskDaemon dd) {
        this.experimentName=experimentName;
        this.dataPath=dataPath;
        this.histFilePath=histFilePath;
        this.sortDaemon=sortDaemon;
        this.netDaemon=netDaemon;
        textExptName.setText(experimentName);
        if (dd == null) {//case if front end is taking care of storing events
            device=FRONT_END;
        } else {
			diskDaemon=dd;
			device=DISK;
        }
        bbegin.setEnabled(true);
    }

    /**
     * Starts acquisition of data.
     * Figure out if online or offline an run appropriate method.
     *
     * @exception   JamException    all exceptions given to <code>JamException</code> go to the console
     */
    public void startAcq() throws JamException {
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
    public void stopAcq() throws JamException {
        vmeComm.VMEstop();
        /*Commented out next line to see if this stops our problem of "leftover"
         *buffers DWV 15 Nov 2001 */
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
    private void beginRun() throws JamException, SortException  {
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
        }
        if (checkHistogramZero.isSelected()) {// should we zero histograms
            histogramControl.zeroAll();
        }
        if (zeroScalers.isSelected()) {//should we zero scalers
            vmeComm.clearScalers();
        }
        if (device != FRONT_END) {//tell net daemon to write events to storage daemon
        	netDaemon.setWriter(true);
        } 
        // enable end button, display run number
        bend.setEnabled(true);
        bbegin.setEnabled(false);
        jamMain.setRunState(RunState.RUN_ON(runNumber));
        if(device==DISK){
            console.messageOutln("Began run "+runNumber+", events being written to file: "+dataFileName);
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
    private void endRun() {
        RunInfo.runEndTime=getTime();
        vmeComm.end();			    //stop Acq. flush buffer
        vmeComm.readScalers();		    //read scalers
        bend.setEnabled(false);	    		    //toggle button states
        jamMain.setRunState(RunState.RUN_OFF);
        console.messageOutln("Ending run "+runNumber+", waiting for sorting to finish.");
        int numSeconds=0;
        do {//wait for sort to catch up
            try {
                Thread.sleep(1000);		//sleep 1 second
                numSeconds++;
                if (numSeconds % 3 ==0){
                	console.warningOutln("Waited "+numSeconds+" seconds for "+
                	"sorter and file writer to finish. Sending commands to "+
                	"front end again.");
                	vmeComm.end();
                	vmeComm.readScalers();
                }
            } catch(InterruptedException ie){
                console.errorOutln(getClass().getName()+".endRun(), Error: Interrupted while"
                +" waiting for sort to finish.");
            }
        } while(!sortDaemon.caughtUp() && !storageCaughtUp());
        diskDaemon.resetReachedRunEnd();
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
    
    private boolean storageCaughtUp(){
    	final boolean rval = device==FRONT_END ? true :
    	diskDaemon.caughtUpOnline();
    	return rval;
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
     * 
     * @throws IllegalStateException if the device is not an expected value
     */
    public void atWriteEnd()  {
		if (device != FRONT_END) {
			netDaemon.setWriter(false);
		} 
        try {
            if (device==DISK){
                diskDaemon.closeEventOutputFile();
                console.messageOutln("Event file closed "+dataFile.getPath());
            } else  if (device ==FRONT_END) {
            	console.errorOutln(getClass().getName()+".atWriteEnd()"+
            	" device=FRONT_END not implemented");
                // **** send message to indicate end of run file? ****
            } else {
                throw new IllegalStateException(
				"Expect device to be DISK or FRONT_END.");
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