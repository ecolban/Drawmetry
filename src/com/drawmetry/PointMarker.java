/*
 * PointMarker.java
 *
 */

package com.drawmetry;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

/**
 *
 * @author  Erik Colban
 */

public final class PointMarker implements ViewObject {
    
    private static double x, y;
    private static Color color;
    private static boolean selected;
    private static boolean visible;
    private static int dof;
    private static Point2D.Double srcPt = new Point2D.Double();
    private static Point2D.Double trgPt = new Point2D.Double();
    private static PointMarker pm = new PointMarker();
    
    /** Creates a new instance of PointMarker */
    private PointMarker() {
    }
    
    public static PointMarker getInstance(PointVar cp, 
            AffineTransform at, boolean selected, boolean visible) {
        srcPt.x = cp.x;
        srcPt.y = cp.y;
        trgPt = (Point2D.Double) at.transform(srcPt, trgPt);
        PointMarker.x = trgPt.x;
        PointMarker.y = trgPt.y;
        dof = cp.dof();
        if (dof == 0) {
            PointMarker.color = Color.RED;
        } else if (dof == 1) {
            PointMarker.color = Color.ORANGE;
        } else if (dof == 2) {
            PointMarker.color = Color.GREEN;
        }
        PointMarker.selected = selected;
        PointMarker.visible = visible;
       return pm;
    }
    
    public boolean hits(int x, int y) {
        return
                x - 3 <= PointMarker.x && PointMarker.x <= x + 3 &&
                y - 3 <= PointMarker.y && PointMarker.y <= y + 3;
    }
    
    public boolean containedIn(Rectangle2D rect) {
        return rect.contains(x, y);
    }
    
    public void draw(java.awt.Graphics2D g2) {
        if (!visible) return;
        g2.setColor(color);
        if (selected)
            g2.fill(new Rectangle2D.Double(x - 3, y - 3, 7, 7));
        else
            g2.fill(new Ellipse2D.Double(x - 2, y - 2, 5, 5));
        if (g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING) ==
                RenderingHints.VALUE_ANTIALIAS_ON) {
        // Do whatever ...
        }
    }
}

