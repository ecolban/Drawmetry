/*
 * EquidistantConstraint.java
 *
 * Created on July 3, 2004, 12:07 AM
 */
package com.drawmetry.constraints;

import com.drawmetry.CircleVar;
import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.NumVar;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik Colban
 */
public class DistanceConstraint extends Constraint {

    private PointVar pred1;
    private NumVar pred2;
    private CircleVar target;

    /** Creates a new instance of DistanceConstraint */
    public DistanceConstraint() {
    }

    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{DmEntity.Type.POINT_VAR, DmEntity.Type.NUM_VAR};
    }

    public DmEntity getTarget() {
        return target;
    }

    public DmEntity.Type getTargetType() {
        return DmEntity.Type.CIRCLE_VAR;
    }

    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 1 ||
                i == 0 && !(e instanceof PointVar) && e != null ||
                i == 1 && !(e instanceof NumVar) && e != null) {
            throw new IllegalArgumentException("Problems adding distance constraint on " + target);
        }
        switch (i) {
            case 0:
                pred1 = (PointVar) e;
                break;
            case 1:
                pred2 = (NumVar) e;
                break;
            default:
        }
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 2 ||
                !(preds[0] instanceof PointVar) && preds[0] != null ||
                !(preds[1] instanceof NumVar) && preds[1] != null) {
            throw new IllegalArgumentException("Problems adding distance constraint on " + target);
        }
        pred1 = (PointVar) preds[0];
        pred2 = (NumVar) preds[1];
    }

    public void setTarget(DmEntity target) {
        if (!(target instanceof CircleVar) && target != null) {
            throw new IllegalArgumentException("Problems adding distance constraint on " + target);
        }
        this.target = (CircleVar) target;
    }

    public DmEntity[] getPreds() {
        return new DmEntity[]{pred1, pred2};
    }

    public String toString() {
        return "distance(" + pred1.getEntityID() + "," + pred2.toString() + ")";
    }

    @Override
    public int degree() {
        return 1;
    }

    public void fire() {
        if (pred1 != null && pred2 != null && pred2.nValue >= 0) {
            target.x_0 = pred1.x;
            target.y_0 = pred1.y;
            target.radius = pred2.nValue;
        }
    }
}
