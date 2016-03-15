/*
 * DrawingModel.java
 *
 * Created on May 26, 2004, 10:02 PM
 */
package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.drawmetry.MCell.CellFormat;
import com.drawmetry.constraints.MPolylineConstraint;
import com.drawmetry.constraints.NullConstraint;
import com.drawmetry.constraints.VarPredsConstraint;

/**
 *
 * @author  Erik Colban
 */
public class DrawingModel implements EntityManager, UndoableEditListener {

    static final String PROP_UNDO_ENABLED = "undoEnabled";
    static final String PROP_REDO_ENABLED = "redoEnabled";
    static final String PROP_SAVE_ENABLED = "saveEnabled";
    static final boolean TOP_TO_BOTTOM = true;
    static final boolean BOTTOM_TO_TOP = false;
    private GraphEngine cgraph = new GraphEngine(this);
    private GraphEngineEdit currentUpdate = new GraphEngineEdit();
    private boolean currentUpdatePosted = true;
    private Object timeStamp;
    private List<DmEntity> dependents;
    private boolean updateEnabled;
    private Map<String, PointVar> points;
    private DrawablesList drawableEntities;
    private Object foreground = new Object();
    private Object background = new Object();
    private List<DrawableEntity> backgroundDrawables =
            new ArrayList<DrawableEntity>();
    private List<DrawableEntity> foregroundDrawables =
            new ArrayList<DrawableEntity>();
    private boolean undoEnabled;
    private boolean redoEnabled;
    private PropertyChangeSupport propertyChangeSupport;
    private EventListenerList changeEventListeners = new EventListenerList();
    private ChangeEvent changeEvent;
    private int pointNum = 0;
    private int cellNum = 0;
    private int lineNum = 0;
    private int traceNum = 0;
    private int imageNum = 0;

    boolean contains(DrawableEntity de) {
        DrawableEntity de2;
//        Timer t = new Timer(20, new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        });
        for (Iterator<DrawableEntity> i = getDrawableEntityIterator(TOP_TO_BOTTOM);
                i.hasNext();) {
            de2 = i.next();
            if (de == de2) {
                return true;
            }
        }
        return false;
    }

    boolean isEmpty() {
        return drawableEntities == null || drawableEntities.size() == 0;
    }

    private class EntityEdit extends AbstractUndoableEdit {

        private List<EntityRecord> removedEntityRecords = new ArrayList<EntityRecord>();
        private List<EntityRecord> addedEntityRecords = new ArrayList<EntityRecord>();

        void entityRemoved(DmEntity e, int z) {
            assert e.getConstraint() == null;
            assert e.getSuccessors().isEmpty();
            removedEntityRecords.add(e.makeRecord(z));
        }

        void entityAdded(int z, DmEntity e) {
            assert e.getConstraint() == null;
            assert e.getSuccessors().isEmpty();
            addedEntityRecords.add(e.makeRecord(z));
        }

        @Override
        public void redo() {
            super.redo();
            for (EntityRecord er : removedEntityRecords) {
                DmEntity e = er.getEntity();
                int i = removeEntity_(e);
                assert i == er.getZIndex();
            }
            for (EntityRecord er : addedEntityRecords) {
                restoreEntity(er);
            }
        }

//        public List<EntityRecord> getAddedEntityRecords() {
//            return addedEntityRecords;
//        }
        @Override
        public void undo() {
            super.undo();
            for (int i = removedEntityRecords.size() - 1; i >= 0; i--) {
                EntityRecord er = removedEntityRecords.get(i);
                restoreEntity(er);
            }
            for (EntityRecord er : addedEntityRecords) {
                DmEntity e = er.getEntity();
                int i = removeEntity_(e);
                assert i == er.getZIndex();
            }
        }

        @Override
        public String toString() {
            return "EntityEdit:" + removedEntityRecords + addedEntityRecords;
        }
    }
    private EntityEdit entityEdit = null;
    private UndoManager undoManager = new UndoManager();

    private class UndoableEditSupportWithRollback extends UndoableEditSupport {

        UndoableEditSupportWithRollback(Object r) {
            super(r);
        }

        void rollBack() {
            updateLevel--;
            if (updateLevel == 0) {
                compoundEdit.end();
                if (compoundEdit.canUndo()) {
                    compoundEdit.undo();
                }
                compoundEdit = null;
            }
        }
    }
    private UndoableEditSupportWithRollback undoableEditSupport;

    DrawingModel() {
        propertyChangeSupport = new PropertyChangeSupport(this);
        undoableEditSupport = new UndoableEditSupportWithRollback(this);
        addUndoableEditListener(this);
        points = new HashMap<String, PointVar>();
        drawableEntities = new DrawablesList();
        undoManager = new UndoManager();
    }

    public void undo() {
        assert undoManager != null;
        if (undoManager.canUndo()) {
            undoManager.undo();
            refreshDrawables();
            setUndoEnabled(undoManager.canUndo());
            setRedoEnabled(undoManager.canRedo());
            setSaveEnabled();
        }
    }

    public void redo() {
        assert undoManager != null;
        if (undoManager.canRedo()) {
            undoManager.redo();
            refreshDrawables();
            setUndoEnabled(undoManager.canUndo());
            setRedoEnabled(undoManager.canRedo());
            setSaveEnabled();
        }

    }

    public void discardAllEdits() {
        undoManager.discardAllEdits();
        undoEnabled = false;
        redoEnabled = false;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeEventListeners.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {

        changeEventListeners.remove(ChangeListener.class, listener);
    }

    protected void fireStateChanged(Object source) {
        // Guaranteed to return a non-null array
        Object[] listeners = changeEventListeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(source);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters
    ////////////////////////////////////////////////////////////////////////////
    private void setUndoEnabled(boolean enabled) {
        boolean oldValue = undoEnabled;
        undoEnabled = enabled;
        propertyChangeSupport.firePropertyChange(PROP_UNDO_ENABLED, oldValue, undoEnabled);
    }

    private void setRedoEnabled(boolean enabled) {
        boolean oldValue = redoEnabled;
        redoEnabled = enabled;
        propertyChangeSupport.firePropertyChange(PROP_REDO_ENABLED, oldValue, redoEnabled);
    }

    private void setSaveEnabled() {
        propertyChangeSupport.firePropertyChange(PROP_SAVE_ENABLED, false, true);
    }

    void setStrokeColor(DrawableEntity de, Color currentColor) {
        UndoableEdit edit = de.setStrokeColor(currentColor);
        if (edit != null) {
            postEdit(edit);
        }
    }

    void setStroke(DrawableEntity de, BasicStroke stroke) {
        UndoableEdit edit = de.setStroke(stroke);
        if (edit != null) {
            postEdit(edit);
        }
    }

    void setFillColor(DrawableEntity de, Color color) {
        UndoableEdit edit = de.setFillColor(color);
        if (edit != null) {
            postEdit(edit);
        }
    }

    void setOpacity(DrawableEntity de, double opacity) {
        UndoableEdit edit = de.setOpacity(opacity);
        if (edit != null) {
            postEdit(edit);
        }
    }

    void setStartArrowhead(DrawableEntity de, Arrowhead arrowhead) {
        UndoableEdit edit = de.setStartArrowhead(arrowhead);
        if (edit != null) {
            postEdit(edit);
        }
    }

    void setEndArrowhead(DrawableEntity de, Arrowhead arrowhead) {
        UndoableEdit edit = de.setEndArrowhead(arrowhead);
        if (edit != null) {
            postEdit(edit);
        }
    }

    void setCellFormat(final MCell cell, final CellFormat newFormat) {
        final CellFormat oldFormat = cell.getFormat();
        cell.setFormat(newFormat);
        if (oldFormat != null) {
            postEdit(new AbstractUndoableEdit() {

                @Override
                public void undo() {
                    super.undo();
                    cell.setFormat(oldFormat);
                }

                @Override
                public void redo() {
                    super.redo();
                    cell.setFormat(newFormat);
                }
            });
        }

    }

    /** Creates a new instance of DrawingModel */
    void addPoint(final PointVar pv) {
        points.put(pv.getEntityID(), pv);
        final int i = drawableEntities.add(pv);
        if (i >= 0) {
            EntityEdit ee = new EntityEdit();
            ee.entityAdded(i, pv);
            postEdit(ee);
        } else {
        }
    }

    PointVar getPoint(
            String name) {
        return points.get(name);
    }

    MCell getCell(String cellID) {
        Iterator<DrawableEntity> i = drawableEntities.drawableIterator(true);
        while (i.hasNext()) {
            DrawableEntity de = i.next();
            if (de instanceof MCell) {
                MCell mc = (MCell) de;
                if (mc.getEntityID().equals(cellID)) {
                    return (MCell) de;
                }

            }
        }
        return null;
    }

    DrawableEntity getDrawableEntity(String id) {
        Iterator<DrawableEntity> i = drawableEntities.drawableIterator(true);
        while (i.hasNext()) {
            DrawableEntity de = i.next();
            if (de.getEntityID().equals(id)) {
                return de;
            }

        }
        return null;
    }

    void addDrawableEntity(DrawableEntity de) {
        assert !(de instanceof PointVar);
        int i = drawableEntities.add(de);
        if (i >= 0) {
            EntityEdit ee = new EntityEdit();
            ee.entityAdded(i, de);
            postEdit(ee);
        } else {
        }
    }

    /**
     *Merges two entities if possible.
     *@param a an <code>DmEntity</code> instance
     *@param b an <code>DmEntity</code> instance
     *@see com.drawmetry.ConstraintManager#mergeEntities
     */
    void mergeEntities(DmEntity a, DmEntity b) throws ConstraintGraphException {
        CompoundEdit edit = new CompoundEdit();
        edit.addEdit(cgraph.mergeEntities(a, b));
        entityEdit = new EntityEdit();
        entityDetached(b);
        edit.addEdit(entityEdit);
        UndoableEdit pe = checkAndClosePolylines(a);
        if (pe != null) {
            edit.addEdit(pe);
        }
        edit.end();
        entityEdit = null;
        postEdit(edit);

    }

    /**
     *Removes a <code>Constraint</code>.
     *@param entity an <code>DmEntity</code> instance
     *@see com.drawmetry.ConstraintManager#removeConstraint
     */
    public void removeConstraint(DmEntity entity) {
        CompoundEdit edit = new CompoundEdit();
        entityEdit = new EntityEdit();
        edit.addEdit(cgraph.removeConstraint(entity));
        edit.addEdit(entityEdit);
        entityEdit = null;
        edit.end();
        postEdit(edit);
    }

    /**
     * Removes an <code>DmEntity</code> entity.
     *
     * @param entity an <code>DmEntity</code> instance
     * @see com.drawmetry.ConstraintManager#detachEntity
     */
    void removeEntity(DmEntity entity) {
        CompoundEdit edit = new CompoundEdit();
        entityEdit = new EntityEdit();
        edit.addEdit(cgraph.detachEntity(entity));
        edit.addEdit(entityEdit);
        entityEdit = null;
        edit.end();
        postEdit(edit);
    }

    private int removeEntity_(DmEntity e) {
        assert e.getSuccessors().isEmpty();
        assert e.getConstraint() == null;
        if (e instanceof DrawableEntity) {
            int i = drawableEntities.remove((DrawableEntity) e);
            if (e instanceof PointVar) {
                points.remove(((PointVar) e).getEntityID());
            }

            return i;
        } else {
            return -1;
        }

    }

    private void restoreEntity(EntityRecord er) {
        DmEntity e = er.getEntity();
        if (e instanceof DrawableEntity) {
            drawableEntities.add(er.getZIndex(), (DrawableEntity) e);
            if (e instanceof PointVar) {
                points.put(e.getEntityID(), (PointVar) e);
            }

        }
    }

    /**
     * This method is called by a ConstraintManager when an entity has been
     * detached from all constraints and successors.
     */
//    @Override
    public void entityDetached(DmEntity entity) {
        assert entity.getConstraint() == null;
        assert entity.getSuccessors().isEmpty();
        if (entity instanceof DrawableEntity) {
            int i = drawableEntities.remove((DrawableEntity) entity);
            if (i >= 0) {
                entityEdit.entityRemoved(entity, i);
                if (entity instanceof PointVar) {
                    points.remove(((PointVar) entity).getEntityID());
                    //TODO: check if any of VarPredsConstraints has a pred == null
                }

            }
        }
    }

    void addConstraint(Constraint c, DmEntity[] preds, DmEntity target)
            throws IllegalArgumentException, ConstraintGraphException {
        UndoableEdit edit = null;
        try {
            edit = cgraph.addConstraint(c, preds, target);
            if (edit != null) {
                postEdit(edit);
            }

        } catch (ConstraintGraphException e) {
            if (edit != null && edit.canUndo()) {
                edit.undo();
            }

            throw (e);
        } catch (IllegalArgumentException e) {
            if (edit != null && edit.canUndo()) {
                edit.undo();
            }

            throw (e);
        }

    }

    void addPred(VarPredsConstraint c, DmEntity e, int pos)
            throws ConstraintGraphException {
        postEdit(c.increasePreds(pos));
        postEdit(cgraph.addPred(c, e, pos));
    }

    //TODO: Continue here:
    private UndoableEdit checkAndClosePolylines(DmEntity entity) {
        GraphEngineEdit edit = null;
        for (Iterator<Constraint> it = entity.getSuccessors().iterator(); it.hasNext();) {
            Constraint succ = it.next();
            if (succ instanceof MPolylineConstraint) {
                MPolylineConstraint vpc = (MPolylineConstraint) succ;
                DmEntity[] preds = vpc.getPreds();
                int i = preds.length - 1;
                if (i > 2 && preds[0] == entity && preds[i] == entity) {
                    vpc.setPreds(i, null);
                    edit = new GraphEngineEdit();
                    edit.linkRemoved(preds[i], succ, i);
                    edit.addToBucket(vpc.trim());
                    edit.addToBucket(((MPolyline) succ.getTarget()).setClosed(true));
                }
            }
        }
        if (edit != null) {
            edit.end();
            return edit;
        } else {
            return null;
        }
    }

    private void enableUpdate(DmEntity e) {
        assert !updateEnabled;
        assert timeStamp == null;
        timeStamp = new Object(); //
        GraphEngineEdit edit = new GraphEngineEdit();
        dependents = cgraph.getDependents(e, timeStamp);
        assert dependents != null;
        edit.entitiesToUpdate(dependents);
        refreshDrawables();
        updateEnabled = true;
        postEdit(edit);
    }

    void enableUpdate(Selection selection) {
        assert !updateEnabled;
        assert timeStamp == null;
        if (selection == null || selection.isEmpty()) {
            return;
        }

        timeStamp = new Object();
        currentUpdate = new GraphEngineEdit();
        currentUpdatePosted = false;
        dependents =
                cgraph.getDependents(getSelectionCompletion(selection.getHits()), timeStamp);
        assert dependents != null;
        currentUpdate.entitiesToUpdate(dependents);
        refreshDrawables();
        updateEnabled = true;
    }

    void update() {
        assert updateEnabled == true;
        assert timeStamp != null;
        if (!currentUpdatePosted) {
            postEdit(currentUpdate);
            currentUpdatePosted = true;
        }
        cgraph.updateDependents(timeStamp);
        changeEvent = null;
        fireStateChanged(foreground);
    }

    void disableUpdate() {
        //    assert updateEnabled == true;
        timeStamp = null;
        updateEnabled = false;
        dependents = null;
        currentUpdatePosted = true;
        currentUpdate = null;
        refreshDrawables();

    }

    Iterator<PointVar> getPointIterator(boolean topToBottom) {
        return drawableEntities.pointIterator(topToBottom);
    }

    Iterator<DrawableEntity> getDrawableEntityIterator(boolean topToBottom) {
        assert drawableEntities != null;
        return drawableEntities.drawableIterator(topToBottom);
    }

    /**
     * Recalculates the foreground and the background
     */
    void refreshDrawables() {
        Iterator<DrawableEntity> i = getDrawableEntityIterator(BOTTOM_TO_TOP);
        backgroundDrawables.clear();
        foregroundDrawables.clear();
        if (dependents == null || dependents.isEmpty()) {
            while (i.hasNext()) {
                DrawableEntity de = i.next();
                backgroundDrawables.add(de);
            }

        } else {
            while (i.hasNext()) {
                DrawableEntity de = i.next();
                if (dependents.contains(de)) {
                    foregroundDrawables.add(de);
                } else {
                    backgroundDrawables.add(de);
                }

            }
        }
        changeEvent = null;
        fireStateChanged(background);
    }

    Iterator<DrawableEntity> getBackgroundIterator() {
        return backgroundDrawables.iterator();
    }

    Iterator<DrawableEntity> getForegroundIterator() {
        return foregroundDrawables.iterator();
    }

    void sendToFront(final DrawableEntity de) {
        final int i = drawableEntities.sendToFront(de);
        if (i >= 0) {
            postEdit(new AbstractUndoableEdit() {

                @Override
                public void undo() {
                    super.undo();
                    drawableEntities.remove(de);
                    drawableEntities.add(i, de);
                }

                @Override
                public void redo() {
                    super.redo();
                    drawableEntities.sendToFront(de);
                }
            });
        }

    }

    void sendToBack(final DrawableEntity de) {
        final int i = drawableEntities.sendToBack(de);
        if (i >= 0) {
            postEdit(new AbstractUndoableEdit() {

                @Override
                public void undo() {
                    super.undo();
                    drawableEntities.remove(de);
                    drawableEntities.add(i, de);
                }

                @Override
                public void redo() {
                    super.redo();
                    drawableEntities.sendToBack(de);
                }
            });
        }

    }

    /*
     * UndoableEditSupport -- Begin
     * 
     */
    void addUndoableEditListener(UndoableEditListener listener) {
        undoableEditSupport.addUndoableEditListener(listener);
    }

    int getUpdateLevel() {
        return undoableEditSupport.getUpdateLevel();
    }

    void postEdit(UndoableEdit edit) {
        int i = undoableEditSupport.getUpdateLevel(); //Needed for debugging
        undoableEditSupport.postEdit(edit);
    }

    void beginUpdate() {
        undoableEditSupport.beginUpdate();
    }

    void endUpdate() {
        assert undoableEditSupport.getUpdateLevel() > 0;
        undoableEditSupport.endUpdate();
    }

    void rollBack() {
        assert undoableEditSupport.getUpdateLevel() > 0;
        undoableEditSupport.rollBack();
    }
    /*
     * UndoableEditSupport -- End
     *
     */

    int getPointNum() {
        return pointNum++;
    }

    int getCellNum() {
        return cellNum++;
    }

    int getLineNum() {
        return lineNum++;
    }

    int getTraceNum() {
        return traceNum++;
    }

    int getImageNum() {
        return imageNum++;
    }

    Object getForeground() {
        return foreground;
    }

    Object getBackground() {
        return background;
    }

    /*************************************************************************
     * Pastes a copy of the selection into this model. For each element of the
     * selection that is constrained, the preds of that constraint are also in
     * the selection, i.e., the selection is closed under the predecessor
     * relation.
     *
     *************************************************************************/
    void pasteSelection(final DmEntity[] selection, DmEntity[] selection2)
            throws IllegalArgumentException {
        if (selection.length != selection2.length) {
            throw new IllegalArgumentException();
        }
// Validate that selection is closed under the predecessor relation:

        for (int i = 0; i < selection.length; i++) {
            if (selection[i].getConstraint() != null) {
                Constraint constraint = selection[i].getConstraint();
                DmEntity[] preds = constraint.getPreds();
                for (int j = 0; j < preds.length; j++) {
                    int k;
                    for (k = 0; k < selection.length; k++) {
                        if (selection[k] == preds[j]) {
                            break;
                        }
                    }
                    if (k == selection.length) {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
        // The selection is OK
        beginUpdate();
        double deltaX = (int) (Math.random() * 30) + 10;
        double deltaY = (int) (Math.random() * 30) + 10;
        // Adding the entities
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] instanceof PointVar) {
                try {
                    PointVar pv = (PointVar) selection[i];
                    PointVar pv2 = (PointVar) pv.clone();
                    pv2.setEntityID("P_" + getPointNum());
                    selection2[i] = pv2;
                    pv2.translate(deltaX, deltaY);
                    addPoint((PointVar) selection2[i]);
                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                }

            } else if (selection[i] instanceof MCell) {
                try {
                    MCell mc = (MCell) selection[i];
                    MCell mc2 = mc.clone();
                    mc2.setEntityID("C_" + getCellNum());
                    selection2[i] = mc2;
                    addDrawableEntity(mc2);
                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                }

            } else if (selection[i] instanceof MPolyline) {
                try {
                    MPolyline ml = (MPolyline) selection[i];
                    MPolyline ml2 = ml.clone();
                    ml2.setEntityID("L_" + getLineNum());
                    selection2[i] = ml2;
                    addDrawableEntity(ml2);
                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                }

            } else if (selection[i] instanceof MTrace) {
                try {
                    MTrace mt = (MTrace) selection[i];
                    MTrace mt2 = mt.clone();
                    mt2.setEntityID("S_" + getTraceNum());
                    selection2[i] = mt2;
                    addDrawableEntity(mt2);
                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                }
            } else if (selection[i] instanceof MImage) {
                try {
                    MImage mImage = (MImage) selection[i];
                    MImage mImage2 = mImage.clone();
                    mImage2.setEntityID("I_" + getImageNum());
                    selection2[i] = mImage2;
                    addDrawableEntity(mImage2);
                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                }

            } else {
                try {
                    DmEntity de = (DmEntity) selection[i].clone();
                    de.translate(deltaX, deltaY);
                    selection2[i] = de;
                    if (de instanceof DrawableEntity) {
                        addDrawableEntity((DrawableEntity) de);
                    }
                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                }

            }
        }
        // Adding the constraints
        for (int i = 0; i
                < selection.length; i++) {
            if (selection[i].getConstraint() != null) {
                Constraint constraint = selection[i].getConstraint();
                DmEntity[] preds = constraint.getPreds();
                DmEntity[] preds2 = new DmEntity[preds.length];
                for (int j = 0; j < preds.length; j++) {
                    int k;
                    for (k = 0; k < selection.length; k++) {
                        if (selection[k] == preds[j]) {
                            preds2[j] = selection2[k];
                            break;
                        }
                    }
                    assert k != selection.length;
                }

                try {
                    Constraint constraintClone = constraint.clone();
                    addConstraint(constraintClone, preds2, selection2[i]);
                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (ConstraintGraphException ex) {
                    ex.printStackTrace();
                }

            }
        }
        endUpdate();
    }

    DmEntity[] copySelection(List<DrawableEntity> hitSelection) {
        ArrayList<DmEntity> c = new ArrayList<DmEntity>(hitSelection);
        return cgraph.copySelection(c);
    }

    DmEntity[] getSelectionCompletion(Collection<DrawableEntity> c) {
        DmEntity[] result = new DmEntity[0];
        return cgraph.completeSelection(c).toArray(result);
    }

    void updateCellContents(final CellContent target, CellContent newContent) {
        if (target == null || newContent == null) {
            return;
        }

        if (newContent.isNaN()) {
            if (target.isNaN()) {
                final String s1 = target.getSValue();
                final String s2 = newContent.getSValue();
                target.setSValue(s2);
                postEdit(new AbstractUndoableEdit() {

                    @Override
                    public void undo() {
                        super.undo();
                        target.setSValue(s1);
                    }

                    @Override
                    public void redo() {
                        super.redo();
                        target.setSValue(s2);
                    }
                });
            } else { // !cc1.isNan() && cc2.isNaN();
                final double d1 = target.getNValue();
                final String s2 = newContent.getSValue();
                target.setSValue(s2);
                postEdit(new AbstractUndoableEdit() {

                    @Override
                    public void undo() {
                        super.undo();
                        target.setNValue(d1);
                    }

                    @Override
                    public void redo() {
                        super.redo();
                        target.setSValue(s2);
                    }
                });
            }

        } else {
            if (target.isNaN()) { //  cc1.isNaN() && !cc2.isNaN()
                final String s1 = target.getSValue();
                final double d2 = newContent.getNValue();
                target.setNValue(d2);
                postEdit(new AbstractUndoableEdit() {

                    @Override
                    public void undo() {
                        super.undo();
                        target.setSValue(s1);
                    }

                    @Override
                    public void redo() {
                        super.redo();
                        target.setNValue(d2);
                    }
                });
            } else { // !cc1.isNaN() && !cc2.isNaN()
                final double d1 = target.getNValue();
                final double d2 = newContent.getNValue();
                target.setNValue(d2);
                postEdit(new AbstractUndoableEdit() {

                    @Override
                    public void undo() {
                        super.undo();
                        target.setNValue(d1);
                    }

                    @Override
                    public void redo() {
                        super.redo();
                        target.setNValue(d2);
                    }
                });
            }

        }
    }

    List<DmEntity> getDependents(DmEntity e) {
        if (updateEnabled) {
            disableUpdate();
        }
        enableUpdate(e);
        return dependents;
    }

    List<DmEntity> getDependents(Selection s) {
        if (updateEnabled) {
            disableUpdate();
        }
        enableUpdate(s);
        return dependents;
    }

    void addCell(MCell mc) {
        assert mc != null;
        AnchorVar a = mc.getAnchor();
        CellContent cc = mc.getContent();
        assert a != null;
        assert cc != null;
        beginUpdate();

        addDrawableEntity(mc);
        try {
            addConstraint(new NullConstraint(2), new DmEntity[]{cc, a}, mc);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (ConstraintGraphException ex) {
            ex.printStackTrace();
        }

        endUpdate();
    }

    Rectangle2D getBounds() {
        Selection sel = new Selection();
        for (Iterator<DrawableEntity> i = drawableEntities.drawableIterator(true); i.hasNext();) {
            sel.addHit(i.next());
        }

        return sel.getBounds();
    }

    public void undoableEditHappened(UndoableEditEvent e) {
        undoManager.addEdit(e.getEdit());
        setUndoEnabled(undoManager.canUndo());
        setRedoEnabled(undoManager.canRedo());
        setSaveEnabled();

    }
}
