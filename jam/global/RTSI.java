/**
 * RTSI.java
 *
 * Created: Wed Jan 24 11:15:02 2001
 *
 */
package jam.global;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.*;

/**
 * This utility class is looking for all the classes implementing or 
 * inheriting from a given interface or class.
 * (RunTime Subclass Identification)
 *
 * @author <a href="mailto:daniel@satlive.org">Daniel Le Berre</a>
 * @version 1.0
 */
public class RTSI {

	/**
	 * Display all the classes inheriting or implementing a given
	 * class in the currently loaded packages.
	 * 
	 * @param tosubclassname the name of the class to inherit from
	 */
	private static void find(String tosubclassname, boolean recurse) {
		try {
			Class tosubclass = Class.forName(tosubclassname);
			Package[] pcks = Package.getPackages();
			System.out.println("Packages:");
			for (int i = 0; i < pcks.length; i++) {
				System.out.println("\t" + pcks[i].getName());
			}
			for (int i = 0; i < pcks.length; i++) {
				Collection c = find(pcks[i].getName(), tosubclass, recurse);
				if (!c.isEmpty()) {
					Iterator it = c.iterator();
					while (it.hasNext()) {
						Class cl = (Class) it.next();
						System.out.println("Found class: " + cl.getName());
					}
				}
			}
		} catch (ClassNotFoundException ex) {
			System.err.println("Class " + tosubclassname + " not found!");
		}
	}

	/**
	 * Display all the classes inheriting or implementing a given
	 * class in a given package.
	 * 
	 * @param pckgname the fully qualified name of the package
	 * @param tosubclass the name of the class to inherit from
	 */
	private static void find(
		String pckname,
		String tosubclassname,
		boolean recurse) {
		try {
			Class tosubclass = Class.forName(tosubclassname);
			Iterator result = find(pckname, tosubclass, recurse).iterator();
			System.out.println(
				"Find classes assignable as "
					+ tosubclass.getName()
					+ " in \""
					+ pckname
					+ "\"");
			while (result.hasNext()) {
				System.out.println("\t" + ((Class) result.next()).getName());
			}
		} catch (ClassNotFoundException ex) {
			System.err.println("Class " + tosubclassname + " not found!");
		}
		System.out.println("done.");
	}

	public static Set find(
		String pckgname,
		Class tosubclass,
		boolean recurse) {
		Iterator i = findClassNames(pckgname, tosubclass, recurse).iterator();
		Set rval = new LinkedHashSet(); //preserves order of add()'s
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		try {
			while (i.hasNext()) {
				rval.add(loader.loadClass((String) (i.next())));
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
		SortedSet rval = new TreeSet();
		String name = new String(pckgname); //copy
		if (!name.startsWith("/")) {
			name = "/" + name;
		}
		name = name.replace('.', '/');

		/* Get a File object for the package */
		URL url = RTSI.class.getResource(name);
		/* Happens only if the jar file is not well constructed, i.e.
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
		if (url == null)
			return rval;

		/* next command deals with quirk when there's a space 
		 * in a folder name */
		String s_file = url.getFile() /*.replaceAll("%20"," ")*/;
		//replaceAll only works in JDK1.4
		s_file = replaceURLspaces(s_file);
		File directory = new File(s_file);
		//System.out.println("Location of package "+pckgname+" is "+directory.getAbsolutePath());
		if (directory.exists()) {
			/* Get the list of the files contained 
			 * in the package */
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				String fname = files[i].getName();
				/* we are only interested in .class files */
				if (fname.endsWith(".class")) {
					/* removes the .class extension */
					String classname =
						pckgname + "." + fname.substring(0, fname.length() - 6);
					try {
						//System.out.println(classname);
						Class c =
							ClassLoader.getSystemClassLoader().loadClass(
								classname);
						if (tosubclass.isAssignableFrom(c)) {
							rval.add(classname);
						}
					} catch (ClassNotFoundException cnfex) {
						System.err.println(cnfex);
					} //how to deal with illegal files?
				} else if (fname.endsWith(".jar")) {
					/* recursively add the results of the jar file? */

				} else {
					/*if a folder, assume it's a package and add it's results 
					 * recursively*/
					if (recurse) {
						if (files[i].isDirectory()) {
							//System.out.println(files[i]);
							rval.addAll(
								findClassNames(
									pckgname + "." + files[i].getName(),
									tosubclass,
									true));
						}
					}
				}
			}
		} else {
			try {
				/* It does not work with the filesystem: we must
				   be in the case of a package contained in a jar file. */
				JarURLConnection conn = (JarURLConnection) url.openConnection();
				String starts = conn.getEntryName();
				JarFile jfile = conn.getJarFile();
				Enumeration e = jfile.entries();
				while (e.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) e.nextElement();
					String entryname = entry.getName();
					if (entryname.startsWith(starts)
						&& (entryname.lastIndexOf('/') <= starts.length())
						&& entryname.endsWith(".class")) {
						String classname =
							entryname.substring(0, entryname.length() - 6);
						if (classname.startsWith("/"))
							classname = classname.substring(1);
						classname = classname.replace('/', '.');
						try {
							/* Try to create an instance of the object */
							Class c = Class.forName(classname);
							if (tosubclass.isAssignableFrom(c)) {
								rval.add(classname);
							}
						} catch (ClassNotFoundException cnfex) {
							System.err.println(cnfex);
						}
					}
				}
			} catch (IOException ioex) {
				System.err.println(ioex);
			}
		}
		return rval;
	}

	/**
	 * Replaces %20 with spaces.  Writing this to avoid
	 * using regex and maintain compatibility with JDK 1.3
	 */
	private static String replaceURLspaces(String in) {
		int index;
		String rval = new String(in);
		do {
			index = rval.lastIndexOf("%20");
			if (index > -1) {
				String temp =
					rval.substring(0, index)
						+ " "
						+ rval.substring(index + 3, rval.length());
				rval = temp;
			}
		} while (index > -1);
		return rval;
	}

	/**
	 * Find all the classes inheriting or implementing a given
	 * class in a given package and any sub-packages.
	 * WARNING: a jar file as classpath hasn't been implemented.
	 * 
	 * @param classpath folder containing the classpath to search
	 * @param tosubclass the Class object to be assignable to
	 * @return an alphabetically ordered set of classes assignable as requested
	 */
	public static Set find(File classpath, Class tosubclass) {
		ClassLoader loader = null;
		URL url = null;
		/* */
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
		if (loader == null) {
			//use default system loader if creation unsuccessful
			Object temp = new Object();
			loader = temp.getClass().getClassLoader();
		}
		if (classpath == null) {
			return new HashSet(); //hack to skip an empty path
		}
		SortedSet nameSet =
			getClassesRecursively(
				tosubclass,
				classpath.getAbsolutePath(),
				classpath,
				loader);
		/* create the set of classes, preserving the name order */
		Iterator it = nameSet.iterator();
		Set rval = new LinkedHashSet(); //to guarantee order is preserved
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
	 * Creates own <code>ClassLoader</code> using the given classpath in order to find all classes which are 
	 * assignable as the given class or interface.
	 * 
	 * @param tosubclass type we are looking for
	 * @param classpath string representing the folder at the base of the classpath
	 * @param file where to start the search
	 * @param loader the classloader, so we won't keep creating them in recursive calls
	 * @return an alphabetically ordered set of classes assignable as <code>tosubclass</code>
	 */
	private static SortedSet getClassesRecursively(
		Class tosubclass,
		String classpath,
		File file,
		ClassLoader loader) {
		SortedSet rval = new TreeSet();
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			for (int i = 0; i < list.length; i++) {
				rval.addAll(
					getClassesRecursively(
						tosubclass,
						classpath,
						list[i],
						loader));
			}
		} else { // we are only interested in .class files 
			if (file.getName().endsWith(".class")) {
				String fullpath = file.getPath();
				/* removes the .class extension */
				String temp = fullpath.substring(0, fullpath.length() - 5);
				if (temp.startsWith(classpath)) {
					temp =
						temp.substring(classpath.length(), temp.length() - 1);
				}
				temp = temp.replace(File.separatorChar, '.');
				if (temp.startsWith(".")) {
					temp = temp.substring(1);
				}
				try {
					Class c = loader.loadClass(temp);
					if (tosubclass.isAssignableFrom(c)) {
						rval.add(temp);
					}
				} catch (ClassNotFoundException cnfex) {
					System.err.println(cnfex);
				}
			}
		}
		return rval;
	}

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
			ClassLoader loader = new URLClassLoader(new URL[] { url });
			try {
				rval = loader.loadClass(className);
			} catch (ClassNotFoundException e) {
				System.err.println(e);
			}
		}
		return rval;
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			find(args[0], args[1], true);
		} else {
			if (args.length == 1) {
				find(args[0], true);
			} else {
				System.out.println("Usage: java RTSI [<package>] <subclass>");
			}
		}
	}
} // RTSI
