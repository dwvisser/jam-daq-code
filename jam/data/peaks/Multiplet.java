/*
 * Multiplet.java
 *
 * Created on February 14, 2001, 1:30 PM
 */

package jam.data.peaks;
import java.util.Vector;

/**
 * Represents a group of peaks in a spectrum.
 * 
 * @author  <a href="mailto:dale@visser.name">Dale Visser</a>
 */
final class Multiplet extends Vector {

    private Peak [] getAllPeaks(){
        final Peak [] rval=new Peak[size()];
        toArray(rval);
        return rval;
    }

    double [] getAllCentroids(){
        Peak [] peaks=getAllPeaks();
        double [] centroids=new double[peaks.length];
        if (peaks.length >0){
            for (int i=0;i<peaks.length;i++){
                centroids[i]=peaks[i].getPosition();
            }
        }
        return centroids;
    }

    private void addMultiplet(Multiplet madd){
        addAll(madd);
    }

    static Multiplet combineMultiplets(Multiplet [] marray){
        Multiplet rval = new Multiplet();
        for (int i=0; i<marray.length; i++){
            rval.addMultiplet(marray[i]);
        }
        return rval;
    }

    static Multiplet combineMultiplets(Multiplet m0, Multiplet m1){
        Multiplet [] temp = new Multiplet[2];
        temp[0]=m0;
        temp[1]=m1;
        return combineMultiplets(temp);
    }

    void addPeak(Peak p) {
        if (p != null){
            addElement(p);
        }
    }
    
    Peak getPeak(int index) {
        return (Peak)elementAt(index);
    }
}