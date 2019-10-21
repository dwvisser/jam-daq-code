package jam.sort.control;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import injection.GuiceInjector;
import jam.data.Factory;
import jam.data.Group;
import jam.global.JamException;
import jam.global.RuntimeSubclassIdentifier;
import jam.sort.AbstractSortRoutine;

@SuppressWarnings("serial")
final class SortChooser extends JComboBox<Class<? extends AbstractSortRoutine>> {

	private transient File classPath;

	private transient Class<? extends AbstractSortRoutine> sortClass;

	private transient AbstractSortRoutine sortRoutine;

	private transient final List<Class<? extends AbstractSortRoutine>> listClasses = new ArrayList<>();

	private transient final RuntimeSubclassIdentifier subclassIdentifier = GuiceInjector
			.getObjectInstance(RuntimeSubclassIdentifier.class);

	SortChooser() {
		super();
		setToolTipText("Select sort routine class");
		addActionListener(event -> sortClass = getItemAt(getSelectedIndex()));
	}

	protected AbstractSortRoutine getSortRoutine() {
		return sortRoutine;
	}

	protected void forgetSortRoutine() {
		sortRoutine = null;// NOPMD
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
	@SuppressWarnings("unchecked")
	protected void loadSorter(final boolean userSpecifiedPath)
			throws JamException {
		if (sortClass == null) {
			sortClass = (Class<? extends AbstractSortRoutine>) getSelectedItem();
		}
		if (sortClass == null) {
			throw new JamException("No sort routine has been selected.");
		}
		// FIXME maybe we should do DataBase.clearAll(); here
		jam.data.Warehouse.getGroupCollection().clear();
		final String sortName = Group.parseSortClassName(sortClass.getName());
		Factory.createGroup(sortName, Group.Type.SORT);
		try {
			if (userSpecifiedPath) {
				synchronized (this) {
					sortRoutine = (AbstractSortRoutine) subclassIdentifier.loadClass(
							classPath, sortClass.getName()).getDeclaredConstructor().newInstance();
				}
			} else {// use default loader
				/* we call loadClass() in order to guarantee latest version */
				synchronized (this) {
					sortRoutine = sortClass.getDeclaredConstructor().newInstance();
				}
			}
		} catch (InstantiationException | InvocationTargetException | NoSuchMethodException ie) {
			throw new JamException("Cannot instantiate sort routine: "
					+ sortClass.getName(), ie);
		} catch (IllegalAccessException iae) {
			throw new JamException("Cannot access sort routine: "
					+ sortClass.getName(), iae);
		}
	}

	/**
	 * Sets whether to use the default classpath or a user-specified one.
	 * 
	 * @param isDefaultPath
	 *            <code>true</code> to use the default classpath
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
	 * @param inPath
	 *            path to classes
	 */
	protected void loadChooserClassPath(final File inPath) {
		classPath = inPath;
		loadChooser(false);
	}

	/**
	 * Get a list of the classes
	 * 
	 * @return List of classes
	 */
	protected List<Class<? extends AbstractSortRoutine>> getClassList() {
		return listClasses;
	}

	/**
	 * Select a sort class
	 * 
	 * @param className
	 *            name of class to select
	 */
	public void selectSortClass(final String className) {
		for (Class<? extends AbstractSortRoutine> clazz : getClassList()) {
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
	private Set<Class<? extends AbstractSortRoutine>> findSortClasses(
			final File path) {
		return subclassIdentifier.find(path, AbstractSortRoutine.class);
	}

	/**
	 * Get the sort classes using the default class path.
	 * 
	 * @return set of available sort routines
	 */
	private Set<Class<? extends AbstractSortRoutine>> findSortClassesDefault() {
		final Set<Class<? extends AbstractSortRoutine>> set = new LinkedHashSet<>();
		set.addAll(subclassIdentifier.find("help", AbstractSortRoutine.class, true));
		set.addAll(subclassIdentifier.find("sort", AbstractSortRoutine.class, true));
		return set;
	}
}
