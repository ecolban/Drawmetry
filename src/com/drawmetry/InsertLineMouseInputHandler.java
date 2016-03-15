/*
 * InsertLineMouseListener.java
 *
 * Created on January 23, 2005, 11:22 AM
 */
package com.drawmetry;

import java.awt.Cursor;
import java.awt.event.*;
import java.util.List;

import com.drawmetry.constraints.ColinearConstraint;
import com.drawmetry.constraints.HorizontalConstraint;
import com.drawmetry.constraints.MPolylineConstraint;
import com.drawmetry.constraints.OnLineConstraint;
import com.drawmetry.constraints.SlopeConstraint;
import com.drawmetry.constraints.VerticalConstraint;

//import java.io.*;
/**
 *
 * @author Erik Colban
 */
final class InsertLineMouseInputHandler
        extends StatefulMouseInputListener
{

    private DrawingPane drawingPane;
    private DrawingModel model;
    private int viewFirstPointX, viewFirstPointY;
    private int viewSecondPointX, viewSecondPointY;
    private PointVar firstPoint;
    private PointVar secondPoint;
    private List<DmEntity> dependents;
    private int cursor;
    private DrawableEntity hit;
    private PointVar hitPoint;
    private DmEntity[] adjPoints;
    private boolean legalHitPoint;
    private PointVar first0, last0;
    private MPolyline mpl0;
    private DmEntity[] preds0;

    /** Creates a new instance of InsertLineMouseInputHandler */
    InsertLineMouseInputHandler(DrawingPane drawingPane)
    {
        super();
        this.drawingPane = drawingPane;
    }

    protected void initialMousePressed(MouseEvent evt)
    {
        drawingPane.selectionClear();
        model = drawingPane.getModel();
        viewFirstPointX = evt.getX();
        viewFirstPointY = evt.getY();
        firstPoint = drawingPane.hitPoint(viewFirstPointX, viewFirstPointY, null);
        mpl0 = null;
        if (firstPoint != null)
        {
            mpl0 = drawingPane.hitLineWithEndpoint(firstPoint);
            DmEntity de = drawingPane.hitEntity(viewFirstPointX, viewFirstPointY);
            if (de instanceof MPolyline)
            {
                mpl0 = (MPolyline) de;
            }
        }
        if (mpl0 != null)
        {
            MPolylineConstraint c = (MPolylineConstraint) mpl0.getConstraint();
            preds0 = c.getPreds();
            first0 = (PointVar) preds0[0];
            last0 = (PointVar) preds0[preds0.length - 1];
        } else
        {
            preds0 = null;
            first0 = last0 = null;
        }
        drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    protected void beforeDraggingMouseReleased(MouseEvent e)
    {
        drawingPane.resetMode(); // Goto Select Mode.

    }

    protected void initialMouseDragged(MouseEvent evt)
    {
        // This is the first drag event
        model.beginUpdate();
        if (firstPoint == null)
        {
            firstPoint = new PointVar(
                    drawingPane.modelX(viewFirstPointX),
                    drawingPane.modelY(viewFirstPointY), model.getPointNum());
            model.addPoint(firstPoint);
            if (mpl0 != null)
            {
                adjPoints = drawingPane.getAdjacentPoints(mpl0, viewFirstPointX, viewFirstPointY);
                try
                {
                    assert adjPoints != null && firstPoint != null;
                    LineVar cline = new LineVar();
                    model.addConstraint(new ColinearConstraint(), adjPoints, cline);
                    model.addConstraint(new OnLineConstraint(), new DmEntity[]
                            {
                                cline
                            }, firstPoint);
                } catch (IllegalArgumentException ex)
                {
                    ex.printStackTrace();
                } catch (ConstraintGraphException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        assert firstPoint != null;
        setViewSecondPoint(evt); //determines viewSecondPointX and viewSecondPointY
        secondPoint = new PointVar(drawingPane.modelX(viewSecondPointX), drawingPane.modelY(viewSecondPointY), model.getPointNum());
        model.addPoint(secondPoint);
        if (firstPoint == last0)
        {
//            int pos = ((MPolylineConstraint) mpl0.getConstraint()).getNumPreds();
            int pos = preds0.length;
            try
            {
                model.addPred((MPolylineConstraint) mpl0.getConstraint(), secondPoint, pos);
            } catch (ConstraintGraphException ex)
            {
                ex.printStackTrace();
            }
            last0 = secondPoint;
            preds0 = mpl0.getConstraint().getPreds();
        } else if (firstPoint == first0)
        {
            try
            {
                model.addPred((MPolylineConstraint) mpl0.getConstraint(), secondPoint, 0);
            } catch (ConstraintGraphException ex)
            {
                ex.printStackTrace();
            }
            first0 = secondPoint;
            preds0 = mpl0.getConstraint().getPreds();
        } else
        {
            mpl0 = new MPolyline(
                    drawingPane.getStrokeColor(),
                    drawingPane.getFillColor(),
                    drawingPane.getOpacity(),
                    drawingPane.getStroke(),
                    drawingPane.getStartArrowhead(),
                    drawingPane.getEndArrowhead(),
                    drawingPane.getWindingRule());
            mpl0.setEntityID("L_" + model.getLineNum());
            mpl0.setPoints(new double[4]);
            model.addDrawableEntity(mpl0);
            MPolylineConstraint c0 = new MPolylineConstraint();
            preds0 = new PointVar[]
                    {
                        firstPoint, secondPoint
                    };
            try
            {
                model.addConstraint(c0, preds0, mpl0);
            } catch (IllegalArgumentException ex)
            {
                ex.printStackTrace();
            } catch (ConstraintGraphException ex)
            {
                ex.printStackTrace();
            }
        }
        dependents = model.getDependents(secondPoint);
        model.refreshDrawables();
    }

    protected void subsequentMouseDragged(MouseEvent evt)
    {
        setViewSecondPoint(evt);
        hitPoint = drawingPane.hitPoint(evt.getX(), evt.getY(), dependents);
        legalHitPoint = checkHitPoint(hitPoint);
        if (legalHitPoint)
        {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if (drawingPane.hitLine(evt.getX(), evt.getY(), dependents) != null)
        {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else
        {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        secondPoint.move(drawingPane.modelX(viewSecondPointX), drawingPane.modelY(viewSecondPointY));
        model.update();
    }

    protected void afterDraggingMouseReleased(MouseEvent evt)
    {
        model.endUpdate();
        if (evt.isShiftDown())
        {
            assert model.getUpdateLevel() == 0;
            model.beginUpdate();
            addSlopeConstraints(secondPoint, setViewSecondPoint(evt));
            //model.endUpdate() is called from within addSlopeConstraints()
        }
        hitPoint = drawingPane.hitPoint(evt.getX(), evt.getY(), dependents);
        legalHitPoint = checkHitPoint(hitPoint);
        if (legalHitPoint)
        {
            try
            {
                model.mergeEntities(hitPoint, secondPoint);
            } catch (ConstraintGraphException e)
            {
                drawingPane.setFeedback(e.getMessage());
            }
        } else
        {
            mpl0 = drawingPane.hitLine(evt.getX(), evt.getY(), dependents);
            if (mpl0 != null && secondPoint.dof() > 0)
            {
                adjPoints = drawingPane.getAdjacentPoints(mpl0, viewSecondPointX, viewSecondPointY);
                if (adjPoints != null)
                {
                    addLineConstraint(secondPoint, adjPoints);
                }
            }
        }
        model.disableUpdate();
        drawingPane.getSelection().addHit(mpl0);
        drawingPane.updateSelection(false);
        model.refreshDrawables();
    }

    @Override
    public void mouseMoved(MouseEvent evt)
    {
        hit = drawingPane.hitEntity(evt.getX(), evt.getY());
        if (hit != null)
        {
            if (hit instanceof MPolyline)
            {
                cursor = Cursor.DEFAULT_CURSOR;
            } else if (hit instanceof PointVar)
            {
                cursor = Cursor.CROSSHAIR_CURSOR;
            } else
            {
                cursor = Cursor.HAND_CURSOR;
            }
        } else
        {
            cursor = Cursor.HAND_CURSOR;
        }
        drawingPane.setCursor(Cursor.getPredefinedCursor(cursor));

    }

    private void addLineConstraint(PointVar point, DmEntity[] preds)
    {
        assert preds.length == 2;
        Constraint c = point.getConstraint();
        boolean needToAddColinearConstraint = true;
        if (c != null && c instanceof OnLineConstraint)
        {
            Constraint c0 = c.getPreds()[0].getConstraint();
            if (c0 != null && c0 instanceof ColinearConstraint)
            {
                DmEntity[] preds2 = c0.getPreds();
                if (preds[0] == preds2[0] && preds[1] == preds2[1] ||
                        preds[0] == preds2[1] && preds[1] == preds2[0])
                {
                    needToAddColinearConstraint = false;
                }
            }
        }
        if (needToAddColinearConstraint)
        {
            model.beginUpdate();
            try
            {
                addLinearConstraint(new ColinearConstraint(), preds, point);
                model.endUpdate();
            } catch (IllegalArgumentException ex)
            {
                model.rollBack();
                drawingPane.setFeedback(ex.getMessage());
            } catch (ConstraintGraphException ex)
            {
                model.rollBack();
                drawingPane.setFeedback(ex.getMessage());
            }
        }
    }

    private void addSlopeConstraints(PointVar secondPoint, int slope)
    {
        if (slope == 0)
        {
            try
            {
                addLinearConstraint(new HorizontalConstraint(), new DmEntity[]
                        {
                            firstPoint
                        }, secondPoint);
                model.endUpdate();
            } catch (IllegalArgumentException ex)
            {
                model.rollBack();
                drawingPane.setFeedback(ex.getMessage());
            } catch (ConstraintGraphException ex)
            {
                model.rollBack();
                drawingPane.setFeedback(ex.getMessage());
            }
        } else if (slope == -1 || slope == 1)
        {
            try
            {
                addLinearConstraint(new SlopeConstraint(), new DmEntity[]
                        {
                            firstPoint, new NumVar(slope)
                        }, secondPoint);
                model.endUpdate();
            } catch (IllegalArgumentException ex)
            {
                model.rollBack();
                drawingPane.setFeedback(ex.getMessage());
            } catch (ConstraintGraphException ex)
            {
                model.rollBack();
                drawingPane.setFeedback(ex.getMessage());
            }

        } else
        {
            try
            {
                addLinearConstraint(new VerticalConstraint(), new DmEntity[]
                        {
                            firstPoint
                        }, secondPoint);
                model.endUpdate();
            } catch (IllegalArgumentException ex)
            {
                model.rollBack();
                drawingPane.setFeedback(ex.getMessage());
            } catch (ConstraintGraphException ex)
            {
                model.rollBack();
                drawingPane.setFeedback(ex.getMessage());
            }
        }
    }

    private void addLinearConstraint(Constraint linCst, DmEntity[] preds,
            PointVar target)
            throws IllegalArgumentException, ConstraintGraphException
    {
//        assert target != null && target.getConstraint() == null;
        assert linCst != null && linCst.verifyPreds(preds);
        PointVar cp2 = new PointVar();
        LineVar cline = new LineVar();
        model.beginUpdate();
        model.addConstraint(linCst, preds, cline);
        model.addConstraint(new OnLineConstraint(), new DmEntity[]
                {
                    cline
                }, cp2);
        model.mergeEntities(target, cp2);
        model.endUpdate();
    }

    private boolean checkHitPoint(PointVar p)
    {
        if (p == null)
        {
            return false;
        }
        if (secondPoint == first0 && p == last0)
        {
            return true;
        }
        if (secondPoint == last0 && p == first0)
        {
            return true;
        }
        for (DmEntity de : preds0)
        {
            if (p == de)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * If the shift key is pressed, checks the slope between firstPoint and 
     * secondPoint. 
     * @param e a MouseEvent
     * @return If the slope is close to 0, -1, +1, or infinity, then returns the
     * slope value
     */
    private int setViewSecondPoint(MouseEvent e)
    {
        viewSecondPointX = e.getX();
        viewSecondPointY = e.getY();
        if (e.isShiftDown())
        {
            if (Math.abs(viewSecondPointY - viewFirstPointY) <
                    0.41 * Math.abs(viewSecondPointX - viewFirstPointX))
            {
                viewSecondPointY = viewFirstPointY;
                return 0;
            } else if (Math.abs(viewSecondPointY - viewFirstPointY) >
                    2.41 * Math.abs(viewSecondPointX - viewFirstPointX))
            {
                viewSecondPointX = viewFirstPointX;
                return Integer.MAX_VALUE;
            } else if (viewSecondPointY > viewFirstPointY && viewSecondPointX >
                    viewFirstPointX || viewSecondPointY < viewFirstPointY && viewSecondPointX < viewFirstPointX)
            {
                viewSecondPointY = viewFirstPointY + viewSecondPointX - viewFirstPointX;
                return -1;
            } else
            {
                viewSecondPointY = viewFirstPointY - viewSecondPointX + viewFirstPointX;
                return 1;
            }
        }
        return 0;
    }

    @Override
    protected void doubleClickPressed(MouseEvent e)
    {
    }

    @Override
    protected void doubleClickReleased(MouseEvent e)
    {
    }

    @Override
    protected void enter()
    {
        drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        first0 = last0 = null;
        mpl0 = null;

    }

    @Override
    protected void exit()
    {
    }
}
