/*
 * PerpendicularConstraint.java
 *
 */
package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.LineVar;
import com.drawmetry.PointVar;
import com.drawmetry.DmEntity.Type;

/**
 *
 * @author  Erik Colban
 */
public class PerpendicularConstraint extends Constraint {

    private PointVar pred1,  pred2,  pred3;
    private LineVar target;

    /** Creates a new instance of EquidistantConstraint */
    public PerpendicularConstraint() {
    }

    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{Type.POINT_VAR, Type.POINT_VAR, Type.POINT_VAR};
    }

    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.LINE_VAR;
    }

    public DmEntity[] getPreds() {
        return new DmEntity[]{pred1, pred2, pred3};
    }

    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 2 ||
                e != null && !(e instanceof PointVar)) {
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
        if (preds.length != 3 ||
                preds[0] != null && !(preds[0] instanceof PointVar) ||
                preds[1] != null && !(preds[1] instanceof PointVar) ||
                preds[2] != null && !(preds[2] instanceof PointVar)) {
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
        pred3 = (PointVar) preds[2];
    }

    protected void setTarget(DmEntity target) {
        if (target != null && !(target instanceof LineVar)) {
            throw new IllegalArgumentException();
        }
        this.target = (LineVar) target;
    }

    protected DmEntity getTarget() {
        return target;
    }

    public void fire() {
        if (pred1 != null && pred2 != null) {
            LineVar t = (LineVar) target;
            double a = pred2.x - pred3.x;
            double b = pred2.y - pred3.y;
            t.a = a;
            t.b = b;
            t.c = -a * pred1.x - b * pred1.y;
        }
    }

    public String toString() {
        return "perpendicular(" + pred1.getEntityID() +
                "," + pred2.getEntityID() +
                "," + pred3.getEntityID() + ")";
    }

    @Override
    public int degree() {
        return 1;
    }
}
