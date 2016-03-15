package com.drawmetry;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 *
 * @author Erik
 */
 class DrawToolBar extends JToolBar {

    private static Icon selectIcon = new ImageIcon(DmFrame.class.getResource(
            "/images/selectArrow16.png"));
    private static Icon insertPointIcon = new ImageIcon(DmFrame.class.getResource(
            "/images/insertPoint16.png"));
    private static Icon insertLineIcon = new ImageIcon(DmFrame.class.getResource(
            "/images/insertLine16.png"));
    private static Icon moveViewIcon = new ImageIcon(DmFrame.class.getResource(
            "/images/moveArrows16.png"));

     DrawToolBar(final FileContext fileContext, final DrawingPane drawingPane) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        setRollover(true);

        final JToggleButton selectToolToggle = new JToggleButton();
        selectToolToggle.setIcon(selectIcon);
        selectToolToggle.setToolTipText("Default mode");
//        selectToolToggle.setText("S");
        selectToolToggle.setBorder(null);
        selectToolToggle.setFocusable(false);
        selectToolToggle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.setMode(DrawingPane.MOUSE_LISTENER_MODE.SELECT);
            }
        });
        add(selectToolToggle);

        final JToggleButton insertPointToggle = new JToggleButton();
        insertPointToggle.setIcon(insertPointIcon);
//        insertPointToggle.setText("P");
        insertPointToggle.setToolTipText("Insert point");
        insertPointToggle.setBorder(null);
        insertPointToggle.setFocusable(false);
        insertPointToggle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.setMode(DrawingPane.MOUSE_LISTENER_MODE.INSERT_POINT);
            }
        });
        add(insertPointToggle);

        final JToggleButton insertLineToggle = new JToggleButton();
        insertLineToggle.setPreferredSize(new Dimension(20, 20));
        insertLineToggle.setIcon(insertLineIcon);
//        insertLineToggle.setText("L");
        insertLineToggle.setToolTipText("Insert line");
        insertLineToggle.setBorder(null);
        insertLineToggle.setFocusable(false);
        insertLineToggle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.setMode(DrawingPane.MOUSE_LISTENER_MODE.INSERT_LINE);
            }
        });
        add(insertLineToggle);

        final JToggleButton moveViewToggle = new JToggleButton();
        moveViewToggle.setIcon(moveViewIcon);
//        moveViewToggle.setText("M");
        moveViewToggle.setToolTipText("Move view");
        moveViewToggle.setBorder(null);
        moveViewToggle.setFocusable(false);
        moveViewToggle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.setMode(DrawingPane.MOUSE_LISTENER_MODE.PAN_VIEW);
            }
        });
        add(moveViewToggle);

        final JComboBox zoomBox = new JComboBox();
        zoomBox.setEditable(true);
        zoomBox.setFont(new Font("Arial", Font.PLAIN, 12));
        zoomBox.setModel(new DefaultComboBoxModel(new String[]{"25%", "50%", "100%", "200%", "400%", "2500%", "Fit to view"}));
        zoomBox.setPreferredSize(new Dimension(60, 20));
        zoomBox.setToolTipText("Zoom");
        zoomBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                JComboBox cb = (JComboBox) evt.getSource();
                String zoomString = (String) cb.getSelectedItem();
                Pattern p = Pattern.compile("(\\d+)%");
                Matcher m = p.matcher(zoomString);
                if (m.lookingAt()) {
                    try {
                        drawingPane.setZoom(Double.parseDouble(m.group(1)) / 100.0);
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                } else if (zoomString.equals("Fit to view")) {
                    Rectangle2D modelSize = drawingPane.getModel().getBounds();
                    double zoom = Math.min(drawingPane.getWidth() / modelSize.getWidth(),
                            drawingPane.getHeight() / modelSize.getHeight());
                    cb.setSelectedItem("" + (int) (zoom * 100) + "%");
                    drawingPane.setZoom(zoom);
                    drawingPane.setOrigin(modelSize.getX(), modelSize.getY());
                }

            }
        });
        add(zoomBox);

        fileContext.addPropertyChangeListener(FileContext.PROP_CURRENT_DOCUMENT,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        zoomBox.setSelectedItem((int) (fileContext.getZoom() * 100) + "%");
                    }
                });

        drawingPane.addPropertyChangeListener(DrawingPane.PROP_MOUSE_LISTENER_MODE,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        insertPointToggle.setSelected(false);
                        insertLineToggle.setSelected(false);
                        moveViewToggle.setSelected(false);
                        selectToolToggle.setSelected(false);
                        switch ((DrawingPane.MOUSE_LISTENER_MODE) evt.getNewValue()) {
                            case INSERT_LINE: {
                                insertLineToggle.setSelected(true);
                                break;
                            }
                            case INSERT_POINT: {
                                insertPointToggle.setSelected(true);
                                break;
                            }
                            case PAN_VIEW: {
                                moveViewToggle.setSelected(true);
                                break;
                            }
                            case SELECT: {
                                selectToolToggle.setSelected(true);
                                break;
                            }
                            default: {
                            }
                        }
                    }
                });

    }
}
