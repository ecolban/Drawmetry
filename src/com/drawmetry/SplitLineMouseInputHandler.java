/*
 * SplitLineMouseInputHandler.java
 *
 * Created on June, 2007
 */
package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

import com.drawmetry.constraints.ColinearConstraint;
import com.drawmetry.constraints.MLineConstraint;
import com.drawmetry.constraints.OnLineConstraint;

/**
 *
 * @author Erik Colban
 */
public final class SplitLineMouseInputHandler extends StatefulMouseInputListener
{
    private DrawingPane drawingPane;
    private DrawingModel model;
    private DmEntity hit;
    private int cursor;
    private double halfGap = 5.0;

    /** Creates a new instance of InsertPointState */
    public SplitLineMouseInputHandler(DrawingPane drawingPane)
    {
        this.drawingPane = drawingPane;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        this.model = drawingPane.getModel();
        if ( e.getButton() == MouseEvent.BUTTON1 ) {
            drawingPane.selectionClear();
            int x = e.getX();
            int y = e.getY();
            hit = drawingPane.hitEntity(x, y);
            if ( hit != null && hit instanceof MLine ) {
                assert model.getUpdateLevel() == 0;
                model.beginUpdate();
                try {
                    splitLine((MLine) hit, drawingPane.modelX(x), drawingPane.
                            modelY(y));
                    model.endUpdate();
                    drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } catch ( ConstraintGraphException exception ) {
                    model.rollBack();
                    exception.printStackTrace();
                }
            } else {
                drawingPane.resetMode(); // Go to Select Mode

            }
            model.refreshDrawables();
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
    }

    @Override
    public void mouseMoved(MouseEvent evt)
    {
        hit = drawingPane.hitEntity(evt.getX(), evt.getY());
        if ( hit != null ) {
            if ( hit instanceof MLine ) {
                cursor = Cursor.CROSSHAIR_CURSOR;
            } else {
                cursor = Cursor.DEFAULT_CURSOR;
            }
        } else {
            cursor = Cursor.DEFAULT_CURSOR;
        }
        drawingPane.setCursor(Cursor.getPredefinedCursor(cursor));

    }

    private double getDistance(PointVar p0, PointVar p1)
    {
        return Math.sqrt(
                (p0.x - p1.x) * (p0.x - p1.x) + (p0.y - p1.y) * (p0.y - p1.y));
    }

    private void splitLine(MLine mls, double x0, double y0)
            throws ConstraintGraphException
    {
        Color color = mls.getStrokeColor();
        BasicStroke stroke = mls.getStroke();
        MLineConstraint mlc = (MLineConstraint) mls.getConstraint();
        PointVar point0, point1;
        MLine mls0;
        assert mlc != null;
        DmEntity[] endPoints = mlc.getPreds();
        model.removeEntity(mls);
        LineVar cline = new LineVar();
        model.addConstraint(new ColinearConstraint(), endPoints, cline);

        // Add the first half of a line:
        point0 = (PointVar) endPoints[0];
        point1 = new PointVar(x0, y0, model.getPointNum());
        mls0 = new MLine(color, stroke, mls.getStartArrowhead(), null);
        double d = getDistance(point0, point1);
        d = halfGap / d;
        point1.x = (point0.x - x0) * d + x0;
        point1.y = (point0.y - y0) * d + y0;
        model.addPoint(point1);
        model.addConstraint(new OnLineConstraint(), new DmEntity[]{cline}, point1);
        model.addDrawableEntity(mls0);
        model.addConstraint(new MLineConstraint(), new DmEntity[]{point0, point1}, mls0);
        
        // Add the second half of a line:
        point0 = new PointVar(x0, y0, model.getPointNum());
        point1 = (PointVar) endPoints[1];
        mls0 = new MLine(color, stroke, null, mls.getEndArrowhead());
        d = getDistance(point0, point1);
        d = halfGap / d;
        point0.x = (point1.x - x0) * d + x0;
        point0.y = (point1.y - y0) * d + y0;
        model.addPoint(point0);
        model.addConstraint(new OnLineConstraint(), new DmEntity[]{cline}, point0);
        model.addDrawableEntity(mls0);
        model.addConstraint(new MLineConstraint(), new DmEntity[]{point0, point1}, mls0);
    }

    @Override
    protected void initialMousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void beforeDraggingMouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void initialMouseDragged(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void subsequentMouseDragged(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void afterDraggingMouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void doubleClickPressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void doubleClickReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void enter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void exit() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
