package jam.plot;

/**
 * Interface for a object that can be 
 * displayed by <code>Display</code>.
 * 
 * @author Ken Swartz
 * @version 1.0
 * @see Display
 */
public interface  Displayable   {
    
    /**
     *	@return name of displayable object
     */
    String getName();

    /**
     * @return title of displayable object
     */ 			        
    String getTitle();
    
    /**
     * @return X axis label
     */    
    String getLabelX();
    
    /**
     * @return Y axis label
     */    
    String getLabelY();
    
    /**
     * @return dimensionality of data, <code>1</code> or 
     * <code>2</code>
     */ 			        
    int getType();
    
    /**
     * @return size in X
     */ 			            
    int getSizeX(); 

    /**
     * @return size in Y
     */ 			            
    int getSizeY();

    /**
     *	@return counts if 1d
     */ 			        
    int [] getCounts();
    
    /**
     * @return counts if 2d
     */ 			        
    int [][] getCounts2d();	
}    

