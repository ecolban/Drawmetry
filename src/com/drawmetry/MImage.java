package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import com.drawmetry.constraints.NullConstraint;
import com.drawmetry.constraints.ShapeConstraint;

/**
 *
 * @author Erik
 */
public class MImage implements DrawableEntity {

//    private static int n;
    private String id;
    private AnchorVar anchor;
    private ShapeVar shape;
    private BufferedImage image;
    private BufferedImage filteredImage;
    private float opacity;
    private Constraint constraint;
    private Set<Constraint> successors = new HashSet<Constraint>();
    private float[] scales = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
    private final float[] offsets = new float[4];
    private String fileName;

    static private class MImageRecord implements EntityRecord {

        private MImage mImage;
        private int zIndex;

        MImageRecord(MImage mi, int z) {
            this.mImage = mi;
            this.zIndex = z;

        }

//        @Override
        public DmEntity getEntity() {
            return this.mImage;
        }

//        @Override
        public double[] getParams() {
            throw new UnsupportedOperationException("Not implemented.");
        }

//        @Override
        public int getZIndex() {
            return zIndex;
        }
    }

    private MImage() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public MImage(BufferedImage image) throws IOException {
//        id = "I_" + n++;
        this.image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = this.image.getGraphics();
        g.drawImage(image, 0, 0, null);
        filteredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        g = filteredImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        opacity = 1.0F;
    }

    @Override
    public MImage clone() throws CloneNotSupportedException {
        MImage clone = (MImage) super.clone();
        clone.filteredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = clone.filteredImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        clone.setOpacity(opacity);
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        clone.anchor = null;
        clone.shape = null;
        return clone;
    }

    public boolean hasStroke() {
        return false;
    }

    public boolean hasFill() {
        return true;
    }

    public Rectangle2D getBounds() {
        Area a = getArea();
        return a.getBounds();
    }

    public Color getStrokeColor() {
        assert false;
        return null;
    }

    public UndoableEdit setStrokeColor(Color color) {
        assert false;
        return null;
    }

    public Color getFillColor() {
        return null;
    }

    public UndoableEdit setFillColor(Color color) {
        return null;
    }

    public double getOpacity() {
        return opacity;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public UndoableEdit setOpacity(final double newOpacity) {
        final double oldOpacity = opacity;
        opacity = (float) newOpacity;
        scales[3] = opacity;
        BufferedImageOp op = new RescaleOp(scales, offsets, null);
        filteredImage = op.filter(image, null);

        return new AbstractUndoableEdit() {

            @Override
            public void undo() {
                opacity = (float) oldOpacity;
                scales[3] = opacity;
                BufferedImageOp op = new RescaleOp(scales, offsets, null);
                filteredImage = op.filter(image, null);
            }

            @Override
            public void redo() {
                opacity = (float) newOpacity;
                scales[3] = opacity;
                BufferedImageOp op = new RescaleOp(scales, offsets, null);
                filteredImage = op.filter(image, null);
            }
        };
    }

    public BasicStroke getStroke() {
        assert false;
        return null;
    }

    public UndoableEdit setStroke(BasicStroke stroke) {
        assert false;
        return null;
    }

    public Arrowhead getStartArrowhead() {
        assert false;
        return null;
    }

    public UndoableEdit setStartArrowhead(Arrowhead arrowhead) {
        assert false;
        return null;
    }

    public Arrowhead getEndArrowhead() {
        assert false;
        return null;
    }

    public UndoableEdit setEndArrowhead(Arrowhead arrowhead) {
        assert false;
        return null;
    }

    public boolean hasType(Type type) {
        return type == Type.ANY;
    }

    public String getEntityID() {
        return id;
    }

    public void setEntityID(String id) {
        this.id = id;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    public Set<Constraint> getSuccessors() {
        return successors;
    }

    public boolean isConstraintRequired() {
        return false;
    }

    public boolean isSuccessorRequired() {
        return false;
    }

    public EntityRecord makeRecord(int zIndex) {
        return new MImageRecord(this, zIndex);
    }

    public void setParams(EntityRecord er) {
    }

    public void translate(double deltaX, double deltaY) {
//        An image never gets translated. Instead, its anchor gets translated.
    }

    public void move(double deltaX, double deltaY) {
        AnchorVar a = getAnchor();
        assert a != null;
        a.move(deltaX, deltaY);
    }

    public void initMove() {
        getAnchor().initMove();
    }

    public void getTransform(AffineTransform at) {
        getAnchor().getTransform(at);
    }

    AnchorVar getAnchor() {
        if (anchor == null) {
            Constraint c = getConstraint();
            assert c != null;
            if (c != null) {
                assert c instanceof NullConstraint && c.getPreds().length == 2;
                anchor = (AnchorVar) c.getPreds()[0];
            }

        }
        assert anchor != null;
        return anchor;
    }

    ShapeVar getShape() {
        if (shape == null) {
            Constraint c = getConstraint();
            assert c != null;
            if (c != null) {
                assert c instanceof NullConstraint && c.getPreds().length == 2;
                shape = (ShapeVar) c.getPreds()[1];
            }
        }
        assert shape != null;
        return shape;
    }

    Area getArea() {
        assert image != null;
        int w = image.getWidth();
        int h = image.getHeight();
        Rectangle2D.Double r = new Rectangle2D.Double(0, 0, w, h);
        Area a = new Area(r);
        AffineTransform at = new AffineTransform();
        getTransform(at);
        a = a.createTransformedArea(at);
        GeneralPath gp = getClipPath();
        if (gp != null) {
            Area clipArea = new Area(gp);
            a.intersect(clipArea);
        }
        return a;
    }

    GeneralPath getClipPath() {
        if (shape == null) {
            shape = getShape();
        }
        assert shape != null;
        return shape.getPath();
    }

    BufferedImage getImage() {
        return filteredImage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
