package jam.data.control;

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

	/** String to prepend to new histogram group in combobox */ 
	final String NEW_HIST = "NEW: ";
	/** String to append to new histogram group in combobox */	
	final String HIST_WILD_CARD="/.";	
	
	public AbstractManipulation(String title, boolean modal){
		super(title, modal);		
	}

	/* non-javadoc:
	 * Adds a list of histograms to a choose
	 */
	private void loadAllHists(JComboBox comboBox) {
		comboBox.removeAllItems();
		/* Add working group new */
		comboBox.addItem(NEW_HIST+Group.WORKING_NAME+HIST_WILD_CARD);
		//Add new histograms
		for (Iterator iter = Group.getGroupList().iterator();iter.hasNext();) {
			Group group = (Group)iter.next();
			if (group.getType() != Group.Type.SORT &&
				!Group.WORKING_NAME.equals(group.getName())	) {
				comboBox.addItem(NEW_HIST+group.getName()+HIST_WILD_CARD);
			}
		}
		/* Add Existing hisograms */
		for (Iterator grpiter = Group.getGroupList().iterator(); grpiter.hasNext();) {
			Group group = (Group)grpiter.next();
			for  (Iterator histiter = group.getHistogramList().iterator(); histiter.hasNext();) {
				Histogram hist =(Histogram)histiter.next();
				if (hist.getType() == Histogram.Type.ONE_D_DOUBLE) {
					comboBox.addItem(hist.getFullName());
				}
			}
		}

		comboBox.setSelectedIndex(0);
	}
	
	/* non-javadoc:
	 * add histograms of type type1 and type2 to chooser
	 */
	void loadAllHists(JComboBox comboBox, boolean addNew, int histDim) {
		comboBox.removeAllItems();
		if(addNew) {
			//Add working group new
			comboBox.addItem(NEW_HIST+Group.WORKING_NAME+HIST_WILD_CARD);
			//Add new histograms
			for (Iterator iter = Group.getGroupList().iterator();iter.hasNext();) {
				Group group = (Group)iter.next();
				if (group.getType() != Group.Type.SORT &&
					!Group.WORKING_NAME.equals(group.getName())) {
					comboBox.addItem(NEW_HIST+group.getName()+HIST_WILD_CARD);
				}
			}
		}
		/* Add Existing hisograms */
		for (Iterator grpiter = Group.getGroupList().iterator(); grpiter.hasNext();) {
			Group group = (Group)grpiter.next();
			for  (Iterator histiter = group.getHistogramList().iterator(); histiter.hasNext();) {
				Histogram hist =(Histogram)histiter.next();
				if (hist.getType().getDimensionality() == histDim) {
					comboBox.addItem(hist.getFullName());
				}
			}
		}

		comboBox.setSelectedIndex(0);
	}
	
	boolean isNewHistogram(String name){
		return name.startsWith(NEW_HIST);
	}
	
	String parseGroupName(String name){
		
		StringBuffer sb = new StringBuffer(name);
		String groupName=sb.substring(NEW_HIST.length(), name.length()-HIST_WILD_CARD.length());
		return groupName;

	}
	
	Histogram createNewHistogram(String name, String histName, int size) {

		Histogram hist;
		
		String groupName = parseGroupName(name);
		if (groupName.equals(Group.WORKING_NAME))
		{
			Group.createGroup(groupName, Group.Type.FILE);
		}
		hist = (AbstractHist1D)Histogram.createHistogram(
				new double[size],histName);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		
		return hist;

	}

	
}
