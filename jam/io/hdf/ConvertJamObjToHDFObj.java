package jam.io.hdf;

import java.util.Iterator;

import jam.data.AbstractHist1D;
import jam.data.DataBase;
import jam.data.DataParameter;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.util.StringUtilities;
import java.util.Iterator;
import java.util.List;


/**
 *  Convert a Jam data objects to a hdf data objects 
 *  
 * @author Ken Swartz
 *
 */
public class ConvertJamObjToHDFObj implements JamHDFFields{

	public ConvertJamObjToHDFObj() {
		
	}
	/**
	 * Convert a histogram into a hdf Virtual group
	 * @param hist
	 * @return VirtualGroup for the histogram
	 * @throws HDFException
	 */
	VirtualGroup convertHistogram(Histogram hist) throws HDFException {
        ScientificData sciData;
        final VirtualGroup histVGroup = new VirtualGroup(hist.getName(),
                HIST_TYPE_NAME);
        //FIXME KBS remove
        //histGroup.addDataObject(temp); //add to Histogram section vGroup
        new DataIDLabel(histVGroup, hist.getName());
        /* vGroup label is Histogram name */
        new DataIDAnnotation(histVGroup, hist.getTitle());
        /* vGroup Annotation is Histogram title */
        final NumericalDataGroup ndg = new NumericalDataGroup();
        /* NDG to contain data */
        new DataIDLabel(ndg, Integer.toString(hist.getNumber()));
        /* make the NDG label the histogram number */
        histVGroup.addDataObject(ndg);
        /* add to specific histogram vGroup (other info maybe later) */
        final ScientificDataDimension sdd = getSDD(hist);
        ndg.addDataObject(sdd); //use new SDD
        histVGroup.addDataObject(sdd); //use new SDD
        final Histogram.Type type = hist.getType();
        if (type == Histogram.Type.ONE_DIM_INT) {
            final AbstractHist1D hist1d = (AbstractHist1D) hist;
            sciData = new ScientificData((int[]) hist.getCounts());
            if (hist1d.errorsSet()) {
                NumericalDataGroup ndgErr = new NumericalDataGroup();
                new DataIDLabel(ndgErr, ERROR_LABEL);
                histVGroup.addDataObject(ndgErr);
                ScientificDataDimension sddErr = getSDD(hist,
                        NumberType.DOUBLE);
                /* explicitly floating point */
                ndgErr.addDataObject(sddErr);
                histVGroup.addDataObject(sddErr);
                ScientificData sdErr = new ScientificData(hist1d.getErrors());
                ndgErr.addDataObject(sdErr);
                histVGroup.addDataObject(sdErr);
            }
        } else if (type == Histogram.Type.ONE_D_DOUBLE) {
            final AbstractHist1D h1 = (AbstractHist1D) hist;
            sciData = new ScientificData((double[]) hist.getCounts());
            if (h1.errorsSet()) {
                NumericalDataGroup ndgErr = new NumericalDataGroup();
                new DataIDLabel(ndgErr, ERROR_LABEL);
                histVGroup.addDataObject(ndgErr);
                ScientificDataDimension sddErr = sdd;
                /* explicitly floating point */
                ndgErr.addDataObject(sddErr);
                ScientificData sdErr = new ScientificData(h1.getErrors());
                ndgErr.addDataObject(sdErr);
                histVGroup.addDataObject(sdErr);
            }
        } else if (type == Histogram.Type.TWO_DIM_INT) {
            sciData = new ScientificData((int[][]) hist.getCounts());
        } else if (type == Histogram.Type.TWO_D_DOUBLE) {
            sciData = new ScientificData((double[][]) hist.getCounts());
        } else {
            throw new IllegalArgumentException(
                    "HDFIO encountered a Histogram of unknown type.");
        }
        ndg.addDataObject(sciData);
        histVGroup.addDataObject(sciData);
        return histVGroup;

	}
	
	
	VirtualGroup convertGate(Gate gate) throws HDFException {	
        String gateType;
        String[] columnNames;        
        int size = 1;

        int[] x = new int[0];
        int[] y = new int[0];
        final short[] types = { VdataDescription.DFNT_INT32,
                VdataDescription.DFNT_INT32 };
        final short[] orders = { 1, 1 };
        final String gateName = gate.getName();
        if (gate.getDimensionality() == 1) {
        	gateType = GATE_1D_TYPE_NAME;
            columnNames = GATE_1D_NAMES;
            size =1;            
        } else { //2d
            gateType = GATE_2D_TYPE_NAME;        	
        	columnNames = GATE_2D_NAMES;
            size = gate.getBananaGate().npoints;
            x = gate.getBananaGate().xpoints;
            y = gate.getBananaGate().ypoints;
        }
        /* get the VG for the current gate */
        final VirtualGroup vggate = new VirtualGroup(gateName, gateType);
        //FIXME KBS remove
        //gateGroup.addDataObject(vg); //add to Gate section vGroup
        final VdataDescription desc = new VdataDescription(gateName, gateType,
                size, columnNames, types, orders);
        final Vdata data = new Vdata(desc);
        vggate.addDataObject(desc); //add vData description to gate VG
        vggate.addDataObject(data); //add vData to gate VG
        if (gate.getDimensionality() == 1) {
            data.addInteger(0, 0, gate.getLimits1d()[0]);
            data.addInteger(1, 0, gate.getLimits1d()[1]);
        } else { //2d
            for (int i = 0; i < size; i++) {
                data.addInteger(0, i, x[i]);
                data.addInteger(1, i, y[i]);
            }
        }
        data.refreshBytes();
        /* add Histogram links... */
        final VirtualGroup hist = VirtualGroup.ofName(DataObject
                .ofType(DataObject.DFTAG_VG), gate.getHistogram().getName());
        /* add name as note to vg */
        new DataIDAnnotation(vggate, gate.getHistogram().getName());
        if (hist != null) {
            hist.addDataObject(vggate);
            //reference the Histogram in the gate group
        }        
        return vggate;		
	}
	
	VirtualGroup convertScalers(List scalers) throws HDFException {
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
        
        return scalerGroup;

	}
	
	VirtualGroup convertParameters(List parameters) throws HDFException {	
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
	    return parameterGroup;
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
	
}
