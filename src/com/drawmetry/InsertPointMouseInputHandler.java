/*
 * InsertPointouseInputHandler.java
 *
 * Created on January 23, 2005, 11:22 AM
 */
package com.drawmetry;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.Timer;

/**
 *
 * @author Erik Colban
 */
public final class InsertPointMouseInputHandler extends StatefulMouseInputListener {

    private DrawingModel model;
    private DrawingPane drawingPane;
    Timer timer = new Timer(DrawingPane.getDoubleClickTimeout(), new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            drawingPane.resetMode();
        }
    });

    /** Creates a new instance of InsertPointState */
    InsertPointMouseInputHandler(DrawingPane drawingPane) {
        this.drawingPane = drawingPane;
        timer.setRepeats(false);
    }

    @Override
    protected void initialMousePressed(MouseEvent e) {
    }

    @Override
    protected void beforeDraggingMouseReleased(MouseEvent e) {
        timer.start();
    }

    @Override
    protected void initialMouseDragged(MouseEvent e) {
    }

    @Override
    protected void subsequentMouseDragged(MouseEvent e) {
    }

    @Override
    protected void afterDraggingMouseReleased(MouseEvent e) {
    }

    @Override
    protected void doubleClickPressed(MouseEvent e) {
        timer.stop();
        this.model = drawingPane.getModel();
        if (e.getButton() == MouseEvent.BUTTON1) {
            drawingPane.selectionClear();
            model = drawingPane.getModel();
            PointVar pv = new PointVar(
                    drawingPane.modelX(e.getX()),
                    drawingPane.modelY(e.getY()),
                    model.getPointNum());
            model.addPoint(pv);
            drawingPane.getSelection().addHit(pv);
            drawingPane.updateSelection(false);
            model.refreshDrawables();
        }
    }

    @Override
    protected void doubleClickReleased(MouseEvent e) {
    }

    @Override
    protected void enter() {
        drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void exit() {
    }
}
