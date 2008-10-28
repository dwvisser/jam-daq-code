package jam.commands;

import javax.swing.Action;

interface ActionCreator {
	Action getAction(String name);
}
