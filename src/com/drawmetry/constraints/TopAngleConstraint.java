/*
 * TopAngleConstraint.java
 *
 * Created on November 3, 2004, 12:07 AM
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
public class TopAngleConstraint extends Constraint {
    
    private PointVar pred1, pred3;
    private NumVar pred2;
    private CircleVar target;
//    static double PI = Math.PI;
    static double PIH = Math.PI * 0.5;
    
    /** Creates a new instance of EquidistantConstraint */
    public TopAngleConstraint() {
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]
        {DmEntity.Type.POINT_VAR, DmEntity.Type.NUM_VAR, DmEntity.Type.POINT_VAR};
    }
    
    public DmEntity getTarget() {
        return target;
    }
    
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.CIRCLE_VAR;
    }
    
    @Override
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || 2 < i ||
                i == 0 && !(e instanceof PointVar) && e != null ||
                i == 1 && !(e instanceof NumVar) && e != null ||
                i == 2 && !(e instanceof PointVar) && e != null){
            throw new IllegalArgumentException();
        }
        switch (i) {
            case 0:
                pred1 = (PointVar) e;
                break;
            case 1:
                pred2 = (NumVar) e;
                break;
            case 2:
                pred3 = (PointVar) e;
                break;
        }
    }

    @Override
    public void setPreds(DmEntity[] preds) {
        if (preds.length != 3 ||
                !(preds[0] instanceof PointVar) && preds[0] != null ||
                !(preds[1] instanceof PointVar) && preds[1] != null ||
                !(preds[2] instanceof NumVar) && preds[2] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
        pred2 = (NumVar) preds[1];
        pred3 = (PointVar) preds[2];
    }

    @Override
    public void setTarget(com.drawmetry.DmEntity target) {
        if (!(target instanceof CircleVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (CircleVar) target;
    }

    public DmEntity[] getPreds() {
        return new DmEntity [] { pred1, pred2, pred3 };
    }
    
    @Override
    public String toString() {
        return "angle(" + pred1.getEntityID() + "," + pred2.toString() + ","
                + pred3.getEntityID() + ")";
    }
    
    @Override
    public int degree() {
        return 1;
    }
    
    
    public void fire() {
        if (pred1 != null && pred2 != null && pred3 != null) {
            double a = (pred1.x - pred3.x) * 0.5;
            double b = (pred1.y - pred3.y) * 0.5;
            double t = -Math.tan(pred2.nValue + PIH);
            target.x_0 = a - b*t + pred3.x;
            target.y_0 = b + a*t + pred3.y;
            target.radius = Math.sqrt((a*a + b*b)*(1 + t*t));
        }
    }
}
