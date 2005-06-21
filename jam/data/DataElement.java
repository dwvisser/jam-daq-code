package jam.data;

/**
 * Interface for all data objects
 * 
 * @author Ken Swartz
 */
public interface DataElement {

	/**
	 * Possible types of data elements.
	 * @author Dale Visser
	 */
	enum Type{GATE, HISTOGRAM, SCALER};
		
	/**
     * 
     * @return ???
     */
    double getCount();
    
    /**
     * 
     * @return which kind of element this is
     */
    Type getElementType();
    
    /**
     * 
     * @return name of the element
	 */
    String getName();
	
}
