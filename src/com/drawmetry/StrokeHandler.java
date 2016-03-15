package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;

class StrokeHandler {

    final static BasicStroke DEFAULT_STROKE = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 4.0F);
    private static StrokeHandler instance = new StrokeHandler();
    private List<BasicStroke> definedStrokes = new ArrayList<BasicStroke>();

    private StrokeHandler() {
        super();
        definedStrokes.add(DEFAULT_STROKE);
    }

    static StrokeHandler getInstance() {
        return instance;
    }

    BasicStroke getStroke(int index) {
        if (0 <= index && index < definedStrokes.size()) {
            return definedStrokes.get(index);
        } else {
            return null;
        }
    }

    boolean addStroke (BasicStroke stroke) {
        BasicStroke pen1 = null;
        boolean found = false;
        if (stroke == null) {
            return false;
        }
        for (Iterator<BasicStroke> i = definedStrokes.iterator(); !found && i.hasNext();) {
            pen1 = i.next();
            if (stroke.equals(pen1)) {
                i.remove();
                found = true;
            }
        }
        definedStrokes.add(0, stroke);
        return !found;
    }

    private static Icon getIcon(final BasicStroke stroke) {
        return new Icon() {

            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.setStroke(stroke);
                g2.drawLine(2, 10, 38, 10);
                g2.dispose();
            }

            public int getIconWidth() {
                return 40;
            }

            public int getIconHeight() {
                return 19;
            }
        };
    }

    Icon[] getIconArray() {
        Icon[] result = new Icon[definedStrokes.size()];
        int i = 0;
        for (BasicStroke pen : definedStrokes) {
            Icon peek = getIcon(pen);
            result[i++] = peek;
        }
        return result;
    }
}
