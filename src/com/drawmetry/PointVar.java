/*
 * PointVar.java
 *
 * Created on February 18, 2004, 10:42 PM
 */
package com.drawmetry;

/**
 *
 * @author  Erik Colban
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.undo.UndoableEdit;

public final class PointVar implements DrawableEntity {

    public double x,  y;
    private double initX,  initY;
    private String id;
    private Constraint constraint;
    private Set<Constraint> successors = new HashSet<Constraint>();
    private boolean hidden = false;

    /**
     * Creates a new instance of PointVar
     */
    public PointVar() {
        this.id = "P_temp";
    }

    public PointVar(double x, double y, int n) {
        this.x = x;
        this.y = y;
        this.id = "P_" + n;
    }

    public PointVar(double x, double y, String id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    @Override
    public PointVar clone() throws CloneNotSupportedException {
        PointVar clone = (PointVar) super.clone();
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        return clone;
    }

//    @Override
    public boolean hasType(DmEntity.Type type) {
        return type == Type.POINT_VAR;
    }

//    @Override
    public int dof() {
        if (getConstraint() == null) {
            return (2);
        } else {
            return (2 - getConstraint().degree());
        }
    }

    @Override
    public String toString() {
        return getEntityID() + "[" + x + ", " + y + "]";
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
    public EntityRecord makeRecord(final int zIndex) {
        final PointVar entity = this;
        return new EntityRecord() {

            private double[] params = new double[]{x, y};

//            @Override
            public PointVar getEntity() {
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
        if (er.getEntity() == this) {
            double[] params = er.getParams();
            x = params[0];
            y = params[1];
        }
    }

//    @Override
    public Constraint getConstraint() {
        return this.constraint;
    }

//    @Override
    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

//    @Override
    public Set<Constraint> getSuccessors() {
        return this.successors;
    }

//    @Override
    public String getEntityID() {
        return id;
    }

    public void setEntityID(String id) {
        this.id = id;
    }

//    @Override
    public UndoableEdit setStrokeColor(Color currentColor) {
        return null;
    }

//    @Override
    public void move(double deltaX, double deltaY) {
        this.x = initX + deltaX;
        this.y = initY + deltaY;
    }

//    @Override
    public void initMove() {
        initX = x;
        initY = y;
    }

//    @Override
    public void translate(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
    }

//    @Override
    public Color getStrokeColor() {
        throw new UnsupportedOperationException("Not implemented.");
    }

//    @Override
    public Color getFillColor() {
        throw new UnsupportedOperationException("Not implemented.");
    }

//    @Override
    public double getOpacity() {
        throw new UnsupportedOperationException("Not implemented.");
    }

//    @Override
    public UndoableEdit setStroke(BasicStroke stroke) {
        return null;
    }

//    @Override
    public BasicStroke getStroke() {
        throw new UnsupportedOperationException("Not imlplemented.");
    }

//    @Override
    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x - 2.0, y - 2.0, 4.0, 4.0);
    }

//    @Override
    public UndoableEdit setFillColor(Color color) {
        return null;
    }

//    @Override
    public UndoableEdit setOpacity(double newValue) {
        return null;
    }

//    @Override
    public Arrowhead getStartArrowhead() {
        throw new UnsupportedOperationException("Not imlplemented.");
    }

//    @Override
    public UndoableEdit setStartArrowhead(final Arrowhead newArrowhead) {

        return null;
    }

//    @Override
    public Arrowhead getEndArrowhead() {
        throw new UnsupportedOperationException("Not imlplemented.");
    }

//    @Override
    public UndoableEdit setEndArrowhead(final Arrowhead newArrowhead) {
        return null;
    }

    public boolean hasStroke() {
        return false;
    }

    public boolean hasFill() {
        return false;
    }

    public void setVisible(boolean visible) {
        hidden = !visible;
    }

    public boolean isVisible() {
        return !hidden;
    }
}
