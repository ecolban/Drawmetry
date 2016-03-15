/*
 * FAngleConstraint.java
 *
 * Created on February, 2006
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
public class FAngleConstraint extends Constraint {

    private PointVar pred1, pred2, pred3;
    private NumVar target;
    private double alpha;
    private static final double TWO_PI = Math.PI * 2.0;

    /** Creates a new instance of AngleConstraint */
    public FAngleConstraint() {
    }

    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{
                    DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR
                };
    }

    public DmEntity.Type getTargetType() {
        return DmEntity.Type.NUM_VAR;
    }

    public DmEntity[] getPreds() {
        return new DmEntity[]{
                    pred1, pred2, pred3
                };
    }

    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 2
                || !(e instanceof PointVar) && e != null) {
            throw new IllegalArgumentException("Problems adding an angle constraint on " + target);
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
            default:
                assert false;
        }
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 3
                || !(preds[0] instanceof PointVar) && preds[0] != null
                || !(preds[1] instanceof PointVar) && preds[1] != null
                || !(preds[2] instanceof PointVar) && preds[2] != null) {
            throw new IllegalArgumentException("Problems adding an angle constraint on " + target);
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
        pred3 = (PointVar) preds[2];
    }

    protected DmEntity getTarget() {
        return target;
    }

    protected void setTarget(DmEntity target) {
        if (!(target instanceof NumVar) && target != null) {
            throw new IllegalArgumentException("Problems adding an angle constraint on " + target);
        }
        this.target = (NumVar) target;
        if (target != null) {
            this.target.precedence = 4;
        }
    }

    @Override
    public String toString() {
        return "angle(" + pred1.getEntityID() + "," + pred2.getEntityID() + "," + pred3.getEntityID() + ")";
    }

    @Override
    public int degree() {
        return 1;
    }

    public void fire() {
        if (pred1 != null && pred2 != null && pred3 != null) {
            alpha = Math.atan2(pred1.y - pred2.y, pred1.x - pred2.x)
                    - Math.atan2(pred3.y - pred2.y, pred3.x - pred2.x);
            target.nValue += Math.IEEEremainder(alpha - target.nValue, TWO_PI);
            // new target.nValue is as close as it can get to old target.nValue,
            // yet equivalent to alpha modulo TWO_PI.
        }
    }
}
