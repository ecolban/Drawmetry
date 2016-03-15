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
public class SlopeConstraint extends Constraint {
    
    private PointVar pred1;
    private NumVar pred2;
    private LineVar target;
    
    /** Creates a new instance of HDistanceConstraint */
    public SlopeConstraint() {
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.POINT_VAR, DmEntity.Type.NUM_VAR};
    }
    
    public DmEntity getTarget() {
        return target;
    }
    
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.LINE_VAR;
    }
    
    @Override
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || 1 < i ||
                i == 0 && !(e instanceof PointVar) && e != null ||
                i == 1 && !(e instanceof NumVar) && e != null ) {
            throw new IllegalArgumentException();
        }
        switch (i) {
            case 0:
                pred1 = (PointVar) e;
                break;
            case 1:
                pred2 = (NumVar) e;
                break;
        }
    }

    @Override
    public void setPreds(DmEntity[] preds) {
        if (preds.length != 2 ||
                !(preds[0] instanceof PointVar) && preds[0] != null ||
                !(preds[1] instanceof NumVar) && preds[1] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
        pred2 = (NumVar) preds[1];
    }

    @Override
    public void setTarget(com.drawmetry.DmEntity target) {
        if (!(target instanceof LineVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (LineVar) target;
    }
    
    public DmEntity[] getPreds() {
        return new DmEntity [] { pred1, pred2 };
    }
    
    @Override
    public String toString() {
        return "slope(" + pred1.getEntityID() + "," + pred2.toString() + ")";
    }
    
    @Override
    public int degree() {
        return 1;
    }
    
    
    public void fire() {
        if (pred1 != null && pred2 != null) {
            target.a = pred2.nValue;
            target.b = 1.0;
            target.c =  -pred1.x * pred2.nValue - pred1.y;
        }
    }
    
    
}
