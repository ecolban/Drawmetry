/*
 * ConstraintManager.java
 *
 * Created on February 12, 2004, 1:07 PM
 */
package com.drawmetry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.undo.UndoableEdit;

import com.drawmetry.constraints.VarPredsConstraint;

/**
 * A <code>ConstraintManager</code> instance manages constraints, each
 * constraint linking one or more entities by some relationship. The
 * constraints are directed, which means that exactly one of the
 * entities that the constraint references is assigned as target; the
 * other entities are called the predecessors of the constraint. If
 * the target of a constraint C is an enity E, we say that C is E's
 * constraint . If E is a predecessor of C, we say that C is a
 * successor of E. Different constraints must have different targets,
 * but may share one or more predecessors. This is equivalent to: An
 * entiry may have at most one constraint, but may have zero or more
 * successors.
 * <p>
 * When a constraint fires, the value of its target entity is updated
 * to so that the entities statisfy the constraint. There could be
 * more than one possible value that, if assigned to the target, would
 * satify a constraint, or there could be none. It is not the
 * constraint manager's responsibility to determine which value gets
 * assigned. The constraint manger determines when a constraint needs
 * to be fired and calls the constraint's <code>fire()</code> method.
 * <p>
 * A path is a finite (the set of constraints managed by a constraint
 * manager is at any time finite) sequence of constraints such that
 * the target of each constraint in the sequence, except for the last,
 * is a predecessor of the following constraint in the sequence. A
 * path is a cycle if the first and last constraints are the same. A
 * constraint manager ensures that no cycles are introduced. If an
 * attempt to add a constraint is made that would introduce a cycle,
 * the constraint manager throws a <code>CycleException</code>.
 * <p>
 * An entity F is a dependent of an entity <code>E</code> if either
 * <code>E == F</code> or there exists a path such that <code>E</code>
 * is a predecessor of the first constraint in the path and
 * <code>F</code> is the target of the last constraint. In the absence
 * of cycles, the dependency relationship is anti-symmetric; i.e., if
 * <code>E</code> is a dependent of <code>F</code> and <code>F</code>
 * is a dependent of <code>E</code>, then <code>E == F</code>. In
 * addition, it is refexisive and transitive. This implies that the
 * dependency relationship is a partial ordering.
 * <p>
 * The ConstraintManager does not control the values given to the
 * entities. In order to preserve the consistency of the values (i.e.,
 * ensure that the entities' value assignement satisfies all the
 * constraints), the following procedues should be followed:
 * <p>
 * First the contraint manager's <code>getDependents()</code> method
 * must be called identifying the entity or set of entities that are
 * going to be updated. The method returns a list of dependents sorted
 * in the order of dependency, i.e., if <code>F</code> is a dependent
 * of <code>E</code>, <code>E</code> is before <code>F</code> in the
 * list. After calling the <code>getDependents()</code>, a client
 * object may assign new values to the entities identified in this
 * method. After new values have been assigned, the client object
 * shall call the <code>updateDependents()</code> method, which
 * triggers the constraint manager to call the constraints'
 * <code>fire()</code> methods. New values may be assigned to the
 * entities and the <code>updateDependents()</code> methods may be
 * called multiple times as long as the entities which values are
 * being updated are within the list returned by the last call to
 * <code>getDependents()</code> and no constraints have been added or
 * removed.
 * <p>
 * <u>Existential Dependencies</u> Entities may be existentially
 * dependent on other entities, i.e., the existence of the entity is
 * dependent on the existance of other entities. Existential dependencies
 * are expressed through the
 * entity properties <code>requiresConstraint</code> and
 * <code>requiresSucessors</code> and are accessible through the
 * corresponding 'get' methods. Since the constraint
 * manager does not control the removal of entities, the constraint
 * manager prepares for the removal of an entity by detaching the
 * entity. An entity is detached if it has neither constraint nor
 * successors.  The constraint manager will ensure
 * that an entity that requires a constraint becomes detached if it
 * removes its constraint and that an entity that requires
 * successors becomes detached if it removes its last successor. The
 * constraint manager notifies the entity manager after detaching an
 * entity if the constraint manger was explicitly requested to detach
 * the entity or if the constraint manager detached the entity to
 * satisfy an existential dependency.
 *
 *
 * @author Erik Colban
 * @see com.drawmetry.Constraint
 * @see com.drawmetry.DmEntity
 */
public class ConstraintManager {

    private List<Constraint> constraintResolutionPath = new ArrayList<Constraint>();
    private Stack<DmEntity> entitiesToBeDetached = new Stack<DmEntity>();
    private Set<Constraint> constraintsToDissolve = new HashSet<Constraint>();
    private Object lastUpdated = null;
    private EntityManager entityManager;
    private ArrayList<DmEntity> selection;

    /** Creates a new instance of ConstraintManager.
     * The <code>entityManager</code> is notified when an entity has been
     * detached.
     * @param entityManager the entity manager, e.g., the model
     */
    public ConstraintManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Adds a constraint if the number and type of the predecessors and target
     * match those of the constraint, the target is not already constrained, and
     * adding the constraint does not introduce a cycle.
     * @param cnstr the <code>Constraint</code> to be added.
     * @param preds an array containing the predecessors
     * @param target the target of the constraint
     * @return an UndoableEdit
     * @throws ConstraintGraphException
     * @throws IllegalArgumentException
     *
     */
    public UndoableEdit addConstraint(Constraint cnstr, DmEntity[] preds,
            DmEntity target)
            throws ConstraintGraphException, IllegalArgumentException {
        lastUpdated = null;
        if (!cnstr.verifyPreds(preds)) {
            throw new IllegalArgumentException(
                    "Cannot add constraint: "
                    + "Wrong number or type of predecessors.");
        }
        if (!cnstr.verifyTarget(target)) {
            throw new IllegalArgumentException(
                    "Cannot add constraint: "
                    + "Wrong type of target.");
        }

        if (target.getConstraint() != null) { //target already has a constraint
            throw new ConstraintGraphException("Cannot add constraint: "
                    + " The target is already constrained.");
        }
        List<DmEntity> dependents = getDependents(target, null);
        int preds_length = preds.length;
        for (DmEntity e : dependents) {
            for (int j = 0; j < preds_length; j++) {
                if (e == preds[j]) //One of the elements in preds is an (indirect)dependent of
                //target. If the constraint were added, a cycle would be
                //introduced in the graph.
                {
                    throw new ConstraintGraphException("Cannot add constraint. "
                            + "Adding the constraint would result in a cycle.");
                }
            }
        }
        // Should be ready to add the constraint without having to throw
        // any exception
        GraphEngineEdit edit = new GraphEngineEdit();
        for (int i = 0; i < preds.length; i++) {
            cnstr.addLink(preds[i], i);
            edit.linkAdded(preds[i], cnstr, i);
        }
        cnstr.addTarget(target);
        edit.linkAdded(cnstr, target);
        edit.entitiesToUpdate(dependents);
        cnstr.fire();
        fireConstraints();
        return edit;
    }

    private void addConstraint_(Constraint cnstr, DmEntity[] preds, DmEntity target) {
        for (int i = 0; i < preds.length; i++) {
            cnstr.addLink(preds[i], i);
        }
        cnstr.addTarget(target);
    }

    /**
     * Removes a constraint. If the target of the constraint requires
     * a constraint, the constraint manager detaches the target and notifies
     * the entity manager by calling the <code>entityDetached()</code>
     * method. If one of the predecessors of the constraint requires a
     * successor and no successors are left after removing the
     * constraint, the constraint manager detaches the predecessor,
     * and notifies the
     * entity manager by calling the <code>entityDetached()</code>
     * method. The removal of constraints continues recursively until
     * the existential dependencies are satified.
     *
     * @param entity the <code>DmEntity</code> which constraint is to
     * be removed
     * @return an UndoableEdit
     * @see com.drawmetry.EntityManager#entityDetached
     */
    public UndoableEdit removeConstraint(DmEntity entity) {
        assert entitiesToBeDetached.isEmpty();
        assert constraintsToDissolve.isEmpty();
        GraphEngineEdit edit = new GraphEngineEdit();
        Constraint cnstr = entity.getConstraint();
        removeConstraint_(cnstr, edit);
        detachEntities_(edit);
        if (!constraintsToDissolve.isEmpty()) {
            dissolveConstraints(constraintsToDissolve, edit);
        }
        lastUpdated = null;
        return edit;
    }

    /**
     * Removes the constraint and all successors of an entity and notifies the
     * entity manager when done.
     * @param entity the <code>DmEntity</code>, to be removed
     * @return an UndoableEdit
     *
     */
    public UndoableEdit detachEntity(DmEntity entity) {
        assert entitiesToBeDetached.isEmpty();
        GraphEngineEdit edit = new GraphEngineEdit();
        lastUpdated = null;
        entitiesToBeDetached.push(entity);
        detachEntities_(edit);
        dissolveConstraints(constraintsToDissolve, edit);
        edit.end();
        return edit;
    }

    public UndoableEdit addPred(VarPredsConstraint constraint, DmEntity entity, int pos)
            throws ConstraintGraphException {
        lastUpdated = null;
        DmEntity[] oldPreds = constraint.getPreds();
        if (pos < 0 || pos >= oldPreds.length) {
            throw new ConstraintGraphException("Cannot add the predecessor.\n"
                    + "pos is out of range.");
        }
        List<DmEntity> dependents = getDependents(constraint.getTarget(), null);
        for (DmEntity e : dependents) {
            if (e == entity) //entity is an (indirect)dependent of
            //target. If entity were added, a cycle would be
            //introduced in the graph.
            {
                throw new ConstraintGraphException("Cannot add predecessor. "
                        + "Adding the predecessor would result in a cycle.");
            }
        }
        DmEntity[] newPreds = oldPreds.clone();
        newPreds[pos] = entity;
        if (!constraint.verifyPreds(newPreds)) {
            throw new IllegalArgumentException(
                    "Cannot add constraint: "
                    + "Wrong number or type of predecessors.");
        }
        // Should be ready to add the predecessor without having to throw
        // any exception
        GraphEngineEdit edit = new GraphEngineEdit();
        constraint.addLink(entity, pos);
        edit.linkAdded(entity, constraint, pos);
        edit.entitiesToUpdate(dependents);
        constraint.fire();
        fireConstraints();
        return edit;
    }

    /**
     * Returns a <code>List</code> of all entities
     * that are dependents of <code>ce</code>
     * including <code>ce</code>. In addition, the constraint manager
     * determines which constraints need to be fired and in which order
     * whenever <code>ce</code> is updated.
     *
     *@param ce an instance of <code>DmEntity</code>
     *@param timestamp an <code>Object</code> used to verify the freshness
     *of the dependents list.
     *@return a <code>List</code> of entities that are dependent of
     *        <code>ce</code>, including <code>ce</code>.
     */
    public List<DmEntity> getDependents(DmEntity ce, Object timestamp) {
        findConstraintResolutionPath(ce);
        int i = constraintResolutionPath.size();
        List<DmEntity> dependents;
        if (ce.getConstraint() == null) {
            dependents = new ArrayList<DmEntity>(i + 1);
            dependents.add(ce);
        } else {
            dependents = new ArrayList<DmEntity>(i);
        }
        for (Constraint ct : constraintResolutionPath) {
            dependents.add(ct.getTarget());
        }
        lastUpdated = timestamp;
        return dependents;
    }

    /**
     * Returns a <code>List</code> of all entities
     * that are dependents of the elements of entityArray, including the
     * elements themselves. In addition, the constraint manager
     * determines which constraints need to be fired and in which order
     * whenever any of the entities are updated.
     *
     *@param entityArray an array of <code>DmEntity</code>s
     *@param timestamp an <code>Object</code> used to verify the freshness
     * of the dependents list.
     *@return a <code>List</code> of entities that are dependent of
     * the elements of entityArray, including the elements themselves
     */
    public List<DmEntity> getDependents(DmEntity[] entityArray, Object timestamp) {
        findConstraintResolutionPath(entityArray);
        List<DmEntity> dependents = new ArrayList<DmEntity>();
        for (int i = 0; i < entityArray.length; i++) {
            DmEntity ce = entityArray[i];
            assert ce != null && !dependents.contains(ce);
            if (ce.getConstraint() == null) {
                //Entities that have a constraint will have their constraint
                //in the constraintResolutionPath
                dependents.add(ce);
            }
        }
        for (Constraint ct : constraintResolutionPath) {
            DmEntity ce = ct.getTarget();
            assert ce != null && !dependents.contains(ce);
            dependents.add(ce);
        }
        lastUpdated = timestamp;
        return dependents;
    }

    /**
     * This method must be called after modifying one or more entities.
     * The entities must have been identified through the last call to
     * <code>getDependents()</code>. The timestamp must be identical to the
     * timestamp used in the last call to <code>getDependent()</code>.
     * If different, a <code>RuntimeException</code> is thrown.
     *
     *
     *@param timestamp an <code>Object</code> that must be identical with
     * the timestamp used in the last call to <code>getDependents()</code>.
     *@throws RuntimeException
     *
     *@see ConstraintManager#getDependents(DmEntity ce, Object timesptamp)
     *@see ConstraintManager#getDependents(DmEntity[] entityArray, Object timesptamp)
     */
    public void updateDependents(Object timestamp) throws RuntimeException {
        // Test if the constraintResolutionPath needs to be regenerated:
        if (timestamp == null || timestamp != lastUpdated) {
            throw new RuntimeException("The dependents list is stale. timeStamp = " + timestamp + " lastUpdated = " + lastUpdated);
        }

        fireConstraints();
    }

    /**
     * This method is called to extend a selection to entities that are
     * existentially dependent on the entities in the selection. Entities
     * that require a successor for which at least one successor's target
     * is in the selection and such that that successor's predecessors are
     * all either in the selection or require a successor, are added to the
     * selection. Entities that require a constraint and for which not all
     * the predecessors of their constraint is in the selection are removed
     * from the selection.
     * @param c a <code>Collection</code> of <code>DmEntity</code>'s.
     * @return a <code>List</code> of <code>DmEntity</code>'s.
     */
    public List<DmEntity> completeSelection(Collection<? extends DmEntity> c) {
        selection = new ArrayList<DmEntity>(c);
        completeSelection_(selection);
        return selection;

    }

    private void completeSelection_(ArrayList<DmEntity> selection) {
        for (Iterator<DmEntity> i = selection.iterator(); i.hasNext();) {
            DmEntity e = i.next();
            assert !e.isSuccessorRequired();
        }
        /*Extend the selection by adding preds. A pred is added only if
        all the preds can be added. */
        boolean selectPreds;
        for (int n = 0; n < selection.size(); n++) {
            DmEntity e = selection.get(n);
            Constraint constraint = e.getConstraint();
            if (constraint != null) {
                DmEntity[] preds = constraint.getPreds();
                selectPreds = true;
                for (int i = 0; i < preds.length; i++) {
                    assert preds[i] != null;
                    if (!preds[i].isSuccessorRequired() && !selection.contains(preds[i])) {
                        selectPreds = false;
                        break;
                    }
                }
                if (selectPreds) {
                    for (int i = 0; i < preds.length; i++) {
                        if (!selection.contains(preds[i])) {
                            selection.add(preds[i]);
                        }
                    }
                }
            }
        }
        /* Remove entities in the selection that require a constraint but such
        that not all the predecessors of their constraint are in the
        selection.*/
        for (Iterator<DmEntity> i = selection.iterator(); i.hasNext();) {
            DmEntity e = i.next();
            Constraint constraint = e.getConstraint();
            if (e.isConstraintRequired() && constraint != null) {
                DmEntity[] preds = constraint.getPreds();
                for (int j = 0; j < preds.length; j++) {
                    if (!selection.contains(preds[j])) {
                        i.remove();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns an array of DmEntities which are copies of the entities of a
     * given collection of entities. The constraints that exist between the
     * respetive entities in the given collection are copied and added to the
     * copied entities.
     *
     *
     * @param entities a <code>Collection</code> of <code>DmEntity</code>'s.
     * @return an array of <code>DmEntity</code> containing a copy
     */
    public DmEntity[] copySelection(Collection<DmEntity> entities) {

        // Start extending the selection.
        selection = new ArrayList<DmEntity>(entities);
        completeSelection_(selection);

        // Clone the selection:
        DmEntity[] selection2 = new DmEntity[0];
        selection2 = selection.toArray(selection2);
        try {
            for (int i = 0; i < selection2.length; i++) {
                DmEntity e = selection2[i];
                selection2[i] = (DmEntity) e.clone();
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        // For each entity in the cloned selection, add the constraints if
        // target and preds are in the selection.
        assert constraintsToDissolve.isEmpty();
        for (int i = 0; i < selection2.length; i++) {
            DmEntity target = selection.get(i);
            assert target != null;
            if (target.getConstraint() != null) {
                Constraint constraint = target.getConstraint();
                DmEntity[] preds = constraint.getPreds();
                boolean allPreds = true;
                boolean somePreds = false;
                DmEntity[] preds2 = new DmEntity[preds.length];
                for (int j = 0; j < preds.length; j++) {
                    int n = selection.indexOf(preds[j]);
                    if (n >= 0) {
                        preds2[j] = selection2[n];
                        somePreds = true;
                    } else {
                        allPreds = false;
                    }
                }
                if (allPreds) {
                    try {
                        Constraint c = constraint.clone();
                        addConstraint_(c, preds2, selection2[i]);
                    } catch (CloneNotSupportedException ex) {
                        ex.printStackTrace();
                    }

                } else if (somePreds && complexConstraint(constraint)) {
                    try {
                        Constraint c = constraint.clone();
                        addConstraint_(c, preds2, selection2[i]);
                        constraintsToDissolve.add(c);
                    } catch (CloneNotSupportedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        if (!constraintsToDissolve.isEmpty()) {
            dissolveConstraints(constraintsToDissolve, null);
        }
        return selection2;
    }

    private void removeConstraint_(Constraint c, GraphEngineEdit monitor) {
        DmEntity t = c.getTarget();
        if (t != null) {
            c.removeTarget();
            if (monitor != null) {
                monitor.linkRemoved(c, t);
            }
            if (t.isConstraintRequired()) {
                entitiesToBeDetached.push(t);
            }
        }
        DmEntity[] preds = c.getPreds();

        for (int i = 0; i < preds.length; i++) {
            if (preds[i] != null) {
                c.removePred(i);
                if (monitor != null) {
                    monitor.linkRemoved(preds[i], c, i);
                }
                if (preds[i].isSuccessorRequired() && preds[i].getSuccessors().
                        isEmpty()) {
                    entitiesToBeDetached.push(preds[i]);
                }
            }
        }
    }

    private void removePred_(Constraint constraint, DmEntity ce,
            GraphEngineEdit monitor) {
        DmEntity[] preds = constraint.getPreds();
        for (int i = 0; i < preds.length; i++) {
            if (preds[i] == ce) {
                constraint.removePred(i);
                if (monitor != null) {
                    monitor.linkRemoved(preds[i], constraint, i);
                }
                if (ce.isSuccessorRequired()
                        && ce.getSuccessors().isEmpty()) {
                    entitiesToBeDetached.push(preds[i]);
                }
            }
        }
    }

    private void detachEntities_(GraphEngineEdit edit) {
        while (!entitiesToBeDetached.isEmpty()) {
            DmEntity entity = entitiesToBeDetached.pop();
            Constraint c;
            if (entity.getConstraint() != null) {
                c = entity.getConstraint();
                removeConstraint_(c, edit);
                if (edit != null) {
                    edit.linkRemoved(c, entity);
                }
            }
            assert entity.getConstraint() == null;
            if (!entity.getSuccessors().isEmpty()) {
                List<Constraint> successorsCopy =
                        new ArrayList<Constraint>(entity.getSuccessors());
                for (Constraint constraint : successorsCopy) {
                    if (complexConstraint(constraint)) {
                        removePred_(constraint, entity, edit);
                        if (allPredsAreNull_(constraint)) {
                            constraintsToDissolve.remove(constraint);
                            removeConstraint_(constraint, edit);
                        } else {
                            constraintsToDissolve.add(constraint);
                        }
                    } else if (constraint instanceof VarPredsConstraint) {
                        VarPredsConstraint vpc = (VarPredsConstraint) constraint;
                        DmEntity[] preds = vpc.getPreds();
                        for (int i = 0; i < preds.length; i++) {
                            if (preds[i] == entity) {
                                vpc.removePred(i);
                                if (edit != null) {
                                    edit.linkRemoved(preds[i], constraint, i);
                                }
                            }
                        }
                        if (vpc.isSustainable()) {
                            edit.addToBucket(vpc.trim());
                        } else {
                            removeConstraint_(constraint, edit);
                        }
                    } else {
                        removeConstraint_(constraint, edit);
                    }
                }
                assert entity.getSuccessors().isEmpty();
            }
            entityManager.entityDetached(entity);
        }
    }

    /** This method identifies all the constraints that need to be fired as a
     * result of updating ce. It also identifies the order in which the
     * constraints need to be fired. It assumes that there are no cyces in
     * the graph.
     */
    private void findConstraintResolutionPath(DmEntity ce) {
        constraintResolutionPath.clear();

        if (ce.getConstraint() != null) {
            findConstraintResolutionPathPhase1(ce.getConstraint());
            constraintResolutionPath.clear();
            findConstraintResolutionPathPhase2(ce.getConstraint());
        } else {
            for (Constraint c : ce.getSuccessors()) {
                findConstraintResolutionPathPhase1(c);
            }
            constraintResolutionPath.clear();
            for (Constraint c : ce.getSuccessors()) {
                findConstraintResolutionPathPhase2(c);
            }
        }
        lastUpdated = null;
    }

    /** This method identifies all the constraints that need to be fired as a
     * result of updating the entities. It also identifies the order
     * in which the constraints need to be fired. It assumes that there are no
     * cyces in the graph.
     */
    private void findConstraintResolutionPath(DmEntity[] entities) {

        constraintResolutionPath.clear();
        for (int i = 0; i < entities.length; i++) {
            DmEntity de = entities[i];
            if (de.getConstraint() != null) {
                findConstraintResolutionPathPhase1(de.getConstraint());
            } else {
                for (Constraint c : de.getSuccessors()) {
                    findConstraintResolutionPathPhase1(c);
                }
            }
        }
        constraintResolutionPath.clear();
        for (int i = 0; i < entities.length; i++) {
            DmEntity de = entities[i];
            if (de.getConstraint() != null) {
                findConstraintResolutionPathPhase2(de.getConstraint());
            } else {
                for (Constraint c : de.getSuccessors()) {
                    findConstraintResolutionPathPhase2(c);
                }
            }
        }
        lastUpdated = null;
    }

    /**This method makes a first pass through this ConstraintGraph and marks all
     * dependents of the Constraint c.  The markings are used
     * by the findConstraintResolutionPath_2 method, which makes a second path
     * through this ConstraintGraph. This method assumes that there are no
     * cycles in the graph.
     */
    private void findConstraintResolutionPathPhase1(Constraint cstr) {
        cstr.numHits++;
        int next = constraintResolutionPath.size();
        if (cstr.numHits == 1) {
            constraintResolutionPath.add(cstr);
        }
        while (next < constraintResolutionPath.size()) {
            //constraintReolutionPath is the *set* of all constraints such
            //that numHits >= 1;
            //For every constraint that appears before next in
            //constraintReolutionPath, its target's immediate successors are
            //also in constraintReolutionPath
            cstr = constraintResolutionPath.get(next++);
            for (Constraint c : cstr.getTarget().getSuccessors()) {
                c.numHits++;
                if (c.numHits == 1) {
                    constraintResolutionPath.add(c);
                }
            }
        }
    }

    /**
     * This method makes a second path through this ConstraintGraph and,
     * if c was visited in the first pass. It assumes that there are no cycles
     * in the graph.
     */
    private void findConstraintResolutionPathPhase2(Constraint c) {
        assert c.numHits >= 1;
        int next = constraintResolutionPath.size();
        if (c.numHits == 1) {
            constraintResolutionPath.add(c);
        } else {
            c.numHits--;
        }
        while (next < constraintResolutionPath.size()) {
            // For all constraints that appear after or at next in
            // constraintReolutionPath, numHits == 1
            // For every constraint that appears before next in
            // constraintResolutionPath, numHits == 0
            Constraint c1 = constraintResolutionPath.get(next++);
            assert c1.numHits == 1;
            c1.numHits--;
            for (Constraint c2 : c1.getTarget().getSuccessors()) {
                assert c2.numHits >= 1;
                if (c2.numHits == 1) {
                    constraintResolutionPath.add(c2);
                } else {
                    c2.numHits--;
                }
            }
        }
    }

    /**
     *
     * This method is called after the constraintResolutionPath has been initiated to fire
     * all constraints in the constraintResolutionPath.
     */
    private void fireConstraints() {
        for (Constraint ct : constraintResolutionPath) {
            ct.fire();
        }
    }

    /**
     *This method is called to merge two entities.
     *<p>The two entities to be merged must satisfy a set of conditions:
     *<ul>
     *<li>The entities must be of compatible types.
     *
     *<li>Neither entity must be a dependent of the other or else a cycle would
     * be introduced into the graph.
     *
     *<li> Their respective constraints must be unifiable
     *</ul>
     *
     *<p>When two entities are merged, they are replaced by the merged entity.
     * The result of unifying their respective constraints becomes the
     * constraint of the merged entity. The merged entity replaces
     * <code>ce1</code> and <code>ce2</code> as predecessor in any of their
     *successor constraints.
     *
     *<p>If the entities cannot be merged, then this method throws an exception.
     *@param ce1 one of the two entities to be merged.
     *@param ce2 the other of the two entities to be merged.
     * @return an UndoableEdit
     * @throws ConstraintGraphException
     */
    public UndoableEdit mergeEntities(DmEntity ce1, DmEntity ce2)
            throws ConstraintGraphException{
        //Are ce1 and ce2 mergeable?
        if (!mergeable(ce1, ce2)) {
            throw new ConstraintGraphException("Cannot merge entities: " + "Incompatible types.");
        }
        //Will merging introduce a cycle?
        if (ce2.getConstraint() != null) {
            findConstraintResolutionPath(ce1);
            if (constraintResolutionPath.contains(ce2.getConstraint())) {
                throw new ConstraintGraphException("Cannot merge entities: " + ce2.getEntityID() + " is a dependent of " + ce1.getEntityID() + ".");
            }
        }
        if (ce1.getConstraint() != null) {
            findConstraintResolutionPath(ce2);
            if (constraintResolutionPath.contains(ce1.getConstraint())) {
                throw new ConstraintGraphException("Cannot merge entities: " + ce1.getEntityID() + " is a dependent of " + ce2.getEntityID() + ".");
            }
        }
        // Are ce1.getConstraint() and ce2.getConstraint() unifiable?
        if (ce1.getConstraint() != null && ce2.getConstraint() != null && !unifiable(ce1.getConstraint(), ce2.getConstraint())) {
            throw new ConstraintGraphException("Cannot merge entities: " + "Ununifiable constraints.");
        }
        // Should be ready to merge without throwing any exception at this
        // point.

        GraphEngineEdit edit = new GraphEngineEdit();
        // Make the successors of ce2 successors of ce1.
        DmEntity[] preds;
        List<Constraint> successorsCopy = new ArrayList<Constraint>(ce2.getSuccessors());
        for (Constraint c : successorsCopy) {
            preds = c.getPreds();
            for (int j = 0; j < preds.length; j++) {
                if (preds[j] == ce2) {
                    c.removeLink(j);
                    edit.linkRemoved(ce2, c, j);
                    c.addLink(ce1, j);
                    edit.linkAdded(ce1, c, j);
                }
            }
        }
        assert ce2.getSuccessors().isEmpty();
        //        done with merging the successors

        if (ce2.getConstraint() != null) {
            Constraint constr2 = ce2.getConstraint();
            if (ce1.getConstraint() == null) {
                // ce1.getConstraint() == null && ce2.getConstraint() != null ...
                constr2.addTarget(ce1);
                ce2.setConstraint(null);
                edit.linkRemoved(constr2, ce2);
                edit.linkAdded(constr2, ce1);
                //TODO: This is dirty. Need to find something better!
                
            } else {
                // ce1.getConstraint() != null && ce2.getConstraint() != null ...
                Constraint cu = unify(ce1.getConstraint(), constr2, edit);
                cu.addTarget(ce1);
                edit.linkAdded(cu, ce1);
            }
        }
        //TODO: Move the following 4 lines out of this method.
        List<DmEntity> dependents = getDependents(ce1, null);
        edit.entitiesToUpdate(dependents);
        fireConstraints();
        return edit;
    }

    /**
     * Tests if two entities are mergeable
     *
     * @param ce1
     * @param ce2
     * @return true if the entities are mergeable
     */
    protected boolean mergeable(DmEntity ce1, DmEntity ce2) {
        return false;
    }

    /**
     * Tests if two constraints are unifiable
     * @param c1
     * @param c2
     * @return true if the two constraints are unifiable
     */
    protected boolean unifiable(Constraint c1, Constraint c2) {
        return false;
    }

    /**
     * This method will only be called in subclasses of this class, in which
     * case it should be overridden in the subclass. Subclasses that override
     * the <code>unifiable</code> method, must override this method too.
     *<p>
     * The overriding method should remove the preds and the target of c1 and
     * c2, so that they get garbage collected.
     * @param c1 a constraint
     * @param c2 a constraint
     * @param monitor a GraphEngineEdit
     * @return the result of unifying the two constraints
     */
    protected Constraint unify(Constraint c1, Constraint c2,
            GraphEngineEdit monitor) {
        // This is a hook
        assert false;
        return null;
    }

    /**
     * Tests if a constraint is the result of unifying two constraints. This
     * method should be overidden if the unifiable method is overidden.
     * @param constraint
     * @return true if the constraint is the reulst of unifying two other
     * constraints
     */
    protected boolean complexConstraint(Constraint constraint) {
        return false;
    }

    /**
     * This method disolve a set of complex constraints.
     * It should be overidden if there are complex constraints, i.e.,
     * if complexConstraint(Constraint constraint) is overidden.
     * @param constraintsToDissolve a set of complex constraints
     * @param monitor a GraphEngineEdit
     */
    protected void dissolveConstraints(
            Set<Constraint> constraintsToDissolve, GraphEngineEdit monitor) {
        assert false;
    }

    private boolean allPredsAreNull_(Constraint constraint) {
        assert constraint != null;
        DmEntity[] preds = constraint.getPreds();
        for (int i = 0; i < preds.length; i++) {
            if (preds[i] != null) {
                return false;
            }
        }
        return true;
    }
} //end of class ConstraintGraph

