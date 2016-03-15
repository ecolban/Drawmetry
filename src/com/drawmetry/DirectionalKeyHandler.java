/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author Erik
 */
public class DirectionalKeyHandler implements KeyListener {

    private final DrawingPane drawingPane;

    private enum State {

        initial, ongoing
    };
    private State currentState = State.initial;
    private double x0;
    private double y0;
    private Selection selection;

    DirectionalKeyHandler(DrawingPane drawingPane) {
        this.drawingPane = drawingPane;
        this.selection = drawingPane.getSelection();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (selection == null || selection.isEmpty()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    drawingPane.setOrigin(drawingPane.getOriginX() - 4 / drawingPane.getZoom(), drawingPane.getOriginY());
                    break;
                case KeyEvent.VK_RIGHT:
                    drawingPane.setOrigin(drawingPane.getOriginX() + 4 / drawingPane.getZoom(), drawingPane.getOriginY());
                    break;
                case KeyEvent.VK_UP:
                    drawingPane.setOrigin(drawingPane.getOriginX(), drawingPane.getOriginY() - 4 / drawingPane.getZoom());
                    break;
                case KeyEvent.VK_DOWN:
                    drawingPane.setOrigin(drawingPane.getOriginX(), drawingPane.getOriginY() + 4 / drawingPane.getZoom());
                    break;
            }
            drawingPane.repaint();
        } else { // selection is not emppty
            if (selection.isMovable()) {
                if (currentState == State.initial) {
                    selection.initMove();
                    currentState = State.ongoing;
                } else {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            selection.move(x0, y0);
                            break;
                        case KeyEvent.VK_RIGHT:
                            drawingPane.setOrigin(drawingPane.getOriginX() - 4, drawingPane.getOriginY());
                            break;
                        case KeyEvent.VK_UP:
                            drawingPane.setOrigin(drawingPane.getOriginX(), drawingPane.getOriginY() + 4);
                            break;
                        case KeyEvent.VK_DOWN:
                            drawingPane.setOrigin(drawingPane.getOriginX(), drawingPane.getOriginY() - 4);
                            break;
                    }

                }
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        currentState = State.initial;
    }
}
