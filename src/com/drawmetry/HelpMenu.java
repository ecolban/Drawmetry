package com.drawmetry;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

/**
 *
 * @author Erik
 */
class HelpMenu extends JMenu {

    private JMenuItem helpMenuItem;
    private JMenuItem aboutMenuItem;
    private ClassLoader cl = getClass().getClassLoader();
    private Icon logo = new ImageIcon(cl.getResource(
            "images/logo128.png"));
    private static final String REVISION = "1.3";
    private static final Logger LOGGER = Logger
            .getLogger("com.drawmetry");

    private HyperlinkListener getHyperLinkListener(final JEditorPane editorPane) {
        return new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(final HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // TIP: Show hand cursor
                            SwingUtilities
                                    .getWindowAncestor(editorPane)
                                    .setCursor(
                                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            // TIP: Show URL as the tooltip
                            editorPane.setToolTipText(e.getURL()
                                    .toExternalForm());
                        }
                    });
                } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            // Show default cursor
                            SwingUtilities.getWindowAncestor(editorPane)
                                    .setCursor(Cursor.getDefaultCursor());

                            // Reset tooltip
                            editorPane.setToolTipText(null);
                        }
                    });
                } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    // TIP: Starting with JDK6 you can show the URL in desktop
                    // browser
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Cannot find the browser");
                        }
                    }
                    // System.out.println("Go to URL: " + e.getURL());
                }
            }
        };
    }

    HelpMenu() {
        setMnemonic('H');
        setText("Help");
//
//        helpMenuItem = new JMenuItem();
//        helpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
//        helpMenuItem.setText("Help Contents");
//        helpMenuItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                aboutMenuItemActionPerformed(e);
////                int ans = JOptionPane.showConfirmDialog(helpMenuItem,
////                        "Visit http://drawmetry.com for examples and documentation.\n"
////                        + "Go there now?",
////                        "Help Contents", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, logo);
////                if (ans == JOptionPane.YES_OPTION) {
////                    try {
////                        URL url = new URL("http://drawmetry.com");
////                        if (url != null) {
////                            showURL(url);
////                        }
////                    } catch (MalformedURLException ex) {
////                    }
////                }
//            }
//        });
//        add(helpMenuItem);
//        //--------------------------------------------------------------------
//        add(new JSeparator());

        aboutMenuItem = new JMenuItem();
        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About Drawmetry");
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                aboutMenuItemActionPerformed(e);

            }
        });
        add(aboutMenuItem);
    }

    private void aboutMenuItemActionPerformed(ActionEvent evt) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        final JEditorPane editorPane = new JEditorPane();

        // Enable use of custom set fonts
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
                Boolean.TRUE);
        editorPane.setFont(new Font("Arial", Font.BOLD, 13));

        editorPane.setPreferredSize(new Dimension(520, 180));
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane
                .setText("<html>"
                + "<body>"
                + "<table border='0px' cxellpadding='10px' height='100%'>"
                + "<tr>"
                + "<td valign='center'>"
                + "<img src=\""
                + cl.getResource("images/logo128.png")
                .toExternalForm()
                + "\">"
                + "</td>"
                + "<td align=center>"
                + "Drawmetry&trade; <br>"
                + REVISION
                + "<br/>"
                + "<br/>"
                + "Copyright &copy; 2013 <a href=\"mailto:support@drawmetry.com\">Erik Colban</a><br>"
                + "All Rights Reserved Worldwide<p>"
                + "<br/>"
                + "Visit <a href=\"http://drawmetry.com\"><b>drawmetry.com</b></a> for examples and documentation.<br>"
                + "</td>" + "</tr>" + "</table>" + "</body>"
                + "</html>");

        // TIP: Add Hyperlink listener to process hyperlinks
        editorPane.addHyperlinkListener(getHyperLinkListener(editorPane));
        JOptionPane.showMessageDialog(null, new JScrollPane(editorPane), "ABOUT",
                JOptionPane.PLAIN_MESSAGE);
    }
}
