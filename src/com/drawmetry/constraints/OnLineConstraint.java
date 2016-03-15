/*
 * OnConstraint.java
 *
 * Created on July 1, 2004, 12:07 AM
 */

package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.LineVar;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik Colban
 */
public class OnLineConstraint extends Constraint {
    
    private LineVar pred1;
    private PointVar target;
    
    /** Creates a new instance of OnConstraint */
    public OnLineConstraint() {
    }
    
    public void fire() {
        double normSquare = pred1.a * pred1.a + pred1.b * pred1.b;
        if (normSquare > 0){
            double lambda = (pred1.a * target.x + pred1.b * target.y + pred1.c) / normSquare;
            target.x = target.x - lambda * pred1.a;
            target.y = target.y - lambda * pred1.b;}
    }
    
    public String toString() {
        if (pred1 != null && pred1.getConstraint() != null) {
            return pred1.getConstraint().toString();
        } else {
            return "OnlineConstraint";
        }
    }
    
    @Override
    public int degree() {
        return 1;
    }
    
    public DmEntity[] getPreds() {
        DmEntity [] preds = { pred1 };
        return preds;
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.LINE_VAR  };
    }
    
    public DmEntity getTarget() {
        return target;
    }
    
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.POINT_VAR;
    }
    
   public void setPreds(int i, DmEntity e) {
        if (i != 0 || !(e instanceof LineVar) && e != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (LineVar) e;
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 1 ||
                !(preds[0] instanceof LineVar) && preds[0] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (LineVar) preds[0];
    }

    public void setTarget(DmEntity target) {
        if (!(target instanceof PointVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (PointVar) target;
    }
    
    
    
}
