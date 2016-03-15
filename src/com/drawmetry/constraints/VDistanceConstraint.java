/*
 * VDistanceConstraint.java
 *
 * Created on July 3, 2004, 12:07 AM
 */

package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.LineVar;
import com.drawmetry.NumVar;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik Colban
 */
public class VDistanceConstraint extends Constraint {
    
    private PointVar pred1;
    private NumVar pred2;
    private LineVar target;
    
    /** Creates a new instance of VDistanceConstraint */
    public VDistanceConstraint() {
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.POINT_VAR, DmEntity.Type.NUM_VAR};
    }
    
    public DmEntity getTarget() {
        return target;
    }
    
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.LINE_VAR;
    }
    
    @Override
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 1 ||
                i == 0 && !(e instanceof PointVar) && e != null ||
                i == 1 && !(e instanceof NumVar) && e != null) {
            throw new IllegalArgumentException("Problems adding a vdistance constraint on " + target);
        }
        switch (i) {
            case 0:
                pred1 = (PointVar) e;
                break;
            case 1:
                pred2 = (NumVar) e;
                break;
        }
    }

    @Override
    public void setPreds(DmEntity[] preds) {
        if (preds.length != 2 ||
                !(preds[0] instanceof PointVar) && preds[0] != null ||
                !(preds[1] instanceof NumVar) && preds[1] != null) {
            throw new IllegalArgumentException("Problems adding a vdistance constraint on " + target);
        }
        pred1 = (PointVar) preds[0];
        pred2 = (NumVar) preds[1];
    }

    @Override
    protected void setTarget(DmEntity target) {
        if (!(target instanceof LineVar) && target != null) {
            throw new IllegalArgumentException("Problems adding a vdistance constraint on " + target);
        }
        this.target = (LineVar) target;
    }

    public DmEntity[] getPreds() {
        return new DmEntity [] { pred1, pred2 };
    }
    
    @Override
    public String toString() {
        return "vdistance(" + pred1.getEntityID() + "," + pred2.toString() + ")";
    }
    
    @Override
    public int degree() {
        return 1;
    }
    
    
    public void fire() {
        if (pred1 != null && pred2 != null) {
            target.a = 0.0;
            target.b = 1.0;
            target.c = -pred1.y + pred2.nValue;
        }
    }
    
    
}
