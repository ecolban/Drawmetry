/*
 * CellRefNumeric.java
 *
 * 
 */
package com.drawmetry.constraints;

import com.drawmetry.*;

/**
 *
 * @author  Erik Colban
 */
public class CellRefNumeric extends Constraint {

    private CellContent pred1;
    private NumVar target;

    /** Creates a new instance of EquidistantConstraint */
    public CellRefNumeric() {
    }

    @Override
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{DmEntity.Type.NUM_VAR};
    }

    @Override
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.NUM_VAR;
    }

    @Override
    public DmEntity[] getPreds() {
        return new DmEntity[]{pred1};
    }

    @Override
    public void setPreds(int i, DmEntity e) {
        if (i != 0 || e != null && !(e instanceof CellContent)) {
            throw new IllegalArgumentException();
        }
        pred1 = (CellContent) e;
    }

    @Override
    public void setPreds(DmEntity[] preds) {
        if (preds.length != 1
                || !(preds[0] instanceof PointVar) && preds[0] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (CellContent) preds[0];
    }

    @Override
    protected void setTarget(DmEntity target) {
        if (!(target instanceof NumVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (NumVar) target;
        if (target != null) {
            this.target.precedence = 0;
        }
    }

    @Override
    protected DmEntity getTarget() {
        return target;
    }

    @Override
    public void fire() {
        if (pred1 != null) {
            target.nValue = pred1.getNValue();
        }
    }

    @Override
    public String toString() {
        return pred1.getEntityID();
    }

    @Override
    public int degree() {
        return 1;
    }
}
