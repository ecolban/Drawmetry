/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import javax.swing.Icon;

/**
 *
 * @author Erik
 */
public enum Arrowhead {

    NONE(null, false),
    AM1(new Polygon(new int[]{0, 7, 7}, new int[]{0, 7, -7}, 3), true),
    AM2(new Polyline(new int[]{7, 0, 7}, new int[]{7, 0, -7}, 3), false),
    AM3(new Polygon(new int[]{10, 0, 10, 9}, new int[]{5, 0, -5, 0}, 4), true),
    BM1(new Ellipse2D.Double(-4, -4, 8, 8), true),
    BM2(new Ellipse2D.Double(-4, -4, 8, 8), false),
    LM1(new Line2D.Double(0, 7, 0, -7), false);
    private Shape shape;
    private boolean fill;

    public static Arrowhead getArrowhead(String value) {
        for (Arrowhead a : Arrowhead.values()) {
            if (value.equals(a.toString())) {
                return a;
            }
        }
        return null;
    }

    private Arrowhead(Shape s, boolean fill) {
        this.shape = s;
        this.fill = fill;
    }

    public void draw(Graphics2D g, double refX, double refY, double endX,
            double endY, double scale) {
        if (getShape() == null) {
            return;
        }
        AffineTransform aT = g.getTransform();
        g.transform(getAlignment(refX, refY, endX, endY, scale));
        g.draw(getShape());
        if (fill) {
            g.fill(getShape());
        }
        g.setTransform(aT);
    }

    public AffineTransform getAlignment(double refX, double refY, double endX,
            double endY, double scale) {
        double eX = refX - endX;
        double eY = refY - endY;
        double norm = Math.sqrt(eX * eX + eY * eY);
        eX /= norm;
        eY /= norm;
        return new AffineTransform(eX * scale, eY * scale, -eY * scale, eX * scale, endX, endY);
    }

    public boolean getFill() {
        return fill;
    }

    public Icon getArrowheadIcon(final boolean start) {
        return new Icon() {

            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.drawLine(2, 7, 22, 7);
                if (start) {
                    draw(g2, 22, 7, 2, 7, 1.0);
                } else {
                    draw(g2, 2, 7, 22, 7, 1.0);
                }
                g2.dispose();
            }

            public int getIconWidth() {
                return 24;
            }

            public int getIconHeight() {
                return 14;
            }
        };
    }

    public static Icon[] getArrowheadIcons(final boolean start) {
        Icon[] icons = new Icon[values().length];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = values()[i].getArrowheadIcon(start);
        }
        return icons;
    }

    /**
     * @return the shape
     */
    public Shape getShape() {
        return shape;
    }

    public static int getArrowheadIndex(Arrowhead arrow) {
        int i = 0;
        for (Arrowhead a : Arrowhead.values()) {
            if (a == arrow) {
                return i;
            }
            i++;
        }
        return 0;
    }
}
