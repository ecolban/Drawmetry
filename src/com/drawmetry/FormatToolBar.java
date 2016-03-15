package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Erik
 */
class FormatToolBar extends JToolBar {

    FormatToolBar(final FileContext fileContext, final DrawingPane drawingPane) {
//        JFrame frame = (JFrame) drawingPane.getParent();
        final StrokeHandler strokeHandler = StrokeHandler.getInstance();
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));

        final JComboBox strokeCombo = new JComboBox(strokeHandler.getIconArray());
        strokeCombo.setToolTipText("Stroke");
        strokeCombo.setPreferredSize(new Dimension(80, 20));
        strokeCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int newIndex = strokeCombo.getSelectedIndex();
                drawingPane.setStroke(strokeHandler.getStroke(newIndex));
            }
        });
        add(strokeCombo);

        final ColorChooser strokeColorChooser = new ColorChooser(Color.BLACK, drawingPane);
        JComboBox strokeColorCombo = strokeColorChooser.getColorCombo();
        strokeColorCombo.setToolTipText("Stroke color");
        strokeColorCombo.setPreferredSize(new Dimension(60, 20));
        strokeColorChooser.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                drawingPane.setStrokeColor((Color) evt.getNewValue());
            }
        });
        add(strokeColorCombo);

        final ColorChooser fillColorChooser = new ColorChooser(Color.BLACK, drawingPane);
        JComboBox fillColorCombo = fillColorChooser.getColorCombo();
        fillColorCombo.setToolTipText("Fill color");
        fillColorCombo.setPreferredSize(new Dimension(60, 20));
        fillColorChooser.setColor(null);
        fillColorChooser.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                drawingPane.setFillColor((Color) evt.getNewValue());
            }
        });
        add(fillColorCombo);


        final JSlider opacitySlider = new JSlider();
        opacitySlider.setToolTipText("Opacity");
        opacitySlider.setValue(100);
        opacitySlider.setPreferredSize(new Dimension(70, 20));
        opacitySlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    double opacity = source.getValue() / 100.0;
                    drawingPane.setOpacity(opacity);
                }
            }
        });
        add(opacitySlider);



        final JComboBox startArrowheadCombo = new JComboBox();
        startArrowheadCombo.setModel(new DefaultComboBoxModel(Arrowhead.getArrowheadIcons(true)));
        startArrowheadCombo.setPreferredSize(new Dimension(70, 20));
        startArrowheadCombo.setToolTipText("Start arrow");
        startArrowheadCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                JComboBox cb = (JComboBox) evt.getSource();
                Arrowhead arrowhead = Arrowhead.NONE;
                int ind = cb.getSelectedIndex();
                if (ind > 0 && ind < cb.getItemCount()) {
                    arrowhead = Arrowhead.values()[ind];
                }
                drawingPane.setStartArrowhead(arrowhead);
            }
        });
        add(startArrowheadCombo);

        final JComboBox endArrowheadCombo = new JComboBox();
        endArrowheadCombo.setModel(new DefaultComboBoxModel(Arrowhead.getArrowheadIcons(false)));
        endArrowheadCombo.setPreferredSize(new Dimension(70, 20));
        endArrowheadCombo.setToolTipText("End arrow");
        endArrowheadCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                JComboBox cb = (JComboBox) evt.getSource();
                Arrowhead arrowhead = Arrowhead.NONE;
                int ind = cb.getSelectedIndex();
                if (ind > 0 && ind < cb.getItemCount()) {
                    arrowhead = Arrowhead.values()[ind];
                }
                drawingPane.setEndArrowhead(arrowhead);
            }
        });
        add(endArrowheadCombo);

        final JComboBox fontComboBox = new JComboBox();
        fontComboBox.setModel(getFonts());
        fontComboBox.setPreferredSize(new Dimension(100, 20));
        fontComboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fontComboBox.setToolTipText("Font");
        fontComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                JComboBox cb = (JComboBox) evt.getSource();
                String fontFamilyName = (String) cb.getSelectedItem();
                drawingPane.setFontFamily(fontFamilyName);
            }
        });
        add(fontComboBox);

        final JComboBox fontSizeComboBox = new JComboBox();
        fontSizeComboBox.setModel(new DefaultComboBoxModel(new String[]{"8", "10", "12", "14", "16", "18", "24", "48", "72"}));
        fontSizeComboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fontSizeComboBox.setPreferredSize(new Dimension(70, 20));
        fontSizeComboBox.setToolTipText("Font size");
        fontSizeComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                JComboBox cb = (JComboBox) evt.getSource();
                int size = Integer.parseInt((String) cb.getSelectedItem());
                drawingPane.setFontSize(size);
            }
        });
        add(fontSizeComboBox);

        final JComboBox hAlignBox = new JComboBox();
        hAlignBox.setEditable(false);
        hAlignBox.setFont(new Font("Arial", Font.PLAIN, 12));
        hAlignBox.setModel(new DefaultComboBoxModel(new String[]{"left", "center", "right"}));
        hAlignBox.setPreferredSize(new Dimension(90, 20));
        hAlignBox.setToolTipText("Horizontal alignment");
        hAlignBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                JComboBox cb = (JComboBox) evt.getSource();
                String hAlignString = (String) cb.getSelectedItem();
                drawingPane.setHorizontalAlignment(hAlignString);
            }
        });
        add(hAlignBox);        

        final JComboBox vAlignBox = new JComboBox();
        vAlignBox.setEditable(false);
        vAlignBox.setFont(new Font("Arial", Font.PLAIN, 12));
        vAlignBox.setModel(new DefaultComboBoxModel(new String[]{"top", "center", "bottom"}));
        vAlignBox.setPreferredSize(new Dimension(90, 20));
        vAlignBox.setToolTipText("Vertical alignment");
        vAlignBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                JComboBox cb = (JComboBox) evt.getSource();
                String vAlignString = (String) cb.getSelectedItem();
                drawingPane.setVerticalAlignment(vAlignString);
            }
        });
        add(vAlignBox);

        drawingPane.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(DrawingPane.PROP_STROKE)) {
                    BasicStroke stroke = (BasicStroke) evt.getNewValue();
                    strokeHandler.addStroke(stroke);
                    strokeCombo.setModel(new DefaultComboBoxModel(strokeHandler.getIconArray()));
                    strokeCombo.setSelectedIndex(0);
                } else if (evt.getPropertyName().equals(DrawingPane.PROP_STROKE_COLOR)) {
                    Color color = (Color) evt.getNewValue();
                    strokeColorChooser.setColor(color);
                } else if (evt.getPropertyName().equals(DrawingPane.PROP_FILL_COLOR)) {
                    Color color = (Color) evt.getNewValue();
                    fillColorChooser.setColor(color);
                } else if (evt.getPropertyName().equals(DrawingPane.PROP_OPACITY)) {
                    double opacity = ((Double) evt.getNewValue()).doubleValue();
                    opacitySlider.setValue((int) (opacity * 100));
                } else if (evt.getPropertyName().equals(DrawingPane.PROP_START_ARROW)) {
                    int index = Arrowhead.getArrowheadIndex((Arrowhead) evt.getNewValue());
                    startArrowheadCombo.setSelectedIndex(index);
                } else if (evt.getPropertyName().equals(DrawingPane.PROP_END_ARROW)) {
                    int index = Arrowhead.getArrowheadIndex((Arrowhead) evt.getNewValue());
                    endArrowheadCombo.setSelectedIndex(index);
                } else if (evt.getPropertyName().equals(DrawingPane.PROP_CELL_FORMAT)) {
                    MCell.CellFormat format = (MCell.CellFormat) evt.getNewValue();
                    fontComboBox.setSelectedItem(format.getFont().getFamily());
                    fontSizeComboBox.setSelectedItem("" + format.getFont().getSize());
                    strokeColorChooser.setColor(format.getColor());
                    hAlignBox.setSelectedItem(format.getHorizontalAlignmentAsString());
                    vAlignBox.setSelectedItem(format.getVerticalAlignmentAsString());
                }
            }
        });
    }

    private ComboBoxModel getFonts() {
        String[] fonts = new String[]{
            "Arial",
            "Arial Black",
            "Courier New",
            "Georgia",
            "Impact",
            "Lucida Console",
            "Monospaced",
            "SansSerif",
            "Serif",
            "Symbol",
            "Times New Roman"
        };
        return new DefaultComboBoxModel(fonts);
    }
}
