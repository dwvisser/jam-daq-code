package jam.io.hdf;

import jam.data.AbstractHist1D;
import jam.data.DataBase;
import jam.data.DataParameter;
import jam.data.Gate;
import jam.data.Histogram;
import jam.data.Group;
import jam.data.Scaler;
import jam.global.MessageHandler;
import jam.global.JamStatus;
import jam.io.DataIO;
import jam.io.FileOpenMode;
import java.io.FileNotFoundException;
import jam.util.StringUtilities;

import java.awt.Frame;
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

/**
 * Reads and writes HDF files containing spectra, scalers, gates, and additional
 * useful information.
 * 
 * @version 0.5 November 98
 * @author Dale Visser
 * @since JDK1.1
 */
public class HDFIO implements DataIO, JamHDFFields {

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

    private static final String LAST_FILE_KEY = "LastValidFile";

    static private final List EMPTY_LIST = Collections
    .unmodifiableList(new ArrayList());
    
    static {
        lastGoodFile = new File(PREFS.get(LAST_FILE_KEY, System
                .getProperty("user.dir")));
    }
    
    /**
     * Parent frame.
     */
    private final Frame frame;

    /**
     * Where messages get sent (presumably the console).
     */
    private final MessageHandler msgHandler;

    private VirtualGroup histGroup, gateGroup;

    /**
     * <code>HDFile</code> object to write out to.
     */
    private HDFile out;

    /**
     * <code>HDFile<code> object to read from.
     */
    private HDFile in;

    /**
     * Constructor for file read access outside of any GUI context.
     * 
     * @param file
     *            to read from
     * @throws HDFException
     *             if there's a formatting error
     * @throws IOException
     *             if ther's a problem accessing the file
     */
    public HDFIO(File file) throws HDFException, IOException {
        frame = null;
        msgHandler = null;
        in = new HDFile(file, "r");
        in.seek(0);
        in.readObjects();
    }

    /**
     * Class constructor handed references to the main class and message
     * handler.
     * 
     * @param f
     *            the parent window
     * @param mh
     *            where to send output
     */
    public HDFIO(Frame f, MessageHandler mh) {
        frame = f;
        msgHandler = mh;
    }

    /**
     * Writes out to a specified file all the currently held spectra, gates,
     * scalers, and parameters.
     * 
     * @param file
     *            to write to
     */
    public void writeFile(File file) {
        writeFile(true, true, true, true, file);
    }

    /**
     * Writes out to a specified file a list of histograms
     * and gates,
     * scalers, and parameters.
     *  
     * @param file the to write to
     * @param histogramList list of histograms to write
     */
    public void writeFile(File file, List histogramList) {
    	
    	//Check to overwrite if file exist
        if (!overWriteExistsConfirm(file)) {
        	return;
        }
        
    	//Histogram list
        final List histList = new ArrayList();        	
        final Iterator iterHist = histogramList.iterator();
        while (iterHist.hasNext()) {
            final Histogram h = (Histogram) iterHist.next();
            if (h.getArea() > 0) {
            	histList.add(h);
            }
        }
        
        //Gate list
        final List gateList = new ArrayList();
        final Iterator it = Gate.getGateList().iterator();
        while (it.hasNext()) {
            final Gate gate = (Gate) (it.next());
            Histogram histGate =gate.getHistogram();     
            //Gate defined and
            //Does the histogram List contain this gates histogram
            if (gate.isDefined() && histList.contains(histGate)) {
            	gateList.add(Gate.getGateList());
            }
        }
        //Scaler list
        final List scalerList = Scaler.getScalerList();

        //Parameter list
        final List parameterList = DataParameter.getParameterList();
               
         //Asyncronized write 
         asyncWriteFile(file, histList, gateList, scalerList, parameterList);
    	
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
     * @param wrtparameters
     *            if true, parameter values will be written
     * @param file
     *            to write to
     */
    public void writeFile(boolean wrthist, boolean wrtgate, boolean wrtscalers,
            boolean wrtparameters, final File file) {
        if (overWriteExistsConfirm(file)) {
        	//Histogram list
            final List tempHist = wrthist ? Histogram.getHistogramList()
                    : EMPTY_LIST;
            final Iterator iter = tempHist.iterator();
            final List hist = new ArrayList();
            while (iter.hasNext()) {
                final Histogram h = (Histogram) iter.next();
                if (h.getArea() > 0) {
                    hist.add(h);
                }
            }
            //Gate list
            final List gate = new ArrayList();
            if (wrtgate) {
                gate.addAll(Gate.getGateList());
            }
            final Iterator it = gate.iterator();
            while (it.hasNext()) {
                final Gate g = (Gate) (it.next());
                if (!g.isDefined()) {
                    it.remove();
                }
            }
            //Scaler list
            final List scaler = wrtscalers ? Scaler.getScalerList()
                    : EMPTY_LIST;
            //Parameter list
            final List parameter = wrtparameters ? DataParameter
                    .getParameterList() : EMPTY_LIST;
                   
             //Asyncronized write 
             asyncWriteFile(file, hist, gate, scaler, parameter);
        }
    }
    
    /* non-javadoc:
     * Asyncronized write 
     */
    private void asyncWriteFile(final File file, final List histograms, final List gates, final List scalers,
            final List parameters) {
        final Runnable r = new Runnable() {
            public void run() {
                writeFile(file, histograms, gates, scalers, parameters);
            }
        };
        /* Spawn in another thread */
        final Thread t = new Thread(r);
        t.start();

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
    private void writeFile(File file, List hists, List gates, List scalers,
            List parameters) {
    	
        final StringBuffer message = new StringBuffer();
        int progress = 1;
        
        if (file.exists()) {
            /*
             * At this point, we've confirmed overwrite with the user.
             */
            file.delete();
        } 
        try {
            synchronized (this) {
            	out = new HDFile(file, "rw");
            }
        } catch (FileNotFoundException e) {
        	msgHandler.errorOutln("Opening file: "
                        + file.getName());
        } 
        
    	out.addDefaultDataObjects(file.getPath());
    	
        final int progressRange = hists.size() + gates.size()
                + scalers.size() + parameters.size();

        final ProgressMonitor pm = new ProgressMonitor(frame,
                "Saving HDF file", "Building file buffer", progress,
                progressRange);

        message.append("Saved ").append(file.getName()).append(" (");        
        try {
            if (hasContents(hists)) {
                addHistogramSection();
                message.append(hists.size()).append(" histograms");
                final Iterator temp = hists.iterator();
                while (temp.hasNext()) {
                    addHistogram((Histogram) (temp.next()));
                    progress++;
                    setProgress(pm, progress);
                }
            }
            if (hasContents(gates)) {   
                addGateSection();
                message.append(", ").append(gates.size()).append(" gates");
                final Iterator temp = gates.iterator();
                while (temp.hasNext()) {
                    addGate((Gate) (temp.next()));
                    progress++;
                    setProgress(pm, progress);
                }
            }
            if (hasContents(scalers)) {
                addScalerSection(scalers);
                message.append(", ").append(scalers.size()).append(" scalers");
                progress += scalers.size();
                setProgress(pm, progress);
            }
            if (hasContents(parameters)) {
                addParameterSection(parameters);
                message.append(", ").append(parameters.size()).append(" parameters");
                progress += parameters.size();
                setProgress(pm, progress);
            }
            message.append(")");         
        } catch (HDFException e) {
        	msgHandler.errorOutln("Error "
                + file.getName() + "': " + e.toString());
        }   
        msgHandler.messageOut("", MessageHandler.END);
        
        try {                


            
            out.setOffsets();
            
        	out.writeHeader();            
            out.writeDataDescriptorBlock();
            out.writeAllObjects(pm);
            
            setProgressNote(pm, "Closing File");
            out.close();
        } catch (HDFException e) {
            msgHandler.errorOutln("Exception writing to file '"
                    + file.getName() + "': " + e.toString());
        } catch (IOException e) {
            msgHandler.errorOutln("Exception writing to file '"
                    + file.getName() + "': " + e.toString());
        }
            
        
        pm.close();

        synchronized (this) {
        	DataObject.clear();
            out = null; //allows Garbage collector to free up memory
        }
        outln(message.toString());
        setLastValidFile(file);
        System.gc();
    }
    
    /* non-javadoc:
     * Confirm overwrite if file exits
     */
    private boolean overWriteExistsConfirm(File file) {
        final boolean writeConfirm = file.exists() ? JOptionPane.YES_OPTION == JOptionPane
                .showConfirmDialog(frame, "Replace the existing file? \n"
                        + file.getName(), "Save " + file.getName(),
                        JOptionPane.YES_NO_OPTION)
                : true;
        return writeConfirm;                
    }

    
    private void setProgressNote(final ProgressMonitor pm, final String note) {
        final Runnable runner = new Runnable() {
            public void run() {
                pm.setNote(note);
            }
        };
        try {
        	//frame.repaint();
        	SwingUtilities.invokeAndWait(runner);
        } catch(Exception e) {
        	//Bury exception
        }
    }

    private void setProgress(final ProgressMonitor pm, final int value) {
        final Runnable runner = new Runnable() {
            public void run() {
                pm.setProgress(value);
            }
        };
        try {
        	SwingUtilities.invokeAndWait(runner);
    	} catch(Exception e) {
    		//Bury exception
    	}

    }

    private void outln(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                msgHandler.messageOutln(msg);
            }
        });
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
    	return readFile(mode, infile, null);
    }
    /**
     * Read in an HDF file 
     * 
     * @param infile
     *            file to load
     * @param mode
     *            whether to open or reload
     * @param histogramNames
     * 				names of histograms to read, null if all
     * @return <code>true</code> if successful
     */    	
   public boolean readFile(FileOpenMode mode, File infile, List histogramNames) {    	
        boolean outF = true;
        final int progressRange = mode == FileOpenMode.ADD ? 4 : 6;
        final ProgressMonitor pm = new ProgressMonitor(frame,
                "Reading HDF file", "Reading from disk", 0, progressRange);
        final StringBuffer message = new StringBuffer();
        try {
            if (mode == FileOpenMode.OPEN) {
                message.append("Opened ").append(infile.getName()).append(" (");
                DataBase.getInstance().clearAllLists();
            }else if(mode==FileOpenMode.OPEN_ADDITIONAL) {
                message.append("Opened Additional ").append(infile.getName()).append(" (");
            } else if (mode == FileOpenMode.RELOAD) {
                message.append("Reloaded ").append(infile.getName()).append(
                        " (");
            } else { //ADD
                message.append("Adding histogram counts in ").append(
                        infile.getName()).append(" (");
            }
            synchronized (this) {
                in = new HDFile(infile, "r");
            }
            if (!in.isHDFFile()){
            	 msgHandler.errorOutln(infile+" is not a valid HDF File!");
            	 return false;
            }
            
            in.seek(0);
            /* read file into set of DataObject's, set their internal variables */
            pm.setNote("Parsing objects");
            in.readObjects();
            pm.setProgress(1);
            pm.setNote("Getting histograms");
            
            //Set group
            if (mode == FileOpenMode.OPEN) {
            	Group.clearList();
            	Group.createGroup(infile.getName(), Group.Type.FILE);
            }else if(mode==FileOpenMode.OPEN_ADDITIONAL) {
            	Group.createGroup(infile.getName(), Group.Type.FILE);
        	} else if (mode == FileOpenMode.RELOAD) {
        		String sortName = JamStatus.getSingletonInstance().getSortName();
        		Group.setCurrentGroup(sortName);
        	} else if (mode ==FileOpenMode.ADD) {
        		//use current group
        	}
            
            int numHists=getHistograms(mode, histogramNames);
            message.append(""+numHists+" histograms");
            pm.setProgress(2);
            pm.setNote("Getting scalers");
            int numScalers=getScalers(mode);
            message.append(", ").append(numScalers).append(" scalers");
            pm.setProgress(3);
            if (mode != FileOpenMode.ADD) {
                pm.setNote("Getting gates");
                int numGates=getGates(mode);
                /* clear if opening and there are histograms in file */
                message.append(", ").append(numGates+" gates");
                
                pm.setProgress(4);
                pm.setNote("Getting parameters");
                int numParams = getParameters(mode);
                message.append(", "+numParams+" parameters");
                pm.setProgress(5);
            }
            message.append(')');
            pm.setNote("Done");
            in.close();
            synchronized (this) {
                /* destroys reference to HDFile (and its DataObject's) */
                in = null;
            }
            msgHandler.messageOutln(message.toString());
            setLastValidFile(infile);
        } catch (HDFException except) {
            msgHandler.errorOutln(except.toString());
            outF = false;
        } catch (IOException except) {
            msgHandler.errorOutln(except.toString());
            outF = false;
        }
        System.gc();
        pm.close();
        return outF;
    }
    
    /** 
     * Read the histograms in.
     * @param infile to read from
     * @return list of attributes
     */
    public List readHistogramAttributes(File infile){
    	final List histAttributes = new ArrayList();
    	try {
			/* Read in histogram names */
	    	in = new HDFile(infile, "r");
            if (!in.isHDFFile()){
           	 msgHandler.errorOutln(infile+" is not a valid HDF File!");
           	 return histAttributes;
           }

	    	in.readObjects();
	    	histAttributes.addAll(loadHistogramAttributes());
			in.close();
        } catch (HDFException except) {
            msgHandler.errorOutln(except.toString());
	    } catch (IOException except) {
	        msgHandler.errorOutln(except.toString());
	    }		
		return histAttributes;   	
    }
    
	/* non-javadoc:
	 * Reads in the histogram and hold them in a tempory array
	 * 
	 * @exception HDFException
	 *                thrown if unrecoverable error occurs
	 */
	private List loadHistogramAttributes() throws HDFException {
		final ArrayList lstHistAtt = new ArrayList();
		NumericalDataGroup ndg = null;
		/* I check ndgErr==null to determine if error bars exist */
		NumericalDataGroup ndgErr = null;
		/* get list of all VG's in file */
		final java.util.List groups = in.ofType(DataObject.DFTAG_VG);
		final VirtualGroup hists = VirtualGroup.ofName(groups,
				JamHDFFields.HIST_SECTION_NAME);
		/* only the "histograms" VG (only one element) */
		ScientificData sdErr = null;
		if (hists != null) {
			/* get list of all DIL's in file */
			final java.util.List labels = in.ofType(DataObject.DFTAG_DIL);
			/* get list of all DIA's in file */
			final java.util.List annotations = in
					.ofType(DataObject.DFTAG_DIA);
			final Iterator temp = hists.getObjects().iterator();
			while (temp.hasNext()) {
				final VirtualGroup current = (VirtualGroup) (temp.next());
				final java.util.List tempVec = in.ofType(current
						.getObjects(), DataObject.DFTAG_NDG);
				final NumericalDataGroup[] numbers = new NumericalDataGroup[tempVec
						.size()];
				tempVec.toArray(numbers);
				if (numbers.length == 1) {
					ndg = numbers[0]; //only one NDG -- the data
				} else if (numbers.length == 2) {
					if (DataIDLabel.withTagRef(labels, DataObject.DFTAG_NDG,
							numbers[0].getRef()).getLabel().equals(
							JamHDFFields.ERROR_LABEL)) {
						ndg = numbers[1];
						ndgErr = numbers[0];
					} else {
						ndg = numbers[0];
						ndgErr = numbers[1];
					}
				} else {
					throw new HDFException("Invalid number of data groups ("
							+ numbers.length + ") in NDG.");
				}
				final ScientificData sd = (ScientificData) (in.ofType(ndg
						.getObjects(), DataObject.DFTAG_SD).get(0));
				final ScientificDataDimension sdd = (ScientificDataDimension) (in
						.ofType(ndg.getObjects(), DataObject.DFTAG_SDD).get(0));
				final DataIDLabel numLabel = DataIDLabel.withTagRef(labels, ndg
						.getTag(), ndg.getRef());
				final int number = Integer.parseInt(numLabel.getLabel());
				final byte histNumType = sdd.getType();
				sd.setNumberType(histNumType);
				final int histDim = sdd.getRank();
				sd.setRank(histDim);
				final int sizeX = sdd.getSizeX();
				final int sizeY = (histDim == 2) ? sdd.getSizeY() : 0;
				final DataIDLabel templabel = DataIDLabel.withTagRef(labels,
						current.getTag(), current.getRef());
				final DataIDAnnotation tempnote = DataIDAnnotation.withTagRef(
						annotations, current.getTag(), current.getRef());
				final String name = templabel.getLabel();
				final String title = tempnote.getNote();
				if (ndgErr != null) {
					sdErr = (ScientificData) (in.ofType(ndgErr
							.getObjects(), DataObject.DFTAG_SD).get(0));
					sdErr.setRank(histDim);
					sdErr.setNumberType(NumberType.DOUBLE);
					/*
					 * final ScientificDataDimension sddErr
					 * =(ScientificDataDimension)(in.ofType(
					 * ndgErr.getObjects(),DataObject.DFTAG_SDD).get(0));
					 */
				}
				/* read in data */
				final Object dataArray;
				if (histDim == 1) {
					if (histNumType == NumberType.INT) {
						dataArray = sd.getData1d(in, sizeX);
					} else { //DOUBLE
						dataArray = sd.getData1dD(in, sizeX);
					}
					/*if (ndgErr != null) {
						histogram.setErrors(sdErr.getData1dD(sizeX));
					}*/
				} else { //2d
					if (histNumType == NumberType.INT) {
						dataArray = sd.getData2d(in, sizeX, sizeY);
					} else {
						dataArray = sd.getData2dD(in, sizeX, sizeY);
					}
				}
				/* Add histogram */
				HistogramAttributes histAtt = new HistogramAttributes();
				histAtt.name = name;
				histAtt.title = title;
				histAtt.number = number;
				histAtt.sizeX = sizeX;
				histAtt.sizeY = sizeY;
				histAtt.histDim = histDim;
				histAtt.histNumType = histNumType;
				histAtt.dataArray = dataArray;
				if (ndgErr != null){
					histAtt.errorArray=sdErr.getData1dD(in, sizeX);
				}
				lstHistAtt.add(histAtt);
			}
		} //hist !=null
		return lstHistAtt;
	}

    /**
     * Adds data objects for the virtual group of histograms.
     * 
     * @see #addHistogram(Histogram)
     */
    protected void addHistogramSection() {
        synchronized (this) {
            histGroup = new VirtualGroup(HIST_SECTION_NAME,
                    FILE_SECTION_NAME);
        }
        new DataIDLabel(histGroup, HIST_SECTION_NAME);
    }

    /**
     * Add the virtual group representing a single histogram, and link it into
     * the virtual group representing all histograms.
     * 
     * @param h
     *            the histogram to add
     * @see #addHistogramSection()
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     */
    protected void addHistogram(Histogram h) throws HDFException {
        ScientificData sd;
        final VirtualGroup temp = new VirtualGroup(h.getName(),
                HIST_TYPE_NAME);
        histGroup.addDataObject(temp); //add to Histogram section vGroup
        new DataIDLabel(temp, h.getName());
        /* vGroup label is Histogram name */
        new DataIDAnnotation(temp, h.getTitle());
        /* vGroup Annotation is Histogram title */
        final NumericalDataGroup ndg = new NumericalDataGroup();
        /* NDG to contain data */
        new DataIDLabel(ndg, Integer.toString(h.getNumber()));
        /* make the NDG label the histogram number */
        temp.addDataObject(ndg);
        /* add to specific histogram vGroup (other info maybe later) */
        final ScientificDataDimension sdd = out.getSDD(h);
        ndg.addDataObject(sdd); //use new SDD
        temp.addDataObject(sdd); //use new SDD
        final Histogram.Type type = h.getType();
        if (type == Histogram.Type.ONE_DIM_INT) {
            final AbstractHist1D h1 = (AbstractHist1D) h;
            sd = new ScientificData((int[]) h.getCounts());
            if (h1.errorsSet()) {
                NumericalDataGroup ndgErr = new NumericalDataGroup();
                new DataIDLabel(ndgErr, ERROR_LABEL);
                temp.addDataObject(ndgErr);
                ScientificDataDimension sddErr = out.getSDD(h,
                        NumberType.DOUBLE);
                /* explicitly floating point */
                ndgErr.addDataObject(sddErr);
                temp.addDataObject(sddErr);
                ScientificData sdErr = new ScientificData(h1.getErrors());
                ndgErr.addDataObject(sdErr);
                temp.addDataObject(sdErr);
            }
        } else if (type == Histogram.Type.ONE_D_DOUBLE) {
            final AbstractHist1D h1 = (AbstractHist1D) h;
            sd = new ScientificData((double[]) h.getCounts());
            if (h1.errorsSet()) {
                NumericalDataGroup ndgErr = new NumericalDataGroup();
                new DataIDLabel(ndgErr, ERROR_LABEL);
                temp.addDataObject(ndgErr);
                ScientificDataDimension sddErr = sdd;
                /* explicitly floating point */
                ndgErr.addDataObject(sddErr);
                ScientificData sdErr = new ScientificData(h1.getErrors());
                ndgErr.addDataObject(sdErr);
                temp.addDataObject(sdErr);
            }
        } else if (type == Histogram.Type.TWO_DIM_INT) {
            sd = new ScientificData((int[][]) h.getCounts());
        } else if (type == Histogram.Type.TWO_D_DOUBLE) {
            sd = new ScientificData((double[][]) h.getCounts());
        } else {
            throw new IllegalArgumentException(
                    "HDFIO encountered a Histogram of unknown type.");
        }
        ndg.addDataObject(sd);
        temp.addDataObject(sd);
    }

    /**
     * looks for the special Histogram section and reads the data into memory.
     * 
     * @param mode
     *            whether to open or reload
     * @param sb
     *            summary message under construction
     * @param histogramNames
     * 			  names of histograms to read, null if all
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     * @throws IllegalStateException
     *             if any histogram apparently has more than 2 dimensions
     */
    private int getHistograms(FileOpenMode mode, List histogramNames)
            throws HDFException {
        int numHists=0;
        /* get list of all VG's in file */
        final List groups = in.ofType(DataObject.DFTAG_VG);
        final VirtualGroup hists = VirtualGroup.ofName(groups,
                HIST_SECTION_NAME);
        /* only the "histograms" VG (only one element) */
        if (hists != null) {
        	//Message 
        	numHists =hists.getObjects().size();
        	
            /* get list of all DIL's in file */
            final List labels = in.ofType(DataObject.DFTAG_DIL);
            /* get list of all DIA's in file */
            final List annotations = in.ofType(DataObject.DFTAG_DIA);
            //Histogram iterator
            final Iterator temp = hists.getObjects().iterator();            
            while (temp.hasNext()) {
                final VirtualGroup current = (VirtualGroup) (temp.next());
                
            	NumericalDataGroup ndg = null;
                /* I check ndgErr==null to determine if error bars exist */
                NumericalDataGroup ndgErr = null;
                /* only the "histograms" VG (only one element) */
                ScientificData sdErr = null;        
            	
                final List tempVec = in.ofType(current.getObjects(),
                        DataObject.DFTAG_NDG);
                final NumericalDataGroup[] numbers = new NumericalDataGroup[tempVec
                        .size()];
                tempVec.toArray(numbers);
                if (numbers.length == 1) {
                    ndg = numbers[0]; //only one NDG -- the data
                } else if (numbers.length == 2) {
                    if (DataIDLabel.withTagRef(labels, DataObject.DFTAG_NDG,
                            numbers[0].getRef()).getLabel().equals(ERROR_LABEL)) {
                        ndg = numbers[1];
                        ndgErr = numbers[0];
                    } else {
                        ndg = numbers[0];
                        ndgErr = numbers[1];
                    }
                } else {
                    throw new IllegalStateException(
                            "Invalid number of data groups (" + numbers.length
                                    + ") in NDG.");
                }
                final ScientificData sd = (ScientificData) (in.ofType(ndg
                        .getObjects(), DataObject.DFTAG_SD).get(0));
                final ScientificDataDimension sdd = (ScientificDataDimension) (in
                        .ofType(ndg.getObjects(), DataObject.DFTAG_SDD).get(0));
                final DataIDLabel numLabel = DataIDLabel.withTagRef(labels, ndg
                        .getTag(), ndg.getRef());
                final int number = Integer.parseInt(numLabel.getLabel());
                final byte histNumType = sdd.getType();
                sd.setNumberType(histNumType);
                final int histDim = sdd.getRank();
                sd.setRank(histDim);
                final int sizeX = sdd.getSizeX();
                int sizeY = 0;
                if (histDim == 2) {
                    sizeY = sdd.getSizeY();
                }
                final DataIDLabel templabel = DataIDLabel.withTagRef(labels,
                        current.getTag(), current.getRef());
                final DataIDAnnotation tempnote = DataIDAnnotation.withTagRef(
                        annotations, current.getTag(), current.getRef());
                final String name = templabel.getLabel();
                final String title = tempnote.getNote();
                if (ndgErr != null) {
                    sdErr = (ScientificData) (in.ofType(ndgErr.getObjects(),
                            DataObject.DFTAG_SD).get(0));
                    
                    sdErr.setRank(histDim);
                    sdErr.setNumberType(NumberType.DOUBLE);
                } else {
                	sdErr=null;
                }
                
                //Given name list check that that the name is in the list
                if (histogramNames==null || histogramNames.contains(name)) {
                	
                	createHistogram(mode, name, title,  number, histNumType, 
                		       		histDim, sizeX, sizeY, sd, sdErr);
                } 
                	/*
                	Iterator iter = histogramNames.iterator();
                	while(iter.hasNext()) {
                		String listName =(String)iter.next();
                		if (listName.compareTo(name)==0) {
                        	createHistogram(mode, name, title,  number, histNumType, 
                		       		histDim, sizeX, sizeY, sd, sdErr);
                			
                		}
                	}
                	*/

            }
        }
        return numHists;        
    }
    
    /* non-javadoc:
     * Create a histgram given all the attributes and data
     */
    private void createHistogram(FileOpenMode mode, String name, String title, int number,
    		                     byte histNumType, int histDim, int sizeX, int sizeY, 
								 ScientificData sd, ScientificData sdErr)  throws HDFException {
        Histogram histogram;
        final StringUtilities su = StringUtilities.instance();        
        
    	if (mode.isOpenMode()) {
            if (histDim == 1) {
                if (histNumType == NumberType.INT) {
                    histogram = Histogram.createHistogram(sd
                            .getData1d(in, sizeX), name, title);
                } else { //DOUBLE
                    histogram = Histogram.createHistogram(sd
                            .getData1dD(in, sizeX), name, title);
                }
                if (sdErr != null) {
                    ((AbstractHist1D) histogram).setErrors(sdErr
                            .getData1dD(in, sizeX));
                }
            } else { //2d
                if (histNumType == NumberType.INT) {
                    histogram = Histogram.createHistogram(sd.getData2d(
                    		in, sizeX, sizeY), name, title);
                } else {
                    histogram = Histogram.createHistogram(sd
                            .getData2dD(in, sizeX, sizeY), name, title);
                }
            }
            histogram.setNumber(number);
        } else if (mode == FileOpenMode.RELOAD) {
        	Group group = Group.getSortGroup();
            histogram = group.getHistogram(su.makeLength(name,
                    Histogram.NAME_LENGTH));                    	
            if (histogram != null) {
                if (histDim == 1) {
                    if (histNumType == NumberType.INT) {
                        histogram.setCounts(sd.getData1d(in, sizeX));
                    } else {
                        histogram.setCounts(sd.getData1dD(in, sizeX));
                    }
                } else { // 2-d
                    if (histNumType == NumberType.INT) {
                        histogram.setCounts(sd.getData2d(in, sizeX, sizeY));
                    } else {
                        histogram
                                .setCounts(sd.getData2dD(in, sizeX, sizeY));
                    }
                }
            }
        } else if (mode == FileOpenMode.ADD) { 
        	Group group = Group.getCurrentGroup();
            histogram = group.getHistogram(su.makeLength(name,
                    Histogram.NAME_LENGTH));            
            if (histogram != null) {
                if (histDim == 1) {
                    if (histNumType == NumberType.INT) {
                        histogram.addCounts(sd.getData1d(in, sizeX));
                    } else {
                        histogram.addCounts(sd.getData1dD(in, sizeX));
                    }
                } else { // 2-d
                    if (histNumType == NumberType.INT) {
                        histogram.addCounts(sd.getData2d(in, sizeX, sizeY));
                    } else {
                        histogram
                                .addCounts(sd.getData2dD(in, sizeX, sizeY));
                    }
                }
            }
        }    	
    }
    /**
     * Adds data objects for the virtual group of gates.
     * 
     * @see #addGate(Gate)
     */
    protected void addGateSection() {
        synchronized (this) {
            gateGroup = new VirtualGroup(GATE_SECTION_NAME,
                    FILE_SECTION_NAME);
        }
        new DataIDLabel(gateGroup, GATE_SECTION_NAME);
    }

    /**
     * Add the virtual group representing a single gate, and link it into the
     * virtual group representing all gates.
     * 
     * @param g
     *            the gate to add
     * @see #addGateSection()
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     */
    protected void addGate(Gate g) throws HDFException {
        String gateType = GATE_1D_TYPE_NAME;
        int size = 1;
        String[] names = GATE_2D_NAMES;
        int[] x = new int[0];
        int[] y = new int[0];
        final short[] types = { VdataDescription.DFNT_INT32,
                VdataDescription.DFNT_INT32 };
        final short[] orders = { 1, 1 };
        final String name = g.getName();
        if (g.getDimensionality() == 1) {
            names = GATE_1D_NAMES;
        } else { //2d
            gateType = GATE_2D_TYPE_NAME;
            size = g.getBananaGate().npoints;
            x = g.getBananaGate().xpoints;
            y = g.getBananaGate().ypoints;
        }
        /* get the VG for the current gate */
        final VirtualGroup vg = new VirtualGroup(name, gateType);
        gateGroup.addDataObject(vg); //add to Gate section vGroup
        final VdataDescription desc = new VdataDescription(name, gateType,
                size, names, types, orders);
        final Vdata data = new Vdata(desc);
        vg.addDataObject(desc); //add vData description to gate VG
        vg.addDataObject(data); //add vData to gate VG
        if (g.getDimensionality() == 1) {
            data.addInteger(0, 0, g.getLimits1d()[0]);
            data.addInteger(1, 0, g.getLimits1d()[1]);
        } else { //2d
            for (int i = 0; i < size; i++) {
                data.addInteger(0, i, x[i]);
                data.addInteger(1, i, y[i]);
            }
        }
        data.refreshBytes();
        /* add Histogram links... */
        final VirtualGroup hist = VirtualGroup.ofName(out
                .ofType(DataObject.DFTAG_VG), g.getHistogram().getName());
        /* add name as note to vg */
        new DataIDAnnotation(vg, g.getHistogram().getName());
        if (hist != null) {
            hist.addDataObject(vg);
            //reference the Histogram in the gate group
        }
    }

    /*
     * non-javadoc: Retrieve the gates from the file.
     * 
     * @param mode whether to open or reload @throws HDFException thrown if
     * unrecoverable error occurs
     */
    private int getGates(FileOpenMode mode) {
    	int numGates=0;
        final StringUtilities su = StringUtilities.instance();
        Gate g = null;
        /* get list of all VG's in file */
        final List groups = in.ofType(DataObject.DFTAG_VG);
        /* get only the "gates" VG (only one element) */
        final VirtualGroup gates = VirtualGroup.ofName(groups,
                GATE_SECTION_NAME);
        final List annotations = in.ofType(DataObject.DFTAG_DIA);
        if (gates != null) {
        	numGates=gates.getObjects().size();
            final Iterator temp = gates.getObjects().iterator();
            while (temp.hasNext()) {
                final VirtualGroup currVG = (VirtualGroup) (temp.next());
                final VdataDescription VH = (VdataDescription) (in.ofType(
                        currVG.getObjects(), DataObject.DFTAG_VH).get(0));
                if (VH != null) {
                    final Vdata VS = (Vdata) (DataObject.getObject(DataObject.DFTAG_VS,
                            VH.getRef()));
                    //corresponding VS
                    final int numRows = VH.getNumRows();
                    final String gname = currVG.getName();
                    final String hname = DataIDAnnotation.withTagRef(
                            annotations, currVG.getTag(), currVG.getRef())
                            .getNote();
                    if (mode.isOpenMode()) {
                    	String groupName = Group.getCurrentGroup().getName();
                    	String histFullName = groupName+"/"+su.makeLength(hname, Histogram.NAME_LENGTH);
                        final Histogram h = Histogram.getHistogram(histFullName);
                        g = h == null ? null : new Gate(gname, h);
                    } else { //reload
                        g = Gate
                                .getGate(su.makeLength(gname, Gate.NAME_LENGTH));
                    }
                    if (g != null) {
                        if (g.getDimensionality() == 1) { //1-d gate
                            g.setLimits(VS.getInteger(0, 0).intValue(), VS
                                    .getInteger(0, 1).intValue());
                        } else { //2-d gate
                            final Polygon pg = new Polygon();
                            for (int i = 0; i < numRows; i++) {
                                pg.addPoint(VS.getInteger(i, 0).intValue(), VS
                                        .getInteger(i, 1).intValue());
                            }
                            g.setLimits(pg);
                        }
                    }
                } else {
                    msgHandler
                            .warningOutln("Problem processing a VH in HDFIO!");
                }
            }
        }
        return numGates;
    }

    /**
     * Adds data objects for the virtual group of scalers.
     * 
     * @param scalers
     *            the list of scalers
     * @throws HDFException
     *             thrown if unrecoverable error occurs
     */
    protected void addScalerSection(List scalers) throws HDFException {
        final StringUtilities su = StringUtilities.instance();
        final short[] types = { VdataDescription.DFNT_INT32,
                VdataDescription.DFNT_CHAR8, VdataDescription.DFNT_INT32 };
        final short[] orders = new short[3];
        final int size = scalers.size();
        orders[0] = 1; //number
        orders[1] = 0; //name ... loop below picks longest name for dimension
        final Iterator iter = scalers.iterator();
        while (iter.hasNext()) {
            final int dimtest = ((Scaler) (iter.next())).getName().length();
            if (dimtest > orders[1]) {
                orders[1] = (short) dimtest;
            }
        }
        orders[2] = 1; //value
        final VirtualGroup scalerGroup = new VirtualGroup(
                SCALER_SECTION_NAME, FILE_SECTION_NAME);
        new DataIDLabel(scalerGroup, SCALER_SECTION_NAME);
        final String name = SCALER_SECTION_NAME;
        final String scalerType = SCALER_TYPE_NAME;
        final String[] names = SCALER_COLUMN_NAMES;
        final VdataDescription desc = new VdataDescription(name,
                scalerType, size, names, types, orders);
        final Vdata data = new Vdata(desc);
        scalerGroup.addDataObject(desc); //add vData description to gate VG
        scalerGroup.addDataObject(data); //add vData to gate VG

        for (int i = 0; i < size; i++) {
            final Scaler s = (Scaler) (scalers.get(i));
            data.addInteger(0, i, s.getNumber());
            data.addChars(1, i, su.makeLength(s.getName(), orders[1]));
            data.addInteger(2, i, s.getValue());
        }
        data.refreshBytes();
    }

    /*
     * non-javadoc: Retrieve the scalers from the file.
     * 
     * @param mode whether to open, reload or add @throws HDFException if there
     * is a problem retrieving scalers
     */
    private int getScalers(FileOpenMode mode) {
    	int numScalers =0;
        final VdataDescription VH = VdataDescription.ofName(in
                .ofType(DataObject.DFTAG_VH), SCALER_SECTION_NAME);
        /* only the "scalers" VH (only one element) in the file */
        if (VH != null) {
            /* get the VS corresponding to the given VH */
            final Vdata VS = (Vdata) (DataObject.getObject(DataObject.DFTAG_VS, VH
                    .getRef()));
            numScalers = VH.getNumRows();

            for (int i = 0; i < numScalers; i++) {
                final Scaler s;
                final String sname = VS.getString(i, 1);
                if (mode.isOpenMode()) {
                    s = new Scaler(sname, VS.getInteger(i, 0).intValue());
                } else { //mode==RELOAD
                    s = Scaler.getScaler(sname);
                }
                if (s != null) {
                    final int fileValue = VS.getInteger(i, 2).intValue();
                    if (mode == FileOpenMode.ADD) {
                        s.setValue(s.getValue() + fileValue);
                    } else {
                        s.setValue(fileValue);
                    }
                }
            }
        }
        return numScalers;
    }

    /**
     * Adds data objects for the virtual group of parameters.
     * 
     * @param parameters
     *            the parameters to write?
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     */
    protected void addParameterSection(List parameters) throws HDFException {
        final short[] types = { VdataDescription.DFNT_CHAR8,
                VdataDescription.DFNT_FLT32 };
        final short[] orders = new short[2];
        final int size = parameters.size();
        /* set order values */
        orders[0] = 0; //name ... loop below picks longest name for dimension
        final Iterator iter = parameters.iterator();
        while (iter.hasNext()) {
            final int lenMax = ((DataParameter) (iter.next())).getName()
                    .length();
            if (lenMax > orders[0]) {
                orders[0] = (short) lenMax;
            }
        }
        orders[1] = 1; //value
        final VirtualGroup parameterGroup = new VirtualGroup(
                PARAMETER_SECTION_NAME, FILE_SECTION_NAME);
        new DataIDLabel(parameterGroup, PARAMETER_SECTION_NAME);
        final String name = PARAMETER_SECTION_NAME;
        final String parameterType = PARAMETER_TYPE_NAME;
        final String[] names = PARAMETER_COLUMN_NAMES;
        final VdataDescription desc = new VdataDescription(name,
                parameterType, size, names, types, orders);
        final Vdata data = new Vdata(desc);
        parameterGroup.addDataObject(desc); //add vData description to gate VG
        parameterGroup.addDataObject(data); //add vData to gate VG
        for (int i = 0; i < size; i++) {
            final StringUtilities su = StringUtilities.instance();
            final DataParameter p = (DataParameter) (parameters.get(i));
            data.addChars(0, i, su.makeLength(p.getName(), orders[0]));
            data.addFloat(1, i, (float) p.getValue());
        }
        data.refreshBytes();
    }

    /*
     * non-javadoc: retrieve the parameters from the file
     * 
     * @param mode whether to open or reload @throws HDFException if an error
     * occurs reading the parameters
     */
    private int  getParameters(FileOpenMode mode) {
    	int numParameters =0;
        final VdataDescription VH = VdataDescription.ofName(in
                .ofType(DataObject.DFTAG_VH), PARAMETER_SECTION_NAME);
        /* only the "parameters" VH (only one element) in the file */
        if (VH != null) {
            /* Get corresponding VS for this VH */
            final Vdata VS = (Vdata) (DataObject.getObject(DataObject.DFTAG_VS, VH
                    .getRef()));
            numParameters = VH.getNumRows();
            for (int i = 0; i < numParameters; i++) {
                final String pname = VS.getString(i, 0);
                /* make if OPEN, retrieve if RELOAD */
                final DataParameter p = mode.isOpenMode() ? new DataParameter(
                        pname)
                        : DataParameter.getParameter(pname);
                if (p != null) {
                    p.setValue(VS.getFloat(i, 1).floatValue());
                }
            }
        }
        return numParameters;        
    }

    /**
     * Determines whether a <code>List</code> passed to it
     * <ol>
     * <li>exists, and</li>
     * <li>
     * </ol>
     * has any elements.
     * 
     * @param v
     *            the list to check
     * @return true if the given list exists and has at least one element
     */
    protected boolean hasContents(List v) {
        final boolean val = (v != null) && (!v.isEmpty());
        return val;
    }

    /**
     * @return last file successfully read from or written to.
     */
    public static File getLastValidFile() {
        synchronized (LVF_MONITOR) {
            return lastGoodFile;
        }
    }

    private static void setLastValidFile(File f) {
        synchronized (LVF_MONITOR) {
            lastGoodFile = f;
            PREFS.put(LAST_FILE_KEY, f.getAbsolutePath());
        }
    }

	/**
	 * Class to hold histogram properties while we decide if we should load them.
	 *  
	 */
	public class HistogramAttributes {

		String name;

		String title;

		int number;

		int sizeX;

		int sizeY;

		int histDim;

		byte histNumType;

		Object dataArray; //generic data array
		
		Object errorArray;
		
		/**
		 * 
		 * @return the name of the histogram
		 */
		public String getName() {
			return name;
		}
	}

}