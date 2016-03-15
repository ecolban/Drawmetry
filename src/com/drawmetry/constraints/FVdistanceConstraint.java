/*
 * EquidistantConstraint.java
 *
 * Created on July 3, 2004, 12:07 AM
 */

package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.NumVar;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik Colban
 */
public class FVdistanceConstraint extends Constraint {
    
    private PointVar pred1, pred2;
    private NumVar target;
    
    /** Creates a new instance of FVdistanceConstraint */
    public FVdistanceConstraint() {
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR};
    }
    
    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.NUM_VAR;
    }
    
    public DmEntity[] getPreds() {
        return new DmEntity [] { pred1, pred2 };
    }
    
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 1 ||
                !(e instanceof PointVar) && e != null) {
            throw new IllegalArgumentException();
        }
        switch (i) {
            case 0:
                pred1 = (PointVar) e;
                break;
            case 1:
                pred2 = (PointVar) e;
                break;
        }
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 2 ||
                !(preds[0] instanceof PointVar) && preds[0] != null ||
                !(preds[1] instanceof PointVar) && preds[1] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
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
        if (pred1 != null && pred2 != null) {
            target.nValue = +pred1.y - pred2.y;
        }
    }
    
    @Override
    public String toString() {
        return "vdistance(" + pred1.getEntityID() + "," + pred2.getEntityID() + ")";
    }
    
    @Override
    public int degree() {
        return 1;
    }
}
