package com.drawmetry;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 *
 * @author Erik
 */
@SuppressWarnings("serial")
public class FileToolBar extends JToolBar {

//    private ClassLoader cl = getClass().getClassLoader();
    private Icon newFileIcon = new ImageIcon(getClass().getResource(
            "/toolbarButtonGraphics/general/New16.gif"));
    private Icon openFileIcon = new ImageIcon(getClass().getResource(
            "/toolbarButtonGraphics/general/Open16.gif"));
    private Icon saveFileIcon = new ImageIcon(getClass().getResource(
            "/toolbarButtonGraphics/general/Save16.gif"));

    FileToolBar(final FileContext fileContext, final DrawingPane drawingPane) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        setRollover(true);

        JButton newFileButton = new JButton();
        newFileButton.setIcon(newFileIcon);
        newFileButton.setToolTipText("New file ...");
        newFileButton.setBorder(null);
        newFileButton.setEnabled(true);
        newFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileContext.newFile();
            }
        });
        add(newFileButton);

        JButton openFileButton = new JButton();
        openFileButton.setIcon(openFileIcon); 
        openFileButton.setToolTipText("Open file ...");
        openFileButton.setBorder(null);
        openFileButton.setEnabled(true);
        openFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileContext.openFile();
            }
        });
        add(openFileButton);

        final JButton saveFileButton = new JButton();
        saveFileButton.setIcon(saveFileIcon);
        saveFileButton.setToolTipText("Save");
        saveFileButton.setBorder(null);
        saveFileButton.setEnabled(false);
        saveFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileContext.saveFile();
            }
        });
        add(saveFileButton);

        fileContext.addPropertyChangeListener(FileContext.PROP_SAVE_ENABLED,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        saveFileButton.setEnabled(((Boolean) evt.getNewValue()).booleanValue());
                    }
                });

    }
}
