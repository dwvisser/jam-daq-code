package jam.io.hdf;

import jam.data.AbstractHist1D;
import jam.data.DataParameter;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.data.func.CalibrationFunction;
import jam.global.JamProperties;
import jam.util.StringUtilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;
import java.util.List;


/**
 *  Convert a Jam data objects to a hdf data objects 
 *  
 * @author Ken Swartz
 */
final class ConvertJamObjToHDFObj implements JamFileFields{

	private final StringUtilities STRING_UTIL = StringUtilities.instance();
    /**
     * Constructs a Jam-to-HDF object converter.
     */
	ConvertJamObjToHDFObj() {
		super();
	}
	
    /*
     * non-javadoc: Add default objects always needed.
     * 
     * Almost all of Jam's number storage needs are satisfied by the type
     * hard-coded into the class <code> NumberType </code> . This method creates
     * the <code> NumberType </code> object in the file that gets referred to
     * repeatedly by the other data elements. LibVersion Adds data element
     * giving version of HDF libraries to use (4.1r2).
     * 
     * @see jam.io.hdf.NumberType
     */
    void addDefaultDataObjects(String fileID) {
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
     * @see jam.global.JamProperties
     */
    void addFileNote() {
        final String noteAddition = "\n\nThe histograms when loaded into jam are displayed starting at channel zero up\n"
                + "to dimension-1.  Two-dimensional data are properly displayed with increasing channel\n"
                + "number from the lower left to the lower right for, and from the lower left to the upper\n"
                + "left."
                + "All error bars on histogram counts should be considered Poisson, unless a\n"
                + "Numerical Data Group labelled 'Errors' is present, in which case the contents\n"
                + "of that should be taken as the error bars.";
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String header = "Jam Properties at time of save:";
        try {
            JamProperties.getProperties().store(baos, header);
        } catch (IOException ioe) {
            throw new UndeclaredThrowableException(ioe,
                    "Unable to serialize properties.");
        }
        final String notation = baos.toString() + noteAddition;
        new FileDescription(notation);
    }


    /**
     * Adds data objects for the virtual group of groups
     */
    VirtualGroup addGroupSection() {
    	VirtualGroup virtualGroup;
       	virtualGroup = new VirtualGroup(GRP_SECTION, FILE_SECTION);
        new DataIDLabel(virtualGroup, GRP_SECTION);
        return  virtualGroup;
    }
    
    /**
     * Adds data objects for the virtual group of histograms.
     */
    VirtualGroup addHistogramSection() {
       	VirtualGroup allHists = new VirtualGroup(HIST_SECTION, FILE_SECTION);
        new DataIDLabel(allHists, HIST_SECTION);
        return allHists;
    }


    /**
     * Adds data objects for the virtual group of gates.
     */
     VirtualGroup addGateSection() {

     	VirtualGroup   allGates = new VirtualGroup(GATE_SECTION, FILE_SECTION);
        new DataIDLabel(allGates, GATE_SECTION);
        return allGates;
    }

    /**
     * Adds data objects for the virtual group of scalers.
     */
   VirtualGroup addScalerSection() {

    	final VirtualGroup scalerGroup = new VirtualGroup(
            SCALER_SECT, FILE_SECTION);
    	new DataIDLabel(scalerGroup, SCALER_SECT);
    	return scalerGroup;
    }

    /**
     * Adds data objects for the virtual group of scalers.
     */
    VirtualGroup addScalers() {

    	final VirtualGroup scalerGroup = new VirtualGroup(
            SCALER_SECT, FILE_SECTION);
    	new DataIDLabel(scalerGroup, SCALER_SECT);
    	return scalerGroup;
    }
    
    /* non-javadoc:
     * Adds data objects for the virtual group of parameters.
     */
    VirtualGroup addParameterSection() {

    	final VirtualGroup paramGroup = new VirtualGroup(
            PARAMETERS, FILE_SECTION);
    	new DataIDLabel(paramGroup, PARAMETERS);
    	
    	return paramGroup;
    }

    /* non-javadoc:
     * Adds group object for the a histogram
     */
    VirtualGroup addHistogramGroup(Histogram hist) {
        final VirtualGroup histVGroup = new VirtualGroup(hist.getName(),
                HIST_TYPE);
        //histGroup.addDataObject(temp); //add to Histogram section vGroup
        new DataIDLabel(histVGroup, hist.getName());
        /* vGroup label is Histogram name */
        new DataIDAnnotation(histVGroup, hist.getTitle());
        
        return histVGroup;
    }
    
    /* non-javadoc:
     * Adds data objects for the virtual group of histograms.
     */
    VirtualGroup convertGroup(Group group) {
    	VirtualGroup virtualGroup;
       	virtualGroup = new VirtualGroup(group.getName(), GROUP_TYPE);
        new DataIDLabel(virtualGroup, group.getName());
        return  virtualGroup;
    }
	
	/* non-javadoc:
	 * Convert a histogram into a hdf Virtual group
	 * @param hist
	 * @return VirtualGroup for the histogram
	 * @throws HDFException
	 */
    NumericalDataGroup convertHistogram(VirtualGroup histVGroup, Histogram hist) {
        ScientificData sciData=null;
        boolean hasErrors=false;
        AbstractHist1D hist1dWithErrors=null;

        /* vGroup Annotation is Histogram title */
        final NumericalDataGroup ndg = new NumericalDataGroup();
        /* make the NDG label the histogram number */
        histVGroup.add(ndg);
        
        /* NDG to contain data */
        new DataIDLabel(ndg, Integer.toString(hist.getNumber()));
        /* add to specific histogram vGroup (other info maybe later) */
        final ScientificDataDimension sdd = getSDD(hist);
        ndg.addDataObject(sdd); //use new SDD

        final Histogram.Type type = hist.getType();
        if (type == Histogram.Type.ONE_DIM_INT) {
            sciData = new ScientificData((int[]) hist.getCounts());
            hist1dWithErrors = (AbstractHist1D) hist;            
            hasErrors=hist1dWithErrors.errorsSet();
        } else if (type == Histogram.Type.ONE_D_DOUBLE) {
            sciData = new ScientificData((double[]) hist.getCounts());
            hist1dWithErrors = (AbstractHist1D) hist;
            hasErrors=hist1dWithErrors.errorsSet();
        } else if (type == Histogram.Type.TWO_DIM_INT) {
            sciData = new ScientificData((int[][]) hist.getCounts());
        } else if (type == Histogram.Type.TWO_D_DOUBLE) {
            sciData = new ScientificData((double[][]) hist.getCounts());
        } else {
            throw new IllegalArgumentException(
                    "HDFIO encountered a Histogram of unknown type.");
        }
        ndg.addDataObject(sciData);
        
        //Add errors
        if (hasErrors) {
        	ScientificDataDimension sddErr=null;
        	if (type == Histogram.Type.ONE_DIM_INT) {
                sddErr = getSDD(hist,
                        NumberType.DOUBLE);        		
        	} else if (type == Histogram.Type.ONE_D_DOUBLE) {
                sddErr = sdd;        		
        	}
            final NumericalDataGroup ndgErr = new NumericalDataGroup();
            histVGroup.add(ndgErr);
            new DataIDLabel(ndgErr, ERROR_LABEL);
            /* explicitly floating point */
            ndgErr.addDataObject(sddErr);
            final ScientificData sdErr = new ScientificData(hist1dWithErrors.getErrors());
            ndgErr.addDataObject(sdErr);
            //histVGroup.addDataObject(sdErr);	//FIXME KBS remove
            //histVGroup.addDataObject(sddErr);            
        }
        

        //histVGroup.addDataObject(sdd); //use new SDD
        //histVGroup.addDataObject(sciData); //FIXME KBS remove
        return ndg;

	}

    /* non-javadoc:
     * Converts a gate to a Virtual group
     * @param g
     *            the gate to convert
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     */	
    VDataDescription convertCalibration(CalibrationFunction calibrationFunction) {	
        String calibrationType;
        String[] columnNames;        
        int size;
        final short[] orders;

        final short[] types;
//        = { VDataDescription.DFNT_DBL64,
//        		VDataDescription.DFNT_DBL64 };
        final String calibrationName = calibrationFunction.getName();
        if (calibrationFunction.isFitPoints()) {   
        	calibrationType = CALIBRATION_TYPE_POINTS;
            columnNames = CALIBRATION_COLUMNS_POINTS;
            size =calibrationFunction.getPtsEnergy().length;
            types = new short [2];
			types [0]=VDataDescription.DFNT_DBL64;
            types [1]=VDataDescription.DFNT_DBL64;
            orders = new short [2];            
            orders[0] = 1;
            orders[1] = 1;
        } else { //2d
        	calibrationType = CALIBRATION_TYPE_COEFF;        	
        	columnNames = CALIBRATION_COLUMNS_COEFF;
            size = calibrationFunction.getNumberTerms();
            types = new short [1];
			types [0]=VDataDescription.DFNT_DBL64;
            orders = new short [1];
            orders[0] = 1;
        }
        
        final VDataDescription desc = new VDataDescription(calibrationName, calibrationType,
                size, columnNames, types, orders);        
        //HDF Undocumented Vdata has same reference as VdataDescription
        final VData data = new VData(desc);
        
        if (calibrationFunction.isFitPoints()) {
        	double [] channels =calibrationFunction.getPtsChannel();
        	double [] energies=calibrationFunction.getPtsEnergy();
        	for (int i = 0; i < size; i++) {
        		data.addDouble(0, i, channels[i]);
        		data.addDouble(1, i, energies[i]);
        	}
        } else { //2d
        	double [] coeffs = calibrationFunction.getCoeff();
            for (int i = 0; i < size; i++) {
                data.addDouble(0, i, coeffs[i]);
            }
        }
        return desc;		
	}
    
    /* non-javadoc:
     * Converts a gate to a Virtual group
     * @param g
     *            the gate to convert
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     */	
	VirtualGroup convertGate(Gate gate) {	
        String gateType;
        String[] columnNames;        
        int size = 1;

        int[] xcoord = new int[0];
        int[] ycoord = new int[0];
        final short[] types = { VDataDescription.DFNT_INT32,
                VDataDescription.DFNT_INT32 };
        final short[] orders = { 1, 1 };
        final String gateName = gate.getName();
        if (gate.getDimensionality() == 1) {
        	gateType = GATE_1D_TYPE;
            columnNames = GATE_1D;
            size =1;            
        } else { //2d
            gateType = GATE_2D_TYPE;        	
        	columnNames = GATE_2D;
            size = gate.getBananaGate().npoints;
            xcoord = gate.getBananaGate().xpoints;
            ycoord = gate.getBananaGate().ypoints;
        }
        /* create the VG for the current gate */
        final VirtualGroup vggate = new VirtualGroup(gateName, gateType);
        /* add name as note to vg */
        new DataIDAnnotation(vggate, gate.getHistogram().getName());
        
        final VDataDescription desc = new VDataDescription(gateName, gateType,
                size, columnNames, types, orders);
        vggate.add(desc); //add vData description to gate VG        
        //HDF Undocumented Vdata has same reference as VdataDescription
        final VData data = new VData(desc);
        //KBS not needed
        //vggate.addDataObject(data); //add vData to gate VG
        
        if (gate.getDimensionality() == 1) {
            data.addInteger(0, 0, gate.getLimits1d()[0]);
            data.addInteger(1, 0, gate.getLimits1d()[1]);
        } else { //2d
            for (int i = 0; i < size; i++) {
                data.addInteger(0, i, xcoord[i]);
                data.addInteger(1, i, ycoord[i]);
            }
        }
        /* FIXME KBS delete
        // add Histogram links...
        final VirtualGroup hist = VirtualGroup.ofName(DataObject
                .ofType(DataObject.DFTAG_VG), gate.getHistogram().getName());
        if (hist != null) {
            hist.addDataObject(vggate);
            //reference the Histogram in the gate group
        } 
        */       
        return vggate;		
	}

    /* non-javadoc:
     * Converts a scaler to a Virtual group
     * @param list
     *            the list to convert
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     */		
	VDataDescription convertScalers(List scalers)  {
                
        final int size = scalers.size();        
        final short[] types = { VDataDescription.DFNT_INT32,
                VDataDescription.DFNT_CHAR8, VDataDescription.DFNT_INT32 };
        final short[] orders = new short[3];
        orders[0] = 1; //number
        orders[1] = (short)maxNameLengthScaler(scalers); //name ... 
        orders[2] = 1; //value
        final String name = SCALER_SECT;
        final String scalerType = SCALER_TYPE;
        final String[] names = SCALER_COLS;
        
        final VDataDescription desc = new VDataDescription(name,
                scalerType, size, names, types, orders);        
        
        final VData data = new VData(desc);

        for (int i = 0; i < size; i++) {
            final Scaler scaler = (Scaler) (scalers.get(i));
            data.addInteger(0, i, scaler.getNumber());
            data.addChars(1, i, STRING_UTIL.makeLength(scaler.getName(), orders[1]));
            data.addInteger(2, i, scaler.getValue());
        }
                
        return desc;
	}

    /* non-javadoc:
     * Converts a scaler to a Virtual group
     * @param list
     *            the list to convert
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     */		
	VDataDescription convertGroupScalers(List scalers)  {
        
        final int size = scalers.size();		
        final short[] types = { VDataDescription.DFNT_INT32,
                VDataDescription.DFNT_CHAR8, VDataDescription.DFNT_INT32 };        
        final short[] orders = new short[3];
        orders[0] = 1; 										//number
        orders[1] =  (short)maxNameLengthScaler(scalers); 	//name 
        orders[2] = 1; 										//value
        final String name = SCALER_SECT;
        final String scalerType = SCALER_TYPE;
        final String[] names = SCALER_COLS;
        
        final VDataDescription desc = new VDataDescription(name,
                scalerType, size, names, types, orders);
        
        final VData data = new VData(desc);

        for (int i = 0; i < size; i++) {
            final Scaler scaler = (Scaler) (scalers.get(i));
            data.addInteger(0, i, scaler.getNumber());
            data.addChars(1, i, STRING_UTIL.makeLength(scaler.getName(), orders[1]));
            data.addInteger(2, i, scaler.getValue());
        }
        
        return desc;

	}
		
    /* non-javadoc:
     * Converts a parameters to a Virtual group
     * @param list
     *            the list to convert
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     */		
	VDataDescription convertParameters(List parameters) {
		
	    final short[] types = { VDataDescription.DFNT_CHAR8,
	            VDataDescription.DFNT_FLT32 };
	    final short[] orders = new short[2];
	    final int size = parameters.size();
	    /* set order values */
	    orders[0] = (short)maxNameLengthParam(parameters); //name 
	    orders[1] = 1; //value
	    final VDataDescription desc = new VDataDescription(PARAMETERS,
	            PAR_TYPE, size, PARAM_COLS, types, orders);
	    final VData data = new VData(desc);
	    for (int i = 0; i < size; i++) {
	        final StringUtilities util = StringUtilities.instance();
	        final DataParameter param = (DataParameter) (parameters.get(i));
	        data.addChars(0, i, util.makeLength(param.getName(), orders[0]));
	        data.addFloat(1, i, (float) param.getValue());
	    }

	    return desc;
	}
	
	int maxNameLengthParam(List dataList) {
		int maxLength =0;
	    final Iterator iter = dataList.iterator();
	    while (iter.hasNext()) {
	    	final String name =((DataParameter)iter.next()).getName();
	        final int len = name.length();
	        if (len >maxLength) {
	        	maxLength =len;
	        }
	    }
	    return maxLength;		
	}
	int maxNameLengthScaler(List dataList) {
		int maxLength =0;
	    final Iterator iter = dataList.iterator();
	    while (iter.hasNext()) {
	    	final String name =((Scaler)iter.next()).getName();
	        final int len = name.length();
	        if (len >maxLength) {
	        	maxLength =len;
	        }
	    }
	    return maxLength;		
	}

	/* non-javadoc:
	 * @return the existing valid SDD type for the histogram, 
	 * creating a new one if necessary.
	 * @param h that type is needed for
	 */
	private ScientificDataDimension getSDD(Histogram hist) {
		byte type=NumberType.DOUBLE;
		if (hist.getType().isInteger()) {
			type = NumberType.INT;
		}
		return getSDD(hist,type);
	}
	
	/* non-javadoc:
	 * Returns the existing valid SDD type for the histogram, 
	 * creating a new one if necessary.  DOUBLE type
	 * is explicitly requested, for error bars.
	 *
	 * @param h which type is needed for
	 * @param numtype the number HDF uses to indicate the type
	 * @return the SDD object representing the histogram size and number 
	 * type
	 */
	private ScientificDataDimension getSDD(Histogram hist, byte numberType) {
		final short rank = (short)hist.getDimensionality();
		final int sizeX = hist.getSizeX();
		int sizeY=0;
		if (rank == 2) {//otherwise rank == 1
			sizeY = hist.getSizeY();
		} 
		return ScientificDataDimension.create(rank, sizeX, sizeY, numberType);
	}
	
}
