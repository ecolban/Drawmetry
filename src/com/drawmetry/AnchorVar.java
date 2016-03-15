/*
 * AnchorVar.java
 *
 * Created on June 24, 2007, 5:05 PM
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.drawmetry.DmEntity;
import com.drawmetry.constraints.AnchorConstraint;
import com.drawmetry.constraints.NullConstraint;

/**
 * AnchorVar implements a variable that positions an image, a cell, or a trace.
 * The positioning is done by  means of an affine transformation. This affine
 * transformation is factorized into two components: ctf and rtf. rtf is applied
 * first, then ctf, so the affine transformation is ctf * rtf.
 * <p>
 * When an AnchorVar is unconstrained, ctf is the identity transformation. When
 * a constraint is added, the identity transformation is factorized into two
 * components: A * A<sup>-1</sup>, and ctf = A and rtf = A<sup>-1</sup> * rtf.
 * When a constraint is removed, rtf = ctf * rtf and ctf = identity. The matrix
 * A is the constraint's initial transformation, which is returned
 * by {@link AnchorConstraint#getInitTransform()}
 * <p>
 * When any of the predecessors of an AnchorConstraint moves (i.e., changes
 * value), only ctf is updated. The purpose of the factorization of the affine
 * transformation into ctf * rtf is to avoid having to perform matrix inversion
 * when the predecessors of a AnchorConstraint move. Matrix inversion is
 * performed only when a constraint is added.
 *
 * @see AnchorConstraint
 *
 * @author Erik
 */
public class AnchorVar implements DmEntity {

    private AnchorConstraint constraint = null;
    private Set<Constraint> successors = new HashSet<Constraint>();
    private AffineTransform ctf = new AffineTransform();
    private AffineTransform rtf = new AffineTransform();
    private AffineTransform inverse;

    @Override
    public AnchorVar clone() throws CloneNotSupportedException {
        AnchorVar clone = (AnchorVar) super.clone();
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        clone.ctf = (AffineTransform) ctf.clone();
        clone.rtf = (AffineTransform) rtf.clone();
        if (inverse != null) {
            clone.inverse = (AffineTransform) inverse.clone();
        }
        return clone;
    }

//    @Override
    public boolean hasType(DmEntity.Type type) {
        return type == Type.ANCHOR_VAR;
    }

//    @Override
    public String getEntityID() {
        for (Constraint c : getSuccessors()) {
            if (c instanceof NullConstraint) {
                assert c.getTarget() != null;
                return c.getTarget().getEntityID();
            }
        }
        return "";
    }

//    @Override
    public Constraint getConstraint() {
        return constraint;
    }

//    @Override
    public void setConstraint(Constraint constraint) {
        if (constraint instanceof AnchorConstraint) {
            this.constraint = (AnchorConstraint) constraint;

        } else {
            this.constraint = null;
        }
    }

//    @Override
    public Set<Constraint> getSuccessors() {
        return successors;
    }

//    @Override
    public boolean isConstraintRequired() {
        return false;
    }

//    @Override
    public boolean isSuccessorRequired() {
        return true;
    }

//    @Override
    public EntityRecord makeRecord(final int zIndex) {
        final DmEntity entity = this;
        final double[] auxTf = new double[6];
        final double[] params = new double[12];
        rtf.getMatrix(auxTf);
        System.arraycopy(auxTf, 0, params, 0, 6);
        ctf.getMatrix(auxTf);
        System.arraycopy(auxTf, 0, params, 6, 6);

        return new EntityRecord() {

//            @Override
            public DmEntity getEntity() {
                return entity;
            }

//            @Override
            public double[] getParams() {
                return params;
            }

//            @Override
            public int getZIndex() {
                return zIndex;
            }
        };
    }

    public void setCtf(double a, double b, double c, double d, double x, double y) {
        if (ctf != null) {
            ctf.setTransform(a, b, c, d, x, y);
        }
    }

//    @Override
    public void setParams(EntityRecord er) {
        if (er.getEntity() == this) {
            double[] params = er.getParams();
            rtf.setTransform(
                    params[0], params[1], params[2],
                    params[3], params[4], params[5]);
            ctf.setTransform(
                    params[6], params[7], params[8],
                    params[9], params[10], params[11]);
        }
    }

//    @Override
    public void translate(double deltaX, double deltaY) {
        ctf.preConcatenate(AffineTransform.getTranslateInstance(deltaX, deltaY));
    }

//    @Override
    public void initMove() {
        if (constraint == null) {
            rtf.preConcatenate(ctf);
            ctf.setToIdentity();
        }
    }

//    @Override
    public void move(double deltaX, double deltaY) {
        if (constraint == null) {
            ctf.setToTranslation(deltaX, deltaY);
        }
    }

    /*
     * Some additional methods
     */
    void getTransform(AffineTransform at) {
        at.setTransform(ctf);
        at.concatenate(rtf);
    }

    @Override
    public String toString() {
        return "An AnchorVar";
    }

    /**
     * This method should be called just before adding an AnchorConstraint.
     * @param constraint the constraint to be added.
     * @throws NoninvertibleTransformException if the constraint's initial
     * transformation is not invertible.
     */
    public void prepareFor(AnchorConstraint constraint)
             throws NoninvertibleTransformException {
        if (constraint == null || constraint.getPreds() == null) {
            throw new IllegalArgumentException();
        }
        ctf = constraint.getInitTransform();
        rtf.preConcatenate(ctf.createInverse());
    }

    private AffineTransform matrixMult(AffineTransform a, AffineTransform b) {
        AffineTransform c = (AffineTransform) a.clone();
        c.concatenate(b);
        return c;
    }

    public boolean refreshInverse() {
        try {
            inverse = matrixMult(ctf, rtf).createInverse();
            return true;
        } catch (NoninvertibleTransformException ex) {
            inverse = null;
            return false;
        }
    }

    /**
     * This method should only be called after a call to refreshInverse() that
     * returns true. The method may be called several times if the affine
     * transform associated with the anchor has not changed.
     */
    public Point2D.Double inverseDelta(Point2D.Double srcPt) {
        assert inverse != null;
        Point2D.Double dstPt = (Point2D.Double) inverse.deltaTransform(srcPt, null);
        return dstPt;
    }

    private String displayTransform(AffineTransform a) {
        double[] d = new double[6];
        a.getMatrix(d);
        return String.format(Locale.US,
                "[ %1$4.2f, %2$4.2f, %3$4.2f, %4$4.2f, %5$4.2f, %6$4.2f ]",
                d[0], d[1], d[2], d[3], d[4], d[5]);
    }

    String display() {
        return displayTransform(ctf) + displayTransform(rtf) + " = "
                + displayTransform(matrixMult(ctf, rtf));

    }
}
