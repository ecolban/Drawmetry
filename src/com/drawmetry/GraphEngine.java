/*
 * GraphEngine.java
 *
 * Created on June 15, 2005, 3:20 PM
 */

package com.drawmetry;

import java.util.Iterator;
import java.util.Set;

import com.drawmetry.constraints.InterCCConstraint;
import com.drawmetry.constraints.InterLCConstraint;
import com.drawmetry.constraints.InterLLConstraint;
import com.drawmetry.constraints.OnCircleConstraint;
import com.drawmetry.constraints.OnLineConstraint;

/**
 *
 * @author Erik
 */
class GraphEngine extends ConstraintManager{
    
    /** Creates a new instance of GraphEngine */
    GraphEngine(DrawingModel model) {
        super(model);
    }
    
    @Override
    protected boolean unifiable(Constraint c1, Constraint c2) {
        return (c1.degree() + c2.degree() <= 2);
    }
    
    @Override
    protected com.drawmetry.Constraint unify(
            Constraint constr1, Constraint constr2, GraphEngineEdit monitor) {
        assert
                (constr1 instanceof OnLineConstraint ||
                constr1 instanceof OnCircleConstraint)
                &&
                (constr2 instanceof OnLineConstraint ||
                constr2 instanceof OnCircleConstraint);
        
        DmEntity e1 = constr1.getPreds()[0];
        DmEntity e2 = constr2.getPreds()[0];
        DmEntity t1 = constr1.getTarget();
        DmEntity t2 = constr2.getTarget();
        Constraint cu;
        constr1.removePreds();
        constr1.removeTarget();
        constr2.removePreds();
        constr2.removeTarget();
        if (monitor != null) {
            monitor.linkRemoved(e1, constr1, 0);
            monitor.linkRemoved(e2, constr2, 0);
            monitor.linkRemoved(constr1, t1);
            monitor.linkRemoved(constr2, t2);
        }
        if (constr1 instanceof OnLineConstraint){
            if (constr2 instanceof OnLineConstraint){
                // case: constr1 instanceof OnLineConstraint &&
                //       constr2 instanceof OnLineConstraint
                cu = new InterLLConstraint();
            } else {
                // case: constr1 instanceof OnLineConstraint &&
                //       constr2 instanceof OnCircleConstraint
                cu = new InterLCConstraint();
            }
            cu.addPreds(new DmEntity[] {e1, e2});
            if (monitor != null) {
                monitor.linkAdded(e1, cu, 0);
                monitor.linkAdded(e2, cu, 1);
            }
        } else {
            if (constr2 instanceof OnLineConstraint){
                // case: constr1 instanceof OnCircleConstraint &&
                //       constr2 instanceof OnLineConstraint
                cu = new InterLCConstraint();
            } else {
                // case: constr1 instanceof OnCircleConstraint &&
                //       constr2 instanceof OnCircleConstraint
                cu = new InterCCConstraint();
            }
            cu.addPreds(new DmEntity[]{e2, e1});
            if (monitor != null) {
                monitor.linkAdded(e2, cu, 0);
                monitor.linkAdded(e1, cu, 1);
            }
        }
        return cu;
    }
    
    @Override
    protected boolean mergeable(DmEntity  ce1, DmEntity ce2) {
        return (ce1 instanceof PointVar &&
                ce2 instanceof PointVar)
                ||
                (ce1 instanceof AnchorVar &&
                ce2 instanceof AnchorVar &&
                ce1.getConstraint() == null)
                ||
                (ce1 instanceof CellContent || ce2 instanceof CellContent &&
                ce1.getConstraint() == null)
                ||
                (ce1 instanceof CellContent || ce2 instanceof NumVar && 
                ce1.getConstraint() == null
                ||
                (ce1 instanceof ShapeVar &&
                ce2 instanceof ShapeVar))
                ;
    }
    
    @Override
    protected boolean complexConstraint(Constraint constraint) {
        return (constraint instanceof InterCCConstraint ||
                constraint instanceof InterLCConstraint ||
                constraint instanceof InterLLConstraint );
    }
    
    @Override
    protected void dissolveConstraints(Set<Constraint> cSet, GraphEngineEdit cgraphEdit) {
//        assert cgraphEdit != null;
        Iterator<Constraint> i = cSet.iterator();
        while (i.hasNext()){
            Constraint c = i.next();
            assert complexConstraint(c);
            i.remove();
            DmEntity [] preds = c.getPreds();
            assert preds.length == 2;
            
            if (preds[0] == null && preds[1] != null) {
                c.removePred(1);
                PointVar p = (PointVar) c.getTarget();
                c.removeTarget();
                Constraint c2 = c instanceof InterLLConstraint ?
                    new OnLineConstraint() :
                    new OnCircleConstraint();
                c2.addLink(preds[1], 0);
                c2.addTarget(p);
                if (cgraphEdit != null){
                    cgraphEdit.linkRemoved(preds[1], c, 1);
                    cgraphEdit.linkRemoved(c, p);
                    cgraphEdit.linkAdded(preds[1], c2, 0);
                    cgraphEdit.linkAdded(c2, p);
                }
            } else {
                assert preds[0] != null && preds[1] == null;
                c.removePred(0);
                PointVar p = (PointVar) c.getTarget();
                c.removeTarget();
                Constraint c2 = c instanceof InterCCConstraint ?
                    new OnCircleConstraint() :
                    new OnLineConstraint();
                c2.addLink(preds[0], 0);
                c2.addTarget(p);
                if (cgraphEdit != null) {
                    cgraphEdit.linkRemoved(preds[0], c, 0);
                    cgraphEdit.linkRemoved(c, p);
                    cgraphEdit.linkAdded(preds[0], c2, 0);
                    cgraphEdit.linkAdded(c2, p);
                }
            }
        }
    }
    
    
}
