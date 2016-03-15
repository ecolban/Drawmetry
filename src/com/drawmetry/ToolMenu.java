package com.drawmetry;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * @author Erik
 */
public class ToolMenu extends JMenu {

    private DrawingPane drawingPane;

    ToolMenu(final DrawingPane drawingPane) {

        this.drawingPane = drawingPane;
        setMnemonic('T');
        setText("Tools");

        final JRadioButtonMenuItem traceOnMenuItem = new JRadioButtonMenuItem();
        traceOnMenuItem.setText("Trace On");
        traceOnMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.setTraceOnEnabled(traceOnMenuItem.isSelected());
            }
        });
        add(traceOnMenuItem);

        final JMenuItem traceConfigMenuItem = new JMenuItem();
        traceConfigMenuItem.setText("Trace tolerance ...");
        traceConfigMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                showTraceConfigDialog();
            }
        });
        add(traceConfigMenuItem);

//        final JRadioButtonMenuItem snippLineToolsMenuItem = new JRadioButtonMenuItem();
//        snippLineToolsMenuItem.setText("Snipp Line");
//        snippLineToolsMenuItem.setEnabled(false);
//        snippLineToolsMenuItem.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                drawingPane.setMode(DrawingPane.MOUSE_LISTENER_MODE.SNIPP_LINE);
//            }
//        });
//        add(snippLineToolsMenuItem);

        drawingPane.addPropertyChangeListener(DrawingPane.PROP_TRACE_ENABLED,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
                        traceOnMenuItem.setSelected(enabled);
                    }
                });

//        drawingPane.addPropertyChangeListener(DrawingPane.PROP_MOUSE_LISTENER_MODE, new PropertyChangeListener() {
//
//            public void propertyChange(PropertyChangeEvent evt) {
//                assert evt.getPropertyName().equals(DrawingPane.PROP_MOUSE_LISTENER_MODE);
//                snippLineToolsMenuItem.setSelected(false);
//                switch ((DrawingPane.MOUSE_LISTENER_MODE) evt.getNewValue()) {
//                    case SNIPP_LINE: {
//                        snippLineToolsMenuItem.setSelected(true);
//                        break;
//                    }
//                    default: {
//                    }
//                }
//            }
//        });

    }

    private void showTraceConfigDialog() {
        Object[] possibilities = {"0.1", "0.5", "1.0", "2.0", "4.0", "8.0",
            "16.0"};
        String s = (String) JOptionPane.showInputDialog(
                drawingPane,
                "Select max distance between trace and sample point", "Trace threshold", JOptionPane.PLAIN_MESSAGE,
                null, possibilities,
                "" + drawingPane.getTraceThreshold());

//If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {

            try {
                drawingPane.setTraceThreshold(Double.parseDouble(s));
            } catch (NumberFormatException nfe) {
                assert false;
            }

            return;
        }
    }
}
