/*
 * Created on Nov 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jam.plot;

import jam.data.Histogram;
import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.TreeMap;


/**
 * @author ken
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class View {

	private final int NAME_LENGTH = 20;
	
	private String name;
	
	private int nRows;
	
	private int nCols;
	
	private int numHists;
	
	private Histogram [] histograms;
	
	private final static List viewNameList;
	
	private final static Map viewMap;
	
	static {
		viewNameList= new ArrayList();
		viewMap = new TreeMap();
	}
	
	public View(String name, int nRows, int nCols){		
		this.name=name;
		this.nRows=nRows;
		this.nCols=nCols;
				

		String addition;
		StringUtilities su = StringUtilities.instance();
		int prime;
		
		numHists=nRows*nCols;
		
		histograms= new Histogram[numHists];
		
		prime=1;
		addition="";
		while (viewMap.containsKey(name)) {
			addition = "[" + prime + "]";
			name = su.makeLength(name, NAME_LENGTH - addition.length())
			       + addition;			
			prime++;			
		}
		
		viewMap.put(name, this);
		viewNameList.add(name);
		
	}
	
	public static Iterator getNameIterator(){
		return viewNameList.iterator();
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
	 * @return rows
	 */
	int getColumns(){
		return nRows;
	}
	/**
	 * Get the number of plots
	 * @return rows
	 */
	int getNumberHists(){
		return numHists;
	}
	/**
	 * Get the number of plots
	 * @return rows
	 */
	Histogram getHistogram(int num){
		return histograms[num];
	}
	/**
	 * Get the number of plots
	 * @return rows
	 */
	void setHistogram( int num, Histogram histIn){
		histograms[num]=histIn;
	}
	
}
