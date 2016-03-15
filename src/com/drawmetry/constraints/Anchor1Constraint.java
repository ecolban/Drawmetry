/*
 * Anchor1Constraint.java
 *
 * Created on May 28, 2006
 */
package com.drawmetry.constraints;

import java.awt.geom.AffineTransform;

import com.drawmetry.AnchorVar;
import com.drawmetry.DmEntity;
import com.drawmetry.PointVar;

/**
 * An implementation of an {@link AnchorConstraint} that takes 1 predecessor.
 * It identifies the translation that would need to be applied to the
 * predecessor to bring it from its position at the time the constraint
 * was added to its current position and updates the target's affine
 * transformation accordingly.
 *
 * @author  Erik Colban
 */
public class Anchor1Constraint extends AnchorConstraint {

    private PointVar pred1;
    private AnchorVar target;

    @Override
    public com.drawmetry.DmEntity.Type[] getPredTypes() {
        return new com.drawmetry.DmEntity.Type[]{DmEntity.Type.POINT_VAR};
    }

    @Override
    public DmEntity.Type getTargetType() {
        return DmEntity.Type.ANCHOR_VAR;
    }

    @Override
    public DmEntity[] getPreds() {
        DmEntity[] preds = {pred1};
        return preds;
    }

    @Override
    protected void setPreds(DmEntity[] preds) {
        if (preds.length != 1 || !(preds[0] instanceof PointVar) && preds[0] != null) {
            throw new IllegalArgumentException("Problems inserting anchor constraint on " + target);
        }
        pred1 = (PointVar) preds[0];
    }

    @Override
    public void setPreds(int i, DmEntity e) {
        if (i != 0 || !(e instanceof PointVar) && e != null) {
            throw new IllegalArgumentException("Problems inserting anchor constraint on " + target);
        }
        pred1 = (PointVar) e;
    }

    @Override
    public DmEntity getTarget() {
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
        return "anchor(" + pred1.getEntityID() + ")";
    }

    @Override
    public int degree() {
        return 2;
    }
    /**
     * Updates the target AnchorVar's ctf to
     * <pre>
     *
     *      [  1   0   pred1.x ]
     * A =  |  0   1   pred1.y |
     *      [  0   0     1     ]
     * </pre>
     * This matrix is a translation matrix.
     */
    @Override
    public void fire() {
        if (pred1 != null && target != null) {
            target.setCtf(1.0, 0.0, 0.0, 1.0, pred1.x, pred1.y);
        }
    }

     /**
     * @return A, where
     * <pre>
     *
     *      [  1   0   pred1.x ]
     * A =  |  0   1   pred1.y |
     *      [  0   0     1     ]
     * </pre>
     */
    @Override
    public AffineTransform getInitTransform() throws IllegalStateException {
        if (pred1 == null) {
            throw new IllegalStateException(
                    "A non-null predecessor must be have been added before calling this method.");
        }
        return new AffineTransform(1.0, 0.0, 0.0, 1.0, pred1.x, pred1.y);
    }
}
