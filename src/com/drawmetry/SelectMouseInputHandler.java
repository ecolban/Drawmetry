/*
 * SelectToolState.java
 *
 * Created on January 15, 2005, 11:33 PM
 *
 *
 *Use cases:
 * 1) Press, hit a point, release:
 */
package com.drawmetry;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

import com.drawmetry.constraints.ColinearConstraint;
import com.drawmetry.constraints.OnLineConstraint;

/**
 *
 * @author  Erik Colban
 */
final class SelectMouseInputHandler extends StatefulMouseInputListener {

    private DrawingPane drawingPane;
    private DrawingModel model;
    private Selection selection;
    private boolean selectionChanged = false;
    private boolean gotHit;
    private Rectangle selectedRect = new Rectangle();
    private PointVar focusPoint;
    private MCell focusCell;
    private MImage focusImage;
    private int initPointX, initPointY, lastPointX, lastPointY;
    private double modelX, modelY;
    private DrawableEntity hit;
    private List<DmEntity> dependents;
    private boolean selectionIsMovable;
    private int direction;
    private int cursor;
    private PointVar hitPoint;
    private DmEntity[] adjPoints;
    private boolean controlPressed = false;

    /**
     * Creates a new instance of SelectState
     */
    SelectMouseInputHandler(DrawingPane drawingPane) {
        super();
        this.drawingPane = drawingPane;
        this.selection = drawingPane.getSelection();
    }

    @Override
    public void enter() {
        drawingPane.setCursor(Cursor.getDefaultCursor());
        drawingPane.selectionClear();
    }

    @Override
    public void exit() {
    }

    @Override
    protected void initialMousePressed(MouseEvent evt) {
        if (evt.isControlDown()) {
            controlPressed = true;
            return;
        } else {
            controlPressed = false;
        }
        this.model = drawingPane.getModel();
        selectionChanged = false;
        focusPoint = null;
        focusCell = null;
        focusImage = null;
        dependents = null;
        selectionIsMovable = false;
        direction = 0;
        hit = drawingPane.hitEntity(evt.getX(), evt.getY());
        modelX = drawingPane.modelX(evt.getX());
        modelY = drawingPane.modelY(evt.getY());
        if (hit != null) {
            gotHit = true;
            if (hit instanceof PointVar) {
                focusPoint = (PointVar) hit;
                selectionIsMovable = true;
            } else if (hit instanceof MCell) {
                focusCell = ((MCell) hit);
                direction = drawingPane.edgeHit(focusCell, evt.getX(), evt.getY());
                if (selection.contains(hit) && selection.isMovable()) {
                    selectionIsMovable = true;
                }
            } else if (hit instanceof MImage) {
                focusImage = (MImage) hit;
                direction = drawingPane.edgeHit(focusImage, evt.getX(), evt.getY());
                if (selection.contains(hit) && selection.isMovable()) {
                    selectionIsMovable = true;
                }
            } else {// hit is not point nor cell.
                if (selection.contains(hit) && selection.isMovable()) {
                    selectionIsMovable = true;
                }
            }
        } else { //Nothing was hit.
            gotHit = false;
            initPointX = evt.getX();
            initPointY = evt.getY();
        }
    }

    @Override
    protected void beforeDraggingMouseReleased(MouseEvent evt) {
        if (controlPressed) {
            return;
        }
        if (gotHit) {
            if ((evt.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == 0) {
                if (!selection.isEmpty()) {
                    selectionChanged = true;
                }
                selection.clear();
            }
            if (selection.contains(hit)) {
                selectionChanged = selection.remove(hit);
            } else {
                selection.addHit(hit);
                selectionChanged = true;
            }
        } else {
            if (!selection.isEmpty()) {
                selectionChanged = true;
            }
            selection.clear();
        }
        if (selectionChanged) {
            drawingPane.updateSelection(direction == 0);
            model.refreshDrawables();
        }
    }

    @Override
    protected void initialMouseDragged(MouseEvent evt) {
        if (controlPressed) {
            return;
        }
        if (gotHit) {
            assert model.getUpdateLevel() == 0;
            model.beginUpdate();
            if (focusPoint != null) {// A point has been hit
                focusPoint.initMove();
                dependents = model.getDependents(focusPoint);
                if (drawingPane.getTraceOnEnabled()) {
                    drawingPane.traceStart(dependents);
                    // The following line is necessary because drawingPane.traceStart()
                    // adds constraints and, therefore, the dependents need to be re-
                    // computed.
                    dependents = model.getDependents(focusPoint);
                }
                if (drawingPane.hitPoint(evt.getX(), evt.getY(), dependents) != null) {
                    cursor = Cursor.CROSSHAIR_CURSOR;
                } else if (drawingPane.hitLine(evt.getX(), evt.getY(), dependents) != null) {
                    cursor = Cursor.DEFAULT_CURSOR;
                } else {
                    cursor = Cursor.HAND_CURSOR;
                }

                drawingPane.setCursor(Cursor.getPredefinedCursor(cursor));
                focusPoint.move(
                        drawingPane.modelX(evt.getX()) - modelX,
                        drawingPane.modelY(evt.getY()) - modelY);
                model.update();
            } else if (focusCell != null && direction != 0) { //A cell has been hit on the edge
                model.getDependents(focusCell);
                focusCell.getAnchor().refreshInverse();
                focusCell.initMove();
                focusCell.resize(
                        drawingPane.modelX(evt.getX()) - modelX,
                        drawingPane.modelY(evt.getY()) - modelY,
                        direction);
                model.update();
            } else if (selectionIsMovable) { //A previous selection has been hit
                selection.initMove();
                model.getDependents(selection);
                selection.move(
                        drawingPane.modelX(evt.getX()) - modelX,
                        drawingPane.modelY(evt.getY()) - modelY);
                model.update();
            } else {
                selectionIsMovable = false;
            }

        } else { // !path. Selecting a rectangle.
            lastPointX = evt.getX();
            lastPointY = evt.getY();
            drawingPane.setSelectionRectangle(selectedRect);
            selectedRect.setBounds(Math.min(initPointX, lastPointX),
                    Math.min(initPointY, lastPointY),
                    Math.abs(initPointX - lastPointX),
                    Math.abs(initPointY - lastPointY));
            drawingPane.repaint();
        }

    }

    @Override
    protected void subsequentMouseDragged(MouseEvent evt) {
        if (controlPressed) {
            return;
        }
        if (gotHit) {
            if (focusPoint != null) {
                if (drawingPane.hitPoint(evt.getX(), evt.getY(), dependents) != null) {
                    cursor = Cursor.CROSSHAIR_CURSOR;
                } else if (drawingPane.hitLine(evt.getX(), evt.getY(), dependents) != null) {
                    cursor = Cursor.DEFAULT_CURSOR;
                } else {
                    cursor = Cursor.HAND_CURSOR;
                }

                drawingPane.setCursor(Cursor.getPredefinedCursor(cursor));
                focusPoint.move(
                        drawingPane.modelX(evt.getX()) - modelX,
                        drawingPane.modelY(evt.getY()) - modelY);
                model.update();
            } else if (hit != null) {
                if (direction != 0 && focusCell != null) {
                    focusCell.resize(
                            drawingPane.modelX(evt.getX()) - modelX,
                            drawingPane.modelY(evt.getY()) - modelY,
                            direction);
                    model.update();
                } else if (selectionIsMovable) {
                    selection.move(
                            drawingPane.modelX(evt.getX()) - modelX,
                            drawingPane.modelY(evt.getY()) - modelY);
                    model.update();
                }
            }
        } else { // !path Selecting a rectangle
            lastPointX = evt.getX();
            lastPointY = evt.getY();
            selectedRect.setBounds(Math.min(initPointX, lastPointX),
                    Math.min(initPointY, lastPointY),
                    Math.abs(initPointX - lastPointX),
                    Math.abs(initPointY - lastPointY));
            drawingPane.repaint();
        }
    }

    @Override
    protected void afterDraggingMouseReleased(MouseEvent evt) {
        if (controlPressed) {
            return;
        }
        if (gotHit) {
            if (focusPoint != null) {
                drawingPane.traceStop();
                model.endUpdate();
                hitPoint = drawingPane.hitPoint(evt.getX(), evt.getY(), dependents);
                if (hitPoint != null) {
                    try {
                        model.mergeEntities(hitPoint, hit);
                    } catch (ConstraintGraphException e) {
                        drawingPane.setFeedback(e.getMessage());
                    }
                } else {
                    MPolyline mpl = drawingPane.hitLine(evt.getX(), evt.getY(), dependents);
                    if (mpl != null) {
                        assert model.getUpdateLevel() == 0;
                        model.beginUpdate();
                        adjPoints = drawingPane.getAdjacentPoints(mpl, evt.getX(), evt.getY());
                        try {
                            addColinearConstraint(adjPoints, (PointVar) hit);
                            model.endUpdate();
                        } catch (Exception e) {
                            drawingPane.setFeedback(e.getMessage());
                            model.rollBack();
                        }
                    }
                }
            } else if (selectionIsMovable) {
                model.endUpdate();
            } else if (direction != 0) {
                direction = 0;
                model.endUpdate();
            } else {
                model.endUpdate();
            }
            selection.clear();
            drawingPane.updateSelection(false);
            drawingPane.setFeedback("");
            model.disableUpdate();
        } else { //!path, Selecting a rectangle
            if (!evt.isShiftDown()) {
                if (!selection.isEmpty()) {
                    selection.clear();
                    drawingPane.updateSelection(false);
                }
            }
//            selection.getModel()
            if (drawingPane.addRectToSelection(selection, selectedRect)) {
                drawingPane.updateSelection(false);
            }
            drawingPane.setSelectionRectangle(null);
            model.refreshDrawables();
        }

    }

    @Override
    public void mouseMoved(MouseEvent evt) {
        hit = drawingPane.hitEntity(evt.getX(), evt.getY());
        modelX = drawingPane.modelX(evt.getX());
        modelY = drawingPane.modelY(evt.getY());
        if (hit != null) {
            cursor = Cursor.HAND_CURSOR;
            if (hit instanceof MCell) {
                cursor = drawingPane.edgeHit((MCell) hit, evt.getX(), evt.getY());
                if (cursor == 0) {
                    cursor = Cursor.HAND_CURSOR;
                }
            } else if (hit instanceof MImage) {
                if (drawingPane.edgeHit((MImage) hit, evt.getX(), evt.getY()) != 0) {
                    cursor = Cursor.N_RESIZE_CURSOR;
                }
            }
        } else {
            cursor = Cursor.DEFAULT_CURSOR;
        }
        drawingPane.setCursor(Cursor.getPredefinedCursor(cursor));

    }

    private void addColinearConstraint(DmEntity[] preds, PointVar cp)
            throws IllegalArgumentException, ConstraintGraphException {
        assert preds != null && cp != null;

        if (cp.dof() < 1) {
            return;
        }

        Constraint c = cp.getConstraint();
        if (c != null && c instanceof OnLineConstraint) {
            Constraint c0 = c.getPreds()[0].getConstraint();
            if (c0 != null && c0 instanceof ColinearConstraint) {
                DmEntity[] preds2 = c0.getPreds();
                if (preds[0] == preds2[0] && preds[1] == preds2[1]
                        || preds[0] == preds2[1] && preds[1] == preds2[0]) {
                    return;
                }
            }
        }
        PointVar cp2 = new PointVar();
        LineVar cline = new LineVar();
        model.addPoint(cp2);
        model.addConstraint(new ColinearConstraint(), preds, cline);
        model.addConstraint(new OnLineConstraint(), new DmEntity[]{cline}, cp2);
        model.mergeEntities(cp, cp2);
    }

    @Override
    protected void doubleClickPressed(MouseEvent e) {
    }

    @Override
    protected void doubleClickReleased(MouseEvent e) {
    }
}
