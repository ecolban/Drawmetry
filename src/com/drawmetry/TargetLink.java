/*
 * CELinkRecord.java
 *
 * Created on July 13, 2005, 11:18 PM
 */

package com.drawmetry;

/**
 *
 * @author Erik
 */
public class TargetLink {
    private Constraint c;
    private DmEntity e;
    
    /** Creates a new instance of CELinkRecord */
    public TargetLink(Constraint c, DmEntity e) {
        this.c = c;
        this.e = e;
    }
    
    public Constraint getConstraint() {
        return this.c;
    }
    
    public DmEntity getEntity() {
        return this.e;
    }
    
}
