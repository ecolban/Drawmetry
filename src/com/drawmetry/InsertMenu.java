package com.drawmetry;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * @author Erik
 */
public class InsertMenu extends JMenu {

    private DrawingPane drawingPane;

    InsertMenu(final DrawingPane drawingPane) {

        this.drawingPane = drawingPane;
        setMnemonic('I');
        setText("Insert");

        final JRadioButtonMenuItem insertPointRadio = new JRadioButtonMenuItem();
        insertPointRadio.setText("Insert point");
        insertPointRadio.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.setMode(DrawingPane.MOUSE_LISTENER_MODE.INSERT_POINT);
            }
        });
        add(insertPointRadio);

        final JRadioButtonMenuItem insertLineRadio = new JRadioButtonMenuItem();
        insertLineRadio.setText("Insert line");
        insertLineRadio.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.setMode(DrawingPane.MOUSE_LISTENER_MODE.INSERT_LINE);
            }
        });
        add(insertLineRadio);

        final JRadioButtonMenuItem insertCellRadio = new JRadioButtonMenuItem();
        insertCellRadio.setText("Insert cell");
        insertCellRadio.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.setMode(DrawingPane.MOUSE_LISTENER_MODE.INSERT_CELL);
            }
        });
        add(insertCellRadio);

        drawingPane.addPropertyChangeListener(DrawingPane.PROP_MOUSE_LISTENER_MODE,
                new PropertyChangeListener() {

//            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                assert evt.getPropertyName().equals(DrawingPane.PROP_MOUSE_LISTENER_MODE);
                insertCellRadio.setSelected(false);
                insertPointRadio.setSelected(false);
                insertLineRadio.setSelected(false);
                switch ((DrawingPane.MOUSE_LISTENER_MODE) evt.getNewValue()) {
                    case INSERT_CELL: {
                        insertCellRadio.setSelected(true);
                        break;
                    }
                    case INSERT_LINE: {
                        insertLineRadio.setSelected(true);
                        break;
                    }
                    case INSERT_POINT: {
                        insertPointRadio.setSelected(true);
                        break;
                    }
                    default: {
                    }
                }
            }
        });
    }
}
