/*
 * Class.java
 *
 * Created on February 18, 2004, 11:51 PM
 */

package com.drawmetry;

import java.util.*;

/**
 *
 * @author  Erik Colban
 */


public final class LineVar implements DmEntity {
    
    public double a, b, c;
    private double initC;
    private LineVar entity = this;
    private Constraint constraint;
    private static int n = 0;
    private String id;
    private Set<Constraint> successors = new HashSet<Constraint>();
    
    
    @Override
    public LineVar clone() throws CloneNotSupportedException {
        LineVar clone = (LineVar) super.clone();
        clone.entity = clone;
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        clone.id = "LineVar_" + n++;
        return clone;
    }
    
    
    public String toString() {
        return id + "[" + a + "x + " + b + "y + " + c + " = 0]" ;
    }
    
    public boolean isConstraintRequired() {
        return true;
    }
    
    public boolean isSuccessorRequired() {
        return true;
    }
    
    public EntityRecord makeRecord(final int z) {
        return new EntityRecord() {
            private double[] params = new double [] {a, b, c};
            public LineVar getEntity() {
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
        if (er.getEntity() == this ) {
            double[] params = er.getParams();
            a = params[0];
            b = params[1];
            c = params[2];
        }
    }

    public boolean hasType(Type type) {
        return type == Type.LINE_VAR;
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
        c -= a * deltaX + b * deltaY;
    }

    public void move(double deltaX, double deltaY) {
        c = initC - a * deltaX  - b * deltaY; 
    }

    public void initMove() {
        initC = c;
    }    
}
