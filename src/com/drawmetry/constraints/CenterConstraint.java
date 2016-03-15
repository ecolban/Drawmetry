/*
 * EquidistantConstraint.java
 *
 * Created on February 19, 2004, 12:07 AM
 */
package com.drawmetry.constraints;

import com.drawmetry.*;

/**
 *
 * @author  Erik Colban
 */
public class CenterConstraint extends Constraint {

    private PointVar pred1,  pred2;
    private PointVar target;

    /** Creates a new instance of EquidistantConstraint */
    public CenterConstraint() {
    }

    @Override
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR};
    }

    @Override
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.POINT_VAR;
    }

    @Override
    public DmEntity[] getPreds() {
        return new DmEntity[]{pred1, pred2};
    }

    @Override
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || 1 < i || !(e instanceof PointVar) && e != null) {
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
    public void setTarget(com.drawmetry.DmEntity target) {
        if (!(target instanceof PointVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (PointVar) target;
    }

    @Override
    protected DmEntity getTarget() {
        return target;
    }

    @Override
    public void fire() {
        if (pred1 != null && pred2 != null) {
            target.x = (pred1.x + pred2.x) / 2.0;
            target.y = (pred1.y + pred2.y) / 2.0;
        }
    }

    @Override
    public String toString() {
        return "center(" + pred1.getEntityID() + "," + pred2.getEntityID() + ")";
    }

    @Override
    public int degree() {
        return 2;
    }
}
