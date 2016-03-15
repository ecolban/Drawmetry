/*
 * RotationConstraint.java
 *
 * Created on July 15, 2004, 10:01 PM
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
public class RotationConstraint extends Constraint{
    
    private PointVar pred2, pred1;
    private NumVar pred3;
    private PointVar target;
    
    /** Creates a new instance of RotationConstraint */
    public RotationConstraint() {
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]
        { DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR, DmEntity.Type.NUM_VAR };
    }
    
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.POINT_VAR;
    }
    
    @Override
    public DmEntity[] getPreds() {
        DmEntity [] preds = { pred1, pred2, pred3 };
        return preds;
    }
    
    @Override
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || 2 < i ||
                i < 2 && !(e instanceof PointVar) && e != null ||
                i == 2 && !(e instanceof NumVar) && e != null) {
            throw new IllegalArgumentException("Problems adding a rotation constraint on " + target);
        }
        switch (i) {
            case 0:
                pred1 = (PointVar) e;
                break;
            case 1:
                pred2 = (PointVar) e;
                break;
            case 2:
                pred3 = (NumVar) e;
                break;
        }
    }

    @Override
    public void setPreds(DmEntity[] preds) {
        if (preds.length != 3 ||
                !(preds[0] instanceof PointVar) && preds[0] != null ||
                !(preds[1] instanceof PointVar) && preds[1] != null ||
                !(preds[2] instanceof NumVar) && preds[2] != null) {
            throw new IllegalArgumentException("Problems adding a rotation constraint on " + target);
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
        pred3 = (NumVar) preds[2];
    }

    @Override
    public void setTarget(com.drawmetry.DmEntity target) {
        if (!(target instanceof PointVar) && target != null) {
            throw new IllegalArgumentException("Problems adding a rotation constraint on " + target);
        }
        this.target = (PointVar) target;
    }

    @Override
    protected DmEntity getTarget() {
        return target;
    }
    
    @Override
    public int degree() {
        return 2;
    }
    
    public void fire() {
        double n_x = pred1.x - pred2.x;
        double n_y = pred1.y - pred2.y;
        double cosAlpha = Math.cos(pred3.nValue);
        double sinAlpha = -Math.sin(pred3.nValue);
        target.x = pred2.x + cosAlpha * n_x - sinAlpha * n_y;
        target.y = pred2.y + sinAlpha * n_x + cosAlpha * n_y;
    }
    
    @Override
    public String toString() {
        return "rotate(" + pred1.getEntityID() + "," + pred2.getEntityID() + 
                "," + pred3.toString() + ")";
        
    }
    
}
