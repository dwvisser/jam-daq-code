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
final class NumericalDataGroup extends DataObject {

    /**
     * List of data elements this NDG ties together.
     */
    private List elements;

    NumericalDataGroup() {
        super(DFTAG_NDG); //sets tag
        elements = Collections.synchronizedList(new ArrayList());
    }

    /**
     * Should be called whenever a change is made to the contents of the NDG.
     */
    protected void refreshBytes() {

        final int numBytes = 4 * elements.size();
        /* see DFTAG_NDG specification for HDF 4.1r2 */
        bytes = ByteBuffer.allocate(numBytes);
        for (final Iterator temp = elements.iterator(); temp.hasNext();) {
            final DataObject dataObject = (DataObject) (temp.next());
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
        elements = new ArrayList(numItems);
        for (int i = 0; i < numItems; i++) {
            final short tag = bytes.getShort();
            final short ref = bytes.getShort();
            /* look up tag/ref in file and add object */
            addDataObject(getObject(tag, ref));
        }
    }

    /**
     * Adds the data element to the NDG.
     * 
     * @param data
     *            data element to be added
     */
    void addDataObject(DataObject data) {
        elements.add(data);
    }

    /*
     * non-javadoc: Passes the internal vector back.
     */
    List getObjects() {
        return elements;
    }
}