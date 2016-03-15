/*
 * Class.java
 *
 * Created on July 3, 2004, 11:51 PM
 */

package com.drawmetry;

import java.util.HashSet;
import java.util.Set;



/**
 *
 * @author  default
 */
public final class CircleVar implements DmEntity {
    
    public double x_0, y_0, radius;
    private double initX_0, initY_0;
    private CircleVar entity = this;
    private Constraint constraint;
    private static int n = 0;
    private String id;
    private Set<Constraint> successors = new HashSet<Constraint>();
    
    /** Creates a new instance of Class */
    public CircleVar() {
        id = "CircleVar_" + n++;
    }
    
    @Override
    public CircleVar clone() throws CloneNotSupportedException {
        CircleVar clone = (CircleVar) super.clone();
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        clone.id = "CircleVar_" + n++;
        clone.entity = clone;
//        clone.initX_0 = 0.0;
//        clone.initY_0 = 0.0;
        return clone;
    }
    
    @Override
    public String toString() {
        return id + "[" +
                "(x - " + x_0 + ")^2 + (y - " + y_0 + ")^2 = " + radius + "^2]";
    }
    
    public boolean isConstraintRequired() {
        return true;
    }
    
    public boolean isSuccessorRequired() {
        return true;
    }
    
    public EntityRecord makeRecord(final int z) {
        return new EntityRecord() {
            private double[] params = new double [] {x_0, y_0, radius};
            public CircleVar getEntity() {
                return entity;
            }
            public double[] getParams() {
                return params;
            }
            public int getZIndex() {
                return z;
            }
        };
    }
    
    public void setParams(EntityRecord er) {
        if (er.getEntity() == entity ) {
            double[] params = er.getParams();
            x_0 = params[0];
            y_0 = params[1];
            radius = params[2];
        }
    }

    public boolean hasType(Type type) {
        return type == Type.CIRCLE_VAR;
    }

    public Constraint getConstraint() {
        return this.constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    public Set<Constraint> getSuccessors() {
        return this.successors;
    }

    public String getEntityID() {
        return id;
    }
    

    public void translate(double deltaX, double deltaY) {
        x_0 += deltaX;
        y_0 += deltaY;
    }

    public void move(double deltaX, double deltaY) {
        x_0 = initX_0 + deltaX;
        y_0 = initY_0 + deltaY;
    }

    public void initMove() {
        initX_0 = x_0;
        initY_0 = y_0;
    }
}
