package jam.plot.color;
import jam.plot.Scale;

import java.awt.Color;
/**
 * @author Dale Visser
 *
 * Smoothly varying rainbow color scale.
 */
public final class GradientColorScale implements ColorScale{
		
	private double min,max,constant;
	private boolean recalculateConstant=true;
	private boolean logScale;//linear if false, log if true
		
	/**
	 * Create a gradient color scale.
	 *  
	 * @param min minimum counts for scale
	 * @param max maximum counts for scale
	 * @param scale whether linear or logarithmic
	 */
	public GradientColorScale(double min, double max, Scale scale){
		if (min > max) {
			setMaxCounts(min);
			setMinCounts(max);
		} else {
			setMaxCounts(max);
			setMinCounts(min);
		}
		logScale = (scale==Scale.LOG);
	}
		
	private void setMaxCounts(double maxCounts){
		max=maxCounts;
		recalculateConstant=true;
	}
		
	private void setMinCounts(double minCounts){
		min=Math.max(1.0,minCounts);
		recalculateConstant=true;
	}
		
	public Color getColor(double counts){
		return returnRGB(getScaleValue(counts)); 
	}
		
	private void calculateConstant(){
		if (logScale){
			constant=1.0/Math.log(max/min);
		} else {
			constant=1.0/(max-min);
		}
		recalculateConstant=false;
	}
	
	private double getScaleValue(double counts){
		double normValue=0.0;
		if(counts!=0.0){
			if (recalculateConstant){
				calculateConstant();
			}
			if (logScale){
				normValue = constant*Math.log(counts);
			} else {
				normValue = constant*(counts-min);
			}
		}
		return normValue;
	}
		
	private static final double X0R = 0.8;
	private static final double X0G = 0.6;
	private static final double X0B = 0.2;
	private static final double ARED = 0.25;
	private static final double AGREEN = 0.16;
	private static final double ABLUE = 0.09;
	private Color returnRGB (double x) {
		int red = (int) (255*Math.exp( -(x-X0R)*(x-X0R)/ARED ));
		int green = (int) (255*Math.exp( -(x-X0G)*(x-X0G)/AGREEN ));
		int blue = (int) (255*Math.exp( -(x-X0B)*(x-X0B)/ABLUE ));
		return new Color(red,green,blue);   
	}
		
    		
}
