/*
 * InterLLConstraint.java
 *
 * Created on August 15, 2004, 9:55 AM
 */

package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.LineVar;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik
 */
public class InterLLConstraint extends Constraint{
    private LineVar pred1, pred2;
    private PointVar target;
    
    /** Creates a new instance of InterLLConstraint */
    public InterLLConstraint() {}
    
    
    @Override
    public int degree() {
        return 2;
    }
    
    public void fire() {
        double determinant = pred1.a * pred2.b - pred1.b * pred2.a;
        if (determinant!= 0){
            target.x = (pred1.b * pred2.c - pred1.c * pred2.b) / determinant;
            target.y = (pred1.c * pred2.a - pred1.a * pred2.c) / determinant;
        }
    }
    
    public DmEntity[] getPreds() {
        DmEntity [] preds = { pred1, pred2};
        return preds;
    }
    
    
    public String toString() {
        return pred1.getConstraint().toString() + " and " + 
                pred2.getConstraint().toString();
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.LINE_VAR, DmEntity.Type.LINE_VAR};
    }
    
    public com.drawmetry.DmEntity getTarget() {
        return target;
    }
    
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.POINT_VAR;
    }
    
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 1 ||
                i == 0 && !(e instanceof LineVar) && e != null ||
                i == 1 && !(e instanceof LineVar) && e != null) {
            throw new IllegalArgumentException();
        }
        switch(i) {
            case 0 :
                pred1 = (LineVar) e;
                break;
            case 1:
                pred2 = (LineVar) e;
                break;
        }
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 2 ||
                !(preds[0] instanceof LineVar) && preds[0] != null ||
                !(preds[1] instanceof LineVar) && preds[1] != null ) {
            throw new IllegalArgumentException();
        }
        pred1 = (LineVar) preds[0];
        pred2 = (LineVar) preds[1];
    }

    protected void setTarget(DmEntity target) {
        if (!(target instanceof PointVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (PointVar) target;
    }

}
