package jam.commands;

import javax.swing.*;

interface ActionCreator {
	Action getAction(String name);
}
