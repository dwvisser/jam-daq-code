/*
 *
 */
package jam.fit; 

/**
 * Takes 4 channels as input...limits of background, and limits of peak.  
 * Returns a background level and peak intensity.
 *
 * @author  Dale Visser
 * @author Carrie Rowland
 * @version 28 July 2002
 */
 public class PeakIntensity extends AbstractFit {
 
    /**
     * input <code>Parameter</code>
     */
    private Parameter lowChannel=new Parameter("Low Channel",Parameter.MOUSE,
    							Parameter.NO_OUTPUT,Parameter.INT,Parameter.KNOWN);
    private Parameter highChannel=new Parameter("High Channel",Parameter.MOUSE,
    							Parameter.NO_OUTPUT,Parameter.INT,Parameter.KNOWN);
    private Parameter lowPeak=new Parameter("Low Peak",Parameter.MOUSE,
    							Parameter.NO_OUTPUT,Parameter.INT,Parameter.KNOWN);
    private Parameter highPeak=new Parameter("High Peak",Parameter.MOUSE,
    							Parameter.NO_OUTPUT,Parameter.INT,Parameter.KNOWN);
    
  
    /**
     * function <code>Parameter</code>--constant background term
     */
    private Parameter paramA,paramB;
 
	private Parameter paramPeakArea, paramPeakCentroid; 

    /**
     * Class constructor.
     */
    public PeakIntensity(){
		super("Peak Intensity");
		
		Parameter comment=new Parameter("Comment", Parameter.TEXT);
		comment.setValue("Checking \"Fixed\" on Slope fixes the value to 0.");
		addParameter(lowChannel);
		addParameter(highChannel);
		addParameter(lowPeak);
		addParameter(highPeak);
		paramA=new Parameter("Constant",Parameter.DOUBLE);
		addParameter(paramA);		
		paramB=new Parameter("Slope",Parameter.DOUBLE, Parameter.FIX);	
		addParameter(paramB);	
		addParameter(comment);
		paramPeakArea=new Parameter("Peak Area",Parameter.DOUBLE);
		addParameter(paramPeakArea);	
		paramPeakCentroid=new Parameter("Peak Centroid",Parameter.DOUBLE);
		addParameter(paramPeakCentroid);	
    }
    
    /**
     *
     */
    public void estimate(){
    	//not used
    }
    
    /**
     * Performs the calibration fit.
     *
     * @return	    message for fit dialog
     * @exception   FitException	    thrown if something goes wrong in the fit
     */
    public String doFit() throws FitException{
    	double s,sx,sy,syy;
    	double var;//variance for current channel
    	double peakArea,totalArea,bkgdArea;
    	int lc=getParameter("Low Channel").getIntValue();
    	int hc=getParameter("High Channel").getIntValue();
    	int lp=getParameter("Low Peak").getIntValue();
    	int hp=getParameter("High Peak").getIntValue();
        int [] before = {lc,lp,hp,hc};
        java.util.Arrays.sort(before);
        lc = before[0]; lp = before[1]; hp = before[2]; hc = before[3];
		lowChannel.setValue(lc);
		lowPeak.setValue(lp);
		highPeak.setValue(hp);
		highChannel.setValue(hc);		
    	s=0.0;sx=0.0;sy=0.0;syy=0.0;
    	if (paramB.isFixed()){//flat background
    		for (int i=lc;i<=hc; i++) {
    			sy += counts[i];
    			if (i==lp-1) i = hp;
    		}
    		int numBackgdChannels = lp-lc+hc-hp;
    		sy /= numBackgdChannels;	
    		for (int i=lc;i<=hc; i++) {
    			syy += Math.pow(counts[i]-sy,2.0);
    			if (i==lp-1) i = hp;
    		}
    		syy /= numBackgdChannels*(numBackgdChannels-1);
    		paramA.setValue(sy,Math.sqrt(syy));
    		paramB.setValue(0.0,0.0);
    	} else {//fit a regression line
    		for (int i=lc;i<=hc; i++) {
    			var = counts[i]>0 ? counts[i] : 1.0;
    			s += 1.0/var;
    			sx += i/var;
    			sy += counts[i] / var;
    			if (i==lp-1) i = hp;
    		}
    		double xbar = sx/s;
    		double st2=0.0; double b=0.0;
    		for (int i=lc;i<=hc; i++) {
    			double sigma = counts[i]>0 ? Math.sqrt(counts[i]) : 1.0;
    			double t = (i-xbar)/sigma;
    			st2 += t*t;
    			b += t*counts[i]/sigma;
    			if (i==lp-1) i = hp;
    		}
    		b /= st2;
    		double db = 1/st2;
    		double da = Math.sqrt((1+sx*sx/(s*st2))/s);
    		double a = (sy-sx*b)/s;
    		paramA.setValue(a,da);
    		paramB.setValue(b,db);
    	}
    	totalArea=0.0; bkgdArea=0.0;
    	for(int i=lp;i<=hp;i++){
    		totalArea += counts[i];
    		bkgdArea += calculate(i);
    	}
    	peakArea = totalArea - bkgdArea;
    	double centroid=0.0;
    	for(int i=lp;i<=hp;i++){
    		centroid += i*(counts[i]-calculate(i))/peakArea;
    	}
    	double peakError = Math.sqrt(totalArea);
    	double variance=0.0;
    	for(int i=lp;i<=hp;i++){
    		double distance = i-centroid;
    		variance += (counts[i]-calculate(i))/peakArea*distance*distance;
    	}
    	variance /= hp-lp+1.0;
    	paramPeakArea.setValue(peakArea,peakError);
    	paramPeakCentroid.setValue(centroid,Math.sqrt(variance));
    	lowerLimit=lc;
    	upperLimit=hc;
    	residualOption=false;
    	return "Done.";
    }
    
    public double calculate(int channel){
	  return paramA.getDoubleValue()+paramB.getDoubleValue()*(channel);
    }
    
    double calculateBackground(int channel){
    	return calculate(channel);
    }
    
    boolean hasBackground(){
    	return true;
    }
    
    int getNumberOfSignals(){
    	return 0;
    }
    
    double calculateSignal(int signal, int channel){
    	return 0.0;
    }
    
}