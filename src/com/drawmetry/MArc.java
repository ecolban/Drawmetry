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
import java.awt.geom.Arc2D;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

public final class MArc implements DrawableEntity
{
    private MArc entity = this;
    private Constraint constraint;
    private Set<Constraint> successors = new HashSet<Constraint>();
    private String id;
    private static int n = 0;
    private transient BasicStroke stroke;
    private Color strokeColor = Color.BLACK;
    private transient boolean selected;
    public double startx,  starty;
    public double midx,  midy;
    public double endx,  endy;
    private double centerx;
    private double centery;
    private double radius;
    private double direction = 1;
    public double initStartx,  initStarty;
    public double initMidx,  initMidy;
    public double initEndx,  initEndy;
    private Arrowhead startArrowhead = null;
    private Arrowhead endArrowhead = null; // TODO: Changes to save and load file.

    /** Creates a new instance of MLine */
    public MArc(Color c, BasicStroke stroke, Arrowhead start, Arrowhead end)
    {
        this.strokeColor = c;
        this.stroke = stroke;
        this.id = "L_" + n++;
        this.startArrowhead = start;
        this.endArrowhead = end;
    }

    @Override
    public MArc clone() throws CloneNotSupportedException
    {
        MArc clone = (MArc) super.clone();
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        clone.id = "A_" + n++;
        clone.entity = clone;
        return clone;
    }

    @Override
    public String toString()
    {
        return String.format(Locale.US, "arc([%.2f, %.2f], [%.2f, %.2f], [%.2f, %.2f])",
                startx, starty, midx, midy, endx, endy);
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
                    new double[]{startx, starty, midx, midy, endx, endy};

//            @Override
            public MArc getEntity()
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
            midx = params[2];
            midy = params[3];
            endx = params[4];
            endy = params[5];
        }
    }

//    @Override
    public boolean hasType(Type type)
    {
        return type == Type.ARC;
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

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public boolean getSelected()
    {
        return selected;
    }

//    @Override
    public Color getStrokeColor()
    {
        return strokeColor;
    }

//    @Override
    public UndoableEdit setStrokeColor(final Color newColor)
    {
        if ( newColor == null ) {
            return null;
        }
        final Color oldColor = this.strokeColor;
        this.strokeColor = newColor;
        return new AbstractUndoableEdit()
        {
            @Override
            public void undo()
            {
                super.undo();
                strokeColor = oldColor;
            }

            @Override
            public void redo()
            {
                super.redo();
                strokeColor = newColor;
            }
        };
    }

//    @Override
    public Color getFillColor()
    {
        return null;
    }

    public UndoableEdit setFillColor(Color color)
    {
        return null;
    }

    public double getOpacity() {
        return 1.0;
    }

    public UndoableEdit setOpacity(double opacity) {
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
        midx += deltaX;
        midy += deltaY;
        endx += deltaX;
        endy += deltaY;
    }

//    @Override
    public void move(double x, double y)
    {
        startx = initStartx + x;
        starty = initStarty + y;
        midx = initMidx + x;
        midy = initMidy + y;
        endx = initEndx + x;
        endy = initEndy + y;
    }

//    @Override
    public void initMove()
    {
        initStartx = startx;
        initStarty = starty;
        initMidx = midx;
        initMidy = midy;
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
            return 5 - getConstraint().degree();
        } else {
            return 5;
        }
    }

    public Rectangle2D getBounds()
    {
        updateCircleParams();
        if ( direction == 0.0 ) {
            return null;
        }
        double normalx = starty - endy;
        double normaly = endx - startx;
        double left, right, top, bottom;
        if ( direction *
                ((centerx - radius - startx) * normalx + (centery - starty) * normaly) > 0.0 ) {
            left = centerx - radius;
        } else {
            left = Math.min(startx, endx);
        }
        if ( direction *
                ((centerx + radius - startx) * normalx + (centery - starty) * normaly) > 0.0 ) {
            right = centerx + radius;
        } else {
            right = Math.max(startx, endx);
        }
        if ( direction *
                ((centerx - startx) * normalx + (centery - radius - starty) * normaly) > 0.0 ) {
            top = centery - radius;
        } else {
            top = Math.min(starty, endy);
        }
        if ( direction *
                ((centerx - startx) * normalx + (centery + radius - starty) * normaly) > 0 ) {
            bottom = centery + radius;
        } else {
            bottom = Math.max(starty, endy);
        }
        return new Rectangle2D.Double(left, top, right - left, bottom - top);
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

    public double [] getStartVector() {
        updateCircleParams();
        if (direction > 0) {
            return new double [] {starty - centery, centerx - startx};
        } else {
            return null;
        }
    }

    public double [] getEndVector() {
        updateCircleParams();
        if (direction > 0) {
            return new double [] {endy - centery, centerx - endx};
        } else {
            return null;
        }
    }

    private void updateCircleParams()
    {

        double a1 = midx - startx;
        double b1 = midy - starty;
        double a2 = endx - startx;
        double b2 = endy - starty;
        double delta = a1 * b2 - a2 * b1;
        if ( delta != 0 ) {
            double c1 = a1 * a1 + b1 * b1;
            double c2 = a2 * a2 + b2 * b2;
            double u1 = (c1 * b2 - c2 * b1) / (2 * delta);
            double u2 = (a1 * c2 - a2 * c1) / (2 * delta);
            centerx = startx - u1;
            centery = starty - u2;
            radius = Math.sqrt(u1 * u1 + u2 * u2);
            direction = (midx - startx) * (starty - endy) + (midy - starty) * (endx - startx);
        //direction has same sign as extent.

        } else {
            centerx = Double.NaN;
            centery = Double.NaN;
            radius = Double.POSITIVE_INFINITY;
            direction = 0;
        }
    }

    public Arc2D getArc()
    {
        updateCircleParams();
        if ( direction != 0.0 ) {
            double startAngle;
            double endAngle;
            double extent;
            startAngle = Math.toDegrees(Math.acos((startx - centerx) / radius));
            if ( starty - centery > 0 ) {
                startAngle *= -1;
            }
            endAngle = Math.toDegrees(Math.acos((endx - centerx) / radius));
            if ( endy - centery > 0 ) {
                endAngle *= -1;
            }
            extent = endAngle - startAngle;
            if ( extent * direction < 0.0 ) {
                if ( extent < 0.0 ) {
                    extent += 360;
                } else {
                    extent -= 360;
                }
            }
            return new Arc2D.Double(centerx - radius, centery - radius, 
                    2 * radius, 2 * radius, startAngle, extent, Arc2D.OPEN);
        } else {
            return null;
        }
    }

    public boolean hasStroke() {
        return true;
    }

    public boolean hasFill() {
        return true;
    }

}