package jam.data;

import jam.global.Nameable;

import java.util.List;
import java.util.Set;

/**
 * @author Dale Visser
 * 
 * @param <T>
 *            value type
 */
public interface NameValueCollection<T extends Nameable> {
	/**
	 * @param nameable
	 *            object to remap
	 * @param oldName
	 *            old name
	 * @param newName
	 *            new name
	 */
	void remap(T nameable, String oldName, String newName);

	/**
	 * @return a read-only list of the items in the order they were added
	 */
	List<T> getList();

	/**
	 * @return the set of all names
	 */
	Set<String> getNameSet();

	/**
	 * @param nameable
	 *            item to add
	 * @param uniqueName
	 *            name to map to
	 */
	void add(final T nameable, final String uniqueName);

	/**
	 * @param nameable
	 *            to remove
	 */
	void remove(final T nameable);

	/**
	 * Clear the collection.
	 */
	void clear();

	/**
	 * @param name
	 *            name of item to get
	 * @return item mapped to the given name
	 */
	T get(final String name);
}
