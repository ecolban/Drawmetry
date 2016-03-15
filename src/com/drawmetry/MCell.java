/*
 * MCell.java
 *
 * Created on May 9, 2006, 5:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import com.drawmetry.DmEntity.Type;

/**
 * This class needs major overhaul!!!
 * 
 * @author Erik
 */
class MCell implements DrawableEntity {

    private String id;
//    private boolean transformed = false;
    private AnchorVar anchor;
    private CellContent content;
//    private Color strokeColor = Color.BLACK;
    private Constraint constraint;
    private Set<Constraint> successors = new HashSet<Constraint>();
//    transient private boolean selected;
    static private int n = 0;
    //width and height of the shape
    private double west, east, south, north;
    private double west0, east0, south0, north0;
    private CellFormat format;
    private boolean hidden = false;

    MCell(String id) {
        super();
        anchor = new AnchorVar();
        content = new CellContent();
        this.id = id;
    }

    @Override
    public MCell clone() throws CloneNotSupportedException {
        MCell clone = (MCell) super.clone();
        clone.format = (CellFormat) format.clone();
        clone.constraint = null;
        clone.anchor = null;
        clone.content = null;
        clone.successors = new HashSet<Constraint>();
        return clone;
    }

    public boolean hasStroke() {
        return false;
        //TODO: Allow cells to have stroked boundary.
    }

    public boolean hasFill() {
        return false;
        //TODO: Allow cells to have fill.
    }

    public UndoableEdit setStrokeColor(Color color) {
        return null;
    }

    static public class CellFormat implements Cloneable {

        public static final int CENTER = 0;
        public static final int LEFT = 1;
        public static final int TOP = 1;
        public static final int RIGHT = 2;
        public static final int BOTTOM = 2;
        private Font font;
        private Color fontColor;
        private int horizontalAlignment;
        private int verticalAlignment;

        public CellFormat(Font font, Color fontColor, int halign, int valign) {
            this.font = font;
            this.fontColor = fontColor;
            this.horizontalAlignment = halign;
            this.verticalAlignment = valign;
        }

        @Override
        public CellFormat clone() throws CloneNotSupportedException {
            return (CellFormat) super.clone();
        }

        public Font getFont() {
            return font;
        }

        public Color getColor() {
            return fontColor;
        }

        String getHorizontalAlignmentAsString() {
            switch (getHorizontalAlignment()) {
                case LEFT:
                    return "left";
                case CENTER:
                    return "center";
                case RIGHT:
                    return "right";
                default:
                    return "left";
            }
        }

        String getVerticalAlignmentAsString() {
            switch (getVerticalAlignment()) {
                case TOP:
                    return "top";
                case CENTER:
                    return "center";
                case BOTTOM:
                    return "bottom";
                default:
                    return "top";
            }
        }

        /**
         * @return the horizontalAlignment
         */
        public int getHorizontalAlignment() {
            return horizontalAlignment;
        }

        /**
         * @return the verticalAlignment
         */
        public int getVerticalAlignment() {
            return verticalAlignment;
        }
    }

    static private class MCellRecord implements EntityRecord {

        private MCell mc;
        private int zIndex;
        private CellFormat format;
        private double[] params = new double[4];

        MCellRecord(MCell mc, int z) {
            this.mc = mc;
            this.zIndex = z;
            this.format = mc.format;
            params[0] = mc.west;
            params[1] = mc.east;
            params[2] = mc.south;
            params[3] = mc.north;

        }

//        @Override
        public DmEntity getEntity() {
            return this.mc;
        }

//        @Override
        public double[] getParams() {
            return params;
        }

//        @Override
        public int getZIndex() {
            return zIndex;
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%1$s[ %2$.2f, %3$.2f, %4$.2f, %5$.2f ]",
                getEntityID(), west, south, east - west, -north + south);
    }

//    @Override
    public String getEntityID() {
        return id;
    }

    public void setEntityID(String id) {
        this.id = id;
    }

//    @Override
    public boolean hasType(DmEntity.Type type) {
        return type == Type.ANY;

    }

//    @Override
    public Constraint getConstraint() {
        return constraint;

    }

//    @Override
    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

//    @Override
    public Set<Constraint> getSuccessors() {
        return this.successors;
    }

//    @Override
    public boolean isConstraintRequired() {
        return true;
    }

//    @Override
    public boolean isSuccessorRequired() {
        return false;
    }

//    @Override
    public void translate(double deltaX, double deltaY) {
        // This method is required by contract.
    }

//    @Override
    public void initMove() {
        getAnchor().initMove();
        west0 = west;
        east0 = east;
        south0 = south;
        north0 = north;
    }

//    @Override
    public void move(double x, double y) {
        getAnchor().move(x, y);
    }

//    @Override
    public EntityRecord makeRecord(int zIndex) {
        return new MCellRecord(this, zIndex);
    }

//    @Override
    public void setParams(EntityRecord er) {
        if (er.getEntity() == this && (er instanceof MCellRecord)) {
            MCellRecord mcr = (MCellRecord) er;
            format = mcr.format;
            west = er.getParams()[0];
            east = er.getParams()[1];
            south = er.getParams()[2];
            north = er.getParams()[3];
        }
    }

//    @Override
    public Color getStrokeColor() {
        return null;
    }

    public Color getFontColor() {
        return format.fontColor;
    }

//    public UndoableEdit setFontColor(final Color newColor) {
//        final Color oldColor = format.fontColor;
//        format.fontColor = newColor;
//        return new AbstractUndoableEdit(){
//            @Override
//            public void undo() {
//                format.fontColor = oldColor;
//            }
//
//            @Override
//            public void redo() {
//                format.fontColor = newColor;
//            }
//        };
//    }
//    @Override
    public Color getFillColor() {
        return null;
    }

//    @Override
    public UndoableEdit setFillColor(final Color newColor) {
        return null;
    }

    public double getOpacity() {
        return 0.0;
    }

//    @Override
    public UndoableEdit setOpacity(final double newValue) {
        return null;
    }

//    @Override
    public BasicStroke getStroke() {
        return null;
    }

//    @Override
    public UndoableEdit setStroke(BasicStroke stroke) {
        return null;
    }

    public int getHorizontalAlignment() {
        return format.getHorizontalAlignment();
    }

    public int getVerticalAlignment() {
        return format.getVerticalAlignment();
    }

    public void setHorizontalAlignment(String halignString) {
        if (halignString == null) {
            return;
        }
        int halign = CellFormat.CENTER;
        if (halignString.equals("left")) {
            halign = CellFormat.LEFT;
        } else if (halignString.equals("center")) {
            halign = CellFormat.CENTER;
        } else if (halignString.equals("right")) {
            halign = CellFormat.RIGHT;
        }
        format = new CellFormat(format.font, format.fontColor, halign, format.getVerticalAlignment());
        try {
            format = format.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
    }

    public void setVerticalAlignment(String valignString) {
        if (valignString == null) {
            return;
        }
        int valign = CellFormat.CENTER;
        if (valignString.equals("top")) {
            valign = CellFormat.TOP;
        } else if (valignString.equals("center")) {
            valign = CellFormat.CENTER;
        } else if (valignString.equals("bottom")) {
            valign = CellFormat.BOTTOM;
        }
        format = new CellFormat(format.font, format.fontColor, format.getHorizontalAlignment(), valign);
    }

    public AnchorVar getAnchor() {
        if (anchor == null) {
            Constraint c = getConstraint();
            if (c != null) {
//                assert c instanceof NullConstraint;
                assert c.getPreds().length == 2;
                content = (CellContent) c.getPreds()[0];
                anchor = (AnchorVar) c.getPreds()[1];
            }
        }
        return anchor;
    }

    public void getTransform(AffineTransform at) {
        getAnchor().getTransform(at);
    }

    public Font getFont() {
        return format.getFont();
    }

//    @Override
    public int dof() {
        if (getConstraint() == null) {
            return (2);
        } else {
            return (2 - getConstraint().degree());
        }
    }

    public CellContent getContent() {
        assert content != null;
        return content;

    }

    public void resize(double x, double y, int direction) {
        //Using the same integer representation of direction as used by Cursor.
        Point2D.Double srcPt = new Point2D.Double(x, y);
        Point2D.Double dstPt = getAnchor().inverseDelta(srcPt);
        x = dstPt.x;
        y = dstPt.y;
        switch (direction) {
            case Cursor.N_RESIZE_CURSOR: {
                resize(0.0, 0.0, 0.0, y);
                break;
            }
            case Cursor.S_RESIZE_CURSOR: {
                resize(0.0, 0.0, y, 0.0);
                break;
            }
            case Cursor.W_RESIZE_CURSOR: {
                resize(x, 0.0, 0.0, 0.0);
                break;
            }
            case Cursor.E_RESIZE_CURSOR: {
                resize(0.0, x, 0.0, 0.0);
                break;
            }
            case Cursor.NE_RESIZE_CURSOR: {
                resize(0.0, x, 0.0, y);
                break;
            }
            case Cursor.SW_RESIZE_CURSOR: {
                resize(x, 0.0, y, 0.0);
                break;
            }
            case Cursor.NW_RESIZE_CURSOR: {
                resize(x, 0.0, 0.0, y);
                break;
            }
            case Cursor.SE_RESIZE_CURSOR: {
                resize(0.0, x, y, 0.0);
                break;
            }
            default: {
                assert false;
            }


        }
    }

    CellFormat getFormat() {
        return format;
    }

    private void resize(double deltaW, double deltaE, double deltaS,
            double deltaN) {

        assert deltaW == 0.0 || deltaE == 0.0;
        assert deltaS == 0.0 || deltaN == 0.0;

        // direction = W: deltaLeftX
        if (deltaW != 0.0) {
            if (west0 + deltaW > east0) {
                east = west0 + deltaW;
                west = east0;
            } else {
                west = west0 + deltaW;
            }
        }

        // direction = E: deltaRightX
        if (deltaE != 0.0) {
            if (east0 + deltaE < west0) {
                west = east0 + deltaE;
                east = west0;
            } else {
                east = east0 + deltaE;
            }
        }

        // direction = S: deltaBottomY
        if (deltaS != 0.0) {
            if (south0 + deltaS < north0) {
                north = south0 + deltaS;
                south = north0;
            } else {
                south = south0 + deltaS;
            }
        }

        // direction = N: deltaTopY
        if (deltaN != 0.0) {
            if (north0 + deltaN > south0) {
                south = north0 + deltaN;
                north = south0;
            } else {
                north = north0 + deltaN;
            }
        }

    }

    public void updateBounds(double x, double y, double w, double h) {
        west = x;
        east = x + w;
        south = y + h;
        north = y;
    }

    public Rectangle2D getBounds() {
//        getAnchor();
        Rectangle2D.Double rect =
                new Rectangle2D.Double(west, north, east - west, south - north);
        //TODO
        return rect;
    }

    public void setFormat(final CellFormat format) {
        this.format = format;
//        this.strokeColor = format.getStrokeColor();
    }

//    @Override
    public Arrowhead getStartArrowhead() {
        return null;
    }

//    @Override
    public UndoableEdit setStartArrowhead(final Arrowhead newArrowhead) {

        return null;
    }

//    @Override
    public Arrowhead getEndArrowhead() {
        return null;
    }

//    @Override
    public UndoableEdit setEndArrowhead(final Arrowhead newArrowhead) {
        return null;
    }

    public boolean isVisible() {
        return !hidden;
    }

    public void setVisible(boolean b) {
        hidden = !b;
    }
}
