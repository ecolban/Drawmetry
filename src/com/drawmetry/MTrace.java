/*
 * MTrace.java
 *
 * Created on October 9, 2005, 10:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import com.drawmetry.DmEntity.Type;
import com.drawmetry.constraints.NullConstraint;

/**
 *
 * @author Erik Colban
 */
public final class MTrace implements PathEntity, DrawableEntity, SimplePath {

    private GeneralPath path = new GeneralPath();
    private boolean closed = false;
    transient private ArrayList<Point2D.Double> points;
    private String id;
    private AnchorVar anchor;
    private Color strokeColor;
    private Color fillColor;
    private double opacity = 1.0;
    private BasicStroke stroke;
    private Constraint constraint;
    private Set<Constraint> successors = new HashSet<Constraint>();
    transient private Point2D lastPoint;
    private Point2D.Double[] samplePoints;
    private double threshold = 1.0;
    private Arrowhead startArrowhead = null;
    private Arrowhead endArrowhead = null;

    /**
     * Creates a new instance of MTrace
     */
    MTrace(Color strokeColor, Color fillColor, double opacity, BasicStroke stroke,
            AnchorVar anchor, Arrowhead startArrowhead, Arrowhead endArrowhead, int windingRule) {
        setStrokeColor(strokeColor);
        setFillColor(fillColor);
        setOpacity(opacity);
        setStroke(stroke);
        points = new ArrayList<Point2D.Double>();
        this.anchor = anchor;
        this.startArrowhead = startArrowhead;
        this.endArrowhead = endArrowhead;
        path.setWindingRule(windingRule);
    }

    MTrace() {
        super();
        setStrokeColor(Color.BLACK);
        setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        points = null;
        anchor = new AnchorVar();
    }

    @Override
    public MTrace clone() throws CloneNotSupportedException {
        MTrace clone = null;
        clone = (MTrace) super.clone();
        clone.path = (GeneralPath) path.clone();
//        clone.id = "S_" + n++;
        clone.constraint = null;
        clone.anchor = null;
        clone.successors = new HashSet<Constraint>();
        return clone;
    }

    boolean isDegenerate() {
        int numSegments = 0;
        double[][] d = new double[2][6];
        int[] types = new int[2];
        for (PathIterator pit = getPathIterator(null); !pit.isDone() && numSegments < 2; pit.next()) {
            types[numSegments] = pit.currentSegment(d[numSegments]);
            numSegments++;

        }
        return numSegments < 2 || numSegments == 2 &&
                types[0] == PathIterator.SEG_MOVETO &&
                types[1] == PathIterator.SEG_LINETO &&
                d[0][0] == d[1][0] &&
                d[0][1] == d[1][1];
    }

    public boolean hasStroke() {
        return true;
    }

    public boolean hasFill() {
        return true;
    }

    static private class MTraceRecord implements EntityRecord {

        private MTrace mc;
        private int zIndex;

        MTraceRecord(MTrace mc, int z) {
            this.mc = mc;
            this.zIndex = z;

        }

//        @Override
        public DmEntity getEntity() {
            return this.mc;
        }

//        @Override
        public double[] getParams() {
            throw new UnsupportedOperationException("Not implemented.");
        }

//        @Override
        public int getZIndex() {
            return zIndex;
        }
    }

//    @Override
    public String getEntityID() {
        return id;
    }

    public void setEntityID(String id) {
        this.id = id;
    }

    AnchorVar getAnchor() {
        if (anchor == null) {
            Constraint c = getConstraint();
            assert c != null;
            if (c != null) {
                assert c instanceof NullConstraint && c.getPreds().length == 1;
                anchor = (AnchorVar) c.getPreds()[0];
                assert anchor != null;
            }

        }
        return anchor;
    }

//    @Override
    public UndoableEdit setStrokeColor(final Color newColor) {
        final Color oldColor = this.strokeColor;
        this.strokeColor = newColor;
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                strokeColor = oldColor;
            }

            @Override
            public void redo() {
                super.redo();
                strokeColor = newColor;
            }
        };
    }

//    @Override
    public Color getStrokeColor() {
        return strokeColor;
    }

    public UndoableEdit setFillColor(final Color newColor) {
        final Color oldColor = fillColor;
        fillColor = newColor;
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                fillColor = oldColor;
            }

            @Override
            public void redo() {
                super.redo();
                fillColor = newColor;
            }
        };
    }

    public Color getFillColor() {
        return fillColor;
    }

    public UndoableEdit setOpacity(final double newValue) {
        final double oldValue = opacity;
        opacity = newValue;
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                opacity = oldValue;
            }

            @Override
            public void redo() {
                super.redo();
                opacity = newValue;
            }
        };
    }

    public double getOpacity() {
        return opacity;
    }

    public UndoableEdit setWindingRule(final int newValue) {
        final int oldValue = path.getWindingRule();
        path.setWindingRule(newValue);
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                path.setWindingRule(oldValue);
            }

            @Override
            public void redo() {
                super.redo();
                path.setWindingRule(newValue);
            }
        };
    }

    public int getWindingRule() {
        return path.getWindingRule();
    }

//    @Override
    public UndoableEdit setStroke(final BasicStroke newStroke) {
        final BasicStroke oldStroke = stroke;
        stroke = newStroke;
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                stroke = oldStroke;
            }

            @Override
            public void redo() {
                super.redo();
                stroke = newStroke;
            }
        };
    }

//    @Override
    public BasicStroke getStroke() {
        return stroke;
    }

//    @Override
    public Arrowhead getStartArrowhead() {
        return startArrowhead;
    }

//    @Override
    public UndoableEdit setStartArrowhead(final Arrowhead newArrowhead) {
        final Arrowhead oldArrowhead = startArrowhead;
        startArrowhead = newArrowhead;
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                startArrowhead = oldArrowhead;
            }

            @Override
            public void redo() {
                super.redo();
                startArrowhead = newArrowhead;
            }
        };

    }

//    @Override
    public Arrowhead getEndArrowhead() {
        return endArrowhead;
    }

//    @Override
    public UndoableEdit setEndArrowhead(final Arrowhead newArrowhead) {
        final Arrowhead oldArrowhead = endArrowhead;
        endArrowhead = newArrowhead;
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                endArrowhead = oldArrowhead;
            }

            @Override
            public void redo() {
                super.redo();
                endArrowhead = newArrowhead;
            }
        };


    }

//    @Override
    public boolean hasType(Type type) {
        return type == Type.TRACE || type == Type.PATH;
    }

    public void getTransform(AffineTransform at) {
        getAnchor().getTransform(at);
    }

//    @Override
    public Constraint getConstraint() {
        return constraint;
    }

//    @Override
    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

//    @Override
    public Set<Constraint> getSuccessors() {
        return successors;
    }

//    @Override
    public boolean isConstraintRequired() {
        return false;
    }

//    @Override
    public boolean isSuccessorRequired() {
        return false;
    }

//    @Override
    public EntityRecord makeRecord(
            int zIndex) {
        return new MTraceRecord(this, zIndex);

    }

//    @Override
    public void setParams(EntityRecord er) {
//        if (er.getEntity() == this && (er instanceof MTraceRecord)) {
//            MTraceRecord mcr = (MTraceRecord) er;
//            strokeColor = mcr.color;
//            stroke = mcr.stroke;
//        }

    }

//    @Override
    public void translate(double deltaX, double deltaY) {
//        This method is required by contract.
//        A trace never gets translated. Instead, its anchor gets translated.
    }

//    @Override
    public void move(double deltaX, double deltaY) {
        AnchorVar a = getAnchor();
        assert a != null;
        a.move(deltaX, deltaY);
    }

//    @Override
    public void initMove() {
        getAnchor().initMove();
    }

    /**
     *
     */
    void addInitial(PointVar cp) {
        lastPoint = new Point2D.Double(cp.x, cp.y);
        points.add((Point2D.Double) lastPoint.clone());
        reset();
        moveTo(cp.x, cp.y);
    }

    public void add(PointVar cp) {

        double epsilon = 1.0;
        double deltaX = lastPoint.getX() - cp.x;
        double deltaY = lastPoint.getY() - cp.y;
        if (Math.abs(deltaX) > epsilon || Math.abs(deltaY) > epsilon) {
            lastPoint.setLocation(cp.x, cp.y);
            points.add((Point2D.Double) lastPoint.clone());
            lineTo(cp.x, cp.y);
        }

    }

    public GeneralPath getPath(AffineTransform at) {
        GeneralPath clone = (GeneralPath) path.clone();
        clone.transform(at);
        return clone;
    }

//    @Override
    public void reset() {
        path.reset();
    }

//    @Override
    public void moveTo(double x, double y) {
        path.moveTo((float) x, (float) y);
    }

//    @Override
    public void lineTo(double x, double y) {
        path.lineTo((float) x, (float) y);
    }

//    @Override
    public void quadTo(double x1, double y1, double x2, double y2) {
        path.quadTo((float) x1, (float) y1, (float) x2, (float) y2);
    }

//    @Override
    public void curveTo(double x1, double y1, double x2, double y2, double x3,
            double y3) {
        path.curveTo((float) x1, (float) y1, (float) x2, (float) y2, (float) x3, (float) y3);
    }

    public void close() {
        path.closePath();
    }

    PathIterator getPathIterator(final AffineTransform at) {
        return path.getPathIterator(at);
    }

    double[] getStartVector(AffineTransform tf) {
        int i = 0;
        double[] result = new double[4];
        double[] d = new double[6];
        for (PathIterator pit = getPathIterator(tf); !pit.isDone(); pit.next()) {
            pit.currentSegment(d);
            result[i++] = d[0];
            result[i++] = d[1];
            if (i >= 4) {
                break;
            }
        }
        return result;
    }

    double[] getEndVector(AffineTransform tf) {

        double[] first = new double[2];
        double[] result = new double[4];
        double[] d = new double[6];

        for (PathIterator pit = path.getPathIterator(tf); !pit.isDone(); pit.next()) {
            int type = pit.currentSegment(d);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    first[0] = d[0];
                    first[1] = d[1];
                    break;
                case PathIterator.SEG_LINETO:
                    result[0] = result[2];
                    result[1] = result[3];
                    result[2] = d[0];
                    result[3] = d[1];
                    break;
                case PathIterator.SEG_QUADTO:
                    result[0] = d[0];
                    result[1] = d[1];
                    result[2] = d[2];
                    result[3] = d[3];
                    break;
                case PathIterator.SEG_CUBICTO:
                    result[0] = d[2];
                    result[1] = d[3];
                    result[2] = d[4];
                    result[3] = d[5];
                    break;
                case PathIterator.SEG_CLOSE:
                    result[0] = result[2];
                    result[1] = result[3];
                    result[2] = first[0];
                    result[3] = first[1];
                    break;
                default:
                    assert false;
            }
        }
        return result;
    }

    void end() {
        if (points.get(0).distanceSq(lastPoint) < 25) {
            points.add((Point2D.Double) points.get(0).clone());
            closed = true;
        }

        smoothenPath();
    }

    private void smoothenPath() {
        int numPoints = points.size();
        samplePoints = new Point2D.Double[numPoints];
        samplePoints = points.toArray(samplePoints);
        points = null;
        if (numPoints < 2) {
            return;
        }

        PathGenerator.getPath(this, samplePoints, threshold, closed);
        samplePoints = null;
    }

    public Rectangle2D getBounds() {
        AffineTransform at = new AffineTransform();
        getTransform(at);
        GeneralPath path2 = getPath(at);
        return path2.getBounds2D();
    }

    void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
