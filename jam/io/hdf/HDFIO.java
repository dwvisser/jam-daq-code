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
 * Reads and writes HDF files containing spectra, scalers, gates, and additional useful
 * information.
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
    private static File lastValidFile;

    /**
     * File to save HDF information to.
     */
    File fileSave;

    /**
     * Parent frame.
     */
    private Frame frame;

    /**
     * Where messages get sent (presumably the console).
     */
    private MessageHandler msgHandler;

    /**
     * State determines whether histograms get written out.
     */
    private boolean writeHistograms=true;


    /**
     * State determines whether gates get written out.
     */
    private boolean writeGates=true;

    /**
     * State determines whether scalers get written out.
     */
    private boolean writeScalers=true;

    /**
     * State determines whether parameters get written out.
     */
    private boolean writeParameters=true;

    ScientificDataLabel histLabel1d, histLabel2d;

    VirtualGroup histGroup;
    VirtualGroup gateGroup;
    VirtualGroup scalerGroup;
    VirtualGroup parameterGroup;

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
     * @param  his  if true, Histograms will be written
     * @param  gate  if true, Gates will be written
     * @param  scalers  if true, scaler values will be written
     */
    public void writeFile(boolean wrthis, boolean wrtgate, boolean wrtscalers)   {
        writeFile( wrthis,  wrtgate,  wrtscalers, true);
    }

    /**
     * Writes out the currently held spectra, gates, and scalers, subject to the options given
     *.
     * @param  his  if true, Histograms will be written
     * @param  gate  if true, Gates will be written
     * @param  scalers  if true, scaler values will be written
     * @param  parameters if true, parameter values will be written
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
     * @param  his  if true, Histograms will be written
     * @param  gate  if true, Gates will be written
     * @param  scalers  if true, scaler values will be written
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
     * @param  his  if true, Histograms will be written
     * @param  gate  if true, Gates will be written
     * @param  scalers  if true, scaler values will be written
     * @param  parameters if true, parameter values will be written
     */
    public void writeFile(boolean wrthis, boolean wrtgate, boolean wrtscalers, boolean wrtparameters, File file) {
        java.util.List hist, gate, scaler, parameter;
        
        writeHistograms=wrthis;
        writeGates=wrtgate;
        writeScalers=wrtscalers;
        writeParameters=wrtparameters;
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
        JFileChooser jfile = new JFileChooser(fileSave);
        jfile.setFileFilter(new HDFileFilter(true));
        int option=jfile.showOpenDialog(frame);
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

        VirtualGroup temp;
        ScientificDataDimension sdd,sddErr;
        NumericalDataGroup ndg,ndgErr;
        ScientificData sd,sdErr;

        try {
            temp = new VirtualGroup(out, h.getName(),HIST_TYPE_NAME);
            histGroup.addDataObject(temp); //add to Histogram section vGroup
            new DataIDLabel(temp,h.getName());  // vGroup label is Histogram name
            new DataIDAnnotation(temp,h.getTitle()); // vGroup Annotation is Histogram title
            ndg = new NumericalDataGroup(out);// NDG to contain data
            new DataIDLabel(ndg,Integer.toString(h.getNumber()));//make the NDG label the histogram number
            temp.addDataObject(ndg); //add to specific histogram vGroup (other info maybe later)
            sdd = out.getSDD(h);
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
        VirtualGroup current;//current histogram to read in
        NumericalDataGroup ndg;
        NumericalDataGroup ndgErr=null;//I check for null to determine if error bars exist
        ScientificData sd;
        ScientificDataDimension sdd,sddErr;
        byte histNumType;
        int histDim;
        int number;//histogram number
        String name;//histogram name
        String title;//histogram title
        int sizeX,sizeY;//histogram size
        DataIDLabel templabel;
        DataIDAnnotation tempnote;
        DataIDLabel numLabel;
        Histogram histogram;
        NumericalDataGroup [] numbers;
        try{
            java.util.List groups=in.ofType(DataObject.DFTAG_VG);//all VG's in file
            VirtualGroup hists=VirtualGroup.ofName(groups,HIST_SECTION_NAME);
            /* only the "histograms" VG (only one element) */
            ScientificData sdErr=null;
            if (hists != null){
                if (mode==OPEN) DataBase.clearAllLists();//clear if opening and there are histograms in file
                java.util.List labels=in.ofType(DataObject.DFTAG_DIL);//all DIL's in file
                java.util.List annotations=in.ofType(DataObject.DFTAG_DIA);//all DIA's in file
                msgHandler.messageOut(hists.getObjects().size()+" histograms",MessageHandler.CONTINUE);
                for (Iterator temp = hists.getObjects().iterator() ; temp.hasNext() ;) {
                    current=(VirtualGroup)(temp.next());
                    java.util.List tempVec = in.ofType(current.getObjects(),DataObject.DFTAG_NDG);
                    numbers = new NumericalDataGroup[tempVec.size()];
                    tempVec.toArray(numbers);
                    //System.out.println("numbers has "+numbers.length+" elements");
                    if (numbers.length == 1) {
                        ndg=numbers[0]; //only one NDG -- the data
                    } else if (numbers.length == 2) {
                        if (DataIDLabel.withTagRef(labels,DataObject.DFTAG_NDG,numbers[0].getRef()).getLabel().equals(ERROR_LABEL)){
                            ndg=numbers[1];
                            ndgErr=numbers[0];
                        } else {
                            ndg=numbers[0];
                            ndgErr=numbers[1];
                        }
                    } else {
                        throw new HDFException("Invalid number of data groups ("+numbers.length+") in NDG.");
                    }
                    sd=(ScientificData)(in.ofType(ndg.getObjects(),DataObject.DFTAG_SD).get(0));
                    sdd=(ScientificDataDimension)(in.ofType(ndg.getObjects(),DataObject.DFTAG_SDD).get(0));
                    numLabel=DataIDLabel.withTagRef(labels,ndg.getTag(),ndg.getRef());
                    number=Integer.parseInt(numLabel.getLabel());
                    histNumType=sdd.getType();
                    sd.setNumberType(histNumType);
                    histDim=sdd.getRank();
                    sd.setRank(histDim);
                    sizeX=sdd.getSizeX();
                    if (histDim==2) {
                        sizeY=sdd.getSizeY();
                    } else {
                        sizeY=0;
                    }
                    templabel=DataIDLabel.withTagRef(labels,current.getTag(),current.getRef());
                    tempnote=DataIDAnnotation.withTagRef(annotations,current.getTag(),current.getRef());
                    name=templabel.getLabel();
                    title=tempnote.getNote();
                    if (ndgErr != null){
                        sdErr = (ScientificData)(in.ofType(ndgErr.getObjects(),DataObject.DFTAG_SD).get(0));
                        sdErr.setRank(histDim);
                        sdErr.setNumberType(NumberType.DOUBLE);
                        sddErr =(ScientificDataDimension)(in.ofType(ndgErr.getObjects(),DataObject.DFTAG_SDD).get(0));
                    }
                    if (mode==OPEN){
                        System.out.print("New Histogram: "+name+", "+histDim+
                            "-D, "+sizeX);
                        if (histDim==1) {
                            System.out.println(" channels");
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
                            System.out.println(" x "+sizeY+" channels");
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
                        histogram = Histogram.getHistogram(StringUtilities.makeLength(name,Histogram.NAME_LENGTH));
                        if (histogram != null) {
                            if (histDim==1) {
                                if (histNumType==NumberType.INT) {
                                    histogram.setCounts(sd.getData1d(sizeX));
                                } else {
                                    histogram.setCounts(sd.getData1dD(sizeX));
                                }
                            } else { // 2-d
                                if (histNumType==NumberType.INT) {
                                    histogram.setCounts(sd.getData2d(sizeX,sizeY));
                                } else {
                                    histogram.setCounts(sd.getData2dD(sizeX,sizeY));
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
            throw new HDFException ("Problem getting Histograms: "+e.getMessage());
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
     * Add the virtual group representing a single gate, and link it into
     * the virtual group representing all gates.
     *
     * @param  g   the gate to add
     * @see #addGateSection()
     * @exception   HDFException thrown if unrecoverable error occurs
     */
    protected void addGate(Gate g) throws HDFException{
        int i;
        VirtualGroup vg;
        VdataDescription desc;
        Vdata data;
        String name;
        String gateType;
        int size;

        try{

            int []x=new int [0];
            int []y=new int [0];
            String [] names;
            VirtualGroup hist; //hist to link to this gate
        short [] types = {VdataDescription.DFNT_INT32 , VdataDescription.DFNT_INT32};
        short [] orders = {1,1};
            DataIDAnnotation note;//note contains histogram name gate resides in

            name = g.getName();
            if (g.getType()==Gate.ONE_DIMENSION){
                gateType = GATE_1D_TYPE_NAME;
                size = 1;
                names = GATE_1D_NAMES;
            } else {//2d
                gateType = GATE_2D_TYPE_NAME;
                size = g.getBananaGate().npoints;
                x=g.getBananaGate().xpoints;
                y=g.getBananaGate().ypoints;
                names = GATE_2D_NAMES;
            }
            vg = new VirtualGroup(out,name,gateType);//VG for curent gate
            gateGroup.addDataObject(vg);//add to Gate section vGroup
            desc = new VdataDescription(out,name,gateType,size,names,types,orders);
            data = new Vdata(out,desc);
            vg.addDataObject(desc); //add vData description to gate VG
            vg.addDataObject(data); //add vData to gate VG
            if (g.getType()==1) {

                data.addInteger(0,0,g.getLimits1d()[0]);
                data.addInteger(1,0,g.getLimits1d()[1]);
            } else {//2d
                for (i=0;i<size;i++) {
                    data.addInteger(0,i,x[i]);
                    data.addInteger(1,i,y[i]);
                }
            }
            data.refreshBytes();
            //add Histogram links...
            hist=VirtualGroup.ofName(out.ofType(DataObject.DFTAG_VG),g.getHistogram().getName());
            note = new DataIDAnnotation(vg,g.getHistogram().getName());
            hist.addDataObject(vg);//reference the Histogram in the gate group
        } catch (DataException e) {
            throw new HDFException ("Problem adding Gate: "+e.getMessage());
        }
    }

    /**
     * Retrieve the gates from the file.
     *
     * @param  mode  whether to open or reload
     * @exception HDFexception thrown if unrecoverable error occurs
     */
    private void getGates(int mode) throws HDFException{
        VdataDescription    VH;
        Vdata        VS;
        int        i, numRows;
        String        gname, hname;
        VirtualGroup      gates;
        java.util.List groups, annotations;
        VirtualGroup      currVG;
        Polygon        pg;

        Gate g=null;
        try {
            groups = in.ofType(DataObject.DFTAG_VG);//all VG's in file
            gates = VirtualGroup.ofName(groups,GATE_SECTION_NAME);
            //only the "gates" VG (only one element)
            annotations = in.ofType(DataObject.DFTAG_DIA);
            if (gates != null){
                if (mode==OPEN) Gate.clearList();//clear if opening and there are histograms in file
                msgHandler.messageOut(gates.getObjects().size()+" gates",MessageHandler.CONTINUE);
                for (Iterator temp = gates.getObjects().iterator() ; temp.hasNext(); ){
                    currVG = (VirtualGroup)(temp.next());
                    VH=(VdataDescription)(in.ofType(currVG.getObjects(),DataObject.DFTAG_VH).get(0));
                    if (VH != null) {
                        VS=(Vdata)(in.getObject(DataObject.DFTAG_VS,VH.getRef()));//corresponding VS
                        numRows = VH.getNumRows();
                        gname = currVG.getName();
                        hname = DataIDAnnotation.withTagRef(annotations,currVG.getTag(),currVG.getRef()).getNote();
                        if (mode==OPEN){
                            g = new Gate(gname,Histogram.getHistogram(StringUtilities.makeLength(hname,Histogram.NAME_LENGTH)));
                        } else {//reload
                            g = Gate.getGate(StringUtilities.makeLength(gname,Gate.NAME_LENGTH));
                        }
                        if (g!=null) {                  
                        	if (g.getType()==Gate.ONE_DIMENSION){//1-d gate
                            	g.setLimits(VS.getInteger(0,0).intValue(),VS.getInteger(0,1).intValue());
                        	} else {//2-d gate
                            	pg = new Polygon();
                            	for (i=0;i<numRows;i++){
                                	pg.addPoint(VS.getInteger(i,0).intValue(),VS.getInteger(i,1).intValue());
                            	}
                            	g.setLimits(pg);
                        	}
                        }
                    } else {
                        msgHandler.messageOutln("Problem processing a VH in HDFIO!");
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
     * @exception   HDFException      thrown if unrecoverable error occurs
     */
    protected void addScalerSection(java.util.List scalers) throws HDFException{
        VdataDescription desc;
        Vdata data;

        int size;
        String scalerType;
        String [] names;
    short [] types = {VdataDescription.DFNT_INT32,VdataDescription.DFNT_CHAR8,VdataDescription.DFNT_INT32};
        short [] orders = new short[3];
        int dimtest;
        Scaler s;
        String name;

        size=scalers.size();

        //set orders
        orders[0]=1;//number
        orders[1]=0;//name ... loop below picks longest name for dimension
        for (Iterator enum = scalers.iterator() ; enum.hasNext() ;) {
            dimtest=((Scaler)(enum.next())).getName().length();
            if (dimtest > orders[1]) {
                orders[1]=(short)dimtest;
            }
        }
        orders[2]=1;//value

        scalerGroup = new VirtualGroup(out,SCALER_SECTION_NAME,FILE_SECTION_NAME);
        new DataIDLabel(scalerGroup,SCALER_SECTION_NAME);
        name = SCALER_SECTION_NAME;
        scalerType = SCALER_TYPE_NAME;
        names = SCALER_COLUMN_NAMES;
        desc = new VdataDescription(out,name,scalerType,size,names,types,orders);
        data = new Vdata(out,desc);
        scalerGroup.addDataObject(desc); //add vData description to gate VG
        scalerGroup.addDataObject(data); //add vData to gate VG

        for (int i=0; i < size ; i++) {
            s=(Scaler)(scalers.get(i));
            //System.out.println("Trying to add row "+i+": "+s.getNumber()+", '"+s.getName()+"', "+s.getValue());
            //msgHandler.messageOut(" . ",MessageHandler.CONTINUE);
            data.addInteger(0,i,s.getNumber());
            data.addChars(1,i,StringUtilities.makeLength(s.getName(),orders[1]));
            data.addInteger(2,i,s.getValue());
        }
        data.refreshBytes();
    }

    /**
     * retrieve the scalers from the file
     *
     * @param  mode  whether to open or reload
     */
    private void getScalers(int mode) throws HDFException {
        VdataDescription    VH;
        Vdata        VS;
        int        i, numScalers;
        Scaler        s;
        String        sname;

        VH=VdataDescription.ofName(in.ofType(DataObject.DFTAG_VH),SCALER_SECTION_NAME);
        //only the "scalers" VH (only one element) in the file
        try {
            if (VH != null) {
                if (mode==OPEN) Scaler.clearList();
                VS=(Vdata)(in.getObject(DataObject.DFTAG_VS,VH.getRef()));//corresponding VS
                numScalers=VH.getNumRows();
                msgHandler.messageOut(numScalers+" scalers",MessageHandler.CONTINUE);
                for (i=0;i<numScalers;i++){
                    sname = VS.getString(i,1);
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
                        //XXXSystem.err.println("HDFIO.getScalers("+mode+"): No Scaler in memory with name: '"+sname+"'");
                    }

                }
            } else {
                System.err.println("HDFIO.getScalers("+mode+"): No Scalers section in HDF file.");
            }
        } catch (DataException e) {
            throw new HDFException("Problem creating scalers: "+e.getMessage());
        }

    }
    /**
     * Adds data objects for the virtual group of parameters.
     *
     * @exception   HDFException      thrown if unrecoverable error occurs
     */
    protected void addParameterSection(java.util.List parameters) throws HDFException{

        int size;          //number of parameters
        VdataDescription desc;
        Vdata data;
        String parameterType;
        String [] names;
    short [] types = {VdataDescription.DFNT_CHAR8, VdataDescription.DFNT_FLT32};
        short [] orders = new short[2];
        int lenMax;
        DataParameter p;
        String name;

        size=parameters.size();

        //set order values
        orders[0]=0;//name ... loop below picks longest name for dimension
        for (Iterator enum = parameters.iterator() ; enum.hasNext() ;) {
            lenMax=((DataParameter)(enum.next())).getName().length();
            if (lenMax > orders[0]) {
                orders[0]=(short)lenMax;
            }
        }
        orders[1]=1;//value

        parameterGroup = new VirtualGroup(out, PARAMETER_SECTION_NAME, FILE_SECTION_NAME);
        new DataIDLabel(parameterGroup, PARAMETER_SECTION_NAME);
        name = PARAMETER_SECTION_NAME;
        parameterType = PARAMETER_TYPE_NAME;
        names = PARAMETER_COLUMN_NAMES;
        desc = new VdataDescription(out, name, parameterType, size, names, types, orders);
        data = new Vdata(out,desc);
        parameterGroup.addDataObject(desc); //add vData description to gate VG
        parameterGroup.addDataObject(data); //add vData to gate VG

        for (int i=0; i < size ; i++) {
            p=(DataParameter)(parameters.get(i));
            //System.out.println("Trying to add row "+i+": "+p.getNumber()+", '"+p.getName()+"', "+p.getValue());
            //msgHandler.messageOut(" . ",MessageHandler.CONTINUE);
            data.addChars(0,i,StringUtilities.makeLength(p.getName(),orders[0]));
            data.addFloat(1,i,(float)p.getValue());
        }
        data.refreshBytes();
    }

    /**
     * retrieve the parameters from the file
     *
     * @param  mode  whether to open or reload
     */
    private void getParameters(int mode) throws HDFException{

        VdataDescription    VH;
        Vdata        VS;
        int        i, numParameters;
        DataParameter      p;
        String        pname;

        VH=VdataDescription.ofName(in.ofType(DataObject.DFTAG_VH),PARAMETER_SECTION_NAME);
        //only the "parameters" VH (only one element) in the file
        try{
            if (VH != null) {
                if (mode==OPEN) DataParameter.clearList();
                VS=(Vdata)(in.getObject(DataObject.DFTAG_VS,VH.getRef()));//corresponding VS
                numParameters=VH.getNumRows();
                msgHandler.messageOut(" "+numParameters+" parameters ",MessageHandler.CONTINUE);
                for (i=0;i<numParameters;i++){
                    pname = VS.getString(i,0);
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
                System.err.println("HDFIO.getParameter("+mode+"): No Parameters section in HDF file.");
            }
        } catch (DataException e) {
            throw new HDFException ("Problem creating Parameters: "+e.getMessage());
        }
    }

    /**
     * Determines whether a <code>Vector</code> passed to it <ol><li>exists, and </li><li></ol>
     * has any elements.
     *
     * @param  v   the <code>Vector</code> to check
     */
    protected boolean hasContents(java.util.List v){
        boolean val;

        val=(v!=null)&&(!v.isEmpty());
        return val;
    }

    /**
     * The name of the file opened
     */
    public String getFileNameOpen(){
        return fileNameOpen;
    }

    /**
     * Last file successfully read from or written to.
     */
    static public File lastValidFile(){
        return lastValidFile;
    }
    
    public File getInputFile(){
        return in.getFile();
    }

    public int [] readIntegerSpectrum(String spectrumName) throws IOException, HDFException{
        NumericalDataGroup ndg;
        
        int [] rval=null;//default return value
        java.util.List groups=in.ofType(DataObject.DFTAG_VG);//all VG's in file
        VirtualGroup hists=VirtualGroup.ofName(groups,HIST_SECTION_NAME);
        /* only the "histograms" VG (only one element) */
        if (hists == null) {
            throw new HDFException("No Histogram section in file: "+in.getFile());
        } else{
            /* there are histograms in the file */
            java.util.List labels=in.ofType(DataObject.DFTAG_DIL);//all DIL's in file
            lookForSpectrum : for (Iterator temp = hists.getObjects().iterator() ; temp.hasNext() ;) {
                VirtualGroup current=(VirtualGroup)(temp.next());
                java.util.List tempVec = in.ofType(current.getObjects(),DataObject.DFTAG_NDG);//NDG's in current hist record
                NumericalDataGroup [] numbers = new NumericalDataGroup[tempVec.size()]; 
                tempVec.toArray(numbers);
                String name = DataIDLabel.withTagRef(labels,current.getTag(),current.getRef()).getLabel();
                if (name.trim().equals(spectrumName)) {
                    if (numbers.length == 1) {//only one NDG -- the data
                        ndg=numbers[0];
                    } else if (numbers.length == 2) {
                    	/* determine which of the two contains error bars
                    	 * and ignore it, assigning the other to ndg */
                        if (DataIDLabel.withTagRef(
                        labels,DataObject.DFTAG_NDG,numbers[0].getRef()).getLabel().equals(ERROR_LABEL)){
                            ndg=numbers[1];
                        } else {
                            ndg=numbers[0];
                        }
                    } else {
                        throw new HDFException("Invalid number of data groups ("+numbers.length+") in NDG.");
                    }
                    ScientificData sd=(ScientificData)(in.ofType(ndg.getObjects(),DataObject.DFTAG_SD).get(0));
                    ScientificDataDimension sdd=(ScientificDataDimension)
                    (in.ofType(ndg.getObjects(),DataObject.DFTAG_SDD).get(0));
                    sd.setNumberType(sdd.getType());//Whether integer or floating point
                    int histDim=sdd.getRank();
                    if (histDim != 1) {
                        throw new HDFException("Invalid number of dimensions in '"+name+"': "+histDim);
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