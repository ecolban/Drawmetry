/*
 * Constraint.java
 *
 * Created on February 12, 2004, 1:36 PM
 */
package com.drawmetry.constraints;

import javax.swing.undo.UndoableEdit;

import com.drawmetry.Constraint;

/**
 * An interface to be implmented by Constraints that may have a variable number
 * of predecessors.
 */
public abstract class VarPredsConstraint extends Constraint{

    /**
     * A method to be called after modifying the list of predecessors to check
     * if this list contains an appropriate number of preds. 
     * @return true if the list is OK.
     */
    public abstract boolean isSustainable();

    public abstract UndoableEdit increasePreds(int pos);

    public abstract UndoableEdit decreasePreds(int pos);

    public abstract UndoableEdit trim();

}
