package com.drawmetry;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;


public interface ViewObject {
    public boolean hits(int x, int y);
    public void draw(Graphics2D g2);
    public boolean containedIn(Rectangle2D rect);
//    static ViewObject getInstance(
//            DrawableEntity de, AffineTransform at, 
//            boolean selected, boolean fastRendering);
}
