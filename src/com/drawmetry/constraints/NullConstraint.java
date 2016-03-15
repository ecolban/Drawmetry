/*
 * NullConstraint.java
 *
 * Created on April 20, 2007, 7:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.DmEntity.Type;

/**
 *
 * @author Erik
 */
public class NullConstraint extends Constraint implements Cloneable {
    
    private DmEntity [] preds;
    private final DmEntity.Type [] types;
    private DmEntity target;
    
    
    
    /**
     * Creates a new instance of NullConstraint
     */
    public NullConstraint(int numPreds) {
        preds = new DmEntity [numPreds];
        types = new DmEntity.Type [numPreds];
        for (int i = 0; i < numPreds; i++) {
            types[i] = Type.ANY;
        }
    }

    @Override
    protected DmEntity.Type[] getPredTypes() {
        return types;
    }
    
    @Override
    protected DmEntity.Type getTargetType() {
        return Type.ANY;
    }
    
    @Override
    public DmEntity[] getPreds() {
        return (DmEntity []) preds.clone();
    }
    
    @Override
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i> preds.length) {
            throw new IllegalArgumentException();
        }
        preds[i] = e;
    }

    @Override
    public void setPreds(DmEntity[] preds) {
        this.preds = preds.clone();
    }

    @Override
    protected void setTarget(DmEntity target) {
        this.target = target;
    }

    @Override
    protected boolean verifyPreds(DmEntity[] preds) {
        if (preds.length != this.preds.length) {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    protected boolean verifyTarget(DmEntity target) {
        return true;
    }
    
    
    @Override
    protected DmEntity getTarget() {
        return target;
    }
    
    @Override
    public void fire() {
    }
    
    @Override
    public String toString() {
        return "";
    }
}
