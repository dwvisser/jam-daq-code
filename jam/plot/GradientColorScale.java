package jam.plot;
import java.awt.Color;
/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
class GradientColorScale implements ColorScale{
		
	private double min,max,constant;
	private boolean recalculateConstant=true;
	private boolean logScale;//linear if false, log if true
		
	public GradientColorScale(double min, double max, Limits.ScaleType scale){
		if (min > max) {
			setMaxCounts(min);
			setMinCounts(max);
		} else {
			setMaxCounts(max);
			setMinCounts(min);
		}
		logScale = (scale==Limits.ScaleType.LOG);
	}
		
	public void setMaxCounts(double mc){
		max=mc;
		recalculateConstant=true;
	}
		
	public void setMinCounts(double mc){
		min=Math.max(1.0,mc);
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
		
	private static final double x0R = 0.8;
	private static final double x0G = 0.6;
	private static final double x0B = 0.2;
	private static final double aR = 0.25;
	private static final double aG = 0.16;
	private static final double aB = 0.09;
	private Color returnRGB (double x) {
		int red = (int) (255*Math.exp( -(x-x0R)*(x-x0R)/aR ));
		int green = (int) (255*Math.exp( -(x-x0G)*(x-x0G)/aG ));
		int blue = (int) (255*Math.exp( -(x-x0B)*(x-x0B)/aB ));
		return new Color(red,green,blue);   
	}
		
    		
}
