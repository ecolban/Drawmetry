/*
 * MoveViewMouseInputHandler.java
 *
 * Created on February, 2007, 1
 *
 *
 *Use cases:
 * 1) Press, hit a point, release:
 */
package com.drawmetry;

/**
 *
 * @author  Erik Colban
 */
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.Timer;

/**
 *
 * @author  Erik Colban
 */
final class MoveViewMouseInputHandler extends StatefulMouseInputListener {

    private DrawingModel model;
    private DrawingPane drawingPane;
    private int initPointX, initPointY;
    private double x0, y0;
    Timer timer = new Timer(DrawingPane.getDoubleClickTimeout(), new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            drawingPane.resetMode();
        }
    });

    /**
     * Creates a new instance of MoveViewMouseInputHandler
     */
    MoveViewMouseInputHandler(DrawingPane drawingPane) {
        super();
        this.drawingPane = drawingPane;
        timer.setRepeats(false);
    }

    @Override
    protected void initialMousePressed(MouseEvent evt) {
        this.model = drawingPane.getModel();
        initPointX = evt.getX();
        initPointY = evt.getY();
        x0 = drawingPane.getOriginX();
        y0 = drawingPane.getOriginY();
    }

    @Override
    protected void beforeDraggingMouseReleased(MouseEvent evt) {
        timer.start();
    }

    @Override
    protected void initialMouseDragged(MouseEvent evt) {
    }

    @Override
    protected void subsequentMouseDragged(MouseEvent evt) {
        drawingPane.setOrigin(
                x0 + drawingPane.modelDX(-evt.getX() + initPointX),
                y0 + drawingPane.modelDY(-evt.getY() + initPointY));
    }

    @Override
    protected void afterDraggingMouseReleased(MouseEvent evt) {
    }

    @Override
    protected void doubleClickPressed(MouseEvent evt) {
        timer.stop();
        this.model = drawingPane.getModel();
        int w = evt.getX() - drawingPane.getWidth() / 2;
        int h = evt.getY() - drawingPane.getHeight() / 2;
        drawingPane.setOrigin(drawingPane.modelX(w), drawingPane.modelY(h)); // the origin is
        //set such as to keep the center of the drawing pane centered
        model.refreshDrawables();

    }

    @Override
    protected void doubleClickReleased(MouseEvent e) {
    }

    @Override
    protected void enter() {
        drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    @Override
    protected void exit() {
    }
}




