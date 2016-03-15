/*
 * DMFrame.java
 *
 * Created on February 17, 2004, 8:28 PM
 */
package com.drawmetry;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

/**
 *
 * @author  Erik Colban
 */
public class DmFrame extends JFrame implements Runnable, WindowListener, ComponentListener {

    /*****************************************************************
     * GUI components
     *****************************************************************/
    private JPanel toolBarPanel;
    private JPanel mainPanel;
    private static int initialWindowWidth = 1020;
    private static int initialWindowHeight = 680;
    private static int toolBarHGap = 0;
    private static int toolBarVGap = 2;
    /************************************************************
     * Private variables
     ************************************************************/
    private DrawingPane drawingPane;
    private FileContext fileContext;

    /*************************************************************
     * The main method
     *************************************************************/
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new DmFrame());
    }

    // ========================================================================
    // Runnable implementation
    // ========================================================================
    public void run() {
        boolean secure = false;
        setTitle("Drawmetry");
        Image icon = null;
        try {
            URL iconURL = getClass().getClassLoader().getResource("images/logo64.png");
            if (iconURL != null) {
                icon = ImageIO.read(iconURL);
            } else {
                System.err.println("Cannot load icon!");
            }
        } catch (IOException ex) {
            System.err.println("Cannot load icon!");
        }
        if (icon != null) {
            setIconImage(icon);
        }
        setPreferredSize(new Dimension(initialWindowWidth, initialWindowHeight));
        setLocationByPlatform(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        addComponentListener(this);
        drawingPane = new DrawingPane();
        fileContext = FileContext.getFileContext(this, secure);
        fileContext.addPropertyChangeListener(FileContext.PROP_CURRENT_DOCUMENT, drawingPane);
        drawingPane.addPropertyChangeListener(DrawingPane.PROP_VIEW_MAP, fileContext);

        //Toolbars
        toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, toolBarHGap, toolBarVGap));
        getContentPane().add(toolBarPanel, BorderLayout.NORTH);

        //fileToolBar
        JToolBar fileToolBar = new FileToolBar(fileContext, drawingPane);
        toolBarPanel.add(fileToolBar);

        //editToolBar
        EditToolBar editToolBar = new EditToolBar(drawingPane);
        toolBarPanel.add(editToolBar);
        fileContext.addPropertyChangeListener(editToolBar);
        drawingPane.addPropertyChangeListener(editToolBar);

        //drawToolBar
        JToolBar drawToolBar = new DrawToolBar(fileContext, drawingPane);
        toolBarPanel.add(drawToolBar);

        //formatToolBar
        JToolBar formatToolBar = new FormatToolBar(fileContext, drawingPane);
        toolBarPanel.add(formatToolBar);

        //mainPanel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        //constraintToolBar
        JToolBar constraintToolBar = new ConstraintToolBar(drawingPane);
        mainPanel.add(constraintToolBar, BorderLayout.NORTH);

        //drawingPane
        mainPanel.add(drawingPane, BorderLayout.CENTER);
        drawingPane.requestFocusInWindow();

        // ====================================================================
        // Menu bar
        // ====================================================================
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuBar.setBackground(new Color(204, 204, 204));

        // fileMenu
        FileMenu fileMenu = new FileMenu(this, fileContext, drawingPane);
        fileContext.addPropertyChangeListener(FileContext.PROP_SAVE_ENABLED, fileMenu);
        menuBar.add(fileMenu);

        // editMenu
        EditMenu editMenu = new EditMenu(drawingPane);
        fileContext.addPropertyChangeListener(FileContext.PROP_UNDO_ENABLED, editMenu);
        fileContext.addPropertyChangeListener(FileContext.PROP_REDO_ENABLED, editMenu);
        drawingPane.addPropertyChangeListener(DrawingPane.PROP_COPY_ENABLED, editMenu);
        drawingPane.addPropertyChangeListener(DrawingPane.PROP_PASTE_ENABLED, editMenu);
        menuBar.add(editMenu);

        // viewMenu
        JMenu viewMenu = new ViewMenu(drawingPane);
        menuBar.add(viewMenu);

        //insertMenu
        JMenu insertMenu = new InsertMenu(drawingPane);
        menuBar.add(insertMenu);

        // formatMenu
        JMenu formatMenu = new FormatMenu(drawingPane);
        menuBar.add(formatMenu);

        // toolsMenu
        JMenu toolsMenu = new ToolMenu(drawingPane);
        menuBar.add(toolsMenu);

        // documentMenu
        DocumentMenu documentMenu = new DocumentMenu(this);
        fileContext.addPropertyChangeListener(FileContext.PROP_CURRENT_DOCUMENT, documentMenu);
        menuBar.add(documentMenu);

        // helpMenu
        JMenu helpMenu = new HelpMenu();
        menuBar.add(helpMenu);

        // ====================================================================
        // Feedback Window
        // ====================================================================
        final JLabel feedbackLabel = new JLabel();
        feedbackLabel.setFont(new Font("Arial", 0, 10));
        feedbackLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        feedbackLabel.setOpaque(true);
        feedbackLabel.setPreferredSize(new Dimension(200, 20));
        drawingPane.addPropertyChangeListener(DrawingPane.PROP_FEEDBACK,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        feedbackLabel.setText((String) evt.getNewValue());
                    }
                });

        mainPanel.add(feedbackLabel, BorderLayout.SOUTH);

        // ====================================================================
        // Popup Menu
        // ====================================================================
        final JPopupMenu popupMenu = new DmPopup(drawingPane);
        drawingPane.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3
                        || e.getButton() == MouseEvent.BUTTON1 && e.isControlDown()) {
                    popupMenu.show(drawingPane, e.getX(), e.getY());
                }
            }
        });
        //
        fileContext.newFile();
        resizeToolbarPanel();
        pack();
        setVisible(true);

    } // end of run() method

    private void resizeToolbarPanel() {
        Component[] components = toolBarPanel.getComponents();
        int rowWidth = toolBarPanel.getWidth();
        int componentWidth = 0;
        int componentHeight = 0;
        int sumWidth = 0;
        int numRows = 1;
        int rowHeight = 0;
        int sumHeight = 0;
        for (int i = 0; i < components.length; i++) {
            componentWidth = components[i].getPreferredSize().width;
            componentHeight = components[i].getPreferredSize().height;
            sumWidth += componentWidth;
            if (sumWidth > rowWidth) {
                numRows++;
                sumWidth = componentWidth;
                sumHeight += rowHeight + toolBarVGap;
                rowHeight = componentHeight;
            } else {
                rowHeight = Math.max(rowHeight, componentHeight);
            }
        }
        sumHeight += rowHeight;
        toolBarPanel.setPreferredSize(new Dimension(rowWidth, sumHeight));
        toolBarPanel.revalidate();
    }

    // ========================================================================
    // WindowListener implementation
    // ========================================================================
    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        if (fileContext.exit()) {
            System.exit(0);
        }
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    // ========================================================================
    // ComponentListener implementation
    // ========================================================================
    public void componentResized(ComponentEvent e) {
        resizeToolbarPanel();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }
}
