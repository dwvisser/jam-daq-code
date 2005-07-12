/*
 * Multiplet.java
 *
 * Created on February 14, 2001, 1:30 PM
 */

package jam.data.peaks;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Represents a group of peaks in a spectrum.
 * 
 * @author  <a href="mailto:dale@visser.name">Dale Visser</a>
 */
final class Multiplet extends Vector<Peak> {
	
	Multiplet(){
		super();
	}

    List<Double> getAllCentroids(){
        final List<Double> centroids=new ArrayList<Double>();
        for (Peak peak : this){
            centroids.add(peak.getPosition());
        }
        return centroids;
    }

    static Multiplet combineMultiplets(final Multiplet [] marray){
        final Multiplet rval = new Multiplet();
        for (int i=0; i<marray.length; i++){
            rval.addAll(marray[i]);
        }
        return rval;
    }

    static Multiplet combineMultiplets(final Multiplet mult0, final Multiplet mult1){
        Multiplet [] temp = new Multiplet[2];
        temp[0]=mult0;
        temp[1]=mult1;
        return combineMultiplets(temp);
    }

    /*void addPeak(Peak p) {
        if (p != null){
            addElement(p);
        }
    }*/
    
    /*Peak getPeak(int index) {
        return elementAt(index);
    }*/
}