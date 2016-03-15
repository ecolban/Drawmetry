/*
 * PlusConstraint.java
 *
 * Created on July 3, 2004, 12:07 AM
 */

package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.NumVar;

/**
 *
 * @author  Erik Colban
 */
public class PlusConstraint extends Constraint {
    
    private NumVar pred1, pred2;
    private NumVar target;
    
    /** Creates a new instance of MinusConstraint */
    public PlusConstraint() {
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.NUM_VAR, DmEntity.Type.NUM_VAR};
    }
    
    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.NUM_VAR;
    }
    
    public DmEntity[] getPreds() {
        return new DmEntity [] { pred1, pred2 };
    }
    
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 1 || !(e instanceof NumVar) && e != null) {
            throw new IllegalArgumentException();
        }
        switch (i) {
            case 0:
                pred1 = (NumVar) e;
                break;
            case 1:
                pred2 = (NumVar) e;
                break;
        }
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 2 ||
                !(preds[0] instanceof NumVar) && preds[0] != null ||
                !(preds[1] instanceof NumVar) && preds[1] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (NumVar) preds[0];
        pred2 = (NumVar) preds[1];
    }

    protected void setTarget(DmEntity target) {
        if (!(target instanceof NumVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (NumVar) target;
        if (target != null) {
            this.target.precedence = 0;
        }
    }
    protected DmEntity getTarget() {
        return target;
    }
    
    public void fire() {
        if (pred1 != null && pred2 != null) {
            target.nValue = pred1.nValue + pred2.nValue;
        }
    }
    
    public String toString() {
//        String p1 = pred1.toString();
//        String p2 = pred2.toString();
//        if (pred1 instanceof CellContent) {
//            CellContent cc = (CellContent) pred1;
//            if (cc.getParentCell() != null) {
//                p1 = cc.getParentCell().getEntityID();
//            } 
//        } 
//        if (pred2 instanceof CellContent) {
//            CellContent cc = (CellContent) pred2;
//            if (cc.getParentCell() != null) {
//                p2 = cc.getParentCell().getEntityID();
//            }
//        }
//        return p1 + " + " + p2;
        return pred1.toString() + " + " + pred2.toString();
    }
    
@Override
public int degree() {
        return 1;
    }
    
}
