/*
 * MCircle.java
 *
 * Created on February 18, 2004, 10:42 PM
 */
package com.drawmetry;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.awt.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

public final class MCircle implements DrawableEntity {

    private MCircle entity = this;
    private Constraint constraint;
    private Set<Constraint> successors = new HashSet<Constraint>();
    private boolean selected = false;
    public double x,  y;
    public double radius;
    private String id;
    private transient BasicStroke stroke;
    private Color color;
    private static int n;

    /** Creates a new instance of PointVar */
    public MCircle(Color c) {
        this.id = "Circle_" + n++;
        this.color = c;
    }

    @Override
    public MCircle clone() throws CloneNotSupportedException {
        MCircle clone = (MCircle) super.clone();
        clone.entity = clone;
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        clone.id = "Circle_" + n++;
        return clone;
    }

//    @Override
    public String getEntityID() {
        return id;
    }

    @Override
    public String toString() {
        return new String(id + "(" + "[" + (x + radius) + "," + (y + radius) + "], " + radius + ")");
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
    public EntityRecord makeRecord(final int zIndex) {
        return new EntityRecord() {

            private double[] params = new double[]{x, y, radius};

//            @Override
            public MCircle getEntity() {
                return entity;
            }

//            @Override
            public double[] getParams() {
                return params;
            }

//            @Override
            public int getZIndex() {
                return zIndex;
            }
        };
    }

//    @Override
    public void setParams(EntityRecord er) {
        if (er.getEntity() == entity) {
            double[] params = er.getParams();
            x = params[0];
            y = params[1];
            radius = params[2];
        }
    }

//    @Override
    public boolean hasType(Type type) {
        return type == Type.ANY;
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

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getSelected() {
        return selected;
    }

//    @Override
    public UndoableEdit setStrokeColor(final Color newColor)
    {
        final Color oldColor = this.color;
        this.color = newColor;
        return new AbstractUndoableEdit(){
            @Override
            public void undo(){
                super.undo();
                color = oldColor;
            }

            @Override
            public void redo() {
                super.redo();
                color = newColor;
            }
        };
    }

    public double getX() {
        return 0.0;
    }

    public double getY() {
        return 0.0;
    }

//    @Override
    public void translate(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
    }

//    @Override
    public void move(double x, double y) {
    }

//    @Override
    public void initMove() {
    }

//    @Override
    public Color getStrokeColor() {
        return color;
    }

//    @Override
    public UndoableEdit setStroke(final BasicStroke newStroke)
    {
        final BasicStroke oldStroke = stroke;
        stroke = newStroke;
        return new AbstractUndoableEdit(){
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
    public int dof() {
        if (getConstraint() != null) {
            return 3 - getConstraint().degree();
        } else {
            return 3;
        }
    }

    public Rectangle2D getBounds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UndoableEdit setFillColor(Color color)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Color getFillColor()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Arrowhead getStartArrowhead()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UndoableEdit setStartArrowhead(Arrowhead arrowhead)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Arrowhead getEndArrowhead()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UndoableEdit setEndArrowhead(Arrowhead arrowhead)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getOpacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UndoableEdit setOpacity(double opacity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasStroke() {
        return true;
    }

    public boolean hasFill() {
        return true;
    }
}

