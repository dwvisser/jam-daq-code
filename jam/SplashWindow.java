package jam;

import injection.GuiceInjector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Generates the "splash" window that displays while Jam is launching. The
 * window disappears after a specified timeout period or when the user clicks on
 * it.
 */
class SplashWindow extends JWindow {

    private transient final URL urlNukeIcon;

    private transient final URL urlOSIGif;

    private static final Logger LOGGER = Logger.getLogger(SplashWindow.class
            .getPackage().getName());

    /**
     * Creates the splash window which will exist for as long as the specified
     * wait time in milliseconds.
     * @param frame
     *            parent frame
     * @param waitTime
     *            time in milliseconds after which the window disappears
     */
    public SplashWindow(final Frame frame, final int waitTime) {
        super(frame);
        final ClassLoader classLoader = getClass().getClassLoader();
        urlNukeIcon = classLoader.getResource("jam/nukeicon.png");
        if (urlNukeIcon == null) {
            JOptionPane.showMessageDialog(frame,
                    "Can't load resource: jam/nukeicon.png");
        }
        urlOSIGif = classLoader.getResource("jam/OSI.png");
        if (urlOSIGif == null) {
            JOptionPane.showMessageDialog(frame,
                    "Can't load resource: jam/OSI.gif");
        }
        drawWindow();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent mouseEvent) {
                setVisible(false);
                dispose();
            }
        });
        final int pause = waitTime;
        final Runnable closerRunner = new Runnable() {
            public void run() {
                setVisible(false);
                dispose();
            }
        };
        final Runnable waitRunner = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(pause);
                    SwingUtilities.invokeAndWait(closerRunner);
                } catch (Exception e) {
                    LOGGER.log(
                            Level.SEVERE,
                            "Exception occured trying to display splash window.",
                            e);
                    // can catch InvocationTargetException
                    // can catch InterruptedException
                }
            }
        };
        setVisible(true);
        final Thread splashThread = new Thread(waitRunner, "SplashThread");
        splashThread.start();
    }

    private void drawWindow() {
        final Container contents = getContentPane();
        final JPanel west = new JPanel(new FlowLayout());
        west.setBackground(Color.WHITE);
        west.setBorder(BorderFactory
                .createMatteBorder(1, 1, 0, 0, Color.BLACK));

        final ImageIcon nukeicon = new ImageIcon(urlNukeIcon);
        final int sizexy = 80;
        nukeicon.setImage(nukeicon.getImage().getScaledInstance(sizexy,
                sizexy, Image.SCALE_SMOOTH));
        west.add(new JLabel(nukeicon));
        final JPanel center = new JPanel(new GridLayout(0, 1));
        center.setBackground(Color.WHITE);
        center.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                Color.BLACK));
        center.add(new JLabel("Jam: Data Acquisition for Nuclear Physics"));
        center.add(new JLabel("\u00a9 2002 Yale University"));
        center.add(new JLabel(
                "University of Illinois/NCSA Open Source License"));

        center.add(new JLabel("See Help|License... for license text."));
        final JPanel east = new JPanel(new FlowLayout());
        east.setBackground(Color.WHITE);
        east.setBorder(BorderFactory
                .createMatteBorder(1, 0, 0, 1, Color.BLACK));
        final JLabel osi = new JLabel(new ImageIcon(urlOSIGif));
        osi.setToolTipText("Open Source Initiative. See http://www.opensource.org/");
        east.add(osi);
        final JPanel panelSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JLabel versionLabel = new JLabel("v. "
                + GuiceInjector.getObjectInstance(Version.class).getName(),
                SwingConstants.RIGHT);
        panelSouth.add(versionLabel);
        panelSouth.setBackground(Color.CYAN);
        panelSouth.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1,
                Color.BLACK));
        contents.add(panelSouth, BorderLayout.SOUTH);
        contents.add(west, BorderLayout.WEST);
        contents.add(center, BorderLayout.CENTER);
        contents.add(east, BorderLayout.EAST);
        pack();
        final Dimension screenSize = Toolkit.getDefaultToolkit()
                .getScreenSize();
        final Dimension labelSize = getSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2),
                screenSize.height / 2 - (labelSize.height / 2));
    }

    /**
     * Displays this window for up to a minute.
     * @param args
     *            ignored
     */
    static public void main(final String[] args) {
        new SplashWindow(null, 60000);
    }

}
