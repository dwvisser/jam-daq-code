package jam.data.control;

import java.awt.FontMetrics;
import java.util.Iterator;

import javax.swing.JComboBox;

import jam.data.AbstractHist1D;
import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;

/**
 * Base class for histogram manipulations
 * has methods to create combo box of histograms
 * 
 * @author Ken Swartz
 *
 */
public abstract class AbstractManipulation extends AbstractControl {

	final int CHOOSER_SIZE = 200;
	final int CHOOSER_CHAR_LENGTH = 35;
	final int NEW_NAME_LENGTH=15;
	
	/** String to prepend to new histogram group in combobox */ 
	final static String NEW_HIST = "NEW: ";
	
	/** String to append to new histogram group in combobox */	
	private final static String WILD_CARD="/.";	
	
	/**
	 * Constructs a dialog box for manipulation of histograms.
	 * 
	 * @param title if dialog
	 * @param modal whether to grab focus from parent window
	 */
	public AbstractManipulation(String title, boolean modal){
		super(title, modal);		
	}
	
	/* non-javadoc:
	 * add histograms of type type1 and type2 to chooser
	 */
	void loadAllHists(JComboBox comboBox, boolean addNew, int histDim) {
		comboBox.removeAllItems();
		if(addNew) {
			//Add working group new
			comboBox.addItem(NEW_HIST+Group.WORKING_NAME+WILD_CARD);
			//Add new histograms
			for (final Iterator iter = Group.getGroupList().iterator();iter.hasNext();) {
				final Group group = (Group)iter.next();
				if (group.getType() != Group.Type.SORT &&
					!Group.WORKING_NAME.equals(group.getName())) {
					comboBox.addItem(NEW_HIST+group.getName()+WILD_CARD);
				}
			}
		}
		/* Add Existing hisograms */
		for (final Iterator grpiter = Group.getGroupList().iterator(); grpiter.hasNext();) {
			final Group group = (Group)grpiter.next();
			for  (final Iterator histiter = group.getHistogramList().iterator(); histiter.hasNext();) {
				final Histogram hist =(Histogram)histiter.next();
				if (hist.getType().getDimensionality() == histDim) {
					comboBox.addItem(hist.getFullName());
				}
			}
		}
		
		if (0<comboBox.getItemCount()) {			
			comboBox.setSelectedIndex(0);
		}
	}
	/*
	 * Is the histogram name one of a new histogram
	 */
	boolean isNewHistogram(String name){
		return name.startsWith(NEW_HIST);
	}
	/*
	 * Get the group name from the combobox string
	 * @param name The name in the combobox 
	 * @return	the group name
	 */
	String parseGroupName(String name) {
        final StringBuffer buffer = new StringBuffer(name);
        final String groupName = buffer.substring(NEW_HIST.length(), name
                .length()
                - WILD_CARD.length());
        return groupName;
    }
	
	/*
	 * Create a new histogram given a group, name and size 
	 */
	Histogram createNewHistogram(String groupName, String name, String histName, int size) {
		Group group;
		Histogram hist;
		group=Group.getGroup(groupName);
		if (group==null) { 
			group= Group.createGroup(groupName, Group.Type.FILE);
		}
		hist = (AbstractHist1D)Histogram.createHistogram(group, new double[size],histName);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		return hist;
	}

	/**
	 * Converts int array to double array.
	 * 
	 * @param intArray array to convert
	 * @return double array most closely approximating the given array
	 */
	protected double[] intToDoubleArray(int[] intArray) {
	    final int len = intArray.length;
		final double[] out = new double[len];
		for (int i = 0; i < len; i++) {
			out[i] = intArray[i];
		}
		return out;
	}

	/**
	 * Converts double array to int array.
	 * 
	 * @param dArray array to convert
	 * @return int array most closely approximating the given array
	 */
	protected int[] doubleToIntArray(double[] dArray) {
	    final int len = dArray.length;
		final int[] out = new int[len];
		for (int i = 0; i < len; i++) {
			out[i] = (int) Math.round(dArray[i]);
		}
		return out;
	}
	
	/**
	 * Convert int 2 dim array to double 2 dim array.
	 * 
	 * @param int2D array to convert
	 * @return double array most closely approximating the given array
	 */
	protected double[][] intToDouble2DArray(int[][] int2D) {
	    final int lenX=int2D.length;
	    final int lenY=int2D[0].length;
		double[][] rval = new double[lenX][lenY];
		for (int i = 0; i < lenX; i++) {
			for (int j = 0; j < lenY; j++) {
				rval[i][j] = int2D[i][j];
			}
		}
		return rval;
	}
	/**
	 * Get the mean character width in pixels
	 * @param fm
	 * @return
	 */
	int getMeanCharWidth(FontMetrics fm) {
		
		final double NUMBER_WIDTHS =256; 
		double sum =0;
		int meanWidth;
		
		int[] widths =fm.getWidths(); 
		for (int i=0;i<NUMBER_WIDTHS; i++){
			sum+=widths[i];		
		}
		
		return meanWidth =(int)Math.round(sum/NUMBER_WIDTHS); 
	}
}
