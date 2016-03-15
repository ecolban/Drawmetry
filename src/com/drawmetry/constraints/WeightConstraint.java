/*
 * WeightConstraint.java
 *
 * Created on November, 2005.
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
public class WeightConstraint extends Constraint {
    
    private PointVar pred1;
    private NumVar pred2;
    private PointVar pred3;
    private NumVar pred4;
    private PointVar target;
    
    /** Creates a new instance of DistanceConstraint */
    public WeightConstraint() {
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.POINT_VAR, DmEntity.Type.NUM_VAR,
                DmEntity.Type.POINT_VAR, DmEntity.Type.NUM_VAR};
    }
    
    public DmEntity getTarget() {
        return target;
    }
    
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.POINT_VAR;
    }
    
    @Override
    public void setPreds(int i, DmEntity e) {
        if (i <0 || i > 3 ||
                i == 0 && !(e instanceof PointVar) && e != null ||
                i == 1 && !(e instanceof NumVar) && e != null ||
                i == 2 && !(e instanceof PointVar) && e != null ||
                i == 3 && !(e instanceof NumVar) && e != null) {
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
            case 3:
                pred4 = (NumVar) e;
                break;
        }
    }

    @Override
    public void setPreds(DmEntity[] preds) {
        if (preds.length != 4 ||
                !(preds[0] instanceof PointVar) && preds[0] != null ||
                !(preds[1] instanceof NumVar) && preds[1] != null ||
                !(preds[2] instanceof PointVar) && preds[2] != null ||
                !(preds[3] instanceof NumVar) && preds[3] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
        pred2 = (NumVar) preds[1];
        pred3 = (PointVar) preds[2];
        pred4 = (NumVar) preds[3];
    }

    @Override
    protected void setTarget(DmEntity target) {
        if (!(target instanceof PointVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (PointVar) target;
    }

    public DmEntity[] getPreds() {
        return new DmEntity [] { pred1, pred2, pred3, pred4 };
    }
    
    public String toString() {
        return "weight(" + pred1.getEntityID() + ", " + pred2.toString() + ", "
                + pred3.getEntityID() + ", " + pred4.toString() + ")";
    }
    
    @Override
    public int degree() {
        return 2;
    }
    
    
    public void fire() {
        if (pred1 != null && pred2 != null && pred3 != null && pred4 != null) {
            double sumOfWeights = pred2.nValue + pred4.nValue;
            if (sumOfWeights != 0){
                target.x = ( pred1.x * pred2.nValue + pred3.x * pred4.nValue ) / 
                        sumOfWeights;
                target.y = ( pred1.y * pred2.nValue + pred3.y * pred4.nValue ) / 
                        sumOfWeights;
            }
        }
    }
    
    
}
