package com.drawmetry;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

final class VTrace implements ViewObject {

    private static GeneralPath transformedPath;
    private static GeneralPath rawPath;
    private static AffineTransform fullTransform;
    private static AffineTransform anchorTransform = new AffineTransform();
    private static AffineTransform modelToViewTransform;
    private static final BasicStroke defaultStroke = new BasicStroke();
    private static BasicStroke stroke;
    private static Color strokeColor;
    private static Color fillColor;
    private static float opacity;
    private static boolean selected;
    private static boolean constrained;
    private static VTrace vt = new VTrace();
    private static PathIterator pathIterator;
    private static boolean testMode = true;
    private static Arrowhead startArrowhead;
    private static Arrowhead endArrowhead;
    private static double[] startVector;
    private static double[] endVector;

    private VTrace() {
    }

    public static VTrace getInstance(
            final MTrace mt, final AffineTransform mvt, final boolean selected) {
        VTrace.modelToViewTransform = mvt;
        mt.getTransform(anchorTransform);
        VTrace.fullTransform = new AffineTransform(mvt);
        VTrace.fullTransform.concatenate(anchorTransform);
        VTrace.selected = selected;
        VTrace.constrained = mt.getAnchor().getConstraint() != null;
        VTrace.strokeColor = mt.getStrokeColor();
        VTrace.fillColor = mt.getFillColor();
        VTrace.opacity = (float) mt.getOpacity();
        VTrace.stroke = mt.getStroke();
        VTrace.rawPath = mt.getPath(anchorTransform);
        VTrace.transformedPath = mt.getPath(fullTransform);
        assert rawPath != transformedPath;
        VTrace.startArrowhead = mt.getStartArrowhead();
        VTrace.endArrowhead = mt.getEndArrowhead();
        if (VTrace.startArrowhead != null) {
            startVector = mt.getStartVector(anchorTransform);
        }
        if (VTrace.endArrowhead != null) {
            endVector = mt.getEndVector(anchorTransform);
        }
        VTrace.pathIterator = mt.getPathIterator(fullTransform);
        return vt;
    }

//    @Override
    public boolean hits(int x, int y) {
        return transformedPath.intersects(x - 2, y - 2, 4, 4) &&
                !transformedPath.contains(x - 2, y - 2, 4, 4);
    }

//    @Override
    public boolean containedIn(Rectangle2D rect) {
        return (rect.contains(transformedPath.getBounds()));
    }

//    @Override
    public void draw(Graphics2D g2) {
        AffineTransform saveAT = g2.getTransform();
        g2.transform(modelToViewTransform);
        if (fillColor != null) {
            g2.setColor(fillColor);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g2.fill(rawPath);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
        }
        if (strokeColor != null) {
            g2.setColor(strokeColor);
            g2.setStroke(stroke);
            g2.draw(rawPath);
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

        g2.setTransform(saveAT);
        if (selected) {
            g2.setColor(constrained ? Color.PINK : Color.LIGHT_GRAY);
            if (testMode) {
                insertControlPoints(pathIterator, g2);
            }
            g2.draw(transformedPath.getBounds());
        }
    }

    private void insertControlPoints(PathIterator iterator, Graphics2D g2) {
        double[] tmp = new double[6];

        while (!iterator.isDone()) {
            switch (iterator.currentSegment(tmp)) {
                case PathIterator.SEG_CLOSE: {
                    break;
                }
                case PathIterator.SEG_MOVETO: {
                    g2.fillOval((int) tmp[0] - 2, (int) tmp[1] - 2, 4, 4);
                }
                case PathIterator.SEG_LINETO: {
                    g2.fillOval((int) tmp[0] - 2, (int) tmp[1] - 2, 4, 4);
                    break;
                }
                case PathIterator.SEG_QUADTO: {
                    g2.drawOval((int) tmp[0] - 2, (int) tmp[1] - 2, 4, 4);
                    g2.fillOval((int) tmp[2] - 2, (int) tmp[3] - 2, 4, 4);
                    break;
                }
                case PathIterator.SEG_CUBICTO: {
                    g2.drawOval((int) tmp[0] - 2, (int) tmp[1] - 2, 4, 4);
                    g2.drawOval((int) tmp[2] - 2, (int) tmp[3] - 2, 4, 4);
                    g2.fillOval((int) tmp[4] - 2, (int) tmp[5] - 2, 4, 4);
                    break;
                }
                default: {
                    assert false;
                }
            }
            iterator.next();
        }
    }
}
