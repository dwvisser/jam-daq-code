/*
 * Created on Jun 10, 2004
 */
package jam.commands;

import jam.global.CommandListenerException;

import java.util.prefs.Preferences;

import javax.swing.ImageIcon;

/**
 * Abstract implementation of <code>Commandable</code> which can set/unset a
 * given preference.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 10, 2004
 */
public abstract class AbstractSetBooleanPreference extends AbstractCommand {// NOPMD

	AbstractSetBooleanPreference() {
		super();
	}

	/**
	 * Preference node which must be defined in full implementations.
	 */
	protected transient Preferences prefsNode;

	/**
	 * Name of the preference must be defined in full implementations.
	 */
	protected transient String key;

	/**
	 * Placeholder for the previous/next state.
	 */
	protected transient boolean state;

	private final static ImageIcon CHECK_MARK, CLEAR;

	static {
		final ClassLoader loader = ClassLoader.getSystemClassLoader();
		CHECK_MARK = new ImageIcon(loader
				.getResource("jam/commands/checkmark.png"));
		CLEAR = new ImageIcon(loader.getResource("jam/ui/clear.png"));
	}

	/**
	 * Set to <code>true</code> here. Set differently in <em>constructor</em>
	 * if necessary.
	 */
	protected transient boolean defaultState = true;

	/**
	 * Subclass initialization must be in initialization blocks or constructor.
	 * This method may not be overriden.
	 * 
	 * @see jam.commands.Commandable#initCommand()
	 */
	public final void initCommand() {
		synchronized (this) {
			state = prefsNode.getBoolean(key, defaultState);
		}
		changeIcon();
	}

	/**
	 * If there is at least one element, and it is a <code>Boolean</code>,
	 * set the state to the same value. Otherwise, toggle the state.
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected final void execute(final Object[] cmdParams) {
		synchronized (this) {
			if (cmdParams != null && cmdParams.length > 0) {
				final Object param0 = cmdParams[0];
				if (param0 instanceof Boolean) {
					final Boolean bParam0 = (Boolean) param0;
					state = bParam0.booleanValue();
				}
			} else {
				state = !state;
			}
			prefsNode.putBoolean(key, state);
			changeIcon();
		}
	}

	private void changeIcon() {
		synchronized (this) {
			putValue(SMALL_ICON, state ? CHECK_MARK : CLEAR);
		}
	}

	/**
	 * Sees if the first parameter is one of the items in the list. If so, sets
	 * preference to <code>true</code>, otherwise <code>false</code>. If
	 * there are no parameters, the state is toggled.
	 * 
	 * <ul>
	 * <li>on</li>
	 * <li>true</li>
	 * <li>1</li>
	 * </ul>
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected final void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		final Boolean[] pass = new Boolean[1];
		synchronized (this) {
			if (cmdTokens.length > 0) {
				final String token = cmdTokens[0];
				if (token.equalsIgnoreCase("true")
						|| token.equalsIgnoreCase("yes")
						|| token.equalsIgnoreCase("on")
						|| token.equalsIgnoreCase("1")) {
					pass[0] = Boolean.TRUE;
				} else {
					pass[0] = Boolean.FALSE;
				}
			} else {
				pass[0] = Boolean.valueOf(!state);
			}
			execute(pass);
		}
	}
}
