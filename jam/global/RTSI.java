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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * This utility class is looking for all the classes implementing or 
 * inheriting from a given interface or class.
 * (RunTime Subclass Identification)
 *
 * @author <a href="mailto:daniel@satlive.org">Daniel Le Berre</a>
 * @version 1.0
 */
public class RTSI {

	static final String period = ".";
	static final String classext = ".class";
	static final String slash = "/";
	static final ClassLoader defaultLoader = ClassLoader.getSystemClassLoader();

	/**
	 * Display all the classes inheriting or implementing a given
	 * class in the currently loaded packages.
	 * 
	 * @param tosubclassname the name of the class to inherit from
	 * @param recurse whether to recurse into subfolders
	 */
	private static void find(String tosubclassname, boolean recurse) {
		final Class tosubclass = resolveClass(tosubclassname);
		if (tosubclass != null) {
			final Package[] pcks = Package.getPackages();
			System.out.println("Packages:");
			for (int i = 0; i < pcks.length; i++) {
				System.out.println("\t" + pcks[i].getName());
			}
			for (int i = 0; i < pcks.length; i++) {
				final Collection coll =
					find(pcks[i].getName(), tosubclass, recurse);
				if (!coll.isEmpty()) {
					final Iterator it = coll.iterator();
					while (it.hasNext()) {
						final Class cl = (Class) it.next();
						System.out.println("Found class: " + cl.getName());
					}
				}
			}
		}
	}

	private static Class resolveClass(String name) {
		Class tosubclass = null;
		try {
			tosubclass = Class.forName(name);
		} catch (ClassNotFoundException ex) {
			System.err.println("Class " + name + " not found!");
			tosubclass = null;
		}
		return tosubclass;
	}

	/**
	 * Display all the classes inheriting or implementing a given
	 * class in a given package.
	 * 
	 * @param pckname the fully qualified name of the package
	 * @param tosubclassname the name of the class to inherit from
	 * @param recurse try all sub-packages as well if true
	 */
	private static void find(
		String pckname,
		String tosubclassname,
		boolean recurse) {
		final Class tosubclass = resolveClass(tosubclassname);
		final Iterator result = find(pckname, tosubclass, recurse).iterator();
		System.out.println(
			"Find classes assignable as "
				+ tosubclass.getName()
				+ " in \""
				+ pckname
				+ "\"");
		while (result.hasNext()) {
			System.out.println("\t" + ((Class) result.next()).getName());
		}
		System.out.println("done.");
	}

	/**
	 * @return true if <code>c</code> can have instances and is 
	 * assignable as <code>tosubclass</code>
	 * @param tosubclass to superclass we desire instances of
	 * @param cl the candidate class to check
	 */
	public static boolean canUseClassAs(Class tosubclass, Class cl) {
		return tosubclass.isAssignableFrom(cl)
			&& ((cl.getModifiers() & Modifier.ABSTRACT) == 0);
	}

	/**
	 * @return a set of unique classes that can have instances and are 
	 * assignable as <code>tosubclass</code>.
	 *
	 * @param pckgname the package to search
	 * @param tosubclass the superclass we desire implementations of
	 * @param recurse whether to recurse through sub-packages
	 */
	public static Set find(
		String pckgname,
		Class tosubclass,
		boolean recurse) {
		final Iterator it =
			findClassNames(pckgname, tosubclass, recurse).iterator();
		final Set rval = new LinkedHashSet(); //preserves order of add()'s
		try {
			while (it.hasNext()) {
				rval.add(defaultLoader.loadClass((String) (it.next())));
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e.getMessage());
		}
		return rval;
	}

	/**
	 * Find all the classes inheriting or implementing a given
	 * class in a given package (but it does not search any 
	 * sub-packages).
	 * 
	 * @param pckgname the fully qualified name of the package
	 * @param tosubclass the Class object to inherit from
	 * @param recurse whether to traverse subpackages recursively
	 * @return an unordered list of classes assignable as requested
	 */
	private static Set findClassNames(
		String pckgname,
		Class tosubclass,
		boolean recurse) {
		/* Code from JWhich
		 * Translate the package name into an absolute path */
		final SortedSet rval = new TreeSet();
		String name = new String(pckgname); //copy
		if (!name.startsWith(slash)) {
			name = slash + name;
		}
		name = name.replace('.', '/');
		final URL url = RTSI.class.getResource(name);
		if (url != null) {
			/* Null only if the jar file is not well constructed, i.e.
			 * if the directories do not appear alone in the jar file like here:
			 * 
			 *          meta-inf/
			 *          meta-inf/manifest.mf
			 *          commands/                  <== IMPORTANT
			 *          commands/Command.class
			 *          commands/DoorClose.class
			 *          commands/DoorLock.class
			 *          commands/DoorOpen.class
			 *          commands/LightOff.class
			 *          commands/LightOn.class
			 *          RTSI.class
			 */
			/* replace any URL space codes with actual spaces */
			final String urlsp = "%20";
			final String sp = " ";
			final File directory =
				new File(url.getFile().replaceAll(urlsp, sp));
			if (directory.exists()) {
				final File[] files = directory.listFiles();
				for (int i = 0; i < files.length; i++) {
					final String fname = files[i].getName();
					if (fname.endsWith(classext)) {
						final String classname =
							filenameToClassname(fname, pckgname);
						addToCollection(
							classname,
							tosubclass,
							defaultLoader,
							rval);
						//} else if (fname.endsWith(".jar")) {
						/* recursively add the results of the jar file? */
					} else {
						/* if a folder and recursing, add it's results */
						if (recurse && files[i].isDirectory()) {
							rval.addAll(
								findClassNames(
									pckgname + period + files[i].getName(),
									tosubclass,
									true));
						}
					}
				}
			} else {
				rval.addAll(findClassNamesFromJarURL(url, tosubclass));
			}
		}
		return rval;
	}

	/**
	 * Adds class name to the given collection if it instances of the 
	 * given superclass can be made with it. If not, does nothing.
	 * 
	 * @param classname the class name in question
	 * @param tosubclass the superclass
	 * @param loader the class loader we need to resolve the classname 
	 * with
	 * @param coll the collection to add to
	 */
	private static void addToCollection(
		String classname,
		Class tosubclass,
		ClassLoader loader,
		Collection coll) {
		try {
			final Class cl = loader.loadClass(classname);
			if (canUseClassAs(tosubclass, cl)) {
				coll.add(classname);
			}
		} catch (ClassNotFoundException cnfex) {
			System.err.println(cnfex);
		}
	}

	private static Set findClassNamesFromJarURL(URL url, Class tosubclass) {
		JarURLConnection conn = null;
		JarFile jfile = null;
		final SortedSet rval = new TreeSet();
		boolean success = false;
		try {
			/* It does not work with the filesystem: we must
			   be in the case of a package contained in a jar file. */
			conn = (JarURLConnection) url.openConnection();
			jfile = conn.getJarFile();
			success = true;
		} catch (IOException ioex) {
			System.err.println(ioex);
			success = false;
		}
		if (success) {
			rval.addAll(
				findClassNamesFromJarConnection(conn, jfile, tosubclass));
		}
		return rval;
	}

	private static Set findClassNamesFromJarConnection(
		JarURLConnection conn,
		JarFile jfile,
		Class tosubclass) {
		final SortedSet rval = new TreeSet();
		final String starts = conn.getEntryName();
		final Enumeration e = jfile.entries();
		while (e.hasMoreElements()) {
			final ZipEntry entry = (ZipEntry) e.nextElement();
			final String classname = jarEntryToClassname(entry, starts);
			if (classname != null) {
				addToCollection(classname, tosubclass, defaultLoader, rval);
			}
		}
		return rval;
	}

	private static String jarEntryToClassname(ZipEntry entry, String starts) {
		final String entryname = entry.getName();
		String classname = null;
		if (entryname.startsWith(starts)
			&& (entryname.lastIndexOf('/') <= starts.length())
			&& entryname.endsWith(classext)) {
			classname =
				entryname.substring(0, entryname.length() - classext.length());
			if (classname.startsWith(slash)) {
				classname = classname.substring(1);
			}
			classname = classname.replace('/', '.');
		}
		return classname;
	}

	/**
	 * @param f filename minus any path
	 * @param pckg the string representing the package for the filename
	 * @return representation of the proper full reference to the class
	 */
	private static String filenameToClassname(String f, String pckg) {
		final StringBuffer rval = new StringBuffer(pckg);
		rval.append('.');
		rval.append(f.substring(0, f.length() - classext.length()));
		return rval.toString();
	}

	/**
	 * 
	 * @param f file containing the class
	 * @param classpath representation of the proper classpath for 
	 * loading this file
	 * @return representation of the proper full reference to the class
	 */
	private static String fileToClassname(File f, String classpath) {
		final String fullpath = f.getPath();
		String temp =
			fullpath.substring(0, fullpath.length() - classext.length());
		if (temp.startsWith(classpath)) {
			temp = temp.substring(classpath.length(), temp.length());
		}
		temp = temp.replace(File.separatorChar, '.');
		if (temp.startsWith(period)) {
			temp = temp.substring(1);
		}
		return temp;
	}

	/**
	 * Find all the classes inheriting or implementing a given
	 * class in a given package and any sub-packages.
	 * WARNING: a jar file as classpath hasn't been implemented.
	 * 
	 * @param classpath folder containing the classpath to search
	 * @param tosubclass the Class object to be assignable to
	 * @return an alphabetically ordered set of classes assignable as 
	 * requested
	 */
	public static Set find(File classpath, Class tosubclass) {
		final Set rval = new LinkedHashSet(); //to guarantee order is preserved
		ClassLoader loader = defaultLoader;
		URL url = null;
		if (classpath != null) {
			try {
				url = classpath.toURL();
			} catch (MalformedURLException e) {
				System.err.println(e.getMessage());
			}
			if (url != null) {
				loader = new URLClassLoader(new URL[] { url });
			}
		}
		if (classpath != null) {
			/* create the set of classes, preserving the name order */
			rval.addAll(
				nameSetToClassSet(
					getClassesRecursively(
						tosubclass,
						classpath.getAbsolutePath(),
						classpath,
						loader),
					loader));
		}
		return rval;
	}

	private static Set nameSetToClassSet(SortedSet ns, ClassLoader loader) {
		final Set rval = new LinkedHashSet();
		final Iterator it = ns.iterator();
		try {
			while (it.hasNext()) {
				rval.add(loader.loadClass((String) it.next()));
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e.getMessage());
		}
		return rval;
	}

	/**
	 * Creates own <code>ClassLoader</code> using the given classpath in 
	 * order to find all classes which are 
	 * assignable as the given class or interface.
	 * 
	 * @param tosubclass type we are looking for
	 * @param classpath string representing the folder at the base of the
	 * classpath
	 * @param file where to start the search
	 * @param loader the classloader, so we won't keep creating them in 
	 * recursive calls
	 * @return an alphabetically ordered set of classes assignable as 
	 * <code>tosubclass</code>
	 */
	private static SortedSet getClassesRecursively(
		Class tosubclass,
		String classpath,
		File file,
		ClassLoader loader) {
		final SortedSet rval = new TreeSet();
		if (file.isDirectory()) {
			final File[] list = file.listFiles();
			for (int i = 0; i < list.length; i++) {
				rval.addAll(
					getClassesRecursively(
						tosubclass,
						classpath,
						list[i],
						loader));
			}
		} else { // we are only interested in .class files 
			if (file.getName().endsWith(classext)) {
				final String temp = fileToClassname(file, classpath);
				try {
					final Class cl = loader.loadClass(temp);
					if (canUseClassAs(tosubclass, cl)) {
						rval.add(temp);
					}
				} catch (ClassNotFoundException cnfex) {
					System.err.println(cnfex);
				}
			}
		}
		return rval;
	}

	/**
	 * Given the root of a classpath, find the given class and 
	 * load it into memory, passing the reference to the Class
	 * back to the caller.
	 *
	 * @param path to search
	 * @param className fully qualified classname
	 * @return the object referring to the Class, null if not found
	 */
	public static Class loadClass(File path, String className) {
		Class rval = null;
		URL url = null;
		try {
			if (path != null) {
				url = path.toURL();
			}
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
		}
		if (url == null) {
			rval = null;
		} else {
			final ClassLoader loader = new URLClassLoader(new URL[] { url });
			try {
				rval = loader.loadClass(className);
			} catch (ClassNotFoundException e) {
				System.err.println(e);
			}
		}
		return rval;
	}

	/**
	* Find all valid instantiatable subclasses of the given classname.
	* The first argument is the classname, and it is assumed the whole
	* classpath will be searched if no other argument is given. If a 
	* second argument is given, the first argument is the package to 
	* search.
	*
	* @param args the command-line arguments
	*/
	public static void main(String[] args) {
		final String usage = "Usage: java RTSI [<package>] <subclass>";
		final int minargs = 1;
		if (args.length > minargs) {
			find(args[0], args[1], true);
		} else {
			if (args.length == minargs) {
				find(args[0], true);
			} else {
				System.out.println(usage);
			}
		}
	}
}
