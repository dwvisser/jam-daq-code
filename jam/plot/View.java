/*
 * Created on Nov 3, 2004
 *
 */
package jam.plot;

import jam.data.Histogram;
import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * @author Ken Swartz
 *
 */
public class View {

	private final int NAME_LENGTH = 20;
	
	private final String name;
	
	private final int nRows;
	
	private final int nCols;
	
	private final String [] histogramNames;
	
	private final static List viewNameList;
	
	private final static Map viewMap;
	
	static {
		viewNameList= new ArrayList();
		viewMap = new TreeMap();
	}
	
	public View(String viewName, int rows, int cols){	
		if (rows < 1) {
			throw new IllegalArgumentException("Can't have a view with " + rows
					+ " rows.");
		}
		if (cols < 1) {
			throw new IllegalArgumentException("Can't have a view with " + cols
					+ " columns.");
		}
		String tempName=viewName;
		nRows=rows;
		nCols=cols;
		String addition;
		StringUtilities su = StringUtilities.instance();
		int prime;
		final int numHists=rows*cols;
		histogramNames= new String[numHists];
		prime=1;
		addition="";
		while (viewMap.containsKey(tempName)) {
			addition = "[" + prime + "]";
			tempName = su.makeLength(tempName, NAME_LENGTH - addition.length())
			       + addition;			
			prime++;			
		}
		name=tempName;
		viewMap.put(name, this);
		viewNameList.add(name);
	}
	
	public static Iterator getNameIterator(){
		return Collections.unmodifiableList(viewNameList).iterator();
	}
	
	public static View getView(String name){
		return (View)viewMap.get(name);
	}
	
	/**
	 * Get the number of rows
	 * @return rows
	 */
	int getRows(){
		return nRows;
	}
	
	/**
	 * Get the number of columns
	 * @return columns
	 */
	int getColumns(){
		return nCols;
	}
	
	/**
	 * Get the number of histogram plots.
	 * @return the number of plots
	 */
	int getNumberHists(){
		return histogramNames.length;
	}
	
	/**
	 * Returns the histogram associatied with the given plot.
	 * 
	 * @param num which plot
	 * @return histogram for the given plot
	 */
	Histogram getHistogram(int num){
		return Histogram.getHistogram(histogramNames[num]);
	}
	
	/**
	 * Associates the given histogram with the given plot.
	 * 
	 * @param num which plot
	 * @param histIn the Histogram
	 */
	void setHistogram(int num, Histogram histIn){
		if (histIn!=null)
			histogramNames[num]=histIn.getName();
	}
}
