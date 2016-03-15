/*
 * AngleConstraint.java
 *
 * Created on November 5, 2004, 12:07 AM
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
public class AngleConstraint extends Constraint {

//    private AngleConstraint constraint = this;
    private PointVar pred1, pred2;
    private NumVar pred3;
    private double a_h, b_h;
    private LineVar target;
    private double cosAlpha, sinAlpha;

    /** Creates a new instance of AngleConstraint */
    public AngleConstraint() {
    }

    public com.drawmetry.DmEntity.Type[] getPredTypes() {
        return new com.drawmetry.DmEntity.Type[]{
                    DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR, DmEntity.Type.NUM_VAR
                };
    }

    @Override
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.LINE_VAR;
    }

    @Override
    public DmEntity[] getPreds() {
        DmEntity[] preds = {
            pred1, pred2, pred3
        };
        return preds;
    }

    @Override
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || 2 < i
                || i < 2 && !(e instanceof PointVar) && e != null
                || i == 2 && !(e instanceof NumVar) && e != null) {
            throw new IllegalArgumentException("Problems inserting angle constraint on " + target);
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
        if (preds.length != 3
                || !(preds[0] instanceof PointVar) && preds[0] != null
                || !(preds[1] instanceof PointVar) && preds[1] != null
                || !(preds[2] instanceof NumVar) && preds[2] != null) {
            throw new IllegalArgumentException("Problems inserting angle constraint on " + target);
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
        pred3 = (NumVar) preds[2];
    }

    @Override
    public void setTarget(com.drawmetry.DmEntity target) {
        if (!(target instanceof LineVar) && target != null) {
            throw new IllegalArgumentException("Problems inserting angle constraint on " + target);
        }
        this.target = (LineVar) target;
    }

    @Override
    protected DmEntity getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "angle(" + pred1.getEntityID() + "," + pred2.getEntityID() + "," + pred3.toString() + ")";
    }

    @Override
    public int degree() {
        return 1;
    }

    @Override
    public void fire() {
        if (pred1 != null && pred2 != null && pred3 != null) {
            a_h = pred2.y - pred1.y;
            b_h = -pred2.x + pred1.x;
            cosAlpha = Math.cos(pred3.nValue);
            sinAlpha = -Math.sin(pred3.nValue);

            target.a = a_h * cosAlpha - b_h * sinAlpha;
            target.b = a_h * sinAlpha + b_h * cosAlpha;
            target.c = -target.a * pred2.x - target.b * pred2.y;
        }
    }
}
