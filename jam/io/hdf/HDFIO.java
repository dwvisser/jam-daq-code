package jam.io.hdf;
import java.io.*;
import java.util.*;
import java.awt.*;
import jam.global.MessageHandler;
import jam.global.JamProperties;
import jam.util.FileUtilities;
import jam.util.StringUtilities;
import jam.data.*;
import jam.io.DataIO;
import javax.swing.*;

/**
 * Reads and writes HDF files containing spectra, scalers, gates, and 
 * additional useful information.
 *
 * @version  0.5 November 98
 * @author   Dale Visser
 * @since       JDK1.1
 */
public class HDFIO implements DataIO,JamHDFFields {

    private String defaultPath;

    /**
     * last file successfully read from or written to
     *
     * see #readFile
     */
    private File lastValidFile;

    /**
     * File to save HDF information to.
     */
    private File fileSave;

    /**
     * Parent frame.
     */
    private Frame frame;

    /**
     * Where messages get sent (presumably the console).
     */
    private MessageHandler msgHandler;

    private VirtualGroup histGroup,gateGroup;

    /**
     * Utilty class to handle file dialogs.
     */
    private FileUtilities fu;

    /**
     * <code>HDFile</code> object to write out to.
     */
    private HDFile out;

    /**
     * <code>HDFile<code> object to read from.
     */
    private HDFile in;

    /**
     * File name that was read from.  Gets set when reading.
     *
     * @see getFileNameOpen
     */
    private String fileNameOpen;

    /**
     * Class constructor handed references to the main class and message handler.
     */
    public HDFIO(Frame frame,  MessageHandler msgHandler){
        this.frame=frame;
        this.msgHandler=msgHandler;
        defaultPath=JamProperties.getPropString(JamProperties.HIST_PATH);
        fu=new FileUtilities(frame,defaultPath);
    }

    /**
     * Constructor for file read access outside jam.  No frame or message handler given.
     */
    public HDFIO(File file) throws HDFException, IOException {
        in = new HDFile(file, "r");
        in.seek(0);
        in.readObjects();//reads file into set of DataObject's, sets their internal variables
    }

    /**
     * Writes out to a specified file all the currently held spectra, gates, scalers,
     * and parameters.
     */
    public void writeFile(File file) {
        writeFile(true, true, true, true, file);
    }

    /**
     * Writes out the currently held spectra, gates, and scalers.
     */
    public void writeFile()  {
        writeFile(true, true, true, true);
    }

    /**
     * Writes out the currently held spectra, gates, and scalers, subject to the options given.
     * (Parameters are always written.)
     *
     * @param  wrthis  if true, Histograms will be written
     * @param  wrtgate  if true, Gates will be written
     * @param  wrtscalers  if true, scaler values will be written
     */
    public void writeFile(boolean wrthis, boolean wrtgate, boolean wrtscalers)   {
        writeFile( wrthis,  wrtgate,  wrtscalers, true);
    }

    /**
     * Writes out the currently held spectra, gates, and scalers, subject to the options given
     *.
     * @param  wrthis  if true, Histograms will be written
     * @param  wrtgate  if true, Gates will be written
     * @param  wrtscalers  if true, scaler values will be written
     * @param  wrtparameters if true, parameter values will be written
     */
    public void writeFile(boolean wrthis, boolean wrtgate, boolean wrtscalers,
    boolean wrtparameters)   {
        JFileChooser jfile = new JFileChooser(fileSave);
        jfile.setFileFilter(new HDFileFilter(true));
        int option=jfile.showSaveDialog(frame);
        // dont do anything if it was cancel
        if (option == JFileChooser.APPROVE_OPTION && jfile.getSelectedFile() != null) {
            fileSave = jfile.getSelectedFile();
            writeFile(wrthis, wrtgate, wrtscalers, fileSave);
        }
    }

    /**
     * Writes out (to a specific file) the currently held spectra, gates, and scalers, subject to the
     * options given.
     * Sets separately which data writeFile should actually output.  Not writing histograms
     * when you are saving tape data can significantly save time when you have many 2-d spectra.
     * parameters always written
     * @param  wrthis  if true, Histograms will be written
     * @param  wrtgate  if true, Gates will be written
     * @param  wrtscalers  if true, scaler values will be written
     */
    public void writeFile(boolean wrthis, boolean wrtgate, boolean wrtscalers, File file) {
        writeFile( wrthis,  wrtgate,  wrtscalers, true, file);
    }

    /**
     * Writes out (to a specific file) the currently held spectra, gates, and scalers, subject to the
     * options given.
     * Sets separately which data writeFile should actually output.  Not writing histograms
     * when you are saving tape data can significantly save time when you have many 2-d spectra.
     *
     * @param  wrthis  if true, Histograms will be written
     * @param  wrtgate  if true, Gates will be written
     * @param  wrtscalers  if true, scaler values will be written
     * @param  wrtparameters if true, parameter values will be written
     */
    public void writeFile(boolean wrthis, boolean wrtgate, boolean wrtscalers, boolean wrtparameters, File file) {
        java.util.List hist, gate, scaler, parameter;
        
        final boolean writeHistograms=wrthis;
        final boolean writeGates=wrtgate;
        final boolean writeScalers=wrtscalers;
        final boolean writeParameters=wrtparameters;
        if (writeHistograms){
            hist = Histogram.getHistogramList();
        } else {
            hist=new Vector(0);
        }
        if (writeGates) {
            gate = Gate.getGateList();
            /* only save those gates which are defined */
            Vector temp = new Vector(gate.size());
            for (Iterator enum = gate.iterator() ; enum.hasNext() ;) {
                Gate g=(Gate)(enum.next());
                if (g.isDefined()){
                    temp.addElement(g);
                }
            }
            temp.trimToSize();
            gate=temp;
        } else {
            gate=new Vector(0);
        }
        if (writeScalers) {
            scaler = Scaler.getScalerList();
        } else {
            scaler = new Vector(0);
        }
        if (writeParameters) {
            parameter = DataParameter.getParameterList();
        } else {
            parameter = new Vector(0);
        }
        writeFile(file, hist, gate, scaler, parameter);
    }

    /**
     * Given separate vectors of the writeable objects, constructs and writes out an HDF file
     * containing the contents.  Null or empty
     * <code>Vector</code> arguments are skipped.
     *
     * @param  file  disk file to write to
     * @param  spectra  specified list of <code>Histogram</code> objects to write
     * @param  gates  specified list of <code>Gate</code> objects to write
     * @param  scalers  specified list of <code>Scaler</code> objects to write
     * @param  parameters specified list of <code>Parameter</code> objects to write
     */
    private void writeFile(File file, java.util.List spectra, java.util.List gates, java.util.List scalers, java.util.List parameters) {
        try {
            out = new HDFile(file,"rw");
            msgHandler.messageOut("Save "+file.getName()+": ",MessageHandler.NEW);
            out.addFileID(file.getPath());
            out.addFileNote();
            out.addMachineType();
            out.addNumberTypes();

        } catch (HDFException e) {
            msgHandler.errorOutln("Exception when opening file '"+file.getName()+"': "+e.toString());
        } catch (IOException e) {
            msgHandler.errorOutln("Exception when opening file '"+file.getName()+"': "+e.toString());
        }

        try{
            if (hasContents(spectra)){
                addHistogramSection();
                msgHandler.messageOut(spectra.size()+" histograms, ",MessageHandler.CONTINUE);
                for (Iterator temp = spectra.iterator() ; temp.hasNext() ;) {
                    addHistogram((Histogram)(temp.next()));
                    //msgHandler.messageOut(" . ",MessageHandler.CONTINUE);
                }
            }
            if (hasContents(gates)){
                addGateSection();
                msgHandler.messageOut(gates.size()+" gates, ",MessageHandler.CONTINUE);
                for (Iterator temp = gates.iterator() ; temp.hasNext() ;) {
                    addGate((Gate)(temp.next()));
                    //msgHandler.messageOut(" . ",MessageHandler.CONTINUE);
                }
            }
            if (hasContents(scalers)){
                addScalerSection(scalers);
                msgHandler.messageOut(scalers.size()+" scalers, ",MessageHandler.CONTINUE);
            }
            if (hasContents(parameters)){
                addParameterSection(parameters);
                msgHandler.messageOut(parameters.size()+" parameters ",MessageHandler.CONTINUE);
            }
            out.setOffsets();
            out.writeDataDescriptorBlock();
            out.writeAllObjects(msgHandler);
            out.close();
        } catch (Exception e) {
            msgHandler.messageOut("",MessageHandler.END);
            msgHandler.errorOutln("Exception writing to file '"+file.getName()+"': "+e.toString());
        }
        out = null; //allows Garbage collector to free up memory
        System.gc();
        msgHandler.messageOut("done!",MessageHandler.END);
        lastValidFile = file;
    }

    /**
     * Read in an unspecified file by opening up a dialog box.
     *
     * @param  mode  whether to open or reload
     * @return  <code>true</code> if successful, <code>false</code> if not
     */
    public boolean readFile(int mode) {
        boolean outF=false;
        final JFileChooser jfile = new JFileChooser(fileSave);
        jfile.setFileFilter(new HDFileFilter(true));
        final int option=jfile.showOpenDialog(frame);
        // dont do anything if it was cancel
        if (option == JFileChooser.APPROVE_OPTION && jfile.getSelectedFile() != null) {
            fileSave = jfile.getSelectedFile();
            outF = readFile(mode, fileSave);
        } else {//dialog didn't return a file
            outF = false;
        }
        return outF;
    }

    /**
     * Read in an HDF file.
     *
     * @param  infile  file to load
     * @param  mode  whether to open or reload
     * @return  <code>true</code> if successful, <code>false</code> if not
     */
    public boolean readFile(int mode, File infile) {
        boolean outF = true;
        try {
            fileNameOpen=infile.getName();
            if (mode==OPEN){
                msgHandler.messageOut("Open "+fileNameOpen+": ",MessageHandler.NEW);
            } else {//reload
                msgHandler.messageOut("Reload "+fileNameOpen+": ",MessageHandler.NEW);
            }
            in = new HDFile(infile, "r");
            in.seek(0);
            in.readObjects();//reads file into set of DataObject's, sets their internal variables
            getHistograms(mode);
            getScalers(mode);
            getGates(mode);
            getParameters(mode);
            in.close();
            in = null;  // destroys reference to HDFile (and its DataObject's
            //  allowing Garbage Collector to free up memory
            System.gc();
            msgHandler.messageOut("done!",MessageHandler.END);
            lastValidFile = infile;
        } catch (HDFException except) {
            msgHandler.messageOut("",MessageHandler.END);
            msgHandler.errorOutln(except.toString());
            outF = false;
        }  catch (IOException except) {
            msgHandler.messageOut("",MessageHandler.END);
            msgHandler.errorOutln(except.toString());
            outF = false;
        }
        return outF;
    }


    /**
     * Adds data objects for the virtual group of histograms.
     *
     * @see #addHistogram(Histogram)
     */
    protected void addHistogramSection(){
        histGroup = new VirtualGroup(out, HIST_SECTION_NAME,FILE_SECTION_NAME);
        new DataIDLabel(histGroup,HIST_SECTION_NAME);
    }

    /**
     * Add the virtual group representing a single histogram, and link it into
     * the virtual group representing all histograms.
     *
     * @param  h   the histogram to add
     * @see #addHistogramSection()
     * @exception   HDFException  thrown if unrecoverable error occurs
     */
    protected void addHistogram(Histogram h) throws HDFException {
        ScientificDataDimension sddErr;
        NumericalDataGroup ndgErr;
        ScientificData sd,sdErr;

        try {
            final VirtualGroup temp = new VirtualGroup(out, h.getName(),HIST_TYPE_NAME);
            histGroup.addDataObject(temp); //add to Histogram section vGroup
            new DataIDLabel(temp,h.getName());  // vGroup label is Histogram name
            new DataIDAnnotation(temp,h.getTitle()); // vGroup Annotation is Histogram title
            final NumericalDataGroup ndg = new NumericalDataGroup(out);// NDG to contain data
            new DataIDLabel(ndg,Integer.toString(h.getNumber()));//make the NDG label the histogram number
            temp.addDataObject(ndg); //add to specific histogram vGroup (other info maybe later)
            final ScientificDataDimension sdd = out.getSDD(h);
            ndg.addDataObject(sdd);//use new SDD
            temp.addDataObject(sdd);//use new SDD
            switch (h.getType()) {
                case Histogram.ONE_DIM_INT:      sd = new ScientificData(out, (int [])h.getCounts());
                //sdl = new ScientificDataLabel(out,h.getLabelX()+"\n"+h.getLabelY());
                if (h.errorsSet()) {
                    ndgErr=new NumericalDataGroup(out);
                    new DataIDLabel(ndgErr,ERROR_LABEL);
                    temp.addDataObject(ndgErr);
                    sddErr=out.getSDD(h,NumberType.DOUBLE);//explicitly floating point
                    ndgErr.addDataObject(sddErr);
                    temp.addDataObject(sddErr);
                    sdErr = new ScientificData(out,h.getErrors());
                    ndgErr.addDataObject(sdErr);
                    temp.addDataObject(sdErr);
                }
                break;
                case Histogram.ONE_DIM_DOUBLE:  sd = new ScientificData(out,(double [])h.getCounts());
                //sdl = new ScientificDataLabel(out,h.getLabelX()+"\n"+h.getLabelY());
                if (h.errorsSet()) {
                    ndgErr=new NumericalDataGroup(out);
                    new DataIDLabel(ndgErr,ERROR_LABEL);
                    temp.addDataObject(ndgErr);
                    sddErr=sdd;//explicitly floating point
                    ndgErr.addDataObject(sddErr);
                    //temp.addDataObject(sddErr); no need to add, already added
                    sdErr = new ScientificData(out,h.getErrors());
                    ndgErr.addDataObject(sdErr);
                    temp.addDataObject(sdErr);
                }
                break;
                case Histogram.TWO_DIM_INT:      sd = new ScientificData(out, (int [][])h.getCounts());
                //sdl = new ScientificDataLabel(out,h.getLabelX()+"\0"+h.getLabelY());
                break;
                case Histogram.TWO_DIM_DOUBLE:  sd = new ScientificData(out, (double [][])h.getCounts());
                //sdl = new ScientificDataLabel(out,h.getLabelX()+"\0"+h.getLabelY());
                break;
                default :          throw new DataException("HDFIO encountered a Histogram of unknown type.");
            }
            ndg.addDataObject(sd);
            temp.addDataObject(sd);
        } catch (DataException e) {
            throw new HDFException("Problem adding Histogram '"+h.getName()+"' :"+e.getMessage());
        }
    }

    /**
     * looks for the special Histogram section and reads the data into memory.
     *
     * @param  mode  whether to open or reload
     * @exception   HDFException  thrown if unrecoverable error occurs
     */
    protected void getHistograms(int mode) throws HDFException{
        NumericalDataGroup ndg=null;
        NumericalDataGroup ndgErr=null;//I check for null to determine if error bars exist
        Histogram histogram;
        try{
        	/* get list of all VG's in file */
            final java.util.List groups=in.ofType(DataObject.DFTAG_VG);
            final VirtualGroup hists=
            VirtualGroup.ofName(groups,HIST_SECTION_NAME);
            /* only the "histograms" VG (only one element) */
            ScientificData sdErr=null;
            if (hists != null){
            	/* clear if opening and there are histograms in file */
                if (mode==OPEN) {
                	DataBase.clearAllLists();
                }
                /* get list of all DIL's in file */
                final java.util.List labels=in.ofType(DataObject.DFTAG_DIL);
                /* get list of all DIA's in file */
                final java.util.List annotations=
                in.ofType(DataObject.DFTAG_DIA);
                msgHandler.messageOut(hists.getObjects().size()+" histograms",
                MessageHandler.CONTINUE);
                final Iterator temp = hists.getObjects().iterator() ;
                while ( temp.hasNext()) {
                    final VirtualGroup current=(VirtualGroup)(temp.next());
                    final java.util.List tempVec = in.ofType(
                    current.getObjects(), DataObject.DFTAG_NDG);
                    final NumericalDataGroup [] numbers = 
                    new NumericalDataGroup[tempVec.size()];
                    tempVec.toArray(numbers);
                    //System.out.println("numbers has "+numbers.length+" elements");
                    if (numbers.length == 1) {
                        ndg=numbers[0]; //only one NDG -- the data
                    } else if (numbers.length == 2) {
                        if (DataIDLabel.withTagRef(labels,DataObject.DFTAG_NDG,
                        numbers[0].getRef()).getLabel().equals(ERROR_LABEL)){
                            ndg=numbers[1];
                            ndgErr=numbers[0];
                        } else {
                            ndg=numbers[0];
                            ndgErr=numbers[1];
                        }
                    } else {
                        throw new HDFException(
                        "Invalid number of data groups ("+numbers.length+
                        ") in NDG.");
                    }
                    final ScientificData sd=(ScientificData)(in.ofType(ndg.getObjects(),
                    DataObject.DFTAG_SD).get(0));
                    final ScientificDataDimension sdd=(ScientificDataDimension)(in.ofType(ndg.getObjects(),
                    DataObject.DFTAG_SDD).get(0));
                    final DataIDLabel numLabel=DataIDLabel.withTagRef(labels,
                    ndg.getTag(),ndg.getRef());
                    final int number=Integer.parseInt(numLabel.getLabel());
                    final byte histNumType=sdd.getType();
                    sd.setNumberType(histNumType);
                    final int histDim=sdd.getRank();
                    sd.setRank(histDim);
                    final int sizeX=sdd.getSizeX();
                    int sizeY=0;
                    if (histDim==2) {
                        sizeY=sdd.getSizeY();
                    } 
                    final DataIDLabel templabel=DataIDLabel.withTagRef(labels,current.getTag(),
                    current.getRef());
                    final DataIDAnnotation tempnote=DataIDAnnotation.withTagRef(annotations,
                    current.getTag(),current.getRef());
                    final String name=templabel.getLabel();
                    final String title=tempnote.getNote();
                    if (ndgErr != null){
                        sdErr = (ScientificData)(in.ofType(ndgErr.getObjects(),
                        DataObject.DFTAG_SD).get(0));
                        sdErr.setRank(histDim);
                        sdErr.setNumberType(NumberType.DOUBLE);
                        /*final ScientificDataDimension sddErr =(ScientificDataDimension)(in.ofType(
                        ndgErr.getObjects(),DataObject.DFTAG_SDD).get(0));*/
                    }
                    if (mode==OPEN){
                        if (histDim==1) {
                            if (histNumType==NumberType.INT) {
                                histogram = new Histogram(name,title,
                                sd.getData1d(sizeX));
                            } else {//DOUBLE
                                histogram = new Histogram(name,title,
                                sd.getData1dD(sizeX));
                            }
                            if (ndgErr != null) {
                                histogram.setErrors(sdErr.getData1dD(sizeX));
                            }
                        } else {//2d
                            //System.out.println(" x "+sizeY+" channels");
                            if (histNumType==NumberType.INT) {
                                histogram = new Histogram(name,title,
                                sd.getData2d(sizeX,sizeY));
                            } else {
                                histogram = new Histogram(name,title,
                                sd.getData2dD(sizeX,sizeY));
                            }
                        }
                        histogram.setNumber(number);
                    } else {//RELOAD
                        histogram = Histogram.getHistogram(
                        StringUtilities.makeLength(name,Histogram.NAME_LENGTH));
                        if (histogram != null) {
                            if (histDim==1) {
                                if (histNumType==NumberType.INT) {
                                    histogram.setCounts(sd.getData1d(sizeX));
                                } else {
                                    histogram.setCounts(sd.getData1dD(sizeX));
                                }
                            } else { // 2-d
                                if (histNumType==NumberType.INT) {
                                    histogram.setCounts(
                                    sd.getData2d(sizeX,sizeY));
                                } else {
                                    histogram.setCounts(
                                    sd.getData2dD(sizeX,sizeY));
                                }
                            }
                        } else {//not in memory
                            msgHandler.messageOut("X",MessageHandler.CONTINUE);
                        }
                    }
                    msgHandler.messageOut(". ",MessageHandler.CONTINUE);
                }
            } 
        } catch (DataException e) {
            throw new HDFException ("Problem getting Histograms: "+
            e.getMessage());
        }
    }

    /**
     * Adds data objects for the virtual group of gates.
     *
     * @see #addGate(Gate)
     */
    protected void addGateSection(){
        gateGroup = new VirtualGroup(out,GATE_SECTION_NAME,FILE_SECTION_NAME);
        new DataIDLabel(gateGroup,GATE_SECTION_NAME);
    }

    /**
     * Add the virtual group representing a single gate, and link it 
     * into the virtual group representing all gates.
     *
     * @param  g   the gate to add
     * @see #addGateSection()
     * @exception   HDFException thrown if unrecoverable error occurs
     */
    protected void addGate(Gate g) throws HDFException{
        String gateType = GATE_1D_TYPE_NAME;
        int size = 1;

        try{
            String [] names = GATE_2D_NAMES;
            int []x=new int [0];
            int []y=new int [0];
        	final short [] types = {VdataDescription.DFNT_INT32 , 
        	VdataDescription.DFNT_INT32};
        	final short [] orders = {1,1};
            final String name = g.getName();
            if (g.getType()==Gate.ONE_DIMENSION){
                names = GATE_1D_NAMES;
            } else {//2d
                gateType = GATE_2D_TYPE_NAME;
                size = g.getBananaGate().npoints;
                x=g.getBananaGate().xpoints;
                y=g.getBananaGate().ypoints;
            }
            /* get the VG for the current gate */
            final VirtualGroup vg = new VirtualGroup(out,name,gateType);
            gateGroup.addDataObject(vg);//add to Gate section vGroup
            final VdataDescription desc = new VdataDescription(out,name,
            gateType,size,names,types,orders);
            final Vdata data = new Vdata(out,desc);
            vg.addDataObject(desc); //add vData description to gate VG
            vg.addDataObject(data); //add vData to gate VG
            if (g.getType()==1) {
                data.addInteger(0,0,g.getLimits1d()[0]);
                data.addInteger(1,0,g.getLimits1d()[1]);
            } else {//2d
                for (int i=0;i<size;i++) {
                    data.addInteger(0,i,x[i]);
                    data.addInteger(1,i,y[i]);
                }
            }
            data.refreshBytes();
            /* add Histogram links... */
            final VirtualGroup hist=VirtualGroup.ofName(
            out.ofType(DataObject.DFTAG_VG),g.getHistogram().getName());
            /* add name as note to vg */
            new DataIDAnnotation(vg,g.getHistogram().getName());
            hist.addDataObject(vg);//reference the Histogram in the gate group
        } catch (DataException e) {
            throw new HDFException ("Problem adding Gate: "+e.getMessage());
        }
    }

    /**
     * Retrieve the gates from the file.
     *
     * @param  mode  whether to open or reload
     * @throws HDFException thrown if unrecoverable error occurs
     */
    private void getGates(int mode) throws HDFException{
        Gate g=null;
        try {
        	/* get list of all VG's in file */
            final java.util.List groups = in.ofType(DataObject.DFTAG_VG);
            /* get only the "gates" VG (only one element) */
            final VirtualGroup gates = VirtualGroup.ofName(groups,
            GATE_SECTION_NAME);
            final java.util.List annotations = in.ofType(DataObject.DFTAG_DIA);
            if (gates != null){
            	/* clear if opening and there are histograms in file */
                if (mode==OPEN) {
                	Gate.clearList();
                }
                msgHandler.messageOut(gates.getObjects().size()+" gates",
                MessageHandler.CONTINUE);
                final Iterator temp = gates.getObjects().iterator() ;
                while ( temp.hasNext()){
                    final VirtualGroup currVG = (VirtualGroup)(temp.next());
                    final VdataDescription VH=(VdataDescription)(in.ofType(
                    currVG.getObjects(),DataObject.DFTAG_VH).get(0));
                    if (VH != null) {
                        final Vdata VS=(Vdata)(in.getObject(DataObject.DFTAG_VS,
                        VH.getRef()));//corresponding VS
                        final int numRows = VH.getNumRows();
                        final String gname = currVG.getName();
                        final String hname = DataIDAnnotation.withTagRef(annotations,
                        currVG.getTag(),currVG.getRef()).getNote();
                        if (mode==OPEN){
                            g = new Gate(gname,Histogram.getHistogram(
                            StringUtilities.makeLength(hname,Histogram.NAME_LENGTH)));
                        } else {//reload
                            g = Gate.getGate(StringUtilities.makeLength(gname,
                            Gate.NAME_LENGTH));
                        }
                        if (g!=null) {                  
                        	if (g.getType()==Gate.ONE_DIMENSION){//1-d gate
                            	g.setLimits(VS.getInteger(0,0).intValue(),
                            	VS.getInteger(0,1).intValue());
                        	} else {//2-d gate
                            	final Polygon pg = new Polygon();
                            	for (int i=0;i<numRows;i++){
                                	pg.addPoint(VS.getInteger(i,0).intValue(),
                                	VS.getInteger(i,1).intValue());
                            	}
                            	g.setLimits(pg);
                        	}
                        }
                    } else {
                        msgHandler.messageOutln(
                        "Problem processing a VH in HDFIO!");
                    }
                    if (g==null) {
                        msgHandler.messageOut("X ",MessageHandler.CONTINUE);
                    } else {//got a gate
                        msgHandler.messageOut(". ",MessageHandler.CONTINUE);
                    }
                }
            }
        } catch (DataException e) {
            throw new HDFException("Problem getting gates: "+e.getMessage());
        }
    }

    /**
     * Adds data objects for the virtual group of scalers.
     *
     * @exception HDFException thrown if unrecoverable error occurs
     */
    protected void addScalerSection(java.util.List scalers) throws HDFException{
    	final short [] types = {VdataDescription.DFNT_INT32,
    	VdataDescription.DFNT_CHAR8, VdataDescription.DFNT_INT32};
        final short [] orders = new short[3];
        final int size=scalers.size();
        orders[0]=1;//number
        orders[1]=0;//name ... loop below picks longest name for dimension
        final Iterator enum = scalers.iterator(); 
        while (enum.hasNext()) {
            final int dimtest=((Scaler)(enum.next())).getName().length();
            if (dimtest > orders[1]) {
                orders[1]=(short)dimtest;
            }
        }
        orders[2]=1;//value
        final VirtualGroup scalerGroup = new VirtualGroup(out,SCALER_SECTION_NAME,
        FILE_SECTION_NAME);
        new DataIDLabel(scalerGroup,SCALER_SECTION_NAME);
        final String name = SCALER_SECTION_NAME;
        final String scalerType = SCALER_TYPE_NAME;
        final String [] names = SCALER_COLUMN_NAMES;
        final VdataDescription desc = new VdataDescription(out,name,scalerType,size,names,types,
        orders);
        final Vdata data = new Vdata(out,desc);
        scalerGroup.addDataObject(desc); //add vData description to gate VG
        scalerGroup.addDataObject(data); //add vData to gate VG

        for (int i=0; i < size ; i++) {
            final Scaler s=(Scaler)(scalers.get(i));
            data.addInteger(0,i,s.getNumber());
            data.addChars(1,i,StringUtilities.makeLength(s.getName(),orders[1]));
            data.addInteger(2,i,s.getValue());
        }
        data.refreshBytes();
    }

    /**
     * Retrieve the scalers from the file.
     *
     * @param  mode  whether to open or reload
     * @throws HDFException if there is a problem retrieving scalers
     */
    private void getScalers(int mode) throws HDFException {
        final VdataDescription VH=VdataDescription.ofName(
        in.ofType(DataObject.DFTAG_VH),SCALER_SECTION_NAME);
        /* only the "scalers" VH (only one element) in the file */
        try {
            if (VH != null) {
                if (mode==OPEN) {
                	Scaler.clearList();
                }
                /* get the VS corresponding to the given VH */
                final Vdata VS=(Vdata)(in.getObject(DataObject.DFTAG_VS,
                VH.getRef()));
                final int numScalers=VH.getNumRows();
                msgHandler.messageOut(numScalers+" scalers",
                MessageHandler.CONTINUE);
                for (int i=0;i<numScalers;i++){
                	final Scaler s;
                    final String sname = VS.getString(i,1);
                    if (mode==OPEN) {
                        s = new Scaler(sname,VS.getInteger(i,0).intValue());
                    } else {//mode==RELOAD
                        s = Scaler.getScaler(sname);
                    }
                    if (s != null){
                        s.setValue(VS.getInteger(i,2).intValue());
                        msgHandler.messageOut(". ",MessageHandler.CONTINUE);
                    } else {//not found
                        msgHandler.messageOut("X",MessageHandler.CONTINUE);
                    }

                }
            } else {
                msgHandler.messageOut("(no scalers)",MessageHandler.CONTINUE);
            }
        } catch (DataException e) {
            throw new HDFException("Problem creating scalers: "+e.getMessage());
        }

    }
    /**
     * Adds data objects for the virtual group of parameters.
     *
     * @param parameters the parameters to write?
     * @exception   HDFException      thrown if unrecoverable error 
     * occurs
     */
    protected void addParameterSection(java.util.List parameters) 
    throws HDFException{
    	final short [] types = {VdataDescription.DFNT_CHAR8, 
    	VdataDescription.DFNT_FLT32};
        final short [] orders = new short[2];
        final int size=parameters.size();
        /* set order values */
        orders[0]=0;//name ... loop below picks longest name for dimension
        final Iterator enum = parameters.iterator();
        while (enum.hasNext()) {
            final int lenMax=((DataParameter)(enum.next())).getName().length();
            if (lenMax > orders[0]) {
                orders[0]=(short)lenMax;
            }
        }
        orders[1]=1;//value

        final VirtualGroup parameterGroup = new VirtualGroup(out, 
        PARAMETER_SECTION_NAME, FILE_SECTION_NAME);
        new DataIDLabel(parameterGroup, PARAMETER_SECTION_NAME);
        final String name = PARAMETER_SECTION_NAME;
        final String parameterType = PARAMETER_TYPE_NAME;
        final String [] names = PARAMETER_COLUMN_NAMES;
        final VdataDescription desc = new VdataDescription(out, name, 
        parameterType, size, names, types, orders);
        final Vdata data = new Vdata(out,desc);
        parameterGroup.addDataObject(desc); //add vData description to gate VG
        parameterGroup.addDataObject(data); //add vData to gate VG

        for (int i=0; i < size ; i++) {
            final DataParameter p=(DataParameter)(parameters.get(i));
            data.addChars(0,i,StringUtilities.makeLength(p.getName(),
            orders[0]));
            data.addFloat(1,i,(float)p.getValue());
        }
        data.refreshBytes();
    }

    /**
     * retrieve the parameters from the file
     *
     * @param  mode  whether to open or reload
     * @throws HDFException if an error occurs reading the parameters
     */
    private void getParameters(int mode) throws HDFException{
        DataParameter p;
 
        final VdataDescription VH=VdataDescription.ofName(
        in.ofType(DataObject.DFTAG_VH), PARAMETER_SECTION_NAME);
        /* only the "parameters" VH (only one element) in the file */
        try{
            if (VH != null) {
                if (mode==OPEN) {
                	DataParameter.clearList();
                }
                /* Get corresponding VS for this VH */
                final Vdata VS=(Vdata)(in.getObject(
                DataObject.DFTAG_VS,VH.getRef()));
                final int numParameters=VH.getNumRows();
                msgHandler.messageOut(" "+numParameters+" parameters ",
                MessageHandler.CONTINUE);
                for (int i=0;i<numParameters;i++){
                    final String pname = VS.getString(i,0);
                    if (mode==OPEN) {
                        p = new DataParameter(pname);
                    } else {//mode==RELOAD
                        p = DataParameter.getParameter(pname);
                    }
                    if (p != null){
                        p.setValue(VS.getFloat(i,1).floatValue());
                        msgHandler.messageOut(". ",MessageHandler.CONTINUE);
                    } else {//not found
                        msgHandler.messageOut("X",MessageHandler.CONTINUE);
                    }
                }
            } else {
				msgHandler.messageOut("(no parameters)",MessageHandler.CONTINUE);
            }
        } catch (DataException e) {
            throw new HDFException ("Problem creating Parameters: "+
            e.getMessage());
        }
    }

    /**
     * Determines whether a <code>List</code> passed to it 
     * <ol><li>exists, and </li><li></ol>
     * has any elements.
     *
     * @param  v  the list to check
	 * @return true if the given list exists and has at least one element
     */
    protected boolean hasContents(java.util.List v){
        final boolean val=(v!=null)&&(!v.isEmpty());
        return val;
    }

    /**
     * @return the name of the file opened
     */
    public String getFileNameOpen(){
        return fileNameOpen;
    }

    /**
     * @return last file successfully read from or written to.
     */
    public File lastValidFile(){
        return lastValidFile;
    }
    
    /**
     * @return the file data is to be read from
     */
    public File getInputFile(){
        return in.getFile();
    }

	/**
	 * @param spectrumName the name of the histogram
	 * @return the integer array corresponding to the histogram data
	 * @throws HDFException if the file doesn't have the requested 
	 * spectrum
	 */
    public int [] readIntegerSpectrum(String spectrumName) throws HDFException{
        NumericalDataGroup ndg;
        
        int [] rval=null;//default return value
        /* all VG's in file */
        final java.util.List groups=in.ofType(DataObject.DFTAG_VG);
        final VirtualGroup hists=VirtualGroup.ofName(groups,HIST_SECTION_NAME);
        /* only the "histograms" VG (only one element) */
        if (hists == null) {
            throw new HDFException("No Histogram section in file: "+
            in.getFile());
        } else{
            /* there are histograms in the file */
            final java.util.List labels=in.ofType(DataObject.DFTAG_DIL);
            final Iterator temp = hists.getObjects().iterator();
            lookForSpectrum : while (temp.hasNext()) {
                final VirtualGroup current=(VirtualGroup)(temp.next());
                /* NDG's in current hist record */
                final java.util.List tempVec = in.ofType(
                current.getObjects(),DataObject.DFTAG_NDG);
                final NumericalDataGroup [] numbers = 
                new NumericalDataGroup[tempVec.size()]; 
                tempVec.toArray(numbers);
                final String name = DataIDLabel.withTagRef(labels,
                current.getTag(), current.getRef()).getLabel();
                if (name.trim().equals(spectrumName)) {
                    if (numbers.length == 1) {//only one NDG -- the data
                        ndg=numbers[0];
                    } else if (numbers.length == 2) {
                    	/* determine which of the two contains error bars
                    	 * and ignore it, assigning the other to ndg */
                        if (DataIDLabel.withTagRef(labels,
                        DataObject.DFTAG_NDG,numbers[0].getRef()).getLabel().equals(ERROR_LABEL)){
                            ndg=numbers[1];
                        } else {
                            ndg=numbers[0];
                        }
                    } else {
                        throw new HDFException(
                        "Invalid number of data groups ("+numbers.length+
                        ") in NDG.");
                    }
                    final ScientificData sd=(ScientificData)(in.ofType(
                    ndg.getObjects(),DataObject.DFTAG_SD).get(0));
                    final ScientificDataDimension sdd=(ScientificDataDimension)
                    (in.ofType(ndg.getObjects(),DataObject.DFTAG_SDD).get(0));
                    /* Whether integer or floating point */
                    sd.setNumberType(sdd.getType());
                    final int histDim=sdd.getRank();
                    if (histDim != 1) {
                        throw new HDFException(
                        "Invalid number of dimensions in '"+name+
                        "': "+histDim);
                    }
                    sd.setRank(histDim);
                    if (sd.getNumberType()==NumberType.INT) {
                        rval = sd.getData1d(sdd.getSizeX());
                        break lookForSpectrum;
                    } else {//DOUBLE
                        throw new HDFException("'"+name+"' is not integer!");
                    }
                }                
            }
        }
        return rval;
    }
    
}