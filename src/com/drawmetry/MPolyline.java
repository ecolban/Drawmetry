/*
 * MPolyline.java
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
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import com.drawmetry.DmEntity.Type;
import com.drawmetry.constraints.MPolylineConstraint;

/**
 *
 * @author Erik Colban
 */
public final class MPolyline implements DrawableEntity, PathEntity {

    private double[] points; //
    private double[] initPoints;
    private String id;
    private Color strokeColor;
    private Color fillColor;
    private double opacity = 1.0;
    private BasicStroke stroke;
    private MPolylineConstraint constraint;
    private Set<Constraint> successors = new HashSet<Constraint>();
    private boolean isClosed;
    private Arrowhead startArrowhead = Arrowhead.NONE;
    private Arrowhead endArrowhead = Arrowhead.NONE;
    private int windingRule = GeneralPath.WIND_NON_ZERO; //default

    /**
     * Creates a new instance of MPolyline
     */
    public MPolyline(Color strokeColor, Color fillColor, double Opacity,
            BasicStroke stroke, Arrowhead startArrow, Arrowhead endArrow, int windingRule) {
        setStrokeColor(strokeColor);
        setFillColor(fillColor);
        setOpacity(Opacity);
        setStroke(stroke);
        setStartArrowhead(startArrow);
        setEndArrowhead(endArrow);
        setWindingRule(windingRule);
        points = new double[0];
    }

    public void setEntityID(String id) {
        this.id = id;
    }

    public double[] getPoints() {
        return points;
    }

    public void setPoints(double[] points) {
        this.points = points;
    }

    @Override
    public MPolyline clone() throws CloneNotSupportedException {
        MPolyline clone = (MPolyline) super.clone();
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        clone.points = points.clone();
        return clone;
    }

//    @Override
    public UndoableEdit setFillColor(final Color newColor) {
        final Color oldColor = this.fillColor;
        this.fillColor = newColor;
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

//    @Override
    public Color getFillColor() {
        return fillColor;
    }

//    @Override
    public UndoableEdit setOpacity(final double newValue) {
        final double oldValue = this.opacity;
        this.opacity = newValue;
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

//    @Override
    public double getOpacity() {
        return opacity;
    }

//    @Override
    public Arrowhead getStartArrowhead() {
        if (startArrowhead != null) {
            return startArrowhead;
        } else {
            return Arrowhead.NONE;
        }
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
        if (endArrowhead != null) {
            return endArrowhead;
        } else {
            return Arrowhead.NONE;
        }
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

    public boolean hasStroke() {
        return true;
    }

    public boolean hasFill() {
        return true;
    }

    public GeneralPath getPath(AffineTransform at) {
        if (points == null || points.length < 2) {
            return null;
        }
        GeneralPath path = new GeneralPath();
        assert points.length % 2 == 0;
        path.moveTo(points[0], points[1]);
        for(int i = 2; i < points.length;) {
            path.lineTo(points[i++], points[i++]);
        }
        if(isClosed()) {
            path.closePath();
        }
        path.transform(at);
        return path;
    }

    public void getTransform(AffineTransform at) {
        at = new AffineTransform(); // Only the identity transform is supported
    }

    static private class MPolylineRecord implements EntityRecord {

        private MPolyline mpl;
        private int zIndex;
        private Color strokeColor;
        private Color fillColor;
        private BasicStroke stroke;
        private double[] params;

        MPolylineRecord(MPolyline mpl, int z) {
            this.mpl = mpl;
            this.zIndex = z;
            this.strokeColor = mpl.strokeColor;
            this.fillColor = mpl.fillColor;
            this.stroke = mpl.stroke;
            this.params = mpl.points == null ? new double[0] : mpl.points.clone();
        }

//        @Override
        public DmEntity getEntity() {
            return this.mpl;
        }

//        @Override
        public double[] getParams() {
            return params;
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

    public UndoableEdit setWindingRule(final int newValue) {
        final int oldValue = windingRule;
        windingRule = newValue;
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                windingRule = oldValue;
            }

            @Override
            public void redo() {
                super.redo();
                windingRule = newValue;
            }
        };
    }

    public int getWindingRule() {
        return windingRule;
    }

//    @Override
    public boolean hasType(Type type) {
        return type == Type.LINE || type == Type.PATH;
    }

//    public void getTransform(AffineTransform at) {
//       
//    }
//    @Override
    public MPolylineConstraint getConstraint() {
        return constraint;
    }

//    @Override
    public void setConstraint(Constraint constraint) {
        this.constraint = (MPolylineConstraint) constraint;
    }

//    @Override
    public Set<Constraint> getSuccessors() {
        return successors;
    }

//    @Override
    public boolean isConstraintRequired() {
        return true;
    }

//    @Override
    public boolean isSuccessorRequired() {
        return false;
    }

//    @Override
    public EntityRecord makeRecord(int zIndex) {
        return new MPolylineRecord(this, zIndex);

    }

//    @Override
    public void setParams(EntityRecord er) {
        if (er.getEntity() == this && (er instanceof MPolylineRecord)) {
            MPolylineRecord mcr = (MPolylineRecord) er;
            strokeColor = mcr.strokeColor;
            fillColor = mcr.fillColor;
            stroke = mcr.stroke;
            double[] params = er.getParams();
            for (int i = 0; i < params.length; i++) {
                points[i] = params[i];
            }
        }

    }

//    @Override
    public void translate(double deltaX, double deltaY) {
        for (int i = 0; i < points.length;) {
            points[i++] += deltaX;
            points[i++] += deltaY;
        }
    }

//    @Override
    public void move(double deltaX, double deltaY) {
        for (int i = 0; i < points.length;) {
            initPoints[i++] += deltaX;
            initPoints[i++] += deltaY;
        }
    }

//    @Override
    public void initMove() {
        initPoints = points.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Polyline(");
        for (int i = 0; i < points.length;) {
            sb.append(String.format(Locale.US, "[%.2f, %.2f]", points[i++], points[i++]));
        }
        sb.append(")");
        return sb.toString();

    }

    public Rectangle2D getBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < points.length;) {
            minX = Math.min(points[i], minX);
            maxX = Math.max(points[i++], maxX);
            minY = Math.min(points[i], minY);
            maxY = Math.max(points[i++], maxY);
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    public boolean isClosed() {
        return isClosed;
    }

    public UndoableEdit setClosed(final boolean b) {
        isClosed = b;
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                isClosed = !b;
            }

            @Override
            public void redo() {
                super.redo();
                isClosed = b;
            }
        };
    }

    double[] getStartVector() {
        if (points.length < 4) {
            return null;
        }
        double[] vector = new double[4];
        for (int i = 0; i < 4; i++) {
            vector[i] = points[i];
        }
        return vector;
    }

    double[] getEndVector() {
        if (points.length < 4) {
            return null;
        }
        double[] vector = new double[4];
        int offset = points.length - 4;
        for (int i = 0; i < 4; i++) {
            vector[i] = points[i + offset];
        }
        return vector;
    }
}
