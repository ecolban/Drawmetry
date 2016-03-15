/*
 * Distance2Constraint.java
 *
 */
package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.LineVar;
import com.drawmetry.NumVar;
import com.drawmetry.PointVar;
import com.drawmetry.DmEntity.Type;

/**
 *
 * @author  Erik Colban
 */
public class Distance2Constraint extends Constraint {

    private PointVar pred1, pred2;
    private NumVar pred3;
    private LineVar target;
    private double a, b;

    /** Creates a new instance of Distance2Constraint */
    public Distance2Constraint() {
    }

    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{Type.POINT_VAR, Type.POINT_VAR, Type.NUM_VAR};
    }

    public DmEntity getTarget() {
        return target;
    }

    public DmEntity.Type getTargetType() {
        return DmEntity.Type.LINE_VAR;
    }

    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 2 ||
                (i == 0 || i == 1) && !(e instanceof PointVar) && e != null ||
                i == 2 && !(e instanceof NumVar) && e != null) {
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
                pred3 = (NumVar) e;
                break;
            default:
        }
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 3 ||
                !(preds[0] instanceof PointVar) && preds[0] != null ||
                !(preds[1] instanceof PointVar) && preds[1] != null ||
                !(preds[2] instanceof NumVar) && preds[2] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
        pred3 = (NumVar) preds[2];
    }

    public void setTarget(DmEntity target) {
        if (!(target instanceof LineVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (LineVar) target;
    }

    public DmEntity[] getPreds() {
        return new DmEntity[]{pred1, pred2, pred3};
    }

    @Override
    public String toString() {
        return "distance(" + pred1.getEntityID() +
                "," + pred2.getEntityID() +
                "," + pred3.toString() + ")";
    }

    @Override
    public int degree() {
        return 1;
    }

    public void fire() {
        if (pred1 != null && pred2 != null) {
            a = pred1.y - pred2.y;
            b = pred2.x - pred1.x;
            target.a = a;
            target.b = b;
            target.c = -a * pred1.x - b * pred1.y +
                    pred3.nValue * Math.sqrt(a * a + b * b);
        }
    }
}
