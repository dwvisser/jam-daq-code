package jam.plot;

/**
 * Interface for a object that can be 
 * displayed  
 */
public interface  Displayable   {

    //FIXME in more then one place
    public static final int ONE_DIMENSION=1;       

    public static final int TWO_DIMENSION=2;   
    
    /**
     *	Name of displayable object
     */
    public String getName();

    /**
     *	Displable title
     */ 			        
    public String getTitle();
    
    /**
     * get X axis label
     */    
    public String getLabelX();
    
    /**
     * get Y axis label
     */    
    public String getLabelY();
    
    /**
     *	type of data 1 d or 2 d
     */ 			        
    public int getType();
    
    /**
     *	size in X
     */ 			            

    public int getSizeX(); 

    /**
     *	size in Y
     */ 			            
    public int getSizeY();

    /**
     *	counts if 1d
     */ 			        
    public int [] getCounts();
    
    /**
     *	counts if 2d
     */ 			        
    public int [][] getCounts2d();	
}    

