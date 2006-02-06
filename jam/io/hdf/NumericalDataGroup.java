package jam.io.hdf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Class to represent an HDF <em>Numerical Data Group</em> data object.
 * 
 * @version 0.5 December 98
 * @author Dale Visser
 * @since JDK1.1
 */
final class NumericalDataGroup extends AbstractData {

    /**
     * List of data elements this NDG ties together.
     */
    private transient List<AbstractData> elements;

    NumericalDataGroup() {
        super(DFTAG_NDG); //sets tag
        elements = Collections.synchronizedList(new ArrayList<AbstractData>());
    }

    /**
     * Should be called whenever a change is made to the contents of the NDG.
     */
    protected void refreshBytes() {
        final int numBytes = 4 * elements.size();
        /* see DFTAG_NDG specification for HDF 4.1r2 */
        bytes = ByteBuffer.allocate(numBytes);
        for (final Iterator temp = elements.iterator(); temp.hasNext();) {
            final AbstractData dataObject = (AbstractData) (temp.next());
            bytes.putShort(dataObject.getTag());
            bytes.putShort(dataObject.getRef());
        }
    }

    /**
     * Implementation of <code>DataObject</code> abstract method.
     */
    public void interpretBytes() {
        bytes.rewind();
        /* 2 for each tag, 2 for each ref */
        final int numItems = bytes.capacity() / 4;
        elements = new ArrayList<AbstractData>(numItems);
        for (int i = 0; i < numItems; i++) {
            final short tagType = bytes.getShort();
            final short reference = bytes.getShort();
            /* look up tag/ref in file and add object */
            addDataObject(getObject(tagType, reference));
        }
    }

    /**
     * Adds the data element to the NDG.
     * 
     * @param data
     *            data element to be added
     */
    void addDataObject(final AbstractData data) {
        elements.add(data);
    }

    /*
     * non-javadoc: Passes the internal vector back.
     */
    List<AbstractData> getObjects() {
        return elements;
    }
    
    public String toString(){
        final StringBuffer rval=new StringBuffer();
        rval.append("NDG(");
        final Iterator iterator = elements.iterator();
        while (iterator.hasNext()){
            rval.append(iterator.next().toString());
            if (iterator.hasNext()){
                rval.append(", ");
            }
        }
        rval.append(")");
        return rval.toString();
    }
}