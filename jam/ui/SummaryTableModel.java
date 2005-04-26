package jam.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jam.data.DataElement;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.Scaler;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Model for summary table showen a group is selected
 * 
 * @author ken
 *
 */
public class SummaryTableModel implements TableModel {
	
	private String COL_NAME_GROUP="Group";

	private String COL_NAME_TYPE ="Type";
	
	private String COL_NAME_NAME ="NAME";
	
	private String COL_NAME_VALUE ="COUNTS";
	
	Group selectedGroup;
	
	int numColumns;
	
	String [] columnTitles;
	
	List dataList = new ArrayList();
	
	List listenerList= new ArrayList();
	
	SummaryTableModel() {
		setSelectionType(SummaryTable.SINGLE_GROUP_SELECTED);
	}
	
	public void setGroup(Group groupIn) {
		
		selectedGroup=groupIn;
		dataList.clear();
				
		if (selectedGroup!=null) {
			
			//Scalers
			Iterator scalIter = selectedGroup.getScalerList().iterator();
			while (scalIter.hasNext()) {
				DataElement dataElement =(DataElement)scalIter.next();
				dataList.add(dataElement);						
			}
						
			//Histograms
			Iterator histIter = selectedGroup.getHistogramList().iterator();			
			while (histIter.hasNext()) {
				Histogram hist = (Histogram)histIter.next();
				DataElement dataElement =(DataElement)hist;
				dataList.add(dataElement);
				
				//Gates
				Iterator gateIter = hist.getGates().iterator();
				while (gateIter.hasNext()) {
					DataElement dataElementGate =(DataElement)gateIter.next();
					dataList.add(dataElementGate);				
				}
			
			}
			
			/*
			//Histograms
			Iterator histIter = selectedGroup.getHistogramList().iterator();			
			while (histIter.hasNext()) {
				Histogram hist = (Histogram)histIter.next();
				DataElement dataElement =(DataElement)hist;
				dataList.add(dataElement);						
			}			
			*/

		}
									
	}
	/**
	 * Set the selection type
	 *
	 */
	public void setSelectionType(int selectionType) {
		
		if (selectionType==SummaryTable.ALL_GROUPS_SELECTED ) {
			numColumns =4;		
			columnTitles = new String[numColumns];
			
			columnTitles[0]=COL_NAME_GROUP;
			columnTitles[1]=COL_NAME_TYPE;
			columnTitles[2]=COL_NAME_NAME;
			columnTitles[3]=COL_NAME_VALUE;					
		} else {
			numColumns =3;		
			columnTitles = new String[numColumns];
	
			columnTitles[0]=COL_NAME_TYPE;
			columnTitles[1]=COL_NAME_NAME;
			columnTitles[2]=COL_NAME_VALUE;			
		}

		TableModelEvent tme = new TableModelEvent(this);
		
		Iterator listenerIter =listenerList.iterator();		
		while (listenerIter.hasNext()) {			
			TableModelListener tml = (TableModelListener)listenerIter.next();
			tml.tableChanged(tme);
		}

	}
	/**
	 * Get the number of columns
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return numColumns;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return dataList.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int arg0) {
		return String.class;
	}

	/**
	 * Get the value of a cell 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		Object retValue=null; 
		
		DataElement dataElement  = (DataElement)dataList.get(row);
    	//Type
    	if (col==0) {
    		
    		if (dataElement.getElementType()==DataElement.ELEMENT_TYPE_HISTOGRAM) {
        		retValue="Histogram";    			
    		} else if (dataElement.getElementType()==DataElement.ELEMENT_TYPE_GATE) {
        		retValue="Gate";
    		} else if (dataElement.getElementType()==DataElement.ELEMENT_TYPE_SCALER) {
        		retValue="Scaler";
    		}
    		
    	//Name	
    	} else if (col==1) {
    		retValue=dataElement.getName();
    	//Value	
    	} else if (col==2) {    		
    		//retValue = new Double(dataElement.getCount());
    		retValue = new Integer(dataElement.getCount());
	    }
	
		return retValue;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		return columnTitles[col];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void addTableModelListener(TableModelListener listener) {
		listenerList.add(listener);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void removeTableModelListener(TableModelListener arg0) {
		// TODO Auto-generated method stub

	}

}
