package com.drawmetry;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

final class VImage implements ViewObject {

    private static Area area;
    private static AffineTransform fullTransform;
    private static AffineTransform anchorTransform = new AffineTransform();
    private static boolean selected;
    private static boolean constrained;
    private static VImage vt = new VImage();
    private static BufferedImage image;
    private static GeneralPath clippingRegion;

    private VImage() {
    }

    public static VImage getInstance(
            final MImage mi, final AffineTransform mvt, final boolean selected) {
        mi.getTransform(VImage.anchorTransform);
        VImage.area = mi.getArea().createTransformedArea(mvt);
        VImage.fullTransform = new AffineTransform(mvt);
        VImage.fullTransform.concatenate(anchorTransform);
        VImage.selected = selected;
        VImage.constrained = mi.getAnchor().getConstraint() != null;
        VImage.clippingRegion = mi.getClipPath(); //Note: clippingRegion might be null.
        if (VImage.clippingRegion != null) {
            VImage.clippingRegion.transform(mvt);
        }
        VImage.image = mi.getImage();
        return vt;
    }

//    @Override
    public boolean hits(int x, int y) {
        return area.intersects(x - 2, y - 2, 4, 4);
    }

    public int edgeHits(int x, int y) {
        return area.intersects(x - 2, y - 2, 4, 4)
                && !area.contains(x - 2, y - 2, 4, 4) ? 1 : 0;
    }

//    @Override
    public boolean containedIn(Rectangle2D rect) {
        return (rect.contains(area.getBounds()));
    }

//    @Override
    public void draw(Graphics2D g2) {
        if (clippingRegion != null) {
            g2.setClip(clippingRegion);
        }
        g2.drawImage(image, fullTransform, null);
        g2.setClip(null);
        if (selected) {
            Rectangle rect = area.getBounds();
            g2.setColor(Color.red);
            g2.draw(rect);
        }
    }
}
