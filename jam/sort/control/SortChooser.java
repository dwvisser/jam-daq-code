package jam.sort.control;

import jam.JamException;
import jam.data.Group;
import jam.global.RTSI;
import jam.global.Sorter;
import jam.sort.SortRoutine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

final class SortChooser extends JComboBox {

	private transient File classPath;

	private transient Class sortClass;
	
	private transient SortRoutine sortRoutine;

	final List<Class<?>> listClasses = new ArrayList<Class<?>>();	
	
	SortChooser() {
		super();
		setToolTipText("Select sort routine class");
		addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				sortClass = (Class) getSelectedItem();
			}
		});
	}
	
	SortRoutine getSortRoutine() {
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
					sortRoutine = (SortRoutine) RTSI.getSingletonInstance()
							.loadClass(classPath, sortClass.getName())
							.newInstance();
				}
			} else {// use default loader
				/* we call loadClass() in order to guarantee latest version */
				synchronized (this) {
					sortRoutine = (SortRoutine) sortClass.newInstance();
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
	private void loadChooser(final boolean isDefaultPath) {
		listClasses.clear();
		if (isDefaultPath) {
			listClasses.addAll(findSortClassesDefault());
		} else {
			if (classPath.exists()) {
				listClasses.addAll(findSortClasses(classPath));
			}
		}
		
		setModel(new DefaultComboBoxModel(listClasses.toArray()));
		
		if (getModel().getSize() > 0) {
			setSelectedIndex(0);
		}

	}

	protected void loadChooserDefault() {
		loadChooser(true);
	}
	/**
	 * Sets the class path for loading sort routines.
	 * 
	 * @param file
	 *            path to classes
	 */
	protected void loadChooserClassPath(final File inPath) {
		classPath = inPath;
		loadChooser(false);
	}
	/**
	 * Get a list of the classes
	 * @return List of classes
	 */
	protected List<Class<?>> getClassList() {	
		return listClasses;
	}
	
	/**
	 *  Select a sort class
	 * @param className name of class to select 
	 */ 
	public void selectSortClass(String className) {
		for (Class clazz : getClassList()) {
			final String name = clazz.getName();
			if (name.equals(className)) {
				setSelectedItem(clazz);
				break;
			}
		}
	}
	/**
	 * Get the sort classes using the given file as the class path.
	 * 
	 * @param path
	 *            class path
	 * @return set of available sort routines
	 */
	private Set<Class<?>> findSortClasses(final File path) {
		final RTSI rtsi = RTSI.getSingletonInstance();
		return rtsi.find(path, Sorter.class);
	}
	/**
	 * Get the sort classes using the default class path.
	 * 
	 * @return set of available sort routines
	 */
	private Set<Class<?>> findSortClassesDefault() {
		final Set<Class<?>> set = new LinkedHashSet<Class<?>>();
		final RTSI rtsi = RTSI.getSingletonInstance();
		set.addAll(rtsi.find("help", Sorter.class, true));
		set.addAll(rtsi.find("sort", Sorter.class, true));
		return set;
	}

}
