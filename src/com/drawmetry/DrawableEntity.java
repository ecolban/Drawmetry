/*
 * DrawableEntity.java
 *
 * Created on September 4, 2005, 7:04 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author Erik Colban
 */
public interface DrawableEntity extends DmEntity, Cloneable
{
    public boolean hasStroke();
    
    public boolean hasFill();
    
    public Rectangle2D getBounds();

    public Color getStrokeColor();

    public UndoableEdit setStrokeColor(Color color);

    public Color getFillColor();

    public UndoableEdit setFillColor(Color color);

    public double getOpacity();

    public UndoableEdit setOpacity(double opacity);

    public BasicStroke getStroke();

    public UndoableEdit setStroke(BasicStroke stroke);

    public Arrowhead getStartArrowhead();

    public UndoableEdit setStartArrowhead(Arrowhead arrowhead);

    public Arrowhead getEndArrowhead();
    
    public UndoableEdit setEndArrowhead(Arrowhead arrowhead);
//    public double getX();
//    public double getY();
//    public void draw(Graphics2D g2);
}
