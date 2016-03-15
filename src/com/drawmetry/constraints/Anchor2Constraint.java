/*
 * Anchor2Constraint.java
 *
 * Created on May 28, 2006
 */
package com.drawmetry.constraints;

import java.awt.geom.AffineTransform;

import com.drawmetry.AnchorVar;
import com.drawmetry.DmEntity;
import com.drawmetry.PointVar;

/**
 * An implementation of an {@link AnchorConstraint} that takes 2 predecessors.
 * It identifies the translation, rotation, and scaling that would need to be
 * applied to the predecessors to bring them from their positions at the time
 * the constraint was added to their current positions and updates the target's
 * affine transformation accordingly.
 *
 * @author  Erik Colban
 */
public class Anchor2Constraint extends AnchorConstraint {

    private PointVar pred1, pred2;
    private AnchorVar target;

    public com.drawmetry.DmEntity.Type[] getPredTypes() {
        return new com.drawmetry.DmEntity.Type[]{DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR};
    }

    @Override
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.ANCHOR_VAR;
    }

    @Override
    public DmEntity[] getPreds() {
        DmEntity[] preds = {pred1, pred2};
        return preds;
    }

    @Override
    protected void setPreds(DmEntity[] preds) {
        if (preds.length != 2
                || !(preds[0] instanceof PointVar) && preds[0] != null
                || !(preds[1] instanceof PointVar) && preds[1] != null) {
            throw new IllegalArgumentException("Problems inserting anchor constraint on " + target);
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
    }

    @Override
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i > 1 || !(e instanceof PointVar) && e != null) {
            throw new IllegalArgumentException("Problems inserting anchor constraint on " + target);
        }
        switch (i) {
            case 0:
                pred1 = (PointVar) e;
                break;
            case 1:
                pred2 = (PointVar) e;
                break;
        }
    }

    @Override
    protected DmEntity getTarget() {
        return target;
    }

    @Override
    public void setTarget(com.drawmetry.DmEntity target) {
        if (!(target instanceof AnchorVar) && target != null) {
            throw new IllegalArgumentException("Problems inserting anchor constraint on " + target);
        }
        this.target = (AnchorVar) target;
    }

    @Override
    public String toString() {
        return "anchor(" + pred1.getEntityID() + ", "
                + pred2.getEntityID() + ")";
    }

    @Override
    public int degree() {
        return 2;
    }

    /**
     * Updates the target AnchorVar's ctf to
    <pre>
     *
     *      [pred2.x - pred1.x  pred1.y - pred2.y  pred1.x ]
     * A =  |pred2.y - pred1.y  pred2.x - pred1.x  pred1.y |
     *      [        0                  0            1     ]
     * </pre>
     * This matrix is a combination of a translation, scaling, and rotation
     * matrix.
     */
    @Override
    public void fire() {
        if (pred1 != null && pred2 != null && target != null) {
            double a = pred2.x - pred1.x;
            double b = pred2.y - pred1.y;
            target.setCtf(a, b, -b, a, pred1.x, pred1.y);
        }
    }

    /**
     * @return an <code>AffineTransform</code> A, where
     * <pre>
     *
     *      [pred2.x - pred1.x  pred1.y - pred2.y  pred1.x ]
     * A =  |pred2.y - pred1.y  pred2.x - pred1.x  pred1.y |
     *      [        0                  0            1     ]
     * </pre>
     */
    @Override
    public AffineTransform getInitTransform() throws IllegalStateException {
        if (pred1 == null || pred2 == null) {
            throw new IllegalStateException(
                    "Non-null predecessors must be have been added before calling this method.");
        }
        double a = pred2.x - pred1.x;
        double b = pred2.y - pred1.y;
        return new AffineTransform(a, b, -b, a, pred1.x, pred1.y);
    }
}
