package jam.io.hdf;

import jam.data.DataBase;
import jam.data.DataParameter;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.io.DataIO;
import jam.io.FileOpenMode;
import jam.util.SwingWorker;

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

/**
 * Reads and writes HDF files containing spectra, scalers, gates, and additional
 * useful information.
 * 
 * @version 0.5 November 98, January 2005
 * @author Dale Visser, Ken Swartz
 * @since JDK1.1
 */
public final class HDFIO implements DataIO, JamHDFFields {

    /**
     * Last file successfully read from or written to for all instances of
     * HDFIO.
     * 
     * see #readFile
     */
    private static File lastGoodFile;

    private static final Object LVF_MONITOR = new Object();

    private static final Preferences PREFS = Preferences
            .userNodeForPackage(HDFIO.class);

    private static final String LFILE_KEY = "LastValidFile";

    static private final List EMPTY_LIST = Collections
            .unmodifiableList(new ArrayList());

    static {
        lastGoodFile = new File(PREFS.get(LFILE_KEY, System
                .getProperty("user.dir")));
    }

    /**
     * Number of steps in progress, 
     * 1 for converting objects, 10 for writing them out
     */    
    private static final int MONITOR_STEPS_READ_WRITE=11;		//1 Count DD's, 10 read objects
    private static final int MONITOR_STEPS_OVERHEAD_WRITE=3;	//2 for start of read object
    private static final int MONITOR_STEPS_OVERHEAD_READ=1;		//
    /**
     * Parent frame.
     */
    private final Frame frame;
    
    AsyncProgressMonitor asyncMonitor;
    /**
     * Where messages get sent (presumably the console).
     */
    private final MessageHandler msgHandler; 

    /**
     * <code>HDFile<code> object to read from.
     */
    private HDFile inHDF;

    AsyncListener asyncListener;
	
    private final ConvertJamObjToHDFObj jamToHDF;

    private ConvertHDFObjToJamObj hdfToJam;
    
    private String uiMessage;
    private String uiErrorMsg;
    private int groupCount=0;
    private int histCount=0;
    private int gateCount=0;
    private int scalerCount=0;
    private int parameterCount=0;

    /**
     * Class constructor handed references to the main class and message
     * handler.
     * 
     * @param parent
     *            the parent window
     * @param console
     *            where to send output
     */
    public HDFIO(Frame parent, MessageHandler console) {
        frame = parent;
        msgHandler = console;
        asyncMonitor = new AsyncProgressMonitor(frame);
        jamToHDF = new ConvertJamObjToHDFObj();
        hdfToJam = new ConvertHDFObjToJamObj();
    }

    /**
     * Writes out to a specified file all the currently held spectra, gates,
     * scalers, and parameters.
     * 
     * @param file
     *            to write to
     */
    public void writeFile(File file) {
        writeFile(file, null, null, true, true); 
    }

    /**
     * Writes out to a specified file all the currently held spectra, gates,
     * scalers, and parameters.
     * 
     * @param file
     *            to write to
     */
    public void writeFile(File file, Group group) {
    	List groupList = new ArrayList();
		groupList.add(group);
        writeFile(file, groupList, null, true, true); 
    }

    /**
     * 
     * @param file
     * @param histograms
     * @param wrtdata
     * @param wrtsettings
     */
    public void writeFile(final File file, List histograms) {
    	
    	writeFile(file, null, histograms, true, true);    	
    }

    /**
     * 
     * @param file
     * @param histograms
     * @param wrtdata
     * @param wrtsettings
     */
    public void writeFile(final File file, boolean wrtdata, boolean wrtsettings) {
    	
    	writeFile(file, null, null, wrtdata, wrtsettings);    	
    }
    
    /**
     * Center call for all public writes
     */
    private void writeFile(final File file, List groups, List histograms, boolean wrtdata, boolean wrtsettings) {
    	
    	//Groups specified determines histograms 
    	if (groups!=null) {
	    	histograms =new ArrayList();
	    	Iterator iterGroup = groups.iterator();
	    	while (iterGroup.hasNext()){
	    		Group g =(Group)iterGroup.next();
	    		Iterator iterHist = g.getHistogramList().iterator();
	    		while(iterHist.hasNext()) {	    		
	    			histograms.add( (Histogram)(iterHist.next()) );
	    		}
	    	}
	   //Histograms specified determines groups	    	
    	} else if (histograms!=null) {
    		groups =new ArrayList();
	    	Iterator iterHist = histograms.iterator();
	    	while (iterHist.hasNext()){
	    		Histogram h= (Histogram)iterHist.next();
	    		if (!groups.contains(h.getGroup())){
	    			groups.add(h.getGroup());
	    		}
	    	}
    	//Neither groups nor histograms specified
    	} else {
    		groups =Group.getGroupList();
	    	histograms =new ArrayList();
	    	Iterator iterGroup = groups.iterator();
	    	while (iterGroup.hasNext()){
	    		Group g =(Group)iterGroup.next();
	    		Iterator iterHist = g.getHistogramList().iterator();
	    		while(iterHist.hasNext()) {
	    			histograms.add( (Histogram)iterHist.next() );
	    		}
	    	}    		
    	}
		
    	
        if (overWriteExistsConfirm(file)) {        	
            spawnAsyncWriteFile(file, groups, histograms, wrtdata, wrtsettings);
        }
    }
    
    /**
     * Writes out to a specified file a list of histograms and gates, scalers,
     * and parameters.
     * 
     * @param file
     *            the to write to
     * @param histograms
     *            list of histograms to write
     */
    //FIXME KBS remove when new write is tested
    public void writeFileOld(File file, List histograms) {
        /* Check to overwrite if file exists. */
        if (!overWriteExistsConfirm(file)) {
            return;
        }
        /* Histogram list */
        final List histList = new ArrayList();
        final Iterator iterHist = histograms.iterator();
        while (iterHist.hasNext()) {
            final Histogram hist = (Histogram) iterHist.next();
            if (hist.getArea() > 0) {
                histList.add(hist);
            }
        }
        /* Gate list */
        final List gateList = new ArrayList();
        final Iterator iter = Gate.getGateList().iterator();	
        while (iter.hasNext()) {
            final Gate gate = (Gate) (iter.next());
            final Histogram histGate = gate.getHistogram();
            /*
             * Gate defined and Does the histogram List contain this gate's
             * histogram
             */
            if (gate.isDefined() && histList.contains(histGate)) {
                gateList.add(Gate.getGateList());
            }
        }
        final List scalerList = Scaler.getScalerList();
        final List paramList = DataParameter.getParameterList();
        spawnAsyncWriteFile(file, histList, gateList, scalerList, paramList);
    }

    
    /**
     * Writes out (to a specific file) the currently held spectra, gates, and
     * scalers, subject to the options given. Sets separately which data
     * writeFile should actually output. Not writing histograms when you are
     * saving tape data can significantly save time when you have many 2-d
     * spectra.
     * 
     * @param wrthist
     *            if true, Histograms will be written
     * @param wrtgate
     *            if true, Gates will be written
     * @param wrtscalers
     *            if true, scaler values will be written
     * @param wrtparams
     *            if true, parameter values will be written
     * @param file
     *            to write to
     */
    //FIXME KBS old write remove when new writes tested
    public void writeFile(boolean wrthist, boolean wrtgate, boolean wrtscalers,
            boolean wrtparams, final File file) {
        if (overWriteExistsConfirm(file)) {
            /* Histogram list */
            final List tempHist = wrthist ? Histogram.getHistogramList()
                    : EMPTY_LIST;
            final Iterator iter = tempHist.iterator();
            final List histList = new ArrayList();
            while (iter.hasNext()) {
                final Histogram hist = (Histogram) iter.next();
                if (hist.getArea() > 0) {
                    histList.add(hist);
                }
            }
            /* Gate list */
            final List gateList = new ArrayList();
            if (wrtgate) {
                gateList.addAll(Gate.getGateList());
            }
            final Iterator gateIterator = gateList.iterator();
            while (gateIterator.hasNext()) {
                final Gate gate = (Gate) (gateIterator.next());
                if (!gate.isDefined()) {
                    gateIterator.remove();
                }
            }
            final List scaler = wrtscalers ? Scaler.getScalerList()
                    : EMPTY_LIST;
            final List parameter = wrtparams ? DataParameter.getParameterList()
                    : EMPTY_LIST;
            spawnAsyncWriteFile(file, histList, gateList, scaler, parameter);
        }
    }

    /**
     * Read in an HDF file.
     * 
     * @param infile
     *            file to load
     * @param mode
     *            whether to open or reload
     * @return <code>true</code> if successful
     */
    public boolean readFile(FileOpenMode mode, File infile) {
    	return readFile(mode, infile, null, null);

    }

    /**
     * Read in an HDF file.
     * 
     * @param infile
     *            file to load
     * @param mode
     *            whether to open or reload
     * @return <code>true</code> if successful
     */
    public boolean readFile(FileOpenMode mode, File infile, Group group) {
    	return readFile(mode, infile, group, null);
    }
    /**
     * Read in an HDF file.
     * 
     * @param infile
     *            file to load
     * @param mode
     *            whether to open or reload
     * @param histNames list of names of histograms to read in
     * @return <code>true</code> if successful
     */
    public boolean readFile(FileOpenMode mode, File infile, Group group, List histNames) {
    	if(!infile.isFile()) {
    		msgHandler.errorOutln("Cannot find file "+infile+".");
    		return false;
    	}
        if (!HDFile.isHDFFile(infile)) {
        	msgHandler.errorOutln("File "+ infile + " is not a valid HDF file.");
            return  false;
        }
    	
    	spawnAsyncReadFile(mode, infile, group, histNames);
        return true;
    }

    
    /*
     * non-javadoc: Asyncronized write
     */
    private void spawnAsyncWriteFile(final File file, final List groups, final List histograms, final boolean wrtdata, final boolean wrtsettings) {
    	uiMessage="";
    	uiErrorMsg ="";
    	final SwingWorker worker = new SwingWorker() {

            public Object construct() {
            	 asyncWriteFile(file, groups, histograms, wrtdata, wrtsettings);
                 System.gc();
            	return null;
            }

            //Runs on the event-dispatching thread.
            public void finished() {
            	if (!uiErrorMsg.equals("")) {
                    msgHandler.errorOutln(uiErrorMsg);
            	} else {
            		msgHandler.messageOutln(uiMessage);
            	}
            	
            }
        };
        worker.start();     	    	
    }

    /*
     * non-javadoc: Asyncronized write
     */
    private void spawnAsyncWriteFile(final File file, final List histograms,
            final List gates, final List scalers, final List parameters) {
    	uiMessage="";
    	uiErrorMsg ="";
    	final SwingWorker worker = new SwingWorker() {

            public Object construct() {
            	 asyncWriteFile(file, histograms, gates, scalers, parameters);
                 System.gc();
            	return null;
            }

            //Runs on the event-dispatching thread.
            public void finished() {
            	if (!uiErrorMsg.equals("")) {
                    msgHandler.errorOutln(uiErrorMsg);
            	} else {
            		msgHandler.messageOutln(uiMessage);
            	}
            	
            }
        };
        worker.start();     	    	
    }
    
    /*
     * non-javadoc: Asyncronized read
     */
    private void spawnAsyncReadFile(final FileOpenMode mode, final File infile, final Group group, final List histNames) {
    	uiMessage="";
    	uiErrorMsg ="";

    	final SwingWorker worker = new SwingWorker() {
            public Object construct() {
            	//FIXME KBS Test change thread priority to make monitor pop up sooner
            	Thread.yield();
            	Thread thisTread =Thread.currentThread();
            	thisTread.setPriority(thisTread.getPriority()-1);
            	//End test 
            	try {
            		asyncReadFileGroup(infile, mode, group, histNames);
            		//asyncReadFile(infile, mode, histNames);
            	}catch (Exception e) {
            		uiErrorMsg ="UError reading file "+infile.getName()+", "+e;
            		e.printStackTrace();
            		asyncMonitor.close();
            	}
            	return null;
            }
            /* Runs on the event-dispatching thread. */
            public void finished() {
            	if (uiErrorMsg.equals("")) {
            		msgHandler.messageOutln(uiMessage);
            	} else {
                    msgHandler.errorOutln(uiErrorMsg);
            	}
            	if (asyncListener!=null) {
            		asyncListener.CompletedIO(uiMessage, uiErrorMsg);
            	}
            	/*
            	//FIXME KBS delete when all async read hist lists cases are handled
            	 * 
            	JamStatus STATUS=JamStatus.getSingletonInstance();
            	Broadcaster BROADCASTER=Broadcaster.getSingletonInstance();
            	//Set overall status
            	STATUS.setSortMode(infile);  	            	
        		AbstractControl.setupAll();        		
        		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
        		
        		//Set first group as current group
        		Group.setCurrentGroup((Group)Group.getGroupList().get(0));
        		Histogram firstHist = (Histogram)Group.getCurrentGroup().getHistogramList().get(0);        		
        		STATUS.setCurrentHistogram(firstHist);
        		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, firstHist);
        		STATUS.getFrame().repaint();
        		*/
            }
        };
        worker.start();     	
    }

    
    /**
     * Given separate vectors of the writeable objects, constructs and writes
     * out an HDF file containing the contents. Null or empty
     * <code>Vector</code> arguments are skipped.
     * 
     * @param file
     *            disk file to write to
     * @param hists
     *            list of <code>Histogram</code> objects to write
     * @param gates
     *            list of <code>Gate</code> objects to write
     * @param scalers
     *            list of <code>Scaler</code> objects to write
     * @param parameters
     *            list of <code>Parameter</code> objects to write
     */
    synchronized private void asyncWriteFile(File file, List groups, List histograms, boolean wrtdata, boolean wrtsetting) {
    	
        final StringBuffer message = new StringBuffer();
        //reset all counters
        groupCount=0;
        histCount=0;
        gateCount=0;
        scalerCount=0;
        parameterCount=0;
        
        AbstractHData.clearAll();
        jamToHDF.addDefaultDataObjects(file.getPath());
        
        asyncMonitor.setup("Saving HDF file", "Converting Objects", 
        					MONITOR_STEPS_READ_WRITE+MONITOR_STEPS_OVERHEAD_WRITE);
        
        convertJamToHDF(groups, histograms, wrtdata, wrtsetting);
        
        message.append("Saved ").append(file.getName()).append(" (");
        message.append(groupCount).append(" groups");
        message.append(", ").append(histCount).append(" histograms");     
        message.append(", ").append(gateCount).append(" gates");
        message.append(", ").append(scalerCount).append(" scalers");
        message.append(", ").append(parameterCount).append(" parameters");
        message.append(")");
        
        asyncMonitor.increment();
        
        HDFile out=null;
        try {

            out = new HDFile(file, "rw", asyncMonitor, MONITOR_STEPS_READ_WRITE);
            asyncMonitor.setNote("Writing Data Objects");

            out.writeFile();
            asyncMonitor.setNote("Closing File");
        
        } catch (FileNotFoundException e) {
        	uiErrorMsg ="Opening file: " + file.getName();
        } catch (HDFException e) {
        	uiErrorMsg = "Exception writing to file '"
                + file.getName() + "': " + e.toString();       	
        } finally {
        	try {
        		out.close();
        	}catch (IOException e) {
            	uiErrorMsg = "Closing file " +file.getName();
            }
            asyncMonitor.close();            
        }

        AbstractHData.clearAll();
        out = null; //allows Garbage collector to free up memory
     
        setLastValidFile(file);
        uiMessage =message.toString();
        
    }
    
    /**
     * Given separate vectors of the writeable objects, constructs and writes
     * out an HDF file containing the contents. Null or empty
     * <code>Vector</code> arguments are skipped.
     * 
     * @param file
     *            disk file to write to
     * @param hists
     *            list of <code>Histogram</code> objects to write
     * @param gates
     *            list of <code>Gate</code> objects to write
     * @param scalers
     *            list of <code>Scaler</code> objects to write
     * @param parameters
     *            list of <code>Parameter</code> objects to write
     */
    synchronized private void asyncWriteFile(File file, List hists, List gates, List scalers,
            List parameters) {
    	
        final StringBuffer message = new StringBuffer();
        VirtualGroup allHists, allGates;

        AbstractHData.clearAll();
        jamToHDF.addDefaultDataObjects(file.getPath());
        
        asyncMonitor.setup("Saving HDF file", "Converting Objects", 
        		MONITOR_STEPS_READ_WRITE+MONITOR_STEPS_OVERHEAD_WRITE);
        message.append("Saved ").append(file.getName()).append(" (");
        if (hasContents(hists)) {
        	allHists=jamToHDF.addHistogramSection();
            message.append(hists.size()).append(" histograms");
            final Iterator iter = hists.iterator();
            while (iter.hasNext()) {
                final Histogram hist = (Histogram) iter.next();
                final VirtualGroup histVGroup = jamToHDF.addHistogramGroup(hist);
                jamToHDF.convertHistogram(histVGroup, hist);
                allHists.addDataObject(histVGroup);
            }
        }
        if (hasContents(gates)) {
        	allGates=jamToHDF.addGateSection();
            message.append(", ").append(gates.size()).append(" gates");
            final Iterator iter = gates.iterator();
            while (iter.hasNext()) {
                final Gate gate = (Gate) iter.next();
                final VirtualGroup gateVGroup = jamToHDF.convertGate(gate);
                allGates.addDataObject(gateVGroup);
                // add Histogram links...
                final VirtualGroup hist = VirtualGroup.ofName(AbstractHData
                        .ofType(AbstractHData.DFTAG_VG), gate.getHistogram().getName());
                if (hist != null) {
                    //reference the Gate in the Histogram group
                    hist.addDataObject(gateVGroup);
                } 
                
            }
        }
        if (hasContents(scalers)) {
        	 final VirtualGroup vgScaler = jamToHDF.addScalerSection();
        	 vgScaler.addDataObject(jamToHDF.convertScalers(scalers));            
            message.append(", ").append(scalers.size()).append(" scalers");            
        }
        if (hasContents(parameters)) {
        	final VirtualGroup vgParams = jamToHDF.addParameterSection();
            vgParams.addDataObject(jamToHDF.convertParameters(parameters));
            message.append(", ").append(parameters.size()).append(" parameters");
        }
        asyncMonitor.increment();
        message.append(")");

        HDFile out=null;
        try {
            synchronized (this) {
                out = new HDFile(file, "rw", asyncMonitor, MONITOR_STEPS_READ_WRITE);
                asyncMonitor.setNote("Writing Data Objects");
            }
            out.writeFile();
            asyncMonitor.setNote("Closing File");
        
        } catch (FileNotFoundException e) {
        	uiErrorMsg ="Opening file: " + file.getName();
        } catch (HDFException e) {
        	uiErrorMsg = "Exception writing to file '"
                + file.getName() + "': " + e.toString();       	
        } finally {
        	try {
        		out.close();
        	}catch (IOException e) {
            	uiErrorMsg = "Closing file " +file.getName();
            }
            asyncMonitor.close();            
        }
        synchronized (this) {
            AbstractHData.clearAll();
            out = null; //allows Garbage collector to free up memory
        }     
        setLastValidFile(file);
        uiMessage =message.toString();
        
    }
     
    /**
     * Read in an HDF file
     * 
     * @param infile
     *            file to load
     * @param mode
     *            whether to open or reload
     * @param histNames
     *            names of histograms to read, null if all
     * @return <code>true</code> if successful
     */
    synchronized private boolean asyncReadFileGroup(File infile, FileOpenMode mode, Group existingGroup, List histNames) {
        boolean rval = true;
        final StringBuffer message = new StringBuffer();
        //reset all counters
        groupCount=0;
        histCount=0;
        gateCount=0;
        scalerCount=0;
        parameterCount=0;
        
        asyncMonitor.setup("Reading HDF file", "Reading Objects", 
        		MONITOR_STEPS_READ_WRITE+MONITOR_STEPS_OVERHEAD_READ);
        try {

            AbstractHData.clearAll();
            
            //Read in objects
            inHDF = new HDFile(infile, "r", asyncMonitor, MONITOR_STEPS_READ_WRITE);
            inHDF.setLazyLoadData(true);
             /* read file into set of AbstractHData's, set their internal variables */
            inHDF.readFile();
            
            AbstractHData.interpretBytesAll();
            
            asyncMonitor.increment();                
            if (hdfToJam.hasVGroupRootGroup()) {
            	convertHDFToJam(mode, existingGroup, infile.getName());
            } else {
            	convertHDFToJamOriginal(mode, existingGroup, infile.getName(), null);
            }
            
            //Create output message
            if (mode == FileOpenMode.OPEN) {
                message.append("Opened ").append(infile.getName());
            } else if (mode == FileOpenMode.OPEN_ADDITIONAL) {
                message.append("Opened Additional ").append(infile.getName());
            } else if (mode == FileOpenMode.RELOAD) {
                message.append("Reloaded ").append(infile.getName());
            } else { //ADD
                message.append("Adding counts in ").append(infile.getName());
            }
            message.append(" (");
            message.append(groupCount).append(" groups");
            message.append(", ").append(histCount).append(" histograms");    
            message.append(", ").append(gateCount).append(" gates");
            message.append(", ").append(scalerCount).append(" scalers");
            message.append(", ").append(parameterCount).append(" scalers");
            message.append(')');

         } catch (FileNotFoundException e) {
           	uiErrorMsg ="Opening file: " + infile.getPath()+
			" Cannot find file or file is locked";               
            
        } catch (HDFException e) {
        	uiErrorMsg ="Reading file: '"
            + infile.getName() + "', Exception " + e.toString();            	
            rval = false;
        } finally {
        	try {
        		inHDF.close();
        	} catch (IOException except) {
        		uiErrorMsg ="Closing file "+infile.getName();
        		rval = false;
        	}    
            asyncMonitor.close();
             /* destroys reference to HDFile (and its AbstractHData's) */
             inHDF = null;
        }
        
        AbstractHData.clearAll();
        setLastValidFile(infile);
            
        uiMessage =message.toString();
        return rval;
    }


    /**
     * Read in an HDF file
     * 
     * @param infile
     *            file to load
     * @param mode
     *            whether to open or reload
     * @param histNames
     *            names of histograms to read, null if all
     * @return <code>true</code> if successful
     */
    //FIXME KBS can be remove after checking functionallity of new asynReadFile for old format
    synchronized private boolean asyncReadFile( File infile, FileOpenMode mode, List histNames) {
        boolean rval = true;
        Group currentGroup;
        
        //reset all counters
        groupCount=0;
        histCount=0;
        gateCount=0;
        scalerCount=0;
        parameterCount=0;

        final StringBuffer message = new StringBuffer();
        
        asyncMonitor.setup("Reading HDF file", "Reading Objects", 
        		MONITOR_STEPS_READ_WRITE+MONITOR_STEPS_OVERHEAD_READ);
        if (rval) {
            try {
                if (mode == FileOpenMode.OPEN) {
                    message.append("Opened ").append(infile.getName());
                    DataBase.getInstance().clearAllLists();
                } else if (mode == FileOpenMode.OPEN_ADDITIONAL) {
                    message.append("Opened Additional ").append(
                            infile.getName());
                } else if (mode == FileOpenMode.RELOAD) {
                    message.append("Reloaded ").append(infile.getName());
                } else { //ADD
                    message.append("Adding histogram counts in ").append(
                            infile.getName());
                }
                              
                AbstractHData.clearAll();
                
                //Read in objects
                inHDF = new HDFile(infile, "r", asyncMonitor, MONITOR_STEPS_READ_WRITE);
                inHDF.setLazyLoadData(true);
                inHDF.seek(0);
                 /* read file into set of AbstractHData's, set their internal variables */
                inHDF.readFile();
                
                AbstractHData.interpretBytesAll();
                
                asyncMonitor.increment();                
                
                //Set group
                if (mode == FileOpenMode.OPEN) {                   
                    Group.createGroup(infile.getName(), Group.Type.FILE);
                } else if (mode == FileOpenMode.OPEN_ADDITIONAL) {
                    Group.createGroup(infile.getName(), Group.Type.FILE);
                } else if (mode == FileOpenMode.RELOAD) {
                    final String sortName = JamStatus.getSingletonInstance()
                            .getSortName();
                    Group.setCurrentGroup(sortName);
                } // else mode == FileOpenMode.ADD, so use current group
                groupCount=1;
                
                hdfToJam.setInFile(inHDF);
                currentGroup=Group.getCurrentGroup();
                
                histCount=hdfToJam.convertHistogramsOriginal(currentGroup, mode, histNames);

                final VdataDescription vddScalers= hdfToJam.findScalersOriginal();                
                int numScalers =0;
                if (vddScalers!=null) {
                	numScalers = hdfToJam.convertScalers(currentGroup, vddScalers, mode);
                }

                if (mode != FileOpenMode.ADD) {

                	gateCount = hdfToJam.convertGates(mode);
                    int numParams=0;
                    /* clear if opening and there are histograms in file */

                    final VdataDescription vddParam= hdfToJam.findParametersOriginal();

                    if (vddParam!=null) {
                    	numParams = hdfToJam.convertParameters(currentGroup, vddParam, mode);
                    }
                }
                
                message.append(" (");
                message.append(histCount).append(" histograms");
                message.append(", ").append(gateCount).append(" gates");                
                message.append(", ").append(numScalers).append(" scalers");
                message.append(", ").append(parameterCount).append(" parameters");
                message.append(')');

            } catch (HDFException e) {
            	uiErrorMsg ="Exception reading file '"
                + infile.getName() + "': " + e.toString();            	
                rval = false;
            } catch (IOException e) {
            	uiErrorMsg ="Exception reading file '"
                    + infile.getName() + "': " + e.toString();            	
                rval = false;
            } finally {
            	try {
            		inHDF.close();
            	} catch (IOException except) {
            		uiErrorMsg ="Closing file "+infile.getName();
            		rval = false;
            	}    
                asyncMonitor.close();
                 /* destroys reference to HDFile (and its AbstractHData's) */
                 inHDF = null;
            }
            AbstractHData.clearAll();
            setLastValidFile(infile);
        }
        uiMessage =message.toString();
        return rval;
    }
    
    /**
     * Read the histograms in.
     * 
     * @param infile
     *            to read from
     * @return list of attributes
     */
    public List readHistogramAttributes(File infile) throws HDFException{
    	
        List rval = new ArrayList();
        
        if (!HDFile.isHDFFile(infile)) {
            rval = Collections.EMPTY_LIST;
        	throw new HDFException("File:+ "+ infile.getPath()+ "is not an HDF file.");
        }
        	            
        try {
        	
            AbstractHData.clearAll();
            
            /* Read in histogram names */
            inHDF = new HDFile(infile, "r");
            inHDF.setLazyLoadData(true);
            inHDF.readFile();
            
            AbstractHData.interpretBytesAll();
            
            if (hdfToJam.hasVGroupRootGroup()) {
            	rval.addAll(loadHistogramAttributesGroup());
            } else {
            	rval.addAll(loadHistogramAttributesOriginal());
            }
                        
        } catch (FileNotFoundException e) {
        	throw new HDFException("Opening file: " + infile.getPath()+
        			" Cannot find file or file is locked");            	                
        } catch (HDFException e) {
        	throw new HDFException("Reading file: '"
            + infile.getName() + "', Exception " + e.toString());            	                
        } finally {
        	try {
        		inHDF.close();

        	} catch (IOException except) {
        		//NOP Bury exception
        	}
    		inHDF=null;
    	}
        
        AbstractHData.clearAll();

        return rval;
    }
    
    private void convertJamToHDF(List groups, List histogramList, boolean wrtdata, boolean wrtsetting) {
    	
        VirtualGroup globalVirtualGroupGroups= jamToHDF.addGroupSection();
        VirtualGroup globalVirtualGroupHistogram= jamToHDF.addHistogramSection();
        VirtualGroup globalVirtualGroupGate = jamToHDF.addGateSection();
        VirtualGroup globalVirtualGroupScaler = jamToHDF.addScalerSection();
        VirtualGroup globalVirtualGroupParameter = jamToHDF.addParameterSection();
        
        /* Loop for all groups */
        final Iterator groupsIter = groups.iterator(); 
        while(groupsIter.hasNext()){
        	Group group = (Group)groupsIter.next();
        	VirtualGroup virtualGroupGroup = jamToHDF.convertGroup(group);
        	globalVirtualGroupGroups.addDataObject(virtualGroupGroup);
        	
        	//Histograms 
        	//virtualGroupGroup.addDataObject(virtualGroupHists);
            //Loop for all histograms
            final Iterator histsIter = group.getHistogramList().iterator();
            while (histsIter.hasNext()) {
                final Histogram hist = (Histogram) histsIter.next();
                //Histogram is in histogram list
                if (histogramList.contains(hist)) {
	                final VirtualGroup histVGroup =jamToHDF.addHistogramGroup(hist);
	               	if (wrtdata) {
	               		jamToHDF.convertHistogram(histVGroup, hist);
	            	virtualGroupGroup.addDataObject(histVGroup);
	            	//backward compatible
	            	globalVirtualGroupHistogram.addDataObject(histVGroup);
	 
	               		histCount++;
	               	}

                
	                //Loop for all gates
	                if (wrtsetting) {
		                final Iterator gatesIter = hist.getGates().iterator();
		                while (gatesIter.hasNext()) {
		                    final Gate gate = (Gate) gatesIter.next();
		                    if(gate.isDefined()){
		                    	final VirtualGroup gateVGroup =jamToHDF.convertGate(gate);
		                    	histVGroup.addDataObject(gateVGroup);
		                    	//backward compatiable
		                    	globalVirtualGroupGate.addDataObject(gateVGroup);	
		                    	gateCount++;
		                	}
		                } //end loop gates
	                }
                }                
            } //end loop histograms

            //Convert all scalers
            if (wrtdata) {
	            final List scalerList = group.getScalerList();
	            if (scalerList.size()>0) {
		            final VirtualGroup virtualGroupScalers = jamToHDF.addScalerSection();
		            virtualGroupGroup.addDataObject(virtualGroupScalers);                        
		            final VdataDescription scalerDataDescription =jamToHDF.convertScalers(scalerList);
		            virtualGroupScalers.addDataObject(scalerDataDescription);
		            if (group==Group.getSortGroup()) {    
		            	//backward compatiable
		            	globalVirtualGroupScaler.addDataObject(scalerDataDescription);	
		            }
	            }
	            scalerCount=scalerList.size();
            }

            //Convert all parameters
            if (wrtsetting) {
            	final List parameterList =DataParameter.getParameterList();
            	if (parameterList.size()>0) {
		            final VirtualGroup virtualGroupParameters = jamToHDF.addParameterSection();
		            virtualGroupGroup.addDataObject(virtualGroupParameters); 
		            if (group==Group.getSortGroup()) {            	 
		            	final VdataDescription parameterDataDescription =jamToHDF.convertParameters(parameterList);
		            	virtualGroupParameters.addDataObject(parameterDataDescription);
		            	//Backward compatiable
		            	globalVirtualGroupParameter.addDataObject(parameterDataDescription); 
		                parameterCount=parameterList.size();;
		            }
            	}
            }

            groupCount++;
        }
    }
    
    /**
     * 
     * @param mode
     * @throws HDFException
     */
    private void convertHDFToJam(FileOpenMode mode, Group existingGroup, String fileName) throws HDFException {
        hdfToJam.setInFile(inHDF);
	    //Find groups	
	    List groupVirtualGroups = hdfToJam.findGroups(mode, null);
	    groupCount =groupVirtualGroups.size();
	    //Loop over groups
	    Iterator groupIter =groupVirtualGroups.iterator();
	    while (groupIter.hasNext()) {
	    	VirtualGroup currentVGroup = (VirtualGroup)groupIter.next();
	    	Group currentGroup =hdfToJam.convertGroup(currentVGroup);
	    	if (mode==FileOpenMode.OPEN_ADDITIONAL) {
	    		appendFileName(currentGroup, fileName);
	    	}
	        //Find histograms
	    	List histList =hdfToJam.findHistograms(currentVGroup, null);
	    	histCount = histList.size();
	        //Loop over histograms
	    	Iterator histIter =histList.iterator();
	    	 while (histIter.hasNext()) {
	    	 	VirtualGroup histVGroup = (VirtualGroup)histIter.next();
	    	 	Histogram hist =(Histogram)hdfToJam.convertHist(currentGroup, histVGroup,  null, mode);
	    	 	//Load gates if not add
                if (mode != FileOpenMode.ADD) {
                	List gateList = hdfToJam.findGates(histVGroup, hist.getType());
                	gateCount = gateList.size();
                	//Loop over gates
                	Iterator gateIter =gateList.iterator();
       	    	 	while (gateIter.hasNext()) {
       	    	 		VirtualGroup gateVGroup = (VirtualGroup)gateIter.next();
       	    	 		hdfToJam.convertGate(hist, gateVGroup, mode);
       	    	 	}                	                	
                }
	    	 }
	    	 //Load scalers
	    	 List scalerList = hdfToJam.findScalers(currentVGroup);
	    	 //Has scalers
	    	 if (scalerList.size()>0) {
	    	 	hdfToJam.convertScalers(currentGroup, (VirtualGroup)scalerList.get(0), mode);
	    	 }
	    	 
	    	 //Load Parameters
	    	 List parameterList = hdfToJam.findParameters(currentVGroup);
	    	 //Has Parameters
	    	 if (parameterList.size()>0) {
	    	 	hdfToJam.convertParameters(currentGroup, (VirtualGroup)parameterList.get(0), mode);
	    	 }
	    	  
	    }
	    
    }
    
    private void convertHDFToJamOriginal(FileOpenMode mode, Group existingGroup, String fileName, List histNames) throws HDFException {
        hdfToJam.setInFile(inHDF);
    	Group currentGroup=null;
        //Set group
        if (mode == FileOpenMode.OPEN) {                   
        	currentGroup=Group.createGroup(fileName, Group.Type.FILE);
        } else if (mode == FileOpenMode.OPEN_ADDITIONAL) {
        	currentGroup=Group.createGroup(fileName, Group.Type.FILE);
        } else if (mode == FileOpenMode.RELOAD) {
            final String sortName = JamStatus.getSingletonInstance()
                    .getSortName();
            Group.setCurrentGroup(sortName);
            currentGroup=Group.getCurrentGroup();
        } // else mode == FileOpenMode.ADD, so use current group
        groupCount=0;
        
        histCount=hdfToJam.convertHistogramsOriginal(currentGroup, mode, histNames);

        final VdataDescription vddScalers= hdfToJam.findScalersOriginal();                
        int numScalers =0;
        if (vddScalers!=null) {
        	numScalers = hdfToJam.convertScalers(currentGroup, vddScalers, mode);
        }

        if (mode != FileOpenMode.ADD) {

        	gateCount = hdfToJam.convertGates(mode);
            int numParams=0;
            /* clear if opening and there are histograms in file */

            final VdataDescription vddParam= hdfToJam.findParametersOriginal();

            if (vddParam!=null) {
            	numParams = hdfToJam.convertParameters(currentGroup, vddParam, mode);
            }
        }
    	
    	
    }
    
    /*
     * non-javadoc: Reads in the histogram and hold them in a tempory array
     * 
     * @exception HDFException thrown if unrecoverable error occurs
     */
    private List loadHistogramAttributesGroup() throws HDFException {
    	
    	final ArrayList lstHistAtt = new ArrayList();
	    FileOpenMode mode=FileOpenMode.ATTRIBUTES;
	    hdfToJam.setInFile(inHDF);
	    
	    //Find groups	
	    List groupVirtualGroups = hdfToJam.findGroups(mode, null);
	    groupCount =groupVirtualGroups.size();
	    //Loop over groups
	    Iterator groupIter =groupVirtualGroups.iterator();
	    while (groupIter.hasNext()) {
	    	VirtualGroup currentVGroup = (VirtualGroup)groupIter.next();
	    	Group currentGroup =hdfToJam.convertGroup(currentVGroup);
	        //Find histograms
	    	List histList =hdfToJam.findHistograms(currentVGroup, null);
	    	histCount = histList.size();
	        //Loop over histograms
	    	Iterator histIter =histList.iterator();
	    	 while (histIter.hasNext()) {
	    	 	VirtualGroup histVGroup = (VirtualGroup)histIter.next();
	    	 	HistogramAttributes histAttributes =(HistogramAttributes)hdfToJam.convertHist(currentGroup, histVGroup,  null, mode);
	    	 	lstHistAtt.add(histAttributes);
	    	 }
	    }
	    return lstHistAtt;
    }
    
    /*
     * non-javadoc: Reads in the histogram and hold them in a tempory array
     * 
     * @exception HDFException thrown if unrecoverable error occurs
     */
    private List loadHistogramAttributesOriginal() throws HDFException {
    	
    	final ArrayList lstHistAtt = new ArrayList();
	    FileOpenMode mode=FileOpenMode.ATTRIBUTES;
	    hdfToJam.setInFile(inHDF);
        
        final VirtualGroup hists = VirtualGroup.ofName(HIST_SECTION);
        /* only the "histograms" VG (only one element) */
        if (hists != null) {
            /* Histogram iterator */
            final Iterator iter = hists.getObjects().iterator();
            // loop begin
            while (iter.hasNext()) {
            	final VirtualGroup currHistGrp = (VirtualGroup) (iter.next());
            	HistogramAttributes histAttributes =(HistogramAttributes)hdfToJam.convertHist(null, currHistGrp, null, mode);
            	lstHistAtt.add(histAttributes);
            }
            //after loop
        }
        return lstHistAtt;
    }

    /*
     * non-javadoc: Confirm overwrite if file exits
     */
    private boolean overWriteExistsConfirm(File file) {
        final boolean writeConfirm = file.exists() ? JOptionPane.YES_OPTION == JOptionPane
                .showConfirmDialog(frame, "Replace the existing file? \n"
                        + file.getName(), "Save " + file.getName(),
                        JOptionPane.YES_NO_OPTION)
                : true;
        if (writeConfirm) {
            /* we've confirmed overwrite with the user. */
            file.delete();
        }

        return writeConfirm;
    }
    
    /**
     * Determines whether a <code>List</code> passed to it
     * <ol>
     * <li>exists, and</li>
     * <li>
     * </ol>
     * has any elements.
     * 
     * @param list
     *            the list to check
     * @return true if the given list exists and has at least one element
     */
    private boolean hasContents(List list) {
        final boolean val = (list != null) && (!list.isEmpty());
        return val;
    }
    
    private void appendFileName(Group group, String fileName) {
    	group.setName(group.getName()+" ("+fileName+")");
    }

    /**
     * @return last file successfully read from or written to.
     */
    public static File getLastValidFile() {
        synchronized (LVF_MONITOR) {
            return lastGoodFile;
        }
    }

    private static void setLastValidFile(File file) {
        synchronized (LVF_MONITOR) {
            lastGoodFile = file;
            PREFS.put(LFILE_KEY, file.getAbsolutePath());
        }
    }

    public void setListener(AsyncListener listener){
    	asyncListener=listener;
    }
    public void removeListener(){
    	asyncListener=null;
   
    }
	/**
	 * Interface to be called when asynchronized IO is completed  
	 */
    public interface AsyncListener {
    	public void CompletedIO(String message, String errorMessage);
    }
    
}