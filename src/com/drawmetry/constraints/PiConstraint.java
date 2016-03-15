/*
 * PiConstraint.java
 *
 * Created on March 2007.
 */

package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.NumVar;

/**
 *
 * @author  Erik Colban
 */
public class PiConstraint extends Constraint {
    
    private NumVar target;
    
    /** Creates a new instance of CosConstraint */
    public PiConstraint() {
    }
    
    public com.drawmetry.DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {};
    }
    
    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.NUM_VAR;
    }
    
    public DmEntity[] getPreds() {
        return new DmEntity [] {};
    }
    
    public void setPreds(int i, DmEntity e) {
        assert false;
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 0 ){
            throw new IllegalArgumentException();
        }
    }
    
    protected void setTarget(DmEntity target) {
        if (!(target instanceof NumVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (NumVar) target;
        if (target != null) {
            this.target.precedence = 4;
        }
    }
    protected DmEntity getTarget() {
        return target;
    }
    
    public void fire() {
            target.nValue = Math.PI;
    }
    
    public String toString() {
        return "pi";
    }
    
    @Override
    public int degree() {
        return 1;
    }
}
