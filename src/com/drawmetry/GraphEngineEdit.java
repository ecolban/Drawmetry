package com.drawmetry;

import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;


public class GraphEngineEdit extends AbstractUndoableEdit {
    
    private List<TargetLink> addedTargetLinks = new ArrayList<TargetLink>();
    private List<TargetLink> removedTargetLinks = new ArrayList<TargetLink>();;
    private List<PredLink> addedPredLinks = new ArrayList<PredLink>();
    private List<PredLink> removedPredLinks = new ArrayList<PredLink>();
    private EntityRecord[] updatedEntityRecords;
    private CompoundEdit bucketEdit;
    
    void linkAdded(Constraint c, DmEntity e) {
        for (TargetLink lr : addedTargetLinks){
            if (c == lr.getConstraint() && e == lr.getEntity()) {
                assert false;
                return;
            }
        }
        addedTargetLinks.add(new TargetLink(c,e));
    }
    
    void linkRemoved(Constraint c, DmEntity e) {
        for (TargetLink lr : addedTargetLinks){
            if (c == lr.getConstraint() && e == lr.getEntity()) {
                assert false;
                return;
            }
        }
        removedTargetLinks.add(new TargetLink(c,e));
    }
    
    void linkAdded(DmEntity e, Constraint c, int i){
        for (PredLink lr : addedPredLinks ) {
            if (c == lr.getConstraint()
            && e == lr.getEntity()
            && i == lr.getIndex() ) {
                assert false;
            }
        }
        addedPredLinks.add(new PredLink(e, c, i));
    }
    
    void linkRemoved(DmEntity e, Constraint c, int i){
        for (PredLink lr : addedPredLinks ) {
            if (c == lr.getConstraint()
            && e == lr.getEntity()
            && i == lr.getIndex() ) {
                assert false;
                return;
            }
        }
        removedPredLinks.add(new PredLink(e, c, i));
    }
    
    void entitiesToUpdate(List<DmEntity> elist) {
        int length = elist.size();
        updatedEntityRecords = new EntityRecord [length];
        for (int i = 0; i < length; i++) {
            updatedEntityRecords[i] = elist.get(i).makeRecord(-1);
        }
    }
    
    @Override
    public String getPresentationName() {
        return "Alter ConstraintGraph";
    }
    
    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        for (PredLink link : removedPredLinks) {
            link.getConstraint().removeLink(link.getIndex());
        }
        for (PredLink lr : addedPredLinks) {
            lr.getConstraint().addLink(lr.getEntity(), lr.getIndex());
        }
        for (TargetLink link : removedTargetLinks ) {
            link.getConstraint().removeTarget();
        }
        for (TargetLink lr : addedTargetLinks) {
            
            lr.getConstraint().addTarget(lr.getEntity());
        }
        if (updatedEntityRecords != null) {
            for (int i = 0; i < updatedEntityRecords.length; i++ ) {
                EntityRecord er1 = updatedEntityRecords[i];
                DmEntity e = er1.getEntity();
                EntityRecord er2 = e.makeRecord(-1);
                e.setParams(er1);
                updatedEntityRecords[i] = er2;
            }
        }
        if (bucketEdit != null) {
            bucketEdit.redo();
        }
    }
    
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        
        if(bucketEdit != null) {
            bucketEdit.undo();
        }
        for ( PredLink link : addedPredLinks) {
            link.getConstraint().removeLink(link.getIndex());
        }
        for ( PredLink lr : removedPredLinks) {
            lr.getConstraint().addLink(lr.getEntity(), lr.getIndex());
        }
        for ( TargetLink link : addedTargetLinks ) {
            link.getConstraint().removeTarget();
        }
        for ( TargetLink lr : removedTargetLinks) {
            lr.getConstraint().addTarget(lr.getEntity());
        }
        if (updatedEntityRecords != null) {
            for (int i = 0; i < updatedEntityRecords.length; i++ ) {
                EntityRecord er1 = updatedEntityRecords[i];
                DmEntity e = er1.getEntity();
                EntityRecord er2 = e.makeRecord(-1);
                e.setParams(er1);
                updatedEntityRecords[i] = er2;
            }
        }
    }
    
    @Override
    public String toString() {
        return "GraphEngineEdit:" ;
    }

    void addToBucket(UndoableEdit edit) {
        if (bucketEdit == null) {
            bucketEdit = new CompoundEdit();
        }
        bucketEdit.addEdit(edit);
    }

    void end() {
        if (bucketEdit != null) {
            bucketEdit.end();
        }
    }
}
