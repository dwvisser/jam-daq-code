package jam.plot;

import jam.data.Histogram;
import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Represents the number and arrangements of plots to show within
 * the <code>Display</code>.
 * 
 * @author Ken Swartz
 * @version 2004-11-03
 * @see Display
 */
public final class View {

	private final static List viewNameList;
	
	private final static Map viewMap;	

	static {
		viewNameList= new ArrayList();
		viewMap = new TreeMap();
	}
	
	public static final View SINGLE=new View("Single", 1,1) ;
	
	private final int NAME_LENGTH = 20;
	
	private final String name;
	
	private final int nRows;
	
	private final int nCols;
	
	private final String [] histogramNames;
	
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
		StringUtilities su = StringUtilities.instance();
		int prime;
		final int numHists=rows*cols;
		histogramNames= new String[numHists];
		prime=1;
		while (viewMap.containsKey(tempName)) {
			final String addition = "[" + prime + "]";
			tempName = su.makeLength(tempName, NAME_LENGTH - addition.length())
			       + addition;			
			prime++;			
		}
		name=tempName;
		addView(name,this);
	}
	
	private static synchronized void addView(String name, View view){
		viewMap.put(name,view);
		viewNameList.add(name);
	}
	
	public static List getNameList(){
		return Collections.unmodifiableList(viewNameList);
	}
	
	public static synchronized View getView(String name){
		return (View)viewMap.get(name);
	}
	
	public static synchronized void removeView(String name){
		viewMap.remove(name);
		viewNameList.remove(name);
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
		final String name = histIn==null ? null : histIn.getName();
		histogramNames[num]=name;
	}
	
	public String getName(){
		return name;
	}
}
