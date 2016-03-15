package com.drawmetry;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

/**
 *
 * @author Erik
 */
public class FileMenu extends JMenu implements PropertyChangeListener {

    private final JMenuItem saveMenuItem;

    FileMenu(final DmFrame frame, final FileContext fileContext, final DrawingPane drawingPane) {
        setMnemonic('F');
        setText("File");
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        JMenuItem newMenuItem = new JMenuItem();
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, menuMask));
        newMenuItem.setMnemonic('N');
        newMenuItem.setText("New");
        newMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileContext.newFile();
            }
        });
        add(newMenuItem);

        JMenuItem openMenuItem = new JMenuItem();
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, menuMask));
        openMenuItem.setMnemonic('O');
        openMenuItem.setText("Open ...");
        openMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileContext.openFile();
            }
        });
        add(openMenuItem);

        JMenuItem includeMenuItem = new JMenuItem();
        includeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, menuMask));
        includeMenuItem.setMnemonic('I');
        includeMenuItem.setText("Include ...");
        includeMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileContext.includeFile();
            }
        });
        add(includeMenuItem);

        saveMenuItem = new JMenuItem();
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText("Save");
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileContext.saveFile();
            }
        });
        add(saveMenuItem);

        final JMenuItem saveAsMenuItem = new JMenuItem();
        saveAsMenuItem.setMnemonic('A');
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileContext.saveFileAs();
            }
        });
        add(saveAsMenuItem);

        JMenuItem closeMenuItem = new JMenuItem();
        closeMenuItem.setMnemonic('C');
        closeMenuItem.setText("Close");
        closeMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileContext.closeFile();
            }
        });
        add(closeMenuItem);
        //--------------------------------------------------------------------
        add(new JSeparator());

        JMenuItem exportAsMenuItem = new JMenuItem();
        exportAsMenuItem.setMnemonic('E');
        exportAsMenuItem.setText("Export as ...");
        exportAsMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileContext.exportAs();
            }
        });
        add(exportAsMenuItem);


        //--------------------------------------------------------------------
        add(new JSeparator());

        JMenuItem pageSetupMenuItem = new JMenuItem();
        pageSetupMenuItem.setText("Page Setup ...");
        pageSetupMenuItem.addActionListener(new ActionListener() {

//            @Override
            public void actionPerformed(ActionEvent e) {
                fileContext.pageSetup();
            }
        });
        add(pageSetupMenuItem);

        JMenuItem previewMenuItem = new JMenuItem();
        previewMenuItem.setText("Preview ...");
        previewMenuItem.addActionListener(new ActionListener() {

//            @Override
            public void actionPerformed(ActionEvent e) {
                PreviewDialog dialog = new PreviewDialog(fileContext, frame, true);
                dialog.setVisible(true);
            }
        });
        add(previewMenuItem);

        JMenuItem printMenuItem = new JMenuItem();
        printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, menuMask));
        printMenuItem.setText("Print ...");
        printMenuItem.addActionListener(new ActionListener() {

//            @Override
            public void actionPerformed(ActionEvent e) {
                fileContext.print();
//                printService.print(printView);
            }
        });
        add(printMenuItem);
        //-----------------------------------------------------------------
        add(new JSeparator());

        JMenuItem exitMenuItem = new JMenuItem();
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if (fileContext.exit()) {
                    System.exit(0);
                }
            }
        });
        add(exitMenuItem);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof FileContext) {
            if (evt.getPropertyName().equals(FileContext.PROP_SAVE_ENABLED)) {
                saveMenuItem.setEnabled(((Boolean) evt.getNewValue()).booleanValue());
            }
        }
    }
}
