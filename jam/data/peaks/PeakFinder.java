package jam.data.peaks;
import jam.fit.GaussianConstants;

import java.util.Vector;

/**
 * Given sensitivity and width parameters, finds peaks in a given spectrum.
 * 
 * @author  <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2001-05-11
 */
public class PeakFinder {

    static private double [] spectrum;
    static private double sensitivity, width;

    /** Maximum separation in sigma between peaks to count them as being in the same
     * multiplet.
     */
    static final double MAX_SEP=1.3;

    /**     
     * Given a spectrum and search parameters, performs a digital filter peak search as
     * specified in V. Hnatowicz et al in Comp Phys Comm 60 (1990) 111-125.  Setting
     * the sensitivity to a typical value of 3 gives a 3% chance for any peak found
     * to be false.
     * 
     * @param data spectrum to be searched
     * @param _sensitivity larger numbers (typical=3) require better defined peaks
     * @param _width typical FWHM of peaks in spectrum
     * @return array of centroids
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
        Multiplet peaks = new Multiplet();
        Multiplet current=new Multiplet();
        Vector multiplets = new Vector();
        Multiplet [] rval;
        double [] sum1 = new double[spectrum.length];//defined by Java spec to be zeroes initially
        double [] sum2 = new double[spectrum.length];
        int filterLimit=(int)Math.ceil(1.5*width);//gives filter at limit < 0.005 filter at center
        double [] filter = new double[2*filterLimit+1];
        double [] filter2 = new double[2*filterLimit+1]; //will contain the squares of filter's elements
        double sigma=width/GaussianConstants.SIG_TO_FWHM;
        // Create the filter and its square.
        for (int i=0; i < filter.length; i++) {
            final int iPrime = i-filterLimit;
            filter[i] = 2*(sigma*sigma - iPrime*iPrime)/(Math.sqrt(Math.PI)*sigma*sigma*sigma)*
            Math.exp(-(iPrime*iPrime)/(2.0*sigma*sigma));
            filter2[i] = filter[i] * filter[i];
        }
        //Run the filter on the spectrum. (Eqns 2 in article)
        for (int i=filterLimit; i < spectrum.length-filterLimit; i++){
            for (int j = 0; j < filter.length; j++){
                int l=j-filterLimit;
                sum1[i] += filter[j] * spectrum[i-l];
                sum2[i] += filter2[j] * spectrum[i-l];
            }
        }
        //Build list of peak candidates
        for (int i=filterLimit+1; i < spectrum.length-filterLimit-1; i++){
            if (sum1[i] > sensitivity*Math.sqrt(sum2[i]) &&
            sum1[i] > sum1[i-1] &&
            sum1[i] > sum1[i+1]) {//conditions met, calculate centroid
                double posn = (sum1[i-1]*(i-1)+sum1[i]*i+sum1[i+1]*(i+1))/(sum1[i-1]+sum1[i]+sum1[i+1]);
                peaks.addPeak(new Peak(posn, sum1[i],width));
            }
        }
        //break into multiplets
        for (int i=0; i<peaks.size(); i++){
            if (i==0) {
                multiplets.addElement(current);
                current.addPeak(peaks.getPeak(i));
            } else {
                if ((peaks.getPeak(i).getPosition()-peaks.getPeak(i-1).getPosition()) > (MAX_SEP*width)){
                    current=new Multiplet();
                    multiplets.addElement(current);
                    current.addPeak(peaks.getPeak(i));
                } else {//add current peak to working multiplet
                    current.addPeak(peaks.getPeak(i));
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
                        final Peak thisPeak = rval[i].getPeak(j);
                        final Peak nextPeak = rval[i].getPeak(j+1);
                        final double dNext = thisPeak.getPosition()-nextPeak.getPosition();
                        final double kNext = dNext * Math.exp(-dNext*dNext/(4.0*sigma*sigma)) *
                        (1.0 - dNext*dNext/(6.0 * sigma * sigma));
                        final double correction = nextPeak.getArea()*kNext/thisPeak.getArea();
                        temp.addPeak(new Peak(thisPeak.getPosition()+correction, thisPeak.getArea(),
                        thisPeak.getWidth()));
                    } else if (j == (rval[i].size()-1)) {
                        final Peak thisPeak = rval[i].getPeak(j);
                        final Peak lastPeak = rval[i].getPeak(j-1);
                        final double dLast = thisPeak.getPosition()-lastPeak.getPosition();
                        final double kLast = dLast * Math.exp(-dLast*dLast/(4.0*sigma*sigma)) *
                        (1.0 - dLast*dLast/(6.0 * sigma * sigma));
                        final double correction = lastPeak.getArea()*kLast/thisPeak.getArea();
                        temp.addPeak(new Peak(thisPeak.getPosition()+correction, thisPeak.getArea(),
                        thisPeak.getWidth()));
                    } else {//in the middle somewhere
                        final Peak thisPeak = rval[i].getPeak(j);
                        final Peak nextPeak = rval[i].getPeak(j+1);
                        final double dNext = thisPeak.getPosition()-nextPeak.getPosition();
                        final double kNext = dNext * Math.exp(-dNext*dNext/(4.0*sigma*sigma)) *
                        (1.0 - dNext*dNext/(6.0 * sigma * sigma));
                        final Peak lastPeak = rval[i].getPeak(j-1);
                        final double dLast = thisPeak.getPosition()-lastPeak.getPosition();
                        final double kLast = dLast * Math.exp(-dLast*dLast/(4.0*sigma*sigma)) *
                        (1.0 - dLast*dLast/(6.0 * sigma * sigma));
                        final double correction = (nextPeak.getArea()*kNext+lastPeak.getArea()*kLast)/
                        thisPeak.getArea();
                        temp.addPeak(new Peak(thisPeak.getPosition()+correction, thisPeak.getArea(),
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
            for (int ch = (int)Math.round(rval[m].getPeak(0).getPosition()-width*MAX_SEP);
            ch < (int)Math.round(rval[m].getPeak(rval[m].size()-1).getPosition()+width*MAX_SEP);
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