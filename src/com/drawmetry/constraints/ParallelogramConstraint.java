/*
 * ParallelogramConstraint.java
 *
 */
package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.PointVar;
import com.drawmetry.DmEntity.Type;

/**
 *
 * @author  default
 */
public class ParallelogramConstraint extends Constraint {

    private PointVar pred1, pred2, pred3;
    private PointVar target;

    /** Creates a new instance of EquidistantConstraint */
    public ParallelogramConstraint() {
    }

    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{Type.POINT_VAR, Type.POINT_VAR, Type.POINT_VAR};
    }

    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.POINT_VAR;
    }

    public DmEntity[] getPreds() {
        return new DmEntity[]{pred1, pred2, pred3};
    }

    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 2
                || e != null && !(e instanceof PointVar)) {
            throw new IllegalArgumentException();
        }
        switch (i) {
            case 0:
                pred1 = (PointVar) e;
                break;
            case 1:
                pred2 = (PointVar) e;
                break;
            case 2:
                pred3 = (PointVar) e;
                break;
        }

    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 3
                || preds[0] != null && !(preds[0] instanceof PointVar)
                || preds[1] != null && !(preds[1] instanceof PointVar)
                || preds[2] != null && !(preds[2] instanceof PointVar)) {
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
        pred3 = (PointVar) preds[2];
    }

    protected void setTarget(DmEntity target) {
        if (target != null && !(target instanceof PointVar)) {
            throw new IllegalArgumentException();
        }
        this.target = (PointVar) target;
    }

    protected DmEntity getTarget() {
        return target;
    }

    public void fire() {
        if (pred1 != null && pred2 != null && pred3 != null) {
            target.x = pred2.x + pred3.x - pred1.x;
            target.y = pred2.y + pred3.y - pred1.y;
        }
    }

    public String toString() {
        return "parallelogram(" + pred1.getEntityID()
                + "," + pred2.getEntityID()
                + "," + pred3.getEntityID() + ")";
    }

    @Override
    public int degree() {
        return 2;
    }
}
