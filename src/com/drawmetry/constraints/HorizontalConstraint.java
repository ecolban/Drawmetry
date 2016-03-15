/*
 * Class.java
 *
 * Created on February 18, 2004, 11:42 PM
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
public class HorizontalConstraint extends Constraint {
    private PointVar pred;
    private LineVar target;
    
    /** Creates a new instance of HorizontalConstraint */
    public HorizontalConstraint() {
    }
    
    @Override
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.POINT_VAR};
    }
    
    @Override
    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.LINE_VAR;
    }
    
    @Override
    public DmEntity[] getPreds() {
        return new DmEntity [] { pred };
    }
    
    @Override
    public com.drawmetry.DmEntity getTarget() {
        return target;
    }

    @Override
    public void setPreds(int i, DmEntity e) {
        if (i != 0 ||
                !(e instanceof PointVar) && e != null) {
            throw new IllegalArgumentException();
        }
                pred = (PointVar) e;
    }

    @Override
    public void setPreds(DmEntity[] preds) {
        if (preds.length != 1 ||
                !(preds[0] instanceof PointVar) && preds[0] != null) {
            throw new IllegalArgumentException();
        }
        pred = (PointVar) preds[0];
    }

    @Override
    protected void setTarget(DmEntity target) {
        if (!(target instanceof LineVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (LineVar) target;
    }

    @Override
    public String toString() {
        return "horizontal(" + pred.getEntityID() + ")";
    }
    
    @Override
    public int degree() {
        return 1;
    }
    
    @Override
    public void fire() {
        target.a = 0.0;
        target.b = 1.0;
        target.c = - pred.y;
    }
}
