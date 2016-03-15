  /*
 * InsertLineMouseListener.java
 *
 * Created on January 23, 2005, 11:22 AM
 */
package com.drawmetry;

import java.awt.Cursor;
import java.awt.event.*;

/**
 *
 * @author Erik Colban
 */
public final class InsertCellMouseInputHandler extends StatefulMouseInputListener {

    private DrawingModel model;
    private DrawingPane drawingPane;
    private int x0,  y0,  x,  y;
    private double mw,  me,  ms,  mn;
    private MCell mc;

    /** Creates a new instance of InsertPointState */
    InsertCellMouseInputHandler(DrawingPane drawingPane) {
        super();
        this.drawingPane = drawingPane;
    }

    @Override
    protected void initialMousePressed(MouseEvent evt) {
        drawingPane.selectionClear();
        model = drawingPane.getModel();
        x0 = evt.getX();
        y0 = evt.getY();
    }

    @Override
    protected void beforeDraggingMouseReleased(MouseEvent e) {
        drawingPane.resetMode(); // Goto Select Mode.
//        drawingPane.setModeCursor();
    }

    @Override
    protected void initialMouseDragged(MouseEvent evt) {
        // This is the first drag event
        x = evt.getX();
        y = evt.getY();

        if (x < x0) { // Moving W
            mw = drawingPane.modelX(x);
            me = drawingPane.modelX(x0);
        } else { // Moving E
            mw = drawingPane.modelX(x0);
            me = drawingPane.modelX(x);
        }
        if (y < y0) { // Moving N
            mn = drawingPane.modelY(y);
            ms = drawingPane.modelY(y0);
        } else { //Moving S
            ms = drawingPane.modelY(y);
            mn = drawingPane.modelY(y0);
        }

        if (x < x0 && y < y0) {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
        } else if (x < x0 && y >= y0) {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
        } else if (x >= x0 && y < y0) {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
        } else if (x >= x0 && y >= y0) {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        }

        // This is the first drag event
        mc = new MCell("C_" + model.getCellNum());
        mc.setFormat(drawingPane.getCellFormat());
        try {
            model.beginUpdate();
            model.addCell(mc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        model.getDependents(mc);
        model.refreshDrawables();
    }

    @Override
    protected void subsequentMouseDragged(MouseEvent evt) {
        x = evt.getX();
        y = evt.getY();
        if (x < x0) { // Moving W
            mw = drawingPane.modelX(x);
            me = drawingPane.modelX(x0);
        } else { // Moving E
            mw = drawingPane.modelX(x0);
            me = drawingPane.modelX(x);
        }
        if (y < y0) { // Moving N
            mn = drawingPane.modelY(y);
            ms = drawingPane.modelY(y0);
        } else { //Moving S
            ms = drawingPane.modelY(y);
            mn = drawingPane.modelY(y0);
        }

        if (x < x0 && y < y0) {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
        } else if (x < x0 && y >= y0) {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
        } else if (x >= x0 && y < y0) {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
        } else if (x >= x0 && y >= y0) {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        }

        mc.updateBounds(mw, mn, me - mw, ms - mn);
        model.update();
    }

    @Override
    protected void afterDraggingMouseReleased(MouseEvent evt) {
        model.endUpdate();
        model.disableUpdate();
//        drawingPane.setModeCursor();
    }

    @Override
    protected void doubleClickPressed(MouseEvent e) {
    }

    @Override
    protected void doubleClickReleased(MouseEvent e) {
    }

    @Override
    protected void enter() {
        
    }

    @Override
    protected void exit() {
        
    }
}
