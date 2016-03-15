/*
 * MPolylineConstraint.java
 *
 * Created on April, 2009
 */
package com.drawmetry.constraints;

import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import com.drawmetry.DmEntity;
import com.drawmetry.MPolyline;
import com.drawmetry.PointVar;

/**
 *
 * @author  Erik
 */
public class MPolylineConstraint extends VarPredsConstraint {

//    private PointVar[] preds;
    private List<PointVar> predsList = new ArrayList<PointVar>();
    private MPolyline target;
    private double[] targetPoints;
    private int numPreds = 2;

    public MPolylineConstraint() {
        predsList.add(null);
        predsList.add(null);
    }

    public MPolylineConstraint(int numPreds) {
        this.numPreds = numPreds;
        for (int i = 0; i < numPreds; i++) {
            predsList.add(null);
        }
    }

    public DmEntity[] getPreds() {
        return predsList.toArray(new PointVar[predsList.size()]);
    }

    public void fire() {

//        targetPoints = target.getPoints();
        int index = 0;
        for (PointVar p : predsList) {
            targetPoints[index++] = p.x;
            targetPoints[index++] = p.y;
        }
    }

    @Override
    public int degree() {
        return 0;
    }

    @Override
    protected boolean verifyPreds(DmEntity[] preds) {
        for (int i = 0; i < preds.length; i++) {
            if (preds[i] != null && !preds[i].hasType(DmEntity.Type.POINT_VAR)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean verifyTarget(DmEntity target) {
        if (target.hasType(getTargetType())) {
            this.target = (MPolyline) target;
            return true;
        } else {
            return false;
        }
    }

    public DmEntity.Type[] getPredTypes() {
        assert predsList != null;
        DmEntity.Type[] pt = new DmEntity.Type[predsList.size()];
        for (int i = 0; i < predsList.size(); i++) {
            pt[i] = DmEntity.Type.POINT_VAR;
        }
        return pt;
    }

    public com.drawmetry.DmEntity getTarget() {
        return target;
    }

    public DmEntity.Type getTargetType() {
        return DmEntity.Type.LINE;
    }

    public void setPreds(int i, DmEntity e) {
        if (i < 0 || i >= predsList.size() ||
                !(e instanceof PointVar) && e != null) {
            throw new IllegalArgumentException();
        }
        predsList.set(i, (PointVar) e);
    }

    public void setPreds(DmEntity[] preds) {
        for (int i = 0; i < preds.length; i++) {
            if (!(preds[i] instanceof PointVar) && preds[i] != null) {
                throw new IllegalArgumentException();
            }
        }
        predsList = new ArrayList<PointVar>();
        for (int i = 0; i < preds.length; i++) {
            predsList.add((PointVar) preds[i]);
        }
    }

    protected void setTarget(DmEntity target) {
        if (!(target instanceof MPolyline) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (MPolyline) target;
        if (target != null) {
            targetPoints = this.target.getPoints();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("vertices(");
        boolean first = true;
        for (PointVar e : predsList) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(e.getEntityID());
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean isSustainable() {
        return predsList.size() > 2;
    }

    public UndoableEdit increasePreds(int pos) {
        final int actualPos;
        final double[] pointsBefore;
        final double[] pointsAfter;
        if (pos < 0) {
            predsList.add(0, null);
            actualPos = 0;
        } else if (pos >= numPreds) {
            predsList.add(null);
            actualPos = numPreds;
        } else {
            predsList.add(pos, null);
            actualPos = pos;
        }
        numPreds++;
        if (target != null) {
            pointsBefore = target.getPoints();
            pointsAfter = targetPoints = new double[2 * numPreds];
            target.setPoints(pointsAfter);
        } else {
            pointsBefore = pointsAfter = null;
        }
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                predsList.remove(actualPos);
                numPreds--;
                if (target != null) {
                    target.setPoints(targetPoints = pointsBefore);
                }
            }

            @Override
            public void redo() {
                super.redo();
                predsList.add(actualPos, null);
                numPreds++;
                if (target != null) {
                    target.setPoints(targetPoints = pointsAfter);
                }
            }
        };
    }

    public UndoableEdit decreasePreds(int pos) {
        final int actualPos;
        final PointVar removedPoint;
        final double[] pointsBefore;
        final double[] pointsAfter;
        if (pos < 0) {
            removedPoint = predsList.remove(0);
            actualPos = 0;
        } else if (pos >= numPreds) {
            removedPoint = predsList.remove(numPreds - 1);
            actualPos = numPreds - 1;
        } else {
            removedPoint = predsList.remove(pos);
            actualPos = pos;
        }
        numPreds--;
        if (target != null) {
            pointsBefore = target.getPoints();
            pointsAfter = targetPoints = new double[2 * numPreds];
            target.setPoints(pointsAfter);
        } else {
            pointsBefore = pointsAfter = null;
        }
        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                super.undo();
                predsList.add(actualPos, removedPoint);
                numPreds++;
                if (target != null) {
                    target.setPoints(targetPoints = pointsBefore);
                }
            }

            @Override
            public void redo() {
                super.redo();
                predsList.remove(actualPos);
                numPreds--;
                if (target != null) {
                    target.setPoints(targetPoints = pointsAfter);
                }
            }
        };
    }

    public void initRemovePred(DmEntity pred) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UndoableEdit trim() {
        CompoundEdit edit = new CompoundEdit();
        int index = 0;
        while (index < predsList.size()) {
            if (predsList.get(index) == null) {
                edit.addEdit(decreasePreds(index));
            } else {
                index++;
            }
        }
        fire();
        edit.end();
        return edit;
    }
}