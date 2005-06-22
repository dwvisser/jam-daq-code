package jam.ui;

import jam.data.DataElement;
import jam.data.Group;
import jam.data.Histogram;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Model for summary table showen a group is selected
 * 
 * @author ken
 * 
 */
public final class SummaryTableModel implements TableModel {

	/**
	 * Class with data for a row
	 */
	class RowDataElement {

		private DataElement dataElement;

		private String groupName;

		RowDataElement(String groupName, DataElement dataElement) {
			this.groupName = groupName;
			this.dataElement = dataElement;
		}

		DataElement getDataElement() {
			return dataElement;
		}

		String getGroupName() {
			return groupName;
		}
	}

	private String COL_NAME_GROUP = "Group";

	private String COL_NAME_NAME = "Name";

	private String COL_NAME_TYPE = "Type";

	private String COL_NAME_VALUE = "Counts";

	/** The titles of the columns */
	private String[] columnTitles;

	private List<RowDataElement> dataList = new ArrayList<RowDataElement>();

	private List<TableModelListener> listenerList = new ArrayList<TableModelListener>();

	/** The number of columns */
	int numColumns;

	NumberFormat numFormat;

	Group selectedGroup;

	/** The selection type, single group or all groups */
	int selectionType;

	/** Flag to show gates */
	boolean showGates;

	/** Flag to show histograms */
	boolean showHistograms;

	/** Flag to show scalers */
	boolean showScalers;

	SummaryTableModel() {
		setSelectionType(SummaryTable.SINGLE_GROUP_SELECTED);

		numFormat = NumberFormat.getInstance();
		numFormat.setGroupingUsed(false);
		final int fracDigits = 2;
		numFormat.setMinimumFractionDigits(fracDigits);
		numFormat.setMaximumFractionDigits(fracDigits);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void addTableModelListener(TableModelListener listener) {
		listenerList.add(listener);
	}

	/**
	 * Create the list of data elements needed given a group
	 * 
	 * @param group
	 */
	private void createGroupDataList(Group group) {
		if (group != null) {
			// Scalers
			if (showScalers) {
				Iterator scalIter = group.getScalerList().iterator();
				while (scalIter.hasNext()) {
					DataElement dataElement = (DataElement) scalIter.next();
					dataList.add(new RowDataElement(group.getName(),
							dataElement));
				}
			}
			// Histograms
			Iterator histIter = group.getHistogramList().iterator();
			while (histIter.hasNext()) {
				Histogram hist = (Histogram) histIter.next();
				if (showHistograms) {
					final DataElement dataElement = hist;
					dataList.add(new RowDataElement(group.getName(),
							dataElement));
				}

				// Gates
				if (showGates) {
					Iterator gateIter = hist.getGates().iterator();
					while (gateIter.hasNext()) {
						DataElement dataElementGate = (DataElement) gateIter
								.next();
						dataList.add(new RowDataElement(group.getName(),
								dataElementGate));
					}
				}
			}
		}
	}

	/**
	 * Fire table event to listeners
	 * 
	 * @param tableModelEvent
	 */
	private void fireTableEvent(TableModelEvent tableModelEvent) {

		final Iterator listenerIter = listenerList.iterator();
		while (listenerIter.hasNext()) {
			final TableModelListener tml = (TableModelListener) listenerIter
					.next();
			tml.tableChanged(tableModelEvent);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int arg0) {
		return String.class;
	}

	/**
	 * Get the number of columns
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return numColumns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		return columnTitles[col];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return dataList.size();
	}

	/**
	 * Get the value of a cell
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		Object retValue = null;
		int offsetCol;
		RowDataElement rowDataElement = dataList.get(row);
		String groupName = rowDataElement.getGroupName();
		DataElement dataElement = rowDataElement.getDataElement();
		if (selectionType == SummaryTable.ALL_GROUPS_SELECTED) {
			offsetCol = 1;
			// Group
			if (col == 0) {
				retValue = groupName;
			}
		} else {
			offsetCol = 0;
		}
		if (col == offsetCol) {// Type
			if (dataElement.getElementType() == DataElement.Type.HISTOGRAM) {
				retValue = "Histogram";
			} else if (dataElement.getElementType() == DataElement.Type.GATE) {
				retValue = "Gate";
			} else if (dataElement.getElementType() == DataElement.Type.SCALER) {
				retValue = "Scaler";
			}
		} else if (col == offsetCol + 1) {// Name
			retValue = dataElement.getName();
		} else if (col == offsetCol + 2) {// Value
			retValue = numFormat.format(dataElement.getCount());
		}
		return retValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	/**
	 * Refresh the model data.
	 * 
	 */
	public void refresh() {
		dataList.clear();
		if ((selectionType == SummaryTable.SINGLE_GROUP_SELECTED)
				&& (selectedGroup != null)) {
			createGroupDataList(selectedGroup);
		} else {
			Iterator groupIter = Group.getGroupList().iterator();
			while (groupIter.hasNext()) {
				Group group = (Group) groupIter.next();
				createGroupDataList(group);
			}
		}
		fireTableEvent(new TableModelEvent(this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void removeTableModelListener(TableModelListener listenerRemove) {
		listenerList.remove(listenerRemove);
	}

	/**
	 * Set the current group and refresh.
	 * 
	 * @param groupIn
	 *            new current group
	 */
	public void setGroup(Group groupIn) {
		selectedGroup = groupIn;
		refresh();
	}

	/**
	 * Set options of which data elements to show
	 * 
	 * @param showScalers
	 * @param showHistograms
	 * @param showGates
	 */
	public void setOptions(boolean showScalers, boolean showHistograms,
			boolean showGates) {
		this.showScalers = showScalers;
		this.showHistograms = showHistograms;
		this.showGates = showGates;
		refresh();
	}

	/*
	 * non-javadoc: Set the selection type
	 */
	final void setSelectionType(int type) {
		selectionType = type;
		if (type == SummaryTable.ALL_GROUPS_SELECTED) {
			numColumns = 4;
			columnTitles = new String[numColumns];
			columnTitles[0] = COL_NAME_GROUP;
			columnTitles[1] = COL_NAME_TYPE;
			columnTitles[2] = COL_NAME_NAME;
			columnTitles[3] = COL_NAME_VALUE;
		} else {
			numColumns = 3;
			columnTitles = new String[numColumns];
			columnTitles[0] = COL_NAME_TYPE;
			columnTitles[1] = COL_NAME_NAME;
			columnTitles[2] = COL_NAME_VALUE;
		}
		fireTableEvent(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
