package com.drawmetry;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Erik
 */
class Polyline implements Shape
{
    private GeneralPath path;
    public int [] xpoints;
    public int [] ypoints;

    Polyline(int[] xpoints, int[] ypoints, int npoints) throws
            NegativeArraySizeException, IndexOutOfBoundsException, NullPointerException
    {
        this.xpoints = xpoints.clone();
        this.ypoints = ypoints.clone();
        if (npoints < 0) {
            throw new NegativeArraySizeException();
        }
        path = new GeneralPath();
        if (npoints > 0 ) {
            path.moveTo(xpoints[0], ypoints[0]);
        }
        for ( int i = 1; i < npoints; i++ ) {
            path.lineTo(xpoints[i], ypoints[i]);
        }
    }

    public Rectangle getBounds()
    {
        return path.getBounds();
    }

    public Rectangle2D getBounds2D()
    {
        return path.getBounds2D();
    }

    public boolean contains(double x, double y)
    {
        return path.contains(x, y);
    }

    public boolean contains(Point2D p)
    {
        return path.contains(p);
    }

    public boolean intersects(double x, double y, double w, double h)
    {
        return path.intersects(x, y, w, h);
    }

    public boolean intersects(Rectangle2D r)
    {
        return path.intersects(r);
    }

    public boolean contains(double x, double y, double w, double h)
    {
        return path.contains(x, y, w, h);
    }

    public boolean contains(Rectangle2D r)
    {
        return path.contains(r);
    }

    public PathIterator getPathIterator(AffineTransform at)
    {
        return path.getPathIterator(at);
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness)
    {
        return path.getPathIterator(at, flatness);
    }
}
