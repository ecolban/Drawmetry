package com.drawmetry;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author Erik
 */
class ViewMenu extends JMenu {

    private DrawingPane drawingPane;

    ViewMenu(final DrawingPane drawingPane) {

        this.drawingPane = drawingPane;
        setMnemonic('V');
        setText("View");
        final JCheckBoxMenuItem showAll = new JCheckBoxMenuItem();
        showAll.setSelected(true);
        showAll.setText("Toggle markers");
        showAll.addActionListener(new ActionListener() {

//            @Override
            public void actionPerformed(ActionEvent evt) {
                drawingPane.setShowMarkers(showAll.isSelected());
            }
        });
        drawingPane.setShowMarkers(showAll.isSelected());
        add(showAll);

        final JMenuItem showSelected = new JMenuItem();
        showSelected.setText("Toggle markers in selection");
        showSelected.addActionListener(new ActionListener() {

//            @Override
            public void actionPerformed(ActionEvent evt) {
                Selection s = drawingPane.getSelection();
                s.setShowMarkers(!s.getShowing());

            }
        });
        add(showSelected);

    }
}
