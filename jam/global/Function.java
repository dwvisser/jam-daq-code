package jam.global;
/**
 * This interface defines methods for a real-valued function of one variable.
 */
public interface  Function  {

    /**
     *	Returns the value of the function at the given x-value.
     *
     * @param	x   where to evaluate the function
     * @return	the value of the function at the given x-value
     */ 			        
    public double getValue(int x);

    /**
     *	Returns the value of the function at the given x-value.
     *
     * @param	x   where to evaluate the function
     * @return	the value of the function at the given x-value
     */ 			        
    public double getValue(double x);
    
}    