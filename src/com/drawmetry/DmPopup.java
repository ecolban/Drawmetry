package com.drawmetry;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Erik
 */
 class DmPopup extends JPopupMenu{
     DmPopup(final DrawingPane drawingPane) {
        // Delete
        JMenuItem deletePopupMenuItem = new JMenuItem();
        deletePopupMenuItem.setText("Delete");
        deletePopupMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.delete();
            }
        });
        add(deletePopupMenuItem);
        //Send to back
        JMenuItem sendToBackPopupMenuItem = new JMenuItem();
        sendToBackPopupMenuItem.setText("Send to back");
        sendToBackPopupMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.sendToBack();
            }
        });
        add(sendToBackPopupMenuItem);
        // Send to Front
        JMenuItem sendToFrontPopupMenuItem = new JMenuItem();
        sendToFrontPopupMenuItem.setText("Send to front");
        sendToFrontPopupMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.sendToFront();
            }
        });
        add(sendToFrontPopupMenuItem);
    }
}
