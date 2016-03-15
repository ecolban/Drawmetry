/*
 * Selection.java
 *
 * Created on January 29, 2007, 7:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Erik Colban
 */
public class Selection {

    private final List<DrawableEntity> hitSelection = new ArrayList<DrawableEntity>();
    private DmEntity[] movableSelection;
    private DmEntity[] copySelection;
    private DrawingModel model;
    private boolean upToDate;
    private boolean movable;
    
    void setModel(DrawingModel model) {
        this.model = model;
    }

    DrawingModel getModel() {
        return model;
    }

    List<DrawableEntity> getHits() {
        return hitSelection;
    }

    boolean isMovable() {
        if (!upToDate) {
            movableSelection = model.getSelectionCompletion(hitSelection);
            movable = movableSelection != null;
            upToDate = true;
        }
        return movable;
    }

    boolean addHit(DrawableEntity hit) {
        boolean added = false;
        if (!hitSelection.contains(hit)) {
            added = hitSelection.add(hit);
            if (added) {
                upToDate = false;
            }
        }
        return added;
    }

    void clear() {
        hitSelection.clear();
        movableSelection = null;
        upToDate = true;
        movable = false;
    }

    boolean isEmpty() {
        return hitSelection.isEmpty();
    }

    boolean contains(DrawableEntity hit) {
        return hitSelection.contains(hit);
    }

    boolean remove(DrawableEntity hit) {
        boolean removed = hitSelection.remove(hit);
        if (removed) {
            upToDate = false;
        }
        return removed;
    }

    void copy() {
        copySelection = model.copySelection(hitSelection);
    }

    void paste() {
        DmEntity[] selection2 = new DmEntity[copySelection.length];
        assert model.getUpdateLevel() == 0;
        model.beginUpdate();
        model.pasteSelection(copySelection, selection2);
        hitSelection.clear();
        for (int i = 0; i < selection2.length; i++) {
            if (selection2[i] instanceof DrawableEntity) {
                hitSelection.add((DrawableEntity) selection2[i]);
            }
        }
        upToDate = false;
        model.endUpdate();
    }

    void delete() {
        assert hitSelection != null;
        if (!hitSelection.isEmpty()) {
            Iterator<DrawableEntity> i = hitSelection.iterator();
            while (i.hasNext()) {
                model.removeEntity(i.next());
            }
        }
    }

    void initMove() {
        if (isMovable()) {
            for (int i = 0; i < movableSelection.length; i++) {
                movableSelection[i].initMove();
            }
        }
    }

    void move(double deltaX, double deltaY) {
        for (int i = 0; i < movableSelection.length; i++) {
            movableSelection[i].move(deltaX, deltaY);
        }
    }

    DrawableEntity getOneHit() {
        for (DrawableEntity e : hitSelection) {
            return e;
        }
        return null;
    }

    Iterator<DrawableEntity> drawablesIterator() {
        return hitSelection.iterator();
    }

    Rectangle2D getBounds() {
        Rectangle2D rect = null;
        DrawableEntity d;
        for (Iterator<DrawableEntity> i = hitSelection.iterator(); i.hasNext();) {
            d = i.next();
            Rectangle2D r = d.getBounds();
            if (d instanceof MCell) {
                AffineTransform at = new AffineTransform();
                ((MCell) d).getTransform(at);
                r = getOuterBounds(r, at);
            }
            if (rect == null) {
                rect = r;
            } else if (r != null) {
                rect = rect.createUnion(r);
            }
        }
        if (rect != null) {
            return rect;
        } else {
            return new Rectangle2D.Double();
        }
    }

    private Rectangle2D.Double getOuterBounds(
            Rectangle2D rect, AffineTransform tf) {
        double minX, minY, maxX, maxY;
        Point2D.Double srcPt = new Point2D.Double();
        Point2D.Double dstPt = new Point2D.Double();

        minX = Double.POSITIVE_INFINITY;
        minY = Double.POSITIVE_INFINITY;
        maxX = Double.NEGATIVE_INFINITY;
        maxY = Double.NEGATIVE_INFINITY;
        double[] verticesX = {rect.getX(), rect.getX() + rect.getWidth()};
        double[] verticesY = {rect.getY(), rect.getY() + rect.getHeight()};
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                srcPt.setLocation(verticesX[i], verticesY[j]);
                tf.transform(srcPt, dstPt);
                minX = dstPt.x < minX ? dstPt.x : minX;
                minY = dstPt.y < minY ? dstPt.y : minY;
                maxX = dstPt.x > maxX ? dstPt.x : maxX;
                maxY = dstPt.y > maxY ? dstPt.y : maxY;
            }
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    boolean getShowing() {
        for (DrawableEntity de : hitSelection) {
            if (de instanceof PointVar && !((PointVar) de).isVisible() ||
                    de instanceof MCell && !((MCell) de).isVisible()) {
                return false;
            }
        }
        return true;
    }

    void setShowMarkers(boolean b) {
        for (DrawableEntity de : hitSelection) {
            if (de instanceof PointVar) {
                ((PointVar) de).setVisible(b);
            } else if (de instanceof MCell) {
                ((MCell) de).setVisible(b);
            }
        }
    }


}



