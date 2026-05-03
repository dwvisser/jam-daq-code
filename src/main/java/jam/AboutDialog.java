package jam;

import injection.GuiceInjector;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * The About Dialog
 *
 * @author Ken Swartz
 */
public class AboutDialog {

  private final transient JDialog dialog;

  /**
   * Constructs the "about" dialog.
   *
   * @param frame parent of the dialog
   */
  public AboutDialog(final Frame frame) {
    super();
    dialog = new JDialog(frame, "About Jam", false);
    final Container cad = dialog.getContentPane();
    dialog.setResizable(false);
    final int positionX = 20;
    final int positionY = 50;
    dialog.setLocation(positionX, positionY);
    cad.setLayout(new BorderLayout());
    final JPanel pcenter = new JPanel(new GridLayout(0, 1));
    final Border border = new EmptyBorder(20, 20, 20, 20);
    pcenter.setBorder(border);
    cad.add(pcenter, BorderLayout.CENTER);
    pcenter.add(
        new JLabel(
            "Jam v" + GuiceInjector.getObjectInstance(Version.class).getName(),
            SwingConstants.CENTER));
    pcenter.add(new JLabel("by", SwingConstants.CENTER));
    pcenter.add(new JLabel("Ken Swartz, Dale Visser, and John Baris", SwingConstants.CENTER));
    final String homeURL = "https://github.com/dwvisser/jam-daq-code/";
    final JTextPane urlPane = new JTextPane();
    urlPane.setContentType("text/html");
    urlPane.setText(
        "<html><body style='text-align:center;font-family:Sans-Serif;font-size:12px;'>"
            + "<a href=\""
            + homeURL
            + "\">"
            + homeURL
            + "</a></body></html>");
    urlPane.setEditable(false);
    urlPane.setOpaque(false);
    urlPane.setBorder(null);
    urlPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    urlPane.addHyperlinkListener(
        new HyperlinkListener() {
          @Override
          public void hyperlinkUpdate(final HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED
                && Desktop.isDesktopSupported()) {
              try {
                Desktop.getDesktop().browse(new URI(event.getURL().toString()));
              } catch (Exception ignored) {
                // Ignore errors opening the browser on unsupported platforms.
              }
            }
          }
        });
    pcenter.add(urlPane);
    final JPanel pButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
    cad.add(pButton, BorderLayout.SOUTH);
    final JButton bok = new JButton("OK");
    bok.addActionListener(event -> dialog.dispose());
    pButton.add(bok);
    dialog.pack();
    /* Receives events for closing the dialog box and closes it. */
    dialog.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(final WindowEvent event) {
            dialog.dispose();
          }
        });
  }

  /**
   * Gets the dialog.
   *
   * @return the dialog
   */
  public JDialog getDialog() {
    return dialog;
  }
}
