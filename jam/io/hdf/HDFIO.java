package jam.io.hdf;

import jam.data.AbstractHist1D;
import jam.data.DataBase;
import jam.data.DataParameter;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.io.DataIO;
import jam.io.FileOpenMode;
import jam.util.StringUtilities;

import java.awt.Frame;
import java.awt.Polygon;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
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
 * @version 0.5 November 98, January 2005
 * @author Dale Visser, Ken Swartz
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

    private VirtualGroup allHistogramsGroup, allGatesGroup;

    /**
     * <code>HDFile</code> object to write out to.
     */
    //private HDFile out;

    /**
     * <code>HDFile<code> object to read from.
     */
    private HDFile inHDF;

    private ConvertJamObjToHDFObj convertJamToHDF;
    private ConvertHDFObjToJamObj convertHDFToJam;

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
        convertJamToHDF = new ConvertJamObjToHDFObj();
		convertHDFToJam = new ConvertHDFObjToJamObj(); 
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
     * @param histograms list of histograms to write
     */
    public void writeFile(File file, List histograms) { 	
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
            final Histogram histGate =gate.getHistogram();     
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
        asyncWriteFile(file, histList, gateList, scalerList, paramList);    	
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
            final List parameter = wrtparams ? DataParameter
                    .getParameterList() : EMPTY_LIST;
            asyncWriteFile(file, histList, gateList, scaler, parameter);
        }
    }
    
    /* non-javadoc:
     * Asyncronized write 
     */
    private void asyncWriteFile(final File file, final List histograms, final List gates, final List scalers,
            final List parameters) {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
            	try {
            		writeFile(file, histograms, gates, scalers, parameters);
            	} catch (HDFException hde) {
            		outerr("Writing hdf file "+hde.getMessage());
            	}
            }
        });
        thread.start();
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
     * @throws HDFException if there's a problem creating data ojbects
     */
    private void writeFile(File file, List hists, List gates, List scalers,
            List parameters) throws HDFException {
        final StringBuffer message = new StringBuffer();
        int progress = 1;
    	DataObject.clearAll();
    	addDefaultDataObjects(file.getPath());
        final int totalToDo = hists.size() + gates.size()
                + scalers.size() + parameters.size();
        final ProgressMonitor monitor = new ProgressMonitor(frame,
                "Saving HDF file", "Building file buffer", progress,
                totalToDo);
        message.append("Saved ").append(file.getName()).append(" (");        
        try {
            if (hasContents(hists)) {
                addHistogramSection();
                message.append(hists.size()).append(" histograms");
                final Iterator iter = hists.iterator();
                while (iter.hasNext()) {
                	Histogram hist =(Histogram)iter.next();
                	VirtualGroup histVGroup = convertJamToHDF.convertHistogram(hist);
                	allHistogramsGroup.addDataObject(histVGroup);
                	//addHistogram((Histogram) (temp.next()));
                    progress++;
                    setProgress(monitor, progress);
                }
            }
            if (hasContents(gates)) {   
                addGateSection();
                message.append(", ").append(gates.size()).append(" gates");
                final Iterator iter = gates.iterator();
                while (iter.hasNext()) {
                	Gate gate = (Gate)iter.next();
                	VirtualGroup gateVGroup = convertJamToHDF.convertGate(gate);
                	allGatesGroup.addDataObject(gateVGroup); 
                    //addGate((Gate) (temp.next()));
                    progress++;
                    setProgress(monitor, progress);
                }
            }
            if (hasContents(scalers)) {
            	VirtualGroup scalerVGroup =convertJamToHDF.convertScalers(scalers);
                //addScalerSection(scalers);
                message.append(", ").append(scalers.size()).append(" scalers");
                progress += scalers.size();
                setProgress(monitor, progress);
            }
            if (hasContents(parameters)) {
            	VirtualGroup parameterVGroup =convertJamToHDF.convertParameters(parameters);
                //addParameterSection(parameters);
                message.append(", ").append(parameters.size()).append(" parameters");
                progress += parameters.size();
                setProgress(monitor, progress);
            }
            message.append(")");         
        } catch (HDFException e) {
        	msgHandler.errorOutln("Error "
                + file.getName() + "': " + e.toString());
        }   
        msgHandler.messageOut("", MessageHandler.END);
        HDFile out;
        try {
            synchronized (this) {
            	out = new HDFile(file, "rw");
            }            
        	out.writeFile(monitor);    
            setProgressNote(monitor, "Closing File");
            out.close();
        } catch (FileNotFoundException e) {
        	msgHandler.errorOutln("Opening file: "
                        + file.getName());             
        } catch (HDFException e) {
            msgHandler.errorOutln("Exception writing to file '"
                    + file.getName() + "': " + e.toString());
        } catch (IOException e) {
            msgHandler.errorOutln("Exception writing to file '"
                    + file.getName() + "': " + e.toString());
        }
        monitor.close();
        synchronized (this) {
        	DataObject.clearAll();
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
        if (writeConfirm) {
            /*  we've confirmed overwrite with the user. */
            file.delete();
        } 

        return writeConfirm;                
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
     * @param histNames
     *            names of histograms to read, null if all
     * @return <code>true</code> if successful
     */
    public boolean readFile(FileOpenMode mode, File infile, List histNames) {
        boolean rval = true;
        final int amountToDo = mode == FileOpenMode.ADD ? 4 : 6;
        final ProgressMonitor monitor = new ProgressMonitor(frame,
                "Reading HDF file", "Reading from disk", 0, amountToDo);
        if (!HDFile.isHDFFile(infile)) {
            msgHandler.errorOutln(infile + " is not a valid HDF File!");
            rval = false;
        }
        if (rval) {
            final StringBuffer message = new StringBuffer();
            try {
                if (mode == FileOpenMode.OPEN) {
                    message.append("Opened ").append(infile.getName()).append(
                            " (");
                    DataBase.getInstance().clearAllLists();
                } else if (mode == FileOpenMode.OPEN_ADDITIONAL) {
                    message.append("Opened Additional ").append(
                            infile.getName()).append(" (");
                } else if (mode == FileOpenMode.RELOAD) {
                    message.append("Reloaded ").append(infile.getName())
                            .append(" (");
                } else { //ADD
                    message.append("Adding histogram counts in ").append(
                            infile.getName()).append(" (");
                }

                synchronized (this) {
                    inHDF = new HDFile(infile, "r");
                }
                DataObject.clearAll();
                inHDF.seek(0);
                /*
                 * read file into set of DataObject's, set their internal
                 * variables
                 */
                monitor.setNote("Parsing objects");
                inHDF.readFile();
                monitor.setProgress(1);
                monitor.setNote("Getting histograms");

                //Set group
                if (mode == FileOpenMode.OPEN) {
                    Group.clearList();
                    Group.createGroup(infile.getName(), Group.Type.FILE);
                } else if (mode == FileOpenMode.OPEN_ADDITIONAL) {
                    Group.createGroup(infile.getName(), Group.Type.FILE);
                } else if (mode == FileOpenMode.RELOAD) {
                    final String sortName = JamStatus.getSingletonInstance()
                            .getSortName();
                    Group.setCurrentGroup(sortName);
                } else if (mode == FileOpenMode.ADD) {
                    //use current group
                }
                final int numHists = getHistograms(mode, histNames);
                message.append(numHists).append(" histograms");
                monitor.setProgress(2);
                monitor.setNote("Getting scalers");
                final int numScalers = getScalers(mode);
                message.append(", ").append(numScalers).append(" scalers");
                monitor.setProgress(3);
                if (mode != FileOpenMode.ADD) {
                    monitor.setNote("Getting gates");
                    final int numGates = getGates(mode);
                    /* clear if opening and there are histograms in file */
                    message.append(", ").append(numGates).append(" gates");

                    monitor.setProgress(4);
                    monitor.setNote("Getting parameters");
                    final int numParams = getParameters(mode);
                    message.append(", ").append(numParams)
                            .append(" parameters");
                    monitor.setProgress(5);
                }
                message.append(')');
                monitor.setNote("Done");
                inHDF.close();
                synchronized (this) {
                    /* destroys reference to HDFile (and its DataObject's) */
                    inHDF = null;
                }
                msgHandler.messageOutln(message.toString());
                setLastValidFile(infile);
            } catch (HDFException except) {
                msgHandler.errorOutln(except.toString());
                except.printStackTrace();
                rval = false;
            } catch (IOException except) {
                msgHandler.errorOutln(except.toString());
                except.printStackTrace();
                rval = false;
            }
            DataObject.clearAll();
            System.gc();
            monitor.close();
        }
        return rval;
    }
    
    /**
     * Read the histograms in.
     * 
     * @param infile
     *            to read from
     * @return list of attributes
     */
    public List readHistogramAttributes(File infile) {
        final List rval;
        if (HDFile.isHDFFile(infile)) {
            rval = new ArrayList();
            try {
                DataObject.clearAll();
                /* Read in histogram names */
                inHDF = new HDFile(infile, "r");
                inHDF.readFile();
                rval.addAll(loadHistogramAttributes());
                inHDF.close();
            } catch (HDFException except) {
                msgHandler.errorOutln(except.toString());
            } catch (IOException except) {
                msgHandler.errorOutln(except.toString());
            }
            DataObject.clearAll();
        } else {
            msgHandler.errorOutln(infile + " is not a valid HDF File!");
            rval = Collections.EMPTY_LIST;
        }
        return rval;
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
		final java.util.List groups = DataObject.ofType(DataObject.DFTAG_VG);
		final VirtualGroup hists = VirtualGroup.ofName(groups,
				JamHDFFields.HIST_SECTION_NAME);
		/* only the "histograms" VG (only one element) */
		ScientificData sdErr = null;
		if (hists != null) {
			/* get list of all DIL's in file */
			final java.util.List labels = DataObject.ofType(DataObject.DFTAG_DIL);
			/* get list of all DIA's in file */
			final java.util.List annotations = DataObject
					.ofType(DataObject.DFTAG_DIA);
			final Iterator temp = hists.getObjects().iterator();
			while (temp.hasNext()) {
				final VirtualGroup current = (VirtualGroup) (temp.next());
				final java.util.List tempVec = DataObject.ofType(current
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
				final ScientificData sd = (ScientificData) (DataObject.ofType(ndg
						.getObjects(), DataObject.DFTAG_SD).get(0));
				final ScientificDataDimension sdd = (ScientificDataDimension) (
						DataObject.ofType(ndg.getObjects(), DataObject.DFTAG_SDD).get(0));
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
					sdErr = (ScientificData) (DataObject.ofType(ndgErr
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
						dataArray = sd.getData1d(inHDF, sizeX);
					} else { //DOUBLE
						dataArray = sd.getData1dD(inHDF, sizeX);
					}
					/*if (ndgErr != null) {
						histogram.setErrors(sdErr.getData1dD(sizeX));
					}*/
				} else { //2d
					if (histNumType == NumberType.INT) {
						dataArray = sd.getData2d(inHDF, sizeX, sizeY);
					} else {
						dataArray = sd.getData2dD(inHDF, sizeX, sizeY);
					}
				}
				/* Add histogram */
				final HistogramAttributes histAtt = new HistogramAttributes();
				histAtt.name = name;
				histAtt.title = title;
				histAtt.number = number;
				histAtt.sizeX = sizeX;
				histAtt.sizeY = sizeY;
				histAtt.histDim = histDim;
				histAtt.histNumType = histNumType;
				histAtt.dataArray = dataArray;
				if (ndgErr != null){
					histAtt.errorArray=sdErr.getData1dD(inHDF, sizeX);
				}
				lstHistAtt.add(histAtt);
			}
		} //hist !=null
		return lstHistAtt;
	}

	/**
	 * Add default objects always needed.
	 * 
	 * Almost all of Jam's number storage needs are satisfied by the type
	 * hard-coded into the class <code>NumberType</code>.  This method
	 * creates the <code>NumberType</code> object in the file
	 * that gets referred to repeatedly by the other data elements.
	 * LibVersion Adds data element giving version of HDF libraries to use (4.1r2).
	 * @see jam.io.hdf.NumberType
	 */
	
	void addDefaultDataObjects(String fileID) throws HDFException {
		new LibVersion(); //DataObjects add themselves
		
		NumberType.createDefaultTypes();
		
		new JavaMachineType();
		new FileIdentifier(fileID);
		addFileNote();
		
	}			
	/**
	 * Add a text note to the file, which includes the state of 
	 * <code>JamProperties</code>.
	 *
	 * @throws IOException if there's a problem writing to the file
	 * @see jam.global.JamProperties
	 */
	void addFileNote() throws HDFException {
		final String noteAddition=
			"\n\nThe histograms when loaded into jam are displayed starting at channel zero up\n"
				+ "to dimension-1.  Two-dimensional data are properly displayed with increasing channel\n"
				+ "number from the lower left to the lower right for, and from the lower left to the upper\n"
				+ "left."
				+ "All error bars on histogram counts should be considered Poisson, unless a\n"
				+ "Numerical Data Group labelled 'Errors' is present, in which case the contents\n"
				+ "of that should be taken as the error bars.";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final String header="Jam Properties at time of save:";
		try {
			JamProperties.getProperties().store(baos,header);			
		} catch (IOException ioe) {
			throw new UndeclaredThrowableException(ioe,
			        "Unable to serialize properties.");
		}
		final String notation = baos.toString()+noteAddition;
		new FileDescription(notation);
	}
	
    /**
     * Adds data objects for the virtual group of histograms.
     * 
     * @see #addHistogram(Histogram)
     */
    protected void addHistogramSection()  throws HDFException {
        synchronized (this) {
            allHistogramsGroup = new VirtualGroup(HIST_SECTION_NAME,
                    FILE_SECTION_NAME);
        }
        new DataIDLabel(allHistogramsGroup, HIST_SECTION_NAME);
    }

    /**

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
        final List groups = DataObject.ofType(DataObject.DFTAG_VG);
        final VirtualGroup hists = VirtualGroup.ofName(groups,
                HIST_SECTION_NAME);
        /* only the "histograms" VG (only one element) */
        if (hists != null) {
        	//Message 
        	numHists =hists.getObjects().size();
        	
            /* get list of all DIL's in file */
            final List labels = DataObject.ofType(DataObject.DFTAG_DIL);
            /* get list of all DIA's in file */
            final List annotations = DataObject.ofType(DataObject.DFTAG_DIA);
            //Histogram iterator
            final Iterator temp = hists.getObjects().iterator();            
            while (temp.hasNext()) {
                final VirtualGroup current = (VirtualGroup) (temp.next());
                
            	NumericalDataGroup ndg = null;
                /* I check ndgErr==null to determine if error bars exist */
                NumericalDataGroup ndgErr = null;
                /* only the "histograms" VG (only one element) */
                ScientificData sdErr = null;        
            	
                final List tempVec = DataObject.ofType(current.getObjects(),
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
                final ScientificData sd = (ScientificData) (DataObject.ofType(ndg
                        .getObjects(), DataObject.DFTAG_SD).get(0));
                final ScientificDataDimension sdd = (ScientificDataDimension) (
                		DataObject.ofType(ndg.getObjects(), DataObject.DFTAG_SDD).get(0));
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
                    sdErr = (ScientificData) (DataObject.ofType(ndgErr.getObjects(),
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
                            .getData1d(inHDF, sizeX), name, title);
                } else { //DOUBLE
                    histogram = Histogram.createHistogram(sd
                            .getData1dD(inHDF, sizeX), name, title);
                }
                if (sdErr != null) {
                    ((AbstractHist1D) histogram).setErrors(sdErr
                            .getData1dD(inHDF, sizeX));
                }
            } else { //2d
                if (histNumType == NumberType.INT) {
                    histogram = Histogram.createHistogram(sd.getData2d(
                    		inHDF, sizeX, sizeY), name, title);
                } else {
                    histogram = Histogram.createHistogram(sd
                            .getData2dD(inHDF, sizeX, sizeY), name, title);
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
                        histogram.setCounts(sd.getData1d(inHDF, sizeX));
                    } else {
                        histogram.setCounts(sd.getData1dD(inHDF, sizeX));
                    }
                } else { // 2-d
                    if (histNumType == NumberType.INT) {
                        histogram.setCounts(sd.getData2d(inHDF, sizeX, sizeY));
                    } else {
                        histogram
                                .setCounts(sd.getData2dD(inHDF, sizeX, sizeY));
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
                        histogram.addCounts(sd.getData1d(inHDF, sizeX));
                    } else {
                        histogram.addCounts(sd.getData1dD(inHDF, sizeX));
                    }
                } else { // 2-d
                    if (histNumType == NumberType.INT) {
                        histogram.addCounts(sd.getData2d(inHDF, sizeX, sizeY));
                    } else {
                        histogram
                                .addCounts(sd.getData2dD(inHDF, sizeX, sizeY));
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
    protected void addGateSection()  throws HDFException {
        synchronized (this) {
            allGatesGroup = new VirtualGroup(GATE_SECTION_NAME,
                    FILE_SECTION_NAME);
        }
        new DataIDLabel(allGatesGroup, GATE_SECTION_NAME);
    }


    /*
     * non-javadoc: Retrieve the gates from the file.
     * 
     * @param mode whether to open or reload @throws HDFException thrown if
     * unrecoverable error occurs
     */
    private int getGates(FileOpenMode mode) throws HDFException {
    	int numGates=0;
        final StringUtilities su = StringUtilities.instance();
        Gate g = null;
        /* get list of all VG's in file */
        final List groups = DataObject.ofType(DataObject.DFTAG_VG);
        /* get only the "gates" VG (only one element) */
        final VirtualGroup gates = VirtualGroup.ofName(groups,
                GATE_SECTION_NAME);
        final List annotations = DataObject.ofType(DataObject.DFTAG_DIA);
        if (gates != null) {
        	numGates=gates.getObjects().size();
            final Iterator temp = gates.getObjects().iterator();
            while (temp.hasNext()) {
                final VirtualGroup currVG = (VirtualGroup) (temp.next());
                final VdataDescription VH = (VdataDescription) (DataObject.ofType(
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
    private int getScalers(FileOpenMode mode) throws HDFException {
    	int numScalers =0;
        final VdataDescription VH = VdataDescription.ofName(DataObject
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
    private int  getParameters(FileOpenMode mode) throws HDFException {
    	int numParameters =0;
        final VdataDescription VH = VdataDescription.ofName(DataObject
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
	 * @return the existing valid SDD type for the histogram, 
	 * creating a new one if necessary.
	 * @param h that type is needed for
	 */
	ScientificDataDimension getSDD(Histogram h) throws HDFException {
		byte type=NumberType.DOUBLE;
		if (h.getType().isInteger()) {
			type = NumberType.INT;
		}
		return getSDD(h,type);
	}
	
	/**
	 * Returns the existing valid SDD type for the histogram, 
	 * creating a new one if necessary.  DOUBLE type
	 * is explicitly requested, for error bars.
	 *
	 * @param h which type is needed for
	 * @param numtype the number HDF uses to indicate the type
	 * @return the SDD object representing the histogram size and number 
	 * type
	 */
	ScientificDataDimension getSDD(Histogram h, byte numberType) throws HDFException {
		ScientificDataDimension rval=null;//return value
		final int rank = h.getDimensionality();
		final int sizeX = h.getSizeX();
		int sizeY=0;
		if (rank == 2) {//otherwise rank == 1
			sizeY = h.getSizeY();
		} 
		return ScientificDataDimension.getSDD(rank, sizeX, sizeY, numberType);
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

    
    private void setProgressNote(final ProgressMonitor monitor, final String note) {
        final Runnable runner = new Runnable() {
            public void run() {
                monitor.setNote(note);
            }
        };
        try {
        	SwingUtilities.invokeAndWait(runner);
        } catch(Exception e) {
        	e.printStackTrace();
        }
    }

    private void setProgress(final ProgressMonitor monitor, final int value) {
        final Runnable runner = new Runnable() {
            public void run() {
                monitor.setProgress(value);
            }
        };
        try {
        	SwingUtilities.invokeAndWait(runner);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}

    }

    private void outln(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                msgHandler.messageOutln(msg);
            }
        });
    }

    private void outerr(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                msgHandler.errorOutln(msg);
            }
        });
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