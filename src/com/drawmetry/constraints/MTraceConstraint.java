/*
 * ShapeConstraint.java
 *
 * Created on October 22, 2004, 10:24 AM
 */
package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.MTrace;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik
 */
public class MTraceConstraint extends Constraint {

    private PointVar pred1;
    private MTrace target;

    public MTraceConstraint() {
    }

    /** Creates a new instance of ShapeConstraint */
    public DmEntity[] getPreds() {
        DmEntity[] preds = {pred1};
        return preds;
    }

    public void fire() {
        target.add(pred1);
    }

    @Override
    public int degree() {
        return 0;
    }

    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{DmEntity.Type.POINT_VAR};
    }

    public com.drawmetry.DmEntity getTarget() {
        return target;
    }

    public DmEntity.Type getTargetType() {
        return DmEntity.Type.TRACE;
    }

    public void setPreds(int i, DmEntity e) {
        if (i != 0
                || i == 0 && !(e instanceof PointVar) && e != null) {
            throw new IllegalArgumentException();
        }
        switch (i) {
            case 0:
                pred1 = (PointVar) e;
                break;
        }
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 1
                || !(preds[0] instanceof PointVar) && preds[0] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
    }

    protected void setTarget(DmEntity target) {
        if (!(target instanceof MTrace) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (MTrace) target;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
