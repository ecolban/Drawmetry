package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Erik
 */
public class StrokeFormatDialog extends JDialog {

    private static BasicStroke resultStroke;
//    private Color color = Color.BLACK;
//    private StrokeHandler strokeHandler = StrokeHandler.getInstance();
    private float width = StrokeHandler.DEFAULT_STROKE.getLineWidth();
    private int cap = StrokeHandler.DEFAULT_STROKE.getEndCap();
    private int join = StrokeHandler.DEFAULT_STROKE.getLineJoin();
    private final static float miterLimit = StrokeHandler.DEFAULT_STROKE.getMiterLimit();
    private final static float dashPhase = StrokeHandler.DEFAULT_STROKE.getDashPhase();
    private float[] dashArray = StrokeHandler.DEFAULT_STROKE.getDashArray();
    private static String[] capStringValues = new String[]{"Butt", "Round", "Square"};
    private static int[] capIntValues = new int[]{BasicStroke.CAP_BUTT, BasicStroke.CAP_ROUND, BasicStroke.CAP_SQUARE};
    private static String[] joinStringValues = new String[]{"Bevel", "Miter", "Round"};
    private static int[] joinIntValues = new int[]{BasicStroke.JOIN_BEVEL, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND};
    private static float[][] dashArrays = new float[][]{
        {3.0F, 3.0F},
        {5.0F, 3.0F},
        {8.0F, 5.0F},
        {10.0F, 8.0F},
        {10.0F, 3.0F, 2.0F, 3.0F}
    };
    private final JComboBox widthCombo = new JComboBox(new String[]{"0.5", "1.0", "1.5"});
    private final JComboBox capCombo = new JComboBox(capStringValues);
    private final JComboBox joinCombo = new JComboBox(joinStringValues);
    private final JComboBox dashCombo = new JComboBox(getDashIcons());
//    private final ColorChooser colorChooser = new ColorChooser(Color.BLACK);
//    private final JComboBox colorCombo = colorChooser.getColorCombo();


    private StrokeFormatDialog() {
        initialize();
        pack();
    }

    static BasicStroke showDialog(DrawingPane pane) {
        StrokeFormatDialog dialog = new StrokeFormatDialog();
        BasicStroke stroke = pane.getStroke();
//        dialog.color = pane.getStrokeColor();
//        dialog.colorChooser.setColor(dialog.color);
        dialog.width = stroke.getLineWidth();
        dialog.widthCombo.setSelectedItem("" + dialog.width);
        dialog.cap = stroke.getEndCap();
        dialog.capCombo.setSelectedIndex(getCapIndex(dialog.cap));
        dialog.join = stroke.getLineJoin();
        dialog.joinCombo.setSelectedIndex(getJoinIndex(dialog.join));
        dialog.dashArray = stroke.getDashArray();
        dialog.dashCombo.setSelectedIndex(getDashIndex(dialog.dashArray));
        dialog.setVisible(true);
        return resultStroke;
    }

    private void initialize() {

        setTitle("Format Stroke");
//        setPreferredSize(new Dimension(200, 280));
        setModal(true);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2, 2, 5));

        JLabel widthLabel = new JLabel("Width");
        widthLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(widthLabel);
        widthCombo.setEditable(true);
        widthCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    width = Float.parseFloat((String) (widthCombo.getSelectedItem()));
                } catch (NumberFormatException ex) {
                }
            }
        });
        mainPanel.add(widthCombo);

        JLabel capLabel = new JLabel("Cap");
        capLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(capLabel);
        capCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String capString = (String) (((JComboBox) e.getSource()).getSelectedItem());
                for (int i = 0; i < capStringValues.length; i++) {
                    if (capStringValues[i].equals(capString)) {
                        cap = capIntValues[i];
                        break;
                    }
                }
            }
        });
        mainPanel.add(capCombo);

        JLabel joinLabel = new JLabel("Join");
        joinLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(joinLabel);
        joinCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String joinString = (String) (((JComboBox) e.getSource()).getSelectedItem());
                for (int i = 0; i < joinStringValues.length; i++) {
                    if (joinStringValues[i].equals(joinString)) {
                        join = joinIntValues[i];
                        break;
                    }
                }
            }
        });
        mainPanel.add(joinCombo);

        JLabel dashLabel = new JLabel("Dash");
        dashLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(dashLabel);
        dashCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int i = ((JComboBox) e.getSource()).getSelectedIndex();
                dashArray = i == 0 ? null : dashArrays[i - 1];
            }
        });
        mainPanel.add(dashCombo);
        JPanel okCancelPanel = new JPanel();
//        okCancelPanel.setPreferredSize(new Dimension(200, 24));
        okCancelPanel.setLayout(new GridLayout(1, 2, 10, 10));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                BasicStroke newStroke = new BasicStroke(width, cap, join, miterLimit, dashArray, dashPhase);
//                strokeHandler.addStroke(newStroke);
                resultStroke = newStroke;
                setVisible(false);
                dispose();
            }
        });
        okCancelPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                resultStroke = null;
                setVisible(false);
                dispose();
            }
        });
        okCancelPanel.add(cancelButton);
        add(mainPanel, BorderLayout.CENTER);
        add(okCancelPanel, BorderLayout.SOUTH);
    }

    private static int getCapIndex(int cap) {
        for (int i = 0; i < capIntValues.length; i++) {
            if (capIntValues[i] == cap) {
                return i;
            }
        }
        return 0;
    }

    private static int getJoinIndex(int join) {
        for (int i = 0; i < joinIntValues.length; i++) {
            if (joinIntValues[i] == join) {
                return i;
            }
        }
        return 0;
    }

    private Icon[] getDashIcons() {
        Icon[] icons = new Icon[dashArrays.length + 1];
        icons[0] = getDashIcon(null);
        for (int i = 0; i < dashArrays.length; i++) {
            icons[i + 1] = getDashIcon(dashArrays[i]);
        }
        return icons;
    }

    private Icon getDashIcon(final float[] dash) {
        return new Icon() {

            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(dash == null ? new BasicStroke() : new BasicStroke(
                        1.0F,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL,
                        10.0F,
                        dash,
                        0.0F));
                g2.setColor(Color.BLACK);
                g2.drawLine(2, 7, 30, 7);
                g2.dispose();
            }

            public int getIconWidth() {
                return 32;
            }

            public int getIconHeight() {
                return 14;
            }
        };

    }

    private static int getDashIndex(float[] dashArray) {
        if (dashArray == null) {
            return 0;
        } else {
            int index;
            boolean found = true;
            for (index = 0; index < dashArrays.length; index++) {
                for (int j = 0; j < dashArrays[index].length; j++) {
                    found = true;
                    if (dashArray[j] != dashArrays[index][j]) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return index + 1;
                }
            }
            return 0;
        }
    }
}
