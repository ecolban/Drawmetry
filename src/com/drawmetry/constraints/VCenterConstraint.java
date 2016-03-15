/*
 * VCenterConstraint.java
 *
 * Created on February 19, 2004, 12:07 AM
 */

package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.LineVar;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik Colban
 */
public class VCenterConstraint extends Constraint {
    
    private PointVar pred1, pred2;
    private LineVar target;
    
    /** Creates a new instance of VCenterConstraint */
    public VCenterConstraint() {
    }
    
    @Override
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR};
    }
    
    @Override
    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.LINE_VAR;
    }
    
    @Override
    public DmEntity[] getPreds() {
        return new DmEntity [] { pred1, pred2 };
    }
    
    @Override
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

    @Override
    public void setPreds(DmEntity[] preds) {
        if (preds.length != 2 ||
                !(preds[0] instanceof PointVar) && preds[0] != null ||
                !(preds[1] instanceof PointVar) && preds[1] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
    }

    @Override
    protected void setTarget(DmEntity target) {
        if (!(target instanceof LineVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (LineVar) target;
    }

    @Override
    protected DmEntity getTarget() {
        return target;
    }
    
    
    @Override
    public void fire() {
        if (pred1 != null && pred2 != null) {
            target.a = 0.0;
            target.b = 2.0;
            target.c = -pred1.y - pred2.y;
        }
    }
    
    
    @Override
    public String toString() {
        return "vcenter(" + pred1.getEntityID() + "," + pred2.getEntityID() + ")";
    }
    
    @Override
    public int degree() {
        return 1;
    }
    
}
