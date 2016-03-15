/*
 * InterCCConstraint.java
 *
 * Created on August 15, 2004, 9:55 AM
 */
package com.drawmetry.constraints;

import com.drawmetry.CircleVar;
import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik
 */
public class InterCCConstraint extends Constraint {

    private CircleVar pred1, pred2;
    private PointVar target;

    /** Creates a new instance of InterLLConstraint */
    public InterCCConstraint() {
    }

    @Override
    public int degree() {
        return 2;
    }

    public void fire() {
        double a = 2.0 * (pred1.x_0 - pred2.x_0);
        double b = 2.0 * (pred1.y_0 - pred2.y_0);
        double r2Square = pred2.radius * pred2.radius;
        double normSquare = a * a + b * b;
        double d = pred1.radius * pred1.radius - r2Square - normSquare / 4.0;
        double discriminant = r2Square * normSquare - d * d;
        if (discriminant >= 0) {
            double sqrtDiscriminant = Math.sqrt(discriminant);
            if ((target.x - pred2.x_0) * b > (target.y - pred2.y_0) * a) {
                target.x = pred2.x_0 - (a * d - b * sqrtDiscriminant) / normSquare;
                target.y = pred2.y_0 - (b * d + a * sqrtDiscriminant) / normSquare;
            } else {
                target.x = pred2.x_0 - (a * d + b * sqrtDiscriminant) / normSquare;
                target.y = pred2.y_0 - (b * d - a * sqrtDiscriminant) / normSquare;
            }
        }
    }

    public DmEntity[] getPreds() {
        return new DmEntity[]{pred1, pred2};
    }

    public String toString() {
        return pred1.getConstraint().toString() + " and "
                + pred2.getConstraint().toString();
    }

    public com.drawmetry.DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{DmEntity.Type.CIRCLE_VAR, DmEntity.Type.CIRCLE_VAR};
    }

    public com.drawmetry.DmEntity getTarget() {
        return target;
    }

    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.POINT_VAR;
    }

    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 1
                || !(e instanceof CircleVar) && e != null) {
            throw new IllegalArgumentException();
        }
        switch (i) {
            case 0:
                pred1 = (CircleVar) e;
                break;
            case 1:
                pred2 = (CircleVar) e;
                break;
        }
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 2
                || !(preds[0] instanceof CircleVar) && preds[0] != null
                || !(preds[1] instanceof CircleVar) && preds[1] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (CircleVar) preds[0];
        pred2 = (CircleVar) preds[1];
    }

    protected void setTarget(DmEntity target) {
        if (!(target instanceof PointVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (PointVar) target;
    }
}
