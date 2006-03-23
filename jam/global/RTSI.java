package jam.global;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import javax.swing.JOptionPane;

/**
 * This utility class is looking for all the classes implementing or inheriting
 * from a given interface or class. (RunTime Subclass Identification)
 * 
 * @author <a href="mailto:daniel@satlive.org">Daniel Le Berre</a>
 * @version 1.0
 */
public class RTSI {

	static final String CLASS_EXT = ".class";

	static final ClassLoader DEF_LOADER = ClassLoader.getSystemClassLoader();

	private static final RTSI instance = new RTSI();

	private static final Logger LOGGER = Logger.getLogger(RTSI.class
			.getPackage().getName());

	static final String PERIOD = ".";

	static final String SLASH = "/";

	/**
	 * @return true if <code>c</code> can have instances and is assignable as
	 *         <code>tosubclass</code>
	 * @param tosubclass
	 *            to superclass we desire instances of
	 * @param clazz
	 *            the candidate class to check
	 */
	public static boolean canUseClassAs(final Class<?> tosubclass,
			final Class<?> clazz) {
		return tosubclass.isAssignableFrom(clazz)
				&& ((clazz.getModifiers() & Modifier.ABSTRACT) == 0);
	}

	/**
	 * @param fileName
	 *            filename minus any path
	 * @param pckg
	 *            the string representing the package for the filename
	 * @return representation of the proper full reference to the class
	 */
	private static String filenameToClassname(final String fileName,
			final String pckg) {
		final StringBuffer rval = new StringBuffer(pckg);
		rval.append('.');
		rval.append(fileName.substring(0, fileName.length()
				- CLASS_EXT.length()));
		return rval.toString();
	}

	/**
	 * 
	 * @param file
	 *            file containing the class
	 * @param classpath
	 *            representation of the proper classpath for loading this file
	 * @return representation of the proper full reference to the class
	 */
	private static String fileToClassname(final File file,
			final String classpath) {
		final String fullpath = file.getPath();
		String temp = fullpath.substring(0, fullpath.length()
				- CLASS_EXT.length());
		if (temp.startsWith(classpath)) {
			temp = temp.substring(classpath.length(), temp.length());
		}
		temp = temp.replace(File.separatorChar, '.');
		if (temp.startsWith(PERIOD)) {
			temp = temp.substring(1);
		}
		return temp;
	}

	/**
	 * 
	 * @return the unique singleton instance of this class
	 */
	static public RTSI getSingletonInstance() {
		return instance;
	}

	private static String jarEntryToClassname(final ZipEntry entry,
			final String starts) {
		final String entryname = entry.getName();
		String classname = null;
		if (entryname.startsWith(starts) && entryname.endsWith(CLASS_EXT)) {
			classname = entryname.substring(0, entryname.length()
					- CLASS_EXT.length());
			classname = classname.replace('/', '.');
		}
		return classname;
	}

	/**
	 * Find all valid instantiatable subclasses of the given classname. The
	 * first argument is the classname, and it is assumed the whole classpath
	 * will be searched if no other argument is given. If a second argument is
	 * given, the first argument is the package to search.
	 * 
	 * @param args
	 *            the command-line arguments
	 */
	public static void main(final String[] args) {
		final String usage = "Usage: java RTSI [<package>] <subclass>";
		final int minargs = 1;
		if (args.length > minargs) {
			instance.find(args[0], args[1], true);
		} else {
			if (args.length == minargs) {
				instance.find(args[0], true);
			} else {
				LOGGER.info(usage);
			}
		}
	}

	private transient final String rtsiName;

	private RTSI() {
		super();
		rtsiName = getClass().getName();
	}

	/**
	 * Adds class name to the given collection if it instances of the given
	 * superclass can be made with it. If not, does nothing.
	 * 
	 * @param classname
	 *            the class name in question
	 * @param tosubclass
	 *            the superclass
	 * @param loader
	 *            the class loader we need to resolve the classname with
	 * @param coll
	 *            the collection to add to
	 */
	private void addToCollection(final String classname,
			final Class tosubclass, final ClassLoader loader,
			final Collection<String> coll) {
		try {
			final Class clazz = loader.loadClass(classname);
			if (canUseClassAs(tosubclass, clazz)) {
				coll.add(classname);
			}
		} catch (ClassNotFoundException cnfex) {
			JOptionPane.showMessageDialog(null, cnfex.getMessage(), rtsiName,
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Find all the classes inheriting or implementing a given class in a given
	 * package and any sub-packages. WARNING: a jar file as classpath hasn't
	 * been implemented.
	 * 
	 * @param classpath
	 *            folder containing the classpath to search
	 * @param tosubclass
	 *            the Class object to be assignable to
	 * @return an alphabetically ordered set of classes assignable as requested
	 */
	public Set<Class<?>> find(final File classpath, final Class tosubclass) {
		/* used linked hash set to guarantee order is preserved */
		final Set<Class<?>> rval = new LinkedHashSet<Class<?>>();
		ClassLoader loader = DEF_LOADER;
		URL url = null;
		if (classpath != null) {
			try {
				url = classpath.toURL();
			} catch (MalformedURLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), rtsiName,
						JOptionPane.ERROR_MESSAGE);
			}
			if (url != null) {
				loader = new URLClassLoader(new URL[] { url });
			}
		}
		if (classpath != null) {
			/* create the set of classes, preserving the name order */
			rval.addAll(nameSetToClassSet(getClassesRecursively(tosubclass,
					classpath.getAbsolutePath(), classpath, loader), loader));
		}
		return rval;
	}

	/**
	 * Display all the classes inheriting or implementing a given class in the
	 * currently loaded packages.
	 * 
	 * @param tosubclassname
	 *            the name of the class to inherit from
	 * @param recurse
	 *            whether to recurse into subfolders
	 */
	private void find(final String tosubclassname, final boolean recurse) {
		final Class tosubclass = resolveClass(tosubclassname);
		if (tosubclass != null) {
			final Package[] pcks = Package.getPackages();
			LOGGER.info("Packages:");
			for (int i = 0; i < pcks.length; i++) {
				LOGGER.info("\t" + pcks[i].getName());
			}
			for (int i = 0; i < pcks.length; i++) {
				for (Class clazz : find(pcks[i].getName(), tosubclass, recurse)) {
					LOGGER.info("Found class: " + clazz.getName());
				}
			}
		}
	}

	/**
	 * @return a set of unique classes that can have instances and are
	 *         assignable as <code>tosubclass</code>.
	 * 
	 * @param pckgname
	 *            the package to search
	 * @param tosubclass
	 *            the superclass we desire implementations of
	 * @param recurse
	 *            whether to recurse through sub-packages
	 */
	public Set<Class<?>> find(final String pckgname, final Class tosubclass,
			final boolean recurse) {
		final StringBuffer errmessage = new StringBuffer("Searching in ")
				.append(pckgname)
				.append(
						"\nYou've probably incorrectly specified a classpath,\n")
				.append("or moved/renamed an existing .class file.\n");
		final Set<String> names = findClassNames(pckgname, tosubclass, recurse);
		final Set<Class<?>> rval = new LinkedHashSet<Class<?>>(); // preserves
		// order of
		// add()'s
		try {
			for (String name : names) {
				rval.add(DEF_LOADER.loadClass(name));
			}
		} catch (ClassNotFoundException e) {
			errmessage.append(e.getMessage());
			JOptionPane.showMessageDialog(null, errmessage.toString(),
					rtsiName, JOptionPane.ERROR_MESSAGE);
		} catch (LinkageError e) {
			errmessage.append(e.getMessage());
			JOptionPane.showMessageDialog(null, errmessage.toString(),
					rtsiName, JOptionPane.ERROR_MESSAGE);
		}
		return rval;
	}

	/**
	 * Display all the classes inheriting or implementing a given class in a
	 * given package.
	 * 
	 * @param pckname
	 *            the fully qualified name of the package
	 * @param tosubclassname
	 *            the name of the class to inherit from
	 * @param recurse
	 *            try all sub-packages as well if true
	 */
	private void find(final String pckname, final String tosubclassname,
			final boolean recurse) {
		final Class tosubclass = resolveClass(tosubclassname);
		final Set<Class<?>> result = find(pckname, tosubclass, recurse);
		LOGGER.info("Find classes assignable as " + tosubclass.getName()
				+ " in \"" + pckname + "\"");
		for (Class clazz : result) {
			LOGGER.info("\t" + clazz.getName());
		}
		LOGGER.info("done.");
	}

	/**
	 * Find all the classes inheriting or implementing a given class in a given
	 * package (but it does not search any sub-packages).
	 * 
	 * @param pckgname
	 *            the fully qualified name of the package
	 * @param tosubclass
	 *            the Class object to inherit from
	 * @param recurse
	 *            whether to traverse subpackages recursively
	 * @return an unordered list of classes assignable as requested
	 */
	private Set<String> findClassNames(final String pckgname,
			final Class tosubclass, final boolean recurse) {
		/*
		 * Code from JWhich Translate the package name into an absolute path
		 */
		final SortedSet<String> rval = new TreeSet<String>();
		String name = pckgname; // copy
		if (!name.startsWith(SLASH)) {
			name = SLASH + name;
		}
		name = name.replace('.', '/');
		final URL url = RTSI.class.getResource(name);
		if (url != null) {
			/*
			 * Null only if the jar file is not well constructed, i.e. if the
			 * directories do not appear alone in the jar file like here:
			 * 
			 * meta-inf/ meta-inf/manifest.mf commands/ <== IMPORTANT
			 * commands/Command.class commands/DoorClose.class
			 * commands/DoorLock.class commands/DoorOpen.class
			 * commands/LightOff.class commands/LightOn.class RTSI.class
			 */
			/* replace any URL space codes with actual spaces */
			final String urlsp = "%20";
			final String space = " ";
			final File directory = new File(url.getFile().replaceAll(urlsp,
					space));
			if (directory.exists()) {
				final File[] files = directory.listFiles();
				for (int i = 0; i < files.length; i++) {
					final String fname = files[i].getName();
					if (fname.endsWith(CLASS_EXT)) {
						final String classname = filenameToClassname(fname,
								pckgname);
						addToCollection(classname, tosubclass, DEF_LOADER, rval);
						// } else if (fname.endsWith(".jar")) {
						/* recursively add the results of the jar file? */
					} else {
						/* if a folder and recursing, add it's results */
						if (recurse && files[i].isDirectory()) {
							rval.addAll(findClassNames(pckgname + PERIOD
									+ files[i].getName(), tosubclass, true));
						}
					}
				}
			} else {
				rval.addAll(findClassNamesFromJarURL(url, tosubclass, name
						.substring(1)));
			}
		}
		return rval;
	}

	private Set<String> findClassNamesFromJarConnection(
			final Enumeration enumeration, final Class tosubclass,
			final String starts) {
		final SortedSet<String> rval = new TreeSet<String>();
		while (enumeration.hasMoreElements()) {
			final ZipEntry entry = (ZipEntry) enumeration.nextElement();
			final String classname = jarEntryToClassname(entry, starts);
			if (classname != null) {
				addToCollection(classname, tosubclass, DEF_LOADER, rval);
			}
		}
		return rval;
	}

	private Set<String> findClassNamesFromJarURL(final URL url,
			final Class tosubclass, final String starts) {
		JarURLConnection conn = null;
		JarFile jfile = null;
		final SortedSet<String> rval = new TreeSet<String>();
		boolean success = false;
		try {
			/*
			 * It does not work with the filesystem: we must be in the case of a
			 * package contained in a jar file.
			 */
			conn = (JarURLConnection) url.openConnection();
			jfile = conn.getJarFile();
			success = true;
		} catch (IOException ioex) {
			JOptionPane.showMessageDialog(null, ioex.getMessage(), rtsiName,
					JOptionPane.ERROR_MESSAGE);
			success = false;
		}
		if (success) {
			rval.addAll(findClassNamesFromJarConnection(jfile.entries(),
					tosubclass, starts));
		}
		return rval;
	}

	/**
	 * Creates own <code>ClassLoader</code> using the given classpath in order
	 * to find all classes which are assignable as the given class or interface.
	 * 
	 * @param tosubclass
	 *            type we are looking for
	 * @param classpath
	 *            string representing the folder at the base of the classpath
	 * @param file
	 *            where to start the search
	 * @param loader
	 *            the classloader, so we won't keep creating them in recursive
	 *            calls
	 * @return an alphabetically ordered set of classes assignable as
	 *         <code>tosubclass</code>
	 */
	private SortedSet<String> getClassesRecursively(final Class tosubclass,
			final String classpath, final File file, final ClassLoader loader) {
		final SortedSet<String> rval = new TreeSet<String>();
		if (file.isDirectory()) {
			final File[] list = file.listFiles();
			if (list!=null) {	//In case we don't have permission
				for (int i = 0; i < list.length; i++) {
					rval.addAll(getClassesRecursively(tosubclass, classpath,
							list[i], loader));
				}
			}
		} else { // we are only interested in .class files
			if (file.getName().endsWith(CLASS_EXT)) {
				final String temp = fileToClassname(file, classpath);
				try {
					final Class clazz = loader.loadClass(temp);
					if (canUseClassAs(tosubclass, clazz)) {
						rval.add(temp);
					}
				} catch (ClassNotFoundException cnfex) {//NOPMD
					//fall through and return what we have
				} catch (LinkageError le) {//NOPMD
					//fall through and return what we have
				}
			}
		}
		return rval;
	}

	/**
	 * Given the root of a classpath, find the given class and load it into
	 * memory, passing the reference to the Class back to the caller.
	 * 
	 * @param path
	 *            to search
	 * @param className
	 *            fully qualified classname
	 * @return the object referring to the Class, null if not found
	 */
	public Class loadClass(final File path, final String className) {
		Class rval = null;
		URL url = null;
		try {
			if (path != null) {
				url = path.toURL();
			}
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), rtsiName,
					JOptionPane.ERROR_MESSAGE);
		}
		if (url != null) {
			final ClassLoader loader = new URLClassLoader(new URL[] { url });
			try {
				rval = loader.loadClass(className);
			} catch (ClassNotFoundException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), rtsiName,
						JOptionPane.ERROR_MESSAGE);
			}
		}
		return rval;
	}

	private Set<Class<?>> nameSetToClassSet(final SortedSet<String> names,
			final ClassLoader loader) {
		final Set<Class<?>> rval = new LinkedHashSet<Class<?>>();
		final StringBuffer errmessage = new StringBuffer(
				"\nYou've probably incorrectly specified a classpath,\n")
				.append("or moved/renamed an existing .class file.\n");
		try {
			for (String name : names) {
				rval.add(loader.loadClass(name));
			}
		} catch (ClassNotFoundException e) {
			errmessage.append(e.getMessage());
			JOptionPane.showMessageDialog(null, errmessage, rtsiName,
					JOptionPane.ERROR_MESSAGE);
		}
		return rval;
	}

	private Class resolveClass(final String name) {
		Class tosubclass = null;
		try {
			tosubclass = Class.forName(name);
		} catch (ClassNotFoundException ex) {
			JOptionPane.showMessageDialog(null,
					"Class " + name + " not found!", rtsiName,
					JOptionPane.ERROR_MESSAGE);
		}
		return tosubclass;
	}
}
