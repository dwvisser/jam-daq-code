/*
 * PeakFinder.java
 *
 * Created on May 11, 2001, 3:45 PM
 */

package jam.data.peaks;
import java.util.Vector;
import jam.data.Histogram;

/**
 * Given sensitivity and width parameters, finds peaks in a given spectrum.
 * 
 * @author  <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public class PeakFinder extends Object {

    //static private Multiplet [] multiplets;
    //public String name;

    static private double [] spectrum;
    static private double sensitivity, width;

    /** Maximum separation in sigma between peaks to count them as being in the same
     * multiplet.
     */
    static final double MAX_SEP=1.3;

    /** When given the standard deviation for a gaussian peak, multiplying by this
     * gives the full width at half maximum height.
     */
    static final double SIGMA_TO_FWHM = 2.354;

    /** Creates new PeakFinder.
     * 
     */
    public PeakFinder() {
    	//default constructor
    }
    
    /*     
     * Given a spectrum and search parameters, performs a digital filter peak search as
     * specified in V. Hnatowicz et al in Comp Phys Comm 60 (1990) 111-125.  Setting
     * the sensitivity to a typical value of 3 gives a 3% chance for any peak found
     * to be false.
     * 
     * @param spectrum spectrum to be searched
     * @param sensitivity larger numbers (typical=3) require better defined peaks
     * @param width typical FWHM of peaks in spectrum
     */
	static public double [] getCentroids(double [] data, double _sensitivity,
    double _width){
        spectrum=data;
        sensitivity=_sensitivity;
        width=_width;
        return getCentroids();
    }

    /**
     * Workhorse for peak-finding.
     * @return an array of the multiplets found in the spectrum
     */
    static private Multiplet [] peakFind(){
        double maxSeparation=MAX_SEP;
        Multiplet peaks = new Multiplet();
        Multiplet currentMultiplet=new Multiplet();
        Vector multiplets = new Vector();
        Multiplet [] rval;
        double [] SC = new double[spectrum.length];//defined by Java spec to be zeroes initially
        double [] SCC = new double[spectrum.length];
        int filter_limit=(int)Math.ceil(1.5*width);//gives filter at limit < 0.005 filter at center
        double [] filter = new double[2*filter_limit+1];
        double [] filter2 = new double[2*filter_limit+1]; //will contain the squares of filter's elements
        double sigma=width/SIGMA_TO_FWHM;
        // Create the filter and its square.
        for (int i=0; i < filter.length; i++) {
            int k = i-filter_limit;
            filter[i] = 2*(sigma*sigma - k*k)/(Math.sqrt(Math.PI)*sigma*sigma*sigma)*
            Math.exp(-(k*k)/(2.0*sigma*sigma));
            filter2[i] = filter[i] * filter[i];
        }
        //Run the filter on the spectrum. (Eqns 2 in article)
        for (int i=filter_limit; i < spectrum.length-filter_limit; i++){
            for (int j = 0; j < filter.length; j++){
                int l=j-filter_limit;
                SC[i] += filter[j] * spectrum[i-l];
                SCC[i] += filter2[j] * spectrum[i-l];
            }
        }
        //Build list of peak candidates
        for (int i=filter_limit+1; i < spectrum.length-filter_limit-1; i++){
            if (SC[i] > sensitivity*Math.sqrt(SCC[i]) &&
            SC[i] > SC[i-1] &&
            SC[i] > SC[i+1]) {//conditions met, calculate centroid
                double posn = (SC[i-1]*(i-1)+SC[i]*i+SC[i+1]*(i+1))/(SC[i-1]+SC[i]+SC[i+1]);
                peaks.addPeak(new Peak(posn, SC[i],width));
            }
        }
        //break into multiplets
        for (int i=0; i<peaks.size(); i++){
            if (i==0) {
                multiplets.addElement(currentMultiplet);
                currentMultiplet.addPeak(peaks.getPeak(i));
            } else {
                if ((peaks.getPeak(i).getPosition()-peaks.getPeak(i-1).getPosition()) > (maxSeparation*width)){
                    currentMultiplet=new Multiplet();
                    multiplets.addElement(currentMultiplet);
                    currentMultiplet.addPeak(peaks.getPeak(i));
                } else {//add current peak to working multiplet
                    currentMultiplet.addPeak(peaks.getPeak(i));
                }
            }
        }
        //create return value array and correct peak positions within multiplets
        rval = new Multiplet[multiplets.size()];
        for (int i = 0; i< rval.length; i++) {
            rval[i]=(Multiplet)multiplets.elementAt(i);
            if (rval[i].size()>1){
                Multiplet temp=new Multiplet();
                for (int j=0; j<rval[i].size(); j++){
                    if (j==0){
                        Peak thisPeak = rval[i].getPeak(j);
                        Peak nextPeak = rval[i].getPeak(j+1);
                        double Dnext = thisPeak.getPosition()-nextPeak.getPosition();
                        double Knext = Dnext * Math.exp(-Dnext*Dnext/(4.0*sigma*sigma)) *
                        (1.0 - Dnext*Dnext/(6.0 * sigma * sigma));
                        double psnCorrection = nextPeak.getArea()*Knext/thisPeak.getArea();
                        temp.addPeak(new Peak(thisPeak.getPosition()+psnCorrection, thisPeak.getArea(),
                        thisPeak.getWidth()));
                    } else if (j == (rval[i].size()-1)) {
                        Peak thisPeak = rval[i].getPeak(j);
                        Peak lastPeak = rval[i].getPeak(j-1);
                        double Dlast = thisPeak.getPosition()-lastPeak.getPosition();
                        double Klast = Dlast * Math.exp(-Dlast*Dlast/(4.0*sigma*sigma)) *
                        (1.0 - Dlast*Dlast/(6.0 * sigma * sigma));
                        double psnCorrection = lastPeak.getArea()*Klast/thisPeak.getArea();
                        temp.addPeak(new Peak(thisPeak.getPosition()+psnCorrection, thisPeak.getArea(),
                        thisPeak.getWidth()));
                    } else {//in the middle somewhere
                        Peak thisPeak = rval[i].getPeak(j);
                        Peak nextPeak = rval[i].getPeak(j+1);
                        double Dnext = thisPeak.getPosition()-nextPeak.getPosition();
                        double Knext = Dnext * Math.exp(-Dnext*Dnext/(4.0*sigma*sigma)) *
                        (1.0 - Dnext*Dnext/(6.0 * sigma * sigma));
                        Peak lastPeak = rval[i].getPeak(j-1);
                        double Dlast = thisPeak.getPosition()-lastPeak.getPosition();
                        double Klast = Dlast * Math.exp(-Dlast*Dlast/(4.0*sigma*sigma)) *
                        (1.0 - Dlast*Dlast/(6.0 * sigma * sigma));
                        double psnCorrection = (nextPeak.getArea()*Knext+lastPeak.getArea()*Klast)/
                        thisPeak.getArea();
                        temp.addPeak(new Peak(thisPeak.getPosition()+psnCorrection, thisPeak.getArea(),
                        thisPeak.getWidth()));
                    }
                }
                rval[i]=temp;
            }
        }
        //now renormalize multiplet amplitudes
        for (int m=0; m < rval.length; m++){
            double trueArea=0.0;
            double estArea=0.0;
            for (int p=0; p<rval[m].size(); p++) {
                estArea += rval[m].getPeak(p).getArea();
            }
            for (int ch = (int)Math.round(rval[m].getPeak(0).getPosition()-width*maxSeparation);
            ch < (int)Math.round(rval[m].getPeak(rval[m].size()-1).getPosition()+width*maxSeparation);
            ch++) {
                trueArea += spectrum[ch];
            }
            double factor = trueArea/estArea;
            for (int p=0; p<rval[m].size(); p++) {
                double area = rval[m].getPeak(p).getArea();
                rval[m].getPeak(p).setArea(factor*area);
            }
        }
        return rval;
    }
    
    private static double [] getCentroids(){
    	Multiplet [] multiplets=peakFind();
    	int size=0;
    	for (int i=0; i<multiplets.length; i++){
    		size += multiplets[i].getAllCentroids().length;
    	}
    	double [] rval = new double[size];
 		int pos=0;
    	for (int i=0; i<multiplets.length; i++){
    		double [] source = multiplets[i].getAllCentroids();
    		System.arraycopy(source,0,rval,pos,source.length);
    		pos += source.length;
    	}
    	return rval;
    }

}