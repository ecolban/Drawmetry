/*
 * HDistanceConstraint.java
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
public class HDistanceConstraint extends Constraint {
    
    private PointVar pred1;
    private NumVar pred2;
    private LineVar target;
    
    /** Creates a new instance of HDistanceConstraint */
    public HDistanceConstraint() {
    }
    
    @Override
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.POINT_VAR, DmEntity.Type.NUM_VAR};
    }
    
    @Override
    public DmEntity getTarget() {
        return target;
    }
    
    @Override
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.LINE_VAR;
    }
    
    @Override
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 1 ||
                i == 0 && !(e instanceof PointVar) && e != null ||
                i == 1 && !(e instanceof NumVar) && e != null) {
            throw new IllegalArgumentException();
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
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
        pred2 = (NumVar) preds[1];
    }

    @Override
    protected void setTarget(DmEntity target) {
        if (!(target instanceof LineVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (LineVar) target;
    }

    public DmEntity[] getPreds() {
        return new DmEntity [] { pred1, pred2 };
    }
    
    public String toString() {
        return "hdistance(" + pred1.getEntityID() + "," + pred2.toString() + ")";
    }
    
    public int degree() {
        return 1;
    }
    
    
    public void fire() {
        if (pred1 != null && pred2 != null) {
            target.a = 1;
            target.b = 0;
            target.c = - pred1.x - pred2.nValue;
        }
    }
    
    
}
