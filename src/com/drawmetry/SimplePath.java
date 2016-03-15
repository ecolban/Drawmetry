package com.drawmetry;

public interface SimplePath {

    public void reset();

    public void moveTo(double x, double y);

    public void lineTo(double x, double y);

    public void quadTo(double x1, double y1, double x2, double y2);

    public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3);

    public void close();
}
