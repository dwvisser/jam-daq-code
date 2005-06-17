package jam.data;

/**
 * Interface for all data objects
 * 
 * @author Ken Swartz
 */
public interface DataElement {

    /**
     * Means the element is a gate.
     */
	int ELEMENT_TYPE_GATE=1;
	
    /**
     * Means the element is a histogram.
     */
	int ELEMENT_TYPE_HISTOGRAM=0;
	
    /**
     * Means the element is a scaler.
     */
	int ELEMENT_TYPE_SCALER=2;
		
	/**
     * 
     * @return ???
     */
    double getCount();
    
    /**
     * 
     * @return which kind of element this is
     */
    int getElementType();
    
    /**
     * 
     * @return name of the element
	 */
    String getName();
	
}
