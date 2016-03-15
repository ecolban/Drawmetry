package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * 
 * @author Erik
 */
public class FormatMenu extends JMenu {

    FormatMenu(final DrawingPane drawingPane) {

        setMnemonic('o');
        setText("Format");

        JMenuItem strokeFormatMenuItem = new JMenuItem();
        strokeFormatMenuItem.setText("Stroke ...");
        strokeFormatMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                BasicStroke oldPen = drawingPane.getStroke();
                BasicStroke newPen = StrokeFormatDialog.showDialog(drawingPane);
                if (newPen != null && !newPen.equals(oldPen)) {
                    drawingPane.setStroke(newPen);
                }
            }
        });
        add(strokeFormatMenuItem);

        JMenuItem fillFormatMenuItem = new JMenuItem();
        fillFormatMenuItem.setText("Fill ...");
        //TODO: Allow user to select winding rule.
        add(fillFormatMenuItem);

        JMenuItem fontFormatMenuItem = new JMenuItem();
        fontFormatMenuItem.setText("Font ...");
        add(fontFormatMenuItem);

    }
}
