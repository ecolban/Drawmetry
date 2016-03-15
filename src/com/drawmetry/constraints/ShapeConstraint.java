package com.drawmetry.constraints;

import java.awt.geom.AffineTransform;
import java.util.Locale;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.PathEntity;
import com.drawmetry.ShapeVar;

/**
 *
 *
 * @author  Erik Colban
 */
public class ShapeConstraint extends Constraint {

    private PathEntity pred1;
    private ShapeVar target;

    /** Creates a new instance of Anchor1Constraint */
    public ShapeConstraint() {
    }

    @Override
    public com.drawmetry.DmEntity.Type[] getPredTypes() {
        return new com.drawmetry.DmEntity.Type[]{DmEntity.Type.PATH};
    }

    @Override
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.SHAPE;
    }

    @Override
    public DmEntity[] getPreds() {
        DmEntity[] preds = {pred1};
        return preds;
    }

    @Override
    protected void setPreds(DmEntity[] preds) {
        if (preds.length != 1 || !(preds[0] instanceof PathEntity) && preds[0] != null) {
            throw new IllegalArgumentException("Problems inserting anchor constraint on " + target);
        }
        pred1 = (PathEntity) preds[0];
    }

    @Override
    public void setPreds(int i, DmEntity e) {
        if (i != 0 || !(e instanceof PathEntity) && e != null) {
            throw new IllegalArgumentException("Problems inserting anchor constraint on " + target);
        }
        pred1 = (PathEntity) e;
    }

    @Override
    public DmEntity getTarget() {
        return target;
    }

    @Override
    public void setTarget(com.drawmetry.DmEntity target) {
        if (!(target instanceof ShapeVar) && target != null) {
            throw new IllegalArgumentException("Problems inserting anchor constraint on " + target);
        }
        this.target = (ShapeVar) target;
    }

    @Override
    public String toString() {
        return "clip(" + pred1.getEntityID() + ")";
    }

    @Override
    public int degree() {
        return 2;
    }

    @Override
    public void fire() {
        AffineTransform anchorTransform = new AffineTransform();
        pred1.getTransform(anchorTransform);
        target.setPath(pred1.getPath(anchorTransform));
    }

}
