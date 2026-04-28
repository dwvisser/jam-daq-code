package jam.commands;

import jam.Help;
import jam.global.CommandListenerException;
import javax.swing.JFrame;

/**
 * @author Ken Swartz
 */
final class ShowUserGuide extends AbstractCommand {

  ShowUserGuide() {
    super("User Guide\u2026");
  }

  /**
   * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
   */
  @Override
  protected void execute(final Object[] cmdParams) {
    final JFrame frame = new JFrame("Jam User Guide");
    Help.displayHelpInWebView(frame, "help/toc.md");
  }

  @Override
  protected void executeParse(final String[] cmdTokens) throws CommandListenerException {
    execute(null);
  }
}
