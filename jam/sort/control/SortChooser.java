package jam.sort.control;

import jam.JamException;
import jam.data.Group;
import jam.global.RTSI;
import jam.global.Sorter;
import jam.sort.AbstractSortRoutine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

final class SortChooser extends JComboBox {

	private transient File classPath;

	private transient Class sortClass;
	
	private transient AbstractSortRoutine sortRoutine;

	private transient final JTextComponent textSortPath;

	SortChooser(JTextComponent textSortPath) {
		super();
		this.textSortPath = textSortPath;
		setToolTipText("Select sort routine class");
		addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				sortClass = (Class) getSelectedItem();
			}
		});
	}
	
	AbstractSortRoutine getSortRoutine() {
		return sortRoutine;
	}
	
	void forgetSortRoutine() {
		sortRoutine = null;//NOPMD
	}
	
	/**
	 * Resolves the String objects into class names and loads the sorting class
	 * and event streams.
	 * 
	 * @param userSpecifiedPath
	 *            whether to use user-specified classPath
	 * @throws JamException
	 *             if there's a problem
	 */
	protected void loadSorter(final boolean userSpecifiedPath) throws JamException {
		if (sortClass == null) {
			sortClass = (Class) getSelectedItem();
		}
		if (sortClass == null) {
			throw new JamException("No sort routine has been selected.");
		}
		// FIXME maybe we should do DataBase.clearAll(); here
		Group.clearList();
		final String sortName = Group.parseSortClassName(sortClass.getName());
		Group.createGroup(sortName, Group.Type.SORT);
		try {
			if (userSpecifiedPath) {
				synchronized (this) {
					sortRoutine = (AbstractSortRoutine) RTSI.getSingletonInstance()
							.loadClass(classPath, sortClass.getName())
							.newInstance();
				}
			} else {// use default loader
				/* we call loadClass() in order to guarantee latest version */
				synchronized (this) {
					sortRoutine = (AbstractSortRoutine) sortClass.newInstance();
				}
			}
		} catch (InstantiationException ie) {
			throw new JamException("Cannot instantiate sort routine: "
					+ sortClass.getName());
		} catch (IllegalAccessException iae) {
			throw new JamException("Cannot access sort routine: "
					+ sortClass.getName());
		}
	}
	
	/**
	 * Sets whether to use the default classpath or a user-specified one.
	 * 
	 * @param isDefault
	 *            <code>true</code> to use the default classpath
	 * @return a list of the available sort routines
	 */
	protected List<Class<?>> setChooserDefault(final boolean isDefault) {
		final List<Class<?>> list = new ArrayList<Class<?>>();
		if (isDefault) {
			final Set<Class<?>> set = new LinkedHashSet<Class<?>>();
			final RTSI rtsi = RTSI.getSingletonInstance();
			set.addAll(rtsi.find("help", Sorter.class, true));
			set.addAll(rtsi.find("sort", Sorter.class, true));
			list.addAll(set);
		} else {
			list.addAll(AbstractSetup.getSortClasses(classPath));
		}
		setModel(new DefaultComboBoxModel(list.toArray()));
		return list;
	}

	/**
	 * Sets the class path for loading sort routines.
	 * 
	 * @param file
	 *            path to classes
	 */
	protected void setSortClassPath(final File file) {
		if (file.exists()) {
			classPath = file;
			setModel(new DefaultComboBoxModel(AbstractSetup.getSortClasses(
					classPath).toArray()));
			if (getModel().getSize() > 0) {
				setSelectedIndex(0);
			}
			textSortPath.setText(classPath.getAbsolutePath());
		}
	}

}
