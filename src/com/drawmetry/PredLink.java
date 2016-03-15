/*
 * ECLinkRecord.java
 *
 * Created on July 13, 2005, 11:30 PM
 */

package com.drawmetry;

/**
 *
 * @author Erik
 */
public class PredLink {
    
    private DmEntity e;
    private Constraint c;
    private int i;
    /** Creates a new instance of ECLinkRecord */
    public PredLink(DmEntity e, Constraint c, int i) {
        this.e = e;
        this.c = c;
        this.i = i;
    }
    public DmEntity getEntity() {
        return this.e;
    }
    
    public Constraint getConstraint() {
        return this.c;
    }
    
    public int getIndex() {
        return this.i;
    }
    
    
}
