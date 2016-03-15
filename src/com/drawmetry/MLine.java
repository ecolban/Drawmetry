/*
 * PointVar.java
 *
 * Created on February 18, 2004, 10:42 PM
 */
package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

public final class MLine implements DrawableEntity
{
    private MLine entity = this;
    private Constraint constraint;
    private Set<Constraint> successors = new HashSet<Constraint>();
    private String id;
    private static int n = 0;
    private transient BasicStroke stroke;
    private Color color = Color.BLACK;
    public double startx,  starty;
    public double endx,  endy;
    public double initStartx,  initStarty;
    public double initEndx,  initEndy;
    private Arrowhead startArrowhead = null;
    private Arrowhead endArrowhead = null; // TODO: Changes to save and load file.

    /** Creates a new instance of MLine */
    public MLine(Color c, BasicStroke stroke, Arrowhead start, Arrowhead end)
    {
        this.color = c;
        this.stroke = stroke;
        this.id = "L_" + n++;
        this.startArrowhead = start;
        this.endArrowhead = end;
    }

    @Override
    public MLine clone() throws CloneNotSupportedException
    {
        MLine clone = (MLine) super.clone();
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        clone.id = "L_" + n++;
        clone.entity = clone;
        return clone;
    }

    @Override
    public String toString()
    {
        return String.format(Locale.US, "Line([%.2f, %.2f], [%.2f, %.2f])",
                startx, starty, endx, endy);
    }

//    @Override
    public boolean isConstraintRequired()
    {
        return true;
    }

//    @Override
    public boolean isSuccessorRequired()
    {
        return false;

    }

//    @Override
    public EntityRecord makeRecord(final int zIndex)
    {
        return new EntityRecord()
        {
//            @Override
            public int getZIndex()
            {
                return zIndex;
            }
            private double[] params =
                    new double[]{startx, starty, endx, endy};

//            @Override
            public MLine getEntity()
            {
                return entity;
            }

//            @Override
            public double[] getParams()
            {
                return params;
            }
        };
    }

//    @Override
    public void setParams(EntityRecord er)
    {
        if ( er.getEntity() == entity ) {
            double[] params = er.getParams();
            startx = params[0];
            starty = params[1];
            endx = params[2];
            endy = params[3];
        }
    }

//    @Override
    public boolean hasType(Type type)
    {
        return type == Type.LINE;
    }

//    @Override
    public Constraint getConstraint()
    {
        return this.constraint;
    }

//    @Override
    public void setConstraint(Constraint constraint)
    {
        this.constraint = constraint;
    }

//    @Override
    public Set<Constraint> getSuccessors()
    {
        return successors;
    }

//    @Override
    public String getEntityID()
    {
        return id;
    }

    public void setEntityID(String id)
    {
        this.id = id;
    }

//    @Override
    public Color getStrokeColor()
    {
        return color;
    }

//    @Override
    public Color getFillColor()
    {
        return null;
    }

//    @Override
    public UndoableEdit setStrokeColor(final Color newColor)
    {
        if ( newColor == null ) {
            return null;
        }
        final Color oldColor = this.color;
        this.color = newColor;
        return new AbstractUndoableEdit()
        {
            @Override
            public void undo()
            {
                super.undo();
                color = oldColor;
            }

            @Override
            public void redo()
            {
                super.redo();
                color = newColor;
            }
        };
    }

    public UndoableEdit setFillColor(Color color)
    {
        return null;
    }

    public double getX()
    {
        return 0.0;
    }

    public double getY()
    {
        return 0.0;
    }

    public void draw(Graphics2D g2)
    {
    }

//    @Override
    public void translate(double deltaX, double deltaY)
    {
        startx += deltaX;
        starty += deltaY;
        endx += deltaX;
        endy += deltaY;
    }

//    @Override
    public void move(double x, double y)
    {
        startx = initStartx + x;
        starty = initStarty + y;
        endx = initEndx + x;
        endy = initEndy + y;
    }

//    @Override
    public void initMove()
    {
        initStartx = startx;
        initStarty = starty;
        initEndx = endx;
        initEndy = endy;
    }

//    @Override
    public BasicStroke getStroke()
    {
        return stroke;
    }

//    @Override
    public UndoableEdit setStroke(final BasicStroke newStroke)
    {
        final BasicStroke oldStroke = stroke;
        stroke = newStroke;
        return new AbstractUndoableEdit()
        {
            @Override
            public void undo()
            {
                super.undo();
                stroke = oldStroke;
            }

            @Override
            public void redo()
            {
                super.redo();
                stroke = newStroke;
            }
        };
    }

//    @Override
    public int dof()
    {
        if ( getConstraint() != null ) {
            return 4 - getConstraint().degree();
        } else {
            return 4;
        }
    }

    public Rectangle2D getBounds()
    {
        double minX = Math.min(startx, endx);
        double maxX = Math.max(startx, endx);
        double minY = Math.min(starty, endy);
        double maxY = Math.max(starty, endy);

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

//    @Override
    public Arrowhead getStartArrowhead()
    {
        return startArrowhead;
    }

//    @Override
    public UndoableEdit setStartArrowhead(final Arrowhead newArrowhead)
    {

        final Arrowhead oldArrowhead = startArrowhead;
        startArrowhead = newArrowhead;
        return new AbstractUndoableEdit()
        {
            @Override
            public void undo()
            {
                super.undo();
                startArrowhead = oldArrowhead;
            }

            @Override
            public void redo()
            {
                super.redo();
                startArrowhead = newArrowhead;
            }
        };
    }

//    @Override
    public Arrowhead getEndArrowhead()
    {
        return endArrowhead;
    }

//    @Override
    public UndoableEdit setEndArrowhead(final Arrowhead newArrowhead)
    {

        final Arrowhead oldArrowhead = endArrowhead;
        endArrowhead = newArrowhead;
        return new AbstractUndoableEdit()
        {
            @Override
            public void undo()
            {
                super.undo();
                endArrowhead = oldArrowhead;
            }

            @Override
            public void redo()
            {
                super.redo();
                endArrowhead = newArrowhead;
            }
        };
    }

    public double getOpacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UndoableEdit setOpacity(double opacity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasStroke() {
        return true;
    }

    public boolean hasFill() {
        return false;
    }
}
