/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;

/**
 *
 * @author Erik
 */
@SuppressWarnings("serial")
class ColorChooser extends JComponent implements ActionListener {

    private static String PROP_CHOSEN_COLOR = "chosenColor";
    private JComboBox<Icon> colorCombo;
    private ArrayList<Color> colors = new ArrayList<Color>();
    private Color currentColor = Color.BLACK;
    private Color chosenColor = Color.BLACK;
    private JColorChooser colorChooserPane = new JColorChooser(Color.BLACK);
    private DrawingPane pane;
    private ActionListener colorChooserOKListener = new ActionListener() {

//        @Override
        public void actionPerformed(ActionEvent evt) {
            chosenColor = colorChooserPane.getColor();

        }
    };
    private ActionListener colorChooserCancelListener = new ActionListener() {

//        @Override
        public void actionPerformed(ActionEvent evt) {
            chosenColor = null;
        }
    };
//    private JDialog colorChooserDialog = JColorChooser.createDialog(
//            frame,
//            "Choose_a_Color",
//            true,
//            colorChooserPane,
//            colorChooserOKListener,
//            colorChooserCancelListener);
    private static Icon otherColorsIcon =
            new ImageIcon(ColorChooser.class.getResource("/images/selectColor.png"));
    private DefaultComboBoxModel<Icon> colorComboModel;

    ColorChooser(Color color, DrawingPane pane) {
        super();
        colorCombo = new JComboBox<Icon>();
        initiateColors(color);
        buildModel(getColors(), 0);
        colorCombo.addActionListener(this);
        this.pane = pane;
    }

    JComboBox<Icon> getColorCombo() {
        return colorCombo;
    }

    public void actionPerformed(ActionEvent evt) {
        @SuppressWarnings("unchecked")
        JComboBox<Color> cb = (JComboBox<Color>) evt.getSource();
        Color color = null;

        int ind = cb.getSelectedIndex();
        if (ind >= 0 && ind < getColors().length) {
            color = getColors()[ind];
        } else if (ind == getColors().length) {
            JDialog colorChooserDialog = JColorChooser.createDialog(
                    pane,
                    "Choose_a_Color",
                    true,
                    colorChooserPane,
                    colorChooserOKListener,
                    colorChooserCancelListener);
            colorChooserDialog.setVisible(true);
            if (chosenColor != null) {
                color = chosenColor;
            }
        }
        setColor(color);
    }

    private Icon getColorIcon(final Color color) {
        return new Icon() {

            public void paintIcon(Component c, Graphics g, int x, int y) {
                if (color != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(color);
                    g2.fillRect(x + 2, y + 2, 22, 10);
                    g2.dispose();
                }
            }

            public int getIconWidth() {
                return 24;
            }

            public int getIconHeight() {
                return 12;
            }
        };
    }

    private void initiateColors(Color color) {
        colors.add(Color.BLACK);
        colors.add(Color.RED);
        colors.add(Color.BLUE);
        colors.add(Color.GREEN);
        colors.add(null);
        setColor(color);
    }

    private void buildModel(Color[] colors, int index) {
        if (index >= 0 && index < colors.length) {
            Icon[] colorIcons = new Icon[colors.length + 1];
            for (int i = 0; i < colors.length; i++) {
                colorIcons[i] = getColorIcon(colors[i]);
            }
            colorIcons[colors.length] = otherColorsIcon;
            colorComboModel = new DefaultComboBoxModel<Icon>(colorIcons);
            colorComboModel.setSelectedItem(colorIcons[index]);
        } else {
            Icon[] colorIcons = new Icon[colors.length + 2];
            colorIcons[0] = getColorIcon(null);
            for (int i = 0; i < colors.length; i++) {
                colorIcons[i + 1] = getColorIcon(colors[i]);
            }
//            colorIcons[colors.length + 1] = getOtherColorsIcon();
            colorIcons[colors.length + 1] = otherColorsIcon;
            colorComboModel = new DefaultComboBoxModel<Icon>(colorIcons);
            colorComboModel.setSelectedItem(colorIcons[0]);
        }
        colorCombo.setModel(colorComboModel);
    }

    private Color[] getColors() {
        Color[] a = new Color[0];
        return colors.toArray(a);
    }

    void setColor(Color color) {
        boolean found = false;
        int ind = 0;
        for (Color c : colors) {
            if (c == color || c != null && c.equals(color)) {
                colors.remove(ind);
                colors.add(0, color);
                buildModel(getColors(), 0);
                found = true;
                break;
            }
            ind++;
        }
        if (!found) {
            colors.add(0, color);
            buildModel(getColors(), 0);
        }
        Color oldValue = currentColor;
        currentColor = color;
        if (oldValue != currentColor) {
            firePropertyChange(PROP_CHOSEN_COLOR, oldValue, currentColor);
        }
    }
}
