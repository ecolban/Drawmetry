package com.drawmetry;

import java.awt.Cursor;
import java.awt.event.MouseEvent;



/**
 *
 * @author  Erik Colban
 */

final class TextOutputMouseInputListener extends StatefulMouseInputListener {
    
    private PointVar selectPoint;
    private DrawableEntity selectedEntity;
    private DrawingPane drawingPane;
    private TextInputListener client;
    
    /**
     * Creates a new instance of SelectState
     */
    
    private TextOutputMouseInputListener() {
        super();
    }
    
    
    static TextOutputMouseInputListener getInstance(TextInputListener client,DrawingPane pane) {
        
        if (client == null) {
            throw new IllegalArgumentException("Argument must be non-null");
        }
        TextOutputMouseInputListener instance = new TextOutputMouseInputListener();
        instance.client = client;
        instance.drawingPane = pane;
        return instance;
    }

    public void enter() {
    }
    
    
    public void exit() {
    }
    
    @Override
    protected void initialMousePressed(MouseEvent evt) {
        selectPoint = null;
        selectedEntity = drawingPane.hitEntity(evt.getX(), evt.getY());
        if (selectedEntity != null && (selectedEntity instanceof PointVar 
                || selectedEntity instanceof MCell
                || selectedEntity instanceof MTrace
                || selectedEntity instanceof MPolyline)) {
            client.receiveText(selectedEntity.getEntityID());
        }
    }
    
    @Override
    protected void beforeDraggingMouseReleased(MouseEvent evt) {
    }
    
    @Override
    protected void initialMouseDragged(MouseEvent evt) {
    }
    
    @Override
    protected void subsequentMouseDragged(MouseEvent evt) {
    }
    
    @Override
    protected void afterDraggingMouseReleased(MouseEvent evt) {
    }
    
    @Override
    public void mouseMoved(MouseEvent evt) {
        selectPoint = drawingPane.hitPoint(evt.getX(), evt.getY(), null);
        selectedEntity = drawingPane.hitEntity(evt.getX(), evt.getY());
        if (selectPoint != null || selectedEntity != null) {
            drawingPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            drawingPane.setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    protected void doubleClickPressed(MouseEvent e) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void doubleClickReleased(MouseEvent e) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }
}




