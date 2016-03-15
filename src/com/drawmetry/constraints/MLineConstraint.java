/*
 * ShapeConstraint.java
 *
 * Created on October 22, 2004, 10:24 AM
 */

package com.drawmetry.constraints;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.MLine;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik
 */
public class MLineConstraint extends Constraint{
    
    private PointVar pred1;
    private PointVar pred2;
    private MLine target;
    
    public MLineConstraint() {
        
    }
    
    /** Creates a new instance of ShapeConstraint */
    
    
    
    public DmEntity[] getPreds() {
        DmEntity [] preds = { pred1, pred2 };
        return preds;
    }
    
    
    public void fire() {
        target.startx = pred1.x;
        target.starty = pred1.y;
        target.endx = pred2.x;
        target.endy = pred2.y;
    }
    
    @Override
    public int degree() {
        return 0;
    }
    
    public DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[] {DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR };
    }
    
    public com.drawmetry.DmEntity getTarget() {
        return target;
    }
    
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.LINE;
    }
    
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 1 ||
                i == 0 && !(e instanceof PointVar) && e != null ||
                i == 1 && !(e instanceof PointVar) && e != null){
            throw new IllegalArgumentException();
        }
        switch(i) {
            case 0 :
                pred1 = (PointVar) e;
                break;
            case 1:
                pred2 = (PointVar) e;
                break;
        }
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 2 ||
                !(preds[0] instanceof PointVar) && preds[0] != null ||
                !(preds[1] instanceof PointVar) && preds[1] != null){
            throw new IllegalArgumentException();
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
    }

    protected void setTarget(DmEntity target) {
        if (!(target instanceof MLine) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (MLine) target;
    }

    @Override
    public String toString() {
        return String.format("endpoints(%s, %s)", pred1.getEntityID(), pred2.getEntityID());
    }

}
