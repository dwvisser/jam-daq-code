package injection;

import com.google.inject.Inject;
import com.google.inject.Provider;
import jam.commands.CommandManager;
import jam.global.CommandFinder;

/**
 * Provides the correct instance of CommandFinder for injection.
 *
 * @author Dale Visser
 */
public class CommandFinderProvider implements Provider<CommandFinder> {
  private final transient CommandFinder commandFinder;

  @Inject
  protected CommandFinderProvider(final CommandManager commandManager) {
    this.commandFinder = commandManager.getCommandFinder();
  }

  public CommandFinder get() {
    return this.commandFinder;
  }
}
