package jam.io.hdf;

import jam.data.AbstractHist1D;
import jam.data.DataParameter;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.io.FileOpenMode;
import jam.util.StringUtilities;

import java.awt.Polygon;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ken Swartz
 * @version 15 Feb 2005
 */
public class ConvertHDFObjToJamObj implements JamHDFFields {
    
	private HDFile inHDF;
	
    ConvertHDFObjToJamObj(){
        super();
    }
    
    void setInFile(HDFile infile) {
    	inHDF =infile;
    }
    /**
     * 
     * /** looks for the special Histogram section and reads the data into
     * memory.
     * 
     * @param mode
     *            whether to open or reload
     * @param sb
     *            summary message under construction
     * @param histNames
     *            names of histograms to read, null if all
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     * @throws IllegalStateException
     *             if any histogram apparently has more than 2 dimensions
     * @return number of histograms
     */
    int convertHistograms(FileOpenMode mode, List histNames)
            throws HDFException {
        int numHists = 0;
        /* get list of all VG's in file */
        final List groups = DataObject.ofType(DataObject.DFTAG_VG);
        final VirtualGroup hists = VirtualGroup.ofName(groups, HIST_SECTION);
        /* only the "histograms" VG (only one element) */
        if (hists != null) {
            //Message
            numHists = hists.getObjects().size();

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
                final ScientificData sciData = (ScientificData) (DataObject
                        .ofType(ndg.getObjects(), DataObject.DFTAG_SD).get(0));
                final ScientificDataDimension sdd = (ScientificDataDimension) (DataObject
                        .ofType(ndg.getObjects(), DataObject.DFTAG_SDD).get(0));
                final DataIDLabel numLabel = DataIDLabel.withTagRef(labels, ndg
                        .getTag(), ndg.getRef());
                final int number = Integer.parseInt(numLabel.getLabel());
                final byte histNumType = sdd.getType();
                sciData.setNumberType(histNumType);
                final int histDim = sdd.getRank();
                sciData.setRank(histDim);
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
                if (ndgErr == null) {
                    sdErr = null;
                } else {
                    sdErr = (ScientificData) (DataObject.ofType(ndgErr
                            .getObjects(), DataObject.DFTAG_SD).get(0));
                    sdErr.setRank(histDim);
                    sdErr.setNumberType(NumberType.DOUBLE);
                }
                /* Given name list check that that the name is in the list */
                if (histNames == null || histNames.contains(name)) {
                    createHistogram(mode, name, title, number, histNumType,
                            histDim, sizeX, sizeY, sciData, sdErr);
                }
            }
        }
        return numHists;
    }

    /*
     * non-javadoc: Retrieve the gates from the file.
     * 
     * @param mode whether to open or reload @throws HDFException thrown if
     * unrecoverable error occurs
     */
    int convertGates(FileOpenMode mode) throws HDFException {
        int numGates = 0;
        final StringUtilities util = StringUtilities.instance();
        Gate gate = null;
        /* get list of all VG's in file */
        final List groups = DataObject.ofType(DataObject.DFTAG_VG);
        /* get only the "gates" VG (only one element) */
        final VirtualGroup gates = VirtualGroup.ofName(groups, GATE_SECTION);
        final List annotations = DataObject.ofType(DataObject.DFTAG_DIA);
        if (gates != null) {
            numGates = gates.getObjects().size();
            final Iterator temp = gates.getObjects().iterator();
            while (temp.hasNext()) {
                final VirtualGroup currVG = (VirtualGroup) (temp.next());
                final VdataDescription vdd = (VdataDescription) (DataObject
                        .ofType(currVG.getObjects(), DataObject.DFTAG_VH)
                        .get(0));
                if (vdd == null) {
                    throw new HDFException("Problem processing a VH for a gate.");
                } else {
                    final Vdata data = (Vdata) (DataObject.getObject(
                            DataObject.DFTAG_VS, vdd.getRef()));
                    //corresponding VS
                    final int numRows = vdd.getNumRows();
                    final String gname = currVG.getName();
                    final String hname = DataIDAnnotation.withTagRef(
                            annotations, currVG.getTag(), currVG.getRef())
                            .getNote();
                    if (mode.isOpenMode()) {
                        final String groupName = Group.getCurrentGroup()
                                .getName();
                        final String histFullName = groupName + "/"
                                + util.makeLength(hname, Histogram.NAME_LENGTH);
                        final Histogram hist = Histogram
                                .getHistogram(histFullName);
                        gate = hist == null ? null : new Gate(gname, hist);
                    } else { //reload
                        gate = Gate.getGate(util.makeLength(gname,
                                Gate.NAME_LENGTH));
                    }
                    if (gate != null) {
                        if (gate.getDimensionality() == 1) { //1-d gate
                            gate.setLimits(data.getInteger(0, 0).intValue(),
                                    data.getInteger(0, 1).intValue());
                        } else { //2-d gate
                            final Polygon shape = new Polygon();
                            for (int i = 0; i < numRows; i++) {
                                shape.addPoint(
                                        data.getInteger(i, 0).intValue(), data
                                                .getInteger(i, 1).intValue());
                            }
                            gate.setLimits(shape);
                        }
                    }
                }
            }
        }
        return numGates;
    }

    /*
     * non-javadoc: Retrieve the scalers from the file.
     * 
     * @param mode whether to open, reload or add @throws HDFException if there
     * is a problem retrieving scalers
     */
    int convertScalers(FileOpenMode mode) throws HDFException {
        int numScalers = 0;
        final VdataDescription vdd = VdataDescription.ofName(DataObject
                .ofType(DataObject.DFTAG_VH), SCALER_SECT);
        /* only the "scalers" VH (only one element) in the file */
        if (vdd != null) {
            /* get the VS corresponding to the given VH */
            final Vdata data = (Vdata) (DataObject.getObject(
                    DataObject.DFTAG_VS, vdd.getRef()));
            numScalers = vdd.getNumRows();
            for (int i = 0; i < numScalers; i++) {
                final String sname = data.getString(i, 1);
                final Scaler scaler = mode.isOpenMode() ? new Scaler(sname,
                        data.getInteger(i, 0).intValue()) : Scaler
                        .getScaler(sname);
                if (scaler != null) {
                    final int fileValue = data.getInteger(i, 2).intValue();
                    if (mode == FileOpenMode.ADD) {
                        scaler.setValue(scaler.getValue() + fileValue);
                    } else {
                        scaler.setValue(fileValue);
                    }
                }
            }
        }
        return numScalers;
    }

    /*
     * non-javadoc: retrieve the parameters from the file
     * 
     * @param mode whether to open or reload @throws HDFException if an error
     * occurs reading the parameters
     */
    int convertParameters(FileOpenMode mode) throws HDFException {
        int numParams = 0;
        final VdataDescription vdd = VdataDescription.ofName(DataObject
                .ofType(DataObject.DFTAG_VH), PARAMETERS);
        /* only the "parameters" VH (only one element) in the file */
        if (vdd != null) {
            /* Get corresponding VS for this VH */
            final Vdata data = (Vdata) (DataObject.getObject(
                    DataObject.DFTAG_VS, vdd.getRef()));
            numParams = vdd.getNumRows();
            for (int i = 0; i < numParams; i++) {
                final String pname = data.getString(i, 0);
                /* make if OPEN, retrieve if RELOAD */
                final DataParameter param = mode.isOpenMode() ? new DataParameter(
                        pname)
                        : DataParameter.getParameter(pname);
                if (param != null) {
                    param.setValue(data.getFloat(i, 1).floatValue());
                }
            }
        }
        return numParams;
    }
    
    /*
     * non-javadoc: Create a histgram given all the attributes and data
     */
    private void createHistogram(FileOpenMode mode, String name, String title,
            int number, byte histNumType, int histDim, int sizeX, int sizeY,
            ScientificData sciData, ScientificData sdErr) throws HDFException {
        Histogram histogram;
        final StringUtilities util = StringUtilities.instance();
        if (mode.isOpenMode()) {
            if (histDim == 1) {
                if (histNumType == NumberType.INT) {
                    histogram = Histogram.createHistogram(sciData.getData1d(
                            inHDF, sizeX), name, title);
                } else { //DOUBLE
                    histogram = Histogram.createHistogram(sciData.getData1dD(
                            inHDF, sizeX), name, title);
                }
                if (sdErr != null) {
                    ((AbstractHist1D) histogram).setErrors(sdErr.getData1dD(
                            inHDF, sizeX));
                }
            } else { //2d
                if (histNumType == NumberType.INT) {
                    histogram = Histogram.createHistogram(sciData.getData2d(
                            inHDF, sizeX, sizeY), name, title);
                } else {
                    histogram = Histogram.createHistogram(sciData.getData2dD(
                            inHDF, sizeX, sizeY), name, title);
                }
            }
            histogram.setNumber(number);
        } else if (mode == FileOpenMode.RELOAD) {
            final Group group = Group.getSortGroup();
            histogram = group.getHistogram(util.makeLength(name,
                    Histogram.NAME_LENGTH));
            if (histogram != null) {
                if (histDim == 1) {
                    if (histNumType == NumberType.INT) {
                        histogram.setCounts(sciData.getData1d(inHDF, sizeX));
                    } else {
                        histogram.setCounts(sciData.getData1dD(inHDF, sizeX));
                    }
                } else { // 2-d
                    if (histNumType == NumberType.INT) {
                        histogram.setCounts(sciData.getData2d(inHDF, sizeX,
                                sizeY));
                    } else {
                        histogram.setCounts(sciData.getData2dD(inHDF, sizeX,
                                sizeY));
                    }
                }
            }
        } else if (mode == FileOpenMode.ADD) {
            final Group group = Group.getCurrentGroup();
            histogram = group.getHistogram(util.makeLength(name,
                    Histogram.NAME_LENGTH));
            if (histogram != null) {
                if (histDim == 1) {
                    if (histNumType == NumberType.INT) {
                        histogram.addCounts(sciData.getData1d(inHDF, sizeX));
                    } else {
                        histogram.addCounts(sciData.getData1dD(inHDF, sizeX));
                    }
                } else { // 2-d
                    if (histNumType == NumberType.INT) {
                        histogram.addCounts(sciData.getData2d(inHDF, sizeX,
                                sizeY));
                    } else {
                        histogram.addCounts(sciData.getData2dD(inHDF, sizeX,
                                sizeY));
                    }
                }
            }
        }
    }
    
    
}
