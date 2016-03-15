/*
 * Constraint.java
 *
 * Created on February 12, 2004, 1:36 PM
 */
package com.drawmetry;

/**All the methods and fields of this class are called or read by a
 * ConstraintGraph object.
 *
 */
public abstract class Constraint implements Cloneable {

    boolean checked = false;
    int numHits = 0;
   
    /** Creates a new instance of Constraint */
    public Constraint() {
    }

    /**
     * Returns a clone of the constraint.
     * Warning: This method may makes calls to setPreds(), which may mutate this object.
     * Override clone() in subtypes to protect against this kind of mutation.
     * @see constraints.NullConstraint#clone
     * @return clone
     * @throws java.lang.CloneNotSupportedException
     */
    @Override
    protected Constraint clone() throws CloneNotSupportedException {
        Constraint clone = (Constraint) super.clone();
        clone.setTarget(null);
        DmEntity[] predsClone = getPreds().clone();
        for (int i = 0; i < predsClone.length; i++) {
            predsClone[i] = null;
        }
        clone.setPreds(predsClone);
        return clone;
    }

    /**This method is called by a ConstraintGraph object.
     * getPreds returns an array containing the predecessors of the Constraint
     * object.
     */
    protected abstract DmEntity[] getPreds();

    protected abstract void setPreds(int i, DmEntity e);

    protected abstract void setPreds(DmEntity[] preds);

    protected abstract DmEntity getTarget();

    protected abstract void setTarget(DmEntity e);

    protected abstract DmEntity.Type[] getPredTypes();

    protected abstract DmEntity.Type getTargetType();

    @Override
    public abstract String toString();

    /**addPreds tests that the elements in preds are of the correct type and
     * number. If that is the case, references are added to each element in
     * preds and this object is added to the successors list of each element
     * in preds. It is the caller's responsibility to ensure prior to calling
     * this method that a cycle will not be introduced .
     *
     * It returns true if the elements in preds are of the correct type and
     * number, otherwise it returns false.
     */
    void addPreds(DmEntity[] preds) {
        assert (preds.length == getPredTypes().length);
        for (int i = 0; i < preds.length; i++) {
            addLink(preds[i], i);
        }
    }

    protected void addLink(DmEntity e, int i) {
        setPreds(i, e);
        if (e != null) {
            e.getSuccessors().add(this);
        }
    }

    void removeLink(int i) {
        assert (i <= getPredTypes().length);
        DmEntity[] preds = getPreds();
        DmEntity e = preds[i];
        setPreds(i, null);
        preds[i] = null;
        boolean remove = true;
        for (int j = 0; j < preds.length; j++) {
            if (preds[j] == e) {
                remove = false;
                break;
            }
        }
        if (remove) {
            e.getSuccessors().remove(this);
        }
    }

    /**This method is called by a ConstraintGraph object.
     * The method checks if the ConstrainedEntity ce is of the correct type and,
     * if it is, adds a reference to it and a reference from ce to this object.
     * It is the caller's responsibility to ensure prior to calling this method
     * that a cycle will not be introduced .
     * It returns true if ce is of the correct type, otherwise it returns false.
     */
    public void addTarget(DmEntity e) {
        assert (e != null && verifyTarget(e));
        this.setTarget(e);
        e.setConstraint(this);
    }

    /**This method is called by a ConstraintGraph object. It sets this object's
     * references to its presecessors to null and removes this object from the
     * predecessors' successors.
     */
    public void removePreds() {
        DmEntity e;
        for (int i = 0; i < getPreds().length; i++) {
            e = getPreds()[i];
            if (e != null) {
                e.getSuccessors().remove(this);
                setPreds(i, null);
            }
        }
    }

    public void removePred(int i) {
        if (i >= 0 && i < getPreds().length) {
            DmEntity e = getPreds()[i];
            if (e != null) {
                e.getSuccessors().remove(this);
                setPreds(i, null);
            }
        }
    }

    /**This method is called by a ConstraintGraph object. It removes the
     * reference to the object's target and removes this object from
     * the target's constraint.
     */
    public void removeTarget() {
        DmEntity target = getTarget();
        if (target != null) {
            target.setConstraint(null);
            setTarget(null);
        }
    }

    /**This method is called by a ConstraintGraph object.
     * This method fires the Constraint object, i.e., updates the object's
     * target value based on the predecessors' values. This method does not
     * recursively call the fire() method of the target's successors.
     */
    abstract public void fire();

    public int degree() {
        return 0;
    }

    protected boolean verifyPreds(DmEntity[] preds) {
        DmEntity.Type[] types = getPredTypes();
        if (preds.length != types.length) {
            return false;
        } else {
            for (int i = 0; i < preds.length; i++) {
                if (preds[i] != null && !preds[i].hasType(types[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean verifyTarget(DmEntity target) {
        return target.hasType(getTargetType());
    }
}
