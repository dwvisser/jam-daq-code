package jam;
import javax.swing.*;
import jam.data.Histogram;

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

    // for ListModel interface 
    public Object getElementAt(int index){
        if (numHists()>0){
            return ((Histogram) Histogram.getHistogramList().elementAt(index)).getName();
        } else {
            return NO_HISTS;
        }
    }

    // for ListModel interface 
    public int getSize(){
        if (numHists()>0){
            return numHists();
        } else {
            return 1;//item will be NO_HISTS String
        }
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
