package jam.ui;

import jam.data.DataElement;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.Scaler;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
final class SummaryTableModel implements TableModel {

	/**
	 * Class with data for a row
	 */
	static class RowDataElement {

		private transient final DataElement dataElement;

		private transient final String groupName;

		RowDataElement(String groupName, DataElement dataElement) {
			super();
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

	/** The titles of the columns */
	private transient String[] columnTitles;

	private transient final List<RowDataElement> dataList = new ArrayList<RowDataElement>();

	private transient final Collection<TableModelListener> listeners = new HashSet<TableModelListener>();

	/** The number of columns */
	private transient int numColumns;

	private transient final NumberFormat numFormat;

	private transient Group selectedGroup;

	/** The selection type, single group or all groups */
	private transient SummaryTable.Selection selectionType;

	/** Flag to show gates */
	private transient boolean showGates;

	/** Flag to show histograms */
	private transient boolean showHistograms;

	/** Flag to show scalers */
	private transient boolean showScalers;

	SummaryTableModel() {
		super();
		setSelectionType(SummaryTable.Selection.SINGLE_GROUP);
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
	public void addTableModelListener(final TableModelListener listener) {
		listeners.add(listener);
	}

	/**
	 * Create the list of data elements needed given a group
	 * 
	 * @param group
	 */
	private void createGroupDataList(final Group group) {
		if (group != null) {
			final String gname = group.getName();
			if (showScalers) {
				for (Scaler scaler : group.getScalerList()) {
					dataList.add(new RowDataElement(gname, scaler));//NOPMD
				}
			}
			for (Histogram hist : group.getHistogramList()) {
				if (showHistograms) {
					dataList.add(new RowDataElement(gname, hist));//NOPMD
				}
				if (showGates) {
					for (Gate gate : hist.getGates()) {
						dataList.add(new RowDataElement(gname, gate));//NOPMD
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
	private void fireTableEvent(final TableModelEvent tableModelEvent) {
		for (TableModelListener tml : listeners) {
			tml.tableChanged(tableModelEvent);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(final int arg0) {
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
	public String getColumnName(final int col) {
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
	public Object getValueAt(final int row, final int col) {
		Object retValue = null;
		int offsetCol;
		final RowDataElement rowDataElement = dataList.get(row);
		final String groupName = rowDataElement.getGroupName();
		final DataElement dataElement = rowDataElement.getDataElement();
		if (selectionType == SummaryTable.Selection.ALL_GROUPS) {
			offsetCol = 1;
			// Group
			if (col == 0) {
				retValue = groupName;
			}
		} else {
			offsetCol = 0;
		}
		if (col == offsetCol) {// Type
			final String stype = dataElement.getElementType().toString();
			retValue = stype.substring(0,1)+stype.substring(1).toLowerCase();
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
	public boolean isCellEditable(final int arg0, final int arg1) {
		return false;
	}

	/**
	 * Refresh the model data.
	 * 
	 */
	public void refresh() {
		dataList.clear();
		if ((selectionType == SummaryTable.Selection.SINGLE_GROUP)
				&& (selectedGroup != null)) {
			createGroupDataList(selectedGroup);
		} else {
			for (Group group : Group.getGroupList()) {
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
	public void removeTableModelListener(final TableModelListener listenerRemove) {
		listeners.remove(listenerRemove);
	}

	/**
	 * Set the current group and refresh.
	 * 
	 * @param groupIn
	 *            new current group
	 */
	public void setGroup(final Group groupIn) {
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
	public void setOptions(final boolean showScalers,
			final boolean showHistograms, final boolean showGates) {
		this.showScalers = showScalers;
		this.showHistograms = showHistograms;
		this.showGates = showGates;
		refresh();
	}

	/*
	 * non-javadoc: Set the selection type
	 */
	void setSelectionType(final SummaryTable.Selection type) {
		selectionType = type;
		final String COL_NAME_GROUP = "Group";
		final String COL_NAME_NAME = "Name";
		final String COL_NAME_TYPE = "Type";
		final String COL_NAME_VALUE = "Counts";
		if (type == SummaryTable.Selection.ALL_GROUPS) {
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
	public void setValueAt(final Object arg0, final int arg1, final int arg2) {
		// TODO Auto-generated method stub

	}

}
