package com.drawmetry;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

final class VPolyline implements ViewObject {

    private static GeneralPath transformedPolyline = new GeneralPath();
    private static GeneralPath rawPolyline = new GeneralPath();
    private static boolean selected;
    private static final BasicStroke defaultStroke = new BasicStroke();
    private static double[] points;
    private static Color strokeColor;
    private static Color fillColor;
    private static float opacity;
    private static BasicStroke stroke;
    private static VPolyline vPolyline = new VPolyline();
    private static AffineTransform at;
    private static Arrowhead startArrowhead;
    private static Arrowhead endArrowhead;
    private static double[] startVector;
    private static double[] endVector;
    private static boolean first;
    private static boolean isClosed;

    private VPolyline() {
    }

    public static VPolyline getInstance(MPolyline mpl, AffineTransform at,
            boolean selected) {
        VPolyline.selected = selected;
        strokeColor = mpl.getStrokeColor();
        fillColor = mpl.getFillColor();
        opacity = (float) mpl.getOpacity();
        stroke = mpl.getStroke();
        points = mpl.getPoints();
        VPolyline.at = at;
        rawPolyline.reset();
        first = true;
        for (int i = 0; i < points.length;) {
            if (first) {
                rawPolyline.moveTo((float) points[i++], (float) points[i++]);
                first = false;
            } else {
                rawPolyline.lineTo((float) points[i++], (float) points[i++]);
            }
        }
        if (mpl.isClosed()) {
            isClosed = true;
            rawPolyline.closePath();
        }
        rawPolyline.setWindingRule(mpl.getWindingRule());
        transformedPolyline = (GeneralPath) rawPolyline.clone();
        transformedPolyline.transform(at);
        VPolyline.startArrowhead = mpl.getStartArrowhead();
        VPolyline.endArrowhead = mpl.getEndArrowhead();
        if (VPolyline.startArrowhead != null) {
            startVector = mpl.getStartVector();
        }
        if (VPolyline.endArrowhead != null) {
            endVector = mpl.getEndVector();
        }
        return vPolyline;

    }

    public boolean hits(int x, int y) {
        return transformedPolyline.intersects(x - 2, y - 2, 4, 4) &&
                !transformedPolyline.contains(x - 2, y - 2, 4, 4);
    }

    public int getAdjacentPoints(int x, int y) {
        double[] d = new double[6];
        Line2D.Double currentLine = new Line2D.Double();
        Point2D.Double startPoint = new Point2D.Double();
        int i = 0;
        for (PathIterator pi = transformedPolyline.getPathIterator(null); !pi.isDone();) {
            if (pi.currentSegment(d) == PathIterator.SEG_MOVETO) {
                startPoint.x = currentLine.x1 = d[0];
                startPoint.y = currentLine.y1 = d[1];
            } else if (pi.currentSegment(d) == PathIterator.SEG_LINETO) {
                currentLine.x2 = d[0];
                currentLine.y2 = d[1];
                if (currentLine.intersects(x - 2, y - 2, 4, 4)) {
                    return i;
                } else {
                    i++;
                    currentLine.x1 = d[0];
                    currentLine.y1 = d[1];
                }
            }
            pi.next();
        }
        currentLine.x2 = startPoint.x;
        currentLine.y2 = startPoint.y;
        if (isClosed && currentLine.intersects(x - 2, y - 2, 4, 4)) {
            return i;
        } else {
            return -1;
        }
    }

    public boolean containedIn(Rectangle2D rect) {
        Rectangle r = transformedPolyline.getBounds();
        return rect.getX() <= r.getX() &&
                r.getX() + r.getWidth() <= rect.getX() + rect.getWidth() &&
                rect.getY() <= r.getY() &&
                r.getY() + r.getHeight() <= rect.getY() + rect.getHeight();
    }

    public void draw(Graphics2D g2) {
        AffineTransform saveAT = g2.getTransform();
        g2.transform(at);
        if (fillColor != null) {
            g2.setColor(fillColor);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g2.fill(rawPolyline);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
        }
        if (strokeColor != null) {
            g2.setColor(strokeColor);
            g2.setStroke(stroke);
            g2.draw(rawPolyline);
        }
        g2.setStroke(defaultStroke);
        if (startArrowhead != null && startVector != null) {
            startArrowhead.draw(g2, startVector[2], startVector[3], startVector[0], startVector[1],
                    1.0);
        }
        if (endArrowhead != null && endVector != null) {
            endArrowhead.draw(g2, endVector[0], endVector[1], endVector[2], endVector[3],
                    1.0);
        }

        g2.setColor(Color.black);
        if (selected) {
            for (int i = 0; i < points.length;) {
                g2.drawRect((int) points[i++] - 3, (int) points[i++] - 3, 7, 7);
            }
        }
        g2.setTransform(saveAT);
    }
}
