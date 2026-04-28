package jam;

import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Deals with JavaFX WebView-based User Guide (Markdown) and an "About" dialog.
 *
 * @author Ken Swartz
 * @author Dale Visser
 * @version version 1.0 - Modernized with JavaFX WebView
 */
public class Help extends JDialog {
  private static final int POS_X = 20;

  /**
   * Launches the User Guide in a JavaFX WebView displaying Markdown content.
   *
   * @param args ignored
   */
  public static void main(final String[] args) {
    setLookAndFeel();
    final JFrame frame = new JFrame("Jam User Guide");
    displayHelpInWebView(frame, "help/toc.md");
  }

  /**
   * Displays Markdown help content in a JavaFX WebView.
   *
   * @param frame parent frame
   * @param markdownResource path to Markdown resource (e.g., "help/toc.md")
   */
  public static void displayHelpInWebView(final JFrame frame, final String markdownResource) {
    try {
      final String basePath = markdownResource.substring(0, markdownResource.lastIndexOf('/') + 1);
      final URL resourceUrl =
          Thread.currentThread().getContextClassLoader().getResource(markdownResource);
      if (resourceUrl == null) {
        showErrorDialog(new IOException("Resource not found: " + markdownResource));
        return;
      }

      final String markdownContent =
          new String(resourceUrl.openStream().readAllBytes(), StandardCharsets.UTF_8);
      final String html = convertMarkdownToHtml(markdownContent);

      // Get base URL for the help directory
      final URL baseUrl = Thread.currentThread().getContextClassLoader().getResource(basePath);
      final String baseUrlString = baseUrl != null ? baseUrl.toString() : null;

      // Initialize JavaFX on Swing EDT
      final JFXPanel jfxPanel = new JFXPanel();
      Platform.runLater(
          () -> {
            final WebView webView = new WebView();
            webView.getEngine().loadContent(html);
            // Handle link clicks for Markdown files
            webView
                .getEngine()
                .locationProperty()
                .addListener(
                    (obs, oldLocation, newLocation) -> {
                      if (newLocation != null && newLocation.endsWith(".md")) {
                        // It's a Markdown link, load it
                        final String relativePath = basePath + newLocation;
                        try {
                          final URL mdUrl =
                              Thread.currentThread()
                                  .getContextClassLoader()
                                  .getResource(relativePath);
                          if (mdUrl != null) {
                            final String mdContent =
                                new String(
                                    mdUrl.openStream().readAllBytes(), StandardCharsets.UTF_8);
                            final String newHtml = convertMarkdownToHtml(mdContent);
                            webView.getEngine().loadContent(newHtml);
                          }
                        } catch (IOException e) {
                          showErrorDialog(e);
                        }
                      }
                    });
            final Scene scene = new Scene(webView, 800, 600);
            jfxPanel.setScene(scene);
          });

      frame.getContentPane().add(jfxPanel, BorderLayout.CENTER);

      final JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
      final JButton close = new JButton("Close");
      close.addActionListener(e -> frame.dispose());
      south.add(close);
      frame.getContentPane().add(south, BorderLayout.SOUTH);

      frame.setSize(800, 600);
      frame.setLocationRelativeTo(null);
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.setVisible(true);
    } catch (IOException e) {
      showErrorDialog(e);
    }
  }

  /**
   * Converts Markdown to HTML using CommonMark processor.
   *
   * @param markdown the Markdown content
   * @return HTML representation
   */
  private static String convertMarkdownToHtml(final String markdown) {
    final Parser parser = Parser.builder().build();
    final Node document = parser.parse(markdown);
    final HtmlRenderer renderer = HtmlRenderer.builder().build();
    final String html = renderer.render(document);

    // Wrap in HTML with basic styling
    return "<!DOCTYPE html>\n"
        + "<html>\n"
        + "<head>\n"
        + "<meta charset='UTF-8'>\n"
        + "<style>\n"
        + "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;"
        + " line-height: 1.6;          margin: 20px; color: #333; }\n"
        + "  h1, h2, h3 { color: #0066cc; }\n"
        + "  code { background-color: #f4f4f4; padding: 2px 6px; border-radius: 3px;        "
        + "  font-family: 'Courier New', monospace; }\n"
        + "  pre { background-color: #f4f4f4; padding: 10px; border-radius: 5px;        "
        + " overflow-x: auto; }\n"
        + "</style>\n"
        + "</head>\n"
        + "<body>\n"
        + html
        + "</body>\n"
        + "</html>";
  }

  private static void showErrorDialog(final Throwable throwable) {
    JOptionPane.showMessageDialog(
        null, throwable.getMessage(), throwable.getClass().getName(), JOptionPane.ERROR_MESSAGE);
  }

  private static void setLookAndFeel() {
    final String linux = "Linux";
    final String kunststoff = "com.incors.plaf.kunststoff.KunststoffLookAndFeel";
    boolean bKunststoff = linux.equals(System.getProperty("os.name")); // NOPMD
    if (bKunststoff) {
      try {
        UIManager.setLookAndFeel(kunststoff);
      } catch (ClassNotFoundException e) {
        bKunststoff = false;
      } catch (Exception e) { // all other exceptions
        final String title = "Jam--error setting GUI appearance";
        JOptionPane.showMessageDialog(null, e.getMessage(), title, JOptionPane.WARNING_MESSAGE);
      }
    }
    if (!bKunststoff) {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
        final String title = "Error setting GUI appearance";
        JOptionPane.showMessageDialog(null, e.getMessage(), title, JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  @Inject
  public Help(final JFrame frame, final LicenseReader licenseReader, final Version jamVersion) {
    super(frame, "University of Illinois/NCSA Open Source License", true);
    layoutLicenseDialog(licenseReader);
    final String defaultVal = "notseen";
    final String version = jamVersion.getName();
    final String key = "license";
    final Preferences helpnode = Preferences.userNodeForPackage(getClass());
    if (frame.isVisible() && !version.equals(helpnode.get(key, defaultVal))) {
      setVisible(true);
      helpnode.put(key, version);
    }
  }

  private void layoutLicenseDialog(final LicenseReader licenseReader) {
    final Container contents = this.getContentPane();
    this.setResizable(true);
    contents.setLayout(new BorderLayout());
    final JPanel center = new JPanel(new GridLayout(0, 1));
    final String text = licenseReader.getLicenseText();
    center.add(new JScrollPane(new JTextArea(text)));
    contents.add(center, BorderLayout.CENTER);
    final JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
    contents.add(south, BorderLayout.SOUTH);
    final JButton bok = new JButton("OK");
    bok.addActionListener(event -> dispose());
    south.add(bok);
    this.pack();
    final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    this.setSize(this.getWidth(), screen.height / 2);
    this.setLocation(POS_X, screen.height / 4);
  }
}
