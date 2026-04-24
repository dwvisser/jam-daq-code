package jam.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.RunState;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

/**
 * Holds component which displays the current run state.
 *
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
 * @version 2004-12-09
 * @see jam.global.RunState
 */
@Singleton
final class RunStateBox implements PropertyChangeListener {

  private final transient JLabel lrunState = new JLabel("   Welcome   ", SwingConstants.CENTER);

  private final transient JPanel pRunState = new JPanel();

  @Inject
  protected RunStateBox(final Broadcaster broadcaster) {
    super();
    broadcaster.addPropertyChangeListener(this);
    pRunState.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    pRunState.add(new JLabel(" Status: "));
    lrunState.setOpaque(true);
    pRunState.add(lrunState);
  }

  /**
   * Returns the displayable component.
   *
   * @return the displayable component
   */
  protected Component getComponent() {
    return pRunState;
  }

  private void setRunState(final RunState runState) {
    lrunState.setBackground(runState.getColor());
    lrunState.setText(runState.getLabel());
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    final BroadcastEvent event = (BroadcastEvent) evt;
    final BroadcastEvent.Command command = event.getCommand();
    if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
      setRunState((RunState) event.getContent());
    }
  }
}
