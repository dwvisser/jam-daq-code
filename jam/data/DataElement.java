package jam.data;

/**
 * Interface for all data objects
 * 
 * @author Ken Swartz
 */
public interface DataElement {

	public static final int ELEMENT_TYPE_HISTOGRAM=0;
	
	public static final int ELEMENT_TYPE_GATE=1;
	
	public static final int ELEMENT_TYPE_SCALER=2;
	
    public String getName();
    
    public int getCount();
    
    public int getElementType();
	
}
