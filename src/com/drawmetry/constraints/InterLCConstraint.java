/*
 * InterLLConstraint.java
 *
 * Created on August 15, 2004, 9:55 AM
 */

package com.drawmetry.constraints;

import com.drawmetry.CircleVar;
import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.LineVar;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik
 */
public class InterLCConstraint extends Constraint{
    private LineVar pred1;
    private CircleVar pred2;
    private PointVar target;
    
    /** Creates a new instance of InterLLConstraint */
    public InterLCConstraint() {
    }
    
    @Override
    public int degree() {
        return 2;
    }
    
    public void fire() {
        double a = pred1.a;
        double b = pred1.b;
        double normSquare = a * a + b * b;
        double d = a * pred2.x_0 + b * pred2.y_0 + pred1.c;
        double discriminant = pred2.radius * pred2.radius * normSquare - d * d;
        if (discriminant >= 0){
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
        return new DmEntity [] { pred1, pred2};
    }
    
    
    @Override
    public String toString() {
        return pred1.getConstraint().toString() + " and " + 
                pred2.getConstraint().toString();
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] { DmEntity.Type.LINE_VAR, DmEntity.Type.CIRCLE_VAR    };
    }
    
    public DmEntity getTarget() {
        return target;
    }
    
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.POINT_VAR;
    }
    
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 1 ||
                i == 0 && !(e instanceof LineVar) && e != null ||
                i == 1 && !(e instanceof CircleVar) && e != null) {
            throw new IllegalArgumentException();
        }
        switch(i) {
            case 0 :
                pred1 = (LineVar) e;
                break;
            case 1:
                pred2 = (CircleVar) e;
                break;
        }
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 2 ||
                !(preds[0] instanceof LineVar) && preds[0] != null ||
                !(preds[1] instanceof CircleVar) && preds[1] != null ) {
            throw new IllegalArgumentException();
        }
        pred1 = (LineVar) preds[0];
        pred2 = (CircleVar) preds[1];
    }

    protected void setTarget(DmEntity target) {
        if (!(target instanceof PointVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (PointVar) target;
    }

}
