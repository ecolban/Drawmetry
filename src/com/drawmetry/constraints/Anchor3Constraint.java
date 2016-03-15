/*
 * Anchor3Constraint.java
 *
 * Created on May 28, 2006
 */
package com.drawmetry.constraints;

import java.awt.geom.AffineTransform;

import com.drawmetry.AnchorVar;
import com.drawmetry.DmEntity;
import com.drawmetry.PointVar;

/**
 * An implementation of an {@link AnchorConstraint} that takes 3 predecessors.
 * It identifies the affine transformation that would need to be applied to the 
 * predecessors to bring them from their positions at the time the constraint 
 * was added to their current positions and updates the transformation of the
 * target {@link AnchorVar} accordingly.
 * In order to identify this transformation, the 3 predecessors must be
 * non-collinear at the time this constraint is added to the target. A client
 * should verify that the transformation returned by {@link #getInitTransform() }
 * is invertible, which is equivalent to the 3 predecessors being non-collinear,
 * before adding this constraint to the target.
 *
 * @author  Erik Colban
 */
public class Anchor3Constraint extends AnchorConstraint {

    private PointVar pred1, pred2, pred3;
    private AnchorVar target;

    @Override
    public com.drawmetry.DmEntity.Type[] getPredTypes() {
        return new com.drawmetry.DmEntity.Type[]{DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR, DmEntity.Type.POINT_VAR};
    }

    @Override
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.ANCHOR_VAR;
    }

    @Override
    public DmEntity[] getPreds() {
        DmEntity[] preds = {pred1, pred2, pred3};
        return preds;
    }

    @Override
    public void setPreds(int i, DmEntity e) {
        if (i < 0 || 2 < i || !(e instanceof PointVar) && e != null) {
            throw new IllegalArgumentException("Problems inserting anchor constraint on " + target);
        }
        switch (i) {
            case 0:
                pred1 = (PointVar) e;
                break;
            case 1:
                pred2 = (PointVar) e;
                break;
            case 2:
                pred3 = (PointVar) e;
                break;
        }
    }

    @Override
    public void setPreds(DmEntity[] preds) {
        if (preds.length != 3
                || !(preds[0] instanceof PointVar) && preds[0] != null
                || !(preds[1] instanceof PointVar) && preds[1] != null
                || !(preds[2] instanceof PointVar) && preds[2] != null) {
            throw new IllegalArgumentException("Problems inserting anchor constraint on " + target);
        }
        pred1 = (PointVar) preds[0];
        pred2 = (PointVar) preds[1];
        pred3 = (PointVar) preds[2];
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
                + pred2.getEntityID() + ", " + pred3.getEntityID() + ")";
    }

    @Override
    public int degree() {
        return 2;
    }

    /**
     * Updates the target AnchorVar's ctf to
     * <pre>
     *
     *      [pred2.x - pred1.x  pred3.x - pred1.x  pred1.x ]
     * A =  |pred2.y - pred1.y  pred3.y - pred1.y  pred1.y |
     *      [        0                  0            1     ]
     * </pre>
     */
    @Override
    public void fire() {
        if (pred1 != null && pred2 != null && pred3 != null && target != null) {
            target.setCtf(pred2.x - pred1.x, pred2.y - pred1.y,
                    pred3.x - pred1.x, pred3.y - pred1.y, pred1.x, pred1.y);
        }

    }

    /**
     * @return an <code>AffineTransform</code> A, where
     * <pre>
     *
     *      [pred2.x - pred1.x  pred3.x - pred1.x  pred1.x ]
     * A =  |pred2.y - pred1.y  pred3.y - pred1.y  pred1.y |
     *      [        0                  0            1     ]
     * </pre>
     */
    @Override
    public AffineTransform getInitTransform() throws IllegalStateException {

        if (pred1 == null || pred2 == null || pred3 == null) {
            throw new IllegalStateException(
                    "Non-null predecessors must be have been added before calling this method.");
        }

        return new AffineTransform(pred2.x - pred1.x, pred2.y - pred1.y,
                pred3.x - pred1.x, pred3.y - pred1.y, pred1.x, pred1.y);
    }
}
