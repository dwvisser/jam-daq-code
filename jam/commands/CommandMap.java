package jam.commands;

import jam.global.CommandFinder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Lookup map for commands in jam.commands.
 * 
 * @author Dale Visser
 * 
 */
public class CommandMap implements CommandFinder {

	private transient final Map<String, Class<? extends Commandable>> map = Collections
			.synchronizedMap(new HashMap<String, Class<? extends Commandable>>());

	private transient final ActionCreator actionCreator;

	CommandMap(final ActionCreator actionCreator) {
		/* File Menu */
		this.actionCreator = actionCreator;
		this.readXML();
	}

	public Collection<String> getSimilar(final String string,
			final boolean onlyEnabled) {
		final String lowerCase = string.toLowerCase(Locale.US);
		final TreeSet<String> rval = new TreeSet<String>();
		for (int i = lowerCase.length(); i >= 1; i--) {
			final String com = lowerCase.substring(0, i);
			for (String element : this.map.keySet()) {
				final String key = element;
				if (key.startsWith(com)) {
					final boolean addIt = (!onlyEnabled)
							|| this.actionCreator.getAction(key).isEnabled();
					if (addIt) {
						rval.add(key);
					}
				}
			}
			if (!rval.isEmpty()) {
				break;
			}
		}
		return Collections.unmodifiableCollection(rval);
	}

	protected Class<? extends Commandable> get(final String name) {
		return map.get(name.toLowerCase(Locale.US));
	}

	/**
	 * @return all commands in the map in alphabetical order
	 */
	public Collection<String> getAll() {
		return new TreeSet<String>(map.keySet());
	}

	protected boolean containsKey(final String key) {
		return this.map.containsKey(key.toLowerCase(Locale.US));
	}

	@SuppressWarnings("unchecked")
	private void readXML() {
		final ClassLoader loader = ClassLoader.getSystemClassLoader();
		final InputStream file = loader
				.getResourceAsStream("jam/commands/CommandMap.xml");
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		boolean success = false;
		Throwable throwable = null;
		try {
			final DocumentBuilder builder = dbf.newDocumentBuilder();
			final Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();
			final NodeList packages = doc.getElementsByTagName("Package");
			for (int namespaceIndex = 0; namespaceIndex < packages.getLength(); namespaceIndex++) {
				final Element currentPackage = (Element) packages
						.item(namespaceIndex);
				final String packageName = currentPackage.getAttributes()
						.getNamedItem("name").getNodeValue();
				final NodeList actionList = currentPackage
						.getElementsByTagName("Action");
				for (int actionIndex = 0; actionIndex < actionList.getLength(); actionIndex++) {
					final Node action = actionList.item(actionIndex);
					final NamedNodeMap attributes = action.getAttributes();
					final String className = attributes.getNamedItem("class")
							.getNodeValue();
					final String commandName = attributes.getNamedItem("name")
							.getNodeValue();
					final Class<? extends Commandable> clazz = (Class<? extends Commandable>) Class
							.forName(packageName + '.' + className);
					this.put(commandName, clazz);
				}

				success = true;
			}
		} catch (ParserConfigurationException pce) {
			throwable = pce;
		} catch (IOException ioe) {
			throwable = ioe;
		} catch (SAXException saxe) {
			throwable = saxe;
		} catch (ClassNotFoundException cnfe) {
			throwable = cnfe;
		}

		// Fail fast and hard.
		if (!success) {
			throw new IllegalStateException(throwable);
		}
	}

	private void put(final String name, final Class<? extends Commandable> clazz) {
		final String lowerCaseName = name.toLowerCase(Locale.US);
		if (this.map.containsKey(lowerCaseName)) {
			throw new IllegalArgumentException("Map already contains '"
					+ lowerCaseName + "'.");
		}

		this.map.put(lowerCaseName, clazz);
	}
}
