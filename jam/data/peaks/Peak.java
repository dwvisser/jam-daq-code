package jam.data.peaks;

/** This class represents a gaussian peak, in terms of it's properties.  Fields are
 * also provided for the error bars on these properties.
 * 
 * @author  <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2001-02-14
 */
final class Peak extends Object implements Comparable {

    private double position, area, width;
    private double perr, aerr, werr;

    /** Creates new Peak  assuming no uncertainty in values.
     * @param position position of the peak centroid
     * @param area total peak area
     * @param width Full width at half max of the peak
     */
    Peak(double position, double area, double width) {
        this(position, 0.0, area, 0.0, width, 0.0);
    }

    /** Generates a peak with error bars on its parameters.
     * @param p position of peak centroid
     * @param pe error on position
     * @param a area of peak
     * @param ae uncertainty in area
     * @param w FWHM of peak
     * @param we uncertainty in FWHM
     */
    private Peak(double p,double pe, double a, double ae,  double w, double we) {
        setPosition(p,pe);
        setArea(a,ae);
        setWidth(w,we);
    }

    /**
     * @return  centroid of peak
     */
    double getPosition() {
        return position;
    }

    double getArea() {
        return area;
    }

    double getWidth() {
        return width;
    }

    private final void setPosition(double p, double e){
        position=p;
        perr=e;
    }

    void setArea(double a){
        setArea(a,0.0);
    }

    private final void setArea(double a, double e){
        area=a;
        aerr=e;
    }

    private final void setWidth(double w, double e){
        width=w;
        werr=e;
    }

    public String toString() {
        String rval = "Peak\n";
        rval += "  Position = "+position+" +/- "+perr+"\n";
        rval += "  Area = "+area+" +/- "+aerr+"\n";
        rval += "  FWHM = "+width+" +/- "+werr+"\n";
        return rval;
    }

    public int compareTo(Object p1) {
    	int rval=0;//default return value
        if (getPosition() < ((Peak)p1).getPosition()){
            rval = -1;
        } else if (getPosition() > ((Peak)p1).getPosition()){
            rval = 1;
        } 
        return rval;
    }
            
}