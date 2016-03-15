/*
 * StatefulMouseAdapter.java
 *
 * Created on October 24, 2006, 9:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;

/**
 *
 * @author Erik Colban
 */
public abstract class StatefulMouseInputListener implements MouseInputListener
{
    private State currentState;
    private int doubleClickTimeout = DrawingPane.getDoubleClickTimeout();

    private interface State
    {
        void mousePressed(MouseEvent e);

        void mouseDragged(MouseEvent e);

        void mouseReleased(MouseEvent e);
    }
    private State mouseUpState = new State()
    {
        long timestamp = 0;

//        @Override
        public void mousePressed(MouseEvent e)
        {
            if ( e.getWhen() - timestamp > doubleClickTimeout ) {
                timestamp = e.getWhen();
                initialMousePressed(e);
                currentState = mouseDownState;
            } else {
                timestamp = 0;
                doubleClickPressed(e);
                currentState = doubleClickState;
            }

        }

//        @Override
        public void mouseDragged(MouseEvent e)
        {
            //ignore
        }

//        @Override
        public void mouseReleased(MouseEvent e)
        {
            //ignore
        }
    };
    private State mouseDownState = new State()
    {
//        @Override
        public void mouseReleased(MouseEvent e)
        {
            beforeDraggingMouseReleased(e);
            currentState = mouseUpState;
        }

//        @Override
        public void mouseDragged(MouseEvent e)
        {
            initialMouseDragged(e);
            currentState = mouseDraggingState;
        }

//        @Override
        public void mousePressed(MouseEvent e)
        {
            // ignore
        }
    };
    private State mouseDraggingState = new State()
    {
//        @Override
        public void mouseReleased(MouseEvent e)
        {
            afterDraggingMouseReleased(e);
            currentState = mouseUpState;
        }

//        @Override
        public void mouseDragged(MouseEvent e)
        {
            subsequentMouseDragged(e);
        }

//        @Override
        public void mousePressed(MouseEvent e)
        {
            // ignore
        }
    };
    private State doubleClickState = new State()
    {
        public void mousePressed(MouseEvent e)
        {
            // ignore
        }

        public void mouseDragged(MouseEvent e)
        {
            // ignore
        }

        public void mouseReleased(MouseEvent e)
        {
            doubleClickReleased(e);
            currentState = mouseUpState;
        }
    };

    /** Creates a new instance of InitialState */
    public StatefulMouseInputListener()
    {
        currentState = mouseUpState;
    }

    public boolean isLocked() {
        return currentState != mouseUpState;
    }

//    @Override
    public void mouseDragged(MouseEvent e)
    {
        currentState.mouseDragged(e);
    }

//    @Override
    public void mousePressed(MouseEvent e)
    {
        if ( e.getButton() == MouseEvent.BUTTON1 ) {
            currentState.mousePressed(e);
        }
    }

//    @Override
    public void mouseReleased(MouseEvent e)
    {
        if ( e.getButton() == MouseEvent.BUTTON1 ) {
            currentState.mouseReleased(e);
        }
    }

//    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

//    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

//    @Override
    public void mouseExited(MouseEvent e)
    {
    }

//    @Override
    public void mouseMoved(MouseEvent e)
    {
    }

    protected abstract void initialMousePressed(MouseEvent e);

    protected abstract void beforeDraggingMouseReleased(MouseEvent e);

    protected abstract void initialMouseDragged(MouseEvent e);

    protected abstract void subsequentMouseDragged(MouseEvent e);

    protected abstract void afterDraggingMouseReleased(MouseEvent e);

    protected abstract void doubleClickPressed(MouseEvent e);

    protected abstract void doubleClickReleased(MouseEvent e);

    protected abstract void enter();

    protected abstract void exit();
}
