/*
 * AnchorConstraint.java
 *
 * Created on May 28, 2006, 7:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.drawmetry.constraints;

import java.awt.geom.AffineTransform;

import com.drawmetry.AnchorVar;
import com.drawmetry.Constraint;

/**
 * An AnchorConstraint is a constraint that can be added to an {@link AnchorVar}.
 * It identifies the affine transformation that would need to be applied to the 
 * predecessors to bring them from their positions at the time the constraint 
 * was added to their current positions and updates the target's affine
 * transformation accordingly.
 *
 * @see Anchor1Constraint
 * @see Anchor2Constraint
 * @see Anchor3Constraint
 *
 * @author Erik
 */
abstract public class AnchorConstraint extends Constraint {

    /**
     * This method should be called prior to adding this constraint to a target
     * AnchorVar. It returns an <code>AffineTransform</code> used to initialize
     * the target of this constraint. The predecessors of this constraint must
     * have been added before calling this method, as this method uses the
     * current values of the predecessors to compute this transformation.
     *
     * @return  an <code>AffineTransform</code>
     *
     * @throws IllegalStateException if any of the predecessors is null.
     */
    abstract public AffineTransform getInitTransform() throws IllegalStateException;
}
