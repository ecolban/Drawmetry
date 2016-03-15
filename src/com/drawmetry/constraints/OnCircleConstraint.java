/*
 * OnConstraint.java
 *
 * Created on July 1, 2004, 12:07 AM
 */
package com.drawmetry.constraints;

import com.drawmetry.CircleVar;
import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik Colban
 */
public class OnCircleConstraint extends Constraint {

    private CircleVar pred1;
    private PointVar target;

    /** Creates a new instance of OnConstraint */
    public OnCircleConstraint() {
    }

    public void fire() {
        double a = target.x - pred1.x_0;
        double b = target.y - pred1.y_0;
        double norm = Math.sqrt(a * a + b * b);
        if (norm > 0) {
            double lambda = pred1.radius / norm;
            target.x = pred1.x_0 + lambda * a;
            target.y = pred1.y_0 + lambda * b;
        }
    }

    public String toString() {
        return pred1.getConstraint().toString();
    }

    @Override
    public int degree() {
        return 1;
    }

    public DmEntity[] getPreds() {
        DmEntity[] preds = {pred1};
        return preds;
    }

    public com.drawmetry.DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{DmEntity.Type.CIRCLE_VAR};
    }

    public com.drawmetry.DmEntity getTarget() {
        return target;
    }

    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.POINT_VAR;
    }

    public void setPreds(int i, DmEntity e) {
        if (i != 0 || !(e instanceof CircleVar) && e != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (CircleVar) e;
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 1 ||
                !(preds[0] instanceof CircleVar) && preds[0] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (CircleVar) preds[0];
    }

    public void setTarget(DmEntity target) {
        if (!(target instanceof PointVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (PointVar) target;
    }
}
