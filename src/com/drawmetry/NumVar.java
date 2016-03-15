/*
 * Class.java
 *
 * Created on July 3, 2004, 11:51 PM
 */

package com.drawmetry;

import java.util.*;

/**
 *
 * @author  Erik
 */
public class NumVar implements DmEntity {
    
    public double nValue;
    public int precedence = 4;
    private NumVar entity = this;
    private Constraint constraint;
    private Set<Constraint> successors = new HashSet<Constraint>();
    private static int n = 0;
    private String id = "Num_tmp";
    
    /** Creates a new instance of NumVar */
    public NumVar() {
        id = "Num_" + n++;
    }
    
    public NumVar(double value) {
        this.nValue = value;
        this.id = "Num_" + n++;
    }
    
    @Override
    public NumVar clone() throws CloneNotSupportedException {
        NumVar clone = (NumVar) super.clone();
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        clone.entity = clone;
        clone.id = "Num_" + n++;
        return clone;
    }
    
    @Override
    public String toString(){
        return (constraint == null) ?
            Double.toString(nValue) : constraint.toString();
    }
    
//    @Override
    public boolean isConstraintRequired() {
        return false;
    }
    
//    @Override
    public boolean isSuccessorRequired() {
        return true;
    }
    
//    @Override
    public EntityRecord makeRecord(final int z) {
        return new EntityRecord() {
            private double[] params = new double [] {nValue};
//            @Override
            public NumVar getEntity() {
                return entity;
            }
//            @Override
            public double[] getParams() {
                return params;
            }
//            @Override
            public int getZIndex() {
                return z;
            }
        };
    }
    
//    @Override
    public void setParams(EntityRecord er) {
        if ( er.getEntity() == entity) {
            nValue = er.getParams()[0];
        }
    }
    
//    @Override
    public boolean hasType(Type type) {
        return type == Type.NUM_VAR;
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
        return this.successors;
    }
    
//    @Override
    public String getEntityID() {
        return id;
    }
    
//    @Override
    public void translate(double deltaX, double deltaY) {
    }
    
//    @Override
    public void initMove() {
    }
    
//    @Override
    public void move(double deltaX, double deltaY) {
    }

//    @Override
    public double getNValue() {
        return nValue;
    }
    
//    @Override
    public void setNValue(double nVal) {
        nValue = nVal;
    }
    
}
