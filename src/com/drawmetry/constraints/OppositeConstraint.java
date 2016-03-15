package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.NumVar;

/**
 *
 * @author  Erik Colban
 */
public class OppositeConstraint extends Constraint {
    
    private NumVar pred1;
    private NumVar target;
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] { DmEntity.Type.NUM_VAR };
    }
    
    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.NUM_VAR;
    }
    
    public DmEntity[] getPreds() {
        return new DmEntity [] { pred1 };
    }
    
    public void setPreds(int i, DmEntity e) {
        if (i != 0 || !(e instanceof NumVar) && e != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (NumVar) e;
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 1 || !(preds[0] instanceof NumVar) && preds[0] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (NumVar) preds[0];
    }

    protected void setTarget(DmEntity target) {
        if (!(target instanceof NumVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (NumVar) target;
        if (target != null) {
            this.target.precedence = 1;
        }
    }

    protected DmEntity getTarget() {
        return target;
    }
    
    public void fire() {
        if (pred1 != null) {
            target.nValue = - pred1.nValue;
        }
    }
    
    public String toString() {
        String a;
        a = pred1.toString();
        if (pred1.precedence < 1) {a = "(" + a + ")"; }
        return " -" + a;
    }
    
    @Override
    public int degree() {
        return 1;
    }
    
}
