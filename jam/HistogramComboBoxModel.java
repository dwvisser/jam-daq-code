package jam;
import javax.swing.*;
import jam.data.Histogram;
import java.util.List;

/**
 * This class takes care of properly displaying the histogram chooser
 * in Jam's main window.
 * 
 * @author Dale Visser
 */
public class HistogramComboBoxModel  extends DefaultComboBoxModel {

    private String selection=null;
    private String NO_HISTS="No Histograms";
    private JamCommand jc;

    public HistogramComboBoxModel(JamCommand jc){
        super();
        this.jc=jc;
    }

    /**
     * Needs to be called after every action that changes the list of histograms.
     * Utility method added so that JamMain could notify when things have changed.
     */
    void changeOccured(){
        fireContentsChanged(this,0,getSize());
    }

    /**
     * Returns the individual list elements in the histogram chooser.
     */
    public Object getElementAt(int index){
    	Object rval=NO_HISTS; //default value if no histograms
    	List list=Histogram.getHistogramList(); 
    	if (index <0 || index >= list.size()) {
    		System.err.println("WARNING: "+getClass().getName()+
				".getElementAt("+index+"): index out of range.");  	
    	} else {
    		if (numHists()>0){
				rval = ((Histogram) Histogram.getHistogramList().get(index)).getName();
			} 
    	}
        return rval;
    }

    /**
     * Returns the number of list elements in the chooser.
     */
    public int getSize(){
		return Math.max(1,numHists());
    }

    //for ComboBoxModel interface
    public void setSelectedItem(Object anItem) {
        selection = (String) anItem;
    }

    //for ComboBoxModel interface
    public Object getSelectedItem() {
        return selection;
    }

    private int numHists(){
        return  Histogram.getHistogramList().size();
    }

}
