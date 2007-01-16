package jam.plot;

/**
 * Gives the number of pixels per channel for each screen
 * dimension.
 * 
 * @author Dale Visser
 */
final class Conversion {
	
	private transient final double xFactor, yFactor, yLogFactor;
	
	Conversion(double xfac, double yfac, double ylogfac) {
		super();
		xFactor = xfac;
		yFactor = yfac;
		yLogFactor = ylogfac;
	}
	
	double getX(){
		return xFactor;
	}
	
	double getY(){
		return yFactor;
	}
	
	double getYLog(){
		return yLogFactor;
	}
}